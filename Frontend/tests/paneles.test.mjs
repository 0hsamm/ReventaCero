import '@angular/compiler';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { registerHooks } from 'node:module';
import test from 'node:test';
import { fileURLToPath } from 'node:url';
import { createEnvironmentInjector, runInInjectionContext } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { Subject, firstValueFrom, of } from 'rxjs';
import ts from 'typescript';

// Transpila los módulos del frontend con sus dependencias reales de Angular.
const raizFuentes = new URL('../src/', import.meta.url).href;
registerHooks({
  resolve(especificador, contexto, siguiente) {
    const relativo = contexto.parentURL?.startsWith(raizFuentes) && especificador.startsWith('.');
    return siguiente(relativo ? `${especificador}.ts` : especificador, contexto);
  },
  load(url, contexto, siguiente) {
    if (!url.startsWith(raizFuentes) || !url.endsWith('.ts')) return siguiente(url, contexto);
    const compilado = ts.transpileModule(readFileSync(fileURLToPath(url), 'utf8'), {
      compilerOptions: {
        module: ts.ModuleKind.ES2022,
        target: ts.ScriptTarget.ES2022,
        experimentalDecorators: true,
        importHelpers: true,
      },
    });
    return { format: 'module', shortCircuit: true, source: compilado.outputText };
  },
});

const { AuthService } = await import('../src/app/services/auth.service.ts');
const { ThemeService } = await import('../src/app/services/theme.service.ts');
const { ListaEsperaService } = await import('../src/app/services/lista-espera.service.ts');
const { SolicitudReventaService } = await import('../src/app/services/solicitud-reventa.service.ts');
const { PublicacionReventaService } = await import('../src/app/services/publicacion-reventa.service.ts');
const { SidebarComponent } = await import('../src/app/components/sidebar/sidebar.ts');
const { routes } = await import('../src/app/app.routes.ts');
const { sesionDesdeToken } = await import('../src/app/utils/sesion.ts');
const { Login } = await import('../src/app/components/login/login.ts');
const app = routes.find((ruta) => ruta.path === 'app');
const inicioPanel = app.children.find((ruta) => ruta.path === '');

function token(rol, adicionales = {}) {
  const datos = {
    documento: 12345678,
    nombreUsuario: 'Prueba Ángela',
    correo: 'prueba@example.com',
    tipoUsuario: rol,
    exp: Math.floor(Date.now() / 1000) + 3600,
    ...adicionales,
  };
  return `cabecera.${Buffer.from(JSON.stringify(datos)).toString('base64url')}.firma-de-prueba`;
}

function preparar(jwt, metadatos = {}, respuesta = {}, volver = null) {
  const datos = new Map(Object.entries(metadatos));
  const navegaciones = [];
  const eventos = new Subject();
  if (jwt) datos.set('token', jwt);
  globalThis.localStorage = {
    getItem: (clave) => datos.get(clave) ?? null,
    setItem: (clave, valor) => datos.set(clave, String(valor)),
    removeItem: (clave) => datos.delete(clave),
  };
  const injector = createEnvironmentInjector([
    AuthService,
    { provide: HttpClient, useValue: { post: () => of(respuesta) } },
    {
      provide: Router,
      useValue: {
        events: eventos,
        url: '/',
        createUrlTree: (partes) => partes.join('/'),
        parseUrl: (ruta) => ruta,
        navigateByUrl: (ruta) => navegaciones.push(ruta),
      },
    },
    { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => volver } } } },
    { provide: ThemeService, useValue: {} },
    { provide: ListaEsperaService, useValue: {} },
    { provide: SolicitudReventaService, useValue: {} },
    { provide: PublicacionReventaService, useValue: {} },
  ]);
  return {
    auth: injector.get(AuthService),
    datos,
    eventos,
    navegaciones,
    ejecutar: (funcion) => runInInjectionContext(injector, funcion),
  };
}

for (const [rol, destino] of [
  ['ADMINISTRADOR', '/app/admin/dashboard'],
  ['ORGANIZADOR', '/app/gestion/festivales'],
  ['USUARIO', '/eventos/1'],
]) {
  test(`${rol}: al ingresar desde un evento abre el destino adecuado`, () => {
    const contexto = preparar(token(rol), {}, {}, '/eventos/1');
    const login = contexto.ejecutar(() => new Login());
    login.irDespuesDeEntrar();
    assert.deepEqual(contexto.navegaciones, [destino]);
  });
}

test('el retorno de login rechaza direcciones externas', () => {
  for (const volver of ['//example.com', 'https://example.com', null]) {
    const contexto = preparar(token('USUARIO'), {}, {}, volver);
    const login = contexto.ejecutar(() => new Login());
    login.irDespuesDeEntrar();
    assert.deepEqual(contexto.navegaciones, ['/app/usuario/boletas']);
  }
});

// RF-10 y RF-18 a RF-22: navegación y menús acordes con los roles existentes.
for (const [rol, inicio, secciones, cantidadOpciones] of [
  ['ADMINISTRADOR', '/app/admin/dashboard', ['admin', 'gestion'], 7],
  ['ORGANIZADOR', '/app/gestion/festivales', ['gestion'], 4],
  ['USUARIO', '/app/usuario/boletas', ['usuario', 'revendedor'], 3],
]) {
  test(`${rol}: inicio, menú y permisos de cada sección`, () => {
    const contexto = preparar(token(rol));
    assert.equal(contexto.auth.rutaInicio(), inicio);
    assert.equal(contexto.ejecutar(inicioPanel.redirectTo), inicio);
    assert.equal(contexto.ejecutar(app.canActivate[0]), true);
    const sidebar = contexto.ejecutar(() => new SidebarComponent());
    assert.equal(sidebar.opciones().length, cantidadOpciones);
    for (const opcion of sidebar.opciones()) {
      assert.ok(secciones.includes(opcion.ruta.split('/')[2]));
    }
    for (const seccion of app.children.filter((ruta) => ruta.canActivate)) {
      const esperado = secciones.includes(seccion.path) ? true : inicio;
      assert.equal(contexto.ejecutar(seccion.canActivate[0]), esperado);
      assert.equal(contexto.ejecutar(seccion.canActivateChild[0]), esperado);
    }
  });
}

test('USUARIO: pasa al panel de revendedor y lo conserva en las pantallas comunes', () => {
  const contexto = preparar(token('USUARIO'));
  const sidebar = contexto.ejecutar(() => new SidebarComponent());
  const navegar = (url) => contexto.eventos.next(new NavigationEnd(1, url, url));
  const secciones = () => sidebar.opciones().map((opcion) => opcion.ruta.split('/')[2]);

  assert.equal(sidebar.rol(), 'Usuario');
  navegar('/app/revendedor/boletas');
  assert.equal(sidebar.rol(), 'Revendedor');
  assert.deepEqual([...new Set(secciones())], ['revendedor']);
  navegar('/app/perfil');
  assert.equal(sidebar.rol(), 'Revendedor');
  navegar('/app/usuario/boletas');
  assert.equal(sidebar.rol(), 'Usuario');
  assert.deepEqual([...new Set(secciones())], ['usuario']);
});

test('ORGANIZADOR: la ruta de revendedor no cambia su menú', () => {
  const contexto = preparar(token('ORGANIZADOR'));
  const sidebar = contexto.ejecutar(() => new SidebarComponent());
  contexto.eventos.next(new NavigationEnd(1, '/app/revendedor', '/app/revendedor'));
  assert.equal(sidebar.rol(), 'Organizador');
});

test('el token de administrador recupera el panel aunque los metadatos antiguos digan USUARIO', () => {
  const contexto = preparar(token('ADMINISTRADOR'), { tipoUsuario: 'USUARIO', documento: '999' });
  assert.equal(contexto.auth.sesion().tipoUsuario, 'ADMINISTRADOR');
  assert.equal(contexto.datos.get('tipoUsuario'), 'ADMINISTRADOR');
  assert.equal(contexto.datos.get('documento'), '12345678');
  assert.equal(contexto.auth.sesion().nombreUsuario, 'Prueba Ángela');
});

test('editar el rol almacenado no convierte un token de usuario en administrador', () => {
  const contexto = preparar(token('USUARIO'), { tipoUsuario: 'ADMINISTRADOR' });
  const admin = app.children.find((ruta) => ruta.path === 'admin');
  assert.equal(contexto.ejecutar(admin.canActivate[0]), '/app/usuario/boletas');
});

test('sin sesión se redirige a acceso', () => {
  const contexto = preparar(null);
  assert.equal(contexto.ejecutar(app.canActivate[0]), '/acceso');
  assert.equal(contexto.ejecutar(inicioPanel.redirectTo), '/acceso');
});

test('una sesión que vence entre pantallas se limpia al navegar a otro hijo', () => {
  const contexto = preparar(token('ADMINISTRADOR'));
  contexto.datos.set('token', token('ADMINISTRADOR', { exp: 1 }));
  assert.equal(contexto.ejecutar(app.canActivateChild[0]), '/acceso');
  assert.equal(contexto.auth.sesion(), null);
  assert.equal(contexto.datos.size, 0);
});

test('los tokens malformados, vencidos, sin expiración o con roles desconocidos no crean sesión', () => {
  for (const jwt of [
    'token-invalido',
    'a.!.c',
    token(null),
    token('REVENDEDOR'),
    token('ADMINISTRADOR', { exp: undefined }),
    token('ADMINISTRADOR', { exp: 1 }),
    token('ADMINISTRADOR', { documento: null }),
  ]) {
    assert.equal(sesionDesdeToken(jwt), null);
    assert.equal(preparar(jwt).auth.sesion(), null);
  }
});

test('el login guarda el rol e identidad del token y no metadatos contradictorios', async () => {
  const contexto = preparar(
    null,
    {},
    {
      token: token('ADMINISTRADOR'),
      tipoToken: 'Bearer',
      tipoUsuario: 'USUARIO',
      documento: 999,
    },
  );
  await firstValueFrom(contexto.auth.login({ nombreUsuario: 'prueba', contrasenia: 'ficticia' }));
  assert.equal(contexto.auth.rutaInicio(), '/app/admin/dashboard');
  assert.equal(contexto.datos.get('tipoUsuario'), 'ADMINISTRADOR');
});

test('un login con un token sin rol válido informa el problema y no conserva otra sesión', async () => {
  const contexto = preparar(token('ADMINISTRADOR'), {}, { token: token(null) });
  await assert.rejects(
    firstValueFrom(contexto.auth.login({ nombreUsuario: 'prueba', contrasenia: 'ficticia' })),
    /rol válido/,
  );
  assert.equal(contexto.auth.sesion(), null);
  assert.equal(contexto.datos.size, 0);
});
