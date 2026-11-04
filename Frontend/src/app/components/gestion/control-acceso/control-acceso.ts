import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ControlAcceso } from '../../../models/modelos';
import { AdminService } from '../../../services/admin.service';
import { mensajeError } from '../../../utils/validaciones';
import { CampoComponent } from '../../campo/campo';

@Component({
  selector: 'app-control-acceso',
  imports: [DatePipe, FormsModule, CampoComponent],
  templateUrl: './control-acceso.html',
})
export class ControlAccesoComponent {
  private adminService = inject(AdminService);

  protected codigo = '';
  protected enviando = signal(false);
  protected errorCodigo = signal('');
  protected error = signal('');
  protected resultado = signal<ControlAcceso | null>(null);
  protected historial = signal<ControlAcceso[]>([]);

  protected validar(): void {
    const codigo = this.codigo.trim();

    this.error.set('');
    this.resultado.set(null);
    this.historial.set([]);

    if (!/^[1-9]\d*$/.test(codigo)) {
      this.errorCodigo.set('Escribe el código numérico de la boleta');
      return;
    }

    this.errorCodigo.set('');
    this.enviando.set(true);

    this.adminService.registrarIngreso(Number(codigo)).subscribe({
      next: (acceso) => this.mostrar(acceso),
      error: (err: HttpErrorResponse) => {
        // Un ingreso denegado llega como 403 con el mismo DTO en el cuerpo
        if (err.error?.resultado) {
          this.mostrar(err.error);
          return;
        }

        this.enviando.set(false);
        this.error.set(mensajeError(err, 'No se pudo validar la boleta'));
      },
    });
  }

  private mostrar(acceso: ControlAcceso): void {
    this.enviando.set(false);
    this.resultado.set(acceso);
    this.adminService.accesosDeBoleta(acceso.codigoBoleta).subscribe({
      next: (lista) => this.historial.set(lista),
      error: () => this.historial.set([]),
    });
  }
}
