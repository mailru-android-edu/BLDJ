package com.bldj.project.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bldj.project.R
import com.bldj.project.databinding.LoginLayoutBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import data.ConstantValues
import data.User

class LoginFragment : Fragment() {

    private var usersDbRef: DatabaseReference? = null
    private lateinit var loginLayoutBinding: LoginLayoutBinding

    lateinit var inflaterThis: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ConstantValues.auth = FirebaseAuth.getInstance()
        ConstantValues.database = FirebaseDatabase.getInstance()
        usersDbRef = ConstantValues.database?.reference?.child(ConstantValues.USER_DB_REFERENCE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loginLayoutBinding = LoginLayoutBinding.inflate(inflater, container, false)

        val navigationBar = activity?.findViewById<BottomNavigationView>(R.id.nav_bar)
        navigationBar!!.visibility = View.GONE
        loginLayoutBinding.goBttn.setOnClickListener {
            val password = loginLayoutBinding.passwordEdit.text.toString()
            val login = loginLayoutBinding.loginEdit.text.toString()
            if (password.isNotBlank() && login.isNotBlank())
                onLogin()
            else{
                Toast.makeText(
                    this.context, "Введите данные!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return loginLayoutBinding.root
    }

    private fun onLogin(/*loginET: EditText*/) {

        val password = loginLayoutBinding.passwordEdit.text.toString()
        val login = loginLayoutBinding.loginEdit.text.toString()
        // Log.i("LOGINPAROL", login)
//        val actionCodeSettings = actionCodeSettings {
//            // URL you want to redirect back to. The domain (www.example.com) for this
//            // URL must be whitelisted in the Firebase Console.
//            url = "https://www.example.com/finishSignUp?cartId=1234"
//            // This must be true
//            handleCodeInApp = true
//            iosBundleId = "com.example.ios"
//            setAndroidPackageName(
//                "com.bldj.project",
//                true, /* installIfNotAvailable */
//                "21" /* minimumVersion */)
//        }
//        ConstantValues.auth?.sendSignInLinkToEmail(login, actionCodeSettings)
//            ?.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Log.d("send_Email", "Email sent.")
//                }
//            }
        //ConstantValues.auth?.sendSignInLinkToEmail(login)
        ConstantValues.auth?.createUserWithEmailAndPassword(login, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (login.endsWith("hse.ru") || login.endsWith("edu.hse.ru")) {
                        Log.d("auth", "createUserWithEmail:success")
                        var to = 0;
                        for (i in login.indices) {
                            if (login[i] == '@')
                                to = i
                        }
                        val name = login.substring(0, to)
                        val user = User(login, name, "-")
                        user.id = ConstantValues.auth?.currentUser!!.uid
                        ConstantValues.user = user
                        ConstantValues.alreadyCreated = false
                        usersDbRef!!.child(login.replace(".", "")).setValue(user)
                        parentFragmentManager.beginTransaction()
                            .replace(
                                (view?.parent as View).id,
                                AccessCodeFragment(),
                                "LoginSuccess"
                            )
                            .commit()
                    } else {
                        Toast.makeText(
                            this.context, "Неверный домен почты",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                } else {
                    Log.i("authq", "createUserWithEmail:failure", task.exception)
                    ConstantValues.auth?.signInWithEmailAndPassword(login, password)
                        ?.addOnCompleteListener { taskAnother ->
                            if (taskAnother.isSuccessful) {
                                if (login.endsWith("hse.ru") || login.endsWith("edu.hse.ru")) {
                                    var to = 0;
                                    for (i in login.indices) {
                                        if (login[i] == '@')
                                            to = i
                                    }
                                    val name = login.substring(0, to)
                                    val user = User(login, name, "-")
                                    user.id = ConstantValues.auth?.currentUser!!.uid
                                    ConstantValues.user = user
                                    ConstantValues.alreadyCreated = true
                                    parentFragmentManager.beginTransaction()
                                        .replace(
                                            (view?.parent as View).id,
                                            AccessCodeFragment(),
                                            "LoginSuccess"
                                        )
                                        .commit()
                                } else {
                                    Toast.makeText(
                                        this.context, "Неверный домен почты",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            } else {
                                Toast.makeText(
                                    this.context, "Неверный пароль",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
    }
}