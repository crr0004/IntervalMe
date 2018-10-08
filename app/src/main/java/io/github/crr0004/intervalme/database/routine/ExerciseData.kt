package io.github.crr0004.intervalme.database.routine

import java.util.*

data class ExerciseData(val id: Long = 0, val description: String, val value: String, val lastModified: Date) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExerciseData

        if (description != other.description) return false
        if (value != other.value) return false
        if (lastModified != other.lastModified) return false

        return true
    }

    override fun hashCode(): Int {
        var result = description.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + lastModified.hashCode()
        return result
    }

}
