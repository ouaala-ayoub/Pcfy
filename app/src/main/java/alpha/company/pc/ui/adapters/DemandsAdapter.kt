package alpha.company.pc.ui.adapters

import alpha.company.pc.R
import alpha.company.pc.data.models.network.Demand
import alpha.company.pc.databinding.SingleDemandBinding
import alpha.company.pc.utils.BASE_AWS_S3_LINK
import alpha.company.pc.utils.circularProgressBar
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

private const val TAG = "DemandsAdapter"

class DemandsAdapter(private val onDemandClicked: OnDemandClicked) :
    RecyclerView.Adapter<DemandsAdapter.DemandsHolder>() {
    var demandsList: MutableList<Demand> = mutableListOf()

    interface OnDemandClicked {
        fun onDemandClicked(demandId: String)
    }

    inner class DemandsHolder(private val binding: SingleDemandBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val demand = demandsList[position]
            binding.apply {

                demandWhole.setOnClickListener {
                    onDemandClicked.onDemandClicked(demand.id!!)
                }

                demandTitle.text = demand.title
                demandPrice.text = binding.root.context.getString(R.string.price, demand.price)

                if (demand.pictures.isEmpty()) {
                    demandImage.setImageResource(R.drawable.ic_baseline_no_photography_24)
                } else if (demand.pictures[0].isNotBlank()) {
                    Picasso
                        .get()
                        .load("$BASE_AWS_S3_LINK${demand.pictures[0]}")
                        .error(R.drawable.ic_baseline_no_photography_24)
                        .placeholder(circularProgressBar(binding.root.context))
                        .fit()
                        .into(demandImage)

                }
            }
        }
    }

    fun isListEmpty() = demandsList.isEmpty()
    fun setList(list: List<Demand>) {
        demandsList = list.toMutableList()
        Log.i(TAG, "setList: ")
        notifyDataSetChanged()
    }

    fun addDemands(list: List<Demand>) {
//        val test = demandsList.toMutableList()
        demandsList.addAll(list)

        notifyItemRangeInserted(demandsList.lastIndex, list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemandsHolder {
        return DemandsHolder(
            SingleDemandBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = demandsList.size

    override fun onBindViewHolder(holder: DemandsHolder, position: Int) {
        holder.bind(position)
    }

    fun freeList() {
        demandsList = mutableListOf()
    }
}