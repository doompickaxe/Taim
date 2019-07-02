package io.kay.DataService

import io.kay.model.Part
import io.kay.model.WorkDay
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class ExcelDataService : DataService {

    val filename = ExcelDataService::class.java.getResource("/time.xlsx").file
    var xlsx: XSSFWorkbook = XSSFWorkbook(File(filename))
    val dateFormatter = SimpleDateFormat("YYYY-MM-dd")
    val timeFormatter = SimpleDateFormat("HH:mm")

    override fun upsertWorkDay(workDay: WorkDay) {
        if (workDay.parts.size > 3)
            throw RuntimeException("Cannot deal with more than 3 parts")

        val month = workDay.day.month.value
        val currentSheet = xlsx.getSheet(if (month < 10) "0$month" else "$month")
        val currentRow = searchDay(currentSheet, workDay.day)!!

        val relevantCells = relevantCells(currentRow)
        for (i in 0 until workDay.parts.size) {
            val start = workDay.parts[i].start
            val end = workDay.parts[i].end
            relevantCells[i * 2].setCellValue(start.hour / 24.0 + start.minute / (24.0 * 60.0))
            if (end != null)
                relevantCells[i * 2 + 1].setCellValue(end.hour / 24.0 + end.minute / (24.0 * 60.0))
        }

        save()
    }

    override fun getWorkDay(date: LocalDate): WorkDay? {
        val month = date.month.value
        val currentSheet = xlsx.getSheet(if (month < 10) "0$month" else "$month")
        val currentRow = searchDay(currentSheet, date) ?: return null
        val excelParts = extractParts(currentRow)

        return WorkDay(date, convertToPart(excelParts))
    }

    private fun searchDay(currentSheet: XSSFSheet, date: LocalDate): Row? {
        return currentSheet
            .drop(4)
            .find { toLocalDate(it.getCell(1).dateCellValue) == date }
    }

    private fun extractParts(currentRow: Row): List<LocalTime> {
        return relevantCells(currentRow)
            .mapNotNull { it.dateCellValue }
            .map { toLocalTime(it) }
    }

    private fun relevantCells(currentRow: Row): List<Cell> {
        return currentRow
            .drop(3)
            .take(11)
            .chunked(2)
            .map { it.first() }
    }

    private fun convertToPart(excelParts: List<LocalTime>): List<Part> {
        val parts = mutableListOf<Part>()
        for (i in 1..excelParts.size step 2)
            parts.add(Part(excelParts[i - 1], excelParts[i]))

        return parts
            .filterNot { it.start == LocalTime.MIDNIGHT && it.end?.equals(LocalTime.MIDNIGHT) ?: false }
            .toList()
    }

    private fun readFile() {
        xlsx = XSSFWorkbook(File(filename))
    }

    private fun save() {
        val newFile = filename.replace("time.xlsx", "newTime.xlsx")
        xlsx.write(FileOutputStream(File(newFile)))
        xlsx.close()
        val oldFilename = filename.replace("time.xlsx", "time.xlsx.old")
        File(oldFilename).delete()
        File(filename).renameTo(File(oldFilename))
        File(newFile).renameTo(File(filename.replace("newTime.xlsx", "time.xlsx")))
        readFile()
    }

    fun toLocalDate(date: Date) = LocalDate.parse(dateFormatter.format(date))
    fun toLocalTime(date: Date) = LocalTime.parse(timeFormatter.format(date))
}

fun main() {
    val workDay = ExcelDataService().getWorkDay(LocalDate.of(2019, 6, 28))!!
    println("${workDay.day}:")
    workDay.parts.forEach { println("${it.start} -> ${it.end}") }

    val newWorkDay =
        WorkDay(LocalDate.of(2019, 6, 28), listOf(Part(LocalTime.now(), LocalTime.now())))
    ExcelDataService().upsertWorkDay(newWorkDay)
}
