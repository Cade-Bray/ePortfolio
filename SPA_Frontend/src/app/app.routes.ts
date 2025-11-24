import { Routes } from '@angular/router';
import {Login} from './login/login';
import {IotListing} from './iot-listing/iot-listing';

export const routes: Routes = [
  {path: 'home', component: IotListing},
  {path: 'login', component: Login},
  {path: '', redirectTo: 'login', pathMatch: 'full'},
  {path: '**', redirectTo: 'login'}
];
