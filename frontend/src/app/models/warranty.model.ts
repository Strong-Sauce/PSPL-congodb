import { AMC } from './amc.model';

export interface Warranty {
  warrantyId?: string;
  warrantyStartDate: string; // ISO date string (LocalDate)
  warrantyEndDate: string; // ISO date string (LocalDate)
  warrantyStatus: WarrantyStatus;
  productName: string;
  productSerialNumber: string;
  amcList?: AMC[];
}

export type WarrantyStatus = 'ACTIVE' | 'EXPIRING_SOON' | 'EXPIRED';
