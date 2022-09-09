package com.example.pc.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
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

    inner class CategoryHolder(private val binding: SingleCategoryBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int){
            val category = categoriesList[position]
            binding.apply {
                categoryImage.setImageResource(category.imageRes)
                categoryTitle.text = category.title
                wholeCategory.setOnClickListener {
                    onClick.onCategoryClicked(category.title)
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

}