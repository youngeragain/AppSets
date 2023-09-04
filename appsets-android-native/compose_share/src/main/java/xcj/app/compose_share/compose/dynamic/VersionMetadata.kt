package xcj.app.compose_share.compose.dynamic

data class VersionMetadata(val versionCode: Int = 1, val versionName: String = "1.0.0") {
    fun newerThan(versionMetadata: VersionMetadata): Boolean {
        return versionCode > versionMetadata.versionCode
    }

    companion object {
        fun defaultVersionMetadata(): VersionMetadata {
            return VersionMetadata()
        }
    }
}
