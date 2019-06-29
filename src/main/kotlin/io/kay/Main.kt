package io.kay

import io.kay.visuals.TaimApp
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import tornadofx.launch
import java.io.File
import java.time.LocalDate
import java.util.*

fun main() {
//    val today = LocalDate.now()
//    val month = today.month.value
//    val xlsx = XSSFWorkbook(FileReader().read())
//    val currentSheet = xlsx.getSheet(if (month < 10) "0$month" else "$month")
//    val currentRow = currentSheet
//        .drop(4)
//        .find { toLocalDate(it.getCell(1).dateCellValue) == today }!!
//    println(currentRow)
    launch<TaimApp>()
}

fun toLocalDate(date: Date) = LocalDate.from(date.toInstant())

class FileReader {
    fun read() = File(FileReader::class.java.getResource("/time.xlsx").file)
}