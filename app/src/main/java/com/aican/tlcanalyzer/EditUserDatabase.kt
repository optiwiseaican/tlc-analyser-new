package com.aican.tlcanalyzer

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.database.UsersDatabase
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditUserDatabase : AppCompatActivity() {

    lateinit var spinner: Spinner
    lateinit var name: EditText
    lateinit var passwordText: EditText
    var r = arrayOf("Operator", "Supervisor")
    lateinit var databaseHelper: DatabaseHelper
    lateinit var userDatabase: UsersDatabase
    var username = ""
    var userRole = ""
    lateinit var update: Button
    var password = ""
    var uid = ""
    var prevPasscode = ""
    lateinit var previPasscode: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_database)
        spinner = findViewById(R.id.selectRole)
        name = findViewById(R.id.username)
        databaseHelper = DatabaseHelper(this)
        update = findViewById(R.id.updateBtn)
        passwordText = findViewById(R.id.password)
        previPasscode = findViewById(R.id.previPasscode)


        username = intent.getStringExtra("username")!!
        userRole = intent.getStringExtra("userrole")!!
        uid = intent.getStringExtra("uid")!!
        prevPasscode = intent.getStringExtra("passcode")!!

        userDatabase = UsersDatabase(this@EditUserDatabase)

        name.setText(username)

        val role = ArrayAdapter(this, android.R.layout.simple_spinner_item, r)
        role.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = role
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                userRole = adapterView.getItemAtPosition(i).toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        if (userRole == r[0]) {
            spinner.setSelection(0)
        } else {
            spinner.setSelection(1)
        }

//        previPasscode.setText(prevPasscode);


//        previPasscode.setText(prevPasscode);
        update.setOnClickListener { view: View? ->
            if (passwordText.text.toString().isEmpty() || passwordText.text
                    .toString() == "" || passwordText.text.toString() == prevPasscode
            ) {
                if (passwordText.text.toString() == prevPasscode) {
                    previPasscode.text = "Your previous passcode is same as your current passcode"
                    passwordText.error = "Your previous passcode is same as your current passcode"
                } else {
                    passwordText.error = "Enter some password"
                }
            } else {
                val date =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date())
                val time = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date())
                password = passwordText.text.toString()
                val passwordUpdated: Boolean = userDatabase.updateUserDetails(
                    username,
                    uid,
                    name.text.toString(),
                    userRole,
                    password
                )
                if (passwordUpdated) {
//                    databaseHelper.insert_action_data(
//                        time,
//                        date,
//                        "Username: " + username + ", Name: " + name.text.toString() + ", UID: " + uid
//                                + " Password changed",
//                        "",
//                        "",
//                        "",
//                        "",
//                        PhActivity.DEVICE_ID
//                    )
                    Toast.makeText(
                        this,
                        "Password updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getExpiryDate(): String? {
        val date = Calendar.getInstance().time
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val presentDate = dateFormat.format(date)
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        try {
            cal.time = sdf.parse(presentDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        // use add() method to add the days to the given date
        cal.add(Calendar.DAY_OF_MONTH, 90)
        return sdf.format(cal.time)
    }

    private fun getPresentDate(): String? {
        val date = Calendar.getInstance().time
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(date)
    }

}