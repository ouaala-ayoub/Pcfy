package alpha.company.pc.ui.activities

import alpha.company.pc.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.squareup.picasso.Picasso

class DemandsModifyActivity : AppCompatActivity() {
    lateinit var picasso: Picasso
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        picasso = Picasso.get()
        supportActionBar?.hide()
        setContentView(R.layout.activity_demands_modify)
    }
}