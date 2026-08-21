import { ProductCategory } from './product.model';

export interface PurchaseProduct {
  productName: string;
  productCategory: ProductCategory | null;
}

export interface PurchaseRequest {
  saleDate: string;
  products: PurchaseProduct[];
}
