package com.example.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.data.models.local.Detail
import com.example.pc.databinding.SingleDetailFieldBinding

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
            notifyDataSetChanged()
            Log.i(TAG, "addEmptyField details list : $detailsList")
            true
        } else {
            Log.i(TAG, "addEmptyField: max size exceeded")
            false
        }
    }

    private fun deleteElement(position: Int){

        if (detailsList.isNotEmpty()) {
            detailsList.removeAt(position)
            if(detailsList.size == 0){
                isEmpty.postValue(true)
            }
            notifyDataSetChanged()
        }
        else {
            isEmpty.postValue(true)
        }
    }

    inner class AddDetailsHolder(private val binding: SingleDetailFieldBinding) :
        RecyclerView.ViewHolder(binding.root) {

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


                titleEditText.setText(detail.title)
                bodyEditText.setText(detail.body)

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