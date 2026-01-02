package com.semih.basketservice.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "basket")
public class Basket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @Enumerated(EnumType.STRING)
    private BasketStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "basket", fetch = FetchType.LAZY,cascade = {CascadeType.ALL},
            orphanRemoval = true)
    private List<BasketItem> basketItems = new ArrayList<>();


    public Basket() {
    }

    public Basket(String userId, BasketStatus status) {
        this.userId = userId;
        this.status = status;
    }

    public void addItem(BasketItem basketItem){
        if(basketItem!=null){
            basketItems.add(basketItem);
            basketItem.setBasket(this);
        }
    }

    public void removeItem(BasketItem basketItem) {
        basketItems.remove(basketItem);
        basketItem.setBasket(null);
    }

    public String getUserId() {
        return userId;
    }

    public BasketStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<BasketItem> getBasketItems() {
        return basketItems;
    }

    public void setStatus(BasketStatus status) {
        this.status = status;
    }
}
