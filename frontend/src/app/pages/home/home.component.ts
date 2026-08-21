import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Product, Warranty } from '../../models';
import { ProductService } from '../../services/product.service';
import { WarrantyService } from '../../services/warranty.service';
import { WarrantyStatus } from '../../models/warranty.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly warrantyService = inject(WarrantyService);

  // Products
  allProducts = signal<Product[]>([]);
  productSearch = signal('');
  productPage = signal(0);
  readonly productPageSize = 4;
  // Loading states
  productLoading = signal(true);
  warrantyLoading = signal(true);

  filteredProducts = computed(() => {
    const term = this.productSearch().trim().toLowerCase();
    return this.allProducts().filter((p) => {
      return (
        p.productName.toLowerCase().includes(term) ||
        p.productSerialNumber?.toLowerCase().includes(term)
      );
    });
  });

  pagedProducts = computed(() => {
    const start = this.productPage() * this.productPageSize;
    return this.filteredProducts().slice(start, start + this.productPageSize);
  });

  totalProductPages = computed(() =>
    Math.ceil(this.filteredProducts().length / this.productPageSize),
  );

  // Warranties
  allWarranties = signal<Warranty[]>([]);
  warrantySearch = signal('');
  selectedWarrantyStatus = signal<WarrantyStatus | 'ALL'>('ALL');
  warrantyPage = signal(0);
  readonly warrantyPageSize = 4;

  filteredWarranties = computed(() => {
    const term = this.warrantySearch().trim().toLowerCase();
    const selectedStatus = this.selectedWarrantyStatus();

    return this.allWarranties().filter((warranty) => {
      const matchesSearch =
        warranty.productName.toLowerCase().includes(term) ||
        warranty.productSerialNumber.toLowerCase().includes(term);
      const matchesStatus = selectedStatus === 'ALL' || warranty.warrantyStatus === selectedStatus;

      return matchesSearch && matchesStatus;
    });
  });

  activeWarrantyCount = computed(() => this.allWarranties().filter((w) => w.warrantyStatus === 'ACTIVE').length,);

  expiringSoonWarrantyCount = computed(() => this.allWarranties().filter((w) => w.warrantyStatus === 'EXPIRING_SOON').length,);

  expiredWarrantyCount = computed(() => this.allWarranties().filter((w) => w.warrantyStatus === 'EXPIRED').length,);

  totalWarrantyCount = computed(() => this.allWarranties().length);

  pagedWarranties = computed(() => {
    const start = this.warrantyPage() * this.warrantyPageSize;
    return this.filteredWarranties().slice(start, start + this.warrantyPageSize);
  });

  totalWarrantyPages = computed(() =>
    Math.ceil(this.filteredWarranties().length / this.warrantyPageSize),
  );

  ngOnInit(): void {
    this.productLoading.set(true);

    this.productService
      .getAllProducts()
      .pipe(finalize(() => this.productLoading.set(false)))
      .subscribe({
        next: (data) => this.allProducts.set(data),
        error: () => this.allProducts.set([]),
      });

    this.warrantyLoading.set(true);

    this.warrantyService
      .getAllWarranties()
      .pipe(finalize(() => this.warrantyLoading.set(false)))
      .subscribe({
        next: (data) => this.allWarranties.set(data),
        error: () => this.allWarranties.set([]),
      });
  }

  onProductSearch(term: string): void {
    this.productSearch.set(term);
    this.productPage.set(0);
  }

  onWarrantySearch(term: string): void {
    this.warrantySearch.set(term);
    this.warrantyPage.set(0);
  }

  setWarrantyStatusFilter(status: WarrantyStatus | 'ALL'): void {
    this.selectedWarrantyStatus.set(status);
    this.warrantyPage.set(0);
  }

  setProductPage(page: number): void {
    this.productPage.set(page);
  }

  setWarrantyPage(page: number): void {
    this.warrantyPage.set(page);
  }
}
