package xcj.app.binder.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import xcj.app.starter.android.util.LocalMessager

class MainActivity : AppCompatActivity() {
    lateinit var tvServiceState: AppCompatTextView
    lateinit var tvMessageReceivedFromRemote: AppCompatTextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(xcj.app.binder.R.layout.activity_main)
        tvServiceState = findViewById(xcj.app.binder.R.id.tv_shareable_message_service_state)
        tvMessageReceivedFromRemote = findViewById(xcj.app.binder.R.id.tv_message_from)
        LocalMessager.observe<String, String?>(this, "Message_From_Remote") {
            tvMessageReceivedFromRemote.text = "Message from remote:$it"
        }
        LocalMessager.observe<String, String>(this, "Service_Connect_State") {
            tvServiceState.text = "Server state:$it"
        }
    }
}