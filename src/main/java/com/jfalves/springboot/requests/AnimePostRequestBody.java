package com.jfalves.springboot.requests;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimePostRequestBody {
    //Nada mais que um dto

    @NotEmpty(message = "The anime name cannot be empty")
    private String name;
}
