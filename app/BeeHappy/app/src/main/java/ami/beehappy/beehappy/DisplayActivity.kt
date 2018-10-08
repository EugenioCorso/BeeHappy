package ami.beehappy.beehappy

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.support.v4.widget.TextViewCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import android.widget.ToggleButton

import com.loopj.android.http.*

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

import java.io.UnsupportedEncodingException

import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpEntity
import cz.msebera.android.httpclient.entity.StringEntity


class DisplayActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_display)

            // Get the intent that created this activity
            val intent = intent
            // extract its message
            val msg = intent.getStringExtra(BeeHappyMain.ID_MESSAGE)

            // capture the textview layout and set the string as its text
            val textView = findViewById(R.id.textView) as TextView
            textView.text = msg

            val humToggle = newToggleButton(R.id.humToggle, "humidity")
            val tempToggle = newToggleButton(R.id.tempToggle, "temperature")


            // check if the address was previously saved
            var addr = checkAddressSaved()
            if (addr != null) {
                // it was: set it
                restHandle.basE_URL = "http://" + addr
            } else {
                // it wasn't: restore default
                restHandle.restoreBASE_URL()
                addr = restHandle.basE_URL.split("://".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1]
            }
            // show current address
            val editText = findViewById(R.id.editText2) as EditText
            editText.setText(addr)

            // query for infos
            refreshValues()
            // start the background notification service
            var jobScheduled = scheduleJob(0)
            if (!jobScheduled){
                // close activity, return to hive selection
                var nt: NotificationHelper = NotificationHelper(this.baseContext)
                nt.notifyEvent("BeeHappy was unable to start the notification service. Leaving the hive!")
                onBackPressed()
            }
        }

        private fun newToggleButton(id: Int, endpoint: String): ToggleButton {
            // return a toggleButton with enabled / disabled function set
            // requires an id to determine the button, an endpoint to associate
            val toggle = findViewById(id) as ToggleButton
            toggle.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    // toggle enabled
                    try {
                        putVal(endpoint, arrayOf("active"), arrayOf("on"))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }

                } else {
                    // The toggle is disabled
                    try {
                        putVal(endpoint, arrayOf("active"), arrayOf("off"))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }

                }
            }
            return toggle
        }

        fun refreshValues() {
            val v = GetValues()
            queryVal("humidity", v.getHumKeys(), v::setHum)
            queryVal("temperature", v.getTempKeys(), v::setTemp )
        }

        fun refreshValues(endpoint: String) {
            val v = GetValues()
            if (endpoint == "humidity") {
                queryVal("humidity", v.getHumKeys(), v::setHum)
            } else if (endpoint == "temperature") {
                queryVal("temperature", v.getTempKeys(), v::setTemp)
            }
        }

        fun changeIP(view: View) {
            val editText = findViewById(R.id.editText2) as EditText
            val id = editText.text.toString()
            // save the address read from the textview
            saveAddress(id)
            // use the provided address as baseurl
            restHandle.basE_URL = "http://" + id
            refreshValues()
        }

        private fun saveAddress(addr: String) {
            // create / retrieve a file on which preferences can be stored
            val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
            // edit it
            val editor = sharedPref.edit()
            // here we provide to putString the pair (key, value)
            // put the ID of the hive as key and the address as value
            editor.putString(BeeHappyMain.ID_MESSAGE, addr)
            // finish by committing the changes to the shared preferences file
            editor.commit()
        }

        private fun checkAddressSaved(): String? {
            // check if the address corresponding to the shown ID is saved
            // if so, return it
            val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
            if (sharedPref.contains(BeeHappyMain.ID_MESSAGE)) {
                return sharedPref.getString(BeeHappyMain.ID_MESSAGE, null)
            } else {
                return null
            }
        }

        fun startFoodActivity(view: View) {
            // start the food scheduler
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
        }

        fun feedNow(view: View){
            putVal("food", arrayOf("feedNow", "active"), arrayOf("True", "on"))
        }

        fun startWebcamActivity(view: View) {
            // start the food scheduler
            val webcam = WebcamHelper()
            var listIndexImages = arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            for (i: Int in listIndexImages) {
                webcam.download_image("http:"+ restHandle.basE_URL.split(":").get(1) + ":80/media/_" +i+".jpg", this.baseContext)
                val TAG = "WebcamDownload"
                Log.i(TAG, "http:"+ restHandle.basE_URL.split(":").get(1) + ":80/media/_" +i+".jpg")
            }
        }


        private fun queryVal(endpoint: String, keys: Array<String>, set: (String) -> (Unit) ) {
            // uses the restHandle to send a GET request to the server
            // the baseurl of the server is currently hardcoded int RestHttpHandler.java, a setter can modify it
            // the response is supposed to be in a json
            // the method from the interface provided sets the info received on the TextView object
            restHandle.get("/" + endpoint, object : JsonHttpResponseHandler() {
                // here we declare the methods WHOSE DECLARATION CANNOT BE CHANGED
                // they are executed in response to the HTTP status of the request
                override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                    // called when response HTTP status is "200 OK"
                    var json: JSONObject? = null
                    try {
                        json = JSONTokener(response!!.toString()).nextValue() as JSONObject
                        // for string (key) in list of keys
                        // update the gui to the received values
                        for (key in keys) {
                            if (key != "active") { // key = active is used for toggles
                                val respVal = json.getString(key)
                                set(json.getString(key))
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        // val.set("Json decoding error");
                    }

                }

                override fun onFailure(statusCode: Int, headers: Array<Header>?, t: Throwable, response: JSONObject?) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    set("ConnectionError")
                }

            })
        }

        @Throws(JSONException::class, UnsupportedEncodingException::class)
        private fun putVal(endpoint: String, keys: Array<String>, values: Array<String>) {
            // uses the restHandle to send a POST request to the server
            // the baseurl of the server is currently hardcoded int RestHttpHandler.java, a setter can modify it
            // the response is supposed to be in a json
            // the method from the interface provided sets the info received on the TextView object

            // we build a json with the key-value pair given
            val jsonParams = JSONObject()
            var i = 0
            while (i<keys.size){
                jsonParams.put(keys.get(i), values.get(i))
                i++
            }
            val entity = StringEntity(jsonParams.toString())

            restHandle.put("/" + endpoint, entity, object : JsonHttpResponseHandler() {
                // here we declare the methods WHOSE DECLARATION CANNOT BE CHANGED
                // they are executed in response to the HTTP status of the request
                override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONObject?) {
                    // called when response HTTP status is "200 OK"
                    var json: JSONObject? = null
                    try {
                        json = JSONTokener(response!!.toString()).nextValue() as JSONObject
                        // check if the response is OK
                        val status = json.getString("status")
                        val active = json.getString("active")
                        if (status == "err") {
                            val err = json.getString("err")
                            // TODO it should notify the error
                        }
                        if (status == "ok") {
                            if (active == "on") {
                                refreshValues(endpoint)
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        // val.set("Json decoding error");
                    }

                }

                override fun onFailure(statusCode: Int, headers: Array<Header>?, t: Throwable, response: JSONObject?) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    // TODO it should notify the error
                    return
                }

            })
        }

        // these are the methods used by queryVal
        inner class GetValues {
            private val tempKeys = arrayOf("temperature", "active")
            private val humKeys = arrayOf("humidity", "active")

            fun getHumKeys(): Array<String>{
                return this.humKeys
            }

            fun getTempKeys(): Array<String>{
                return this.tempKeys
            }

            private fun isValid(s: String?): Boolean {
                if (s == null || s === "") {
                    return false
                }
                return true
            }

            fun setTemp(temp: String) {
                val text = findViewById(R.id.tempVal) as TextView
                if (isValid(temp)) {
                    var s = String.format("%.2f", temp.toFloat());
                    text.text = s + " °C"
                } else {
                    text.text = "Request Failed"
                }
            }

            fun setHum(hum: String) {
                val text = findViewById(R.id.humVal) as TextView
                if (isValid(hum)) {
                    var s = String.format("%.2f", hum.toFloat());
                    text.text = s + " %"
                } else {
                    text.text = "Request Failed"
                }
            }
        }

    // section used to start the background service
    // the intent passes the url to the worker
    private fun scheduleJob(jobId: Int): Boolean {
        // job scheduler class
        var jobScheduler = getSystemService (Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        // create a new BackgroundService for notification management

        var serviceName = ComponentName(this.baseContext, BackgroundService::class.java);
        var jobInfo = JobInfo.Builder(jobId, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // work on any network type
                .setPeriodic(6000) // milliseconds, this is 1 minute
                .build();

        var result = jobScheduler.schedule(jobInfo); // use the scheduler to set the job
        if (result == JobScheduler.RESULT_SUCCESS) {
            return true // started!
        }
        return false // causes the activity to exit
    }

    companion object {
        // accessible from outer classes
        var restHandle = RestHttpHandler()
        var WORK_DURATION_KEY = BuildConfig.APPLICATION_ID + ".WORK_DURATION_KEY"
    }
}



