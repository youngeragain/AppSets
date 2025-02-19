package xcj.app.main.external

import org.springframework.stereotype.Component
import xcj.app.DesignResponse
import xcj.app.main.service.ExternalDataFetcher

@Component
class ExternalDataFetcherServiceImplV1(
    private val externalDataFetcher: ExternalDataFetcher
) : ExternalDataFetcherService {
    override fun fetch(fetchWhat: Int): DesignResponse<Boolean> {
        if (
            fetchWhat != ExternalDataFetcherService.FETCH_WHAT_MS_BING_WALLPAPER &&
            fetchWhat != ExternalDataFetcherService.FETCH_WHAT_BAIDU_HOT_DATA
        ) {
            return DesignResponse(data = false, info = "no implementation!")
        }
        externalDataFetcher.doFetch(fetchWhat)
        return DesignResponse(data = true)
    }
}