package com.yanweiyi.micodebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanweiyi.micodebackend.common.ApiResponse;
import com.yanweiyi.micodebackend.common.ResultUtils;
import com.yanweiyi.micodebackend.model.entity.Post;
import com.yanweiyi.micodebackend.service.PostService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 题解表(Post)表控制层
 *
 * @author makejava
 * @since 2025-02-09 14:09:55
 */
@RestController
@RequestMapping("post")
public class PostController {
    /**
     * 服务对象
     */
    @Resource
    private PostService postService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param post 查询实体
     * @return 所有数据
     */
    @GetMapping
    public ApiResponse<IPage<Post>> selectAll(Page<Post> page, Post post) {
        return ResultUtils.success(this.postService.page(page, new QueryWrapper<>(post)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ApiResponse<Post> selectOne(@PathVariable Serializable id) {
        return ResultUtils.success(this.postService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param post 实体对象
     * @return 新增结果
     */
    @PostMapping
    public ApiResponse<Boolean> insert(@RequestBody Post post) {
        return ResultUtils.success(this.postService.save(post));
    }

    /**
     * 修改数据
     *
     * @param post 实体对象
     * @return 修改结果
     */
    @PutMapping
    public ApiResponse<Boolean> update(@RequestBody Post post) {
        return ResultUtils.success(this.postService.updateById(post));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public ApiResponse<Boolean> delete(@RequestParam("idList") List<Long> idList) {
        return ResultUtils.success(this.postService.removeByIds(idList));
    }
}

