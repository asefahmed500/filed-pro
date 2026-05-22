package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for NotificationEntity operations.
 * Provides methods for querying and manipulating notification records.
 */
@Dao
interface NotificationDao {
    /**
     * Returns a Flow emitting all notifications for a specific user,
     * ordered by timestamp (newest first).
     *
     * @param userId The user's ID
     * @return Flow emitting list of notifications
     */
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    /**
     * Returns a Flow emitting only unread notifications for a user.
     *
     * @param userId The user's ID
     * @return Flow emitting list of unread notifications
     */
    @Query("""
        SELECT * FROM notifications
        WHERE userId = :userId AND isRead = 0
        ORDER BY timestamp DESC
    """)
    fun getUnreadNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    /**
     * Inserts a new notification.
     *
     * @param notification The notification to insert
     * @return The row ID of the inserted notification
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    /**
     * Marks all notifications for a user as read.
     *
     * @param userId The user's ID
     * @return The number of notifications marked as read
     */
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String): Int

    /**
     * Marks a specific notification as read.
     *
     * @param id The notification ID
     * @return The number of notifications marked as read
     */
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int): Int

    /**
     * Deletes all notifications for a user.
     *
     * @param userId The user's ID
     */
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    /**
     * Deletes a specific notification by ID.
     *
     * @param notificationId The notification ID to delete
     */
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteById(notificationId: Int)

    /**
     * Deletes all read notifications for a user.
     *
     * @param userId The user's ID
     * @return The number of notifications deleted
     */
    @Query("DELETE FROM notifications WHERE userId = :userId AND isRead = 1")
    suspend fun deleteReadForUser(userId: String): Int

    /**
     * Counts unread notifications for a user.
     *
     * @param userId The user's ID
     * @return The count of unread notifications
     */
    @Query("""
        SELECT COUNT(*) FROM notifications
        WHERE userId = :userId AND isRead = 0
    """)
    suspend fun countUnreadForUser(userId: String): Int

    /**
     * Retrieves notifications for a user within a date range.
     *
     * @param userId The user's ID
     * @param startTime Start of the date range (timestamp)
     * @param endTime End of the date range (timestamp)
     * @return List of notifications within the range
     */
    @Query("""
        SELECT * FROM notifications
        WHERE userId = :userId
        AND timestamp BETWEEN :startTime AND :endTime
        ORDER BY timestamp DESC
    """)
    suspend fun getNotificationsForDateRange(
        userId: String,
        startTime: Long,
        endTime: Long
    ): List<NotificationEntity>

    /**
     * Returns a Flow emitting the count of unread notifications.
     *
     * @param userId The user's ID
     * @return Flow emitting the unread count
     */
    @Query("""
        SELECT COUNT(*) FROM notifications
        WHERE userId = :userId AND isRead = 0
    """)
    fun getUnreadCountFlow(userId: String): Flow<Int>

    /**
     * Deletes old notifications beyond a certain timestamp.
     *
     * @param beforeTimestamp Delete notifications older than this
     * @return The number of notifications deleted
     */
    @Query("DELETE FROM notifications WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldNotifications(beforeTimestamp: Long): Int
}
