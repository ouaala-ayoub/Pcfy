package alpha.company.pc.ui.adapters

import alpha.company.pc.R
import alpha.company.pc.data.models.network.Demand
import alpha.company.pc.databinding.SingleDemandBinding
import alpha.company.pc.utils.DEMANDS_AWS_S3_LINK
import alpha.company.pc.utils.circularProgressBar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

private const val TAG = "DemandsAdapter"

class DemandsAdapter(
    private val onDemandClicked: OnDemandClicked,
    private val showDelete: Boolean = false
) :
    RecyclerView.Adapter<DemandsAdapter.DemandsHolder>() {
    var demandsList: MutableList<Demand> = mutableListOf()

    interface OnDemandClicked {
        fun onDemandClicked(demandId: String)
        fun onDeleteClicked(demandId: String) = null
    }

    inner class DemandsHolder(private val binding: SingleDemandBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val demand = demandsList[position]
            binding.apply {

                if (showDelete) {
                    deleteDemand.apply {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            onDemandClicked.onDeleteClicked(demand.id!!)
                        }
                    }
                }

                demandWhole.setOnClickListener {
                    onDemandClicked.onDemandClicked(demand.id!!)
                }

                demandTitle.text = demand.title

                if (!demand.price.isNullOrBlank()) {
                    demandPrice.text =
                        binding.root.context.getString(R.string.price, demand.price)
                } else {
                    demandPrice.text = binding.root.context.getString(R.string.no_price)
                }

                Picasso
                    .get()
                    .load("$DEMANDS_AWS_S3_LINK${demand.picture}")
                    .error(R.drawable.ic_baseline_no_photography_24)
                    .placeholder(circularProgressBar(binding.root.context))
                    .fit()
                    .into(demandImage)
            }
        }
    }

    fun isListEmpty() = demandsList.isEmpty()
    fun setList(list: List<Demand>) {
        demandsList = list.toMutableList()
        Log.i(TAG, "setList: ${demandsList.size}")
        notifyDataSetChanged()
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

//    fun freeList() {
//        demandsList = mutableListOf()
//        notifyDataSetChanged()
//    }
}