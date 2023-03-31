import {Component} from '@angular/core';
import {ApiService} from '../api-service';
// @ts-ignore
import {DialogPosition, MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {DialogConstraintsComponent} from "../dialog-constraints/dialog-constraints.component";
import {map} from 'rxjs/operators';
import {MatTabChangeEvent} from "@angular/material/tabs";

interface Result {
  [key: string]: any;
}
@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.scss']
})
export class HomepageComponent {
  property: any[] = [];
  isPropertyOf: any[] = [];
  showProperty = false;
  showIsPropertyOf = false;
  checkedItems: any[] = [];
  checkedItemsForQuery: any[] = [];
  loading: boolean = false;
  sparqlQueryLoading: boolean = false;
  dataResource: string = "";
  generatedQuery: string = "";
  data: string = "";
  headers: string[] = [];
  rows: string[][] = [];
  formattedResults: any;
  selectedIndex = 0;
  showTabChoose = false;
  keys: string[] = [];
  distinctChecked = false;
  clickedColumn: string = "";
  columnName: string = "";
  language: string = "";

  constructor(private apiService: ApiService,
              private dialog: MatDialog) {}

  executeSparql() {
    const payload = {
      props: this.checkedItemsForQuery,
      query: this.generatedQuery
    };
    this.apiService.executeSparql(payload)
      .pipe(
        map((response) => {
          const results: Result[] = JSON.parse(JSON.stringify(response));
          const keys = Object.keys(results[0]);

          const formattedResults = results.map((result) => {
            const formattedResult: Result = {};
            keys.forEach((key) => {
              formattedResult[key] = result[key];
            });
            return formattedResult;
          });
          this.formattedResults = formattedResults;
          this.keys = keys;

        })
      )
      .subscribe();
  }

  logColumnName(columnName: string) {
    console.log(columnName);
    console.log(this.formattedResults[0][columnName])
    this.clickedColumn = this.formattedResults[0][columnName]
    this.columnName = columnName
    this.openDialog()
  }

  onChangeDistinct(event: any){
    this.distinctChecked = event.target.checked;
    console.log(this.distinctChecked)
  }

  isUrl(value: string): boolean {
    try {
      new URL(value);
      return true;
    } catch (_) {
      return false;
    }
  }

  executeSparqlWithClearResponse(){
    const payload = {
      props: this.checkedItemsForQuery,
      query: this.generatedQuery
    };
    this.apiService.executeSparql(payload)
      .pipe(
        map((response) => {
          const results: Result[] = JSON.parse(JSON.stringify(response));
          const keys = Object.keys(results[0]);

          const formattedResults = results.map((result) => {
            const formattedResult: Result = {};
            keys.forEach((key) => {
              formattedResult[key] = result[key].split("^^")[0];
            });
            return formattedResult;
          });
          this.formattedResults = formattedResults;
          this.keys = keys;

        })
      )
      .subscribe();
  }

  showTableQueryExecute(){
    if(this.formattedResults > 0)
      return true;
    return false;
  }

  showPropertyTable() {
    this.showProperty = true;
    this.showIsPropertyOf = false;
    this.checkedItems.length = 0;
  }

  showIsPropertyOfTable() {
    this.showProperty = false;
    this.showIsPropertyOf = true;
    this.checkedItems.length = 0;
  }

  onCheckChange(event: any, item: any) {
    if (event.target.checked) {
      this.checkedItems.push({ property: item.property, ontology: item.ontology });
      this.checkedItemsForQuery.push({ property: item.property, ontology: item.ontology });
    } else {
      const index = this.checkedItems.findIndex((x) => x.property === item.property && x.ontology === item.ontology);
      if (index !== -1) {
        this.checkedItems.splice(index, 1);
        this.checkedItemsForQuery.splice(index, 1);
      }
    }
  }

  generateSparql(limitValue: string) {
    const num = Number(limitValue);
    let propertyType;
    if(this.showProperty)
      propertyType = "property"
    else
      propertyType = "isPropertyOf"
    const payload = {
      dataResource: this.dataResource,
      props: this.checkedItems,
      propertyType: propertyType,
      limit: num,
      selectDistinct: this.distinctChecked
    }
    this.sparqlQueryLoading = true;
    this.apiService.generateSparql(payload).subscribe((data: any) => {
      this.generatedQuery = data.query;
      this.sparqlQueryLoading = false;
      localStorage.setItem("query", this.generatedQuery)
    });
  }

  generateDynamicSparql(limitValue: string){
    const num = Number(limitValue);
    let propertyType;
    if(this.showProperty)
      propertyType = "property"
    else
      propertyType = "isPropertyOf"
    const payload = {
      query: localStorage.getItem("query"),
      dataResource: this.dataResource,
      props: this.checkedItemsForQuery,
      propertyType: propertyType,
      limit: num,
      selectDistinct: this.distinctChecked
    }
    this.sparqlQueryLoading = true;
    this.apiService.generateDynamicSparql(payload).subscribe((data: any) => {
      this.generatedQuery = data.query;
      this.sparqlQueryLoading = false;
      localStorage.setItem("query", this.generatedQuery)
    });
  }

  generateSparqlWithLabels() {
    console.log(this.checkedItems)
    const payload = {
      dataResource: this.dataResource,
      props: this.checkedItems
    }
    this.sparqlQueryLoading = true;
    this.apiService.generateSparqlWithLabels(payload).subscribe((data: any) => {
      this.generatedQuery = data.query;
      this.sparqlQueryLoading = false;
    });
  }

  getOntology(input: string){
    input = input.replace(/\s/g, '_');
    input = input.toLowerCase().replace(/(^|\s)\S/g, (letter: string) => letter.toUpperCase());
    console.log(input)
    this.loading = true;
    this.apiService.getOntology(input).subscribe((data: any) => {
      this.property = data.property;
      this.isPropertyOf = data.isPropertyOf;
      this.showProperty = true;
      this.loading = false;
      this.dataResource = input;
      this.showTabChoose = true;
    });
  }

  getOntologyWithPage(input: string){
    this.loading = true;
    this.apiService.getOntologyWithPage(input).subscribe((data: any) => {
      localStorage.setItem("query", this.generatedQuery)
      this.property = data.property;
      this.isPropertyOf = data.isPropertyOf;
      this.showProperty = true;
      this.loading = false;
      this.dataResource = input;
      this.checkedItems.length = 0;

    });
  }

  onTabChange(event: MatTabChangeEvent) {
    const index = event.index;
    this.selectedIndex = event.index;
    if (index === 0) {
      // call the showProperty function
      this.showPropertyTable();
    } else if (index === 1) {
      // call the showIsPropertyOf function
      this.showIsPropertyOfTable();
    }
  }

  isLanguageFilterPresent(generatedQuery: string, columnName: string): boolean {
    const languageFilterString = "filter(langMatches(lang(?" + columnName + "),\"";
    return generatedQuery.includes(languageFilterString);
  }


  changeQueryToFilterByLang(language: string) {
    const languageFilterString = "filter(langMatches(lang(?" + this.columnName + "),\"";
    if (this.isLanguageFilterPresent(this.generatedQuery, this.columnName)) {
      const startIndex = this.generatedQuery.indexOf(languageFilterString) + languageFilterString.length;
      const endIndex = this.generatedQuery.indexOf("\")", startIndex);
      const currentLanguage = this.generatedQuery.slice(startIndex, endIndex);
      this.generatedQuery = this.generatedQuery.replace(currentLanguage, language);
    } else {
      const languageAppend = languageFilterString + language + "\")) ";
      this.generatedQuery = this.generatedQuery.replace(/\}\s*$/, `${languageAppend}}`);
    }
  }

  openDialog(){
    const dialogConfig = new MatDialogConfig();
    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.width = '50%';
    dialogConfig.panelClass = 'my-dialog-class';
    const payload = {
      dataResource: this.dataResource,
      props: this.checkedItems,
      clickedColumn: this.clickedColumn,
      query: this.generatedQuery,
      columnName: this.columnName
    }
    dialogConfig.data = payload;
    const dialogRef = this.dialog.open(DialogConstraintsComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      this.language = result;
      this.changeQueryToFilterByLang(this.language)
    })
  }

}
