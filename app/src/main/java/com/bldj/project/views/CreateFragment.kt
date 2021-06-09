package com.bldj.project.views

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bldj.project.R
import com.google.firebase.database.DatabaseReference
import data.Advert
import data.ConstantValues
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * Фрагмент окна создания объявления.
 */
class CreateFragment : Fragment() {

    private var advertsDbRef: DatabaseReference? = null
    private var usersDbRef: DatabaseReference? = null
    private lateinit var fromET: EditText
    private lateinit var toET: EditText
    private lateinit var priceET: EditText
    private lateinit var placesET: EditText
    private lateinit var notesET: EditText
    private lateinit var timePicker: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create, container, false)

        advertsDbRef =
            ConstantValues.database?.reference?.child(ConstantValues.ADVERTS_DB_REFERENCE)
        usersDbRef =
            ConstantValues.database?.reference?.child(ConstantValues.USER_DB_REFERENCE)
        val publishBtn = view.findViewById<Button>(R.id.create_ad)
        fromET = view.findViewById(R.id.A_point)
        toET = view.findViewById(R.id.B_point)
        priceET = view.findViewById(R.id.edit_price)
        placesET = view.findViewById(R.id.edit_places)
        notesET = view.findViewById(R.id.create_comments)
        timePicker = view.findViewById(R.id.time_picker)

        var pickedTime: String? = null

        timePicker.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker_t, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                pickedTime = SimpleDateFormat("HH:mm").format(cal.time)
                timePicker.text = pickedTime
            }

            TimePickerDialog(
                context,
                timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        publishBtn?.setOnClickListener {
            try {
                if (ConstantValues.user!!.isTraveller) {
                    Toast.makeText(
                        context,
                        "Вы уже находитесь в поездке. Удалите или выйдbте из текущих",
                        Toast.LENGTH_LONG
                    ).show()
                    throw Exception()
                }

                val from = fromET.text.toString()
                val to = toET.text.toString()
                val price = priceET.text.toString().toInt()
                val places = placesET.text.toString().toInt()
                val notes = notesET.text.toString()
                if (from.isBlank() || from.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Адрес отправления не должен быть пустым",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (to.isEmpty() || to.isBlank()) {
                    Toast.makeText(
                        context,
                        "Адрес прибытия не должен быть пустым",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else if (price <= 0) {
                    Toast.makeText(context, "Неправильная цена", Toast.LENGTH_SHORT).show()
                } else if (places < 2 || places > 8) {
                    Toast.makeText(context, "Неправильное количество мест", Toast.LENGTH_SHORT)
                        .show()
                } else if (pickedTime == null) {
                    Toast.makeText(context, "Время не выбрано.", Toast.LENGTH_SHORT)
                        .show()
                } else {

                    val adv = Advert(
                        Date(),
                        pickedTime!!,
                        from,
                        to,
                        price,
                        places,
                        notes, arrayListOf(ConstantValues.user!!), ConstantValues.user!!.id//,
                        //mutableListOf(ConstantValues.user!!) падает на стековерфлоу
                    )
                    //ConstantValues.MY_ADVERT = adv
                   // ConstantValues.user!!.myAdvert = adv
                    ConstantValues.user!!.isTraveller = true
                    runBlocking {
                        coroutineSetMyAdvert(adv, from, to)
                    }
                    val bottomSheet = BottomSheetCreateFragment()
                    bottomSheet.show(parentFragmentManager, "TAG")
                    parentFragmentManager.beginTransaction()
                        .replace((view?.parent as View).id, AdsFragment())
                        .addToBackStack(null).commit()
                }
            } catch (e: Exception) {
            }
        }
        // Inflate the layout for this fragment
        return view
    }

    private suspend fun coroutineSetMyAdvert(adv: Advert, from: String, to: String) =
        coroutineScope {
            launch {
                advertsDbRef!!.child("$from-$to").setValue(adv)
                usersDbRef!!.child(ConstantValues.user!!.email.replace(".", ""))
                    .setValue(ConstantValues.user!!)
            }
        }
}