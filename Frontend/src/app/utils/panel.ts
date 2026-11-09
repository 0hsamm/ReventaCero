// Los formularios se abren arriba del panel: sin esto, quedan fuera de vista
// cuando el botón que los abrió estaba más abajo en la tabla.
export function subirPanel(): void {
  document.querySelector('.panel-pane')?.scrollTo({ top: 0 });
}
