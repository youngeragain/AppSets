package com.example.test101.webserver.jetty

import android.util.Log
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

class RequestHandler: AbstractHandler(){
    override fun handle(
        target: String?,
        baseRequest: Request?,
        request: HttpServletRequest?,
        response: HttpServletResponse?
    ) {
        Log.e("blue", "handle:$target")
    }
}