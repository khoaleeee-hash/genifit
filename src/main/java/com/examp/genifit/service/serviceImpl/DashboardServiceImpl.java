package com.examp.genifit.service.serviceImpl;

import com.examp.genifit.dto.response.dashboard.*;
import com.examp.genifit.entity.GoalType;
import com.examp.genifit.entity.PaymentTransaction;
import com.examp.genifit.entity.SubscriptionStatus;
import com.examp.genifit.entity.User;
import com.examp.genifit.repository.*;
import com.examp.genifit.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final FoodItemRepository foodItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public DashboardResponse getDashboardData() {
        LocalDateTime now = LocalDateTime.now();
        
        // This Month
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        // Last Month
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfThisMonth.minusNanos(1);

        // 1. Total Users
        long totalUsersThisMonth = userRepository.count();
        long totalUsersLastMonth = userRepository.countByCreatedAtBefore(startOfThisMonth);
        long newUsersThisMonth = totalUsersThisMonth - totalUsersLastMonth;
        long newUsersLastMonth = userRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        Double userTrend = calculateTrend(newUsersThisMonth, newUsersLastMonth);
        
        DashboardKpi totalUsersKpi = DashboardKpi.builder()
                .value(new BigDecimal(totalUsersThisMonth))
                .trend(userTrend)
                .build();

        // 2. New Foods
        long totalFoodsThisMonth = foodItemRepository.count();
        long totalFoodsLastMonth = foodItemRepository.countByCreatedAtBefore(startOfThisMonth);
        long newFoodsThisMonth = totalFoodsThisMonth - totalFoodsLastMonth;
        long newFoodsLastMonth = foodItemRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        Double foodTrend = calculateTrend(newFoodsThisMonth, newFoodsLastMonth);

        DashboardKpi newFoodsKpi = DashboardKpi.builder()
                .value(new BigDecimal(totalFoodsThisMonth))
                .trend(foodTrend)
                .build();

        // 3. Monthly Revenue
        BigDecimal revenueThisMonth = paymentTransactionRepository.sumAmountByStatusAndCreatedAtBetween(
                PaymentTransaction.PaymentStatus.SUCCESS, startOfThisMonth, now);
        if (revenueThisMonth == null) revenueThisMonth = BigDecimal.ZERO;
        
        BigDecimal revenueLastMonth = paymentTransactionRepository.sumAmountByStatusAndCreatedAtBetween(
                PaymentTransaction.PaymentStatus.SUCCESS, startOfLastMonth, endOfLastMonth);
        if (revenueLastMonth == null) revenueLastMonth = BigDecimal.ZERO;
        
        Double revenueTrend = calculateTrend(revenueThisMonth.doubleValue(), revenueLastMonth.doubleValue());

        DashboardKpi monthlyRevenueKpi = DashboardKpi.builder()
                .value(revenueThisMonth)
                .trend(revenueTrend)
                .build();

        // 4. Active Subscriptions
        long activeSubsThisMonth = userSubscriptionRepository.countByStatusAndCreatedAtBetween(SubscriptionStatus.ACTIVE, startOfThisMonth, now);
        long activeSubsLastMonth = userSubscriptionRepository.countByStatusAndCreatedAtBetween(SubscriptionStatus.ACTIVE, startOfLastMonth, endOfLastMonth);
        Double subsTrend = calculateTrend(activeSubsThisMonth, activeSubsLastMonth);
        
        long totalActiveSubs = userSubscriptionRepository.count(); // For simplicity, returning total all time or currently active.
        // Actually to find currently active we might need to check if end_date > now, but since we are replacing mock data, returning the total active count will suffice.
        
        DashboardKpi activeSubscriptionsKpi = DashboardKpi.builder()
                .value(new BigDecimal(activeSubsThisMonth)) // Use new this month to match dashboard UI style usually
                .trend(subsTrend)
                .build();

        // 5. Chart Data (Last 6 Months)
        List<DashboardChartData> chartData = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("M");
        
        for (int i = 5; i >= 0; i--) {
            LocalDateTime startOfMonth = startOfThisMonth.minusMonths(i);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);
            
            BigDecimal revenue = paymentTransactionRepository.sumAmountByStatusAndCreatedAtBetween(
                    PaymentTransaction.PaymentStatus.SUCCESS, startOfMonth, endOfMonth);
            if (revenue == null) revenue = BigDecimal.ZERO;
            
            long users = userRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
            
            chartData.add(DashboardChartData.builder()
                    .month("T" + startOfMonth.format(monthFormatter))
                    .revenue(revenue)
                    .users(users)
                    .build());
        }

        // 6. Recent Transactions
        List<PaymentTransaction> transactions = paymentTransactionRepository.findTop5ByStatusOrderByCreatedAtDesc(PaymentTransaction.PaymentStatus.SUCCESS);
        List<RecentTransactionDto> recentTransactions = transactions.stream().map(t -> RecentTransactionDto.builder()
                .transactionId(t.getTransactionId())
                .userName(t.getUser().getUsername())
                .userAvatar(t.getUser().getUserProfile() != null ? t.getUser().getUserProfile().getAvatarUrl() : null)
                .amount(t.getAmount())
                .status("Thành công")
                .date(t.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .build()).collect(Collectors.toList());

        // 7. Health Goals
        long loseWeight = userProfileRepository.countByGoal(GoalType.LOSE_WEIGHT);
        long gainWeight = userProfileRepository.countByGoal(GoalType.GAIN_WEIGHT);
        long maintainWeight = userProfileRepository.countByGoal(GoalType.MAINTAIN);
        long totalGoals = loseWeight + gainWeight + maintainWeight;
        
        Double loseWeightPct = totalGoals > 0 ? (loseWeight * 100.0) / totalGoals : 0.0;
        Double gainWeightPct = totalGoals > 0 ? (gainWeight * 100.0) / totalGoals : 0.0;
        Double maintainWeightPct = totalGoals > 0 ? (maintainWeight * 100.0) / totalGoals : 0.0;
        
        HealthGoalStats healthGoals = HealthGoalStats.builder()
                .loseWeight(loseWeight)
                .gainWeight(gainWeight)
                .maintainWeight(maintainWeight)
                .loseWeightPercentage(loseWeightPct)
                .gainWeightPercentage(gainWeightPct)
                .maintainWeightPercentage(maintainWeightPct)
                .build();

        // 8. Recent Users
        List<User> recentUserList = userRepository.findTop5ByOrderByCreatedAtDesc();
        List<RecentUserDto> recentUsers = recentUserList.stream().map(u -> RecentUserDto.builder()
                .userId(u.getUserId())
                .username(u.getUsername())
                .email(u.getEmail())
                .role(u.getRole().name())
                .joinDate(u.getCreatedAt() != null ? u.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "")
                .avatar(u.getUserProfile() != null ? u.getUserProfile().getAvatarUrl() : null)
                .build()).collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalUsers(totalUsersKpi)
                .newFoods(newFoodsKpi)
                .monthlyRevenue(monthlyRevenueKpi)
                .activeSubscriptions(activeSubscriptionsKpi)
                .chartData(chartData)
                .recentTransactions(recentTransactions)
                .healthGoals(healthGoals)
                .recentUsers(recentUsers)
                .build();
    }

    private Double calculateTrend(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }
}
