import {HttpClient} from '@angular/common/http';
import {Inject, Injectable} from '@angular/core';
import {BROWSER_STORAGE, Storage} from '../storage';

@Injectable({
  providedIn: 'root'
})
export class iotData {
  constructor(
    private http: HttpClient,
    @Inject(BROWSER_STORAGE) private storage: Storage
    ) {}

  /**
   * This function is a wrap for the API auth handler to the register end point.
   */
  getIoT(): void {

  }
}
