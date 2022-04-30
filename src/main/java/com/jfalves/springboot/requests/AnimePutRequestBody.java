package com.jfalves.springboot.requests;

import lombok.*;

@Data
@Builder
public class AnimePutRequestBody {
    //Nada mais que um dto
    private Long id;
    private String name;
}
