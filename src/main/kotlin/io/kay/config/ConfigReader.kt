package io.kay.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.kay.DataService.DataService
import io.kay.DataService.ExcelDataService
import io.kay.DataService.LocalDataService
import io.kay.DataService.SQLDataService
import java.io.File

class ConfigReader {

    fun getDataService(): DataService {
        val config = ConfigFactory.parseFile(readFile())
        println(config.extract<DataSource>("data-source"))
        return when(config.extract<DataSource>("data-source")) {
            DataSource.IN_MEMORY -> LocalDataService()
            DataSource.EXCEL -> createExcelDataService(config)
            DataSource.SQL -> createSQLDataService(config)
        }
    }

    private fun createExcelDataService(config: Config) = ExcelDataService(config.extract())

    private fun createSQLDataService(config: Config) = SQLDataService(config.extract())

    private fun readFile() = File(ConfigReader::class.java.getResource("/application.conf").file)
}