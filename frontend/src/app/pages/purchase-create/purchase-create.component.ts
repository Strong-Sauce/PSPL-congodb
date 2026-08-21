import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ProductCategory } from '../../models/product.model';
import {
  PurchaseProduct,
  PurchaseRequest,
} from '../../models/purchase.model';
import { SaleService } from '../../services/sale.service';
import { ProductService, WarrantyService } from '../../services';

@Component({
  selector: 'app-purchase-create',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './purchase-create.component.html',
  styleUrl: './purchase-create.component.css',
})
export class PurchaseCreateComponent {

  private readonly saleService = inject(SaleService);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly warrantyService = inject(WarrantyService);

  productCategories = Object.values(ProductCategory);

  saleDate = signal(this.getToday());

  products = signal<PurchaseProduct[]>([
    this.createEmptyProduct(),
  ]);

  submitting = signal(false);
  errorMsg = signal('');

  private createEmptyProduct(): PurchaseProduct {
    return {
      productName: '',
      productCategory: null,
    };
  }

  addProduct(): void {
    if (this.submitting()) {
      return;
    }

    this.products.update(products => [
      ...products,
      this.createEmptyProduct(),
    ]);
  }

  removeProduct(index: number): void {
    if (this.submitting()) {
      return;
    }

    if (this.products().length === 1) {
      return;
    }

    this.products.update(products =>
      products.filter((_, i) => i !== index)
    );
  }

  updateProductName(index: number, name: string): void {
    if (this.submitting()) {
      return;
    }

    this.products.update(products =>
      products.map((product, i) =>
        i === index
          ? { ...product, productName: name }
          : product
      )
    );
  }

  updateProductCategory(
    index: number,
    category: ProductCategory
  ): void {
    if (this.submitting()) {
      return;
    }

    this.products.update(products =>
      products.map((product, i) =>
        i === index
          ? { ...product, productCategory: category }
          : product
      )
    );
  }

  onSubmit(): void {
    if (this.submitting()) {
      return;
    }

    this.errorMsg.set('');

    if (!this.saleDate()) {
      this.errorMsg.set('Purchase date is required.');
      return;
    }

    if (this.products().length === 0) {
      this.errorMsg.set('A purchase must contain at least one product.');
      return;
    }

    const invalidProduct = this.products().some(product =>
      !product.productName.trim() ||
      !product.productCategory
    );

    if (invalidProduct) {
      this.errorMsg.set(
        'Please complete the name and category for every product.'
      );
      return;
    }

    const request: PurchaseRequest = {
      saleDate: this.saleDate(),
      products: this.products().map(product => ({
        productName: product.productName.trim(),
        productCategory: product.productCategory,
      })),
    };

    this.submitting.set(true);

    this.saleService.createPurchase(request).subscribe({
      next: () => {
        this.productService.refreshProducts();
        this.warrantyService.refreshWarranties();

        this.submitting.set(false);

        this.router.navigate(['/']);
      },

      error: (err) => {
        console.error(err);

        this.submitting.set(false);

        this.errorMsg.set(
          err?.error?.message ||
          'Failed to create purchase. Please try again.'
        );
      },
    });
  }

  private getToday(): string {
    const today = new Date();

    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
