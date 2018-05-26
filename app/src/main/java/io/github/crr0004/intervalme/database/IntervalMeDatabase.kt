package io.github.crr0004.intervalme.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

/**
 * Created by crr00 on 24-Apr-18.
 */

@Database(entities = arrayOf(IntervalData::class), version = 4)
@TypeConverters(IntervalTypeConverters::class)
abstract class IntervalMeDatabase : RoomDatabase() {

    abstract fun intervalDataDao(): IntervalDataDOA

    companion object {
        private var INSTANCE: IntervalMeDatabase? = null

        fun getInstance(context: Context): IntervalMeDatabase? {
            if (INSTANCE == null) {
                synchronized(IntervalMeDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            IntervalMeDatabase::class.java, "intervalme.db").allowMainThreadQueries().fallbackToDestructiveMigration()
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }

        fun getTemporaryInstance(applicationContext: Context): IntervalMeDatabase? {
            return Room.inMemoryDatabaseBuilder(applicationContext,
                    IntervalMeDatabase::class.java).allowMainThreadQueries().build()
        }


    }


}