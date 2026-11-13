package com.corphish.quicktools.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

data class ExchangeRate(
    val code: String,
    val rate: Double,
    val unit: Int
)

interface CurrencyRepository {
    suspend fun getExchangeRates(): List<ExchangeRate>?
}

class CurrencyRepositoryImpl @Inject constructor() : CurrencyRepository {
    override suspend fun getExchangeRates(): List<ExchangeRate>? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.boi.org.il/PublicApi/GetExchangeRates")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                return@withContext parseExchangeRates(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    private fun parseExchangeRates(json: String): List<ExchangeRate> {
        val result = mutableListOf<ExchangeRate>()
        val rootObj = JSONObject(json)
        val ratesArray = rootObj.getJSONArray("exchangeRates")
        
        for (i in 0 until ratesArray.length()) {
            val rateObj = ratesArray.getJSONObject(i)
            result.add(
                ExchangeRate(
                    code = rateObj.getString("key"),
                    rate = rateObj.getDouble("currentExchangeRate"),
                    unit = rateObj.getInt("unit")
                )
            )
        }
        return result
    }
}
