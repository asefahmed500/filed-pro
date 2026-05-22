package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room database for the FieldForce application.
 * Provides access to all DAOs and manages database creation/migrations.
 *
 * @see UserDao
 * @see AttendanceDao
 * @see TaskDao
 * @see VisitDao
 * @see FileRecordDao
 * @see NotificationDao
 * @see SyncQueueDao
 */
@Database(
    entities = [
        UserEntity::class,
        AttendanceEntity::class,
        TaskEntity::class,
        VisitEntity::class,
        FileRecordEntity::class,
        NotificationEntity::class,
        SyncQueueEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class FieldForceDatabase : RoomDatabase() {
    /**
     * Provides access to User DAO.
     */
    abstract fun userDao(): UserDao

    /**
     * Provides access to Attendance DAO.
     */
    abstract fun attendanceDao(): AttendanceDao

    /**
     * Provides access to Task DAO.
     */
    abstract fun taskDao(): TaskDao

    /**
     * Provides access to Visit DAO.
     */
    abstract fun visitDao(): VisitDao

    /**
     * Provides access to FileRecord DAO.
     */
    abstract fun fileRecordDao(): FileRecordDao

    /**
     * Provides access to Notification DAO.
     */
    abstract fun notificationDao(): NotificationDao

    /**
     * Provides access to SyncQueue DAO.
     */
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: FieldForceDatabase? = null

        /**
         * Gets the singleton instance of the database.
         * Creates the database if it doesn't exist.
         *
         * @param context The application context
         * @param scope Coroutine scope for database operations
         * @return The FieldForceDatabase instance
         */
        fun getDatabase(context: Context, scope: CoroutineScope): FieldForceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FieldForceDatabase::class.java,
                    "fieldforce_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Callback for database creation events.
         * Used for initial setup and data seeding if needed.
         */
        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Database created - data will be fetched from server
                // No local seed data needed as we use server as source of truth
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Database opened - can perform migrations or cleanup here
            }
        }

        /**
         * Clears the singleton instance.
         * Useful for testing or when needing to recreate the database.
         */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
