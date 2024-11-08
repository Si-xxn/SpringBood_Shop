package com.shop.dto;

import com.shop.entity.ItemImg;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class ItemImgDto {

    private Long id;

    private String imgName;

    private String oriImgName;

    private String imgUrl;

    private String repImgYn;

    private static ModelMapper modelMapper = new ModelMapper(); // 멤버 변수로 ModelMapper 객체 추가

    public static ItemImgDto of(ItemImg itemImg) { // ItemImg를 파라미터로 받아서 자료형과 변수이름이 같을 때 ItemImgDto로 값을 복사해 반환
        // static 처리를 하여 new 없이 사용하도록 설정
        return modelMapper.map(itemImg, ItemImgDto.class);
    }
}
