package com.shop.repository;

import com.shop.dto.CartDetailDto;
import com.shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 카트 아이디와 상품 아이디를 이용해서 상품이 장바구니에 들어있는지 조회
    CartItem findByCartIdAndItemId(Long cartId, Long itemId);

    // 장바구니 페이지에 전달할 CartDetailDto 리스트를 쿼리 하나로 조회하는 JPQL 작성
    // com.shop.dto.CartDetailDto(ci.id, i.itemNm, i.price, ci.count, im.imgUrl) - DTO 반환할 때 클래스명 사용 / 생성자의 파라미터 순서는 DTO 클래스에 명시한 순으로 넣어줘야함
    @Query("select new com.shop.dto.CartDetailDto(ci.id, i.itemNm, i.price, ci.count, im.imgUrl)" +
                " from CartItem ci, ItemImg im " +
                " join ci.item i " +
                " where ci.cart.id = :cartId " +
                " and im.item.id = ci.item.id " +
                " and im.repimgYn = 'Y' " +
                "order by ci.regTime desc ")
    List<CartDetailDto> findCartDetailDtoList(Long cartId);
    // and im.item.id = ci.item.id " +  " and im.repimgYn = 'Y' "  -> 장바구니에 담겨있는 상품의 대표 이미지만 가지고 오도록 조건문 작성

}
