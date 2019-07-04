package io.kay.DataService

import io.kay.config.SQLConfig
import io.kay.model.Part
import io.kay.model.WorkDay
import java.sql.Date
import java.sql.DriverManager
import java.time.LocalDate


class SQLDataService(private val config: SQLConfig) : DataService {

    val connection = DriverManager.getConnection(config.url)

    init {
        val statement = connection.createStatement()

        statement.executeUpdate("create table if not exists WORK_DAY(day DATE PRIMARY_KEY)")
        statement.executeUpdate(
            "create table if not exists " +
                    "WORK_PARTS(day DATE PRIMARY_KEY, start DATE PRIMARY_KEY, end DATE PRIMARY_KEY, " +
                    "FOREIGN KEY (day) REFERENCES WORK_DAY(day))"
        )
    }

    override fun upsertWorkDay(day: WorkDay) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getWorkDay(date: LocalDate): WorkDay? {
        val statement = connection.prepareStatement("select * from WORK_PARTS where day = ?")
        statement.setDate(1, toSQLDate(date))
        val result = statement.executeQuery()
        val parts = mutableListOf<Part>()
        while (result.next()) {
//            parts.add(Part(fromSQLDate(result.getDate(1)), result.getDate(2)))
        }

        return null
    }

    private fun toSQLDate(date: LocalDate) = Date.valueOf(date)

    private fun fromSQLDate(date: Date) = date.toLocalDate()
}