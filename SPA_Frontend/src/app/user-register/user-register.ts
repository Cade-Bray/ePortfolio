import { Component } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Authentication } from '../services/authentication';

@Component({
  selector: 'app-user-register',
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './user-register.html',
  styleUrl: './user-register.css',
})
export class UserRegister {
  registrationForm: FormGroup;

  constructor(
    private http: HttpClient,
    private auth: Authentication,
    private router: Router
    ) {
    this.registrationForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      name: new FormControl('', [Validators.required, Validators.minLength(2)]),
      password: new FormControl('', [Validators.required, Validators.minLength(6)]),
      confirmPassword: new FormControl('', [Validators.required, Validators.minLength(6)])
    }, {
      validators: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(form: AbstractControl): ValidationErrors | null {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    if (password && confirmPassword && password !== confirmPassword) {
      return { mismatch: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.registrationForm.valid) {
      const {email, name, password} = this.registrationForm.value;
      const normalizedEmail = email.trim().toLowerCase();
      const trimmedName = name.trim();

      // Make the http post request to the backend API to register the user
      this.http.post(`${this.auth.baseURL}/register`, {email: normalizedEmail, name: trimmedName, password}).subscribe({
        next: (response: any) => {
            console.log('Registration successful:', response);
            // Redirect to login page after successful registration
            this.router.navigate(['/login']).then(
              r => console
                .log('Navigation to login successful:', r))
                .catch(err => console.error('Navigation to login failed:', err));
        },
        error: (error: any) => {
            console.error('Registration failed:', error);
        }
      })
    }
  }
}
