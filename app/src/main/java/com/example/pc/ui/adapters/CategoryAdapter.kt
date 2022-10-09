package com.example.pc.ui.adapters

import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pc.data.models.local.Category
import com.example.pc.databinding.SingleCategoryBinding
import com.google.android.material.chip.Chip

private const val TAG = "CategoryAdapter"


class CategoryAdapter(
    private val categoriesList: List<Category>,
    private val onClick: OnCategoryClickedListener
) : RecyclerView.Adapter<CategoryAdapter.CategoryHolder>() {

    private var currentClicked = 0

    interface OnCategoryClickedListener {
        fun onCategoryClicked(title: String)
    }

    inner class CategoryHolder(private val binding: SingleCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun initialise() {
            categoriesList[currentClicked].isClicked = true
        }

        fun bind(position: Int) {

            initialise()

            val category = categoriesList[position]
            binding.apply {

                categoryTitle.text = category.title
                setView(category, categoryTitle)

                categoryTitle.setOnClickListener {

                    onClick.onCategoryClicked(category.title)

                    if (category.isClicked) {
                        onClick.onCategoryClicked(category.title)
                    } else {
                        categoriesList[currentClicked].reverseClicked()
                        notifyItemChanged(currentClicked)

                        category.reverseClicked()
                        setView(category, categoryTitle)
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

    private fun setView(category: Category, chip: Chip) {
        if (category.isClicked) {
            chip.setTypeface(null, Typeface.BOLD)

            chip.isChecked = category.isClicked
        } else {
            chip.setTypeface(null, Typeface.NORMAL)

            chip.isChecked = category.isClicked
        }
    }

}