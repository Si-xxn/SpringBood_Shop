package com.shop.dto;

import com.shop.constant.OrderStatus;
import com.shop.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderHistDto {

    public OrderHistDto(Order order) { // order 객체를 파라미터로 받아서 멤버 변수 값 세팅
        this.orderId = order.getId();
        this.orderDate =
                order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm")); // 화면에 전달할 날짜,시간 포맷팅
        this.orderStatus = order.getOrderStatus();
    }

    private Long orderId; // 주문 아이디

    private String orderDate; // 주문 날짜

    private OrderStatus orderStatus; // 주문 상태

    // 주문 상품 리스트
    private List<OrderItemDto> orderItemDtoList = new ArrayList<>();

    // orderItemDto 객체를 주문 상품 리스트에 추가하는 메서드
    public void addOrderItemDto(OrderItemDto orderItemDto) {
        orderItemDtoList.add(orderItemDto);
    }

}
