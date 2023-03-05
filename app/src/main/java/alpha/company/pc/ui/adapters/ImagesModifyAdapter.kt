package alpha.company.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.R
import alpha.company.pc.databinding.SingleImageModifyBinding
import alpha.company.pc.ui.fragments.Picture
import alpha.company.pc.utils.ANNONCES_AWS_S3_LINK
import alpha.company.pc.utils.circularProgressBar
import androidx.core.view.isVisible
import com.squareup.picasso.Picasso
import java.util.*

private const val TAG = "ImagesModifyAdapter"
const val IMAGE_ADD = "add images button"
private const val MAX_SIZE = 10

class ImagesModifyAdapter(
    private var imagesList: MutableList<Picture>,
    private val onImageClicked: OnImageModifyClicked,
    private val picasso: Picasso
) : RecyclerView.Adapter<ImagesModifyAdapter.ImagesModifyHolder>() {


    interface OnImageModifyClicked {
        fun onImageClicked(imageIndex: Int, imagesList: List<Picture>)
        fun onAddClicked()
//        fun onRightClicked()
//        fun onLeftClicked()
    }

    init {
        Log.i(TAG, "images list : $imagesList")
        if (imagesList.size < MAX_SIZE) {
            imagesList.add(Picture(IMAGE_ADD))
        }
    }

    inner class ImagesModifyHolder(private val binding: SingleImageModifyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun handleRightAndLeft(position: Int) {
            binding.apply {

                if (position == imagesList.lastIndex){
                    moveLeft.isVisible = false
                    moveRight.isVisible = false
                } else {
                    moveLeft.apply {
                        isVisible = position != 0
                        setOnClickListener {
                            swap(position, position - 1)
                        }
                    }
                    moveRight.apply {
                        isVisible = position != imagesList.lastIndex - 1
                        setOnClickListener {
                            swap(position, position + 1)
                        }
                    }
                }

            }

        }

        private fun swap(position1: Int, position2: Int){
            Collections.swap(imagesList, position1, position2 )
            notifyItemChanged(position1)
            notifyItemChanged(position2 )
        }

        fun bind(position: Int) {

            val currentImage = imagesList[position]

            binding.apply {
                handleRightAndLeft(position)
                productImage.apply {
                    if (currentImage.name == IMAGE_ADD) {
                        setImageResource(R.drawable.ic_baseline_add_24)
                        setOnClickListener {
                            onImageClicked.onAddClicked()
                        }

                    } else {
                        val circularProgressDrawable = circularProgressBar(binding.root.context)
                        if (currentImage.uri == null) {
                            val imageUrl = "$ANNONCES_AWS_S3_LINK${imagesList[position].name}"
                            picasso
                                .load(imageUrl)
                                .error(R.drawable.ic_baseline_no_photography_24)
                                .placeholder(circularProgressDrawable)
                                .fit()
                                .into(this)
                        } else {
                            picasso
                                .load(currentImage.uri)
                                .error(R.drawable.ic_baseline_no_photography_24)
                                .placeholder(circularProgressDrawable)
                                .fit()
                                .into(this)
                        }

                        setOnClickListener {
                            onImageClicked.onImageClicked(position, imagesList)
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

    fun addPictures(pictures: List<Picture>) {
        imagesList.addAll(imagesList.lastIndex, pictures)
        Log.d(TAG, "addPictures: $imagesList")
        notifyDataSetChanged()
    }

    fun setImageList(pictures: MutableList<Picture>) {

        if (pictures.size < MAX_SIZE) {
            pictures.add(Picture(IMAGE_ADD))
        }
        imagesList = pictures
        Log.d(TAG, "setImageList: $imagesList")
        notifyDataSetChanged()
    }

}