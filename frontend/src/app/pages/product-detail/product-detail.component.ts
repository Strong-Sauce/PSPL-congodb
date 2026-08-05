import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Product } from '../../models';
import { ProductService } from '../../services/product.service';

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

  product = signal<Product | null>(null);
  editing = signal(false);
  editName = signal('');
  errorMessage = signal('');
  loading = signal(true);

  ngOnInit(): void {
    const productSerialNumber = this.route.snapshot.paramMap.get('productSerialNumber');
    if (productSerialNumber) {
      this.productService.getProductById(productSerialNumber).subscribe({
        next: (p) => {
          this.product.set(p);
          this.editName.set(p.productName);
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          console.error('Product not found', err);
          this.errorMessage.set(err?.message || 'An error occurred while fetching the product.');
        },
      });
    }
  }

  startEdit(): void {
    this.editing.set(true);
  }

  cancelEdit(): void {
    const p = this.product();
    if (p) {
      this.editName.set(p.productName);
    }
    this.editing.set(false);
  }

  saveEdit(): void {
    const p = this.product();
    if (!p) return;

    const updated: Product = {
      ...p,
      productName: this.editName(),
    };

    this.productService.updateProduct(updated).subscribe({
      next: (result) => {
        this.product.set(result);
        this.editing.set(false);
      },
      error: (err) => {
        console.error('Update failed', err);
        this.errorMessage.set(err?.error?.message || 'Failed to update product. Please try again.');
      }
    });
  }

  deleteProduct(): void {
    const p = this.product();
    if (!p?.productSerialNumber) return;

    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.deleteProduct(p.productSerialNumber).subscribe({
        next: () => this.router.navigate(['/']),
        error: (err) => {
          console.error('Delete failed', err);
          this.errorMessage.set(err?.error?.message || 'Failed to delete product. Please try again.');
        }
      });
    }
  }
}

