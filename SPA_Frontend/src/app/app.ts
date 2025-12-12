import { Component, signal } from '@angular/core';
import {Router, RouterLink, RouterOutlet} from '@angular/router';
import {Authentication} from './services/authentication';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  constructor(public authenticate: Authentication, private router: Router) {}

  logout(): void{
    this.authenticate.logout()
    this.router.navigate(['/login']).then(
      () => {
        console.log('User logged out.')
      });
  }
}
