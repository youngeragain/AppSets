package xcj.app.userinfo.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import xcj.app.ApiDesignEncodeStr
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.userinfo.model.res.CombineSearchRes
import xcj.app.userinfo.service.SearchService


@RestController
@RequestMapping("/search")
class SearchController(val searchService: SearchService) {

    @ApiDesignPermission.LoginRequired
    @GetMapping("common")
    fun combineSearch(
        @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String,
        @RequestParam(name = "keywords") keywords:String,
        @RequestParam(name = "types", required = false) types:String? = null,
        @RequestParam(name = "page", required = false) page:Int? = 1,
        @RequestParam(name = "size", required = false) pageSize:Int? = 20,
    ):DesignResponse<CombineSearchRes>{
        return searchService.searchByKeywords(keywords, types, page, pageSize)
    }


}