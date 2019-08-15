package io.kay.dataService

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.isStatusRedirection
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import io.kay.model.*
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

const val TAIMA = "http://me.local.com:8080/rest"

class TaimaDataService {

    val mapper = with(ObjectMapper()) {
        dateFormat = SimpleDateFormat("yyyy-MM-dd")
        registerModules(KotlinModule(), JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    fun fetchWorkTimeForDay(date: LocalDate): LocalTime {
        val formattedDay = formatDay(date)
        val (_, _, result) = "$TAIMA/user/conditions/$formattedDay"
            .httpGet()
            .allowRedirects(false)
            .responseJson()

        return when (result) {
            is Result.Failure -> throw RuntimeException(result.error)
            is Result.Success -> result.get().obj().getString(date.dayOfWeek.name.toLowerCase()).toTime()
        }
    }

    fun fetchConditions(date: LocalDate): Conditions {
        val formattedDay = formatDay(date)
        val (_, _, result) = "$TAIMA/user/conditions/$formattedDay"
            .httpGet()
            .allowRedirects(false)
            .responseJson()

        return when (result) {
            is Result.Failure -> throw RuntimeException(result.error)
            is Result.Success -> mapper.readValue(result.get().content, Conditions::class.java)
        }
    }

    fun getWorkDay(date: LocalDate): WorkDay? {
        val formattedDay = formatDay(date)
        val (_, response, result) = "$TAIMA/user/$formattedDay"
            .httpGet()
            .allowRedirects(false)
            .responseJson()

        if (response.isStatusRedirection)
            return null

        return when (result) {
            is Result.Failure -> throw RuntimeException(result.error)
            is Result.Success -> {
                val parts = result.get().array()
                    .map { it as JSONObject }
                    .map { workPartFromJson(it) }
                WorkDay(date, parts)
            }
        }
    }

    fun getFreePart(date: LocalDate): FreePart {
        val formattedDay = formatDay(date)
        val (_, _, result) = "$TAIMA/user/$formattedDay/free"
            .httpGet()
            .responseJson()

        return when (result) {
            is Result.Failure -> throw RuntimeException(result.error)
            is Result.Success -> freePartFromJson(result.get().obj())
        }
    }

    fun upsertWorkDay(day: WorkDay, remoteFreePart: FreePart?) {
        val formattedDay = formatDay(day.day)
        deleteFreePart(remoteFreePart, formattedDay)

        val (new, edited) = day.parts.partition { it.id == null }
        new.forEach {
            "$TAIMA/user/$formattedDay/work"
                .httpPost()
                .jsonBody(mapper.writeValueAsString(it))
                .response { result ->
                    when (result) {
                        is Result.Failure -> throw RuntimeException(result.error)
                        is Result.Success -> null
                    }
                }
        }

        val (deleted, updated) = edited.partition { it.start == null && it.end == null }
        updated.forEach {
            val (_, _, result) = "$TAIMA/user/$formattedDay/work/${it.id}"
                .httpPut()
                .jsonBody(mapper.writeValueAsString(it))
                .responseJson()

            when (result) {
                is Result.Failure -> throw RuntimeException(result.error)
                is Result.Success -> println("yay: ${result.value.content}")
            }
        }

        deleted.forEach {
            "$TAIMA/user/$formattedDay/work/${it.id}"
                .httpDelete()
                .response { result ->
                    when (result) {
                        is Result.Success -> null
                        is Result.Failure -> throw RuntimeException(result.error)
                    }
                }
        }
    }

    fun enterWorkFreeDay(day: WorkDay, freeType: FreeType, remoteFreePart: FreePart?) {
        val formattedDay = formatDay(day.day)
        val remoteParts = day.parts.filterNot { it.id == null }
        remoteParts.forEach {
            "$TAIMA/user/$formattedDay/work/${it.id}"
                .httpDelete()
                .response { result ->
                    when (result) {
                        is Result.Failure -> throw RuntimeException(result.error)
                        is Result.Success -> null
                    }
                }
        }

        deleteFreePart(remoteFreePart, formattedDay)

        "$TAIMA/user/$formattedDay/free"
            .httpPost()
            .jsonBody(mapper.writeValueAsString(FreePart(freeType)))
            .response { result ->
                when (result) {
                    is Result.Failure -> throw RuntimeException(result.error)
                    is Result.Success -> null
                }
            }
    }

    fun downloadReport(from: LocalDate, to: LocalDate) {
        val formattedFrom = formatDay(from)
        val formattedTo = formatDay(to)
        "$TAIMA/user/report"
            .httpGet(listOf(Pair("from", formattedFrom), Pair("to", formattedTo)))
            .response { result ->
                when(result) {
                    is Result.Failure -> throw java.lang.RuntimeException(result.error)
                    is Result.Success -> writeFile(result.value)
                }
            }
    }

    private fun writeFile(bytes: ByteArray) {
        File("report.csv").writeBytes(bytes)
    }

    private fun deleteFreePart(freePart: FreePart?, formattedDay: String) {
        if (freePart?.id != null) {
            "$TAIMA/user/$formattedDay/free/${freePart.id}"
                .httpDelete()
                .response { result ->
                    when (result) {
                        is Result.Failure -> throw RuntimeException(result.error)
                        is Result.Success -> null
                    }
                }
        }
    }

    private fun workPartFromJson(json: JSONObject) =
        Part(
            json.getString("start").toTime(),
            json.optString("end", null)?.toTime(),
            UUID.fromString(json.getString("id"))
        )

    private fun freePartFromJson(json: JSONObject) =
        FreePart(
            FreeType.valueOf(json.getString("reason")),
            UUID.fromString(json.getString("id"))
        )

    private fun conditionsFromJson(json: JSONObject) =
        Conditions(
            json.getString("monday").toTime(),
            json.getString("tuesday").toTime(),
            json.getString("wednesday").toTime(),
            json.getString("thursday").toTime(),
            json.getString("friday").toTime(),
            json.getString("saturday").toTime(),
            json.getString("sunday").toTime(),
            json.getString("from").toDate(),
            json.getString("to").toDate(),
            json.getInt("vacation"),
            UUID.fromString(json.getString("id"))
        )

    private fun String.toTime() = LocalTime.parse(this)
    private fun String.toDate() = LocalDate.parse(this)
    private fun formatDay(date: LocalDate) = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}
