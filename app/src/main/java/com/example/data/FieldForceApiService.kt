package com.example.data

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface FieldForceApiService {

    // --- Users API ---
    @GET("api/users")
    suspend fun getAllUsers(): List<User>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<User>

    @GET("api/users/email/{email}")
    suspend fun getUserByEmail(@Path("email") email: String): Response<User>

    @POST("api/users")
    suspend fun createOrUpdateUser(@Body user: User): Response<User>

    // --- Attendance API ---
    @GET("api/attendance")
    suspend fun getAllAttendance(): List<Attendance>

    @GET("api/attendance/employee/{employeeId}")
    suspend fun getAttendanceForEmployee(@Path("employeeId") employeeId: String): List<Attendance>

    @GET("api/attendance/employee/{employeeId}/active")
    suspend fun getActiveAttendance(@Path("employeeId") employeeId: String): Response<Attendance>

    @POST("api/attendance")
    suspend fun upsertAttendance(@Body attendance: Attendance): Response<Attendance>

    // --- Tasks API ---
    @GET("api/tasks")
    suspend fun getAllTasks(): List<Task>

    @GET("api/tasks/employee/{employeeId}")
    suspend fun getTasksForEmployee(@Path("employeeId") employeeId: String): List<Task>

    @POST("api/tasks")
    suspend fun upsertTask(@Body task: Task): Response<Task>

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<ResponseBody>

    // --- Visits API ---
    @GET("api/visits")
    suspend fun getAllVisits(): List<Visit>

    @GET("api/visits/employee/{employeeId}")
    suspend fun getVisitsForEmployee(@Path("employeeId") employeeId: String): List<Visit>

    @POST("api/visits")
    suspend fun upsertVisit(@Body visit: Visit): Response<Visit>

    // --- File Records API ---
    @GET("api/files")
    suspend fun getAllFiles(): List<FileRecord>

    @GET("api/files/employee/{employeeId}")
    suspend fun getFilesForEmployee(@Path("employeeId") employeeId: String): List<FileRecord>

    @POST("api/files")
    suspend fun upsertFileRecord(@Body fileRecord: FileRecord): Response<FileRecord>

    @DELETE("api/files/{id}")
    suspend fun deleteFileRecord(@Path("id") id: Int): Response<ResponseBody>

    // --- Notifications API ---
    @GET("api/notifications/user/{userId}")
    suspend fun getNotificationsForUser(@Path("userId") userId: String): List<NotificationModel>

    @POST("api/notifications")
    suspend fun createNotification(@Body notification: NotificationModel): Response<NotificationModel>

    @POST("api/notifications/user/{userId}/read-all")
    suspend fun markAllNotificationsAsRead(@Path("userId") userId: String): Response<ResponseBody>

    @POST("api/notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: Int): Response<ResponseBody>

    // --- Consolidated Sync API ---
    @POST("api/sync")
    suspend fun syncOfflineData(@Body payload: SyncPayload): Response<SyncResponse>

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/"

        fun create(): FieldForceApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(FieldForceApiService::class.java)
        }
    }
}

// Request/Response types for retrofit sync endpoint
data class SyncPayload(
    val attendances: List<Attendance>,
    val tasks: List<Task>,
    val visits: List<Visit>,
    val fileRecords: List<FileRecord>
)

data class SyncResponse(
    val success: Boolean,
    val message: String
)

// Retrofit ResponseBody stub
typealias ResponseBody = okhttp3.ResponseBody
