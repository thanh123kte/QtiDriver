package com.qtifood.driver.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.nio.charset.Charset

class Utf8ResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        // Đảm bảo error body được đọc với UTF-8 encoding
        val originalBody = response.body
        
        return if (originalBody != null) {
            val contentType = originalBody.contentType()
            val charset = contentType?.charset(Charset.forName("UTF-8")) ?: Charset.forName("UTF-8")
            
            // Đọc body với UTF-8 charset
            val bodyString = originalBody.string()
            
            // Tạo response body mới với UTF-8
            val newBody = okhttp3.ResponseBody.create(
                contentType,
                bodyString.toByteArray(charset)
            )
            
            response.newBuilder()
                .body(newBody)
                .build()
        } else {
            response
        }
    }
}
