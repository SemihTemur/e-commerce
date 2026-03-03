package com.semih.basketservice.entity;

public enum BasketStatus {
    ACTIVE,             // Kullanıcı ürün ekleyip çıkarabilir
    ORDER_IN_PROGRESS,  // Checkout başlatıldı, Saga devam ediyor (Sepet kilitli!)
    ORDERED,            // İşlem başarıyla bitti, sepet kapandı
    ABANDONED,          // Kullanıcı sepeti terk etti
}
