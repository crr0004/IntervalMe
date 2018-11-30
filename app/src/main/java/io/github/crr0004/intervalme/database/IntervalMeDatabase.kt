package io.github.crr0004.intervalme.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import android.content.Context
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsDao
import io.github.crr0004.intervalme.database.analytics.IntervalAnalyticsData
import io.github.crr0004.intervalme.database.routine.ExerciseData
import io.github.crr0004.intervalme.database.routine.RoutineDao
import io.github.crr0004.intervalme.database.routine.RoutineTableData

/**
 * Created by crr00 on 24-Apr-18.
 */

@Database(entities = [IntervalData::class,
    IntervalRunProperties::class,
    IntervalAnalyticsData::class,
    RoutineTableData::class,
    ExerciseData::class], version = 19)
@TypeConverters(IntervalTypeConverters::class)
abstract class IntervalMeDatabase : RoomDatabase() {

    abstract fun intervalDataDao(): IntervalDataDAO
    abstract fun propertiesDao(): IntervalRunPropertiesDOA
    abstract fun intervalAnalyticsDao(): IntervalAnalyticsDao
    abstract fun routineDao(): RoutineDao

    companion object {
        private var INSTANCE: IntervalMeDatabase? = null
        private var TEMP_INSTANCE: IntervalMeDatabase? = null
        var USING_TEMP_DATABASE = false

        fun getInstance(context: Context): IntervalMeDatabase?{
            return if(USING_TEMP_DATABASE){
                getTemporaryInstance(context)
            }else{
                getSavedInstance(context)
            }
        }

        private fun getSavedInstance(context: Context): IntervalMeDatabase? {
            if (INSTANCE == null) {
                synchronized(IntervalMeDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context,
                            IntervalMeDatabase::class.java, "intervalme.db")
                            .addMigrations(
                                    MIGRATION_11_12,
                                    MIGRATION_12_13,
                                    MIGRATION_13_14,
                                    MIGRATION_14_17,
                                    MIGRATION_15_17,
                                    MIGRATION_17_18,
                                    MIGRATION_18_19)
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE?.close()
            TEMP_INSTANCE?.close()
            INSTANCE = null
            TEMP_INSTANCE = null
        }

        fun getTemporaryInstance(applicationContext: Context): IntervalMeDatabase? {
            if(TEMP_INSTANCE == null) {
                TEMP_INSTANCE = Room.inMemoryDatabaseBuilder(applicationContext,
                        IntervalMeDatabase::class.java).allowMainThreadQueries().build()
            }
            return TEMP_INSTANCE
        }

        private val MIGRATION_11_12 = object: Migration(11, 12){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `IntervalAnalytics` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `label` TEXT, `group` TEXT NOT NULL, `ownerOfGroup` INTEGER NOT NULL, `lastModified` INTEGER NOT NULL, `duration` INTEGER NOT NULL, `groupPosition` INTEGER NOT NULL)")
            }
        }
        private val MIGRATION_12_13 = object: Migration(12, 13){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `IntervalAnalytics` ADD COLUMN loops INTEGER NOT NULL DEFAULT -1")
            }
        }
        private val MIGRATION_13_14 = object: Migration(13, 14){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `IntervalAnalytics` ADD COLUMN groupName TEXT DEFAULT ''")
                database.execSQL("UPDATE IntervalAnalytics set groupName = (select Interval.label from Interval where Interval.`group`=IntervalAnalytics.`group`)")
            }
        }
        private val MIGRATION_14_17 = object: Migration(14, 17){
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("CREATE TABLE IF NOT EXISTS `Routine` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `description` TEXT NOT NULL)")
                _db.execSQL("CREATE TABLE IF NOT EXISTS `Exercise` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `routineId` INTEGER NOT NULL, `description` TEXT NOT NULL, `lastModified` INTEGER NOT NULL, `value0` TEXT NOT NULL, `value1` TEXT NOT NULL, `value2` TEXT NOT NULL, `isDone` INTEGER NOT NULL, FOREIGN KEY(`routineId`) REFERENCES `Routine`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
            }
        }
        private val MIGRATION_15_17 = object : Migration(15, 17){
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("CREATE TABLE IF NOT EXISTS `Routine` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `description` TEXT NOT NULL)")
                _db.execSQL("DROP TABLE `Exercise`")
                _db.execSQL("CREATE TABLE IF NOT EXISTS `Exercise` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `routineId` INTEGER NOT NULL, `description` TEXT NOT NULL, `lastModified` INTEGER NOT NULL, `value0` TEXT NOT NULL, `value1` TEXT NOT NULL, `value2` TEXT NOT NULL, `isDone` INTEGER NOT NULL, FOREIGN KEY(`routineId`) REFERENCES `Routine`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")

            }
        }
        private val MIGRATION_17_18 = object : Migration(17, 18){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `Routine` ADD COLUMN isTemplate INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_18_19 = object : Migration(18,19){
            override fun migrate(sb: SupportSQLiteDatabase) {
                sb.execSQL("ALTER TABLE `Routine` ADD COLUMN isDone INTEGER NOT NULL DEFAULT 0")
            }
        }
    }


}