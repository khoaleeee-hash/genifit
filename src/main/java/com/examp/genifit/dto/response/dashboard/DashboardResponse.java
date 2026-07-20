package com.examp.genifit.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private DashboardKpi totalUsers;
    private DashboardKpi newFoods;
    private DashboardKpi monthlyRevenue;
    private DashboardKpi activeSubscriptions;

    private List<DashboardChartData> chartData;
    private List<RecentTransactionDto> recentTransactions;
    private HealthGoalStats healthGoals;
    private List<RecentUserDto> recentUsers;
}
