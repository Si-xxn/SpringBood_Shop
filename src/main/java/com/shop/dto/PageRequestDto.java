package com.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder
@AllArgsConstructor
// @NoArgsConstructor - 기본 생성자 수동으로 만들었기 때문에 사용 X
@Data
public class PageRequestDto {
    // 리스트 요청 시 페이징 처리 사용하는 데이터를 재사용하기 위함
    // 페이지번호, 목록 개수, 검색 조건 등...

    private int page;
    private int size;
    private String type;
    private String keyword;

    public PageRequestDto(){ // 생성자 - 페이징 기본값
        this.page = 1;
        this.size = 10;
    }

    public Pageable getPageable(Sort sort){
        return PageRequest.of(page -1, size, sort);
    }
}
