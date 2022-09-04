package com.example.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.R
import com.example.pc.data.models.network.Annonce
import com.example.pc.databinding.SingleFavouriteBinding
import com.example.pc.ui.viewmodels.FavouritesModel
import com.squareup.picasso.Picasso

private const val TAG = "FavouritesAdapter"

class FavouritesAdapter(
    private val onFavouriteClickListener: OnFavouriteClickListener,
): RecyclerView.Adapter<FavouritesAdapter.FavouriteHolder>() {

    interface OnFavouriteClickListener{
        fun onFavouriteClicked(annonceId: String)
        fun onDeleteClickListener(annonceId: String)
    }
    private var favouritesList = mutableListOf<Annonce>()

    fun setList(list: List<Annonce>){
        favouritesList = list.toMutableList()
        notifyDataSetChanged()
    }

    inner class FavouriteHolder(private val binding: SingleFavouriteBinding): RecyclerView.ViewHolder(binding.root){


        fun bind(position: Int) {

            val favourite = favouritesList[position]

            val picasso = Picasso.get()
            Log.i(TAG, "binding : $favourite")

            //set the ui elements
            binding.apply {
                favouriteTitle.text = favourite.title


//                seller name
//                val sellerName = viewModel.getTheSellerName(favourite.sellerId)
//                favouriteSeller.text = sellerName

                favouritePrice.text = favourite.price.toString()

                //including the image
                if (favourite.pictures.isEmpty()){
                    favouriteImage.setImageResource(R.drawable.ic_baseline_no_photography_24)
                }
                else {
                    picasso
                        .load(favourite.pictures[0])
                        .fit()
                        .centerCrop()
                        .into(favouriteImage)
                }

                favouriteWhole.setOnClickListener {
                    onFavouriteClickListener.onFavouriteClicked(favourite.id!!)
                }

                delete.setOnClickListener {
                    onFavouriteClickListener.onDeleteClickListener(favourite.id!!)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteHolder {
        return FavouriteHolder(
            SingleFavouriteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FavouriteHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = favouritesList.size
}