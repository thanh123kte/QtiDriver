package com.qtifood.driver.core

object Constants {
    const val BASE_URL = "https://inconceivably-octantal-chan.ngrok-free.dev"
    const val API_BASE_URL = "$BASE_URL/api"
    const val FIREBASE_DB_URL = "https://datn-foodecommerce-default-rtdb.asia-southeast1.firebasedatabase.app"
    
    // Image URLs
    fun getImageUrl(path: String): String {
        return if (path.startsWith("http")) {
            path
        } else {
            "$BASE_URL${if (path.startsWith("/")) path else "/$path"}"
        }
    }
}
