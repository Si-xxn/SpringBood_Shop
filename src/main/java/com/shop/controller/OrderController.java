package com.shop.controller;

import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping(value = "/order")
    public @ResponseBody ResponseEntity order (@RequestBody @Valid OrderDto orderDto ,
                                               BindingResult bindingResult, Principal principal) {
        // 스프링에서 비동기 처리 할 때 @RequestBody / @ResponseBody 어노테이션 사용
        // @RequestBody : HTTP 요청의 본문 body에 담긴 내용을 자가 객체로 전달
        // @ResponseBody : 자바 객체를 HTTP 요청의 body로 전달

        if(bindingResult.hasErrors()) { // 주문 정보를 받는 orderDto 객체에 데이터 바인딩 시 에러가 있는지 검사
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for(FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
            // 에러 정보를 ResponseEntity 객체에 담아서 반환
        }

        // 현재의 로그인 유저의 정보를 얻기 위해서 @Controller 어노테이션이 선언된 클래으세어 메서드 인자로 principal 객체 넘길 경우 해당 객체에 직접 접근 가능
        // principal 객체에서 현재 로그인한 회원의 이메일 정보 조회
        String email = principal.getName();
        Long orderId;

        try{
            orderId = orderService.order(orderDto, email);
            // 화면으로부터 넘어오는 주문 정보와 회원의 이메일 정보를 이용하여 주문 로직 호출
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
        // 결과 값으로 생성된 주문 번호와 요청이 성공했다는 HTTP 응답 상태 코드를 반환
    }

    @GetMapping(value = {"/orders", "/orders/{page}"}) // 주문 이력 조회용
    public String orderHist(@PathVariable("page")Optional<Integer> page, Principal principal, Model model) {
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 4);
        // 한번에 가지고 올 주문의 개수는 4개로 설정

        Page<OrderHistDto> orderHistDtoList =
                orderService.getOrderList(principal.getName(), pageable);
        // 현재 로그인한 회원은 이메일과 페이징 객체를 파라미터로 전달하여 화면에 전달한 주문 목록 데이터를 리턴값으로 받음

        model.addAttribute("orders", orderHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 5);

        return "order/orderHist";
    }

    // 주문 취소 로직 호출 메서드
    @PostMapping("/order/{orderId}/cancel")
    public @ResponseBody ResponseEntity cancelOrder(@PathVariable("orderId") Long orderId, Principal principal) {
        if(!orderService.validateOrder(orderId, principal.getName())){
            // 자바스크립트에서 취소할 주문 번호는 조작이 가능하므로 다른 사람의 주문을 취소하지 못하도록 주문 취소 권한 검사
            return new ResponseEntity<String>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        orderService.cancelOrder(orderId); // 주문 취소 로직 호출
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

}
