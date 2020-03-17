package com.test.cryptoapp.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.test.cryptoapp.API.ApiFactory
import com.test.cryptoapp.Database.AppDatabase
import com.test.cryptoapp.Activity.CoinPriceListActivity
import com.test.cryptoapp.pojo.CoinPriceInfo
import com.test.cryptoapp.pojo.CoinPriceInfoRawData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CoinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val compositeDisposable = CompositeDisposable()

    val priceList = db.coinPriceInfoDao().getPriceList()

    init {
        loadFirstData()
    }

    fun getDetailInfo(fSym: String): LiveData<CoinPriceInfo> {
        return db.coinPriceInfoDao().getPriceInfoAboutCoin(fSym)
    }


    private fun loadFirstData(){
        val disposable = ApiFactory.apiService.getTopCoinsInfo(limit = 10)
            .map { it.data?.map { it.coinInfo?.name }?.joinToString(",") }
            .flatMap { ApiFactory.apiService.getFullPriceList(fSyms = it) }
            .map { getPriceListFromRawData(it) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                db.coinPriceInfoDao().insertPriceList(it)
                loadData()
            }, {
                Log.d(CoinPriceListActivity.TAG, "Failure" + it.message)
            })
    }

    private fun loadData() {
        val disposable = ApiFactory.apiService.getTopCoinsInfo(limit = 10)
            .map { it.data?.map { it.coinInfo?.name }?.joinToString(",") }
            .flatMap { ApiFactory.apiService.getFullPriceList(fSyms = it) }
            .map { getPriceListFromRawData(it) }
            .subscribeOn(Schedulers.newThread())
            .delaySubscription(10, TimeUnit.SECONDS)
            .repeat()
            .retry()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                db.coinPriceInfoDao().insertPriceList(it)
            }, {
                Log.d(CoinPriceListActivity.TAG, "Failure" + it.message)
            })


    }

    private fun getPriceListFromRawData(coinPriceInfoRawData: CoinPriceInfoRawData): List<CoinPriceInfo> {

        val result = ArrayList<CoinPriceInfo>()

        val jsonObject = coinPriceInfoRawData.coinPriceInfoJsonObject
        if (jsonObject == null) {
            return result
        }

        val coinKeySet = jsonObject.keySet()
        for (coinKey in coinKeySet) {
            val currencyJson = jsonObject.getAsJsonObject(coinKey)
            val currencyKeySet = currencyJson.keySet()
            for (currencyKey in currencyKeySet) {
                val priceInfo = Gson().fromJson(
                    currencyJson.getAsJsonObject(currencyKey),
                    CoinPriceInfo::class.java
                )

                result.add(priceInfo)
            }
        }

        return result
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}