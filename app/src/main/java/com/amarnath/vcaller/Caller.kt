package com.amarnath.vcaller

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


data class Truecaller(
    val data: List<Data>
) {
    data class Data(
        val id: String,
        val name: String,
        val gender: String,
        val score: Double,
        val phones: List<Phone>,
        val addresses: List<Address>,
        val internetAddresses: List<InternetAddress>
    ) {
        data class Phone(
            val e164Format: String,
            val numberType: String,
            val nationalFormat: String,
            val dialingCode: Int,
            val countryCode: String,
            val carrier: String,
            val type: String
        )

        data class Address(
            val address: String,
            val city: String,
            val countryCode: String,
            val timeZone: String,
            val type: String
        )

        data class InternetAddress(
            val id: String,
            val service: String,
            val caption: String,
            val type: String
        )
    }
}

fun parseAddress(address: Truecaller.Data.Address): String {
    var result = ""
    if (address.address.isNotEmpty()) {
        result += address.address
    }
    if (address.city.isNotEmpty()) {
        if (result.isNotEmpty()) {
            result += ", "
        }
        result += address.city
    }
    if (address.countryCode.isNotEmpty()) {
        if (result.isNotEmpty()) {
            result += ", "
        }
        result += address.countryCode
    }
    if (address.timeZone.isNotEmpty()) {
        if (result.isNotEmpty()) {
            result += ", "
        }
        result += address.timeZone
    }
    return result
}

fun parseInternetAddress(internetAddress: Truecaller.Data.InternetAddress): String {
    if (internetAddress.id.isNotEmpty()) {
        return internetAddress.id
    }
    var result = ""
    if (internetAddress.service.isNotEmpty()) {
        result += internetAddress.service
    }
    if (internetAddress.caption.isNotEmpty()) {
        if (result.isNotEmpty()) {
            result += ", "
        }
        result += internetAddress.caption
    }
    if (internetAddress.type.isNotEmpty()) {
        if (result.isNotEmpty()) {
            result += ", "
        }
        result += internetAddress.type
    }
    return result
}


fun getTruecallerDetails(phoneNum: String): Truecaller? {
    val client = OkHttpClient()
    val url = "https://search5-noneu.truecaller.com/v2/search?q=$phoneNum&countryCode=IN&type=4&locAddr=&encoding=json"
    val request = Request.Builder()
        .url(url)
        .header("user-agent", "Truecaller/14.32.8 (Android;14)")
        .header("accept", "application/x-protobuf")
        .build()

    return try {
        val response: Response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            return null
        }
        // parseProtoBuf(response.body?.byteStream()) // private fn
        val responseBody = response.body?.string()
        responseBody?.let {
            Gson().fromJson(it, Truecaller::class.java)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        null
    }
}