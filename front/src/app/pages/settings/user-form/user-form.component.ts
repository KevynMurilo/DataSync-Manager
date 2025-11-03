import { Component, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { RegisterRequest } from '../../../core/models/auth.model';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-form.component.html'
})
export class UserFormComponent {
  @Output() onSaveSuccess = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private userService = inject(UserService);

  userForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;

  constructor() {
    this.userForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  saveUser(): void {
    if (this.userForm.invalid) {
      alert('Formulário inválido');
      return;
    }
    
    this.isLoading = true;
    this.errorMessage = null;
    const dto: RegisterRequest = this.userForm.value;

    this.userService.createUser(dto).subscribe({
      next: () => {
        this.isLoading = false;
        this.onSaveSuccess.emit();
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Falha ao criar usuário.';
      }
    });
  }

  cancel(): void {
    this.onCancel.emit();
  }
}