import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DialogConstraintsComponent } from './dialog-constraints.component';

describe('DialogConstraintsComponent', () => {
  let component: DialogConstraintsComponent;
  let fixture: ComponentFixture<DialogConstraintsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DialogConstraintsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DialogConstraintsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
