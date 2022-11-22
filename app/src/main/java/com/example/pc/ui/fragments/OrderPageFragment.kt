package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.OrdersRepository
import com.example.pc.databinding.FragmentOrderPageBinding
import com.example.pc.ui.activities.FullOrdersActivity
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.viewmodels.OrderPageModel
import com.example.pc.utils.ERROR_MSG
import com.example.pc.utils.toast

private const val TAG = "OrderPageFragment"

class OrderPageFragment : Fragment() {

    private val args: OrderPageFragmentArgs by navArgs()
    private lateinit var binding: FragmentOrderPageBinding
    private lateinit var orderId: String
    private lateinit var orderPageModel: OrderPageModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orderId = args.orderId
        orderPageModel = OrderPageModel(OrdersRepository(RetrofitService.getInstance()))
        Log.i(TAG, "onCreate orderId: $orderId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOrderPageBinding.inflate(inflater, container, false)

        orderPageModel.apply {
            binding.apply {

                getOrderById(orderId)
                order.observe(viewLifecycleOwner) { order ->
                    if (order != null) {
                        Log.i(TAG, "order : $order")

                        id.text = order.orderId

                    } else {
                        requireContext().toast(ERROR_MSG, Toast.LENGTH_SHORT)
                        reloadActivity()
                    }
                }
                isTurning.observe(viewLifecycleOwner) { isTurning ->
                    orderPageProgressbar.isVisible = isTurning
                }
            }
        }

        return binding.root
    }

    private fun reloadActivity() {
        val i = Intent(requireActivity(), FullOrdersActivity::class.java)
        requireActivity().finish()
        requireActivity().overridePendingTransition(0, 0)
        startActivity(i)
        requireActivity().overridePendingTransition(0, 0)
    }

}