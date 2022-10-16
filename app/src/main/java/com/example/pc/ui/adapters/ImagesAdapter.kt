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
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

//enum class Goal(private val goal: String){
//    View("view"),
//    Modify("modify")
//}
private const val TAG = "ImagesAdapter"

class ImagesAdapter(
    private val imagesList: MutableList<String>,
    private val onImageClicked: OnImageClicked,
) : RecyclerView.Adapter<ImagesAdapter.ImagesHolder>() {

    interface OnImageClicked {
        fun onLeftClicked()
        fun onRightClicked()
    }

    private fun clearList() {
        imagesList.removeAll { element -> element == IMAGE_ADD }
    }

    init {
        if (IMAGE_ADD in imagesList) {
            clearList()
            Log.i(TAG, "images List : $imagesList")
        }
    }

    inner class ImagesHolder(private val binding: SingleScrollableImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val picasso: Picasso = Picasso.get()
        fun bind(position: Int) {

            val currentImage = imagesList[position]

            binding.apply {
                //each image

                Log.i(TAG, "bind: $position")
                Log.i(TAG, "bind: $currentImage")
                
                if (currentImage.isBlank()) {
                    val imageSize =
                        binding.root.resources.getDimension(R.dimen.annonce_image_height).toInt()
                    picasso
                        .load(R.drawable.ic_baseline_no_photography_24)
                        .resize(imageSize, imageSize)
                        .centerCrop()
                        .into(productImages)
                }

                picasso
                    .load("$BASE_AWS_S3_LINK${currentImage}")
//                    .networkPolicy(NetworkPolicy.NO_CACHE)
//                    .memoryPolicy(MemoryPolicy.NO_CACHE)
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

                productImages.setOnClickListener {
                    //zoom in the image
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