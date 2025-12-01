import {HttpEvent, HttpHandler, HttpRequest} from '@angular/common/http';
import { Injectable, Provider } from '@angular/core';
import { HttpInterceptor, HTTP_INTERCEPTORS } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Authentication } from '../services/authentication';

@Injectable()
export class jwtInterceptor implements HttpInterceptor {

  /**
   * Constructor for the JWT Interceptor.
   * @param authenticationService - The authentication service to retrieve the JWT token.
   */
  constructor(
    private authenticationService: Authentication
  ) {}

  /**
   * Intercepts HTTP requests to add a JWT token to the Authorization header if the user is logged in.
   * @param request - The outgoing HTTP request.
   * @param next - The next HTTP handler in the chain.
   * @returns An observable of the HTTP event.
   */
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let isAuthApi: boolean;

    // Determine if the request is for authentication endpoints
    isAuthApi = request.url.startsWith('login') || request.url.startsWith('register');

    // If the user is logged in and the request is not for authentication, add the JWT token
    if(this.authenticationService.isLoggedIn() && !isAuthApi) {
      let token = this.authenticationService.getToken();

      // Clone the request to add the new header
      const authReq = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });

      // Pass on the cloned request instead of the original request
      return next.handle(authReq);
    }

    // Pass on the original request if no modifications are needed
    return next.handle(request);
  }
}

// Provider to register the JWT interceptor
export const jwtInterceptorProvider: Provider = {
  provide: HTTP_INTERCEPTORS,
  useClass: jwtInterceptor,
  multi: true,
};
