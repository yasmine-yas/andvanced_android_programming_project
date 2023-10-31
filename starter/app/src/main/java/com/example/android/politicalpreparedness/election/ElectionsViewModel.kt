package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase.Companion.getInstance
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.ElectionsRepository
import com.example.android.politicalpreparedness.utils.NavigationCommand
import com.example.android.politicalpreparedness.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import com.example.android.politicalpreparedness.utils.Result

class ElectionsViewModel(app:Application): AndroidViewModel(app) {

    val showToast: SingleLiveEvent<String> = SingleLiveEvent()
    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()

    private val database = getInstance(app)
    private val electionsRepository = ElectionsRepository(database)

    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections

    val savedElections = electionsRepository.getSaveElections()

    init {
        showLoading.value = true
        viewModelScope.launch {
            val result = electionsRepository.refreshElections()
            showLoading.value = false
            when (result) {
                is Result.Success -> {
                    _upcomingElections.value = result.data.elections
                }
                is Result.Error -> {
                    _upcomingElections.value = emptyList()
                    showToast.value = app.getString(R.string.error_upcoming_election)
                }
            }
        }
    }

}
