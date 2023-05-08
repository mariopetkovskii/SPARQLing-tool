import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, Validators} from "@angular/forms";
import {ApiService} from "../api-service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-dialog-generate-component',
  templateUrl: './dialog-generate-component.component.html',
  styleUrls: ['./dialog-generate-component.component.scss']
})
export class DialogGenerateComponentComponent implements OnInit{

  itemsForm: any;

  constructor(public dialogRef: MatDialogRef<DialogGenerateComponentComponent>,
              private fb: FormBuilder,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private apiService: ApiService,
              private router: Router) {
  }

  initForm() {
    this.itemsForm = this.fb.group({
      comment: ['', Validators.required]
    });
  }


  ngOnInit() {
    this.initForm();
  }

  submit(variableName: string){
    this.dialogRef.close(variableName);
  }
  cancelClick() {
    this.dialogRef.close();
  }
}
