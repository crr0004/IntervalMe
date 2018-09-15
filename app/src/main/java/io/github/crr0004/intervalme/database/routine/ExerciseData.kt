package io.github.crr0004.intervalme.database.routine

import java.util.*

data class ExerciseData(val id: Long = 0, val description: String, val valueCount: Int, val values: Array<Int>, val lastModified: Date) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExerciseData

        if (description != other.description) return false
        if (valueCount != other.valueCount) return false
        if (!Arrays.equals(values, other.values)) return false
        if (lastModified != other.lastModified) return false

        return true
    }

    override fun hashCode(): Int {
        var result = description.hashCode()
        result = 31 * result + valueCount
        result = 31 * result + Arrays.hashCode(values)
        result = 31 * result + lastModified.hashCode()
        return result
    }

}
