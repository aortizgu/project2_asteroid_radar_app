package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface AsteroidDao {
    @Query("select * from databaseasteroid order by closeApproachDate")
    fun getAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Query("select * from databaseasteroid where closeApproachDate <= :date order by closeApproachDate")
    fun getAsteroidsUntil(date: String): LiveData<List<DatabaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroid)

    @Query("delete from databaseasteroid where closeApproachDate < :date")
    fun deleteUntil(date: String): Int

    @Query("select count(*) from databaseasteroid")
    fun count(): Int

    @Query("select count(*) from databaseasteroid where closeApproachDate > :date ")
    fun countAfter(date : String): Int

    @Query("select count(*) from databaseasteroid where closeApproachDate < :date ")
    fun countBefore(date : String): Int

    @Query("select count(*) from databaseasteroid where closeApproachDate == :date ")
    fun countAt(date : String): Int

    @Query("select count(*) from databaseasteroid where closeApproachDate >= :dateUntil and closeApproachDate <= :dateTo ")
    fun countBetweenIncluded(dateUntil: String, dateTo: String): Int
}

@Database(entities = [DatabaseAsteroid::class], version = 1)
abstract class AsteroidDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {
    synchronized(AsteroidDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AsteroidDatabase::class.java,
                "asteroids"
            ).build()
        }
    }
    return INSTANCE
}