import { Component, Signal } from '@angular/core';
import { IoT } from '../models/iot-device';
import { Authentication } from '../services/authentication';
import { DatePipe } from '@angular/common';
import {filter, interval, merge, Observable, startWith, Subject, switchMap} from 'rxjs';
import { HttpClient } from '@angular/common/http';
import {toSignal} from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-iot-listing',
  imports: [
    DatePipe
  ],
  templateUrl: './iot-listing.html',
  styleUrl: './iot-listing.css',
})
export class IotListing {
  devices: Signal<IoT[] | undefined>;
  private racelock: boolean = false; // adding a mutex lock to prevent race condition in the updateDevice method and poll.
  private refreshTrigger = new Subject<void>(); // Subject to trigger manual refresh of devices list

  /**
   * Constructor for IotListing component.
   */
  constructor(public authenticate: Authentication, private http: HttpClient) {
    this.devices = toSignal(
      merge(
        interval(10000),
        this.refreshTrigger
      ).pipe(
        startWith(0),
        filter(() => !this.racelock), // Only fetch devices if not currently updating
        switchMap(() => this.getDevices())
      ),
      { initialValue: [] as IoT[], requireSync: false }
    );
  }

  /**
   * Fetch the list of IoT devices.
   * @returns Array of IoT devices associated with the user as an authorized user.
   */
  getDevices(): Observable<IoT[]> {
    // Skip polling if an update is in progress to prevent race conditions
    if (this.racelock) {
      return new Observable(observer => observer.complete());
    }

    return this.http.get<IoT[]>(`${this.authenticate.baseURL}/iot`, {
      headers: {
        Authorization: `Bearer ${this.authenticate.getToken()}`
      }
    });
  }

  /**
   * Toggle the state of the IoT device between OFF, HEAT, and COOL.
   * @param device The IoT device to update
   * @returns void
   */
  setState(device: IoT) {
    if (device.state == "OFF") {
      device.state = "HEAT";
    } else if (device.state == "HEAT") {
      device.state = "COOL";
    } else {
      device.state = "OFF";
    }

    this.updateDevice(device);
  }

  /**
   * Increase the set temperature of the IoT device by 0.5 degrees.
   * @param device The IoT device to update
   * @returns void
   */
  increaseTemp(device: IoT) {
    device.setTemp += 0.5;

    this.updateDevice(device);
  }

  /**
   * Decrease the set temperature of the IoT device by 0.5 degrees.
   * @param device The IoT device to update
   * @returns void
   */
  decreaseTemp(device: IoT) {
    device.setTemp -= 0.5;

    this.updateDevice(device);
  }

  /**
   * Send updated device settings to the backend API.
   * @param device The IoT device to update
   * @returns void
   */
  updateDevice(device: IoT) {
    // Lock the update to prevent race conditions
    this.racelock = true;

    this.http.put(`${this.authenticate.baseURL}/iot/${device._id}`,
      { name: device.name, state: device.state, setTemp: device.setTemp },
      {
        headers: {
          Authorization: `Bearer ${this.authenticate.getToken()}`
        }
      }
    ).subscribe({
      next: () => {
        console.log('Device state updated successfully');
        this.racelock = false; // Unlock after successful update
        this.refreshTrigger.next();
      },
      error: (err) => {
        console.error('Error updating device state:', err);
        this.racelock = false; // Unlock on error to allow future updates
      }
    });
  }

  /**
   * Register a new IoT device for the user.
   * @returns void
   */
  registerDevice() {
    // TODO: Call backend API to register a new device
  }

}
