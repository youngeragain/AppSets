package xcj.app.userinfo.model.common

import java.util.Base64

data class TencentCosRegionBucket(
    var region:String,
    var bucketName:String,
    var filePathPrefix:String){
    fun encode(){
        region = Base64.getEncoder().encodeToString(region.toByteArray())
        bucketName = Base64.getEncoder().encodeToString(bucketName.toByteArray())
        filePathPrefix = Base64.getEncoder().encodeToString(filePathPrefix.toByteArray())
    }
}