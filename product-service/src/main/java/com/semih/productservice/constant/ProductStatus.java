package com.semih.productservice.constant;

public enum ProductStatus {
    PENDING,      // sadece create bekliyor
    ACTIVE,       // satışta
    PROCESSING,   // update / delete işlemi sürüyor
    REJECTED,     // son işlem başarısız
    DELETED       // tamamen kapalı
}

