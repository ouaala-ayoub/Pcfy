package com.example.pc.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.databinding.SingleImageModifyBinding
import com.example.pc.utils.BASE_AWS_S3_LINK
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlin.reflect.typeOf

class ImagesModifyAdapter(
    private val imagesList: MutableList<String>,
    private val onImageClicked: OnImageModifyClicked
) : RecyclerView.Adapter<ImagesModifyAdapter.ImagesModifyHolder>() {

    private val picasso = Picasso.get()

    interface OnImageModifyClicked {
        fun onImageClicked(imageIndex: Int, imagesList: List<String>)
        fun onAddClicked()
    }

    inner class ImagesModifyHolder(private val binding: SingleImageModifyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                productImage.apply {
                    setOnClickListener {
//                        if(position == imagesList.lastIndex){
//                            onImageClicked.onAddClicked()
//                        }
//                        else {
                            onImageClicked.onImageClicked(position, imagesList)
//                        }
                    }
                    picasso
                        .load("$BASE_AWS_S3_LINK${imagesList[position]}")
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .fit()
                        .into(this)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesModifyHolder {
        return ImagesModifyHolder(
            SingleImageModifyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ImagesModifyHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = imagesList.size
}