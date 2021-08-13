package com.aiden.dev.simpleboard.modules.post;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class WritePostForm {

    @NotBlank
    @Length(max = 50)
    private String title;

    private String contents;

    private boolean secret;
}
