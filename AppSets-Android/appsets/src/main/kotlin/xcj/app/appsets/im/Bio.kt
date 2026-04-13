package xcj.app.appsets.im

import android.os.Parcelable

interface Bio : Parcelable {
    val bioId: String
    val bioName: String?
    val bioUrl: Any?
}