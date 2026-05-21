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
        NotificationModel::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FieldForceDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun taskDao(): TaskDao
    abstract fun visitDao(): VisitDao
    abstract fun fileRecordDao(): FileRecordDao
    abstract fun notificationDao(): NotificationDao

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
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed basic users in a background coroutine
                        scope.launch(Dispatchers.IO) {
                            val database = getDatabase(context, scope)
                            val userDao = database.userDao()
                            val taskDao = database.taskDao()
                            val notifyDao = database.notificationDao()

                            // Basic users seed
                            val admin = User(
                                id = "admin_1",
                                email = "admin@force.com",
                                name = "Arthur Pendragon",
                                role = "ADMIN",
                                phone = "+1 (555) 0101",
                                photoUri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80",
                                reportingManagerId = null,
                                workZoneName = "Headquarters (Zone A)",
                                workZoneLat = 37.7749,
                                workZoneLng = -122.4194,
                                workZoneRadiusMeters = 200.0
                            )

                            val manager = User(
                                id = "manager_1",
                                email = "manager@force.com",
                                name = "Morgan LeFay",
                                role = "MANAGER",
                                phone = "+1 (555) 0202",
                                photoUri = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=256&q=80",
                                reportingManagerId = "admin_1",
                                workZoneName = "Oakland Hub (Zone B)",
                                workZoneLat = 37.8044,
                                workZoneLng = -122.2711,
                                workZoneRadiusMeters = 250.0
                            )

                            val exec1 = User(
                                id = "exec_1",
                                email = "exec@force.com",
                                name = "Lancelot DuLac",
                                role = "EXECUTIVE",
                                phone = "+1 (555) 0303",
                                photoUri = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=256&q=80",
                                reportingManagerId = "manager_1",
                                workZoneName = "San Jose Sector",
                                workZoneLat = 37.3382,
                                workZoneLng = -121.8863,
                                workZoneRadiusMeters = 300.0
                            )

                            val exec2 = User(
                                id = "exec_2",
                                email = "exec2@force.com",
                                name = "Guinevere Row",
                                role = "EXECUTIVE",
                                phone = "+1 (555) 0404",
                                photoUri = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=256&q=80",
                                reportingManagerId = "manager_1",
                                workZoneName = "San Francisco Core",
                                workZoneLat = 37.7749,
                                workZoneLng = -122.4194,
                                workZoneRadiusMeters = 100.0
                            )

                            userDao.insertUser(admin)
                            userDao.insertUser(manager)
                            userDao.insertUser(exec1)
                            userDao.insertUser(exec2)

                            // Add demo tasks
                            val task1 = Task(
                                id = 1,
                                title = "Deliver medical machinery",
                                description = "Deliver and set up the anesthesia station at St. Mary Hospital.",
                                priority = "HIGH",
                                dueDate = System.currentTimeMillis() + 86400000, // tomorrow
                                locationAddress = "St. Mary Hospital, San Francisco",
                                locationLat = 37.7725,
                                locationLng = -122.4533,
                                status = "PENDING",
                                assignedTo = "exec_1",
                                assignedByName = "Morgan LeFay"
                            )

                            val task2 = Task(
                                id = 2,
                                title = "Routine construction inspection",
                                description = "Verify progress on column concrete curing and sign inspection logs.",
                                priority = "MEDIUM",
                                dueDate = System.currentTimeMillis() + 172800000, // 2 days
                                locationAddress = "725 Mission St Site, San Francisco",
                                locationLat = 37.7854,
                                locationLng = -122.4011,
                                status = "IN_PROGRESS",
                                assignedTo = "exec_1",
                                assignedByName = "Morgan LeFay"
                            )

                            val task3 = Task(
                                id = 3,
                                title = "FMCG Inventory Check",
                                description = "Perform audit on the new retail display and report back.",
                                priority = "LOW",
                                dueDate = System.currentTimeMillis() + 259200000, // 3 days
                                locationAddress = "Market St Retail Branch, San Francisco",
                                locationLat = 37.7891,
                                locationLng = -122.4014,
                                status = "PENDING",
                                assignedTo = "exec_2",
                                assignedByName = "Morgan LeFay"
                            )

                            taskDao.insertTask(task1)
                            taskDao.insertTask(task2)
                            taskDao.insertTask(task3)

                            // Initial notifications
                            notifyDao.insertNotification(
                                NotificationModel(
                                    userId = "exec_1",
                                    title = "Welcome to FieldForce Pro",
                                    description = "Set up your check-in selfie to begin your first shift.",
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
