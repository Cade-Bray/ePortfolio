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
    this.authenticationService.login(newUser, this.credentials.password);

    // Check if the user is logged in and navigate accordingly
    if (this.authenticationService.isLoggedIn()){
      this.router.navigate(['']);
    } else {
      // Set a timer to check login status after 3 seconds
      const timer = setTimeout(() => {
        if (this.authenticationService.isLoggedIn()) {
          this.router.navigate(['']);
        }
      }, 3000);
    }
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
      this.router.navigateByUrl('#');
    } else {
      // Clear the name field and proceed with login
      this.credentials.name = ' ';
      this.doLogin();
    }
  }
}
