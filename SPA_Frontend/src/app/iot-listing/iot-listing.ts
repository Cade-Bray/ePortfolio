import { Component } from '@angular/core';
import {RouterLink} from '@angular/router';
import {IoT} from '../models/iot-device';
import {DatePipe} from '@angular/common';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-iot-listing',
  imports: [
    RouterLink,
    DatePipe
  ],
  templateUrl: './iot-listing.html',
  styleUrl: './iot-listing.css',
})
export class IotListing {
  devices: IoT[];
  private pollSub?: Subscription;

  constructor() {
    this.devices = this.getDevices();
  }

  ngOnInit() {
    // Polling every 10 seconds to update device data
    this.pollSub = new Subscription();
    const poller = setInterval(() => {
      this.devices = this.getDevices();
    }, 10000);
    this.pollSub.add({ unsubscribe: () => clearInterval(poller) });
  }

  getDevices() {
    return [
      { _id: '1', name: 'Living Room', setTemp: 72.0, currentTemp: 70.0, lastChecked: new Date(), state: 'COOL' },
      { _id: '2', name: 'Bedroom', setTemp: 68.0, currentTemp: 69.5, lastChecked: new Date(), state: 'HEAT' },
      { _id: '3', name: 'Kitchen', setTemp: 75.1, currentTemp: 75.0, lastChecked: new Date(), state: 'OFF' }
    ];
  }

  setState(device: IoT) {
    if (device.state == "OFF") {
      device.state = "HEAT";
    } else if (device.state == "HEAT") {
      device.state = "COOL";
    } else {
      device.state = "OFF";
    }

    // TODO: Call backend API to update device state
  }

  increaseTemp(device: IoT) {
    device.setTemp += 0.5;

    // TODO: Call backend API to update device temperature
  }

  decreaseTemp(device: IoT) {
    device.setTemp -= 0.5;

    // TODO: Call backend API to update device temperature
  }

  registerDevice() {
    // TODO: Call backend API to register a new device
  }

}
