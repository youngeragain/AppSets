package xcj.app.main.service

import xcj.app.DesignResponse
import xcj.app.main.model.res.CombineSearchRes

interface SearchService {
    fun searchByKeywords(
        keywords: String,
        types: String? = null,
        page: Int?,
        pageSize: Int?
    ): DesignResponse<CombineSearchRes>
}