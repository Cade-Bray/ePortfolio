import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Authentication } from '../services/authentication';
import { IoT } from '../models/iot-device';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-device-registration',
  imports: [CommonModule, FormsModule],
  templateUrl: './device-registration.html',
  styleUrl: './device-registration.css',
})
export class DeviceRegistration implements OnInit {
  device: Partial<IoT> = {
    _id: '',
    name: '',
  };
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private http: HttpClient,
    private auth: Authentication,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
    }
  }

  registerDevice(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.device._id || this.device._id.trim() === '') {
      this.errorMessage = 'Device ID is required.';
      return;
    }

    if (!this.device.name || this.device.name.trim() === '') {
      this.errorMessage = 'Device name is required.';
      return;
    }

    this.isLoading = true;
    const currentUser = this.auth.getCurrentUser();
    if (!currentUser) {
      this.errorMessage = 'User not authenticated.';
      this.isLoading = false;
      return;
    }

    const updateData = {
      name: this.device.name.trim(),
      auth_users: [currentUser._id]
    };

    this.http.put(`${this.auth.baseURL}/iot/${this.device._id}`, updateData, {
      headers: {
        Authorization: `Bearer ${this.auth.getToken()}`
      }
    }).subscribe({
      next: () => {
        this.successMessage = 'Device registered successfully!';
        this.isLoading = false;
        setTimeout(() => this.router.navigate(['/home']), 2000);
      },
      error: (error) => {
        console.error('Error registering device:', error);
        this.errorMessage = 'Failed to register device. Please try again.';
        this.isLoading = false;
      }
    });
  }
}
