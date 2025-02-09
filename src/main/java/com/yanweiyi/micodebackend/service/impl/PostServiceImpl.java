package com.yanweiyi.micodebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanweiyi.micodebackend.mapper.PostMapper;
import com.yanweiyi.micodebackend.model.entity.Post;
import com.yanweiyi.micodebackend.service.PostService;
import org.springframework.stereotype.Service;

/**
 * 题解表(Post)表服务实现类
 *
 * @author makejava
 * @since 2025-02-09 14:07:58
 */
@Service("postService")
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

}

