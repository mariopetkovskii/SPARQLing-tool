import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenerateDialogComponentComponent } from './generate-dialog-component.component';

describe('GenerateDialogComponentComponent', () => {
  let component: GenerateDialogComponentComponent;
  let fixture: ComponentFixture<GenerateDialogComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GenerateDialogComponentComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenerateDialogComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
