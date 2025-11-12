import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IotListing } from './iot-listing';

describe('IotListing', () => {
  let component: IotListing;
  let fixture: ComponentFixture<IotListing>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IotListing]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IotListing);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
