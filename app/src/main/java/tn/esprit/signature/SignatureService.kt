package tn.esprit.signature

import android.graphics.Bitmap
import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.nio.ByteBuffer


object SignatureService {

    val baseUrl = "http://102.159.219.35:5000/"

    val api: SignatureApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .let {
            it.create(SignatureApi::class.java)
        }

    fun addSignature(image: File, name: String, callback: SignatureCallback) {

        val requestImage = RequestBody.create(MediaType.parse("image/*"), image)
        val body =
            MultipartBody.Part.createFormData("image", "image.jpg", requestImage)

        val call = api.addSignature(
            body,
            RequestBody.create(MediaType.parse("text/plain"), name)
        )

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                when (response.code()) {
                    200 -> callback.onSuccess()
                    400 -> callback.onError("There was an unexpected error")
                    409 -> callback.onError("Signature already exists")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("error", "", t)
                callback.onError("Could not perform network call")
            }
        })
    }

    fun checkSignature(image: File, name: String, callback: SignatureCallback) {

        val requestImage = RequestBody.create(MediaType.parse("image/*"), image)
        val body =
            MultipartBody.Part.createFormData("image", "image.jpg", requestImage)

        val call = api.checkSignature(
            body,
            RequestBody.create(MediaType.parse("text/plain"), name)
        )

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("response", response.toString())
                when (response.code()) {
                    200 -> callback.onSuccess()
                    400 -> callback.onError("There was an unexpected error")
                    404 -> callback.onError("No match found")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("error", "", t)
                callback.onError("Could not perform network call")
            }
        })
    }

}

interface SignatureCallback {
    fun onSuccess(name: String? = null)
    fun onError(msg: String)
}

interface SignatureApi {

    @Multipart
    @POST("add")
    fun addSignature(
        @Part image: MultipartBody.Part,
        @Part("name") name: RequestBody
    ): Call<Void>

    @Multipart
    @POST("predict")
    fun checkSignature(
        @Part image: MultipartBody.Part,
        @Part("name") name: RequestBody
    ): Call<Void>

}

fun Bitmap.toByteArray(): ByteArray {
    val size = rowBytes * height
    val byteBuffer = ByteBuffer.allocate(size)
    copyPixelsToBuffer(byteBuffer)
    return byteBuffer.array()
}