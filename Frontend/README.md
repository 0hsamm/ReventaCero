# ReventaCero - Frontend

Aplicación Angular 21 de ReventaCero, pensada solo para PC.

```bash
npm install
npm start
```

Abre `http://localhost:4200`. El backend debe estar en `http://localhost:8080` (se cambia en
`src/environment/environment.ts`) con la base de datos encendida. Debe aceptar el origen
`http://localhost:4200`, que es el valor por defecto de `app.frontend.allowed-origins`.

Las pruebas de sesión, menús y acceso a paneles se ejecutan con `npm run test:paneles`
(Node.js 22.15 o superior). Comprueban los tres roles existentes, sesiones vencidas,
metadatos de sesión inconsistentes y accesos permitidos y denegados. La reventa
se realiza desde **Mis boletas** del rol `USUARIO`; no existe un rol `REVENDEDOR`.
