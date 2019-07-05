package io.kay.DataService


import com.sun.corba.se.spi.orbutil.threadpool.Work
import io.kay.config.SQLConfig
import io.kay.model.Part
import io.kay.model.WorkDay
import java.sql.Date
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.Time
import java.time.LocalDate
import java.time.LocalTime

const val WORKDAY_TABLE = "WORK_DAY"
const val PART_TABLE = "WORK_PARTS"

class SQLDataService(private val config: SQLConfig) : DataService {

    val connection = DriverManager.getConnection(config.url)

    init {
        val statement = connection.createStatement()

        statement.executeUpdate("create table if not exists $WORKDAY_TABLE(day DATE PRIMARY_KEY)")
        statement.executeUpdate(
            "create table if not exists " +
                    "$PART_TABLE(day DATE PRIMARY_KEY, start DATETIME PRIMARY_KEY, end DATETIME PRIMARY_KEY, " +
                    "FOREIGN KEY (day) REFERENCES WORK_DAY(day))"
        )
    }

    override fun upsertWorkDay(day: WorkDay) {
        // To stay SQL agnostic, first fetch data and then insert or update
        val result = getWorkDay(day.day)
        if (result == null)
            insert(day)
        else
            update(day)
    }

    private fun insert(day: WorkDay) {
        var statement = connection.prepareStatement("insert into $WORKDAY_TABLE(day) values (?)")
        statement.setDate(1, toSQLDate(day.day))
        statement.executeUpdate()

        insertWorkParts(day)
    }

    private fun update(day: WorkDay) {
        connection.autoCommit = false
        val statement = connection.prepareStatement("DELETE FROM $PART_TABLE where day = ?")
        statement.setDate(1, toSQLDate(day.day))
        statement.executeUpdate()

        insertWorkParts(day)

        connection.commit()
        connection.autoCommit = true
    }

    private fun insertWorkParts(day: WorkDay) {
        var partInsert = "insert into $PART_TABLE(day, start, end) values "
        for (x in 1 until day.parts.size)
            partInsert += "(?, ?,?), "
        partInsert += "(?, ?, ?)"
        val statement = connection.prepareStatement(partInsert)

        for (part in day.parts) {
            statement.setDate(1, toSQLDate(day.day))
            statement.setTime(2, fromLocalTime(part.start))
            statement.setTime(3, fromLocalTime(part.end))
        }
        statement.executeUpdate()
    }

    override fun getWorkDay(date: LocalDate): WorkDay? {
        val statement = connection.prepareStatement("select * from $PART_TABLE where day = ?")
        statement.setDate(1, toSQLDate(date))
        val result = statement.executeQuery()
        val parts = mutableListOf<Part>()
        while (result.next()) {
            parts.add(Part(fromSQLDate(result.getTime(1)), fromSQLDate(result.getTime(2))))
        }

        return if (parts.isEmpty()) null else WorkDay(date, parts)
    }

    private fun toSQLDate(date: LocalDate) = Date.valueOf(date)

    private fun fromSQLDate(time: Time) = time.toLocalTime()

    private fun fromLocalTime(time: LocalTime?): Time? {
        return if (time == null)
             null
        else
            Time.valueOf(time)
    }
}