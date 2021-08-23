package com.aiden.dev.simpleboard.modules.comment.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WriteCommentForm {

    private Long postId;

    @NotBlank
    private String contents;

    private boolean secret;
}
