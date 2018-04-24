package io.github.crr0004.intervalme.database

import android.arch.persistence.room.*
import android.content.Context

/**
 * Created by crr00 on 24-Apr-18.
 */

@Database(entities = arrayOf(IntervalData::class), version = 1)
@TypeConverters(IntervalTypeConverters::class)
abstract class IntervalMeDatabase : RoomDatabase() {

    abstract fun intervalDataDao(): IntervalDataDOA

    companion object {
        private var INSTANCE: IntervalMeDatabase? = null

        fun getInstance(context: Context): IntervalMeDatabase? {
            if (INSTANCE == null) {
                synchronized(IntervalMeDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            IntervalMeDatabase::class.java, "intervalme.db").allowMainThreadQueries()
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }


    }


}