package com.aican.tlcanalyzer

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.adapterClasses.UserDatabaseAdapter
import com.aican.tlcanalyzer.dataClasses.userIDPASS.UserData
import com.aican.tlcanalyzer.database.UsersDatabase
import com.aican.tlcanalyzer.databinding.ActivityAllUsersMadeBinding
import com.aican.tlcanalyzer.dialog.AuthDialog
import com.aican.tlcanalyzer.interfaces.refreshProjectArrayList
import com.aican.tlcanalyzer.utils.Subscription

class AllUsersMade : AppCompatActivity(), refreshProjectArrayList {

    lateinit var userDatabaseModelList: ArrayList<UserData>
    lateinit var databaseHelper: UsersDatabase
    lateinit var addUsersbtn: AppCompatButton
    lateinit var usersDatabase: UsersDatabase
    var totalUsersMade: Int = 0

    var from = "na"

    lateinit var binding: ActivityAllUsersMadeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllUsersMadeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        if (intent.getStringExtra("from") != null) {
            from = intent.getStringExtra("from").toString()
        }

        if (from == "na") {
            binding.doitLater.visibility = View.GONE
        } else {
            binding.doitLater.visibility = View.VISIBLE
        }

        binding.doitLater.setOnClickListener {

            startActivity(Intent(this@AllUsersMade, ProjectView::class.java))
            finishAffinity()

        }

        binding.back.setOnClickListener(View.OnClickListener {

            startActivity(Intent(this@AllUsersMade, ProjectView::class.java))
            finishAffinity()


        })

        addUsersbtn = findViewById(R.id.addUsersbtn)

        userDatabaseModelList = ArrayList()
        usersDatabase = UsersDatabase(this)

        usersDatabase.logUserAction(
            "Admin",
            "Admin",
            "Admin Accessed All Users Database",
            "na",
            "na",
            AuthDialog.projectType
        )


        databaseHelper = UsersDatabase(this)
//        printBtn = findViewById(R.id.printBtn)

        val recyclerView = findViewById<RecyclerView>(R.id.user_database_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = UserDatabaseAdapter(this, getList(), this)
        recyclerView.adapter = adapter

        addUsersbtn.setOnClickListener {

//            Toast.makeText(this@AllUsersMade, "" + totalUsersMade, Toast.LENGTH_SHORT).show()

            if (totalUsersMade < Subscription.numberOfUsers) {
                startActivity(
                    Intent(this@AllUsersMade, AddUsers::class.java).putExtra(
                        "userMade",
                        totalUsersMade.toString()
                    )
                )
                finish()
            } else {
                Toast.makeText(
                    this,
                    "You have exceeded the limit of user creation which is : " + Subscription.numberOfUsers,
                    Toast.LENGTH_LONG
                ).show()
            }

        }

    }

    private fun getList(): List<UserData?>? {
        totalUsersMade = 0
        userDatabaseModelList = ArrayList()
        val res: Cursor = databaseHelper.get_data()
        if (res.count == 0) {
            Toast.makeText(this@AllUsersMade, "No entry", Toast.LENGTH_SHORT).show()
        }
        var s = 1
        while (res.moveToNext()) {
            totalUsersMade++
            userDatabaseModelList.add(
                UserData(
                    res.getString(0),
                    res.getString(1),
                    res.getString(2),
                    res.getString(3),
                    res.getString(4),
                    res.getString(5)
                )
            )
        }
        return userDatabaseModelList
    }

    override fun refreshProjects() {
        val recyclerView = findViewById<RecyclerView>(R.id.user_database_recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = UserDatabaseAdapter(this, getList(), this)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        refreshProjects()

        if (totalUsersMade <= 0) {
            binding.noUsersText.visibility = View.VISIBLE
            binding.userDatabaseRecyclerView.visibility = View.GONE
        } else {
            binding.noUsersText.visibility = View.GONE
            binding.userDatabaseRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@AllUsersMade, ProjectView::class.java))
        finishAffinity()

    }

}