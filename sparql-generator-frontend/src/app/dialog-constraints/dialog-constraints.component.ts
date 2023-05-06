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
  filterType: string = "";
  isStringLanguage: boolean = false;
  languageString: string = "";
  stringContains: string = "";

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
    this.setXmlSchemaType(this.clickedColumn)
    this.isStringLanguage = this.isLanguage(this.clickedColumn)
  }

  cancelClick() {
    this.dialogRef.close();
  }
  get dataResource() {
    return this.data.dataResource;
  }

  get query(){
    return this.data.query
  }

  get clickedColumn(){
    return this.data.clickedColumn;
  }

  get columnName(){
    return this.data.columnName;
  }

  submitFormForLanguage(language: string, stringContains: string): void{
    let payload;
    if(language !== "" && stringContains !== ""){
      payload = {
        type: "languageAndStringContains",
        language: language,
        stringContains: stringContains
      }
    }else if(stringContains === "") {
      payload = {
        type: "language",
        language: language,
        stringContains: ""
      }
    }else {
      payload = {
        type: "stringContains",
        language: "",
        stringContains: stringContains
      }
    }
    console.log(payload)
    this.dialogRef.close(payload);
  }

  submitFormForDateSorting(dateTypeOfSort: string, stringContains: string): void{
    let payload;
    if(dateTypeOfSort !== "" && stringContains !== ""){
      payload = {
        type: "dateTypeOfSortAndStringContains",
        dateTypeOfSort: dateTypeOfSort,
        stringContains: stringContains
      }
    }else if(stringContains === "") {
      payload = {
        type: "dateTypeOfSort",
        dateTypeOfSort: dateTypeOfSort,
        stringContains: ""
      }
    }else {
      payload = {
        type: "stringContains",
        dateTypeOfSort: "",
        stringContains: stringContains
      }
    }
    console.log(payload)
    this.dialogRef.close(payload);
  }

  setXmlSchemaType(value: string): void {
    const pattern = /(.+)\^\^http:\/\/www\.w3\.org\/2001\/XMLSchema#(\w+)/;
    const match = pattern.exec(value);

    if (match !== null) {
      const dataType = match[2];
      console.log(dataType)

      switch (dataType) {
        case "dateTime":
        case "gMonthDay":
        case "date":
          this.filterType = dataType;
          break;
        case "time":
          this.filterType = dataType;
          break;
        default:
          break;
      }
    } else {
    }
  }

  isLanguage(input: string): boolean{
    const regex = /@\w{2}$/;
    return regex.test(input);
  }


}
