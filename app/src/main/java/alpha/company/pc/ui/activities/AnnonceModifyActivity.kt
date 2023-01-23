package alpha.company.pc.ui.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.AnnonceModifyRepository
import alpha.company.pc.databinding.ActivityAnnonceModifyBinding
import alpha.company.pc.ui.viewmodels.AnnonceModifyModel
import com.squareup.picasso.Picasso

class AnnonceModifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnonceModifyBinding
    val viewModel =
        AnnonceModifyModel(AnnonceModifyRepository(RetrofitService.getInstance()))
    val picasso: Picasso = Picasso.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hiding the action bar
        supportActionBar?.hide()
        binding = ActivityAnnonceModifyBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

}
