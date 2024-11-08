package com.shop.entity;

import com.shop.constant.ItemSellStatus;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderItemRepository;
import com.shop.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class OrderTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @PersistenceContext
    EntityManager em;

    public Item createItem() {
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("상세설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());

        return item;
    }

    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest() {

        Order order = new Order();

        for(int i = 0; i < 3; i++){
            Item item = this.createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        orderRepository.saveAndFlush(order); // order 엔티티를 저장하면서 강제로 flush() 호출하여 영속성 컨텍스트에 있는 객체들을 데이터베이스에 반영
        em.clear(); // 영속성 컨텍스트의 상태 초기화

        Order savedOrder = orderRepository.findById(order.getId()) // 영속성 컨텍스트 초기화했기 때문에 데이터베이스에서 주문 엔티티 조회 -> select 쿼리문 실행
                .orElseThrow(EntityNotFoundException::new);
        assertEquals(3, savedOrder.getOrderItems().size()); // ItemOrder 엔티티 3개가 실제로 데이터베이스에 저장되었는지 검사

        // Hibernate:
        //    insert
        //    into
        //        orders
        //        (member_id, order_date, order_status, reg_time, update_time, order_id)
        //    values
        //        (?, ?, ?, ?, ?, ?)

    }

    // 주문 엔티티에서 주문상품을 삭제했을 때 orderItem 엔티티가 삭제되는지 테스트
    public Order createOrder() {
        Order order = new Order();

        for(int i = 0; i < 3; i++) {
            Item item = createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        Member member = new Member();
        memberRepository.save(member);

        order.setMember(member);
        orderRepository.save(order);
        return order;
    }

    @Test
    @DisplayName("고아객체 제거 테스트")
    public void orphanRemovalTest() {
        Order order = this.createOrder();
        order.getOrderItems().remove(0); // order 엔티티에서 관리하고 있는 orderItem 리스트의 0번째 인덱스 요소 제거
        em.flush();

        // Hibernate:
        //    delete
        //    from
        //        order_item
        //    where
        //        order_item_id=?
    }

    @Test
    @DisplayName("지연 로딩 테스트") // 사용하지 않는 데이터도 한번에 조회 -> 성능문제 있을 수 있음 LAZY 방식으로 설정
    public void lazyLoadingTest() {

        Order order = this.createOrder();
        Long orderItemId = order.getOrderItems().get(0).getId();
        em.flush();
        em.clear();

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(EntityNotFoundException :: new);
        System.out.println("Order class : " + orderItem.getOrder().getClass());
        // Order class : class com.shop.entity.Order
        System.out.println("-----------------------------------------------");
        orderItem.getOrder().getOrderDate();
        System.out.println("-----------------------------------------------");

    }
}
