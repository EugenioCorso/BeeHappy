package ami.beehappy.beehappy

// dependency compiled from maven (build.gradle app module)
import android.content.Context

import com.loopj.android.http.*

import cz.msebera.android.httpclient.HttpEntity
import cz.msebera.android.httpclient.entity.StringEntity

// this class manages the connection to the server
// it performs http requests using a restful api
// it receives json strings which contain the information
class RestHttpHandler {
    // setter, useful to give server location
    var basE_URL = "http://192.168.0.136:9888"
    private val defaultBaseUrl = "http://192.168.2.131:8080"
    private val client: AsyncHttpClient

    init {
        this.client = AsyncHttpClient()
    }

    // perform an HTTP GET request to the server
    fun get(url: String, responseHandler: AsyncHttpResponseHandler) {
        client.get(getAbsoluteUrl(url), responseHandler)
    }

    // perform an HTTP POST request to the server
    fun put(url: String, entity: HttpEntity, responseHandler: AsyncHttpResponseHandler) {
        client.post(null, getAbsoluteUrl(url), entity, "application/json", responseHandler)
    }

    // builds baseurl+relative (parameters) url to perform requests
    private fun getAbsoluteUrl(relativeUrl: String): String {
        return this.basE_URL + relativeUrl
    }

    fun restoreBASE_URL() {
        this.basE_URL = this.defaultBaseUrl
    }

}
