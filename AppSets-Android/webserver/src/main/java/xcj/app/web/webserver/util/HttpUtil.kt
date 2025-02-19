package xcj.app.web.webserver.util

import io.netty.handler.codec.http.HttpHeaderNames
import xcj.app.starter.util.ContentType
import xcj.app.web.webserver.netty.HttpRequestWrapper
import kotlin.text.lowercase

fun isMultiPart(httpRequestWrapper: HttpRequestWrapper): Boolean {
    val contentType = httpRequestWrapper.httpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE)
        .lowercase()
    return contentType == ContentType.MULTIPART_FORM_DATA
}