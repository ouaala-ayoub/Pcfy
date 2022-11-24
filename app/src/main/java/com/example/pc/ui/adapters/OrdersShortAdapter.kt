package com.example.pc.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.R
import com.example.pc.data.models.local.OrderStatus
import com.example.pc.data.models.network.Order
import com.example.pc.data.models.network.Status
import com.example.pc.databinding.SingleOrderShortBinding

class OrdersShortAdapter(
    private val onOrderClicked: OnOrderClicked
) :
    RecyclerView.Adapter<OrdersShortAdapter.OrdersHolder>() {

    interface OnOrderClicked{
        fun onOrderClicked(orderId: String)
    }

    private var ordersList: List<Order> = listOf()
    fun setOrdersList(listToSet: List<Order>){
        ordersList = listToSet
        notifyDataSetChanged()
    }

    inner class OrdersHolder(val binding: SingleOrderShortBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val order = ordersList[position]

            binding.apply {

                orderId.text = binding.root.resources.getString(R.string.id_res, order.orderId)
                quantity.text = order.quantity.toString()
                orderStatus.setImageResource(getImageResource(order.orderStatus))

                orderShortWhole.setOnClickListener {
                    onOrderClicked.onOrderClicked(order.orderId!!)
                }
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersHolder {
        return OrdersHolder(
            SingleOrderShortBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrdersHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = ordersList.size
}
fun getImageResource(orderStatus: String): Int {
    return when(orderStatus) {
        OrderStatus.DELIVERED.status -> R.drawable.ic_baseline_done_24
        OrderStatus.CANCELED.status -> R.drawable.ic_baseline_cancel_24
        else -> R.drawable.ic_baseline_access_time_24
    }
}

