import { Routes } from '@angular/router';
import {Login} from './login/login';
import {IotListing} from './iot-listing/iot-listing';
import {AuthGuard} from './services/auth-guard';
import {DeviceRegistration} from './device-registration/device-registration';
import {UserRegister} from './user-register/user-register';

export const routes: Routes = [
  {path: 'register', component: UserRegister},
  {path: 'home', component: IotListing, canActivate: [AuthGuard]},
  {path: 'device-registration', component: DeviceRegistration, canActivate: [AuthGuard]},
  {path: 'login', component: Login},
  {path: '', redirectTo: 'login', pathMatch: 'full'},
  {path: '**', redirectTo: 'login'}
];
