package com.example.pc.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.data.models.local.Detail
import com.example.pc.databinding.SingleDetailBinding

class DetailsAdapter(
    private val detailsList: List<Detail>
) : RecyclerView.Adapter<DetailsAdapter.DetailsHolder>() {

    inner class DetailsHolder(private val binding: SingleDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {

            val detail = detailsList[position]

            binding.apply {
                detailTitle.text = detail.title
                detailBody.text = detail.body
            }
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