package com.example.demo.controller;

import com.example.demo.dto.TagVO;
import com.example.demo.entity.Result;
import com.example.demo.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
@CrossOrigin(origins = "*")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping("/list")
    public Result<List<TagVO>> getAllTags() {
        List<TagVO> tags = tagService.getAllTags();
        return Result.success(tags);
    }

    @GetMapping("/list/{type}")
    public Result<List<TagVO>> getTagsByType(@PathVariable String type) {
        List<TagVO> tags = tagService.getTagsByType(type);
        return Result.success(tags);
    }
}
