package xcj.app.appsets.ktx

import android.os.Build

internal fun <E> MutableCollection<E>?.removeByPlatform(item: E?) {
    this?:return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.removeIf { it == item }
    } else {
        for (item1 in this) {
            if (item1 == item) {
                this.remove(item1)
                break
            }
        }
    }
}

internal fun <E> MutableCollection<E>?.removeAllByPlatform(items: List<E>?) {
    items ?: return
    this ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.removeIf { items.contains(it) }
    } else {
        for (item1 in this) {
            for (item2 in items) {
                if (item1 == item2) {
                    this.remove(item1)
                    break
                }
            }
        }
    }
}