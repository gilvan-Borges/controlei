import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Family } from '../models/family.model';

@Injectable({
  providedIn: 'root'
})
export class FamilyService {
  private baseUrl = `${environment.apiUrl}/families`;

  constructor(private http: HttpClient) {}

  getCurrentFamily(): Observable<Family> {
    return this.http.get<Family>(`${this.baseUrl}/current`);
  }
}
