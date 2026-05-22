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
            it[email] = email
            it[name] = name
            it[role] = role
            it[phone] = phone
            it[photoUri] = photoUri
            it[reportingManagerId] = reportingManagerId
            it[workZoneName] = workZoneName
            it[workZoneLat] = workZoneLat
            it[workZoneLng] = workZoneLng
            it[workZoneRadiusMeters] = workZoneRadiusMeters
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
            it[title] = title
            it[description] = description
            it[priority] = priority
            it[dueDate] = System.currentTimeMillis() + dueDateOffset
            it[locationAddress] = locationAddress
            it[locationLat] = locationLat
            it[locationLng] = locationLng
            it[status] = "PENDING"
            it[assignedTo] = assignedTo
            it[assignedByName] = assignedByName
        }
    }

    private fun seedSampleAttendance() {
        // Completed attendance for yesterday
        Attendances.insert {
            it[employeeId] = "user_exec_001"
            it[checkInTime] = System.currentTimeMillis() - 86400000
            it[checkOutTime] = System.currentTimeMillis() - 72000000
            it[checkInLat] = 37.3382
            it[checkInLng] = -121.8863
            it[checkInSelfieUri] = "/images/checkin_samples/sample_001.jpg"
            it[checkInNote] = "Started shift on schedule"
            it[checkOutNote] = "Completed 3 deliveries"
            it[checkOutTasksCompleted] = 3
            it[checkOutExpenses] = 15.50
            it[isSyncedOffline] = true
            it[isOutsideGeofence] = false
        }

        // Another completed attendance
        Attendances.insert {
            it[employeeId] = "user_exec_002"
            it[checkInTime] = System.currentTimeMillis() - 86400000
            it[checkOutTime] = System.currentTimeMillis() - 70000000
            it[checkInLat] = 37.7749
            it[checkInLng] = -122.4194
            it[checkInSelfieUri] = "/images/checkin_samples/sample_002.jpg"
            it[checkInNote] = "Morning shift start"
            it[checkOutNote] = "All routes completed"
            it[checkOutTasksCompleted] = 5
            it[checkOutExpenses] = 0.0
            it[isSyncedOffline] = true
            it[isOutsideGeofence] = false
        }
    }

    private fun seedSampleVisits() {
        // Completed visit
        Visits.insert {
            it[executiveId] = "user_exec_001"
            it[customerName] = "St. Mary Hospital"
            it[address] = "St. Mary Hospital, San Francisco"
            it[checkInTime] = System.currentTimeMillis() - 90000000
            it[checkOutTime] = System.currentTimeMillis() - 87000000
            it[notes] = "Equipment delivered and installed. Administrator signed off on delivery."
            it[latitude] = 37.7725
            it[longitude] = -122.4533
            it[signatureBase64] = "sample_signature_base64"
            it[photoUri] = "/images/visit_samples/visit_001.jpg"
            it[reportPdfName] = "VisitReport_StMaryHospital_${System.currentTimeMillis()}.pdf"
        }

        // Another completed visit
        Visits.insert {
            it[executiveId] = "user_exec_002"
            it[customerName] = "General Depot Store"
            it[address] = "725 Mission St Site, San Francisco"
            it[checkInTime] = System.currentTimeMillis() - 180000000
            it[checkOutTime] = System.currentTimeMillis() - 175000000
            it[notes] = "Inspection completed. No issues found."
            it[latitude] = 37.7854
            it[longitude] = -122.4011
            it[signatureBase64] = "sample_signature_base64"
            it[photoUri] = "/images/visit_samples/visit_002.jpg"
            it[reportPdfName] = "VisitReport_GeneralDepot_${System.currentTimeMillis()}.pdf"
        }
    }

    private fun seedSampleFileRecords() {
        // Expense record
        FileRecords.insert {
            it[fileName] = "Gas Receipt - June 15"
            it[category] = "EXPENSE"
            it[fileUri] = "/images/receipts/receipt_001.jpg"
            it[uploadedBy] = "user_exec_001"
            it[uploadedByName] = "Lancelot DuLac"
            it[timestamp] = System.currentTimeMillis() - 3600000
            it[latitude] = 37.7749
            it[longitude] = -122.4194
            it[tags] = "transport, fuel, business"
            it[amount] = 45.50
            it[status] = "APPROVED"
            it[rejectionReason] = null
        }

        // POD record
        FileRecords.insert {
            it[fileName] = "POD - Package #8821"
            it[category] = "POD"
            it[fileUri] = "/images/pods/pod_001.jpg"
            it[uploadedBy] = "user_exec_002"
            it[uploadedByName] = "Guinevere Row"
            it[timestamp] = System.currentTimeMillis() - 7200000
            it[latitude] = 37.7891
            it[longitude] = -122.4014
            it[tags] = "delivery, customer, signature"
            it[amount] = null
            it[status] = "PENDING"
            it[rejectionReason] = null
        }

        // Incident record
        FileRecords.insert {
            it[fileName] = "Traffic Incident Report"
            it[category] = "INCIDENT"
            it[fileUri] = "/images/incidents/incident_001.jpg"
            it[uploadedBy] = "user_exec_001"
            it[uploadedByName] = "Lancelot DuLac"
            it[timestamp] = System.currentTimeMillis() - 14400000
            it[latitude] = 37.7755
            it[longitude] = -122.4199
            it[tags] = "incident, traffic, delay"
            it[amount] = null
            it[status] = "PENDING"
            it[rejectionReason] = null
        }
    }

    private fun seedWelcomeNotifications() {
        val executiveIds = listOf("user_exec_001", "user_exec_002", "user_exec_003")

        executiveIds.forEach { execId ->
            Notifications.insert {
                it[userId] = execId
                it[title] = "Welcome to FieldForce Pro"
                it[description] = "Complete your profile and set up your check-in preferences to get started."
                it[timestamp] = System.currentTimeMillis()
                it[isRead] = false
            }
        }

        // Task assignment notification
        Notifications.insert {
            it[userId] = "user_exec_001"
            it[title] = "New Task Assigned"
            it[description] = "You have been assigned a HIGH priority task: Deliver medical machinery"
            it[timestamp] = System.currentTimeMillis() - 3600000
            it[isRead] = false
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}