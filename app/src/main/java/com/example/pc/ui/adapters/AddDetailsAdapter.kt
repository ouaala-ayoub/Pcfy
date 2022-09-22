package com.example.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.data.models.local.Detail
import com.example.pc.databinding.SingleDetailFieldBinding

private const val MAX_SIZE = 5
private const val TAG = "AddDetailsAdapter"

class AddDetailsAdapter(
    val detailsList: MutableList<Detail>
) : RecyclerView.Adapter<AddDetailsAdapter.AddDetailsHolder>() {

    fun addEmptyField(): Boolean {
        return if (detailsList.size < MAX_SIZE) {
            val newDetail = Detail("", "")
            detailsList.add(newDetail)
//            notifyItemInserted(detailsList.size)
            notifyDataSetChanged()
            Log.i(TAG, "details list : $detailsList")
            true
        } else {
            Log.i(TAG, "addEmptyField: max size exceeded")
            false
        }
    }

    fun deleteElement() {

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
                    addEmptyField.isVisible = false
                    addEmptyField.setOnClickListener(null)
                } else {
                    addEmptyField.apply {
                        isVisible = true
                        setOnClickListener {
                            addEmptyField()
                        }
                    }
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