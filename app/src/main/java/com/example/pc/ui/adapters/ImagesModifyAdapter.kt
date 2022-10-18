package com.example.pc.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pc.R
import com.example.pc.databinding.SingleImageModifyBinding
import com.example.pc.utils.BASE_AWS_S3_LINK
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlin.reflect.typeOf

private const val TAG = "ImagesModifyAdapter"
const val IMAGE_ADD = "add images button"
private const val MAX_SIZE = 10

class ImagesModifyAdapter(
    private val imagesList: MutableList<String>,
    private val onImageClicked: OnImageModifyClicked,
    private val picasso: Picasso
) : RecyclerView.Adapter<ImagesModifyAdapter.ImagesModifyHolder>() {

    interface OnImageModifyClicked {
        fun onImageClicked(imageIndex: Int, imagesList: List<String>)
        fun onAddClicked()
    }

    init {
        if (imagesList.size < MAX_SIZE) {
            imagesList.add(IMAGE_ADD)
        }
    }

    inner class ImagesModifyHolder(private val binding: SingleImageModifyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            Log.i(TAG, "images list : $imagesList")

            val currentImage = imagesList[position]

            binding.apply {
                productImage.apply {
                    if (currentImage != IMAGE_ADD) {
                        val imageUrl = "$BASE_AWS_S3_LINK${imagesList[position]}"
                        picasso.invalidate(imageUrl)
                        picasso
                            .load(imageUrl)
                            .stableKey(imageUrl)
                            .fit()
                            .into(this)
                        setOnClickListener {
                            onImageClicked.onImageClicked(position, imagesList)
                        }
                    } else {
                        setImageResource(R.drawable.ic_baseline_add_24)
                        setOnClickListener {
                            onImageClicked.onAddClicked()
                        }
                    }
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