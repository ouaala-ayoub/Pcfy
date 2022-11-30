package com.example.pc.data.models.local

data class DateDb(
    val year: String? = "year",
    val month: String = "month",
    val day: String = "day",
    val hour: String = "hour",
    val min: String = "min"
) {
    override fun toString() = "$year/$month/$day à $hour:$min"
}

fun getDate(date: String): DateDb {
    val test = date.split("T")
    val dateOnly = test[0].split("-")
    val timeOnly = test[1].split(":")
    return DateDb(
        dateOnly[0],
        dateOnly[1],
        dateOnly[2],
        timeOnly[0],
        timeOnly[1]
    )
}