package com.example.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    private var dataSource: HikariDataSource? = null

    fun init(config: ApplicationConfig) {
        val dbConfig = HikariConfig().apply {
            driverClassName = config.propertyOrNull("database.driver")?.getString() ?: "org.postgresql.Driver"
            jdbcUrl = config.propertyOrNull("database.url")?.getString() ?: "jdbc:postgresql://localhost:5432/fieldforce"
            username = config.propertyOrNull("database.user")?.getString() ?: "postgres"
            password = config.propertyOrNull("database.password")?.getString() ?: "postgres"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        dataSource = HikariDataSource(dbConfig)
        Database.connect(dataSource!!)

        // Auto-create schemas & seed data
        transaction {
            SchemaUtils.create(Users, Attendances, Tasks, Visits, FileRecords, Notifications)
            seedDatabase()
        }
    }

    /**
     * Comprehensive database seeding function
     * Creates initial users, tasks, and sample data for demonstration
     */
    private fun seedDatabase() {
        // Only seed if no users exist
        if (Users.selectAll().count() > 0) {
            return
        }

        // Create Users
        seedUsers()

        // Create Tasks
        seedTasks()

        // Create Sample Attendance
        seedSampleAttendance()

        // Create Sample Visits
        seedSampleVisits()

        // Create Sample File Records
        seedSampleFileRecords()

        // Create Welcome Notifications
        seedWelcomeNotifications()
    }

    private fun seedUsers() {
        // Admin User
        seedUser(
            id = "user_admin_001",
            email = "admin@fieldforce.pro",
            name = "Arthur Pendragon",
            role = "ADMIN",
            phone = "+1 (555) 0101",
            photoUri = "/images/admin_default.png",
            reportingManagerId = null,
            workZoneName = "Headquarters (Zone A)",
            workZoneLat = 37.7749,
            workZoneLng = -122.4194,
            workZoneRadiusMeters = 500.0
        )

        // Manager User
        val managerId = "user_manager_001"
        seedUser(
            id = managerId,
            email = "morgan.lefay@fieldforce.pro",
            name = "Morgan LeFay",
            role = "MANAGER",
            phone = "+1 (555) 0202",
            photoUri = "/images/manager_default.png",
            reportingManagerId = "user_admin_001",
            workZoneName = "Oakland Hub (Zone B)",
            workZoneLat = 37.8044,
            workZoneLng = -122.2711,
            workZoneRadiusMeters = 300.0
        )

        // Executive Users
        seedUser(
            id = "user_exec_001",
            email = "lancelot.dulac@fieldforce.pro",
            name = "Lancelot DuLac",
            role = "EXECUTIVE",
            phone = "+1 (555) 0303",
            photoUri = "/images/exec_default_1.png",
            reportingManagerId = managerId,
            workZoneName = "San Jose Sector",
            workZoneLat = 37.3382,
            workZoneLng = -121.8863,
            workZoneRadiusMeters = 350.0
        )

        seedUser(
            id = "user_exec_002",
            email = "guinevere.row@fieldforce.pro",
            name = "Guinevere Row",
            role = "EXECUTIVE",
            phone = "+1 (555) 0404",
            photoUri = "/images/exec_default_2.png",
            reportingManagerId = managerId,
            workZoneName = "San Francisco Core",
            workZoneLat = 37.7749,
            workZoneLng = -122.4194,
            workZoneRadiusMeters = 150.0
        )

        // Additional Executive for testing
        seedUser(
            id = "user_exec_003",
            email = "percival.val@fieldforce.pro",
            name = "Percival Val",
            role = "EXECUTIVE",
            phone = "+1 (555) 0505",
            photoUri = "/images/exec_default_3.png",
            reportingManagerId = managerId,
            workZoneName = "Daly City District",
            workZoneLat = 37.6879,
            workZoneLng = -122.4702,
            workZoneRadiusMeters = 200.0
        )
    }

    private fun seedUser(
        id: String,
        email: String,
        name: String,
        role: String,
        phone: String,
        photoUri: String,
        reportingManagerId: String?,
        workZoneName: String,
        workZoneLat: Double,
        workZoneLng: Double,
        workZoneRadiusMeters: Double
    ) {
        Users.insert {
            it[Users.id] = id
            it[Users.email] = email
            it[Users.name] = name
            it[Users.role] = role
            it[Users.phone] = phone
            it[Users.photoUri] = photoUri
            it[Users.reportingManagerId] = reportingManagerId
            it[Users.workZoneName] = workZoneName
            it[Users.workZoneLat] = workZoneLat
            it[Users.workZoneLng] = workZoneLng
            it[Users.workZoneRadiusMeters] = workZoneRadiusMeters
        }
    }

    private fun seedTasks() {
        // High Priority Tasks
        seedTask(
            title = "Deliver medical machinery",
            description = "Deliver and set up the anesthesia station at St. Mary Hospital. Requires signature from hospital administrator.",
            priority = "HIGH",
            dueDateOffset = 86400000, // 1 day
            locationAddress = "St. Mary Hospital, San Francisco",
            locationLat = 37.7725,
            locationLng = -122.4533,
            assignedTo = "user_exec_001",
            assignedByName = "Morgan LeFay"
        )

        seedTask(
            title = "Emergency equipment repair",
            description = "Urgent repair of telecommunications cabinet at client site. Customer awaiting restoration.",
            priority = "HIGH",
            dueDateOffset = 43200000, // 12 hours
            locationAddress = "100 Van Ness Ave, San Francisco",
            locationLat = 37.7755,
            locationLng = -122.4199,
            assignedTo = "user_exec_002",
            assignedByName = "Morgan LeFay"
        )

        // Medium Priority Tasks
        seedTask(
            title = "Routine construction inspection",
            description = "Verify progress on column concrete curing and sign inspection logs. Take progress photos.",
            priority = "MEDIUM",
            dueDateOffset = 172800000, // 2 days
            locationAddress = "725 Mission St Site, San Francisco",
            locationLat = 37.7854,
            locationLng = -122.4011,
            assignedTo = "user_exec_001",
            assignedByName = "Morgan LeFay"
        )

        seedTask(
            title = "Inventory audit - Warehouse B",
            description = "Perform monthly inventory count and update system records.",
            priority = "MEDIUM",
            dueDateOffset = 259200000, // 3 days
            locationAddress = "501 Bryant St, San Francisco",
            locationLat = 37.7826,
            locationLng = -122.3934,
            assignedTo = "user_exec_003",
            assignedByName = "Morgan LeFay"
        )

        // Low Priority Tasks
        seedTask(
            title = "FMCG Inventory Check",
            description = "Perform audit on the new retail display and report back with recommendations.",
            priority = "LOW",
            dueDateOffset = 432000000, // 5 days
            locationAddress = "Market St Retail Branch, San Francisco",
            locationLat = 37.7891,
            locationLng = -122.4014,
            assignedTo = "user_exec_002",
            assignedByName = "Morgan LeFay"
        )
    }

    private fun seedTask(
        title: String,
        description: String,
        priority: String,
        dueDateOffset: Long,
        locationAddress: String,
        locationLat: Double,
        locationLng: Double,
        assignedTo: String,
        assignedByName: String
    ) {
        Tasks.insert {
            it[Tasks.title] = title
            it[Tasks.description] = description
            it[Tasks.priority] = priority
            it[Tasks.dueDate] = System.currentTimeMillis() + dueDateOffset
            it[Tasks.locationAddress] = locationAddress
            it[Tasks.locationLat] = locationLat
            it[Tasks.locationLng] = locationLng
            it[Tasks.status] = "PENDING"
            it[Tasks.assignedTo] = assignedTo
            it[Tasks.assignedByName] = assignedByName
        }
    }

    private fun seedSampleAttendance() {
        // Completed attendance for yesterday
        Attendances.insert {
            it[Attendances.employeeId] = "user_exec_001"
            it[Attendances.checkInTime] = System.currentTimeMillis() - 86400000
            it[Attendances.checkOutTime] = System.currentTimeMillis() - 72000000
            it[Attendances.checkInLat] = 37.3382
            it[Attendances.checkInLng] = -121.8863
            it[Attendances.checkInSelfieUri] = "/images/checkin_samples/sample_001.jpg"
            it[Attendances.checkInNote] = "Started shift on schedule"
            it[Attendances.checkOutNote] = "Completed 3 deliveries"
            it[Attendances.checkOutTasksCompleted] = 3
            it[Attendances.checkOutExpenses] = 15.50
            it[Attendances.isSyncedOffline] = true
            it[Attendances.isOutsideGeofence] = false
        }

        // Another completed attendance
        Attendances.insert {
            it[Attendances.employeeId] = "user_exec_002"
            it[Attendances.checkInTime] = System.currentTimeMillis() - 86400000
            it[Attendances.checkOutTime] = System.currentTimeMillis() - 70000000
            it[Attendances.checkInLat] = 37.7749
            it[Attendances.checkInLng] = -122.4194
            it[Attendances.checkInSelfieUri] = "/images/checkin_samples/sample_002.jpg"
            it[Attendances.checkInNote] = "Morning shift start"
            it[Attendances.checkOutNote] = "All routes completed"
            it[Attendances.checkOutTasksCompleted] = 5
            it[Attendances.checkOutExpenses] = 0.0
            it[Attendances.isSyncedOffline] = true
            it[Attendances.isOutsideGeofence] = false
        }
    }

    private fun seedSampleVisits() {
        // Completed visit
        Visits.insert {
            it[Visits.executiveId] = "user_exec_001"
            it[Visits.customerName] = "St. Mary Hospital"
            it[Visits.address] = "St. Mary Hospital, San Francisco"
            it[Visits.checkInTime] = System.currentTimeMillis() - 90000000
            it[Visits.checkOutTime] = System.currentTimeMillis() - 87000000
            it[Visits.notes] = "Equipment delivered and installed. Administrator signed off on delivery."
            it[Visits.latitude] = 37.7725
            it[Visits.longitude] = -122.4533
            it[Visits.signatureBase64] = "sample_signature_base64"
            it[Visits.photoUri] = "/images/visit_samples/visit_001.jpg"
            it[Visits.reportPdfName] = "VisitReport_StMaryHospital_${System.currentTimeMillis()}.pdf"
        }

        // Another completed visit
        Visits.insert {
            it[Visits.executiveId] = "user_exec_002"
            it[Visits.customerName] = "General Depot Store"
            it[Visits.address] = "725 Mission St Site, San Francisco"
            it[Visits.checkInTime] = System.currentTimeMillis() - 180000000
            it[Visits.checkOutTime] = System.currentTimeMillis() - 175000000
            it[Visits.notes] = "Inspection completed. No issues found."
            it[Visits.latitude] = 37.7854
            it[Visits.longitude] = -122.4011
            it[Visits.signatureBase64] = "sample_signature_base64"
            it[Visits.photoUri] = "/images/visit_samples/visit_002.jpg"
            it[Visits.reportPdfName] = "VisitReport_GeneralDepot_${System.currentTimeMillis()}.pdf"
        }
    }

    private fun seedSampleFileRecords() {
        // Expense record
        FileRecords.insert {
            it[FileRecords.fileName] = "Gas Receipt - June 15"
            it[FileRecords.category] = "EXPENSE"
            it[FileRecords.fileUri] = "/images/receipts/receipt_001.jpg"
            it[FileRecords.uploadedBy] = "user_exec_001"
            it[FileRecords.uploadedByName] = "Lancelot DuLac"
            it[FileRecords.timestamp] = System.currentTimeMillis() - 3600000
            it[FileRecords.latitude] = 37.7749
            it[FileRecords.longitude] = -122.4194
            it[FileRecords.tags] = "transport, fuel, business"
            it[FileRecords.amount] = 45.50
            it[FileRecords.status] = "APPROVED"
            it[FileRecords.rejectionReason] = null
        }

        // POD record
        FileRecords.insert {
            it[FileRecords.fileName] = "POD - Package #8821"
            it[FileRecords.category] = "POD"
            it[FileRecords.fileUri] = "/images/pods/pod_001.jpg"
            it[FileRecords.uploadedBy] = "user_exec_002"
            it[FileRecords.uploadedByName] = "Guinevere Row"
            it[FileRecords.timestamp] = System.currentTimeMillis() - 7200000
            it[FileRecords.latitude] = 37.7891
            it[FileRecords.longitude] = -122.4014
            it[FileRecords.tags] = "delivery, customer, signature"
            it[FileRecords.amount] = null
            it[FileRecords.status] = "PENDING"
            it[FileRecords.rejectionReason] = null
        }

        // Incident record
        FileRecords.insert {
            it[FileRecords.fileName] = "Traffic Incident Report"
            it[FileRecords.category] = "INCIDENT"
            it[FileRecords.fileUri] = "/images/incidents/incident_001.jpg"
            it[FileRecords.uploadedBy] = "user_exec_001"
            it[FileRecords.uploadedByName] = "Lancelot DuLac"
            it[FileRecords.timestamp] = System.currentTimeMillis() - 14400000
            it[FileRecords.latitude] = 37.7755
            it[FileRecords.longitude] = -122.4199
            it[FileRecords.tags] = "incident, traffic, delay"
            it[FileRecords.amount] = null
            it[FileRecords.status] = "PENDING"
            it[FileRecords.rejectionReason] = null
        }
    }

    private fun seedWelcomeNotifications() {
        val executiveIds = listOf("user_exec_001", "user_exec_002", "user_exec_003")

        executiveIds.forEach { execId ->
            Notifications.insert {
                it[Notifications.userId] = execId
                it[Notifications.title] = "Welcome to FieldForce Pro"
                it[Notifications.description] = "Complete your profile and set up your check-in preferences to get started."
                it[Notifications.timestamp] = System.currentTimeMillis()
                it[Notifications.isRead] = false
            }
        }

        // Task assignment notification
        Notifications.insert {
            it[Notifications.userId] = "user_exec_001"
            it[Notifications.title] = "New Task Assigned"
            it[Notifications.description] = "You have been assigned a HIGH priority task: Deliver medical machinery"
            it[Notifications.timestamp] = System.currentTimeMillis() - 3600000
            it[Notifications.isRead] = false
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}