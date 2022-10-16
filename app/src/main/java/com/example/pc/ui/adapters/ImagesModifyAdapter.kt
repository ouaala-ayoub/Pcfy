package com.example.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.R
import com.example.pc.databinding.SingleImageModifyBinding
import com.example.pc.utils.BASE_AWS_S3_LINK
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlin.reflect.typeOf

private const val TAG = "ImagesModifyAdapter"
private const val IMAGE_ADD = "add images button"
private const val MAX_SIZE = 10

class ImagesModifyAdapter(
    private val imagesList: MutableList<String>,
    private val onImageClicked: OnImageModifyClicked
) : RecyclerView.Adapter<ImagesModifyAdapter.ImagesModifyHolder>() {

    private val picasso = Picasso.get()

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
//                    if (imagesList.size < MAX_SIZE) {
//                        if (position == imagesList.lastIndex) {
//                            setImageResource(R.drawable.ic_baseline_add_24)
//                            setOnClickListener {
//                                onImageClicked.onAddClicked()
//                            }
//                        } else {
//                            picasso
//                                .load("$BASE_AWS_S3_LINK${imagesList[position]}")
////                              .networkPolicy(NetworkPolicy.NO_CACHE)
////                              .memoryPolicy(MemoryPolicy.NO_CACHE)
//                                .fit()
//                                .into(this)
//                            setOnClickListener {
//                                onImageClicked.onImageClicked(position, imagesList)
//                            }
//                        }
//                    } else {
//                        picasso
//                            .load("$BASE_AWS_S3_LINK${imagesList[position]}")
////                              .networkPolicy(NetworkPolicy.NO_CACHE)
////                              .memoryPolicy(MemoryPolicy.NO_CACHE)
//                            .fit()
//                            .into(this)
//                        setOnClickListener {
//                            onImageClicked.onImageClicked(position, imagesList)
//                        }
//                    }

                    if (currentImage != IMAGE_ADD) {
                        picasso
                            .load("$BASE_AWS_S3_LINK${imagesList[position]}")
//                              .networkPolicy(NetworkPolicy.NO_CACHE)
//                              .memoryPolicy(MemoryPolicy.NO_CACHE)
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