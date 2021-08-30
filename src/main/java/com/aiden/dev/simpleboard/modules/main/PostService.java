package com.aiden.dev.simpleboard.modules.main;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.post.Post;
import com.aiden.dev.simpleboard.modules.post.PostRepository;
import com.aiden.dev.simpleboard.modules.post.PostType;
import com.aiden.dev.simpleboard.modules.post.form.WritePostForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    
    public Page<Post> getPosts(Pageable pageable, String category, String keyword) {
        Page<Post> posts;

        if(category.equalsIgnoreCase("title")) {
            posts = postRepository.findByTitleContains(keyword, pageable);
        } else if (category.equalsIgnoreCase("writer")) {
            posts = postRepository.findByAccount_NicknameContains(keyword, pageable);
        } else {
            posts = postRepository.findAll(pageable);
        }

        return posts;
    }

    public Post writeNewPost(WritePostForm writePostForm, Account account) {
        Post post = modelMapper.map(writePostForm, Post.class);
        post.setAccount(account);
        post.setHits(0L);
        post.setPostType(writePostForm.isSecret() ? PostType.PRIVATE : PostType.PUBLIC);
        return postRepository.save(post);
    }

    public Optional<Post> getPostDetail(Long postId) {
        return postRepository.findById(postId);
    }

    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    public void updatePost(Long postId, WritePostForm writePostForm) {
        Post post = postRepository.findById(postId).get();
        post.setTitle(writePostForm.getTitle());
        post.setPostType(writePostForm.isSecret() ? PostType.PRIVATE : PostType.PUBLIC);
        post.setContents(writePostForm.getContents());
    }

    public void increaseHits(Post post) {
        post.setHits(post.getHits() + 1);
    }
}
