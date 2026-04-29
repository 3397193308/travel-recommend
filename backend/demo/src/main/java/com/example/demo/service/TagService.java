package com.example.demo.service;

import com.example.demo.dto.TagVO;
import com.example.demo.entity.Tag;
import com.example.demo.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    private TagMapper tagMapper;

    public List<TagVO> getAllTags() {
        List<Tag> tags = tagMapper.selectAll();
        return tags.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public List<TagVO> getTagsByType(String type) {
        List<Tag> tags = tagMapper.selectByType(type);
        return tags.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private TagVO convertToVO(Tag tag) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setType(tag.getType());
        vo.setDescription(tag.getDescription());
        return vo;
    }
}
