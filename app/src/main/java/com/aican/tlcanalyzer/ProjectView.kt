package com.aican.tlcanalyzer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.aican.tlcanalyzer.adapterClasses.OffProjectAdapter
import com.aican.tlcanalyzer.dataClasses.ProjectOfflineData
import com.aican.tlcanalyzer.dataClasses.userIDPASS.UserData
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.database.UsersDatabase
import com.aican.tlcanalyzer.databinding.ActivityProjectViewBinding
import com.aican.tlcanalyzer.dialog.AuthDialog
import com.aican.tlcanalyzer.interfaces.refreshProjectArrayList
import com.aican.tlcanalyzer.utils.SharedPrefData
import com.aican.tlcanalyzer.utils.Source
import com.aican.tlcanalyzer.utils.Subscription
import com.aican.tlcanalyzer.utils.UserRoles
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.Locale


class ProjectView : AppCompatActivity(), refreshProjectArrayList, AuthDialog.AuthCallback {

    lateinit var databaseHelper: DatabaseHelper
    lateinit var userDatabase: UsersDatabase
    lateinit var binding: ActivityProjectViewBinding
    lateinit var arrayList: ArrayList<ProjectOfflineData>
    lateinit var adapter: OffProjectAdapter
    lateinit var splitId: String
    lateinit var roiTableID: String
    lateinit var splitProjectImgID: String
    lateinit var id: String
    lateinit var tableName: String
    lateinit var projectNames: String
    lateinit var projectDescriptions: String
    var i = false
    lateinit var drawerLayout: DrawerLayout
    lateinit var toolbar: Toolbar
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    val REQUEST_CODE_ASK_PERMISSIONS = 123
    lateinit var databaseReference: DatabaseReference
    lateinit var databaseReference2: DatabaseReference

    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var firebaseDatabase2: FirebaseDatabase
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var uid: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        databaseHelper = DatabaseHelper(this)
        userDatabase = UsersDatabase(this)
        firebaseAuth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference2 =
            FirebaseDatabase.getInstance(SharedPrefData.getInstance(this@ProjectView)).reference

        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase2 =
            FirebaseDatabase.getInstance(SharedPrefData.getInstance(this@ProjectView))

        uid = firebaseAuth.uid.toString()

        UserRoles.UID = uid

        binding.subscriptionLayout.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.floatingActionBtn.visibility = View.VISIBLE

        binding.recyclerView.setPadding(0, 0, 0, 150); // Adjust the padding value as needed
        binding.recyclerView.setClipToPadding(false);



        userDatabase.logUserAction(
            AuthDialog.activeUserName,
            AuthDialog.activeUserRole,
            "Dashboard", "na",
            "na", "na"
        )

//        runOnUiThread {
//        Source.checkInternet(this@ProjectView)

//        }

        if (Build.VERSION.SDK_INT >= 33) {
            val permissions = arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO
            )

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permissions are not granted, request them
                val STORAGE_PERMISSION_REQUEST_CODE = 1000
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    REQUEST_CODE_ASK_PERMISSIONS
                )
            } else {
                // Permissions are already granted, proceed with using external storage
                // Your code for accessing external storage goes here

            }
        } else {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permissions are not granted, request them
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_CODE_ASK_PERMISSIONS

                )
            } else {
                // Permissions are already granted, proceed with using external storage
                // Your code for accessing external storage goes here

            }
        }
        insertUserData()

        insertToArrayList()



        binding.filterAction.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, 123)
        }


        binding.floatingActionBtn.setOnClickListener {

            if (Source.cfrStatus) {
                AuthDialog.authDialog(this, false, false, userDatabase, this)
            } else {
                onAuthenticationSuccess()
            }

        }
        val currentDate1 = Date()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())


        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val second = calendar[Calendar.SECOND]

        val formattedTime = String.format("%02d:%02d:%02d", hour, minute, second)


        val formattedDate: String = dateFormat.format(currentDate1)

        firebaseDatabase.reference.child("Users").child(uid).child("lastAppAccessDate")
            .setValue(formattedDate)
        firebaseDatabase.reference.child("Users").child(uid).child("lastAppAccessTime")
            .setValue(formattedTime)

        firebaseDatabase2.reference.child("Users").child(uid).child("lastAppAccessDate")
            .setValue(formattedDate)
        firebaseDatabase2.reference.child("Users").child(uid).child("lastAppAccessTime")
            .setValue(formattedTime)
        //

        if (SharedPrefData.getSavedData(
                this@ProjectView, Subscription.SUBSCRIPTION_END_DATE_KEY
            ) != null && SharedPrefData.getSavedData(
                this@ProjectView, Subscription.SUBSCRIPTION_END_DATE_KEY
            ) != ""
        ) {
            Subscription.SUBSCRIPTION_END_DATE = SharedPrefData.getSavedData(
                this@ProjectView, Subscription.SUBSCRIPTION_END_DATE_KEY
            )
        }


//        var LAST_ACCESS_DATE: String = System.currentTimeMillis().toString()
        var LAST_ACCESS_DATE: String = System.currentTimeMillis().toString()


        val lastAccessDate =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(LAST_ACCESS_DATE.toLong())


        if (SharedPrefData.getSavedData(
                this@ProjectView, Subscription.LAST_LAUNCH_DATE_KEY
            ) != null && SharedPrefData.getSavedData(
                this@ProjectView,
                Subscription.LAST_LAUNCH_DATE_KEY
            ) != ""
        ) {
            LAST_ACCESS_DATE =
                SharedPrefData.getSavedData(this@ProjectView, Subscription.LAST_LAUNCH_DATE_KEY)

        } else {
            SharedPrefData.saveData(
                this@ProjectView, Subscription.LAST_LAUNCH_DATE_KEY, LAST_ACCESS_DATE
            )
        }


        val CURRENT_DATE = System.currentTimeMillis()

        if (LAST_ACCESS_DATE.toLong() > CURRENT_DATE) {
//            Source.toast(this@ProjectView, "Invalid Date")

            binding.subscriptionLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            binding.floatingActionBtn.visibility = View.GONE

        } else if (LAST_ACCESS_DATE.toLong() <= CURRENT_DATE) {


            val currentDate = Date()

            val dateFormat1 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val currentFormattedDate = dateFormat1.format(currentDate)
            if (lastAccessDate == currentFormattedDate) {
//                Source.toast(this@ProjectView, "Active Plan LAST_ACCESS_DATE = CURRENT_DATE ")

            } else {

                SharedPrefData.saveData(
                    this@ProjectView,
                    Subscription.LAST_LAUNCH_DATE_KEY,
                    System.currentTimeMillis().toString()
                )

            }
            if (currentFormattedDate == Subscription.SUBSCRIPTION_END_DATE) {
                Source.toast(this@ProjectView, "Plan Expired")

                binding.subscriptionLayout.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.floatingActionBtn.visibility = View.GONE


            } else {

//                if ((CURRENT_DATE - LAST_ACCESS_DATE.toLong()) == Subscription.MAX_ALLOWED_DIFFERENCE_MILLIS)

                val lastFormattedDate = SimpleDateFormat(
                    "dd/MM/yyyy", Locale.getDefault()
                ).format(LAST_ACCESS_DATE.toLong())


                val calendar1 = Calendar.getInstance()
                calendar1.time = Date(LAST_ACCESS_DATE.toLong())

                calendar1.add(Calendar.DAY_OF_MONTH, 1)

                val nextDate1 = calendar.time

                val nextDateString1 = dateFormat.format(nextDate1)


                if (nextDateString1 == currentFormattedDate) {
//                    Source.toast(this@ProjectView, "Active Plan")

                } else {

//                    Source.toast(this@ProjectView, "Invalid Date")



                    binding.subscriptionLayout.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.floatingActionBtn.visibility = View.GONE

                }


            }
        }




        setTime()

        setAdminLoginAndPassword()

        if (SharedPrefData.getSavedData(
                this,
                Subscription.adminIDKey
            ) != null && SharedPrefData.getSavedData(this, Subscription.adminPassKey) != null
        ) {
            Subscription.adminID = SharedPrefData.getSavedData(this, Subscription.adminIDKey)
            Subscription.adminPass = SharedPrefData.getSavedData(this, Subscription.adminPassKey)
        }

        if (SharedPrefData.getSavedData(this@ProjectView, Source.CFR_KEY) != null) {
            Source.cfrStatus = SharedPrefData.getSavedData(this@ProjectView, Source.CFR_KEY) == "on"
        } else {

            Toast.makeText(
                this@ProjectView,
                "Connect with internet, and sync the online data",
                Toast.LENGTH_SHORT
            ).show()
        }

    }


    private fun setAdminLoginAndPassword() {

        firebaseDatabase.reference.child("Users").child(uid).child("numberOfUsers")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Subscription.numberOfUsers = (snapshot.value as Long).toInt()

                        SharedPrefData.saveData(
                            this@ProjectView,
                            Subscription.numberOfUsersKey,
                            Subscription.numberOfUsers.toString()
                        )

                    } else {
                        firebaseDatabase.reference.child("Users").child(uid).child("numberOfUsers")
                            .setValue(10)

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


        firebaseDatabase.reference.child("Users").child(uid).child("email")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Subscription.adminID = snapshot.value as String

                        SharedPrefData.saveData(
                            this@ProjectView, Subscription.adminIDKey, Subscription.adminID
                        )

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        firebaseDatabase.reference.child("Users").child(uid).child("pcode")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Subscription.adminPass = snapshot.value as String

                        SharedPrefData.saveData(
                            this@ProjectView, Subscription.adminPassKey, Subscription.adminPass
                        )

                    } else {
                        firebaseDatabase.reference.child("Users").child(uid)
                            .child("pcode").setValue("demo123pass")

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }


    private fun getDate(time: Long): String? {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time * 1000
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        return dateFormat.format(cal.time)
    }

    fun setTime() {
        val offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")
        offsetRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offset = snapshot.getValue(Double::class.java)!!!!


                val estimatedServerTimeMs = System.currentTimeMillis() + offset

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                Subscription.TODAY_DATE_FROM_INTERNET = dateFormat.format(estimatedServerTimeMs)
//                Source.toast(this@ProjectView, dateFormat.format(estimatedServerTimeMs))

            }

            override fun onCancelled(error: DatabaseError) {
                System.err.println("Listener was cancelled")
            }
        })
    }

    fun checkSubscription() {
        firebaseDatabase.reference.child("Users").child(uid).child("isActive")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Subscription.isActive = snapshot.value as Boolean
                        if (Subscription.isActive) {

                            if (Subscription.SUBSCRIPTION_END_DATE == Subscription.TODAY_DATE_FROM_INTERNET) {
                                binding.subscriptionLayout.visibility = View.VISIBLE
                                binding.recyclerView.visibility = View.GONE
                                binding.floatingActionBtn.visibility = View.GONE
                            } else {
                                binding.subscriptionLayout.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE
                                binding.floatingActionBtn.visibility = View.VISIBLE
                            }
//                            Source.toast(this@ProjectView, "Subscribed")
                        } else {
//                            Source.toast(this@ProjectView, "Not Subscribed")
//                            val dialogView = layoutInflater.inflate(R.layout.not_subscribed, null)
//                            val builder = android.app.AlertDialog.Builder(this@ProjectView)
//                                .setView(dialogView)
//
//
//                            val alertDialog = builder.create()
//                            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//                            alertDialog.setCancelable(false)
//
//                            try {
//                                alertDialog.show()
//
//                            } catch (e: WindowManager.BadTokenException) {
//                                Log.e("Error dialog", e.message.toString())
//                            }

                            binding.subscriptionLayout.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.GONE
                            binding.floatingActionBtn.visibility = View.GONE

                        }
                    } else {
                        firebaseDatabase.reference.child("Users").child(uid).child("isActive")
                            .setValue(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Source.toast(this@ProjectView, "Error : " + error.message)
                }

            })
        if (SharedPrefData.getSavedData(
                this@ProjectView,
                SharedPrefData.PR_LIMIT_KEY
            ) != null && SharedPrefData.getSavedData(
                this@ProjectView,
                SharedPrefData.PR_LIMIT_KEY
            ) != ""
        ) {
            Subscription.PROJECT_LIMIT =
                SharedPrefData.getSavedData(this@ProjectView, SharedPrefData.PR_LIMIT_KEY).toInt()
        }
        firebaseDatabase.reference.child("Users").child(uid).child("projectLimit")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Subscription.PROJECT_LIMIT = (snapshot.value as Long).toInt()
                        SharedPrefData.saveData(
                            this@ProjectView, SharedPrefData.PR_LIMIT_KEY, snapshot.value.toString()
                        )
                    } else {
                        firebaseDatabase.reference.child("Users").child(uid).child("projectLimit")
                            .setValue(10)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        firebaseDatabase.reference.child("Users").child(uid).child("subscriptionEndDate")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Subscription.SUBSCRIPTION_END_DATE = snapshot.value as String
                        SharedPrefData.saveData(
                            this@ProjectView,
                            Subscription.SUBSCRIPTION_END_DATE_KEY,
                            snapshot.value.toString()
                        )
                    } else {
                        firebaseDatabase.reference.child("Users").child(uid)
                            .child("subscriptionEndDate").setValue("26/08/2023")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        firebaseDatabase.reference.child("Users").child(uid).child("name")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Subscription.userName = snapshot.value as String

                    SharedPrefData.saveData(
                        this@ProjectView, Subscription.userNameKey, Subscription.userName
                    )

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        firebaseDatabase.reference.child("Users").child(uid).child("cfr")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.value as String
                        SharedPrefData.saveData(
                            this@ProjectView, Source.CFR_KEY, status
                        )
                        if (SharedPrefData.getSavedData(this@ProjectView, Source.CFR_KEY) != null) {
                            Source.cfrStatus =
                                SharedPrefData.getSavedData(
                                    this@ProjectView,
                                    Source.CFR_KEY
                                ) == "on"
                        }
                    } else {
                        firebaseDatabase.reference.child("Users").child(uid).child("cfr")
                            .setValue("off")
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        firebaseDatabase.reference.child("Users").child(uid).child("email")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Subscription.email = snapshot.value as String

                    SharedPrefData.saveData(
                        this@ProjectView, Subscription.emailKey, Subscription.email
                    )

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Source.CAMERA_REQUEST_SPLIT && resultCode == Activity.RESULT_OK) {
            val intent = Intent(this@ProjectView, SplitImage::class.java)
            intent.putExtra(resources.getString(R.string.isStartedForResultKey), false)
            intent.putExtra("p", "pixel")
            intent.putExtra("w", "new")
            intent.putExtra("projectName", projectNames)
            intent.putExtra("projectDescription", projectDescriptions)
            intent.putExtra("projectImage", splitProjectImgID)
            intent.putExtra("contourImage", "na")
            intent.putExtra("id", id)
            intent.putExtra("splitId", splitId)
            intent.putExtra("imageSplitAvailable", "true")
            intent.putExtra("projectNumber", splitId)
            intent.putExtra("thresholdVal", "0")
            intent.putExtra("numberOfSpots", "0")
            intent.putExtra("tableName", tableName)
            startActivity(intent)

        }
        if (requestCode == Source.CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
//            val intent = Intent(this@ProjectView, ImageAnalysis::class.java)
            val intent = Intent(this@ProjectView, NewImageAnalysis::class.java)
            intent.putExtra(resources.getString(R.string.isStartedForResultKey), false)
            intent.putExtra("p", "pixel")
            intent.putExtra("w", "new")
            intent.putExtra("mtype", "single")
            intent.putExtra("projectName", projectNames)
            intent.putExtra("projectDescription", projectDescriptions)
            intent.putExtra("projectImage", "na")
            intent.putExtra("contourImage", "na")
            intent.putExtra("id", id)
            intent.putExtra("splitId", splitId)
            intent.putExtra("imageSplitAvailable", "false")
            intent.putExtra("projectNumber", splitId)
            intent.putExtra("thresholdVal", "0")
            intent.putExtra("numberOfSpots", "0")
            intent.putExtra("tableName", tableName)
            startActivity(intent)
        }
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
//            Source.toast(this@ProjectView, "Captured")
        }
    }

    override fun onResume() {
        super.onResume()

        uid = firebaseAuth.uid.toString()

        UserRoles.UID = uid

        AuthDialog.projectType = "na"

        Source.splitContourDataList = ArrayList()
        if (SharedPrefData.getSavedData(
                this@ProjectView, SharedPrefData.PR_ACTUAL_LIMIT_KEY
            ) != null && SharedPrefData.getSavedData(
                this@ProjectView,
                SharedPrefData.PR_ACTUAL_LIMIT_KEY
            ) != ""
        ) {
            Subscription.NO_OF_PROJECTS_MADE =
                SharedPrefData.getSavedData(this@ProjectView, SharedPrefData.PR_ACTUAL_LIMIT_KEY)
                    .toInt()
        } else {
            Subscription.NO_OF_PROJECTS_MADE = 0
        }

        if (SharedPrefData.getSavedData(
                this@ProjectView, Subscription.numberOfUsersKey
            ) != null && SharedPrefData.getSavedData(
                this@ProjectView,
                Subscription.numberOfUsersKey
            ) != ""
        ) {
            Subscription.numberOfUsers =
                SharedPrefData.getSavedData(this@ProjectView, Subscription.numberOfUsersKey)
                    .toInt()
        } else {
            Subscription.numberOfUsers = 10
        }

        SplitImage.completed = false


        insertToArrayList()

        insertUserData()
        checkSubscription()
        myDrawerLayout()


    }

    private fun insertUserData() {

        val cursor: Cursor = userDatabase.get_data()
        if (cursor.count == 0) {
//            Toast.makeText(this@ProjectView, "No entry", Toast.LENGTH_SHORT).show()
        }

        val userDataArrayList: ArrayList<UserData> = ArrayList()

        Source.userDataArrayList = ArrayList()

        if (cursor.moveToFirst()) {
            do {
                userDataArrayList.add(
                    UserData(
                        cursor.getString(0).toString(),
                        cursor.getString(1).toString(),
                        cursor.getString(2).toString(),
                        cursor.getString(3).toString(),
                        cursor.getString(4).toString(),
                        cursor.getString(5).toString()
                    )
                )

                Source.userDataArrayList = userDataArrayList

            } while (cursor.moveToNext())
        }


    }

    private fun insertToArrayList() {
        arrayList = ArrayList()

        val cursor: Cursor = databaseHelper.datas

        if (cursor.moveToFirst()) {
            do {
                val note = ProjectOfflineData(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getString(11),
                    cursor.getString(12),
                    cursor.getString(13),
                    cursor.getString(14),
                    cursor.getString(15),
                    cursor.getString(16),
                )
                arrayList.add(note)
            } while (cursor.moveToNext())
        }


        adapter = OffProjectAdapter(this, arrayList, databaseHelper, this)
        binding.recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.filterAction.setOnClickListener {
            if (!arrayList.isEmpty()) {

                val dialogView = layoutInflater.inflate(R.layout.sortby_lay, null)
                val builder = android.app.AlertDialog.Builder(this@ProjectView).setView(dialogView)


                val alertDialog = builder.create()

                val sortbyAlpha = dialogView.findViewById<RadioButton>(R.id.sortbyAlpha)
                val sortbyDate = dialogView.findViewById<RadioButton>(R.id.sortbyDate)
                val applySort = dialogView.findViewById<MaterialButton>(R.id.applySort)
                sortbyAlpha.isChecked = true
                sortbyAlpha.setOnClickListener {
                    sortbyDate.isChecked = !sortbyAlpha.isChecked
                }

                sortbyDate.setOnClickListener {
                    sortbyAlpha.isChecked = !sortbyDate.isChecked
                }
                applySort.setOnClickListener {
                    if (sortbyDate.isChecked) {

                        Collections.sort(arrayList,
                            Comparator<ProjectOfflineData> { o1, o2 -> o1.timeStamp.compareTo(o2.timeStamp) })
                        adapter = OffProjectAdapter(this, arrayList, databaseHelper, this)
                        binding.recyclerView.adapter = adapter
                        adapter.notifyDataSetChanged()
                        alertDialog.dismiss()
                    }
                    if (sortbyAlpha.isChecked) {
                        Collections.sort(arrayList,
                            Comparator<ProjectOfflineData> { o1, o2 -> o1.projectName.compareTo(o2.projectName) })
                        adapter = OffProjectAdapter(this, arrayList, databaseHelper, this)
                        binding.recyclerView.adapter = adapter
                        adapter.notifyDataSetChanged()
                        alertDialog.dismiss()
                    }
                }

                alertDialog.show()


            }
        }

    }

    private fun myDrawerLayout() {
        drawerLayout = binding.drawerLayout
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        val navView = binding.navigation as NavigationView

        val headerView: View = navView.getHeaderView(0)

        val navUsername: TextView = headerView.findViewById<View>(R.id.planExpiryDate) as TextView
        val Uid: TextView = headerView.findViewById<View>(R.id.Uid) as TextView
        val userName: TextView = headerView.findViewById<View>(R.id.userName) as TextView
        val emailID: TextView = headerView.findViewById<View>(R.id.emailId) as TextView
        val projectMade: TextView = headerView.findViewById<View>(R.id.projectMade) as TextView
        val more: TextView = headerView.findViewById<View>(R.id.more) as TextView


        Uid.setText("UID : " + UserRoles.UID.toString())


        if (SharedPrefData.getSavedData(
                this@ProjectView,
                Subscription.emailKey
            ) != null && SharedPrefData.getSavedData(
                this@ProjectView, Subscription.emailKey
            ) != ""
        ) {
            Subscription.email =
                SharedPrefData.getSavedData(this@ProjectView, Subscription.emailKey)
            emailID.text = "Email : " + Subscription.email
        } else {
            emailID.text = "Email : " + "N/A"

        }

        if (SharedPrefData.getSavedData(
                this@ProjectView,
                Subscription.userNameKey
            ) != null && SharedPrefData.getSavedData(
                this@ProjectView, Subscription.userNameKey
            ) != ""
        ) {
            Subscription.userName =
                SharedPrefData.getSavedData(this@ProjectView, Subscription.userNameKey)
            userName.text = "Username : " + Subscription.userName
        } else {
            userName.text = "Username : " + "N/A"

        }

        if (SharedPrefData.getSavedData(
                this@ProjectView,
                Subscription.SUBSCRIPTION_END_DATE_KEY
            ) != null && SharedPrefData.getSavedData(
                this@ProjectView, Subscription.SUBSCRIPTION_END_DATE_KEY
            ) != ""
        ) {
            val subEndDate = SharedPrefData.getSavedData(
                this@ProjectView, Subscription.SUBSCRIPTION_END_DATE_KEY
            )

            navUsername.setText("Plan Expiry Date : " + subEndDate.toString())
        } else {
            navUsername.setText("Plan Expiry Date : Not Set")

        }

        if ((SharedPrefData.getSavedData(
                this@ProjectView,
                SharedPrefData.PR_ACTUAL_LIMIT_KEY
            ) != null && SharedPrefData.getSavedData(
                this@ProjectView, SharedPrefData.PR_ACTUAL_LIMIT_KEY
            ) != ""
                    ) && (SharedPrefData.getSavedData(
                this@ProjectView,
                SharedPrefData.PR_LIMIT_KEY
            ) != null && SharedPrefData.getSavedData(
                this@ProjectView, SharedPrefData.PR_LIMIT_KEY
            ) != ""
                    )
        ) {
            val prMade = SharedPrefData.getSavedData(
                this@ProjectView, SharedPrefData.PR_ACTUAL_LIMIT_KEY
            )

            val prLimit = SharedPrefData.getSavedData(
                this@ProjectView, SharedPrefData.PR_LIMIT_KEY
            )

            projectMade.setText("Project Made : " + prMade.toString() + " / " + prLimit.toString())
        } else {
            projectMade.setText("Project Made : N/A")

        }

        more.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.account_info_dialog, null)
            val builder = AlertDialog.Builder(this).setView(dialogView)


            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


            val navUsernameD: TextView =
                dialogView.findViewById<View>(R.id.planExpiryDate) as TextView
            val UidD: TextView = dialogView.findViewById<View>(R.id.Uid) as TextView
            val userNameD: TextView = dialogView.findViewById<View>(R.id.userName) as TextView
            val emailIDD: TextView = dialogView.findViewById<View>(R.id.emailId) as TextView
            val projectMadeD: TextView = dialogView.findViewById<View>(R.id.projectMade) as TextView


            navUsernameD.setText(navUsername.text.toString())
            UidD.setText(Uid.text.toString())
            userNameD.setText(userName.text.toString())
            emailIDD.setText(emailID.text.toString())
            projectMadeD.setText(projectMade.text.toString())






            alertDialog.show()

        }


        navView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { menuItem ->
            val itemId = menuItem.itemId
            when (itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this@ProjectView, "Home", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawers()
                    return@OnNavigationItemSelectedListener true
                }

                R.id.user_management -> {
//                    Toast.makeText(this@ProjectView, "Manage Your Users", Toast.LENGTH_SHORT)
//                        .show()
                    drawerLayout.closeDrawers()
                    val intent = Intent(applicationContext, AdminLogin::class.java)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.audit_trail -> {
//                    Toast.makeText(this@ProjectView, "Manage Your Users", Toast.LENGTH_SHORT)
//                        .show()
                    drawerLayout.closeDrawers()
                    val intent = Intent(applicationContext, AuditTrailAct::class.java)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }


                R.id.all_files -> {
                    Toast.makeText(this@ProjectView, "All Exported Files", Toast.LENGTH_SHORT)
                        .show()
                    drawerLayout.closeDrawers()
                    val intent = Intent(applicationContext, AllExportedFiles::class.java)
                    startActivity(intent)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.log_out -> {
                    Toast.makeText(this@ProjectView, "See you soon", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                    return@OnNavigationItemSelectedListener true
                }

                else -> {
                    Toast.makeText(this@ProjectView, "NA", Toast.LENGTH_SHORT)
                    false
                }
            }
        })

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.hamburger.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context, permission!!
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    override fun refreshProjects() {
        insertToArrayList()
    }

    override fun onAuthenticationSuccess() {


        Source.retake = true
        if (SharedPrefData.getSavedData(
                this@ProjectView, SharedPrefData.PR_ACTUAL_LIMIT_KEY
            ) != null && SharedPrefData.getSavedData(
                this@ProjectView, SharedPrefData.PR_ACTUAL_LIMIT_KEY
            ) != ""
        ) {
            Subscription.NO_OF_PROJECTS_MADE = SharedPrefData.getSavedData(
                this@ProjectView, SharedPrefData.PR_ACTUAL_LIMIT_KEY
            ).toInt()
        } else {
            Subscription.NO_OF_PROJECTS_MADE = 0
        }
        if (Subscription.NO_OF_PROJECTS_MADE >= Subscription.PROJECT_LIMIT) {
            Source.toast(
                this@ProjectView, "You have exceeded the maximum limit of project creation"
            )
        } else {

            val dialogView = layoutInflater.inflate(R.layout.name_description, null)
            val builder = AlertDialog.Builder(this).setView(dialogView)


            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val projectNameD = dialogView.findViewById<EditText>(R.id.projectNameD)
            val isSplit = dialogView.findViewById<MaterialCheckBox>(R.id.isSplit)
            val projectDescriptionD = dialogView.findViewById<EditText>(R.id.projectDescriptionD)

            projectNameD.setText("Project " + (arrayList.size + 1))

            dialogView.findViewById<MaterialButton>(R.id.submitBtnD).setOnClickListener {
                roiTableID = "ROI_ID_" + System.currentTimeMillis().toString()
                splitId = "AICAN" + System.currentTimeMillis().toString();
                splitProjectImgID = "IMG$splitId.jpg"
                id = "ID" + System.currentTimeMillis()
                tableName = "TBL" + System.currentTimeMillis()
                projectNames = projectNameD.text.toString()
// Get the current time
                val currentTimeMillis = System.currentTimeMillis()

                val currentDate = Date(currentTimeMillis)

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                val formattedDate: String? = sdf.format(currentDate)

                projectDescriptions = projectDescriptionD.text.toString()

                val volumePlotTableID = "VOL_" + System.currentTimeMillis()
                val intensityPlotTableID = "INT_" + System.currentTimeMillis()
                val plotTableID = "TAB_" + System.currentTimeMillis()


                userDatabase.logUserAction(
                    AuthDialog.activeUserName,
                    AuthDialog.activeUserRole,
                    "Started project creating",
                    projectNames,
                    id,
                    AuthDialog.projectType
                )

                //

                val intent = Intent(
                    this@ProjectView, com.aican.tlcanalyzer.CameraActivity::class.java
                )
                intent.putExtra(
                    resources.getString(R.string.isStartedForResultKey), false
                )
                intent.putExtra("p", "pixel")
                intent.putExtra("w", "new")
                intent.putExtra("projectName", projectNames)
                intent.putExtra("projectDescription", projectDescriptions)
                intent.putExtra("timeStamp", formattedDate.toString())
                intent.putExtra("projectImage", splitProjectImgID)
                intent.putExtra("contourImage", "na")
                intent.putExtra("id", id)
                intent.putExtra("splitId", splitId)
                intent.putExtra("imageSplitAvailable", "NA")
                intent.putExtra("projectNumber", splitId)
                intent.putExtra("thresholdVal", "0")
                intent.putExtra("numberOfSpots", "0")
                intent.putExtra("tableName", tableName)
                intent.putExtra("roiTableID", roiTableID)
                intent.putExtra(
                    "volumePlotTableID", volumePlotTableID
                )
                intent.putExtra(
                    "intensityPlotTableID", intensityPlotTableID
                )
                intent.putExtra(
                    "plotTableID", plotTableID
                )
                startActivity(intent)

                insertToArrayList()
//                adapter.notify(arrayList.size)
//                Source.toast(this, i.toString())
                alertDialog.dismiss()
            }

            alertDialog.show()
        }
    }

    private var permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        var isGranted = true
        for (items in it) {
            if (!items.value) {
                isGranted = false
            }
        }
        if (isGranted) {
            Toast.makeText(this@ProjectView, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@ProjectView, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    fun readPermissionAndroid13() {
        var permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            Toast.makeText(this, "Yes", Toast.LENGTH_SHORT).show()
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        }

        if (!hasPermission(permissions[0])) {
            permissionLauncher.launch(permissions)
        }
    }

    fun hasPermission(permission: String): Boolean {

        return ContextCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

}