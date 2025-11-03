import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-force-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './force-change-password.component.html'
})
export class ForceChangePasswordComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  changePasswordForm: FormGroup;
  errorMessage: string | null = null;
  isLoading = false;

  constructor() {
    this.changePasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.changePasswordForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    this.authService.changePassword(this.changePasswordForm.value).subscribe({
      next: (response) => {
        this.isLoading = false;
        
        // AQUI ESTÁ A CORREÇÃO:
        // O backend nos deu um novo token (Token B).
        // O authService.changePassword já salvou essa nova sessão.
        // Nós não fazemos logout. Nós vamos direto para o dashboard.
        
        alert("Senha e e-mail atualizados com sucesso!");
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Falha ao atualizar a senha. Verifique seus dados.';
      }
    });
  }
}