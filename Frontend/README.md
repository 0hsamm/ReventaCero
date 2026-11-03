# ReventaCero - Frontend

Aplicación Angular 21 de ReventaCero, pensada solo para PC.

```bash
npm install
npm start
```

Abre `http://localhost:4200`. La URL del backend está en `src/environment/environment.ts`.

El backend solo expone `/auth` por ahora. Las rutas de `festival.service.ts` y `boleta.service.ts`
son previstas: los paneles mostrarán un error de carga hasta que existan esos controladores.

La landing (`/`) usa contenido provisional: textos y precios en `src/app/data/contenido-landing.ts`
e imágenes en `public/img/`. Reemplázalos por los del festival real antes de publicar.
