package com.example.pc.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.data.models.network.Order
import com.example.pc.databinding.SingleOrderFullBinding
import com.example.pc.utils.BASE_AWS_S3_LINK
import com.squareup.picasso.Picasso

class OrdersFullAdapter(
    private val ordersList: List<Order>,
    private val onOrderClicked: OrdersShortAdapter.OnOrderClicked
) :
    RecyclerView.Adapter<OrdersFullAdapter.OrderFullHolder>() {

    private val picasso = Picasso.get()

    inner class OrderFullHolder(private val binding: SingleOrderFullBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val order = ordersList[position]

            binding.apply {
                val imageUrl = "$BASE_AWS_S3_LINK${order.annonce.picture}"
                picasso
                    .load(imageUrl)
                    .fit()
                    .centerCrop()
                    .into(announceImage)

                orderId.text = order.orderId
                orderProductName.text = order.annonce.name
                costumerName.text = order.customer.name
                costumerNumber.text = order.customer.number

                orderWhole.setOnClickListener {
                    onOrderClicked.onOrderClicked(order.orderId!!)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderFullHolder {
        return OrderFullHolder(
            SingleOrderFullBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrderFullHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = ordersList.size
}