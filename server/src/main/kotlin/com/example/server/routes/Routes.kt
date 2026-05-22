package com.example.server.routes

import com.example.server.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun Route.fieldForceRoutes() {
    route("/api") {
        // --- Users API ---
        route("/users") {
            get {
                val allUsersList = DatabaseFactory.dbQuery {
                    Users.selectAll().map { it.toUserDto() }
                }
                call.respond(allUsersList)
            }

            get("/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")
                val user = DatabaseFactory.dbQuery {
                    Users.selectAll().where { Users.id eq id }.singleOrNull()?.let { it.toUserDto() }
                }
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }

            get("/email/{email}") {
                val email = call.parameters["email"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing email")
                val user = DatabaseFactory.dbQuery {
                    Users.selectAll().where { Users.email eq email }.singleOrNull()?.let { it.toUserDto() }
                }
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }

            post {
                val userDto = call.receive<UserDto>()
                DatabaseFactory.dbQuery {
                    val exists = Users.selectAll().where { Users.id eq userDto.id }.count() > 0
                    if (exists) {
                        Users.update({ Users.id eq userDto.id }) {
                            it[email] = userDto.email
                            it[name] = userDto.name
                            it[role] = userDto.role
                            it[phone] = userDto.phone
                            it[photoUri] = userDto.photoUri
                            it[reportingManagerId] = userDto.reportingManagerId
                            it[workZoneName] = userDto.workZoneName
                            it[workZoneLat] = userDto.workZoneLat
                            it[workZoneLng] = userDto.workZoneLng
                            it[workZoneRadiusMeters] = userDto.workZoneRadiusMeters
                        }
                    } else {
                        Users.insert {
                            it[id] = userDto.id
                            it[email] = userDto.email
                            it[name] = userDto.name
                            it[role] = userDto.role
                            it[phone] = userDto.phone
                            it[photoUri] = userDto.photoUri
                            it[reportingManagerId] = userDto.reportingManagerId
                            it[workZoneName] = userDto.workZoneName
                            it[workZoneLat] = userDto.workZoneLat
                            it[workZoneLng] = userDto.workZoneLng
                            it[workZoneRadiusMeters] = userDto.workZoneRadiusMeters
                        }
                    }
                }
                call.respond(HttpStatusCode.OK, userDto)
            }
        }

        // --- Attendance API ---
        route("/attendance") {
            get {
                val list = DatabaseFactory.dbQuery {
                    Attendances.selectAll().orderBy(Attendances.checkInTime to SortOrder.DESC).map { it.toAttendanceDto() }
                }
                call.respond(list)
            }

            get("/employee/{employeeId}") {
                val empId = call.parameters["employeeId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing employeeId")
                val list = DatabaseFactory.dbQuery {
                    Attendances.selectAll().where { Attendances.employeeId eq empId }
                        .orderBy(Attendances.checkInTime to SortOrder.DESC)
                        .map { it.toAttendanceDto() }
                }
                call.respond(list)
            }

            get("/employee/{employeeId}/active") {
                val empId = call.parameters["employeeId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing employeeId")
                val active = DatabaseFactory.dbQuery {
                    Attendances.selectAll().where { (Attendances.employeeId eq empId) and Attendances.checkOutTime.isNull() }
                        .orderBy(Attendances.checkInTime to SortOrder.DESC)
                        .limit(1)
                        .firstOrNull()?.let { it.toAttendanceDto() }
                }
                if (active != null) {
                    call.respond(active)
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            post {
                val att = call.receive<AttendanceDto>()
                val savedId = DatabaseFactory.dbQuery {
                    upsertAttendance(att)
                }
                call.respond(HttpStatusCode.OK, att.copy(id = savedId))
            }
        }

        // --- Tasks API ---
        route("/tasks") {
            get {
                val list = DatabaseFactory.dbQuery {
                    Tasks.selectAll().orderBy(Tasks.dueDate to SortOrder.ASC).map { it.toTaskDto() }
                }
                call.respond(list)
            }

            get("/employee/{employeeId}") {
                val empId = call.parameters["employeeId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing employeeId")
                val list = DatabaseFactory.dbQuery {
                    Tasks.selectAll().where { Tasks.assignedTo eq empId }
                        .orderBy(Tasks.dueDate to SortOrder.ASC)
                        .map { it.toTaskDto() }
                }
                call.respond(list)
            }

            post {
                val task = call.receive<TaskDto>()
                val savedId = DatabaseFactory.dbQuery {
                    upsertTask(task)
                }
                call.respond(HttpStatusCode.OK, task.copy(id = savedId))
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid id")
                val deleted = DatabaseFactory.dbQuery {
                    Tasks.deleteWhere { Tasks.id eq id } > 0
                }
                if (deleted) {
                    call.respond(HttpStatusCode.OK, "Task deleted")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Task not found")
                }
            }
        }

        // --- Visits API ---
        route("/visits") {
            get {
                val list = DatabaseFactory.dbQuery {
                    Visits.selectAll().orderBy(Visits.checkInTime to SortOrder.DESC).map { it.toVisitDto() }
                }
                call.respond(list)
            }

            get("/employee/{employeeId}") {
                val empId = call.parameters["employeeId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing employeeId")
                val list = DatabaseFactory.dbQuery {
                    Visits.selectAll().where { Visits.executiveId eq empId }
                        .orderBy(Visits.checkInTime to SortOrder.DESC)
                        .map { it.toVisitDto() }
                }
                call.respond(list)
            }

            post {
                val visit = call.receive<VisitDto>()
                val savedId = DatabaseFactory.dbQuery {
                    upsertVisit(visit)
                }
                call.respond(HttpStatusCode.OK, visit.copy(id = savedId))
            }
        }

        // --- File Records API ---
        route("/files") {
            get {
                val list = DatabaseFactory.dbQuery {
                    FileRecords.selectAll().orderBy(FileRecords.timestamp to SortOrder.DESC).map { it.toFileRecordDto() }
                }
                call.respond(list)
            }

            get("/employee/{employeeId}") {
                val empId = call.parameters["employeeId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing employeeId")
                val list = DatabaseFactory.dbQuery {
                    FileRecords.selectAll().where { FileRecords.uploadedBy eq empId }
                        .orderBy(FileRecords.timestamp to SortOrder.DESC)
                        .map { it.toFileRecordDto() }
                }
                call.respond(list)
            }

            post {
                val fileRec = call.receive<FileRecordDto>()
                val savedId = DatabaseFactory.dbQuery {
                    upsertFileRecord(fileRec)
                }
                call.respond(HttpStatusCode.OK, fileRec.copy(id = savedId))
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid id")
                val deleted = DatabaseFactory.dbQuery {
                    FileRecords.deleteWhere { FileRecords.id eq id } > 0
                }
                if (deleted) {
                    call.respond(HttpStatusCode.OK, "File record deleted")
                } else {
                    call.respond(HttpStatusCode.NotFound, "File record not found")
                }
            }
        }

        // --- Notifications API ---
        route("/notifications") {
            get("/user/{userId}") {
                val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing userId")
                val list = DatabaseFactory.dbQuery {
                    Notifications.selectAll().where { Notifications.userId eq userId }
                        .orderBy(Notifications.timestamp to SortOrder.DESC)
                        .map { it.toNotificationDto() }
                }
                call.respond(list)
            }

            post {
                val notif = call.receive<NotificationDto>()
                val savedId = DatabaseFactory.dbQuery {
                    Notifications.insert {
                        it[userId] = notif.userId
                        it[title] = notif.title
                        it[description] = notif.description
                        it[timestamp] = notif.timestamp
                        it[isRead] = notif.isRead
                    } get Notifications.id
                }
                call.respond(HttpStatusCode.OK, notif.copy(id = savedId))
            }

            post("/user/{userId}/read-all") {
                val userId = call.parameters["userId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userId")
                DatabaseFactory.dbQuery {
                    Notifications.update({ Notifications.userId eq userId }) {
                        it[isRead] = true
                    }
                }
                call.respond(HttpStatusCode.OK, "All notifications read")
            }

            post("/{id}/read") {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid id")
                DatabaseFactory.dbQuery {
                    Notifications.update({ Notifications.id eq id }) {
                        it[isRead] = true
                    }
                }
                call.respond(HttpStatusCode.OK, "Notification read")
            }
        }

        // --- Bulk Synchronization API ---
        post("/sync") {
            val payload = call.receive<SyncPayloadDto>()
            try {
                DatabaseFactory.dbQuery {
                    payload.attendances.forEach { upsertAttendance(it) }
                    payload.tasks.forEach { upsertTask(it) }
                    payload.visits.forEach { upsertVisit(it) }
                    payload.fileRecords.forEach { upsertFileRecord(it) }
                }
                call.respond(SyncResponseDto(success = true, message = "Synchronized ${
                    payload.attendances.size + payload.tasks.size + payload.visits.size + payload.fileRecords.size
                } items successfully."))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SyncResponseDto(success = false, message = "Sync failed: ${e.localizedMessage}"))
            }
        }
    }
}

// --- Entity Mapping Extensions ---

fun ResultRow.toUserDto() = UserDto(
    id = this[Users.id],
    email = this[Users.email],
    name = this[Users.name],
    role = this[Users.role],
    phone = this[Users.phone],
    photoUri = this[Users.photoUri],
    reportingManagerId = this[Users.reportingManagerId],
    workZoneName = this[Users.workZoneName],
    workZoneLat = this[Users.workZoneLat],
    workZoneLng = this[Users.workZoneLng],
    workZoneRadiusMeters = this[Users.workZoneRadiusMeters]
)

fun ResultRow.toAttendanceDto() = AttendanceDto(
    id = this[Attendances.id],
    employeeId = this[Attendances.employeeId],
    checkInTime = this[Attendances.checkInTime],
    checkOutTime = this[Attendances.checkOutTime],
    checkInLat = this[Attendances.checkInLat],
    checkInLng = this[Attendances.checkInLng],
    checkInSelfieUri = this[Attendances.checkInSelfieUri],
    checkInNote = this[Attendances.checkInNote],
    checkOutNote = this[Attendances.checkOutNote],
    checkOutTasksCompleted = this[Attendances.checkOutTasksCompleted],
    checkOutExpenses = this[Attendances.checkOutExpenses],
    isSyncedOffline = this[Attendances.isSyncedOffline],
    isOutsideGeofence = this[Attendances.isOutsideGeofence]
)

fun ResultRow.toTaskDto() = TaskDto(
    id = this[Tasks.id],
    title = this[Tasks.title],
    description = this[Tasks.description],
    priority = this[Tasks.priority],
    dueDate = this[Tasks.dueDate],
    locationAddress = this[Tasks.locationAddress],
    locationLat = this[Tasks.locationLat],
    locationLng = this[Tasks.locationLng],
    status = this[Tasks.status],
    assignedTo = this[Tasks.assignedTo],
    assignedByName = this[Tasks.assignedByName],
    actualStart = this[Tasks.actualStart],
    actualEnd = this[Tasks.actualEnd],
    proofPhotoUri = this[Tasks.proofPhotoUri],
    proofSignatureBase64 = this[Tasks.proofSignatureBase64],
    managerFeedback = this[Tasks.managerFeedback]
)

fun ResultRow.toVisitDto() = VisitDto(
    id = this[Visits.id],
    executiveId = this[Visits.executiveId],
    customerName = this[Visits.customerName],
    address = this[Visits.address],
    checkInTime = this[Visits.checkInTime],
    checkOutTime = this[Visits.checkOutTime],
    notes = this[Visits.notes],
    latitude = this[Visits.latitude],
    longitude = this[Visits.longitude],
    signatureBase64 = this[Visits.signatureBase64],
    photoUri = this[Visits.photoUri],
    reportPdfName = this[Visits.reportPdfName]
)

fun ResultRow.toFileRecordDto() = FileRecordDto(
    id = this[FileRecords.id],
    fileName = this[FileRecords.fileName],
    category = this[FileRecords.category],
    fileUri = this[FileRecords.fileUri],
    uploadedBy = this[FileRecords.uploadedBy],
    uploadedByName = this[FileRecords.uploadedByName],
    timestamp = this[FileRecords.timestamp],
    latitude = this[FileRecords.latitude],
    longitude = this[FileRecords.longitude],
    tags = this[FileRecords.tags],
    amount = this[FileRecords.amount],
    status = this[FileRecords.status],
    rejectionReason = this[FileRecords.rejectionReason]
)

fun ResultRow.toNotificationDto() = NotificationDto(
    id = this[Notifications.id],
    userId = this[Notifications.userId],
    title = this[Notifications.title],
    description = this[Notifications.description],
    timestamp = this[Notifications.timestamp],
    isRead = this[Notifications.isRead]
)

// --- Helper Functions for Upsert CRUD Operations ---

private fun upsertAttendance(att: AttendanceDto): Int {
    val existing = if (att.id > 0) {
        Attendances.selectAll().where { Attendances.id eq att.id }.firstOrNull()
    } else {
        // Also fallback to find active session for employee if checking out
        Attendances.selectAll().where { (Attendances.employeeId eq att.employeeId) and (Attendances.checkOutTime.isNull()) }
            .orderBy(Attendances.checkInTime to SortOrder.DESC).firstOrNull()
    }

    return if (existing != null) {
        val exId = existing[Attendances.id]
        Attendances.update({ Attendances.id eq exId }) {
            it[Attendances.employeeId] = att.employeeId
            it[Attendances.checkInTime] = att.checkInTime
            if (att.checkOutTime != null) it[Attendances.checkOutTime] = att.checkOutTime
            it[Attendances.checkInLat] = att.checkInLat
            it[Attendances.checkInLng] = att.checkInLng
            it[Attendances.checkInSelfieUri] = att.checkInSelfieUri
            it[Attendances.checkInNote] = att.checkInNote
            if (att.checkOutNote != null) it[Attendances.checkOutNote] = att.checkOutNote
            it[Attendances.checkOutTasksCompleted] = att.checkOutTasksCompleted
            it[Attendances.checkOutExpenses] = att.checkOutExpenses
            it[Attendances.isSyncedOffline] = true // synced now!
            it[Attendances.isOutsideGeofence] = att.isOutsideGeofence
        }
        exId
    } else {
        Attendances.insert {
            it[Attendances.employeeId] = att.employeeId
            it[Attendances.checkInTime] = att.checkInTime
            it[Attendances.checkOutTime] = att.checkOutTime
            it[Attendances.checkInLat] = att.checkInLat
            it[Attendances.checkInLng] = att.checkInLng
            it[Attendances.checkInSelfieUri] = att.checkInSelfieUri
            it[Attendances.checkInNote] = att.checkInNote
            it[Attendances.checkOutNote] = att.checkOutNote
            it[Attendances.checkOutTasksCompleted] = att.checkOutTasksCompleted
            it[Attendances.checkOutExpenses] = att.checkOutExpenses
            it[Attendances.isSyncedOffline] = true
            it[Attendances.isOutsideGeofence] = att.isOutsideGeofence
        } get Attendances.id
    }
}

private fun upsertTask(task: TaskDto): Int {
    val existing = if (task.id > 0) {
        Tasks.selectAll().where { Tasks.id eq task.id }.firstOrNull()
    } else null

    return if (existing != null) {
        Tasks.update({ Tasks.id eq task.id }) {
            it[Tasks.title] = task.title
            it[Tasks.description] = task.description
            it[Tasks.priority] = task.priority
            it[Tasks.dueDate] = task.dueDate
            it[Tasks.locationAddress] = task.locationAddress
            it[Tasks.locationLat] = task.locationLat
            it[Tasks.locationLng] = task.locationLng
            it[Tasks.status] = task.status
            it[Tasks.assignedTo] = task.assignedTo
            it[Tasks.assignedByName] = task.assignedByName
            it[Tasks.actualStart] = task.actualStart
            it[Tasks.actualEnd] = task.actualEnd
            it[Tasks.proofPhotoUri] = task.proofPhotoUri
            it[Tasks.proofSignatureBase64] = task.proofSignatureBase64
            it[Tasks.managerFeedback] = task.managerFeedback
        }
        task.id
    } else {
        Tasks.insert {
            if (task.id > 0) it[Tasks.id] = task.id
            it[Tasks.title] = task.title
            it[Tasks.description] = task.description
            it[Tasks.priority] = task.priority
            it[Tasks.dueDate] = task.dueDate
            it[Tasks.locationAddress] = task.locationAddress
            it[Tasks.locationLat] = task.locationLat
            it[Tasks.locationLng] = task.locationLng
            it[Tasks.status] = task.status
            it[Tasks.assignedTo] = task.assignedTo
            it[Tasks.assignedByName] = task.assignedByName
            it[Tasks.actualStart] = task.actualStart
            it[Tasks.actualEnd] = task.actualEnd
            it[Tasks.proofPhotoUri] = task.proofPhotoUri
            it[Tasks.proofSignatureBase64] = task.proofSignatureBase64
            it[Tasks.managerFeedback] = task.managerFeedback
        } get Tasks.id
    }
}

private fun upsertVisit(visit: VisitDto): Int {
    val existing = if (visit.id > 0) {
        Visits.selectAll().where { Visits.id eq visit.id }.firstOrNull()
    } else null

    return if (existing != null) {
        Visits.update({ Visits.id eq visit.id }) {
            it[Visits.executiveId] = visit.executiveId
            it[Visits.customerName] = visit.customerName
            it[Visits.address] = visit.address
            it[Visits.checkInTime] = visit.checkInTime
            it[Visits.checkOutTime] = visit.checkOutTime
            it[Visits.notes] = visit.notes
            it[Visits.latitude] = visit.latitude
            it[Visits.longitude] = visit.longitude
            it[Visits.signatureBase64] = visit.signatureBase64
            it[Visits.photoUri] = visit.photoUri
            it[Visits.reportPdfName] = visit.reportPdfName
        }
        visit.id
    } else {
        Visits.insert {
            if (visit.id > 0) it[Visits.id] = visit.id
            it[Visits.executiveId] = visit.executiveId
            it[Visits.customerName] = visit.customerName
            it[Visits.address] = visit.address
            it[Visits.checkInTime] = visit.checkInTime
            it[Visits.checkOutTime] = visit.checkOutTime
            it[Visits.notes] = visit.notes
            it[Visits.latitude] = visit.latitude
            it[Visits.longitude] = visit.longitude
            it[Visits.signatureBase64] = visit.signatureBase64
            it[Visits.photoUri] = visit.photoUri
            it[Visits.reportPdfName] = visit.reportPdfName
        } get Visits.id
    }
}

private fun upsertFileRecord(fileRec: FileRecordDto): Int {
    val existing = if (fileRec.id > 0) {
        FileRecords.selectAll().where { FileRecords.id eq fileRec.id }.firstOrNull()
    } else null

    return if (existing != null) {
        FileRecords.update({ FileRecords.id eq fileRec.id }) {
            it[FileRecords.fileName] = fileRec.fileName
            it[FileRecords.category] = fileRec.category
            it[FileRecords.fileUri] = fileRec.fileUri
            it[FileRecords.uploadedBy] = fileRec.uploadedBy
            it[FileRecords.uploadedByName] = fileRec.uploadedByName
            it[FileRecords.timestamp] = fileRec.timestamp
            it[FileRecords.latitude] = fileRec.latitude
            it[FileRecords.longitude] = fileRec.longitude
            it[FileRecords.tags] = fileRec.tags
            it[FileRecords.amount] = fileRec.amount
            it[FileRecords.status] = fileRec.status
            it[FileRecords.rejectionReason] = fileRec.rejectionReason
        }
        fileRec.id
    } else {
        FileRecords.insert {
            if (fileRec.id > 0) it[FileRecords.id] = fileRec.id
            it[FileRecords.fileName] = fileRec.fileName
            it[FileRecords.category] = fileRec.category
            it[FileRecords.fileUri] = fileRec.fileUri
            it[FileRecords.uploadedBy] = fileRec.uploadedBy
            it[FileRecords.uploadedByName] = fileRec.uploadedByName
            it[FileRecords.timestamp] = fileRec.timestamp
            it[FileRecords.latitude] = fileRec.latitude
            it[FileRecords.longitude] = fileRec.longitude
            it[FileRecords.tags] = fileRec.tags
            it[FileRecords.amount] = fileRec.amount
            it[FileRecords.status] = fileRec.status
            it[FileRecords.rejectionReason] = fileRec.rejectionReason
        } get FileRecords.id
    }
}
