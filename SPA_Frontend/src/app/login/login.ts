import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from "@angular/forms";
import { Router } from '@angular/router';
import { Authentication } from '../services/authentication';
import { User } from '../models/user';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})

/**
 * Login component handles user authentication.
 * It provides a form for users to enter their email and password,
 * and manages the login process through the Authentication service.
 */
export class Login implements OnInit{
  ngOnInit(){}

  public formError: string = '';

  // User credentials for login
  credentials = {
    name: '',
    email: '',
    password: ''
  }

  /**
   * Constructor to initialize Router and Authentication service.
   * @param router
   * @param authenticationService
   */
  constructor(
    private router: Router,
    private authenticationService: Authentication
  ) {}

  /**
   * Performs the login operation using the provided credentials.
   * If login is successful, navigates to the home page.
   */
  public doLogin(): void {
    // Create a new User object with the provided credentials
    let newUser = {
      name: this.credentials.name,
      email: this.credentials.email
    } as User;

    // Call the login method of the Authentication service
    this.authenticationService.login(newUser, this.credentials.password)
      .subscribe({
        next: (res) => {
          if (res) {
            // token already saved by the authentication service
            this.router.navigate(['/home']).then(() => {
              console.info('Navigation to home page successful after login.');
            });
          } else {
            // If no token is received, set an error message
            this.formError = 'An unknown error occurred during login. Please try again.';
            console.warn('Login response did not contain a token:', res);
          }
        },
        error: (err) => {
          // On error, set the form error message
          console.warn('Login failed:', err);
          this.formError = err.error.message;
        }
      });
  }

  /**
   * Handles the login form submission.
   * Validates the input fields and initiates the login process.
   * Sets an error message if validation fails.
   */
  public onLoginSubmit(): void {
    this.formError = '';

    // Validate that email and password are provided
    if (!this.credentials.email || !this.credentials.password) {
      // Set error message for missing fields
      this.formError = 'All fields are required, please try again.';
      return;
    }

    // Clear the name field and proceed with login
    this.credentials.name = '';
    this.doLogin();
  }
}
