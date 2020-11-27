package xyz.malkki.distributed_systems_2020.common.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.InputStream
import java.io.OutputStream

object JsonHelper {
    val objectMapper: ObjectMapper = ObjectMapper().apply {
        configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false)
        configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
        registerModule(KotlinModule())
        registerModule(JavaTimeModule())
    }

    inline fun <reified T> parse(byteArray: ByteArray): T {
        return objectMapper.readValue(byteArray, T::class.java)
    }

    inline fun <reified T> parse(inputStream: InputStream): T {
        return objectMapper.readValue(inputStream, T::class.java)
    }

    inline fun <reified T> parseList(byteArray: ByteArray): List<T> {
        return objectMapper.readerForListOf(T::class.java).readValue(byteArray) as List<T>
    }

    inline fun <reified T> parseList(inputStream: InputStream): List<T> {
        return objectMapper.readerForListOf(T::class.java).readValue(inputStream) as List<T>
    }

    inline fun <reified T> write(value: T): ByteArray {
        return objectMapper.writeValueAsBytes(value)
    }

    inline fun <reified T> write(value: T, outputStream: OutputStream) {
        objectMapper.writeValue(outputStream, value)
    }
}