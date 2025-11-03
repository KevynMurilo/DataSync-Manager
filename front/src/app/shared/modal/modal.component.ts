import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal.component.html',
})
export class ModalComponent {
  @Input() isVisible = false;
  @Output() onClose = new EventEmitter<void>();

  closeModal() {
    this.onClose.emit();
  }
}