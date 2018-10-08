package ami.beehappy.beehappy

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.TimePicker

import com.loopj.android.http.JsonHttpResponseHandler

import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

import java.io.UnsupportedEncodingException

import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.HttpEntity
import cz.msebera.android.httpclient.entity.StringEntity

class FeedActivity : AppCompatActivity() {

    private var hour: Int = 0
    private var minutes: Int = 0
    private var days: Int = 0
    private val foodActive: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        // retrieve schedule from file
        retreiveSched()
        setTime()
        // create a numberpicker
        val np = findViewById(R.id.numberPicker) as NumberPicker
        np.minValue = 0
        np.maxValue = 30
        np.value = this.days
        //np.setWrapSelectorWheel(true);
        np.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        //Set a value change listener for NumberPicker
        np.setOnValueChangedListener { picker, oldVal, newVal ->
            //Display the newly selected value from picker
            setDay(newVal)
            saveSchedule()
            try {
                sendSchedule("food")
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
    }

    fun setHour(hour: Int) {
        this.hour = hour
    }

    fun setDay(d: Int) {
        this.days = d
    }

    fun setMinutes(min: Int) {
        this.minutes = min
    }

    fun setTime() {
        val text = findViewById(R.id.feedingTime) as TextView
        text.text = this.hour.toString() + ":" + this.minutes
    }

    @Throws(JSONException::class, UnsupportedEncodingException::class)
    private fun sendSchedule(endpoint: String) {
        // uses the restHandle to send a POST request to the server
        // the baseurl of the server is currently hardcoded int RestHttpHandler.java, a setter can modify it
        // the response is supposed to be in a json
        // the method from the interface provided sets the info received on the TextView object
        var restH = DisplayActivity.restHandle
        // we build a json with the key-value pair given
        val jsonParams = JSONObject()
        jsonParams.put("hour", this.hour)
        jsonParams.put("days", this.days)
        jsonParams.put("minutes", this.minutes)
        jsonParams.put("active", this.foodActive)
        val entity = StringEntity(jsonParams.toString())

        restH.put("/" + endpoint, entity, object : JsonHttpResponseHandler() {
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
                            // refreshValues(endpoint);
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // val.set("Json decoding error");
                }

            }
        })
    }

    private fun saveSchedule() {
        // create / retrieve a file on which preferences can be stored
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        // edit it
        val editor = sharedPref.edit()
        // write days, hour, minutes
        editor.putInt("days", this.days)
        editor.putInt("hour", this.hour)
        editor.putInt("minutes", this.minutes)
        // finish by committing the changes to the shared preferences file
        editor.commit()
    }

    private fun retreiveSched() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        // clear the idList (is going to be filled with the new values
        // here we retrieve each id
        // we then put it in idList to be loaded in the ListView adapter
        this.hour = sharedPref.getInt("hour", 0)
        this.minutes = sharedPref.getInt("minutes", 0)
        this.days = sharedPref.getInt("days", 0)
    }

    fun showTimePickerDialog(v: View) {
        val timePicker = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            // as time is set, save it and display it
            setHour(hourOfDay)
            setMinutes(minute)
            setTime()
            saveSchedule()
        }, 0, 0, true)

        timePicker.setOnShowListener {
            // This is hiding the "Cancel" button:
            timePicker.getButton(Dialog.BUTTON_NEGATIVE).visibility = View.GONE
        }
        timePicker.show()
    }
}
