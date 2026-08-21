import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Product } from '../../models';
import { ProductService } from '../../services/product.service';
import { WarrantyService } from '../../services';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css'
})
export class ProductDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly warrantyService = inject(WarrantyService);

  product = signal<Product | null>(null);
  editing = signal(false);
  editName = signal('');
  errorMessage = signal('');

  // Loading states
  loading = signal(true);
  saving = signal(false);
  deleting = signal(false);

  ngOnInit(): void {
    const productSerialNumber =
      this.route.snapshot.paramMap.get('productSerialNumber');

    if (!productSerialNumber) {
      this.loading.set(false);
      this.errorMessage.set('Product serial number is missing.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.productService.getProductById(productSerialNumber).subscribe({
      next: (p) => {
        this.product.set(p);
        this.editName.set(p.productName);
        this.loading.set(false);
      },

      error: (err) => {
        this.loading.set(false);
        console.error('Product not found', err);

        this.errorMessage.set(
          err?.error?.message ||
          err?.message ||
          'An error occurred while fetching the product.'
        );
      },
    });
  }

  startEdit(): void {
    if (this.saving() || this.deleting()) return;

    this.errorMessage.set('');
    this.editing.set(true);
  }

  cancelEdit(): void {
    if (this.saving()) return;

    const p = this.product();

    if (p) {
      this.editName.set(p.productName);
    }

    this.errorMessage.set('');
    this.editing.set(false);
  }

  saveEdit(): void {
    const p = this.product();

    if (!p || this.saving() || this.deleting()) {
      return;
    }

    this.errorMessage.set('');
    this.saving.set(true);

    const updated: Product = {
      ...p,
      productName: this.editName(),
    };

    this.productService.updateProduct(updated).subscribe({
      next: (result) => {
        this.product.set(result);
        this.editName.set(result.productName);
        this.editing.set(false);
        this.saving.set(false);
      },

      error: (err) => {
        console.error('Update failed', err);

        this.errorMessage.set(
          err?.error?.message ||
          'Failed to update product. Please try again.'
        );

        this.saving.set(false);
      }
    });
  }

  deleteProduct(): void {
    const p = this.product();

    if (
      !p?.productSerialNumber ||
      this.saving() ||
      this.deleting()
    ) {
      return;
    }

    if (!confirm('Are you sure you want to delete this product?')) {
      return;
    }

    this.errorMessage.set('');
    this.deleting.set(true);

    this.productService.deleteProduct(p.productSerialNumber).subscribe({
      next: () => {
        this.productService.refreshProducts();
        this.warrantyService.refreshWarranties();

        this.router.navigate(['/']);
      },

      error: (err) => {
        console.error('Delete failed', err);

        this.errorMessage.set(
          err?.error?.message ||
          'Failed to delete product. Please try again.'
        );

        this.deleting.set(false);
      }
    });
  }
}
