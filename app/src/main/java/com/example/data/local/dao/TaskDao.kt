package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TaskEntity operations.
 * Provides methods for querying and manipulating task records.
 */
@Dao
interface TaskDao {
    /**
     * Returns a Flow emitting all tasks assigned to a specific employee,
     * ordered by due date (earliest first).
     *
     * @param employeeId The employee's user ID
     * @return Flow emitting list of assigned tasks
     */
    @Query("SELECT * FROM tasks WHERE assignedTo = :employeeId ORDER BY dueDate ASC")
    fun getTasksForEmployee(employeeId: String): Flow<List<TaskEntity>>

    /**
     * Returns a Flow emitting all tasks in the system,
     * ordered by due date (earliest first).
     *
     * @return Flow emitting list of all tasks
     */
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    /**
     * Retrieves all tasks synchronously.
     *
     * @return List of all tasks
     */
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    suspend fun getAllTasks(): List<TaskEntity>

    /**
     * Inserts a new task.
     *
     * @param task The task to insert
     * @return The row ID of the inserted task
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    /**
     * Updates an existing task.
     *
     * @param task The task with updated values
     */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /**
     * Deletes a task by ID.
     *
     * @param taskId The task ID to delete
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    /**
     * Retrieves all tasks with a specific status.
     *
     * @param status The status to filter by
     * @return List of tasks with the specified status
     */
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY dueDate ASC")
    suspend fun getTasksByStatus(status: String): List<TaskEntity>

    /**
     * Retrieves all tasks with a specific priority.
     *
     * @param priority The priority to filter by
     * @return List of tasks with the specified priority
     */
    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY dueDate ASC")
    suspend fun getTasksByPriority(priority: String): List<TaskEntity>

    /**
     * Retrieves all overdue tasks (past due date and not completed).
     *
     * @param currentTime The current time to compare against
     * @return List of overdue tasks
     */
    @Query("""
        SELECT * FROM tasks
        WHERE dueDate < :currentTime
        AND status != 'COMPLETED'
        ORDER BY dueDate ASC
    """)
    suspend fun getOverdueTasks(currentTime: Long = System.currentTimeMillis()): List<TaskEntity>

    /**
     * Counts tasks by status for a specific employee.
     *
     * @param employeeId The employee's user ID
     * @param status The status to count
     * @return The count of tasks with the specified status
     */
    @Query("""
        SELECT COUNT(*) FROM tasks
        WHERE assignedTo = :employeeId AND status = :status
    """)
    suspend fun countTasksByStatusForEmployee(employeeId: String, status: String): Int

    /**
     * Retrieves tasks for multiple employees.
     *
     * @param employeeIds List of employee IDs
     * @return List of tasks assigned to any of the employees
     */
    @Query("SELECT * FROM tasks WHERE assignedTo IN (:employeeIds) ORDER BY dueDate ASC")
    suspend fun getTasksForEmployees(employeeIds: List<String>): List<TaskEntity>
}
