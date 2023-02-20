package alpha.company.pc.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.R
import alpha.company.pc.data.models.network.Order
import alpha.company.pc.databinding.SingleRequestBinding
import alpha.company.pc.utils.BASE_AWS_S3_LINK
import alpha.company.pc.utils.circularProgressBar
import alpha.company.pc.utils.getImageResource
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
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
                val circularProgressDrawable = circularProgressBar(binding.root.context)

                val imageUrl = "$BASE_AWS_S3_LINK${order.annonce.picture}"
                picasso
                    .load(imageUrl)
                    .fit()
                    .placeholder(circularProgressDrawable)
                    .error(R.drawable.ic_baseline_no_photography_24)
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