import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ApiService} from "../api-service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-dialog-constraints',
  templateUrl: './dialog-constraints.component.html',
  styleUrls: ['./dialog-constraints.component.scss'],
  host: {
    '[class.my-dialog-class]': 'true'
  }
})
export class DialogConstraintsComponent implements OnInit{
  itemsForm: any;

  constructor(public dialogRef: MatDialogRef<DialogConstraintsComponent>,
              private fb: FormBuilder,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private apiService: ApiService,
              private router: Router) {
  }

  ngOnInit() {
    this.initForm();
  }

  initForm() {
    this.itemsForm = this.fb.group({
      comment: ['', Validators.required]
    });
  }

  cancelClick() {
    this.dialogRef.close();
  }
  get dataResource() {
    return this.data.dataResource;
  }

  randomClick(){
    console.log(this.data)
  }


}
