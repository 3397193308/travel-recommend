package com.example.demo.controller;

import com.example.demo.entity.Category;
import com.example.demo.entity.Result;
import com.example.demo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @GetMapping("/root")
    public Result<List<Category>> getRootCategories() {
        List<Category> categories = categoryService.getRootCategories();
        return Result.success(categories);
    }

    @GetMapping("/parent/{parentId}")
    public Result<List<Category>> getCategoriesByParentId(@PathVariable Long parentId) {
        List<Category> categories = categoryService.getCategoriesByParentId(parentId);
        return Result.success(categories);
    }

    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return Result.success(category);
    }
}