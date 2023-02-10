package alpha.company.pc.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.R
import alpha.company.pc.data.models.network.Order
import alpha.company.pc.databinding.SingleOrderFullBinding
import alpha.company.pc.utils.BASE_AWS_S3_LINK
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
                    .error(R.drawable.ic_baseline_no_photography_24)
                    .centerCrop()
                    .into(announceImage)

                orderId.text = binding.root.resources.getString(R.string.id_res, order.id)
                orderProductName.text = order.annonce.name
                costumerName.text = order.customer.name
                costumerNumber.text = order.customer.number

                orderWhole.setOnClickListener {
                    onOrderClicked.onOrderClicked(order.id!!)
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