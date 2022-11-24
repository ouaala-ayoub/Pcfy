package com.example.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.R
import com.example.pc.data.models.local.ImageLoader
import com.example.pc.data.models.local.LoadPolicy
import com.example.pc.databinding.SingleScrollableImageBinding
import com.example.pc.utils.BASE_AWS_S3_LINK
import com.example.pc.utils.USERS_AWS_S3_LINK
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

private const val TAG = "ImagesAdapter"

class ImagesAdapter(
    imagesList: List<ImageLoader>,
    private val onImageClicked: OnImageClicked,
    private val picasso: Picasso
) : RecyclerView.Adapter<ImagesAdapter.ImagesHolder>() {

    interface OnImageClicked {
        fun onLeftClicked()
        fun onRightClicked()
    }

    private val newList = imagesList.toMutableList()

    private fun clearList() {
        newList.removeAll { element -> element.imageUrl == IMAGE_ADD }
    }

    init {
        clearList()
    }

    inner class ImagesHolder(private val binding: SingleScrollableImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            Log.i(TAG, "bind: $position")
            val currentImage = newList[position]
            if (currentImage.imageUrl.isBlank()) {
                val imageSize =
                    binding.root.resources.getDimension(R.dimen.annonce_image_height).toInt()
                picasso
                    .load(R.drawable.ic_baseline_no_photography_24)
                    .resize(imageSize, imageSize)
                    .centerCrop()
                    .into(binding.productImages)
            } else {
                if (currentImage.loadingPolicy == LoadPolicy.Cache) {
                    Log.i(TAG, "loading : $position using cache")
                    loadFromCache(position)
                } else if (currentImage.loadingPolicy == LoadPolicy.Reload) {
                    Log.i(TAG, "loading : $position using no cache")
                    loadWithNoCache(position)
                }
                handleRightAndLeft(position)
            }
        }

        private fun loadFromCache(position: Int) {

            val currentImage = newList[position]
            Log.i(TAG, "bind: $currentImage")

            binding.apply {
                //each image
                val imageUrl = "$BASE_AWS_S3_LINK${currentImage.imageUrl}"
                picasso
                    .load(imageUrl)
                    .fit()
                    .centerCrop()
                    .into(productImages)

                productImages.setOnClickListener {
                    //zoom in the image
                }
            }
        }

        private fun loadWithNoCache(position: Int) {
            val currentImage = newList[position]
            Log.i(TAG, "bind: $currentImage")
            binding.apply {
                //each image
                val imageUrl = "$BASE_AWS_S3_LINK${currentImage.imageUrl}"
                picasso
                    .load(imageUrl)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .fit()
                    .centerCrop()
                    .into(productImages)

                productImages.setOnClickListener {
                    //zoom in the image
                }
                currentImage.loadingPolicy = LoadPolicy.Cache
            }
        }

        private fun handleRightAndLeft(position: Int) {
            binding.apply {
                if (newList.size == 1) {
                    left.isVisible = false
                    right.isVisible = false
                    return
                } else {
                    left.apply {
                        setOnClickListener {
                            onImageClicked.onLeftClicked()
                        }
                        if (position == 0) {
                            Log.i(TAG, "left false : $position")
                            isVisible = false
                        }
                    }
                    right.apply {
                        setOnClickListener {
                            onImageClicked.onRightClicked()
                        }
                        if (position == newList.size - 1) {
                            Log.i(TAG, "left false : $position")
                            isVisible = false
                        }
                    }
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

    override fun getItemCount() = newList.size
    fun reloadImageAt(indexToChange: Int) {
        newList[indexToChange].loadingPolicy = LoadPolicy.Reload
        notifyItemChanged(indexToChange)
    }
}