package ami.beehappy.beehappy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*

import java.util.ArrayList


class BeeHappyMain : AppCompatActivity() {
    // the array that will be used to retreive previously saved IDs
    private lateinit var adapter : ArrayAdapter<String>
    private val idList = ArrayList<String>()

    // this creates the activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bee_happy_main)
        // retrieve previously saved ids
        retreiveIdArray()
        this.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, idList)

        val listView = findViewById(R.id.list) as ListView
        listView.adapter = this.adapter
        listView.onItemClickListener = MessageClickedHandler

        var nt: NotificationHelper = NotificationHelper(this.baseContext)
    }

    // called when the user taps the Enter button on the Select Id screen
    // onClick attribute requires: public void and View view as only argument
    fun enterId(view: View) {
        // an intent is the ability to do something (start the displayMessageActivity
        val editText = findViewById(R.id.editText) as EditText
        val id = editText.text.toString()
        // add msg to the list of ids
        if (!idList.contains(id)) {
            idList.add(id)
        }
        this.saveId()
        // start the info activity with the entered id
        startInfoActivity(id)
    }

    fun clearID(view: View) {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        // clear the id file
        editor.clear()
        editor.commit()
    }

    private fun startInfoActivity(id: String) {
        val intent = Intent(this, DisplayActivity::class.java)
        intent.putExtra(ID_MESSAGE, id)
        startActivity(intent)
    }

    // save the entered IDs (it saves the whole list everytime)
    private fun saveId() {
        // create / retrieve a file on which preferences can be stored
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        // edit it
        val editor = sharedPref.edit()
        // for every element in the list of ids saved in memory
        // here we provide to putString the pair (key, value)
        // we also save the size of the id
        editor.putInt("Id_size", idList.size)
        for (i in idList.indices) {
            editor.remove("Id_" + i)
            editor.putString("Id_" + i, this.idList[i])
        }
        // finish by committing the changes to the shared preferences file
        editor.commit()


    }

    // retrieve an array of IDs from the shared preferences file
    private fun retreiveIdArray() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        // clear the idList (is going to be filled with the new values
        this.idList.clear()
        // here we retrieve each id
        // we then put it in idList to be loaded in the ListView adapter
        val size = sharedPref.getInt("Id_size", 0)
        for (i in 0..size - 1) {
            val id = sharedPref.getString("Id_" + i, null)
            this.idList.add(id)
        }
    }

    // Create a message handling object as an anonymous class, useful to handle click events on ListView
    private val MessageClickedHandler = AdapterView.OnItemClickListener { parent, v, position, id ->
        // Start activity in response to the click
        val entry = parent.getItemAtPosition(position) as String
        startInfoActivity(entry)
    }

    companion object {
        var ID_MESSAGE: String = ""
    }
}
