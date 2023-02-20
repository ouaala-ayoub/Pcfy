package alpha.company.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.R
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.databinding.SingleAnnonceBinding
import alpha.company.pc.utils.BASE_AWS_S3_LINK
import alpha.company.pc.utils.circularProgressBar
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.squareup.picasso.Picasso

private const val TAG = "AnnoncesAdapter"

class AnnoncesAdapter(
    private val annonceClickListener: OnAnnonceClickListener
) : RecyclerView.Adapter<AnnoncesAdapter.AnnonceHolder>() {

    interface OnAnnonceClickListener {
        fun onAnnonceClick(annonceId: String)
        fun onAnnonceLoadFail()
    }

    private var annoncesList = listOf<Annonce>()

    fun setAnnoncesList(annonceToSet: List<Annonce>) {
        annoncesList = annonceToSet
        notifyDataSetChanged()
    }

    inner class AnnonceHolder(private val binding: SingleAnnonceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val annonce = annoncesList[position]
            Log.i(TAG, "bind: annonce $annonce")
            binding.apply {

                try {
                    val circularProgressDrawable = circularProgressBar(binding.root.context)
                    annonceTitle.text = annonce.title

                    annoncePrice.text = binding.root.resources.getString(
                        R.string.price,
                        annonce.price.toString()
                    )

                    if (annonce.pictures.isEmpty()){
                        annonceImage.setImageResource(R.drawable.ic_baseline_no_photography_24)
                    }
                    else if (annonce.pictures[0].isNotBlank()) {
                        Picasso
                            .get()
                            .load("$BASE_AWS_S3_LINK${annonce.pictures[0]}")
                            .error(R.drawable.ic_baseline_no_photography_24)
                            .placeholder(circularProgressDrawable)
                            .fit()
                            .into(annonceImage)
                    }

//                    annonceSeller.text = annonce.seller.name

                    binding.annonce.setOnClickListener {
                        annonceClickListener.onAnnonceClick(annonce.id!!)
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "bind: $annonce error ${e.message}")
                    annonceClickListener.onAnnonceLoadFail()
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnonceHolder {
        return AnnonceHolder(
            SingleAnnonceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AnnonceHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = annoncesList.size
}