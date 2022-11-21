package com.example.pc.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.OrdersRepository
import com.example.pc.databinding.ActivityFullOrdersBinding
import com.example.pc.ui.adapters.OrdersFullAdapter
import com.example.pc.ui.adapters.OrdersShortAdapter
import com.example.pc.ui.viewmodels.FullOrdersModel

private const val TAG = "FullOrdersActivity"

class FullOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullOrdersBinding
    private lateinit var fullOrdersModel: FullOrdersModel
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityFullOrdersBinding.inflate(layoutInflater)
        fullOrdersModel = FullOrdersModel(OrdersRepository(RetrofitService.getInstance()))
        userId = intent.getStringExtra("id") as String

        super.onCreate(savedInstanceState)

        fullOrdersModel.apply {
            getSellerOrders(userId)
            sellerOrders.observe(this@FullOrdersActivity) { orders ->
                if(orders == null){
                    Log.i(TAG, "orders are $orders")
                } else {
                    binding.ordersRv.apply {
                        adapter = OrdersFullAdapter(orders, object: OrdersShortAdapter.OnOrderClicked {
                            override fun onOrderClicked(orderId: String) {
                                goToOrderPage(orderId)
                            }
                        })
                        layoutManager = LinearLayoutManager(this@FullOrdersActivity)
                    }
                }
            }
            isTurning.observe(this@FullOrdersActivity) { isTurning ->
                binding.ordersProgressBar.isVisible = isTurning
            }
        }

        setContentView(binding.root)
    }

    private fun goToOrderPage(orderId: String) {
        Log.i(TAG, "onOrderClicked orderId: $orderId")
    }
}