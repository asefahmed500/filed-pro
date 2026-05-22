package com.example.di

import android.content.Context
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.FileRecordDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.SyncQueueDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.VisitDao
import com.example.data.local.database.FieldForceDatabase
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.FileRecordEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.VisitEntity
import com.example.domain.model.Attendance
import com.example.domain.model.FileRecord
import com.example.domain.model.Notification
import com.example.domain.model.Task
import com.example.domain.model.User
import com.example.domain.model.Visit
import com.example.domain.usecase.CheckInUseCase
import com.example.domain.usecase.CheckOutUseCase
import com.example.domain.usecase.LoginWithEmailUseCase
import com.example.ui.FieldForceViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual Dependency Injection container for the FieldForce app.
 * Provides all dependencies without using a DI framework.
 *
 * This container is created in the Application class and accessed
 * throughout the app to obtain dependencies.
 */
class AppContainer(applicationContext: Context) {

    // Application context
    val context: Context = applicationContext.applicationContext

    // Coroutine scope for database operations
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Database
    private val database: FieldForceDatabase by lazy {
        FieldForceDatabase.getDatabase(context, applicationScope)
    }

    // DAOs
    val userDao: UserDao by lazy { database.userDao() }
    val attendanceDao: AttendanceDao by lazy { database.attendanceDao() }
    val taskDao: TaskDao by lazy { database.taskDao() }
    val visitDao: VisitDao by lazy { database.visitDao() }
    val fileRecordDao: FileRecordDao by lazy { database.fileRecordDao() }
    val notificationDao: NotificationDao by lazy { database.notificationDao() }
    val syncQueueDao: SyncQueueDao by lazy { database.syncQueueDao() }

    // Use Cases
    val loginWithEmailUseCase: LoginWithEmailUseCase by lazy {
        LoginWithEmailUseCase(userDao)
    }

    val checkInUseCase: CheckInUseCase by lazy {
        CheckInUseCase(attendanceDao, notificationDao, userDao)
    }

    val checkOutUseCase: CheckOutUseCase by lazy {
        CheckOutUseCase(attendanceDao, notificationDao, userDao)
    }

    // ViewModel Factory
    private val viewModelFactory = ViewModelFactory(this)

    /**
     * Provides a ViewModel instance.
     */
    fun <T : ViewModel> getViewModel(clazz: Class<T>): T {
        return ViewModelProvider(viewModelFactory).get(clazz)
    }

    /**
     * Clears all resources. Call when app is shutting down.
     */
    fun clear() {
        FieldForceDatabase.destroyInstance()
    }
}

/**
 * Custom ViewModelFactory that creates ViewModels with dependencies from AppContainer.
 */
private class ViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            FieldForceViewModel::class.java -> FieldForceViewModel(
                container.context,
                container.userDao,
                container.attendanceDao,
                container.taskDao,
                container.visitDao,
                container.fileRecordDao,
                container.notificationDao,
                container.syncQueueDao,
                container.loginWithEmailUseCase,
                container.checkInUseCase,
                container.checkOutUseCase
            ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
