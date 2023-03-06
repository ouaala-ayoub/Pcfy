package alpha.company.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.R
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.data.models.network.Order
import alpha.company.pc.databinding.SingleFavouriteBinding
import alpha.company.pc.utils.ANNONCES_AWS_S3_LINK
import alpha.company.pc.utils.circularProgressBar
import alpha.company.pc.utils.defineField
import com.squareup.picasso.Picasso

private const val TAG = "FavouritesAdapter"

class FavouritesAdapter(
    private val onFavouriteClickListener: OnFavouriteClickListener,
    private val onOrderClicked: OrdersShortAdapter.OnOrderClicked? = null,
    private val onCommandsClickListener: OnCommandsClicked? = null,

    ) : RecyclerView.Adapter<FavouritesAdapter.FavouriteHolder>() {

    interface OnFavouriteClickListener {
        fun onFavouriteClicked(annonceId: String)
        fun onDeleteClickListener(annonceId: String)
    }

    interface OnCommandsClicked {
        fun onCommandClicked(
            annonceId: String,
            empty: TextView,
            orderRv: RecyclerView,
            onOrderClicked: OrdersShortAdapter.OnOrderClicked?
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
                val circularProgressDrawable = circularProgressBar(binding.root.context)
                // to discuss
                defineField(favouriteSeller, favourite.seller.name, binding.root.context)

                favouritePrice.text = binding.root.resources.getString(
                    R.string.price,
                    favourite.price.toString()
                )

                //including the image

                if (favourite.pictures.isEmpty()) {
                    favouriteImage.setImageResource(R.drawable.ic_baseline_no_photography_24)
                } else if (favourite.pictures[0].isNotBlank()) {
                    picasso
                        .load("$ANNONCES_AWS_S3_LINK${favourite.pictures[0]}")
                        .fit()
                        .error(R.drawable.ic_baseline_no_photography_24)
                        .placeholder(circularProgressDrawable)
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
                    commandes.apply {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            onCommandsClickListener.onCommandClicked(
                                favourite.id!!, binding.empty, ordersRv, onOrderClicked
                            )
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