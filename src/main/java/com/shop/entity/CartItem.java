package com.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cart_item")
public class CartItem extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "cart_item_id")
    private Long id;

    // 다대일 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int count;
    // 같은 상품을 장바구니에 몇 개 담을지 저장

    // 장바구니에 담을 상품 엔티티를 생성하는 메서드와 장바구니에 담을 수량을 증가시켜주는 메서드
    public static CartItem createCartItem(Cart cart, Item item, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setCount(count);

        return cartItem;
    }

    // 장바구니에 기존에 담겨있는 상품
    // 해당 상품을 추가로 장바구니에 담을 때 기존 수량에 현재 담을 수량을 더 해줄 때 사용하는 메서드
    public void addCount(int count) {
        this.count += count;
    }

    // 수량 변경하는 메서드
    public void updateCount(int count) {
        this.count = count;
    }
}
