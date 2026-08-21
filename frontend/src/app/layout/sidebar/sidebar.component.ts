import { Component, OnInit, inject, input, signal, effect } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Product, Warranty } from '../../models';
import { ProductService } from '../../services/product.service';
import { WarrantyService } from '../../services/warranty.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit {
  collapsed = input<boolean>(false);

  private readonly productService = inject(ProductService);
  private readonly warrantyService = inject(WarrantyService);

  products = signal<Product[]>([]);
  warranties = signal<Warranty[]>([]);

  productsOpen = signal(true);
  warrantiesOpen = signal(true);

  // Independent loading states
  productsLoading = signal(true);
  warrantiesLoading = signal(true);

  constructor() {
    effect(() => {
      this.productService.productsRefresh();

      this.loadProducts();
    });

    effect(() => {
      this.warrantyService.warrantiesRefresh();

      this.loadWarranties();
    });
  }

  ngOnInit(): void {
    // Initial loading is handled by the effects.
  }

  loadProducts(): void {
    this.productsLoading.set(true);

    this.productService.getAllProducts().subscribe({
      next: (data) => {
        this.products.set(data);
        this.productsLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load products', err);
        this.productsLoading.set(false);
      }
    });
  }

  loadWarranties(): void {
    this.warrantiesLoading.set(true);

    this.warrantyService.getAllWarranties().subscribe({
      next: (data) => {
        this.warranties.set(data);
        this.warrantiesLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load warranties', err);
        this.warrantiesLoading.set(false);
      }
    });
  }

  toggleProducts(): void {
    this.productsOpen.update(v => !v);
  }

  toggleWarranties(): void {
    this.warrantiesOpen.update(v => !v);
  }
}
