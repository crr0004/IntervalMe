package io.github.crr0004.intervalme.database.routine

/**
 * Represents the merge of [RoutineTableData] and [ExerciseData]
 */
data class RoutineSetData (var routineId: Long,
                           var description: String,
                           var exercises: ArrayList<ExerciseData> = ArrayList(),
                           var isTemplate: Boolean,
                           var isDone: Boolean) {


    constructor(routine: RoutineSetData) : this(
            routine.routineId,
            routine.description,
            ArrayList(routine.exercises),
            routine.isTemplate,
            routine.isDone)
}