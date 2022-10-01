package com.example.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.R
import com.example.pc.databinding.SingleScrollableImageBinding
import com.example.pc.utils.BASE_AWS_S3_LINK
import com.example.pc.utils.USERS_AWS_S3_LINK
import com.squareup.picasso.Picasso


private const val TAG = "ImagesAdapter"

class ImagesAdapter(
    private val imagesList: List<String>,
    private val onImageClicked: OnImageClicked
) : RecyclerView.Adapter<ImagesAdapter.ImagesHolder>() {

    interface OnImageClicked {
        fun onLeftClicked()
        fun onRightClicked()
    }

    inner class ImagesHolder(private val binding: SingleScrollableImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val picasso: Picasso = Picasso.get()
        fun bind(position: Int) {

            val currentImage = imagesList[position]

            binding.apply {
                //each image

                if (currentImage.isBlank()) {
                    productImages.setImageResource(
                        R.drawable.ic_baseline_no_photography_24
                    )
                }

                picasso
                    .load("$BASE_AWS_S3_LINK${currentImage}")
                    .fit()
                    .into(productImages)

                //left and right button
                left.apply {
                    setOnClickListener {
                        onImageClicked.onLeftClicked()
                    }
                    if (position == 0) {
                        isVisible = false
                        isActivated = false
                    }
                }

                right.apply {
                    setOnClickListener {
                        onImageClicked.onRightClicked()
                    }
                    if (position == imagesList.lastIndex) {
                        isVisible = false
                        isActivated = false
                    }
                }

                if (imagesList.size == 1) {
                    left.isVisible = false
                    right.isVisible = false
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