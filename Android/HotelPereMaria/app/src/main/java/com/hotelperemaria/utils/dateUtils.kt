package com.hotelperemaria.utils

import android.content.Context
import com.hotelperemaria.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    // 默认日期格式，防止 `initialize` 未调用导致 `displayDateFormat` 为空
    private var displayDateFormat: String = "dd/MM/yyyy"
    private const val ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"

    // 使用 ThreadLocal 避免 SimpleDateFormat 线程安全问题
    private val localDateFormat = ThreadLocal<SimpleDateFormat>()
    private val iso8601Format = ThreadLocal<SimpleDateFormat>()

    // 初始化方法（在 Application 类中调用）
    fun initialize(context: Context) {
        displayDateFormat = context.getString(R.string.app_date_format)
    }

    //转换时间戳到本地格式
    fun convertMillisToLocalDate(millis: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        return getLocalDateFormat(timeZone).format(Date(millis))
    }

    // 转换时间戳到 ISO 8601 格式
    fun convertMillisToISO8601(millis: Long, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): String {
        return getISO8601Format(timeZone).format(Date(millis))
    }

    // 转换本地格式日期字符串为毫秒
    fun convertDateToMillis(dateString: String, timeZone: TimeZone = TimeZone.getDefault()): Long? {
        return parseDate(dateString, getLocalDateFormat(timeZone))
    }

    // 转换 ISO 8601 日期字符串为毫秒
    fun convertISO8601ToMillis(dateString: String, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Long? {
        return parseDate(dateString, getISO8601Format(timeZone))
    }

    // 解析日期字符串为毫秒
    private fun parseDate(dateString: String, dateFormat: SimpleDateFormat): Long? {
        return try {
            return dateFormat.parse(dateString)?.time
        } catch (e: ParseException) {
            null
        }
    }

    // **获取本地日期格式**
    private fun getLocalDateFormat(timeZone: TimeZone): SimpleDateFormat {
        return localDateFormat.get() ?: SimpleDateFormat(displayDateFormat, Locale.getDefault()).apply {
            this.timeZone = timeZone
            localDateFormat.set(this)
        }
    }

    // **获取 ISO 8601 格式**
    private fun getISO8601Format(timeZone: TimeZone): SimpleDateFormat {
        return iso8601Format.get() ?: SimpleDateFormat(ISO8601_FORMAT, Locale.ROOT).apply {
            this.timeZone = timeZone
            iso8601Format.set(this)
        }
    }

    fun isDateTodayOrAfter(date: Instant): Boolean {
        val today = Instant.now()
        return date >= today
    }
}
