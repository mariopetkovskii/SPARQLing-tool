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

  generateSparql(json: any){
    return this.httpClient.post('http://localhost:8080/my-endpoint', json)
  }
}
