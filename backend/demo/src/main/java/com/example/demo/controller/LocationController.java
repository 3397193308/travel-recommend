package com.example.demo.controller;

import com.example.demo.entity.Location;
import com.example.demo.entity.Result;
import com.example.demo.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    /**
     * 获取所有地点
     */
    @GetMapping
    public Result<List<Location>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        return Result.success(locations);
    }

    /**
     * 根据ID获取地点
     */
    @GetMapping("/{id}")
    public Result<Location> getLocationById(@PathVariable Long id) {
        Location location = locationService.getLocationById(id);
        return Result.success(location);
    }

    /**
     * 根据父级ID获取地点列表（用于级联选择）
     */
    @GetMapping("/parent/{parentId}")
    public Result<List<Location>> getLocationsByParentId(@PathVariable Long parentId) {
        List<Location> locations = locationService.getLocationsByParentId(parentId);
        return Result.success(locations);
    }

    /**
     * 根据层级获取地点列表
     */
    @GetMapping("/level/{level}")
    public Result<List<Location>> getLocationsByLevel(@PathVariable Integer level) {
        List<Location> locations = locationService.getLocationsByLevel(level);
        return Result.success(locations);
    }
}
