package com.aican.tlcanalyzer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aican.tlcanalyzer.dataClasses.SignUpModelC
import com.aican.tlcanalyzer.databinding.ActivitySignUpBinding
import com.aican.tlcanalyzer.utils.SharedPrefData
import com.aican.tlcanalyzer.utils.Source
import com.aican.tlcanalyzer.utils.Subscription
import com.aican.tlcanalyzer.utils.UserRoles
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var databaseReference: DatabaseReference
    lateinit var databaseReference2: DatabaseReference
    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        firebaseAuth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference2 =
            FirebaseDatabase.getInstance(SharedPrefData.getInstance(this@SignUpActivity)).reference

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.serviceToken))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this@SignUpActivity, googleSignInOptions)

        binding.signUpBtn.setOnClickListener {
            signUpUser()
        }

        binding.signInBtn.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.googleSignin.setOnClickListener {

            val intent: Intent = googleSignInClient.signInIntent
            // Start activity for result
            startActivityForResult(intent, 100)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check condition
        if (requestCode == 100) {
            // When request code is equal to 100 initialize task
            val signInAccountTask: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            // check condition
            if (signInAccountTask.isSuccessful) {
                // When google sign in successful initialize string
                val s = "Google sign in successful"
                // Display Toast
//                Source.toast(this, s)
                // Initialize sign in account
                try {
                    // Initialize sign in account
                    val googleSignInAccount = signInAccountTask.getResult(ApiException::class.java)
                    // Check condition
                    if (googleSignInAccount != null) {
                        // When sign in account is not equal to null initialize auth credential
                        val authCredential: AuthCredential = GoogleAuthProvider.getCredential(
                            googleSignInAccount.idToken, null
                        )
                        // Check credential
                        firebaseAuth.signInWithCredential(authCredential)
                            .addOnCompleteListener(this) { task ->
                                // Check condition
                                if (task.isSuccessful) {
                                    val today: Calendar = Calendar.getInstance()
                                    today.set(Calendar.HOUR_OF_DAY, 0)

                                    val currentTime = Calendar.getInstance().time
                                    val currentDate = Date()


                                    val dateFormat =
                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                                    val formattedDate: String = dateFormat.format(currentDate)


                                    val calendar = Calendar.getInstance()
                                    val hour = calendar[Calendar.HOUR_OF_DAY]
                                    val minute = calendar[Calendar.MINUTE]
                                    val second = calendar[Calendar.SECOND]

                                    val formattedTime =
                                        String.format("%02d:%02d:%02d", hour, minute, second)


                                    val signUpModel = SignUpModelC(
                                        firebaseAuth.currentUser?.displayName.toString(),
                                        firebaseAuth.currentUser?.email.toString(),
                                        firebaseAuth.currentUser?.uid.toString(),
                                        today.time.toString(),
                                        System.currentTimeMillis().toString(),
                                        formattedDate,
                                        formattedDate,
                                        formattedDate,
                                        formattedDate,
                                        formattedTime,
                                        10,
                                        10, "null"
                                    )
                                    databaseReference.child("Users")
                                        .child(firebaseAuth.currentUser?.uid.toString())
                                        .setValue(signUpModel)
                                        .addOnSuccessListener {

                                            databaseReference2.child("Users")
                                                .child(firebaseAuth.currentUser?.uid.toString())
                                                .setValue(signUpModel)
                                                .addOnSuccessListener {

                                                }
                                                .addOnFailureListener {

                                                }

                                            Toast.makeText(
                                                this,
                                                "Successfully Singed Up",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            val user = firebaseAuth.currentUser

                                            SharedPrefData.saveData(
                                                this@SignUpActivity,
                                                SharedPrefData.PR_ACTUAL_LIMIT_KEY, "0"
                                            )

                                            SharedPrefData.saveData(
                                                this@SignUpActivity,
                                                Subscription.LAST_LAUNCH_DATE_KEY,
                                                System.currentTimeMillis().toString()
                                            )

                                            UserRoles.UID =
                                                firebaseAuth.currentUser?.uid.toString()


                                            startActivity(
                                                Intent(
                                                    applicationContext,
                                                    ProjectView::class.java
                                                )
                                            )
                                            finishAffinity()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                this,
                                                "Failed to singed Up",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        }

//                                    // When task is successful redirect to profile activity
//                                    startActivity(
//                                        Intent(
//                                            this@LoginActivity,
//                                            ProjectView::class.java
//                                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                    )
//                                    Source.toast(this, "Firebase authentication successful")
                                    // Display Toast

                                } else {
                                    Source.toast(
                                        this,
                                        "Authentication Failed :" + task.exception?.message
                                    )

                                }
                            }
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            } else {
                Source.toast(this, "Failed to signup")
            }
        }
    }


    private fun signUpUser() {
        val name = binding.etNameS.text.toString()
        val email = binding.etEmailS.text.toString()
        val password = binding.etPasswordS.text.toString()

        if (email!!.isBlank() || password!!.isBlank() || name!!.isBlank()) {
            if (email!!.isBlank()) {
                binding.etEmailS.error = "Enter your email"
            }
            if (password!!.isBlank()) {
                binding.etPasswordS.error = "Enter your password"
            }
            if (name!!.isBlank()) {
                binding.etNameS.error = "Enter your name"
            }
        } else {
            if (email!!.contains("@")) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            val today: Calendar = Calendar.getInstance()
                            today.set(Calendar.HOUR_OF_DAY, 0)

                            val currentTime = Calendar.getInstance().time
                            val currentDate = Date()

                            val dateFormat =
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                            val formattedDate: String = dateFormat.format(currentDate)


                            val calendar = Calendar.getInstance()
                            val hour = calendar[Calendar.HOUR_OF_DAY]
                            val minute = calendar[Calendar.MINUTE]
                            val second = calendar[Calendar.SECOND]

                            val formattedTime =
                                String.format("%02d:%02d:%02d", hour, minute, second)

                            SharedPrefData.saveData(
                                this@SignUpActivity,
                                SharedPrefData.PR_ACTUAL_LIMIT_KEY, "0"
                            )

                            val uid = firebaseAuth.currentUser!!.uid
                            val signUpModel = SignUpModelC(
                                name,
                                email,
                                uid,
                                today.time.toString(),
                                System.currentTimeMillis().toString(),
                                formattedDate,
                                formattedDate,
                                formattedDate,
                                formattedDate,
                                formattedTime,
                                10,
                                10,
                                password
                            )

                            databaseReference.child("Users")
                                .child(firebaseAuth.currentUser?.uid.toString())
                                .addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            SharedPrefData.saveData(
                                                this@SignUpActivity,
                                                SharedPrefData.PR_ACTUAL_LIMIT_KEY, "0"
                                            )

                                            UserRoles.UID =
                                                firebaseAuth.currentUser?.uid.toString()


                                            startActivity(
                                                Intent(
                                                    applicationContext,
                                                    ProjectView::class.java
                                                )
                                            )
                                            finishAffinity()
                                        } else {
                                            databaseReference.child("Users").child(uid)
                                                .setValue(signUpModel)
                                                .addOnSuccessListener {

                                                    databaseReference2.child("Users").child(uid)
                                                        .setValue(signUpModel)
                                                        .addOnSuccessListener {

                                                        }
                                                        .addOnFailureListener {

                                                        }


                                                    Toast.makeText(
                                                        this@SignUpActivity,
                                                        "Successfully Singed Up",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                    val user = firebaseAuth.currentUser

                                                    UserRoles.UID =
                                                        firebaseAuth.currentUser?.uid.toString()


                                                    startActivity(
                                                        Intent(
                                                            applicationContext,
                                                            AllUsersMade::class.java
                                                        ).putExtra("from", "SignUp")
                                                    )


                                                    finishAffinity()
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(
                                                        this@SignUpActivity,
                                                        "Failed to singed Up",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                })


//                            finish()
                        } else {
                            Toast.makeText(this, "Singed Up Failed!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this@SignUpActivity, "Email is not valid", Toast.LENGTH_SHORT).show()
            }
        }


    }

}