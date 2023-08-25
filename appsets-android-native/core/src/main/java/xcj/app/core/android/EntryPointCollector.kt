package xcj.app.core.android

import androidx.appcompat.app.AppCompatActivity

object EntryPointCollector{
    val entryPoints:MutableMap<String, Class<AppCompatActivity>> = mutableMapOf()
    var applicationRootPath = "/application/"
    fun startCollectEntryPointsInApplication(){
        
    }
}