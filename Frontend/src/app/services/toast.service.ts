import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'info';

export interface ToastMessage {
  id: number;
  type: ToastType;
  title: string;
  message?: string;
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  readonly messages = signal<ToastMessage[]>([]);

  private nextId = 1;

  success(title: string, message?: string): void {
    this.show('success', title, message);
  }

  error(title: string, message?: string): void {
    this.show('error', title, message);
  }

  info(title: string, message?: string): void {
    this.show('info', title, message);
  }

  remove(id: number): void {
    this.messages.update((messages) => messages.filter((message) => message.id !== id));
  }

  private show(type: ToastType, title: string, message?: string): void {
    const toast: ToastMessage = { id: this.nextId++, type, title, message };

    this.messages.update((messages) => [toast, ...messages]);

    setTimeout(() => this.remove(toast.id), 4200);
  }
}
