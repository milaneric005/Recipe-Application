import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-container">
      <h2>Prijava</h2>
      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <label for="usernameOrEmail">Korisničko ime ili email</label>
        <input id="usernameOrEmail" formControlName="usernameOrEmail" />
        @if (form.controls.usernameOrEmail.touched && form.controls.usernameOrEmail.hasError('required')) {
          <small class="error">Polje je obavezno.</small>
        }

        <label for="password">Lozinka</label>
        <input id="password" type="password" formControlName="password" />
        @if (form.controls.password.touched && form.controls.password.hasError('required')) {
          <small class="error">Polje je obavezno.</small>
        }

        <button type="submit" [disabled]="form.invalid">Prijavi se</button>
      </form>
      <p>Nemaš nalog? <a routerLink="/register">Registruj se</a></p>
    </div>
  `,
  styles: [`
    .auth-container { max-width: 380px; margin: 60px auto; padding: 24px; border: 1px solid #ddd; border-radius: 8px; }
    form { display: flex; flex-direction: column; gap: 6px; }
    label { margin-top: 10px; font-weight: 600; }
    input { padding: 8px; border: 1px solid #bbb; border-radius: 4px; }
    button { margin-top: 16px; padding: 10px; border: none; border-radius: 4px; background: #2e7d32; color: #fff; cursor: pointer; }
    button:disabled { background: #9e9e9e; cursor: not-allowed; }
    .error { color: #c62828; }
  `],
})
export class LoginComponent {
  form;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.nonNullable.group({
      usernameOrEmail: ['', Validators.required],
      password: ['', Validators.required],
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }
    console.log('Login (mock):', this.form.getRawValue());
  }
}