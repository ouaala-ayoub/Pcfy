package alpha.company.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.R
import alpha.company.pc.databinding.SingleScrollableImageBinding
import alpha.company.pc.ui.fragments.Picture
import alpha.company.pc.utils.ANNONCES_AWS_S3_LINK
import alpha.company.pc.utils.circularProgressBar
import android.net.Uri
import com.squareup.picasso.Picasso

private const val TAG = "ImagesAdapter"

class ImagesAdapter(
    private val imagesList: MutableList<Picture>,
    private val onImageClicked: OnImageClicked,
    private val picasso: Picasso
) : RecyclerView.Adapter<ImagesAdapter.ImagesHolder>() {

    interface OnImageClicked {
        fun onImageZoomed()
        fun onLeftClicked()
        fun onRightClicked()
    }

    private fun clearList() {
        this.imagesList.removeAll { element -> element.name == IMAGE_ADD }
    }

    init {
        clearList()
    }

    inner class ImagesHolder(private val binding: SingleScrollableImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            Log.i(TAG, "bind: $position")

            loadImageAtPosition(position)
            handleRightAndLeft(position)

        }

        private fun loadImageAtPosition(position: Int) {

            val currentImage = this@ImagesAdapter.imagesList[position]

            binding.apply {

                productImages

                val circularProgressDrawable = circularProgressBar(binding.root.context)
                //each image
                if (currentImage.uri == null) {
                    val imageUrl = "$ANNONCES_AWS_S3_LINK${currentImage.name}"
                    picasso
                        .load(imageUrl)
                        .error(R.drawable.ic_baseline_no_photography_24)
                        .placeholder(circularProgressDrawable)
                        .fit()
                        .into(productImages)
                } else {
                    picasso
                        .load(currentImage.uri)
                        .error(R.drawable.ic_baseline_no_photography_24)
                        .placeholder(circularProgressDrawable)
                        .fit()
                        .into(productImages)
                }


                productImages.setOnClickListener {
                    //zoom in the image
                }
            }
        }

        private fun handleRightAndLeft(position: Int) {
            binding.apply {
                if (this@ImagesAdapter.imagesList.size == 1) {
                    left.isVisible = false
                    right.isVisible = false
                    return
                } else {
                    left.apply {
                        setOnClickListener {
                            onImageClicked.onLeftClicked()
                        }
                        if (position == 0) {
                            isVisible = false
                        }
                    }
                    right.apply {
                        setOnClickListener {
                            onImageClicked.onRightClicked()
                        }
                        if (position == this@ImagesAdapter.imagesList.size - 1) {
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

    override fun getItemCount() = this.imagesList.size

    fun modifyImageAtPosition(newImageUri: Uri?, position: Int) {
        imagesList[position].uri = newImageUri
        Log.d(TAG, "modifyImageAtPosition: ${imagesList[position]}")
        notifyItemChanged(position)
    }

    fun deleteImageAtPosition(position: Int) {
        if (imagesList.size > 1) {
            imagesList.removeAt(position)
            notifyItemRemoved(position)
        }

    }

//    fun reloadImageAt(indexToChange: Int) {
//        newList[indexToChange].loadingPolicy = LoadPolicy.Reload
//        notifyItemChanged(indexToChange)
//    }
}