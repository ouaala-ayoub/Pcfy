package alpha.company.pc.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import alpha.company.pc.R
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.AnnonceRepository
import alpha.company.pc.ui.viewmodels.AnnonceModel
import alpha.company.pc.ui.viewmodels.AuthModel
import com.squareup.picasso.Picasso

class AnnonceActivity : AppCompatActivity() {

    val picasso: Picasso = Picasso.get()
    private val retrofitService = RetrofitService.getInstance(this)
    val viewModel = AnnonceModel(AnnonceRepository(retrofitService))
    val authModel = AuthModel(retrofitService, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        authModel.apply {
            auth()
        }
        supportActionBar?.hide()

        //to implement
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_annonce)
    }

}