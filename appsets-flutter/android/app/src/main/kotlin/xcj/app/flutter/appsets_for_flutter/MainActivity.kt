package xcj.app.flutter.appsets_for_flutter

import android.content.Intent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "tc").setMethodCallHandler { call, result ->
            when(call.method){
                "toStdActivity"->{
                    startActivity(Intent(this, StdActivity::class.java))
                }
            }
        }
    }
}
