USE db_genifit;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM admin_logs;
DELETE FROM weight_progress;
DELETE FROM meal_suggestions;
DELETE FROM ai_chat_histories;
DELETE FROM ai_scan_histories;
DELETE FROM log_details;
DELETE FROM daily_logs;
DELETE FROM food_items;
DELETE FROM advanced_profiles;
DELETE FROM user_profiles;
DELETE FROM guest;
DELETE FROM users;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users
(user_id, username, email, password_hash, role, created_at, updated_at)
VALUES
    (1, 'khoa_member', 'khoa@gmail.com', '$2a$10$example_hash_member', 'MEMBER', NOW(), NOW()),
    (2, 'an_member', 'an@gmail.com', '$2a$10$example_hash_member2', 'MEMBER', NOW(), NOW()),
    (3, 'admin_genifit', 'admin@genifit.com', '$2a$10$example_hash_admin', 'ADMIN', NOW(), NOW());

INSERT INTO guest
(guest_id, device_id, created_at, expired_at)
VALUES
    (1, 'DEVICE_GUEST_001', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY)),
    (2, 'DEVICE_GUEST_002', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY));

INSERT INTO user_profiles
(profile_id, user_id, height_cm, weight_kg, age, gender, goal, activity_level, base_target_calorie, created_at, updated_at)
VALUES
    (1, 1, 170, 68, 21, 'MALE', 'LOSE_WEIGHT', 'MODERATE', 2100, NOW(), NOW()),
    (2, 2, 160, 50, 20, 'FEMALE', 'GAIN_WEIGHT', 'LIGHT', 1900, NOW(), NOW());

INSERT INTO advanced_profiles
(advanced_profile_id, user_id, medical_conditions, allergies, target_weight, target_date, daily_target_calorie, created_at, updated_at)
VALUES
    (1, 1, 'Không có bệnh nền nghiêm trọng', 'Dị ứng hải sản nhẹ', 62, '2026-09-01', 1800, NOW(), NOW()),
    (2, 2, 'Huyết áp thấp', 'Không dị ứng', 55, '2026-10-01', 2200, NOW(), NOW());

INSERT INTO food_items
(food_id, food_name, calories, protein, carbs, fat, nutrition_info, created_by, created_at)
VALUES
    (1, 'Cơm trắng', 130, 2.7, 28.0, 0.3, '100g cơm trắng chứa khoảng 130 kcal', 3, NOW()),
    (2, 'Ức gà luộc', 165, 31.0, 0.0, 3.6, 'Giàu protein, ít chất béo', 3, NOW()),
    (3, 'Trứng gà luộc', 78, 6.0, 0.6, 5.0, 'Nguồn protein và chất béo tốt', 3, NOW()),
    (4, 'Salad rau củ', 80, 2.0, 12.0, 2.5, 'Ít calo, nhiều chất xơ', 3, NOW()),
    (5, 'Bánh mì thịt', 450, 18.0, 55.0, 18.0, 'Nhiều năng lượng, phù hợp bữa sáng', 3, NOW()),
    (6, 'Phở bò', 500, 25.0, 60.0, 15.0, 'Món ăn nhiều năng lượng, có protein và tinh bột', 3, NOW()),
    (7, 'Sữa chua không đường', 60, 4.0, 6.0, 2.0, 'Tốt cho tiêu hóa, ít calo', 3, NOW());

INSERT INTO daily_logs
(log_id, user_id, guest_id, log_date, total_calories, target_calories, status_color, created_at)
VALUES
    (1, 1, NULL, CURDATE(), 875, 1800, 'BLUE', NOW()),
    (2, 1, NULL, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1376, 1800, 'GREEN', NOW()),
    (3, 2, NULL, CURDATE(), 460, 2200, 'BLUE', NOW()),
    (4, NULL, 1, CURDATE(), 710, 2000, 'BLUE', NOW()),
    (5, NULL, 2, CURDATE(), 1450, 2000, 'YELLOW', NOW());

INSERT INTO log_details
(detail_id, log_id, food_id, quantity, calories, source, meal_time, created_at)
VALUES
    (1, 1, 1, 2, 260, 'MANUAL', 'LUNCH', NOW()),
    (2, 1, 2, 1, 165, 'MANUAL', 'LUNCH', NOW()),
    (3, 1, 5, 1, 450, 'SCAN', 'BREAKFAST', NOW()),
    (4, 2, 6, 1, 500, 'SCAN', 'BREAKFAST', NOW()),
    (5, 2, 1, 3, 390, 'MANUAL', 'LUNCH', NOW()),
    (6, 2, 2, 2, 330, 'MANUAL', 'DINNER', NOW()),
    (7, 2, 3, 2, 156, 'MANUAL', 'SNACK', NOW()),
    (8, 3, 4, 1, 80, 'MANUAL', 'LUNCH', NOW()),
    (9, 3, 1, 2, 260, 'MANUAL', 'LUNCH', NOW()),
    (10, 3, 7, 2, 120, 'AI_SUGGESTION', 'SNACK', NOW()),
    (11, 4, 5, 1, 450, 'SCAN', 'BREAKFAST', NOW()),
    (12, 4, 1, 2, 260, 'MANUAL', 'LUNCH', NOW()),
    (13, 5, 6, 2, 1000, 'SCAN', 'LUNCH', NOW()),
    (14, 5, 5, 1, 450, 'MANUAL', 'BREAKFAST', NOW());

INSERT INTO ai_scan_histories
(scan_id, user_id, guest_id, image_url, detected_food, estimated_calories, nutrition_result, suitability_status, created_at)
VALUES
    (1, 1, NULL, '/uploads/scan/banh-mi.jpg', 'Bánh mì thịt', 450, 'Món ăn nhiều tinh bột và chất béo, nên cân đối với bữa còn lại.', 'SUITABLE', NOW()),
    (2, 1, NULL, '/uploads/scan/pho-bo.jpg', 'Phở bò', 500, 'Phù hợp cho bữa sáng hoặc trưa, cần kiểm soát khẩu phần.', 'SUITABLE', NOW()),
    (3, NULL, 1, '/uploads/scan/com-ga.jpg', 'Cơm gà', 650, 'Lượng calo khá cao, nên giảm tinh bột nếu đang giảm cân.', 'UNSUITABLE', NOW()),
    (4, 2, NULL, '/uploads/scan/salad.jpg', 'Salad rau củ', 80, 'Ít calo, phù hợp người muốn kiểm soát cân nặng.', 'SUITABLE', NOW());

INSERT INTO ai_chat_histories
(chat_id, user_id, prompt, response, created_at)
VALUES
    (1, 1, 'Hôm nay tôi còn thiếu bao nhiêu calo?', 'Bạn còn thiếu khoảng 925 kcal so với mục tiêu hôm nay.', NOW()),
    (2, 1, 'Tôi nên ăn gì vào buổi tối để giảm cân?', 'Bạn có thể ăn ức gà, salad rau củ và một ít cơm trắng.', NOW()),
    (3, 2, 'Tôi muốn tăng cân thì nên ăn gì?', 'Bạn nên tăng lượng tinh bột tốt, protein và chia thành nhiều bữa nhỏ trong ngày.', NOW());

INSERT INTO meal_suggestions
(suggestion_id, user_id, guest_id, suggested_food, reason, estimated_calories, created_at)
VALUES
    (1, 1, NULL, 'Ức gà luộc + Salad rau củ', 'Bạn đang thiếu calo nhưng vẫn cần bữa ăn ít chất béo.', 245, NOW()),
    (2, 1, NULL, 'Trứng gà luộc + Sữa chua không đường', 'Phù hợp cho bữa phụ nhẹ, giàu protein.', 138, NOW()),
    (3, 2, NULL, 'Phở bò + Sữa chua', 'Bạn đang cần tăng cân nên có thể bổ sung bữa nhiều năng lượng hơn.', 560, NOW()),
    (4, NULL, 1, 'Cơm trắng + Ức gà', 'Gợi ý cân bằng giữa tinh bột và protein cho Guest User.', 425, NOW());

INSERT INTO weight_progress
(progress_id, user_id, current_weight, progress_percent, progress_status, recorded_date, created_at)
VALUES
    (1, 1, 68.0, 0, 'ON_TRACK', DATE_SUB(CURDATE(), INTERVAL 7 DAY), NOW()),
    (2, 1, 67.5, 15, 'ON_TRACK', DATE_SUB(CURDATE(), INTERVAL 3 DAY), NOW()),
    (3, 1, 67.0, 25, 'FASTER', CURDATE(), NOW()),
    (4, 2, 50.0, 0, 'ON_TRACK', DATE_SUB(CURDATE(), INTERVAL 7 DAY), NOW()),
    (5, 2, 50.6, 12, 'SLOWER', CURDATE(), NOW());

INSERT INTO admin_logs
(admin_log_id, admin_id, action, target_table, target_id, created_at)
VALUES
    (1, 3, 'CREATE_FOOD_ITEM', 'food_items', 1, NOW()),
    (2, 3, 'CREATE_FOOD_ITEM', 'food_items', 2, NOW()),
    (3, 3, 'VIEW_USER_STATISTICS', 'users', 1, NOW()),
    (4, 3, 'CHECK_AI_SCAN_HISTORY', 'ai_scan_histories', 1, NOW());
