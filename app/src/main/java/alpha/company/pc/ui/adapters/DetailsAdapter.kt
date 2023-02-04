package alpha.company.pc.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.R
import alpha.company.pc.data.models.local.Detail
import alpha.company.pc.databinding.SingleDetailBinding
import android.text.TextUtils

class DetailsAdapter(
    private val detailsList: List<Detail>
) : RecyclerView.Adapter<DetailsAdapter.DetailsHolder>() {

    inner class DetailsHolder(private val binding: SingleDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {

            val detail = detailsList[position]

            binding.apply {
                detailTitle.text =
                    root.resources.getString(R.string.details_title, detail.title)
                detailBody.text = detail.body
            }
        }
        private fun getTitle(fullTitle: String): String{
            return ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsHolder {
        return DetailsHolder(
            SingleDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DetailsHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = detailsList.size

}