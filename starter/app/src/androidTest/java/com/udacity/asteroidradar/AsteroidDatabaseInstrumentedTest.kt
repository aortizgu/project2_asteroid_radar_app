package com.udacity.asteroidradar

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.asteroidradar.database.AsteroidDao
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.DatabaseAsteroid
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class AsteroidDatabaseInstrumentedTest {

    private lateinit var asteroidDao: AsteroidDao
    private lateinit var db: AsteroidDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AsteroidDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        asteroidDao = db.asteroidDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetAsteroids() {
        for (i in 1..10) {
            val databaseAsteroid = DatabaseAsteroid(
                i.toLong(),
                "codename",
                "closeApproachDate",
                2.0,
                3.0,
                4.0,
                5.0,
                true
            )
            asteroidDao.insertAll(databaseAsteroid)
        }
        assertEquals(asteroidDao.count(), 10)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetByDate() {
        var listDates = listOf("2019-08-12", "2021-08-12", "2021-08-23", "2021-08-23", "2021-10-02", "2022-10-02")
        var j : Long = 0
        for (date in listDates) {
            val databaseAsteroid = DatabaseAsteroid(
                j,
                "codename",
                date,
                2.0,
                3.0,
                4.0,
                5.0,
                true
            )
            asteroidDao.insertAll(databaseAsteroid)
            j += 1
        }
        assertEquals(asteroidDao.countAt("2021-08-23"), 2)
        assertEquals(asteroidDao.countAt("2021-08-24"), 0)
        assertEquals(asteroidDao.countAfter("2021-08-24"), 2)
        assertEquals(asteroidDao.countBefore("2021-08-24"), 4)
        assertEquals(asteroidDao.countBetweenIncluded("2021-08-23", "2021-08-24"), 2)
        assertEquals(asteroidDao.countBetweenIncluded("2021-08-23", "2022-10-02"), 4)
        asteroidDao.deleteUntil("2021-08-23")
        assertEquals(asteroidDao.count(), 4)
    }
}
