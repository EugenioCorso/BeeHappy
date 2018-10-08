package ami.beehappy.beehappy

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context
import android.content.Intent;
import android.graphics.BitmapFactory
import android.os.Handler;
import android.util.Log;
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Service to handle callbacks from the JobScheduler. Requests scheduled with the JobScheduler
 * ultimately land on this service's "onStartJob" method. It runs jobs for a specific amount of time
 * and finishes them.
 * BackgroundService polls the server for critical events such as swarming, humidity critical, food requirements
 * If any of these is reported (JSON), it notifies it
 */
class BackgroundService: JobService() {

    val TAG = "BackgroundService"

    override fun onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
    }


    override fun onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }



    override fun onStartJob(params: JobParameters): Boolean {
        // The work that this service "does" is simply wait for a certain duration and finish
        // the job (on another thread).
        Log.i(TAG, "onStartJob called")
        sendMessage(params.jobId)

        var duration = params.getExtras().getLong(DisplayActivity.WORK_DURATION_KEY);

        // Uses a handler to delay the execution of jobFinished().
        var handler = Handler();
        handler.postDelayed(Runnable() {
            fun run() {
                sendMessage(params.jobId)
                jobFinished(params, false);
            }
        }, duration);
        Log.i(TAG, "on start job: " + params.getJobId());

        // Return true as there's more work to be done with this job.
        return true;
    }

    override fun onStopJob (params: JobParameters): Boolean {
        // Stop tracking these job parameters, as we've 'finished' executing.
        sendMessage(params.getJobId());
        Log.i(TAG, "on stop job: " + params.getJobId());

        // Return false to drop the job.
        return false;
    }

    private fun sendMessage(params: Int) {
        Log.d(TAG, "sendMessage called")
        var keys = arrayOf("swarm", "feedDate", "fanOn") // TODO: what are critical events?
        queryVal("critical", keys) // ask the server for the report on critical events
    }

    private fun queryVal(endpoint: String, keys: Array<String> ) {
            // uses the restHandle to send a GET request to the server
            // the baseurl of the server is currently hardcoded int RestHttpHandler.java, a setter can modify it
            // the response is supposed to be in a json
            // if any critical event is reported, notify it
            Log.i(TAG, "entered queryVal")
            DisplayActivity.restHandle.get("/" + endpoint, object : JsonHttpResponseHandler() {
                // here we declare the methods WHOSE DECLARATION CANNOT BE CHANGED
                // they are executed in response to the HTTP status of the request
                override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                    // called when response HTTP status is "200 OK"
                    Log.i(TAG, "received response")
                    var json: JSONObject? = null
                    try {
                        json = JSONTokener(response!!.toString()).nextValue() as JSONObject
                        // for string (key) in list of keys
                        // update the gui to the received values
                        for (key in keys) {
                            if (key != "active") { // key = active is used for toggles
                                var value = json.getString(key)
                                Log.i(TAG, key + ": " + value)
                                if (key == "swarm"){ // or a boolean or an int, TBD
                                    // something happened, notify it
                                    if (value == "True") {
                                        notify(0, "Swarming detected!")
                                    }
                                }
                                if (key == "fanOn"){
                                    if (value == "True") {
                                        notify(1, "Fan activated!")
                                    }
                                }
                                if (key == "feedDate") {
                                    notify(2, "Last feeding: " + value)
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        // val.set("Json decoding error");
                    }

                }

                override fun onFailure(statusCode: Int, headers: Array<Header>?, t: Throwable, response: JSONObject?) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    // TODO should notify the error
                    Log.i(TAG, "Connection Error")
                }
            })
    }

    private fun notify (id: Int, target: String) {
        // notify the critical event
        var icon = R.mipmap.bee_icon
        var notificationManager = this.baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var notification = Notification.Builder(this.baseContext).setContentTitle("Bee Happy").setContentText(target)
                .setLargeIcon(BitmapFactory.decodeResource(this.baseContext.resources, icon))
                .setSmallIcon(icon)
                .build()

        var title = this.baseContext.getString(R.string.app_name);

        var notificationIntent = Intent(this.baseContext, this::class.java)
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        var intent = PendingIntent.getActivity(this.baseContext, 0, notificationIntent, 0);

        notificationManager.notify(id, notification);
        /*var nt: NotificationHelper = NotificationHelper(this.baseContext)
        nt. |= Notification.DEFAULT_VIBRATE;
        nt.notifyEvent(target)
        */
    }
}