import { Component } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return password === confirm ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-container">
      <h2>Registracija</h2>
      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <label for="username">Korisničko ime</label>
        <input id="username" formControlName="username" />
        @if (form.controls.username.touched && form.controls.username.invalid) {
          <small class="error">Korisničko ime je obavezno (3–30 karaktera).</small>
        }

        <label for="email">Email</label>
        <input id="email" type="email" formControlName="email" />
        @if (form.controls.email.touched && form.controls.email.invalid) {
          <small class="error">Unesi ispravnu email adresu.</small>
        }

        <label for="password">Lozinka</label>
        <input id="password" type="password" formControlName="password" />
        @if (form.controls.password.touched && form.controls.password.invalid) {
          <small class="error">Lozinka mora imati najmanje 8 karaktera, slovo i cifru.</small>
        }

        <label for="confirmPassword">Ponovi lozinku</label>
        <input id="confirmPassword" type="password" formControlName="confirmPassword" />
        @if (form.controls.confirmPassword.touched && form.hasError('passwordsMismatch')) {
          <small class="error">Lozinke se ne poklapaju.</small>
        }

        <button type="submit" [disabled]="form.invalid">Registruj se</button>
      </form>
      <p>Već imaš nalog? <a routerLink="/login">Prijavi se</a></p>
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
export class RegisterComponent {
  form;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.nonNullable.group(
      {
        username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
        email: ['', [Validators.required, Validators.email]],
        password: [
          '',
          [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d).+$/)],
        ],
        confirmPassword: ['', Validators.required],
      },
      { validators: passwordsMatch },
    );
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }
    console.log('Register (mock):', this.form.getRawValue());
  }
}