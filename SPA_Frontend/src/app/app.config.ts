import {
  ApplicationConfig,
  importProvidersFrom,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection
} from '@angular/core';
import {provideRouter, RouterModule} from '@angular/router';
import { routes } from './app.routes';
import { jwtInterceptorProvider } from './utils/jwt-interceptor';
import {provideHttpClient} from '@angular/common/http';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    jwtInterceptorProvider,
    importProvidersFrom(RouterModule.forRoot(routes)),
    provideHttpClient()
  ]
};
