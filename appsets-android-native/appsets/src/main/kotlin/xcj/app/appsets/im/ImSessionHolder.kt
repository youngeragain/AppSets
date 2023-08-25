package xcj.app.appsets.im

interface ImSessionHolder {
    var imSession: Session?
    fun asImSingle(): ImObj.ImSingle?
    fun asImGroup(): ImObj.ImGroup?
}