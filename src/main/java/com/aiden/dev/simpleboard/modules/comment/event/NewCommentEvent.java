package com.aiden.dev.simpleboard.modules.comment.event;

import com.aiden.dev.simpleboard.modules.comment.Comment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NewCommentEvent {

    private final Comment comment;
}
