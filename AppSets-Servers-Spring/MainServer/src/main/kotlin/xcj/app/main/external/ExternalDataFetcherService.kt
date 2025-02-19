package xcj.app.main.external

import xcj.app.DesignResponse

interface ExternalDataFetcherService {

    companion object {
        const val FETCH_WHAT_BAIDU_HOT_DATA = 0//baidu
        const val FETCH_WHAT_MS_BING_WALLPAPER = 1//bing
    }

    fun fetch(fetchWhat: Int): DesignResponse<Boolean>
}