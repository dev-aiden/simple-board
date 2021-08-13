package com.aiden.dev.simpleboard.modules.main;

import com.aiden.dev.simpleboard.modules.post.Post;
import com.aiden.dev.simpleboard.modules.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepository postRepository;
    
    public List<Post> getAllPost() {
        return postRepository.findAll();
    }
}
