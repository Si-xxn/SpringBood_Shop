package com.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cart")
@Getter
@Setter
@ToString
public class Cart {

    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY) // 회원 엔티티와 일대일로 매핑 - 전략 설정 FetchType.EAGER(즉시 로딩)
    @JoinColumn(name = "member_id")
    private Member member;

    // 카트 테이블은 member_id 외래키로 가짐

    // 회원 한 명당 1개의 장바구니 -> 상품을 담을 때 해당 회원의 장바구니를 생성
    public static Cart createCart(Member member){
        Cart cart = new Cart();
        cart.setMember(member);
        return cart;
    }
}
