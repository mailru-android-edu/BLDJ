package data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Класс констант.
 */
class ConstantValues {
    companion object {
        //Объект FirebaseAuth.
        var auth: FirebaseAuth? = null

        //Объект FirebaseDatabase.
        var database: FirebaseDatabase? = null

        var MY_ADVERT: Advert? = null

        //Объект текущего User.
        var user: User?=null

        const val USER_DB_REFERENCE = "users"
        const val ADVERTS_DB_REFERENCE = "adverts"
        const val HISTORY_DB_REFERENCE = "history"
        var alreadyCreated = false
    }
}