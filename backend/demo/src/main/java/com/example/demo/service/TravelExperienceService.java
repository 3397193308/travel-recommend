package com.example.demo.service;

import com.example.demo.dto.ExperiencePublishRequest;
import com.example.demo.dto.ExperienceQueryRequest;
import com.example.demo.entity.TravelExperience;
import com.example.demo.entity.TravelExperienceImage;
import com.example.demo.entity.UserBehavior;
import com.example.demo.mapper.TravelExperienceMapper;
import com.example.demo.mapper.UserBehaviorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TravelExperienceService {

    @Autowired
    private TravelExperienceMapper travelExperienceMapper;
    
    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    public Map<String, Object> publish(Long userId, ExperiencePublishRequest request) {
        if (travelExperienceMapper.countDestinationById(request.getDestinationId()) <= 0) {
            throw new IllegalArgumentException("景点不存在，请从系统景点中选择");
        }

        TravelExperience experience = new TravelExperience();
        experience.setUserId(userId);
        experience.setDestinationId(request.getDestinationId());
        experience.setTitle(request.getTitle().trim());
        experience.setContent(request.getContent().trim());
        experience.setStar(request.getStar());
        experience.setStatus(0);
        travelExperienceMapper.insertExperience(experience);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int sort = 0;
            for (String imageUrl : request.getImageUrls()) {
                if (imageUrl == null || imageUrl.isBlank()) {
                    continue;
                }
                TravelExperienceImage image = new TravelExperienceImage();
                image.setExperienceId(experience.getId());
                image.setImageUrl(imageUrl.trim());
                image.setSort(sort++);
                travelExperienceMapper.insertExperienceImage(image);
            }
        }

        UserBehavior shareBehavior = new UserBehavior();
        shareBehavior.setUserId(userId);
        shareBehavior.setDestinationId(request.getDestinationId());
        shareBehavior.setBehaviorType("share");
        userBehaviorMapper.insert(shareBehavior);

        Map<String, Object> data = new HashMap<>();
        data.put("id", experience.getId());
        data.put("status", experience.getStatus());
        return data;
    }

    public Map<String, Object> listApproved(ExperienceQueryRequest request) {
        int page = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        int offset = (page - 1) * pageSize;

        int total = travelExperienceMapper.countApprovedExperiences(request);
        List<TravelExperience> list = travelExperienceMapper.selectApprovedExperiences(request, offset, pageSize);
        fillImages(list);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    public Map<String, Object> myExperiences(Long userId, Integer status, Integer page, Integer pageSize) {
        int pageNum = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (pageNum - 1) * size;

        int total = travelExperienceMapper.countMyExperiences(userId, status);
        List<TravelExperience> list = travelExperienceMapper.selectMyExperiences(userId, status, offset, size);
        fillImages(list);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", pageNum);
        data.put("pageSize", size);
        return data;
    }

    public TravelExperience detail(Long id, Long userId) {
        TravelExperience detail = travelExperienceMapper.selectExperienceById(id);
        if (detail == null) {
            return null;
        }

        boolean isOwner = userId != null && userId.equals(detail.getUserId());
        if (detail.getStatus() != 1 && !isOwner) {
            return null;
        }

        detail.setImages(travelExperienceMapper.selectImagesByExperienceId(id));
        return detail;
    }

    public Map<String, Object> adminList(ExperienceQueryRequest request) {
        int page = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        int offset = (page - 1) * pageSize;

        int total = travelExperienceMapper.countAdminExperiences(request);
        List<TravelExperience> list = travelExperienceMapper.selectAdminExperiences(request, offset, pageSize);
        fillImages(list);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        return data;
    }

    public boolean audit(Long id, Integer status, String rejectReason) {
        if (status == null || (status != 1 && status != 2)) {
            throw new IllegalArgumentException("审核状态不合法");
        }
        if (status == 2 && (rejectReason == null || rejectReason.isBlank())) {
            throw new IllegalArgumentException("驳回时必须填写驳回原因");
        }

        String reason = status == 2 ? rejectReason.trim() : null;
        return travelExperienceMapper.updateAuditStatus(id, status, reason) > 0;
    }

    public TravelExperience adminDetail(Long id) {
        TravelExperience detail = travelExperienceMapper.selectExperienceById(id);
        if (detail == null) {
            return null;
        }
        detail.setImages(travelExperienceMapper.selectImagesByExperienceId(id));
        return detail;
    }

    private void fillImages(List<TravelExperience> list) {
        for (TravelExperience experience : list) {
            experience.setImages(travelExperienceMapper.selectImagesByExperienceId(experience.getId()));
        }
    }
}
