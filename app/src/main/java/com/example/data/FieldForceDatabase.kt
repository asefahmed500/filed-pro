package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Attendance::class,
        Task::class,
        Visit::class,
        FileRecord::class,
        NotificationModel::class,
        SyncQueueItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FieldForceDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun taskDao(): TaskDao
    abstract fun visitDao(): VisitDao
    abstract fun fileRecordDao(): FileRecordDao
    abstract fun notificationDao(): NotificationDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: FieldForceDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FieldForceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FieldForceDatabase::class.java,
                    "fieldforce_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Database created - data will be fetched from server
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
