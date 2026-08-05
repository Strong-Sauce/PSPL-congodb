import { Warranty } from './warranty.model';

export interface Product {
  productName: string;
  productCreatedDate?: string;
  productSerialNumber?: string;
  productCategory: ProductCategory | null;

  warrantyList?: Warranty[];
}

export enum ProductCategory {
  LAPTOP = 'LAPTOP',
  DESKTOP = 'DESKTOP',
  SERVER = 'SERVER',
  ROUTER = 'ROUTER',
  SWITCH = 'SWITCH',
  FIREWALL = 'FIREWALL',
}
