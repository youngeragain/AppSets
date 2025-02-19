package xcj.app.rtc

import com.tinder.scarlet.Scarlet

object ScarletHelper{

    val scarletInstance = Scarlet.Builder()
        .webSocketFactory(WebSocketFactory2())
       /* .addMessageAdapterFactory(MoshiMessageAdapter.Factory())
        .addStreamAdapterFactory(RxJava2StreamAdapterFactory())*/
        .build()


}