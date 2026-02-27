import { Component } from '@angular/core';
import {IoT} from '../models/iot-device';
import {Router, RouterLink} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {Authentication} from '../services/authentication';

@Component({
  selector: 'app-edit-device',
  imports: [
    FormsModule,
    RouterLink
  ],
  templateUrl: './edit-device.html',
  styleUrl: './edit-device.css',
})
export class EditDevice {
  device?: IoT;

  constructor(private router: Router, private http: HttpClient, private authenticate: Authentication) {
    this.device = (history.state?.['device'] as IoT | undefined) ?? undefined;
    if (!this.device) {
      console.error('No device data found in navigation state. Redirecting to device listing.');
      this.router.navigate(['/iot-listing']).then();
    }
  }

  /**
   * Make a request to the backend to update the device information with the current state of the form.
   * @returns void
   */
  protected saveDevice() {
    this.http.put(`${this.authenticate.baseURL}/iot/${this.device?._id}`, {
      name: this.device?.name
    }, {
      headers: {
        Authorization: `Bearer ${this.authenticate.getToken()}`
      }
    }).subscribe({
      next: (res) => {
        console.info('Device updated successfully:', res);
        this.router.navigate(['/home']).then(() => {
          console.info('Navigation to home page successful after device update.');
        });
      },
      error: (error) => {
        console.error('Error updating device:', error);
        this.router.navigate(['/home']).then(() => {
          console.info('Navigation to home page successful after device update error.');
        });
      }
    });
  }
}
