package com.example.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.R
import com.example.pc.data.models.network.Annonce
import com.example.pc.databinding.SinglePopularAnnonceBinding
import com.example.pc.utils.BASE_AWS_S3_LINK
import com.squareup.picasso.Picasso

private const val TAG = "PopularsAdapter"

class PopularsAdapter(private val annonceClickListener: AnnoncesAdapter.OnAnnonceClickListener) :
    RecyclerView.Adapter<PopularsAdapter.PopularsHolder>() {

    private var annoncesList = mutableListOf<Annonce>()
    private val picasso = Picasso.get()

    inner class PopularsHolder(private val binding: SinglePopularAnnonceBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(position: Int) {
            val annonce = annoncesList[position]
            try {
                binding.apply {
                    
                    picasso
                        .load("$BASE_AWS_S3_LINK${annonce.pictures[0]}")
                        .fit()
                        .into(popularAnnonceImage)

                    popularAnnonceTitle.text = annonce.title

                    popularAnnoncePrice.text =
                        binding.root.resources.getString(R.string.price, annonce.price)


                    popularWhole.setOnClickListener {
                        annonceClickListener.onAnnonceClick(annonce.id!!)
                    }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "bind : ${e.message}")
                annonceClickListener.onAnnonceLoadFail()
            }
        }

    }

    fun setPopularsList(list: List<Annonce>) {
        annoncesList = list as MutableList<Annonce>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularsHolder {
        return PopularsHolder(
            SinglePopularAnnonceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PopularsHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = annoncesList.size
}