package com.shop.entity;

import com.shop.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class Order extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime orderDate; // 주문일

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // 주문 상태

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
                orphanRemoval = true, fetch = FetchType.LAZY) // 주문 상품은 엔티티와 일대다 매핑  | orphanRemoval 고아객체 제거
    private List<OrderItem> orderItems = new ArrayList<>();
    // 외래키가 order_item 테이블 - 연관관계 주인은 OrderItem 엔티티
    // Order 엔티티가 주인이 아니므로 mappedBy 속성으로 연관관계 주인을 설정 - OrderItem에 있는 Order에 의해 관리 됨
    // 하나의 주문이 여러 개의 주문 상품을 갖으므로 List 자료형 사용
    // CascadeType.ALL -> 부모 엔티티의 영속성 상태 변화를 자식 엔티티에 모두 전이하는 옵션

   // private LocalDateTime regTime;

   // private LocalDateTime updateTime;

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
        // orderItem에 주문 상품 정보 담음 - orderItem 객체를 order 객체의 orderItems에 추가
        // Order 엔티티와 OrderItem 엔티티가 양방향 참조 관계이므로 orderItem객체에도 order 객체를 세팅
    }

    public static Order createOrder(Member member, List<OrderItem> orderItemList) {
        Order order = new Order();
        order.setMember(member); // 상품을 주문한 회원의 정보를 세팅
        for(OrderItem orderitem : orderItemList) { // 상품페이지에는 1개의 상품 주문, 장바구니 페이지에서는 한 번에 여러개의 상품을 주문할 수 있다.
                                                   // 여러개의 주문 상품을 담을 수 있도록 리스트형태로 파라미터 값을 받으면 주문 객체에 orderItem 객체 추가
            order.addOrderItem(orderitem);
        }
        order.setOrderStatus(OrderStatus.ORDER); // 주문 상태를 'ORDER' 세팅
        order.setOrderDate(LocalDateTime.now()); // 현재 시간을 주문 시간으로 세팅
        return order;
    }

    public int getTotalPrice() { // 총 주문 금액을 구하는 메소드
        int totalPrice=0;
        for(OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    // 주문 취소 시 주문 수량을 상품의 재고에 더해주는 로직
    // 주문 상태를 취소 상태로 바꿔주는 메서드 구현
    public void cancelOrder(){
        this.orderStatus = OrderStatus.CANCEL;

        for(OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }
}
