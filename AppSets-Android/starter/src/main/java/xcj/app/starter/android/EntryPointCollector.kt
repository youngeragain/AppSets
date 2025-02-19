package xcj.app.starter.android

import android.content.Context

object EntryPointCollector{
    val entryPoints: MutableMap<String, Class<Context>> = mutableMapOf()
    var applicationRootPath = "/application/"
    fun startCollectEntryPointsInApplication(){
        
    }
}