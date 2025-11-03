import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { User } from '../../../core/models/user.model';
import { UserService } from '../../../core/services/user.service';
import { ModalComponent } from '../../../shared/modal/modal.component';
import { UserFormComponent } from '../user-form/user-form.component';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    ModalComponent,
    UserFormComponent
  ],
  templateUrl: './user-list.component.html'
})
export class UserListComponent implements OnInit {
  private userService = inject(UserService);
  
  users$!: Observable<User[]>;

  isModalVisible = false;

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.users$ = this.userService.findAll();
  }

  delete(id: string, email: string): void {
    if (confirm(`Tem certeza que deseja excluir o usuário "${email}"?`)) {
      this.userService.deleteById(id).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (err) => {
          alert(err.error?.message || "Não foi possível excluir este usuário.");
        }
      });
    }
  }

  openModal(): void {
    this.isModalVisible = true;
  }

  closeModal(): void {
    this.isModalVisible = false;
  }

  onFormSaved(): void {
    this.loadUsers();
    this.closeModal();
  }
}