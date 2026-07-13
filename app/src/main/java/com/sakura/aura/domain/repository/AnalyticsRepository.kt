package com.sakura.aura.domain.repository

import com.sakura.aura.domain.model.Distribution
import com.sakura.aura.domain.model.Trends
import com.sakura.aura.domain.model.WeekComparison

interface AnalyticsRepository {
    suspend fun getTrends(period: String, days: Int = 30, weeks: Int = 4, months: Int = 6): Result<Trends>
    suspend fun getDistribution(): Result<Distribution>
    suspend fun getComparison(): Result<WeekComparison>
}
