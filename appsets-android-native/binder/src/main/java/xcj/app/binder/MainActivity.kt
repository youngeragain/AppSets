package xcj.app.binder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import xcj.app.core.android.DesignMessageDeliver

class MainActivity : AppCompatActivity() {
    lateinit var tvServiceState:AppCompatTextView
    lateinit var tvMessageReceivedFromRemote:AppCompatTextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvServiceState = findViewById(R.id.tv_shareable_message_service_state)
        tvMessageReceivedFromRemote = findViewById(R.id.tv_message_from)
        DesignMessageDeliver.deliveryThreadType = DesignMessageDeliver.DELIVERY_TYPE_OTHER_THREAD
        DesignMessageDeliver.observe("Message_From_Remote", this, Observer<String?> {
            /*runOnUiThread {

            }*/
            tvMessageReceivedFromRemote.text = "Message from remote:$it"
        })
        DesignMessageDeliver.observe("Service_Connect_State", this, Observer<String?> {
            /*runOnUiThread {

            }*/
            tvServiceState.text = "Server state:$it"
        })
    }
}