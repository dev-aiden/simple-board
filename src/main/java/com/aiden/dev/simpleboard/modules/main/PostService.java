package com.aiden.dev.simpleboard.modules.main;

import com.aiden.dev.simpleboard.modules.account.Account;
import com.aiden.dev.simpleboard.modules.post.Post;
import com.aiden.dev.simpleboard.modules.post.PostRepository;
import com.aiden.dev.simpleboard.modules.post.PostType;
import com.aiden.dev.simpleboard.modules.post.WritePostForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    
    public List<Post> getAllPost() {
        return postRepository.findAll();
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
}
