package xcj.app.appsets.ui.nonecompose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import xcj.app.appsets.R

class RecyclerViewTemplateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview_template)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, RecyclerViewTemplateFragment.newInstance())
                .commitNow()
        }
    }
    companion object{
        fun toHere(context:Context){
            context.startActivity(Intent(context, RecyclerViewTemplateActivity::class.java))
        }
    }
}