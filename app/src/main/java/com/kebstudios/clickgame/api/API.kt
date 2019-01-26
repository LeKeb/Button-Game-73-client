package com.kebstudios.clickgame.api

import com.kebstudios.clickgame.api.objects.ClickResponse
import com.kebstudios.clickgame.api.objects.User
import com.kebstudios.clickgame.api.objects.Winner
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.StringBuilder
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object API {

    private const val SITE_URL = "https://button-game73.herokuapp.com"

    fun sendClickRequest(user: User): ClickResponse {
        val connection = (URL(SITE_URL).openConnection() as HttpsURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            doInput = true
            connect()
        }

        val jsonObj = JSONObject()
        jsonObj.put("name", user.name)

        val out = DataOutputStream(BufferedOutputStream(connection.outputStream))
        out.writeBytes(jsonObj.toString())

        out.flush()

        val responseJson = JSONObject(getResponseString(connection.inputStream))

        connection.disconnect()

        return ClickResponse(responseJson.getInt("prize"), responseJson.getInt("next_win"))
    }

    fun sendGetWinnersRequest(): List<Winner> {
        val connection = (URL("$SITE_URL/winners").openConnection() as HttpsURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            doOutput = false
            doInput = true
            connect()
        }

        val responseJson = JSONObject(getResponseString(connection.inputStream)).getJSONArray("winners")

        connection.disconnect()

        val result = mutableListOf<Winner>()

        for (i in 0 until responseJson.length()) {
            val obj = responseJson.getJSONObject(i)
            val user = User(obj.getString("name"))
            val winner = Winner(user, obj.getInt("prize"))

            result.add(winner)
        }

        return result
    }

    private fun getResponseString(input: InputStream): String {
        val reader = BufferedReader(InputStreamReader(input))

        val response = StringBuilder()

        var line = reader.readLine()

        while (line != null) {
            response.append(line)
            line = reader.readLine()
        }

        return response.toString()
    }
}