package com.moon.convenience_store.tab_fragment

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.moon.convenience_store.MainActivity
import com.moon.convenience_store.R
import com.moon.convenience_store.dto.ContactsDto
import com.moon.convenience_store.item.ItemActivity


class BottomFragment1 : Fragment() {
    private lateinit var MainActivity: MainActivity
    val contactsList = mutableListOf<ContactsDto>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_store, container, false)
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var CVSInfoLayout = view.findViewById<LinearLayout>(R.id.linear_layout_cvs_info_layout)
        var name = view.findViewById<TextView>(R.id.store_name)
        var phoneN = view.findViewById<TextView>(R.id.phone)
        var button = view.findViewById<Button>(R.id.button2)
        var button3 = view.findViewById<Button>(R.id.button3)
        var viewStub = view.findViewById<ViewStub>(R.id.main_stub)
        button3.visibility=View.GONE
        contactsList.add(ContactsDto(name.text.toString(), phoneN.text.toString()))

        CVSInfoLayout.setOnClickListener {
            var intent = Intent(context, ItemActivity::class.java)
            startActivity(intent)
        }
        button.setOnClickListener { //???????????? ???????????? ?????????????????? viewstub ?????????
            checkPermissions()
            viewStub.inflate()
            button3.visibility=View.VISIBLE
        }
        button3.setOnClickListener {
            viewStub.visibility = View.GONE
            button3.visibility=View.GONE
        }

    }

    private val requiredPermissions = arrayOf(
        Manifest.permission.WRITE_CONTACTS
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        MainActivity = context as MainActivity
    }

    private val mPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if(it){
            addContacts(contactsList[0].NAME, contactsList[0].NUMBER)
        }


    }

    // ????????? ?????? ?????? ?????? ??? ?????? ??????
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions() {
        if (context?.checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {//????????? ?????????????????????
            requestPermissions(arrayOf(Manifest.permission.WRITE_CONTACTS), 200)//??????
        } else {//?????? ?????????????????????
            mPermissionResult.launch(Manifest.permission.WRITE_CONTACTS)//?????? mPermissionResult()??? ?????? ???????????? ??????
        }
    }


    private fun addContacts(storeName: String, storeTel: String) {
        val p = ContentValues()
        p.put(ContactsContract.RawContacts.ACCOUNT_TYPE, "com.google")
        p.put(ContactsContract.RawContacts.ACCOUNT_NAME, "moon")
        val rowContact = requireContext().contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, p)
        val rawContactId = ContentUris.parseId(rowContact!!)
        // ???????????? ?????? ??????
        val value = ContentValues()
        value.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
        value.put(ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        value.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, storeName)
        requireContext().contentResolver.insert(ContactsContract.Data.CONTENT_URI, value)
        // ???????????? ???????????? ??????
        val ppv = ContentValues()
        ppv.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
        ppv.put(ContactsContract.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
        ppv.put(ContactsContract.CommonDataKinds.Phone.NUMBER, storeTel)
        ppv.put(ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
        requireContext().contentResolver.insert(ContactsContract.Data.CONTENT_URI, ppv)
        Toast.makeText(requireContext(),"$storeName ???????????? ?????????????????????.",
            Toast.LENGTH_SHORT).show()

    }

}

