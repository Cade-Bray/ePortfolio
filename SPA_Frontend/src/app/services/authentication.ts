import { Inject, Injectable } from '@angular/core';
import { BROWSER_STORAGE } from '../storage';
import { User } from '../models/user';
import { Observable, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class Authentication {

  // Globals
  public baseURL = 'http://localhost:3000/api';

  /**
   * Setup storage and service access
   * @param storage This is the injected storage service.
   * @param http This is the injected HTTP client service.
   */
  constructor(@Inject(BROWSER_STORAGE) private storage: Storage, private http: HttpClient) {}

  /**
   * Get the JWT token from storage.
   * @return The JWT token as a string, or an empty string if not found or invalid.
   */
  public getToken(): string {
    try {
      if (!this.storage || typeof this.storage.getItem !== 'function') {
        console.warn('Storage unavailable');
        return '';
      }
      const raw = this.storage.getItem('token');
      if (raw == null) { return ''; }
      const trimmed = String(raw).trim();
      if (/^(?:undefined|null|)$/i.test(trimmed)) {
        return '';
      }
      return trimmed;
    } catch (err) {
      console.warn('Error reading storage:', err);
      return '';
    }
  }

  /**
   * Save the JWT token to storage.
   * @param token
   */
  public saveToken(token: string): void {
    try {
      this.storage?.setItem('token', token);
    } catch (err) {
      console.warn('Failed to save token:', err);
    }
  }

  /**
   * Logout of the application and remove the JWT from storage.
   */
  public logout(): void {
    try {
      this.storage?.removeItem('token');
    } catch (err) {
      console.warn('Failed to remove token:', err);
    }
  }

  /**
   * Check if the user is logged in and the token is still valid.
   * @return True if the user is logged in and the token is valid, false otherwise.
   */
  public isLoggedIn(): boolean {
    const token: string = this.getToken();
    if (token) {
      // Decode the token to get its payload and check the expiration
      const payload = JSON.parse(atob(token.split('.')[1]));

      // Check if the token has expired based on the exp field of the token payload
      return payload.exp > (Date.now() / 1000);
    } else {
      return false;
    }
  }

  /**
   * This function will get the current user. This function should only be called after the calling method has checked
   * to make sure that the user isLoggedIn.
   * @return User object that contains the email and name of the user
   */
  // noinspection JSUnusedGlobalSymbols
  public getCurrentUser(): User {
    const token: string = this.getToken();

    // Decode the token to extract user information
    const {_id, email, name} = JSON.parse(atob(token.split('.')[1]));
    return {_id, email, name} as User;
  }

  /**
   * This function handles the Auth API Calls given the provided endpoint.
   * @param endpoint This is a simple string endpoint off of the base /api/ endpoint location.
   * @param user This is the user class object that contains a name and email for registration.
   * @param passwd This is the users password as plain text.
   */
  handleAuthAPICall(endpoint: string, user: User, passwd: string): Observable<{token: string, user: string}> {
    let formData = {
      name: user.name,
      email: user.email,
      password: passwd
    };

    return this.http.post<{token: string, user: string}>(`${this.baseURL}/${endpoint}`, formData);
  }

  /**
   * This function is a wrap for the API auth handler to the login end point.
   * @param user This is a user class object containing a name and email.
   * @param passwd This is the users plain text password.
   */
  login(user: User, passwd: string): Observable<{token: string, user: string}> {
    return this.handleAuthAPICall('login', user, passwd).pipe(
      tap((res) => {
        this.saveToken(res.token);
      })
    );
  }
}
