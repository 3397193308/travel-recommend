package com.example.demo.service.impl;

import com.example.demo.entity.Category;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> getAllCategories() {
        return categoryMapper.selectAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryMapper.findById(id);
    }

    @Override
    public List<Category> getCategoriesByParentId(Long parentId) {
        return categoryMapper.selectByParentId(parentId);
    }

    @Override
    public List<Category> getRootCategories() {
        return categoryMapper.selectRootCategories();
    }
}