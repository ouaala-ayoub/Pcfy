package com.example.pc.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pc.data.repositories.SearchRepository

class SearchModel(repository: SearchRepository) : ViewModel(){
    //add business logic
}
class SearchModelFactory constructor(private val repository: SearchRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SearchModel::class.java)) {
            SearchModel(this.repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}