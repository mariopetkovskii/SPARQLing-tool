import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomepageComponent } from './homepage/homepage.component';
import {HttpClientModule} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import { DialogConstraintsComponent } from './dialog-constraints/dialog-constraints.component';
import {MatDialogModule} from "@angular/material/dialog";
import {MatTableModule} from "@angular/material/table";
import {MatTabsModule} from "@angular/material/tabs";
import { GenerateDialogComponentComponent } from './generate-dialog-component/generate-dialog-component.component';
import { DialogGenerateComponentComponent } from './dialog-generate-component/dialog-generate-component.component';

@NgModule({
  declarations: [
    AppComponent,
    HomepageComponent,
    DialogConstraintsComponent,
    GenerateDialogComponentComponent,
    DialogGenerateComponentComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatTableModule,
    MatTabsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
