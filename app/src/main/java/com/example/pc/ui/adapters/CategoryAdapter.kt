package com.example.pc.ui.adapters

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.data.models.local.Category
import com.example.pc.databinding.SingleCategoryBinding

private const val TAG = "CategoryAdapter"



class CategoryAdapter(
    private val categoriesList: List<Category>,
    private val onClick: OnCategoryClickedListener
    ): RecyclerView.Adapter<CategoryAdapter.CategoryHolder>() {

    interface OnCategoryClickedListener {
        fun onCategoryClicked(title: String)
    }
    private var currentClicked = 0

    inner class CategoryHolder(private val binding: SingleCategoryBinding): RecyclerView.ViewHolder(binding.root) {

        private fun initialise(){
            categoriesList[currentClicked].isClicked = true
        }

        fun bind(position: Int){

            initialise()

            val category = categoriesList[position]
            binding.apply {

                categoryTitle.text = category.title
                setFlags(category, categoryTitle)

                categoryTitle.setOnClickListener {

                    if (category.isClicked){
                        onClick.onCategoryClicked(category.title)
                    }
                    else {
                        categoriesList[currentClicked].reverseClicked()
                        notifyItemChanged(currentClicked)

                        category.reverseClicked()
                        setFlags(category, categoryTitle)
                        currentClicked = position
                        onClick.onCategoryClicked(category.title)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        return CategoryHolder(
            SingleCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = categoriesList.size



    private fun inverseFlag(){

    }

    private fun setFlags(category: Category, textView: TextView){
        if (category.isClicked){
            textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
        else {
            textView.paintFlags = 0
        }
    }

}