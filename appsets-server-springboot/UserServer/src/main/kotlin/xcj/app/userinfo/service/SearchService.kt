package xcj.app.userinfo.service

import xcj.app.DesignResponse
import xcj.app.userinfo.model.res.CombineSearchRes

interface SearchService{
    fun searchByKeywords(keywords: String, types:String? = null, page: Int?, pageSize: Int?): DesignResponse<CombineSearchRes>
}