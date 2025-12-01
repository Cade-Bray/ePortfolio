import {HttpClient} from '@angular/common/http';
import {Inject, Injectable} from '@angular/core';
import {BROWSER_STORAGE, Storage} from '../storage';
import {User} from '../models/user';
import {Observable} from 'rxjs';
import {AuthResponse} from '../models/auth-response';
import {IoT} from '../models/iot-device';

@Injectable({
  providedIn: 'root'
})
export class iotData {
  constructor(
    private http: HttpClient,
    @Inject(BROWSER_STORAGE) private storage: Storage
    ) {}

  // Define globals
  endpoint = 'http://localhost:3000/api/spa';
  baseURL = 'http://localhost:3000/api';

  /**
   * This function handles the Auth API Calls given the provided endpoint.
   * @param endpoint This is a simple string endpoint off of the base /api/ endpoint location.
   * @param user This is the user class object that contains a name and email for registration.
   * @param passwd This is the users password as plain text.
   */
  handleAuthAPICall(endpoint: string, user: User, passwd: string): Observable<AuthResponse> {
    let formData = {
      name: user.name,
      email: user.email,
      password: passwd
    };

    return this.http.post<AuthResponse>(`${this.baseURL}/${endpoint}`, formData);
  }

  /**
   * This function is a wrap for the API auth handler to the login end point.
   * @param user This is a user class object containing a name and email.
   * @param passwd This is the users plain text password.
   */
  login(user: User, passwd: string): Observable<AuthResponse> {
    return this.handleAuthAPICall('login', user, passwd);
  }

  /**
   * This function is a wrap for the API auth handler to the register end point.
   */
  getIoT(): Observable<IoT[]> {
    return this.http.get<IoT[]>(this.endpoint);
  }
}
