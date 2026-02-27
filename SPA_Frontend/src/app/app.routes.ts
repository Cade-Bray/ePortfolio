import { Routes } from '@angular/router';
import {Login} from './login/login';
import {IotListing} from './iot-listing/iot-listing';
import {AuthGuard} from './services/auth-guard';
import {DeviceRegistration} from './device-registration/device-registration';
import {UserRegister} from './user-register/user-register';
import {EditDevice} from './edit-device/edit-device';

export const routes: Routes = [
  {path: 'edit-device', component: EditDevice, canActivate: [AuthGuard]},
  {path: 'register', component: UserRegister},
  {path: 'home', component: IotListing, canActivate: [AuthGuard]},
  {path: 'device-registration', component: DeviceRegistration, canActivate: [AuthGuard]},
  {path: 'login', component: Login},
  {path: '', redirectTo: 'login', pathMatch: 'full'},
  {path: '**', redirectTo: 'login'}
];
