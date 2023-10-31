package com.example.android.politicalpreparedness.representative

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.politicalpreparedness.election.ElectionsViewModel
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel(application: Application): AndroidViewModel(application) {

    val address = MutableLiveData<Address>()
    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    var client: CivicsApiService = CivicsApi.retrofitService

    fun getRepresentatives() {
        viewModelScope.launch {
            try {
                val (offices, officials) = client.getRepresentatives(address.value!!.toFormattedString())
                _representatives.postValue(offices.flatMap { office ->
                    office.getRepresentatives(
                        officials
                    )
                })
            } catch (e: Exception) {
                _representatives.value = emptyList()
                Log.d("RepresentativeViewModel", e.toString())
            }
        }
    }

    /**
     *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official :

    val (offices, officials) = getRepresentativesDeferred.await()
    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data housing representatives

     */
}
class Factory(val app: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ElectionsViewModel::class.java)) {
            return ElectionsViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
