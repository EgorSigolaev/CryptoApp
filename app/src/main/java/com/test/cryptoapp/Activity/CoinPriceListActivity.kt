package com.test.cryptoapp.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.test.cryptoapp.Adapter.CoinInfoAdapter
import com.test.cryptoapp.POJO.CoinPriceInfo
import com.test.cryptoapp.R
import com.test.cryptoapp.ViewModel.CoinViewModel
import kotlinx.android.synthetic.main.activity_coin_price_list.*


class CoinPriceListActivity : AppCompatActivity() {

    companion object{
        const val TAG = "LOG_DEBUG"
    }

    private lateinit var viewModel: CoinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_price_list)

        val adapter = CoinInfoAdapter(this)
        adapter.onCoinClickListener = object : CoinInfoAdapter.OnCoinClickListener{
            override fun onCoinClick(coinPriceInfo: CoinPriceInfo) {

                val intent = CoinDetailActivity.newIntent(
                    this@CoinPriceListActivity,
                    coinPriceInfo.fromSymbol
                )
                startActivity(intent)

            }

        }
        rvCoinPriceList.adapter = adapter
        viewModel = ViewModelProvider(this)[CoinViewModel::class.java]
        viewModel.priceList.observe(this, Observer {
            adapter.coinInfoList = it
        })

    }




}
