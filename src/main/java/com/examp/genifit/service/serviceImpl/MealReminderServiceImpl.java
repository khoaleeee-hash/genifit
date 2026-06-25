package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.request.CreateMealReminderRequest;
import com.examp.genifit.dto.request.ToggleMealReminderRequest;
import com.examp.genifit.dto.request.UpdateMealReminderRequest;
import com.examp.genifit.dto.response.MealReminderResponse;
import com.examp.genifit.entity.MealReminder;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.MealReminderRepository;
import com.examp.genifit.repository.UserRepository;
import com.examp.genifit.service.MealReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealReminderServiceImpl implements MealReminderService {

    private final MealReminderRepository mealReminderRepository;
    private final UserRepository userRepository;

    @Override
    public MealReminderResponse createReminder(CreateMealReminderRequest request) {
        User currentUser = getCurrentUser();

        validateCreateRequest(request);

        LocalTime reminderTime = parseReminderTime(request.getReminderTime());

        boolean existedTime = mealReminderRepository.existsByUserAndReminderTime(
                currentUser,
                reminderTime
        );

        if (existedTime) {
            throw new RuntimeException("Bạn đã có lịch nhắc tại thời gian này");
        }

        MealReminder reminder = MealReminder.builder()
                .user(currentUser)
                .mealTime(request.getMealTime())
                .reminderTime(reminderTime)
                .enabled(request.getEnabled() == null ? true : request.getEnabled())
                .build();

        MealReminder savedReminder = mealReminderRepository.save(reminder);

        return new MealReminderResponse(savedReminder);
    }

    @Override
    public List<MealReminderResponse> getMyReminders() {
        User currentUser = getCurrentUser();

        return mealReminderRepository.findByUserOrderByReminderTimeAsc(currentUser)
                .stream()
                .map(MealReminderResponse::new)
                .toList();
    }

    @Override
    public MealReminderResponse updateReminder(Integer reminderId, UpdateMealReminderRequest request) {
        User currentUser = getCurrentUser();

        MealReminder reminder = mealReminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch nhắc"));

        checkOwner(reminder, currentUser);

        if (request.getMealTime() != null) {
            reminder.setMealTime(request.getMealTime());
        }

        if (request.getReminderTime() != null && !request.getReminderTime().trim().isEmpty()) {
            LocalTime newReminderTime = parseReminderTime(request.getReminderTime());
            reminder.setReminderTime(newReminderTime);
        }

        if (request.getEnabled() != null) {
            reminder.setEnabled(request.getEnabled());
        }

        MealReminder updatedReminder = mealReminderRepository.save(reminder);

        return new MealReminderResponse(updatedReminder);
    }

    @Override
    public MealReminderResponse toggleReminder(Integer reminderId, ToggleMealReminderRequest request) {
        User currentUser = getCurrentUser();

        MealReminder reminder = mealReminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch nhắc"));

        checkOwner(reminder, currentUser);

        if (request.getEnabled() == null) {
            throw new RuntimeException("enabled không được để trống");
        }

        reminder.setEnabled(request.getEnabled());

        MealReminder updatedReminder = mealReminderRepository.save(reminder);

        return new MealReminderResponse(updatedReminder);
    }

    @Override
    public void deleteReminder(Integer reminderId) {
        User currentUser = getCurrentUser();

        MealReminder reminder = mealReminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch nhắc"));

        checkOwner(reminder, currentUser);

        mealReminderRepository.delete(reminder);
    }

    private void validateCreateRequest(CreateMealReminderRequest request) {
        if (request == null) {
            throw new RuntimeException("Request không được để trống");
        }

        if (request.getMealTime() == null) {
            throw new RuntimeException("mealTime không được để trống");
        }

        if (request.getReminderTime() == null || request.getReminderTime().trim().isEmpty()) {
            throw new RuntimeException("reminderTime không được để trống");
        }
    }

    private LocalTime parseReminderTime(String reminderTime) {
        try {
            return LocalTime.parse(reminderTime);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("reminderTime không hợp lệ. Format đúng là HH:mm, ví dụ 07:30");
        }
    }

    private void checkOwner(MealReminder reminder, User currentUser) {
        if (!reminder.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Bạn không có quyền thao tác lịch nhắc này");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Bạn cần đăng nhập để sử dụng chức năng này");
        }

        Object principal = authentication.getPrincipal();

        if (principal == null || principal.equals("anonymousUser")) {
            throw new RuntimeException("Bạn cần đăng nhập để sử dụng chức năng này");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user hiện tại"));
    }
}