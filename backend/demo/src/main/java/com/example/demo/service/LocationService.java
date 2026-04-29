package com.example.demo.service;

import com.example.demo.entity.Location;
import com.example.demo.mapper.LocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    @Autowired
    private LocationMapper locationMapper;

    /**
     * 获取所有地点
     */
    public List<Location> getAllLocations() {
        return locationMapper.selectAll();
    }

    /**
     * 根据ID获取地点
     */
    public Location getLocationById(Long id) {
        return locationMapper.findById(id);
    }

    /**
     * 根据父级ID获取地点列表（用于级联选择）
     */
    public List<Location> getLocationsByParentId(Long parentId) {
        return locationMapper.selectByParentId(parentId);
    }

    /**
     * 根据层级获取地点列表
     */
    public List<Location> getLocationsByLevel(Integer level) {
        return locationMapper.selectByLevel(level);
    }
}
