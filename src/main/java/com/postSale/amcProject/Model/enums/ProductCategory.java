package com.postSale.amcProject.Model.enums;

import lombok.Getter;

@Getter
public enum ProductCategory {

    LAPTOP("Laptop", "LAP", 24),
    DESKTOP("Desktop", "DES", 24),
    SERVER("Server", "SRV", 36),
    ROUTER("Router", "RTR", 24),
    SWITCH("Switch", "SWT", 24),
    FIREWALL("Firewall", "FWL", 36);

    private final String displayName;
    private final String serialPrefix;
    private final int defaultWarrantyMonths;

    ProductCategory(String displayName,
                    String serialPrefix,
                    int defaultWarrantyMonths) {
        this.displayName = displayName;
        this.serialPrefix = serialPrefix;
        this.defaultWarrantyMonths = defaultWarrantyMonths;
    }
}