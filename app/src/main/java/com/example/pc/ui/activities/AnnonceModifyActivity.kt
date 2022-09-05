package com.example.pc.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.example.pc.R
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.Category
import com.example.pc.data.models.network.Error
import com.example.pc.data.models.network.Status
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.AnnonceModifyRepository
import com.example.pc.databinding.ActivityAnnonceModifyBinding
import com.example.pc.ui.viewmodels.AnnonceModifyModel
import com.example.pc.utils.toast
import com.google.android.material.textfield.MaterialAutoCompleteTextView

private const val TAG = "AnnonceModifyActivity"
private const val ERROR_GET_ANNONCE = "Erreur innatendue"
private const val ERROR_SET_ANNONCE = "Erreur de La modification de l'annonce"
private const val SUCCESS_SET_ANNONCE = "Annonce modifié avec success"

class AnnonceModifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnonceModifyBinding
    private lateinit var viewModel: AnnonceModifyModel
    private lateinit var annonceToModifyId: String

    override fun onCreate(savedInstanceState: Bundle?) {

        binding =   ActivityAnnonceModifyBinding.inflate(layoutInflater)
        viewModel = AnnonceModifyModel(AnnonceModifyRepository(RetrofitService.getInstance()))
        annonceToModifyId = intent.getStringExtra("id")!!

        super.onCreate(savedInstanceState)

        binding.apply {
            viewModel.apply {
                getAnnonce(annonceToModifyId).observe(this@AnnonceModifyActivity){ annonce ->

                    isTurning.observe(this@AnnonceModifyActivity) {
                        annonceModifyProgressBar.isVisible = it
                    }

                   // on annonce retrieved fail
                    if (annonce == null) {
                        doOnFail(ERROR_GET_ANNONCE)
                    }

                    else {

                        Log.i(TAG, "annonce retrieved : $annonce")

                        // on annonce retrieved success

                        //to add image adding and deleting

                        titleEditText.setText(annonce.title)
                        priceEditText.setText(annonce.price.toString())
                        setTheCategoriesEditText(annonce.category)
                        setTheStatueEditTextView(annonce.status)
                        markEditText.setText(annonce.mark)
                        descriptionEditText.setText(annonce.description)

                        validateTheData()

                        submitChanges.setOnClickListener {
                            val newAnnonce = Annonce(
                                title = titleEditText.text.toString(),
                                price = priceEditText.text.toString().toInt(),
                                category = categoryEditText.text.toString(),
                                status = statusEditText.text.toString(),
                                mark = markEditText.text.toString(),
                                description = descriptionEditText.text.toString()
                            )
                            updateAnnonceInfo(annonceToModifyId, newAnnonce)
                                .observe(this@AnnonceModifyActivity){ annonceModified ->
                                    //on annonce modification fail
                                    if (!annonceModified){
                                        doOnFail(ERROR_SET_ANNONCE)
                                    }
                                    else {
                                        doOnSuccess()
                                    }
                            }
                        }
                    }
                }
            }
        }

        setContentView(binding.root)
    }

    private fun setTheStatueEditTextView(default: String){
        //default is the annonce attribute value
        binding.statusTextField.editText?.setText(default)

        //set the adapter
        val items = Status.values()
        val values = mutableListOf<String>()

        for (element in items){
            values.add(element.status)
        }
        val adapter = ArrayAdapter(this, R.layout.list_item, values)
        (binding.statusTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setTheCategoriesEditText(default: String){

        //default is the annonce attribute value
        binding.categoryTextField.editText?.setText(default)

        //set the adapter
        val values = Category.values().map {
                category -> category.title
        }

        val adapter = ArrayAdapter(this, R.layout.list_item, values)
        (binding.categoryTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun validateTheData(){

        //by default the button is disabled
        binding.apply {

            //initialise the value of the live data to check
            submitChanges.isEnabled = true

            viewModel.apply {
                titleLiveData.value = titleEditText.text.toString()
                priceLiveData.value = priceEditText.text.toString()
            }

            titleEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.titleLiveData.value = text.toString()
            }

            priceEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.priceLiveData.value = text.toString()
            }

            viewModel.isValidInput.observe(this@AnnonceModifyActivity){ isActive ->
                binding.submitChanges.isEnabled = isActive
            }
        }
    }

    private fun geToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun doOnFail(message: String) {
        this.toast(message, Toast.LENGTH_SHORT)
        geToMainActivity()
    }

   private fun doOnSuccess(){
       this.toast(SUCCESS_SET_ANNONCE, Toast.LENGTH_SHORT)
       goToAnnoncesActivity()
   }

    private fun goToAnnoncesActivity() {
        finish()
    }

}
