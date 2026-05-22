package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for UserEntity operations.
 * Provides methods for querying and manipulating user data in the database.
 */
@Dao
interface UserDao {
    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address to search for
     * @return The user if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The user ID to search for
     * @return The user if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    /**
     * Returns a Flow that emits all executive users.
     * The Flow will emit a new list whenever the executives change.
     *
     * @return Flow emitting list of all executives
     */
    @Query("SELECT * FROM users WHERE role = 'EXECUTIVE'")
    fun getExecutivesFlow(): Flow<List<UserEntity>>

    /**
     * Returns a Flow that emits all users.
     * The Flow will emit a new list whenever the users change.
     *
     * @return Flow emitting list of all users
     */
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    /**
     * Retrieves all executive users synchronously.
     *
     * @return List of all executives
     */
    @Query("SELECT * FROM users WHERE role = 'EXECUTIVE'")
    suspend fun getExecutives(): List<UserEntity>

    /**
     * Returns a Flow that emits team members reporting to a manager.
     *
     * @param managerId The manager's user ID
     * @return Flow emitting list of team members
     */
    @Query("SELECT * FROM users WHERE reportingManagerId = :managerId")
    fun getTeamMembersFlow(managerId: String): Flow<List<UserEntity>>

    /**
     * Inserts or updates a user in the database.
     *
     * @param user The user to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Retrieves all users synchronously.
     *
     * @return List of all users
     */
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    /**
     * Deletes all users from the database.
     */
    @Query("DELETE FROM users")
    suspend fun deleteAll()

    /**
     * Deletes a specific user by ID.
     *
     * @param userId The ID of the user to delete
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteById(userId: String)
}
