package xcj.app.compose_share.dynamic

abstract class AbstractStateHolder : StatesHolder {
    override fun onLoad() {

    }

    override fun onUnLoad() {

    }

    override fun onReuse() {

    }

    override fun onDestroy() {

    }

    override fun onTempDestroy() {

    }

    override fun reusable(): Boolean {
        return false
    }
}