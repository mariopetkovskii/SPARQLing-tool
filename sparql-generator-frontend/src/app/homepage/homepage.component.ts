import { Component } from '@angular/core';
import { ApiService } from '../api-service';

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
  loading: boolean = false;
  dataResource: string = "";
  generatedQuery: string = "";

  constructor(private apiService: ApiService) {}

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
    } else {
      const index = this.checkedItems.findIndex((x) => x.property === item.property && x.ontology === item.ontology);
      if (index !== -1) {
        this.checkedItems.splice(index, 1);
      }
    }
  }

  generateSparql() {
    console.log(this.checkedItems)
    const payload = {
      dataResource: this.dataResource,
      props: this.checkedItems
    }
    this.apiService.generateSparql(payload).subscribe((data: any) => {
      this.generatedQuery = data.query;
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
    });
  }

}
