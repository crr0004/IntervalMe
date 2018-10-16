package io.github.crr0004.intervalme.database.routine

/**
 * Represents the merge of [RoutineTableData] and [ExerciseData]
 */
data class RoutineSetData (var routineId: Long, var description: String, var exercises: ArrayList<ExerciseData> = ArrayList() )