package xcj.app.flutter.appsets_for_flutter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup

class StdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_std)
        ((findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0).setOnClickListener {
            startActivity(Intent(this, ComposeActivity::class.java))
        }
    }
}