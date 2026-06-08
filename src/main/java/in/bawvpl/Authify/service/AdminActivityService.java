package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.AdminActivityResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminActivityService {

    public List<AdminActivityResponse> getActivities() {

        List<AdminActivityResponse> activities =
                new ArrayList<>();

        AdminActivityResponse activity =
                new AdminActivityResponse();

        activity.setType("USER_REGISTERED");
        activity.setMessage("New user registered");

        // ISO FORMAT TIME
        activity.setTime(
                Instant.now().toString()
        );

        activities.add(activity);

        return activities;
    }
}