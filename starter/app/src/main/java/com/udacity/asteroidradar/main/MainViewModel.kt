package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch
import timber.log.Timber

enum class AsteroidsFilter { SHOW_ALL, SHOW_TODAY, SHOW_WEEK }

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = getDatabase(application)
    val asteroidRepository = AsteroidRepository(database)
    val asteroids = asteroidRepository.asteroids

    init {
        viewModelScope.launch {
            asteroidRepository.refreshAll()
        }
    }

    private val _navigateAsteroidDetails = MutableLiveData<Asteroid>()
    val navigateAsteroidDetails: LiveData<Asteroid>
        get() = _navigateAsteroidDetails

    fun doneNavigating() {
        _navigateAsteroidDetails.value = null
    }

    fun displayAsteroidDetails(asteroid: Asteroid) {
        Timber.d("click on ${asteroid.id}")
        _navigateAsteroidDetails.value = asteroid
    }
}