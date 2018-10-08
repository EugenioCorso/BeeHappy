package ami.beehappy.beehappy


import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.loopj.android.http.BinaryHttpResponseHandler
import cz.msebera.android.httpclient.Header
import java.io.*



class WebcamHelper {

    companion object {
        var restHandle = RestHttpHandler()
    }

    var location = "/beehappy/media/"

    fun download_image (url: String, context: Context) {
       /* restHandle.get(url, responseHandler = object : BinaryHttpResponseHandler() {
            override fun onFailure(statusCode: Int, headers: Array<out Header>?, binaryData: ByteArray?, error: Throwable?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            // here we declare the methods WHOSE DECLARATION CANNOT BE CHANGED
            // they are executed in response to the HTTP status of the request
            override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: ByteArray) {
                // called when response HTTP status is "200 OK"
                val f: FileSaver = FileSaver()
                val dir: File = f.getAlbumStorageDir("BeeHappy")
                f.saveFile(context, response, dir.name+'/'+"f.jpg")
            }
        })*/
        val request = DownloadManager.Request(Uri.parse(url))
        request.setDescription("image from the hive (swarming)")
        request.setTitle("Image_from_the_Hive")
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "f.jpg")

        // get download service and enqueue file
        val manager: DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }
}
