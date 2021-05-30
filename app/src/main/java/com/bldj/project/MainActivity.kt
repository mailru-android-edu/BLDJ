package com.bldj.project

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import data.Advert
import data.IBackButton
import java.security.InvalidParameterException

class MainActivity : AppCompatActivity(), IBeTraveller {

    private var usersDbRef: DatabaseReference? = null
    private var usersChildEventListener: ChildEventListener? = null
    private var oldId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        val appSettingPrefs: SharedPreferences = getSharedPreferences("AppSettingPrefs", 0)
        val isNightModeOn: Boolean = appSettingPrefs.getBoolean("NightMode", false)
        if (isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }



        ConstantValues.auth = Firebase.auth
        if (ConstantValues.auth?.currentUser != null) {
            ConstantValues.database = FirebaseDatabase.getInstance()
            usersDbRef = ConstantValues.database?.reference?.child(ConstantValues.USER_DB_REFERENCE)
            usersChildEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            }
            usersDbRef?.addChildEventListener(usersChildEventListener as ChildEventListener)
            moveToFragment(TripsFragment())
        } else {

            moveToFragment(LoginFragment())
        }
//        Log.i("Authmail", auth?.currentUser?.email.toString())
//        if (auth?.currentUser != null) {
//            if (savedInstanceState == null)
//                moveToFragment(AdsFragment())
//        } else {
//            if (savedInstanceState == null)
//                moveToFragment(LoginFragment())
//        }

//        if (savedInstanceState == null)
//            moveToFragment(AdsFragment())

        val navigationBar = findViewById<BottomNavigationView>(R.id.nav_bar)
        //Set the click listener on nav bar.
        navigationBar.setOnNavigationItemSelectedListener {
            val frag = when (it.itemId) {
                R.id.home -> TripsFragment()
                R.id.adverts -> AdsFragment()
                R.id.add -> CreateFragment()
                R.id.user -> ProfileFragment()
                else -> throw InvalidParameterException()
            }
            if (oldId != -1 || oldId != it.itemId) {
                moveToFragment(frag)
                oldId = it.itemId
            }
            true
        }
    }

    /**
     * Begins transaction to a selected on a navigation bar fragment.
     */
    private fun moveToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_wrapper, fragment)
            .commit()
    }

    override fun onBackPressed() {
        val fragment = this.supportFragmentManager.findFragmentById(R.id.activity_main_id)
        (fragment as? IBackButton)?.onBackPressed()?.not()?.let {
            super.onBackPressed()
        }
    }

    override fun onBeTravellerClicked(ad: Advert) {
        Log.i("onBeTravellerClicked", ad.from)
        //Checking if user is already a traveler of the advert or other advert.
        if (ad.users.contains(ConstantValues.user) || !ConstantValues.user.isTraveller) {
            Toast.makeText(this, "Вы уже попутчик", Toast.LENGTH_LONG).show()
        } else {
            ad.users.add(ConstantValues.user)
            val advRef = ConstantValues.database?.reference?.child("adverts")
            advRef?.child("${ad.from}-${ad.to}")?.child("users")?.setValue(ad.users)
            Toast.makeText(this, "Поздравляю! Вы теперь попутчик.", Toast.LENGTH_LONG).show()
        }
    }
}