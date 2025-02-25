package alpha.company.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.R
import alpha.company.pc.data.models.network.Annonce
import alpha.company.pc.databinding.SingleAnnonceBinding
import alpha.company.pc.utils.ANNONCES_AWS_S3_LINK
import alpha.company.pc.utils.circularProgressBar
import com.squareup.picasso.Picasso

private const val TAG = "AnnoncesAdapter"

class AnnoncesAdapter(
    private val annonceClickListener: OnAnnonceClickListener,
    private var annoncesList: MutableList<Annonce>
) : RecyclerView.Adapter<AnnoncesAdapter.AnnonceHolder>() {

    interface OnAnnonceClickListener {
        fun onAnnonceClick(annonceId: String)
        fun onAnnonceLoadFail()
    }

//    private var annoncesList: MutableList<Annonce> = mutableListOf()

    fun isListEmpty() = annoncesList.isEmpty()
    fun setAnnoncesListFromAdapter(annonceToSet: List<Annonce>) {
        annoncesList = annonceToSet.toMutableList()



        notifyDataSetChanged()
    }

    fun addElements(list: List<Annonce>) {
        annoncesList.addAll(list)
        notifyItemRangeInserted(itemCount, list.size)
    }

    fun freeList() {
        setAnnoncesListFromAdapter(mutableListOf())
    }

    inner class AnnonceHolder(private val binding: SingleAnnonceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val annonce = annoncesList[position]
            Log.i(TAG, "bind [$position] ${annonce.title}")
            binding.apply {

                try {
                    val circularProgressDrawable = circularProgressBar(binding.root.context)
                    annonceTitle.text = annonce.title

                    annoncePrice.text = binding.root.resources.getString(
                        R.string.price,
                        annonce.price.toString()
                    )

                    if (annonce.pictures.isEmpty()) {
                        annonceImage.setImageResource(R.drawable.ic_baseline_no_photography_24)
                    } else if (annonce.pictures[0].isNotBlank()) {
                        Picasso
                            .get()
                            .load("$ANNONCES_AWS_S3_LINK${annonce.pictures[0]}")
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