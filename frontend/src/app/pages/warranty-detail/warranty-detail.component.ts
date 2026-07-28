import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { WarrantyService } from '../../services';
import {Warranty } from '../../models';

@Component({
  selector: 'app-warranty-detail.component',
  imports: [FormsModule, RouterLink],
  templateUrl: './warranty-detail.component.html',
  styleUrl: './warranty-detail.component.css',
})
export class WarrantyDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly warrantyService = inject(WarrantyService);

  warranty = signal<Warranty | null>(null);
  loading = signal(true);
  errorMessage = signal('');

  ngOnInit(): void {
    const warrantyId = this.route.snapshot.paramMap.get('warrantyId');
    if (warrantyId) {
      this.warrantyService.getByWarrantyId(warrantyId).subscribe({
        next: (w) => {
          this.warranty.set(w);
          this.loading.set(false);
        },
        error: (err: any) => {
          this.loading.set(false);
          console.error('Warranty not found', err);
          this.errorMessage.set(err?.message || 'An error occurred while fetching the warranty.');
        },
      });
    }
  }

  upgradeWarranty(): void {}

  extendWarranty(): void {}
}
