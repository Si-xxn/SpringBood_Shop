package com.shop.repository;

import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import com.shop.dto.ItemSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryCustom {

    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);
    // 상품 조회 조건을 담고 있는 itemSearchDto 객체와 페이징 정보를 담고 있는 pageable 객체를 파라미터로 받는 메서드 정의
    // 반환 데이터로 Page<Item>객체 반환

    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto,
                                      Pageable pageable); // 메인페이지에 보여줄 상품 리스트 가져오는 메서드

}
