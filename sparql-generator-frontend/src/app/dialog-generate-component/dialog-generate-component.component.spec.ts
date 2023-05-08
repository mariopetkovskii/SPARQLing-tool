import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DialogGenerateComponentComponent } from './dialog-generate-component.component';

describe('DialogGenerateComponentComponent', () => {
  let component: DialogGenerateComponentComponent;
  let fixture: ComponentFixture<DialogGenerateComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DialogGenerateComponentComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DialogGenerateComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
