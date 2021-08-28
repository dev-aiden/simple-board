package com.aiden.dev.simpleboard.modules.comment;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.comment.form.WriteCommentForm;
import com.aiden.dev.simpleboard.modules.main.PostService;
import com.aiden.dev.simpleboard.modules.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;


    public Comment writeNewComment(WriteCommentForm writeCommentForm, Account account) {
        Post post = postService.getPostDetail(writeCommentForm.getPostId())
                .orElseThrow(() -> new IllegalArgumentException(writeCommentForm.getPostId() + "에 해당하는 게시글이 존재하지 않습니다."));
        Comment comment = Comment.builder()
                .post(post)
                .contents(writeCommentForm.getContents())
                .commentType(writeCommentForm.isSecret() ? CommentType.PRIVATE : CommentType.PUBLIC)
                .account(account)
                .build();
        return commentRepository.save(comment);
    }

    public List<Comment> getComments(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public Optional<Comment> getComment(Long commentId) {
        return commentRepository.findById(commentId);
    }

    public void deleteComment(Comment comment) {
        commentRepository.delete(comment);
    }

    public void updateComment(Long commentId, boolean updateSecret, String updateContents) {
        Comment comment = commentRepository.findById(commentId).get();
        comment.setCommentType(updateSecret ? CommentType.PRIVATE : CommentType.PUBLIC);
        comment.setContents(updateContents);
    }
}
