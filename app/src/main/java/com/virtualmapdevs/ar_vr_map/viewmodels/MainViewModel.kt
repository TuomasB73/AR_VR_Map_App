package com.virtualmapdevs.ar_vr_map.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virtualmapdevs.ar_vr_map.model.Message
import com.virtualmapdevs.ar_vr_map.model.User
import com.virtualmapdevs.ar_vr_map.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel : ViewModel() {

    private val repository = Repository()

    var getMessageMsg: MutableLiveData<Response<Message>> = MutableLiveData()
    var registerUserMsg: MutableLiveData<Response<Message>> = MutableLiveData()
    var loginUserMsg: MutableLiveData<Response<Message>> = MutableLiveData()
    var secureDataMsg: MutableLiveData<Response<Message>> = MutableLiveData()

    fun getMessage() {
        viewModelScope.launch {
            val message = repository.getMessage()
            getMessageMsg.value = message
        }
    }

    fun postUser(user: User) {
        viewModelScope.launch {
            val message = repository.postUser(user)
            registerUserMsg.value = message
        }
    }

    fun registerUser(name: String, password: String) {
        viewModelScope.launch {
            val message = repository.registerUser(name, password)
            registerUserMsg.value = message
        }
    }

    fun loginUser(name: String, password: String) {
        viewModelScope.launch {
            val message = repository.loginUser(name, password)
            loginUserMsg.value = message
        }
    }

    fun getSecureData(token: String) {
        viewModelScope.launch {
            val message = repository.getSecureData(token)
            secureDataMsg.value = message
        }
    }


}