package xcj.app.main.model.table.mysql

import java.io.Serializable
import java.util.Date

data class ScreenReview(
    val reviewId: String,
    val content: String,
    val reviewTime: Date,
    val likes: Int,
    val dislikes: Int,
    val pid: String?,
    val uid: String,
    val screenId: String,
    val reviewPassed: Int,
    val withdraw: Int,
    val isPublic: Int
) : Serializable
