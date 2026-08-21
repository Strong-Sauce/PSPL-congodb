import {Injectable, inject, signal} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Warranty } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WarrantyService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/warranty`;

  readonly warrantiesRefresh = signal(0);

  refreshWarranties(): void {
    this.warrantiesRefresh.update(value => value + 1);
  }

  // Backend payload also includes productId, productName, and productSerialNumber.
  getAllWarranties(): Observable<Warranty[]> {
    return this.http.get<Warranty[]>(this.baseUrl);
  }

  getByWarrantyId(warrantyId: string): Observable<Warranty> {
    return this.http.get<Warranty>(`${this.baseUrl}/${warrantyId}`);
  }
}
