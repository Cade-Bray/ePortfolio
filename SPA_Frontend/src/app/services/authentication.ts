import { Inject, Injectable } from '@angular/core';
import { BROWSER_STORAGE } from '../storage';
import { User } from '../models/user';
import { AuthResponse } from '../models/auth-response';
import {iotData} from './iot-data';

@Injectable({
  providedIn: 'root'
})
export class Authentication {
  // Setup storage and service access
  constructor(
    @Inject(BROWSER_STORAGE) private storage: Storage,
    private iotDataService: iotData
  ) {}

  // Variable to handle Authentication Responses
  authResp: AuthResponse = new AuthResponse();

  public getToken(): string {
    try {
      if (!this.storage || typeof this.storage.getItem !== 'function') {
        console.warn('Storage unavailable');
        return '';
      }
      const raw = this.storage.getItem('iot-token');
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

  // Save our token to our Storage provider.
  public saveToken(token: string): void {
    try {
      this.storage?.setItem('iot-token', token);
    } catch (err) {
      console.warn('Failed to save token:', err);
    }
  }

  // Logout of our application and remove the JWT from local storage
  public logout(): void {
    try {
      this.storage?.removeItem('iot-token');
    } catch (err) {
      console.warn('Failed to remove token:', err);
    }
  }

  // Boolean to determine if we are logged in and the token is still valid. Even if we have a token we still have to
  // re-authenticate if the token is expired
  public isLoggedIn(): boolean {
    const token: string = this.getToken();
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]));
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
  public getCurrentUser(): User {
    const token: string = this.getToken();
    const {email, name} = JSON.parse(atob(token.split('.')[1]));
    return {email, name} as User;
  }

  // Login method that leverages the login method in the trip data service because that method returns an observable, we
  // subscribe to the result and only process when an observable condition is satisfied.
  public login(user: User, passwd: string): void {
    this.iotDataService.login(user, passwd).subscribe({
      next: (value: any) => {
        if(value){
          this.authResp.token = value;
          this.saveToken(this.authResp.token);
        }
      },
      error: (error: any) => {
        console.log(`Error: ${error}`);
      }
    })
  }
}
