import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/products`;

  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.baseUrl);
  }

  getProductById(productSerialNumber: string): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${productSerialNumber}`);
  }

  createProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, product);
  }

  updateProduct(product: Product): Observable<Product> {
    return this.http.put<Product>(this.baseUrl, product);
  }

  deleteProduct(productSerialNumber: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${productSerialNumber}`);
  }
}

