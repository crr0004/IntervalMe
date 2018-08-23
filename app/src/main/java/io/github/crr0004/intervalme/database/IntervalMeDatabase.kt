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

/**
 * Created by crr00 on 24-Apr-18.
 */

@Database(entities = [IntervalData::class, IntervalRunProperties::class, IntervalAnalyticsData::class], version = 13)
@TypeConverters(IntervalTypeConverters::class)
abstract class IntervalMeDatabase : RoomDatabase() {

    abstract fun intervalDataDao(): IntervalDataDAO
    abstract fun propertiesDao(): IntervalRunPropertiesDOA
    abstract fun intervalAnalyticsDao(): IntervalAnalyticsDao

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

        fun getSavedInstance(context: Context): IntervalMeDatabase? {
            if (INSTANCE == null) {
                synchronized(IntervalMeDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            IntervalMeDatabase::class.java, "intervalme.db").allowMainThreadQueries()
                            .addMigrations(MIGRATION_11_12, MIGRATION_12_13)
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

        val MIGRATION_11_12 = object: Migration(11, 12){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `IntervalAnalytics` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `label` TEXT, `group` TEXT NOT NULL, `ownerOfGroup` INTEGER NOT NULL, `lastModified` INTEGER NOT NULL, `duration` INTEGER NOT NULL, `groupPosition` INTEGER NOT NULL)")
            }
        }
        val MIGRATION_12_13 = object: Migration(12, 13){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `IntervalAnalytics` ADD COLUMN loops INTEGER NOT NULL DEFAULT -1")
            }
        }
    }


}