package com.semih.inventoryservice.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Inventory {

    @Id
    private String id;

    @Indexed(unique = true)
    private Long productId;

    private Integer quantity;

    @Version // Bu alanı ekledik
    private Long version;

    public Inventory() {
    }

    public Inventory(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getter ve Setter'lar
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getVersion() { // Version için Getter
        return version;
    }

    public void setVersion(Long version) { // Version için Setter
        this.version = version;
    }
}