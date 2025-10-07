package com.xertica.controller;

import com.xertica.entity.User;
import com.xertica.entity.UserPipeline;
import com.xertica.service.UserPipelineService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/pipeline")
public class UserPipelineController {

    private final UserPipelineService service;

    public UserPipelineController(UserPipelineService service) {
        this.service = service;
    }

    @PostMapping("/{userId}/add")
    public UserPipeline addStep(
            @PathVariable Long userId,
            @RequestParam String stepName,
            @RequestParam(required = false) String description,
            @RequestParam int order) {

        User user = new User();
        user.setId(userId);
        return service.addStep(user, stepName, description, order);
    }

    @PostMapping("/{userId}/complete/{stepId}")
    public void completeStep(@PathVariable Long userId, @PathVariable Long stepId) {
        User user = new User();
        user.setId(userId);
        service.completeStep(user, stepId);
    }

    @GetMapping("/{userId}")
    public List<UserPipeline> getPipeline(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return service.getUserPipeline(user);
    }

    @GetMapping("/{userId}/progress")
    public double getProgress(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return service.getProgressPercentage(user);
    }
}
