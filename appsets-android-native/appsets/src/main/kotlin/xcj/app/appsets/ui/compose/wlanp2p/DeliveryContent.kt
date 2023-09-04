package xcj.app.appsets.ui.compose.wlanp2p

data class DeliveryContent(
    val header: Map<String, Any>,
    val body: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeliveryContent

        if (header != other.header) return false
        if (body != null) {
            if (other.body == null) return false
            if (!body.contentEquals(other.body)) return false
        } else if (other.body != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        return result
    }

}