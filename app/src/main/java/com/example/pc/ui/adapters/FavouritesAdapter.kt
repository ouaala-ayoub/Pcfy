package com.example.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.R
import com.example.pc.data.models.local.LoggedInUser
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.Order
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.UserInfoRepository
import com.example.pc.databinding.SingleFavouriteBinding
import com.example.pc.ui.viewmodels.SingleAnnounceCommandModel
import com.example.pc.utils.BASE_AWS_S3_LINK
import com.squareup.picasso.Picasso

private const val TAG = "FavouritesAdapter"

class FavouritesAdapter(
    private val onFavouriteClickListener: OnFavouriteClickListener,
    private val onCommandsClickListener: OnCommandsClicked? = null,
    private val onOrderClicked: OrdersShortAdapter.OnOrderClicked? = null,

    ) : RecyclerView.Adapter<FavouritesAdapter.FavouriteHolder>() {

    interface OnFavouriteClickListener {
        fun onFavouriteClicked(annonceId: String)
        fun onDeleteClickListener(annonceId: String)
    }

    interface OnCommandsClicked {
        fun onCommandClicked(
            annonceId: String,
            adapter: OrdersShortAdapter,
            singleCommandModel: SingleAnnounceCommandModel
        )
    }

    private var favouritesList = mutableListOf<Annonce>()

    fun setList(list: List<Annonce>) {
        favouritesList = list.toMutableList()
        notifyDataSetChanged()
    }

    inner class FavouriteHolder(private val binding: SingleFavouriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            val favourite = favouritesList[position]

            val picasso = Picasso.get()
            Log.i(TAG, "binding : $favourite")

            //set the ui elements
            binding.apply {
                favouriteTitle.text = favourite.title

                // to discuss
//                val sellerName = favourite.seller!!.userName
//
//                favouriteSeller.text = sellerName


                favouritePrice.text = binding.root.resources.getString(
                    R.string.price,
                    favourite.price.toString()
                )

                //including the image
                if (favourite.pictures.isEmpty()) {
                    favouriteImage.setImageResource(R.drawable.ic_baseline_no_photography_24)
                } else {
                    picasso
                        .load("$BASE_AWS_S3_LINK${favourite.pictures[0]}")
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


                if (onCommandsClickListener != null && onOrderClicked != null) {
                    val adapter = OrdersShortAdapter(
                        onOrderClicked
                    )
                    linearLayout9.visibility = View.VISIBLE
                    commandes.apply {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            ordersRv.apply {
                                val isVisible = visibility
                                if (isVisible == View.VISIBLE) {
                                    visibility = View.GONE
                                } else {
                                    visibility = View.VISIBLE

                                    this.adapter = adapter
                                    layoutManager = LinearLayoutManager(context)
                                    val singleCommandModel = SingleAnnounceCommandModel(
                                        UserInfoRepository(RetrofitService.getInstance())
                                    )
                                    onCommandsClickListener.onCommandClicked(
                                        favourite.id!!,
                                        adapter,
                                        singleCommandModel
                                    )
                                }
                            }
                        }
                    }
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

class FullItem(private val annonce: Annonce, private val order: List<Order>)