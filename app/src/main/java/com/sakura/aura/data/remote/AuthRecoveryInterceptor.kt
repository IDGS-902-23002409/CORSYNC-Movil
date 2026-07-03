package com.sakura.aura.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.net.SocketException

class AuthRecoveryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        try {
            return chain.proceed(request)
        } catch (e: SocketException) {
            // Verificar si el error ocurrió específicamente en el POST de register
            if (request.method == "POST" && url.endsWith(ApiEndpoints.AUTH_REGISTER)) {

                // Extraer los datos del cuerpo original para armar el Login
                val buffer = okio.Buffer()
                request.body?.writeTo(buffer)
                val requestBodyString = buffer.readUtf8()

                val jsonObject = JSONObject(requestBodyString)
                val username = jsonObject.optString("username")
                val password = jsonObject.optString("password")

                // Construir el nuevo JSON para el Login
                val loginJson = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }.toString()

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val loginBody = loginJson.toRequestBody(mediaType)

                // Reconstruir la URL mutándola hacia el endpoint de Login
                val loginUrl = url.replace(ApiEndpoints.AUTH_REGISTER, ApiEndpoints.AUTH_LOGIN)

                val loginRequest = request.newBuilder()
                    .url(loginUrl)
                    .post(loginBody)
                    .build()

                // Proceder con la petición de Login y retornar su respuesta al flujo
                return chain.proceed(loginRequest)
            }
            throw e
        }
    }
}