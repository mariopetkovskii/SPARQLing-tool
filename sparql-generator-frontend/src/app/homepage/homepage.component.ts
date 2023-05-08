import {Component} from '@angular/core';
import {ApiService} from '../api-service';
// @ts-ignore
import {DialogPosition, MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {DialogConstraintsComponent} from "../dialog-constraints/dialog-constraints.component";
import {map} from 'rxjs/operators';
import {MatTabChangeEvent} from "@angular/material/tabs";
import {DialogGenerateComponentComponent} from "../dialog-generate-component/dialog-generate-component.component";

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
  stringConstraint: string = "";
  dateTypeOfSort: string = "";
  lastDateTypeOfSort: string = "";
  map: Map<string, string> = new Map<string, string>();
  variableName: string = "";
  unionQueryColumnClicked: string = "";
  isNewUrl: boolean = false;
  listOfUnionQueries: any[] = [];
  unionQueriesFlag: boolean = false;
  firstOntologyFlag: boolean = false;
  saveInput: string = "";

  constructor(private apiService: ApiService,
              private dialog: MatDialog) {
  }

  unionQueryFlagChange(){
    this.unionQueriesFlag = !this.unionQueriesFlag;
  }

  saveUnionQuery(){
    this.listOfUnionQueries.push(this.generatedQuery)
    this.generatedQuery = "";
    this.dataResource = "";
    this.checkedItems.length = 0;
    this.sparqlQueryLoading = true;
    this.sparqlQueryLoading = false;
    localStorage.removeItem("query");
    this.checkedItemsForQuery.length = 0;
    this.unionQueryColumnClicked = "";
    this.isNewUrl = false;
    this.getOntology(this.saveInput)
  }

  executeSparql(input: string) {
    const payload = {
      query: input
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
      .subscribe(
        () => {
          this.checkedItemsForQuery.length = 0;
          this.isNewUrl = false;
        }
      );
  }

  logColumnName(columnName: string) {
    console.log(columnName);
    console.log(this.formattedResults[0][columnName])
    this.clickedColumn = this.formattedResults[0][columnName]
    this.columnName = columnName
    this.openDialog()
  }

  onChangeDistinct(event: any) {
    this.distinctChecked = event.target.checked;
  }

  isUrl(value: string): boolean {
    try {
      new URL(value);
      return true;
    } catch (_) {
      return false;
    }
  }

  executeSparqlWithClearResponse() {
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

  showTableQueryExecute() {
    if (this.formattedResults > 0)
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
      this.checkedItems.push({property: item.property, ontology: item.ontology});
      this.checkedItemsForQuery.push({property: item.property, ontology: item.ontology});
    } else {
      const index = this.checkedItems.findIndex((x) => x.property === item.property && x.ontology === item.ontology);
      if (index !== -1) {
        this.checkedItems.splice(index, 1);
        this.checkedItemsForQuery.splice(index, 1);
      }
    }
  }

  onShowColumn(event: any, item: any) {
    if (event.target.checked) {
      this.keys.push(item);
    } else {
      const index = this.keys.findIndex(obj => obj === item);
      if (index !== -1) {
        this.keys.splice(index, 1);
      }
    }
  }

  generateUnionSparql(){
    const payload = {
      queries: this.listOfUnionQueries
    }
    this.apiService.generateUnionQuery(payload).subscribe((data: any) => {
      this.generatedQuery = data.query;
      this.sparqlQueryLoading = false;
      localStorage.setItem("query", this.generatedQuery);
      this.checkedItems.length = 0;
      this.checkedItemsForQuery.length = 0;
    });
  }

  generateSparql(limitValue: string, variableValue: string) {
    const num = Number(limitValue);
    let propertyType;
    if (this.showProperty)
      propertyType = "property"
    else
      propertyType = "isPropertyOf"
    const payload = {
      dataResource: this.dataResource,
      props: this.checkedItems,
      propertyType: propertyType,
      limit: num,
      selectDistinct: this.distinctChecked,
      variableName: variableValue
    }
    this.sparqlQueryLoading = true;
    this.apiService.generateSparql(payload).subscribe((data: any) => {
      this.generatedQuery = data.query;
      this.sparqlQueryLoading = false;
      localStorage.setItem("query", this.generatedQuery);
      this.checkedItems.length = 0;
      this.checkedItemsForQuery.length = 0;
    });
  }

  generateDynamicSparql(limitValue: string, variableValue: string) {
    const num = Number(limitValue);
    let propertyType;
    if (this.showProperty)
      propertyType = "property"
    else
      propertyType = "isPropertyOf"
    const payload = {
      query: localStorage.getItem("query"),
      dataResource: this.dataResource,
      props: this.checkedItemsForQuery,
      propertyType: propertyType,
      limit: num,
      selectDistinct: this.distinctChecked,
      variableName: variableValue,
      columnClicked: this.unionQueryColumnClicked,
      isNewUrl: this.isNewUrl
    }
    this.sparqlQueryLoading = true;
    this.apiService.generateDynamicSparql(payload).subscribe((data: any) => {
      this.generatedQuery = data.query;
      this.sparqlQueryLoading = false;
      localStorage.setItem("query", this.generatedQuery)
      this.checkedItemsForQuery.length = 0;
    });
  }

  getOntology(input: string) {
    this.saveInput = input;
    input = input.replace(/\s/g, '_');
    input = input.toLowerCase().replace(/(^|\s)\S/g, (letter: string) => letter.toUpperCase());
    this.loading = true;
    this.apiService.getOntology(input).subscribe((data: any) => {
      this.property = data.property;
      this.isPropertyOf = data.isPropertyOf;
      this.showProperty = true;
      this.showIsPropertyOf = false;
      this.firstOntologyFlag = true;
      this.loading = false;
      this.dataResource = input;
      this.showTabChoose = true;
    });
  }

  getOntologyWithPage(input: string, columnName: string) {
    this.loading = true;
    this.isNewUrl = true;
    this.apiService.getOntologyWithPage(input).subscribe((data: any) => {
      localStorage.setItem("query", this.generatedQuery)
      this.property = data.property;
      this.isPropertyOf = data.isPropertyOf;
      this.showIsPropertyOf = false;
      this.showProperty = true;
      this.loading = false;
      this.dataResource = input;
      this.checkedItems.length = 0;
      // @ts-ignore
      this.map.set("<" + input + ">", "?" + columnName)
      this.unionQueryColumnClicked = columnName
    });
  }

  onTabChange(event: MatTabChangeEvent) {
    const index = event.index;
    this.selectedIndex = event.index;
    if (index === 0) {
      this.showPropertyTable();
    } else if (index === 1) {
      this.showIsPropertyOfTable();
    }
  }

  isLanguageFilterPresent(generatedQuery: string, columnName: string): boolean {
    const languageFilterString = "filter(langMatches(lang(?" + columnName + "),\"";
    return generatedQuery.includes(languageFilterString);
  }

  isStringConstraintPresent(generatedQuery: string, columnName: string): boolean {
    const languageFilterString = "FILTER regex(str(?" + columnName;
    return generatedQuery.includes(languageFilterString);
  }

  isDateSortFilterPresent(generatedQuery: string, columnName: string): boolean {
    const dateSortFilter = "ORDER BY";
    return generatedQuery.includes(dateSortFilter);
  }


  swapMapKeyAndValue(): string {
    this.map.forEach((value, key) => {
      this.generatedQuery = this.generatedQuery.replace(key, value)
    })
    return this.generatedQuery
  }

  swapUrlWithItsVariable(){
    this.map.forEach((value, key) => {
      this.generatedQuery = this.generatedQuery.replace(key, value)
    })
  }

  changeQueryToFilterByLang(language: string) {
    const languageFilterString = "filter(langMatches(lang(?" + this.columnName + "),\"";
    if (this.isLanguageFilterPresent(this.generatedQuery, this.columnName)) {
      const startIndex = this.generatedQuery.indexOf(languageFilterString) + languageFilterString.length;
      console.log(startIndex)
      const endIndex = this.generatedQuery.indexOf("\")", startIndex);
      const currentLanguage = this.generatedQuery.slice(startIndex, endIndex);
      this.generatedQuery = this.generatedQuery.replace(currentLanguage, language);
    } else {
      const languageAppend = languageFilterString + language + "\")) ";
      this.generatedQuery = this.generatedQuery.replace(/\}\s*$/, `${languageAppend}}`);
    }
  }

  changeQueryToFilterByStringConstraint(stringConstraint: string){
    const stringConstraintFilterString = "FILTER regex(str(?" + this.columnName + "), \"" + stringConstraint + "\")";
    if (this.isStringConstraintPresent(this.generatedQuery, this.columnName)) {
      const startIndex = this.generatedQuery.indexOf(stringConstraintFilterString) + stringConstraintFilterString.length + 1;
      console.log(startIndex)
      const endIndex = this.generatedQuery.indexOf("\")", startIndex);
      const currentConstraint = this.generatedQuery.slice(startIndex, endIndex);
      this.generatedQuery = this.generatedQuery.replace(currentConstraint, stringConstraint);
    } else {
      const languageAppend = stringConstraintFilterString;
      this.generatedQuery = this.generatedQuery.replace(/\}\s*$/, `${languageAppend}}`);
    }
  }

  changeQueryToSortByDate(sortByDateType: string) {
    const typeOfDateSort = "}ORDER BY "  + this.dateTypeOfSort + "(?" + this.columnName + ")";
    if (this.isDateSortFilterPresent(this.generatedQuery, this.columnName)) {
        this.generatedQuery = this.generatedQuery.replace("ORDER BY " + this.lastDateTypeOfSort, "ORDER BY " + sortByDateType);
        console.log(this.lastDateTypeOfSort)
        this.lastDateTypeOfSort = sortByDateType;
    } else {
      this.lastDateTypeOfSort = sortByDateType;
      const sortByDate = typeOfDateSort;
      this.generatedQuery = this.generatedQuery.replace(/\}\s*$/, `${sortByDate}`);
    }
  }

  tableToCsv(): void {
    const table = document.getElementById("myTable");
    // @ts-ignore
    const rows = table.querySelectorAll("tr");

    // get table header
    // @ts-ignore
    const header = Array.from(rows[0].querySelectorAll("th")).map((th) => th.textContent.trim());

    // get table data
    const data = Array.from(rows)
      .slice(1)
      .map((row) => {
        // @ts-ignore
        const rowData = Array.from(row.querySelectorAll("td")).map((cell) => cell.textContent.trim());
        return rowData.join(",");
      })
      .join("\n");

    // create csv content
    const csv = `${header.join(",")}\n${data}`;

    // download csv file
    const filename = "table-data.csv";
    const link = document.createElement("a");
    link.setAttribute("href", "data:text/csv;charset=utf-8," + encodeURIComponent(csv));
    link.setAttribute("download", filename);
    link.style.display = "none";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }


  openDialog() {
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
        this.language = result.language;
        this.stringConstraint = result.stringContains;
        this.dateTypeOfSort = result.dateTypeOfSort;
        console.log(this.dateTypeOfSort)
        if(result.type === "language"){
          this.changeQueryToFilterByLang(this.language);
        }
        if(result.type === "stringContains"){
          this.changeQueryToFilterByStringConstraint(this.stringConstraint);
        }
        if(result.type === "dateTypeOfSort"){
          this.changeQueryToSortByDate(this.dateTypeOfSort);
        }

      }
    )
  }

}
