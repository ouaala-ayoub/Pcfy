package alpha.company.pc.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import alpha.company.pc.R


class UserAnnoncesActivity : AppCompatActivity() {

    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        //to change with fragment ?
        userId = intent.getStringExtra("id")!!
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_user_annonces)
    }


}