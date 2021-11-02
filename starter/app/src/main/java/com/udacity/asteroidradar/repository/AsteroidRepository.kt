package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.main.AsteroidsFilter
import com.udacity.asteroidradar.network.Network
import com.udacity.asteroidradar.network.asDatabaseModel
import com.udacity.asteroidradar.network.parseAsteroidsJsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidDatabase) {

    private val dateFilter = MutableLiveData<String>(null)
    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(Transformations.switchMap(dateFilter) { date ->
            when {
                date == null -> database.asteroidDao.getAsteroids()
                date.isEmpty() -> database.asteroidDao.getAsteroids()
                else -> database.asteroidDao.getAsteroidsUntil(date)
            }
        }) {
            it.asDomainModel()
        }

    private var _pictureOfTheDay = MutableLiveData<String>()
    val pictureOfTheDay: LiveData<String>
        get() = _pictureOfTheDay

    private var _pictureOfTheDayTittle = MutableLiveData<String>()
    val pictureOfTheDayTittle: LiveData<String>
        get() = _pictureOfTheDayTittle

    private suspend fun refreshPictureOfTheDay() {
        withContext(Dispatchers.Default) {
            Timber.d("refreshPictureOfTheDay start")
            val pod = Network.nasa.getPictureOfDayAsync().await()
            Timber.d("refreshPictureOfTheDay end $pod")
            _pictureOfTheDayTittle.postValue(pod.title)
            if (pod.mediaType == "image") {
                _pictureOfTheDay.postValue(pod.url)
            } else {
                // test ok
                //_pictureOfTheDay.postValue("https://file-examples-com.github.io/uploads/2017/10/file_example_JPG_100kB.jpg")
                // test ko
                //_pictureOfTheDay.postValue("https://file-examples-com.github.io/uploads/2017/10/file_example_JPG_100kB.jpgasdcasd")
            }
        }
    }

    private fun todayDate(): String {
        return SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault()).format(
            Calendar.getInstance().time
        )
    }

    fun dateAfter(i: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, i)
        return SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault()).format(
            calendar.time
        )
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            Timber.d("refreshAsteroids start")
            val todayDateFormatted =
                SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault()).format(
                    Calendar.getInstance().time
                )
            // no end date, by default it returns seven days after, we will filter it afterwards
            val asteroidsString =
                Network.nasa.getAsteroidsAsync(startDate = todayDateFormatted).await()
            val asteroidsList = parseAsteroidsJsonResult(JSONObject(asteroidsString))
            database.asteroidDao.insertAll(*asteroidsList.asDatabaseModel())
            database.asteroidDao.deleteUntil(todayDate())
            Timber.d("refreshAsteroids end $asteroidsList")
        }
    }

    suspend fun refreshAll() {
        try {
            refreshPictureOfTheDay()
            refreshAsteroids()
        } catch (e: HttpException) {
            Timber.e("HttpException ${e.message()}")
        } catch (e: SocketException) {
            Timber.e("SocketException ${e.message}")
        } catch (e: SocketTimeoutException) {
            Timber.e("SocketTimeoutException ${e.message}")
        }
    }

    fun updateFilter(filter: AsteroidsFilter) {
        dateFilter.value = when (filter) {
            AsteroidsFilter.SHOW_TODAY -> todayDate()
            AsteroidsFilter.SHOW_WEEK -> dateAfter(Constants.DEFAULT_END_DATE_DAYS)
            else -> null
        }
    }
}