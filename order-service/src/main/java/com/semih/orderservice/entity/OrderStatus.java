package com.semih.orderservice.entity;

public enum OrderStatus {
    PENDING,    // Sipariş veritabanına kaydedildi, Inventory'den cevap bekleniyor
    COMPLETED,  // Stoklar onaylandı, sipariş başarıyla tamamlandı
    CANCELLED,  // Stok yetersizliği veya başka bir hata nedeniyle iptal edildi
    FAILED      // Ödeme hatası veya teknik bir arıza durumu (opsiyonel)
}