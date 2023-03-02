package alpha.company.pc.ui.adapters

import alpha.company.pc.data.models.local.Detail
import alpha.company.pc.databinding.SingleDetailFieldBinding
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView


private const val MAX_SIZE = 5
private const val TAG = "AddDetailsAdapter"

class AddDetailsAdapter(
    var detailsList: MutableList<Detail>
) : RecyclerView.Adapter<AddDetailsAdapter.AddDetailsHolder>() {

    val isEmpty = MutableLiveData(detailsList.isEmpty())

    fun filterDetailsList() {
        detailsList.removeAll { detail ->
            detail.body.isBlank() && detail.title.isBlank()
        }
        if (detailsList.isEmpty()) isEmpty.postValue(true)
    }

    fun addEmptyField(): Boolean {
        return if (detailsList.size < MAX_SIZE) {
            detailsList.add(Detail("", ""))
//            notifyDataSetChanged()
            notifyItemInserted(detailsList.lastIndex)
            Log.i(TAG, "addEmptyField details list : $detailsList")
            true
        } else {
            Log.i(TAG, "addEmptyField: max size exceeded")
            false
        }
    }

    private fun deleteElement(position: Int) {

        if (detailsList.isNotEmpty()) {
            detailsList.removeAt(position)
            if (detailsList.size == 0) {
                isEmpty.postValue(true)
            }
//            notifyDataSetChanged()
            notifyItemRemoved(position)
        } else {
            isEmpty.postValue(true)
        }
    }

    inner class AddDetailsHolder(private val binding: SingleDetailFieldBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val regex = Regex(":")
        private val filter = InputFilter { source, _, _, _, _, _ ->
            return@InputFilter when {
                source?.matches(regex) == true -> ""
                source.length > 1 -> source.trim {
                    it.toString().matches(regex)
                } // Put your desired logic here, these sample logic was doing a trim/remove
                else -> null
            }
        }

        fun bind(position: Int) {

            val detail = detailsList[position]
            val size = detailsList.size

            binding.apply {

                titleEditText.doOnTextChanged { text, _, _, _ ->
                    detailsList[position].title = text.toString()
                }
                bodyEditText.doOnTextChanged { text, _, _, _ ->
                    detailsList[position].body = text.toString()
                }

                titleEditText.apply {
                    filters = arrayOf(filter)
                    setText(detail.title)
                }
                bodyEditText.apply {
                    filters = arrayOf(filter)
                    setText(detail.body)
                }

                if (position <= size - 2) {
                    addEmptyField.setOnClickListener(null)
                } else {
                    addEmptyField.apply {
                        setOnClickListener {
                            addEmptyField()
                        }
                    }
                }
                deleteField.setOnClickListener {
                    deleteElement(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddDetailsHolder {
        return AddDetailsHolder(
            SingleDetailFieldBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AddDetailsHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = detailsList.size

}