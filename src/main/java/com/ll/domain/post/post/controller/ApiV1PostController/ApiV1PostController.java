package com.ll.domain.post.post.controller.ApiV1PostController;

import com.ll.domain.post.post.service.PostService;
import com.ll.spring.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;
}
