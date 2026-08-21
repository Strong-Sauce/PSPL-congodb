import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Sale } from '../models';
import { PurchaseRequest } from '../models/purchase.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SaleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/sales`;

  getByCustomerId(customerId: string): Observable<Sale[]> {
    return this.http.get<Sale[]>(`${this.baseUrl}/${customerId}`);
  }

  createPurchase(request: PurchaseRequest): Observable<Sale> {
    return this.http.post<Sale>(this.baseUrl, request);
  }
}
