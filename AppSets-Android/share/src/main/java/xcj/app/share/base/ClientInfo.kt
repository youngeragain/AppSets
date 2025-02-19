package xcj.app.share.base

data class ClientInfo(val host: String) {
    companion object {
        val NONE_RESOLVED = ClientInfo("127.0.0.1:8080")
    }
}