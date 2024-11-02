package com.aican.tlcanalyzer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.adapterClasses.RecentlyUserCreated
import com.aican.tlcanalyzer.dataClasses.userIDPASS.UserData
import com.aican.tlcanalyzer.database.UsersDatabase
import com.aican.tlcanalyzer.databinding.ActivityAddUsersBinding
import com.aican.tlcanalyzer.dialog.AuthDialog
import com.aican.tlcanalyzer.utils.Source
import com.aican.tlcanalyzer.utils.Subscription
import com.aican.tlcanalyzer.utils.UserRoles
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar


class AddUsers : AppCompatActivity() {

    lateinit var usersDatabase: UsersDatabase
    lateinit var binding: ActivityAddUsersBinding
    lateinit var adapter: RecentlyUserCreated
    lateinit var arrayList: ArrayList<UserData>
    var totalUserMade = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        binding.back.setOnClickListener {
            startActivity(Intent(this@AddUsers, ProjectView::class.java))
            finishAffinity()
        }

        totalUserMade = intent.getStringExtra("userMade")!!.toInt()

        arrayList = ArrayList()

        usersDatabase = UsersDatabase(this@AddUsers)

        binding.userCreationBox.setOnClickListener {
            if (binding.userCreationBox.isChecked) {
                binding.projectCreationBox.isChecked = true
                binding.reportGenerationBox.isChecked = true
                binding.userCreationBox.isChecked = true
            }
        }

        binding.projectCreationBox.setOnClickListener {
            if (binding.projectCreationBox.isChecked) {
                binding.projectCreationBox.isChecked = true
                binding.reportGenerationBox.isChecked = true
            }
        }


        binding.assignRole.setOnClickListener {
            if (totalUserMade < Subscription.numberOfUsers) {
                addUser()
            } else {
                Toast.makeText(
                    this,
                    "You have exceeded the limit of user creation which is : " + Subscription.numberOfUsers,
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(this@AddUsers, AllUsersMade::class.java))
                finish()
            }
        }

        binding.btnUserDatabase.setOnClickListener {
            val intent = Intent(this@AddUsers, AllUsersMade::class.java)
            startActivity(intent)
        }

        binding.username.requestFocus()
        val imm =
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                binding.username,
                InputMethodManager.SHOW_IMPLICIT
            )

        binding.recView.visibility = View.GONE
        binding.tableLayout2.visibility = View.GONE
        binding.checkAddMultiUsers.isChecked = false

        binding.checkAddMultiUsers.setOnClickListener {
            if (binding.checkAddMultiUsers.isChecked) {
                binding.recView.visibility = View.VISIBLE
                binding.tableLayout2.visibility = View.VISIBLE
            } else {
                binding.recView.visibility = View.GONE
                binding.tableLayout2.visibility = View.GONE
            }
        }

    }


    private fun recentRecycler() {

        adapter = RecentlyUserCreated(this@AddUsers, arrayList)

        binding.recView.adapter = adapter

        adapter.notifyDataSetChanged()

    }

    private fun addUser() {

        val username = binding.username.text.toString()
        val userid = binding.userid.text.toString()
        val userpasscode = binding.userpasscode.text.toString()

        if (username.isEmpty() || userid.toString()
                .isEmpty() || userpasscode.toString()
                .isEmpty()
        ) {
            if (username.toString().isEmpty()) {
                binding.username.error = "Enter User Name"
            }
            if (userid.toString().isEmpty()) {
                binding.userid.error = "Enter User ID"
            }
            if (userpasscode.toString().isEmpty()) {
                binding.userpasscode.error = "Enter password"
            }
        } else {
            var role = UserRoles.OPERATOR
            if (binding.projectCreationBox.isChecked
                && binding.userCreationBox.isChecked
                && binding.reportGenerationBox.isChecked
            ) {
                role = UserRoles.ADMIN
            } else if (binding.projectCreationBox.isChecked && binding.reportGenerationBox.isChecked) {
                role = UserRoles.SUPERVISOR
            } else if (binding.reportGenerationBox.isChecked) {
                role = UserRoles.OPERATOR
            }



            usersDatabase.insert_data(username, role, userid, userpasscode)


            usersDatabase.logUserAction(
                "Admin",
                "Admin",
                "Admin Added User - $username & role - $role",
                "na",
                "na",
                AuthDialog.projectType
            )

            usersDatabase.logUserAction(
                username,
                role,
                "New User Added",
                "na",
                "na",
                AuthDialog.projectType
            )


            arrayList.add(
                UserData(
                    username,
                    role,
                    userid,
                    userpasscode,
                    getExpiryDate().toString(),
                    getPresentDate().toString()
                )
            )

            totalUserMade++



            recentRecycler()

            Source.toast(
                this@AddUsers,
                "User created with username : $username & userid : $userid "
            )

            binding.username.setText("")
            binding.userid.setText("")
            binding.userpasscode.setText("")

            if (!binding.checkAddMultiUsers.isChecked) {
                startActivity(Intent(this@AddUsers, AllUsersMade::class.java))
                finish()
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

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@AddUsers, ProjectView::class.java))
        finishAffinity()

    }

}