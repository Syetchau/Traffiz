package com.singapore.trafficcamera.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singapore.trafficcamera.interfaces.SubscribeTrafficListener
import com.singapore.trafficcamera.interfaces.TrafficListener
import com.singapore.trafficcamera.models.response.TrafficResponse
import com.singapore.trafficcamera.repository.TrafficRepository
import com.singapore.trafficcamera.tools.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrafficViewModel @Inject constructor(private val trafficRepository: TrafficRepository): ViewModel() {

    fun getTraffic(dateTime: String, listener: TrafficListener) = viewModelScope.launch {
        var error: String?

        try {
            trafficRepository.getTraffic(dateTime = dateTime).let { response ->
                when {
                    response.isSuccessful && response.code() == 200 -> {
                        when {
                            //if api info is healthy and item size > 0
                            response.body()!!.api_info?.status.equals(Constants.HEALTHY, ignoreCase = true) &&
                                    response.body()!!.items.isNotEmpty() -> {
                                listener.onGetTrafficSuccess(data = response.body()!!.items)
                            }
                        }
                    } else -> {
                        error = response.raw().code().toString() + " " + response.raw().message()
                        if (response.message().isNotEmpty()) {
                            error = response.message()
                        }
                        listener.onGetTrafficFailed(error = error!!)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (!e.localizedMessage.isNullOrEmpty()) {
                listener.onGetTrafficFailed(error = e.localizedMessage!!)
            }
        }
    }

    fun listenTrafficData(dateTime: String, listener: SubscribeTrafficListener) = viewModelScope.launch {
        val observable = trafficRepository.listenTrafficData(dateTime = dateTime)

        val queryObserver: Observer<TrafficResponse> = object: Observer<TrafficResponse> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onNext(response: TrafficResponse) {
                //if api info is healthy and item size > 0
                //continue sub and fetch data
                if (response.api_info?.status.equals(Constants.HEALTHY, ignoreCase = true) && response.items.isNotEmpty()) {
                    listener.onSubscribeSuccess(data = response.items)
                }
            }

            override fun onError(e: Throwable) {
                listener.onSubscribeError(error = e.localizedMessage!!)
            }

            override fun onComplete() {

            }
        }

        observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(queryObserver)
    }
}