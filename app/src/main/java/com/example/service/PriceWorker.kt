package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.data.repository.PriceMonitorManager
import java.util.concurrent.TimeUnit

class PriceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("PriceWorker", "Periodic WorkManager trigger executed for background checks")
        try {
            val context = applicationContext
            val monitorManager = PriceMonitorManager.getInstance(context)
            
            // Ensure background service is running if monitored symbols are active
            val activeList = (monitorManager.activeSymbols.value + monitorManager.liveTickerSymbols.value).distinct()
            if (activeList.isNotEmpty()) {
                Log.d("PriceWorker", "Active monitored symbols exist. Ensuring PriceTrackerService is started.")
                PriceTrackerService.start(context)
            }
            
            // Trigger a single simulated/live price sync check
            // and evaluate alerts to ensure low-power sleep state execution
            monitorManager.executeOneShotSyncCheck()
            
            return Result.success()
        } catch (e: Exception) {
            Log.e("PriceWorker", "Error executing periodic background monitoring check: ${e.message}", e)
            return Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "FinTraceBackgroundMonitorWork"

        fun schedulePeriodicWork(context: Context) {
            Log.d("PriceWorker", "Scheduling periodic background worker via WorkManager.")
            val workRequest = PeriodicWorkRequestBuilder<PriceWorker>(
                15, TimeUnit.MINUTES, // Minimum interval allowed by Android OS WorkManager
                5, TimeUnit.MINUTES // Flex interval for efficiency/low power matching
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Do not cancel or disrupt existing schedules
                workRequest
            )
        }
    }
}
