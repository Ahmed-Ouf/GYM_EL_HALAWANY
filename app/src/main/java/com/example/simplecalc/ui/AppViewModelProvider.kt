package com.example.simplecalc.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.simplecalc.GymApplication
import com.example.simplecalc.di.AppContainer
import com.example.simplecalc.di.PreviewAppContainer
import com.example.simplecalc.ui.viewmodel.AdminViewModel
import com.example.simplecalc.ui.viewmodel.AttendanceViewModel
import com.example.simplecalc.ui.viewmodel.DashboardViewModel
import com.example.simplecalc.ui.viewmodel.MembersViewModel
import com.example.simplecalc.ui.viewmodel.PaymentsViewModel
import com.example.simplecalc.ui.viewmodel.SubscriptionsViewModel

private val previewAppContainer: AppContainer by lazy { PreviewAppContainer() }

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            DashboardViewModel(
                memberRepository = getAppContainer().memberRepository,
                subscriptionRepository = getAppContainer().subscriptionRepository,
                attendanceRepository = getAppContainer().attendanceRepository,
                paymentRepository = getAppContainer().paymentRepository
            )
        }
        initializer {
            MembersViewModel(
                memberRepository = getAppContainer().memberRepository,
                subscriptionRepository = getAppContainer().subscriptionRepository,
                scheduleRepository = getAppContainer().trainingScheduleRepository,
                measurementRepository = getAppContainer().memberMeasurementRepository,
                attendanceRepository = getAppContainer().attendanceRepository
            )
        }
        initializer {
            SubscriptionsViewModel(
                subscriptionRepository = getAppContainer().subscriptionRepository,
                memberRepository = getAppContainer().memberRepository,
                gameRepository = getAppContainer().gameRepository
            )
        }
        initializer {
            PaymentsViewModel(
                paymentRepository = getAppContainer().paymentRepository,
                memberRepository = getAppContainer().memberRepository,
                subscriptionRepository = getAppContainer().subscriptionRepository
            )
        }
        initializer {
            AttendanceViewModel(
                attendanceRepository = getAppContainer().attendanceRepository,
                memberRepository = getAppContainer().memberRepository,
                subscriptionRepository = getAppContainer().subscriptionRepository,
                scheduleRepository = getAppContainer().trainingScheduleRepository,
                gameRepository = getAppContainer().gameRepository,
                measurementRepository = getAppContainer().memberMeasurementRepository
            )
        }
        initializer {
            AdminViewModel(
                gameRepository = getAppContainer().gameRepository,
                paymentRepository = getAppContainer().paymentRepository,
                scheduleRepository = getAppContainer().trainingScheduleRepository
            )
        }
    }
}

fun CreationExtras.getAppContainer(): AppContainer {
    val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
    return (app as? GymApplication)?.container ?: previewAppContainer
}