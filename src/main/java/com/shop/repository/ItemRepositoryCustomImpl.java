package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.Item;
import com.shop.dto.ItemSearchDto;
import com.shop.entity.QItem;
import com.shop.entity.QItemImg;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{

    private JPAQueryFactory queryFactory; // 동적 쿼리를 생성하기 위해서 JPAQueryFactory 클래스 사용

    public ItemRepositoryCustomImpl(EntityManager em) { // JPAQueryFactory의 생성자로 EntityManager 객체 넣어줌
        this.queryFactory = new JPAQueryFactory(em);
        // JPA를 사용하기 위해서는 Database 구조와 맵핑된 JPA Entity 들을 먼저 생성하게 된다.
        // 그리고, 모든 JPA의 동작은 이 Entity들을 기준으로 돌아가게 되는데, 이 때 Entity들을 관리하는 역할을 하는 녀석이 바로 EntityManager인 것이다.
        // 참고 https://velog.io/@juhyeon1114/JPA-%EC%98%81%EC%86%8D%EC%84%B1-%EC%BB%A8%ED%85%8D%EC%8A%A4%ED%8A%B8-%EC%9D%B4%ED%95%B4%ED%95%98%EA%B8%B0-w.-Entity-manager
    }

    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus){
        // 상품 판매 조건이 전체(null)일 경우는 null 리턴
        // 결과 값이 null 이면 where절에서 해당 조건은 무시
        // 상품 판매 상태 조건이 null이 아니라 판매중 or 품절 상태라면 해당 조건의 상품만 조회합니다.
        return searchSellStatus ==
                null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
    }

    private BooleanExpression regDtsAfter(String searchDateType) {
        // searchDateType 값에 따라서 dateTime의 값을 이전 시간의 값으로 세팅 후 해당 시간 이후로 등록된 상품만 조회
        // 예를 들어 searchDateType 값이 1m 인 경우 dateTime의 시간을 한 달 전으로 세팅 후 최근 한 달 동안 등록된 상품만 조회하도록 조건 값 반환
        LocalDateTime dateTime = LocalDateTime.now();

        if(StringUtils.equals("all", searchDateType) || searchDateType == null){
            // 상품 등록일 전체
            return null;
        } else if (StringUtils.equals("id", searchDateType)) { // 최근 한달 동안 등록된 상품
            dateTime = dateTime.minusDays(1);
        } else if (StringUtils.equals("1w", searchDateType)) { // 최근 일주일
            dateTime = dateTime.minusWeeks(1);
        } else if (StringUtils.equals("1m", searchDateType)) { // 최근 한달
            dateTime = dateTime.minusMonths(1);
        } else if (StringUtils.equals("6m", searchDateType)) { // 최근 6개월
            dateTime = dateTime.minusMonths(6);
        }
        return QItem.item.regTime.after(dateTime);
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery){
        // searchBy의 값에 따라서 상품명에 검색어를 포함하고 있는 상품 또는 상품 생성자의 아이디에 검색어를 포함하고 있는 상품을 조회하도록 조건값 반환
        if(StringUtils.equals("itemNm", searchBy)){     // 상품명
            return QItem.item.itemNm.like("%" + searchQuery + "%");
        } else if(StringUtils.equals("createdBy", searchBy)){       // 상품 등록자 id
            return QItem.item.createdBy.like("%" + searchQuery + "%");
        }
        return null;
    }

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        List<Item> content = queryFactory
                .selectFrom(QItem.item)             // 상품 데이터를 조회하기 위해서 Qitem의 item 지정
                .where(regDtsAfter(itemSearchDto.getSearchDateType()),              // 조건 절 ,가 and 조건
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(),
                                itemSearchDto.getSearchQuery()))
                .orderBy(QItem.item.id.desc())
                .offset(pageable.getOffset())                                       // 한번에 가지고올 시작 인덱스
                .limit(pageable.getPageSize())                                      // 한번에 가지고올 최대 개수
                .fetch();

        long total = queryFactory.select(Wildcard.count).from(QItem.item)
                .where(regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
                .fetchOne();
        return new PageImpl<>(content, pageable, total); // 페이징 처리되어 리턴
    }

    private BooleanExpression itemNmLink(String searchQuery) {
        return StringUtils.isEmpty(searchQuery) ?
                null : QItem.item.itemNm.like("%"+ searchQuery + "%");
    } // 검색어가 null이 아니면 상품명에 해당 검색어가 포함되는 상품을 조회하는 조건을 반환

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        List <MainItemDto> content = queryFactory
                .select(
                        new QMainItemDto(       // @QueryProjection 사용하면 DTO로 바로 조회가 가능(엔티티 조회 후 DTO 변환 과정을 줄일 수 있음)
                                item.id,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price)
                )
                .from(itemImg)
                .join(itemImg.item, item)   // itemImg 와 item 내부 조인
                .where(itemImg.repimgYn.eq("Y")) // 상품 이미지의 경우 대표 상품 이미지만 불러옴
                .where(itemNmLink(itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // QueryDSL 조회 결과 메소드 http://querydsl.com/static/querydsl/5.0.0/apidocs/com/querydsl/core/Fetchable.html
        // QueryResults<T> fetchResults() : 조회 대상 리스트 및 전체 개수를 포함하는 QueryResult 반환
        // List<T> fetch() : 조회 대상을 리스트로 반환
        // T fetchOne() : 조회대상이 1건이면 해당 타입 반환, 1건 이상이면 에러 발생
        // T fetchFirst() : 조회 대상이 1건 또는 1건 이상이면 1건만 반환
        // long fetchCount() : 전체 개수 반환 count 쿼리 실행

        long total = queryFactory
                .select(Wildcard.count)
                .from(itemImg)
                .join(itemImg.item, item)
                .where(itemImg.repimgYn.eq("Y"))
                .where(itemNmLink(itemSearchDto.getSearchQuery()))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

}
