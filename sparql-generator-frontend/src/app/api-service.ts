import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private httpClient: HttpClient) {}

  getOntology(string: string){
    return this.httpClient.get('http://localhost:5000/get-ontology?resource=' + string)
  }

  getOntologyWithPage(string: string){
    return this.httpClient.get('http://localhost:5000/get-page-ontology?resource=' + string)
  }

  generateSparql(json: any){
    return this.httpClient.post('http://localhost:8080/generateSparql', json)
  }

  generateDynamicSparql(json: any){
    return this.httpClient.post('http://localhost:8080/generateDynamicSparql', json)
  }

  generateSparqlWithLabels(json: any){
    return this.httpClient.post('http://localhost:8080/generateSparqlWithLabels', json)
  }

  executeSparql(json: any){
    return this.httpClient.post('http://localhost:8080/executeSparql', json)
  }
}
