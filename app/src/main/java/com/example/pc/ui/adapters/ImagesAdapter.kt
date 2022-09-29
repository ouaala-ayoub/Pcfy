package com.example.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.databinding.SingleScrollableImageBinding
import com.example.pc.utils.BASE_AWS_S3_LINK
import com.squareup.picasso.Picasso

private const val TAG = "ImagesAdapter"

class ImagesAdapter(
    private val imagesList: List<String>
) : RecyclerView.Adapter<ImagesAdapter.ImagesHolder>() {

    inner class ImagesHolder(private val binding: SingleScrollableImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val picasso: Picasso = Picasso.get()
        fun bind(position: Int) {

            val currentImage = imagesList[position]

            binding.apply {
                //each image
                picasso
                    .load("$BASE_AWS_S3_LINK${currentImage}")
                    .fit()
                    .into(productImages)

                //left and right button
                left.setOnClickListener {
                    Log.i(TAG, "clicked left of $position")
                }
                right.setOnClickListener {
                    Log.i(TAG, "clicked right of $position")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesHolder {
        val binding = SingleScrollableImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImagesHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagesHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = imagesList.size
}