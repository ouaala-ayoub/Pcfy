package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pc.R
import com.example.pc.data.models.local.OrderStatus
import com.example.pc.data.models.local.OrderStatusRequest
import com.example.pc.data.models.network.Status
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.OrdersRepository
import com.example.pc.databinding.FragmentOrderPageBinding
import com.example.pc.ui.activities.FullOrdersActivity
import com.example.pc.ui.viewmodels.OrderPageModel
import com.example.pc.utils.ERROR_MSG
import com.example.pc.utils.toast
import com.google.android.material.textfield.MaterialAutoCompleteTextView

private const val TAG = "OrderPageFragment"

class OrderPageFragment : Fragment() {

    private val args: OrderPageFragmentArgs by navArgs()
    private lateinit var binding: FragmentOrderPageBinding
    private lateinit var orderPageModel: OrderPageModel
    private lateinit var orderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orderId = args.orderId
        orderPageModel = OrderPageModel(OrdersRepository(RetrofitService.getInstance()))
        Log.i(TAG, "onCreate orderId: $orderId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderPageBinding.inflate(inflater, container, false)

        orderPageModel.apply {
            binding.apply {

                getOrderById(this@OrderPageFragment.orderId)
                order.observe(viewLifecycleOwner) { order ->
                    if (order != null) {
                        order.apply {
                            //order
                            orderId.text = getString(R.string.order_id_res, id)
                            orderQuantity.text = quantity.toString()

                            //costumer
                            costumerName.text = customer.name
                            costumerPhoneNumber.text = customer.number
                            costumerAddress.text = customer.address

                            //product
                            productName.text = annonce.name
                            productPrice.text = getString(R.string.price, annonce.price.toString())

                            setTheStatusEditText(order.orderStatus)

                            orderStatusEditText.doOnTextChanged { text, _, _, _ ->
                                changeOrderStatus(
                                    this@OrderPageFragment.orderId,
                                    OrderStatusRequest(text.toString())
                                )
                                statusModified.observe(viewLifecycleOwner) { statusModified ->
                                    if (!statusModified) {
                                        requireContext().toast(ERROR_MSG, Toast.LENGTH_SHORT)
                                        orderStatusEditText.setText(order.orderStatus)
                                    }
                                }
                            }

                        }

                    } else {
                        requireContext().toast(ERROR_MSG, Toast.LENGTH_SHORT)
                        findNavController().popBackStack()
                    }
                }
                isTurning.observe(viewLifecycleOwner) { isTurning ->
                    orderPageProgressbar.isVisible = isTurning
                }
            }
        }

        return binding.root
    }

    private fun setTheStatusEditText(currentStatus: String) {
        binding.apply {
            orderStatusEditText.setText(currentStatus)
            val values = OrderStatus.values().map { status ->
                status.status
            }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
            (orderStatusTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
        }
    }
}