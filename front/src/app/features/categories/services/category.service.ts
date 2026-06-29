import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Category, CreateCategoryRequest, UpdateCategoryRequest, CategoryType } from '../../../core/models/category.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private baseUrl = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) {}

  listCategories(filters?: { active?: boolean; type?: CategoryType }): Observable<Category[]> {
    let params = new HttpParams();
    if (filters?.active !== undefined && filters.active !== null) {
      params = params.set('active', String(filters.active));
    }
    if (filters?.type) {
      params = params.set('type', filters.type);
    }
    return this.http.get<Category[]>(this.baseUrl, { params });
  }

  getCategory(id: string): Observable<Category> {
    return this.http.get<Category>(`${this.baseUrl}/${id}`);
  }

  createCategory(request: CreateCategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.baseUrl, request);
  }

  updateCategory(id: string, request: UpdateCategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.baseUrl}/${id}`, request);
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
