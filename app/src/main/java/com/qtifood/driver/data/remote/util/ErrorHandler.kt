package com.qtifood.driver.data.remote.util

import retrofit2.Response

object ErrorHandler {
    
    /**
     * Extract error message from API response based on status code
     */
    fun getErrorMessage(response: Response<*>?): String {
        if (response == null) {
            return "Không thể kết nối tới server"
        }
        
        return when (response.code()) {
            404 -> "Mất kết nối"
            500 -> "Vui lòng đến đúng vị trí giao hàng"
            else -> "Có lỗi xảy ra. Vui lòng thử lại"
        }
    }
}
