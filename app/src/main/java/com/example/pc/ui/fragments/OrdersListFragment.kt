package com.example.pc.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.OrdersRepository
import com.example.pc.databinding.FragmentOrdersListBinding
import com.example.pc.ui.activities.FullOrdersActivity
import com.example.pc.ui.adapters.OrdersFullAdapter
import com.example.pc.ui.adapters.OrdersShortAdapter
import com.example.pc.ui.viewmodels.FullOrdersModel
import com.example.pc.utils.toast

private const val TAG = "OrdersListFragment"

class OrdersListFragment : Fragment() {

    private lateinit var binding: FragmentOrdersListBinding
    private lateinit var userId: String
    private lateinit var fullOrdersModel: FullOrdersModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val activity = requireActivity() as FullOrdersActivity

        fullOrdersModel = FullOrdersModel(OrdersRepository(RetrofitService.getInstance()))
        userId = activity.userId

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrdersListBinding.inflate(inflater, container, false)

        fullOrdersModel.apply {
            getSellerOrders(userId)
            sellerOrders.observe(viewLifecycleOwner) { orders ->
                if (orders == null) {
                    Log.i(TAG, "orders are $orders")
                    doOnFail()
                } else {
                    binding.ordersRv.apply {
                        adapter =
                            OrdersFullAdapter(orders, object : OrdersShortAdapter.OnOrderClicked {
                                override fun onOrderClicked(orderId: String) {
                                    goToOrderPage(orderId)
                                }
                            })
                        layoutManager = LinearLayoutManager(requireContext())
                    }
                }
            }

            isEmpty.observe(viewLifecycleOwner) {
                binding.isOrdersEmpty.isVisible = it
            }

            isTurning.observe(viewLifecycleOwner) { isTurning ->
                binding.ordersProgressBar.isVisible = isTurning
            }
            binding.apply {
                swiperefresh.setOnRefreshListener {
                    getSellerOrders(userId)
                    swiperefresh.isRefreshing = false
                }
            }
        }



        return binding.root
    }

    private fun doOnFail() {
        requireContext().toast(com.example.pc.utils.ERROR_MSG, Toast.LENGTH_SHORT)
        val activity = requireActivity() as FullOrdersActivity
        activity.finish()
    }

    fun goToOrderPage(orderId: String) {
        val action =
            OrdersListFragmentDirections.actionOrdersListFragmentToOrderPageFragment(orderId)
        findNavController().navigate(action)
    }

}