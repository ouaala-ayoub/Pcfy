package alpha.company.pc.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.R
import alpha.company.pc.data.models.local.OrderStatus
import alpha.company.pc.data.models.network.Order
import alpha.company.pc.databinding.SingleOrderShortBinding
import alpha.company.pc.utils.getImageResource

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

                orderId.text = binding.root.resources.getString(R.string.id_res, order.id)
                quantity.text = order.quantity.toString()
                orderStatus.setImageResource(getImageResource(order.orderStatus))

                orderShortWhole.setOnClickListener {
                    onOrderClicked.onOrderClicked(order.id!!)
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


