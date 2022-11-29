package com.example.pc.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.R
import com.example.pc.data.models.network.Order
import com.example.pc.databinding.SingleRequestBinding
import com.example.pc.utils.BASE_AWS_S3_LINK
import com.squareup.picasso.Picasso

class RequestsAdapter(
    private val ordersList: List<Order>,
) :
    RecyclerView.Adapter<RequestsAdapter.RequestsHolder>() {
    private val picasso = Picasso.get()
    inner class RequestsHolder(private val binding: SingleRequestBinding) :
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

                orderId.text = binding.root.resources.getString(R.string.id_res, order.id)
                orderProductName.text = order.annonce.name
                orderProductPrice.text =
                    binding.root.resources.getString(R.string.price, order.annonce.price.toString())

                quantity.text = order.quantity.toString()
                orderStatus.setImageResource(getImageResource(order.orderStatus))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestsHolder {
        return RequestsHolder(
            SingleRequestBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RequestsHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = ordersList.size
}