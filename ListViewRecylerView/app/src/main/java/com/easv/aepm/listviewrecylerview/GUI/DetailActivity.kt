package com.easv.aepm.listviewrecylerview.GUI

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.easv.aepm.listviewrecylerview.R
import com.easv.aepm.listviewrecylerview.data.BEFriend
import com.easv.aepm.listviewrecylerview.data.IntentValues
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.cell.view.*
import android.text.TextWatcher as TextWatcher


class DetailActivity : AppCompatActivity() {

    lateinit var friend: BEFriend

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        tvName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { validateFriend() }
        })
        tvMail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { validateFriend() }
        })
        tvNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { validateFriend() }
        })

        imgCallFriend.setOnClickListener { view ->
            var intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:(+45)${friend.number}"))
            startActivity(intent);
         }

        tvLink.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { validateFriend() }
        })

        if(intent.extras == null){
            imgCallFriend.isVisible = false
        }
        else{
            btnCreate.isVisible = false
            updateLayout.isVisible = true
            friend = intent.extras?.getSerializable("FRIEND") as BEFriend
            initializeText(friend)
        }
    }

    fun validateFriend(){
        if(tvName.text.isNotEmpty() && tvMail.text.isNotEmpty() && tvNumber.text.isNotEmpty()){
            btnCreate.isEnabled = true
            btnUpdate.isEnabled = true
        }
        else{
            btnCreate.isEnabled = false
            btnUpdate.isEnabled = false
        }
    }

    fun initializeText(friend: BEFriend){
        tvName.setText(friend.name)
        tvMail.setText(friend.mail)
        tvNumber.setText(friend.number)
        isFavorite.isChecked = friend.isFavorite
    }


    fun createCustomer(view: View) {
        val name = tvName.text.toString()
        val mail = tvMail.text.toString()
        val number = tvNumber.text.toString()
        val url = tvLink.text.toString()
        val isFavorite = isFavorite.isChecked
        val friend = BEFriend(0, name, number, mail, isFavorite, url)

        val intent = Intent()
        intent.putExtra("FRIEND_CREATE", friend)
        setResult(IntentValues.RESPONSE_DETAIL_CREATE.code, intent)
        finish()
    }

    fun updateCustomer(view: View) {
        friend.name = tvName.text.toString()
        friend.mail = tvMail.text.toString()
        friend.number = tvNumber.text.toString()
        friend.url = tvLink.text.toString()
        friend.isFavorite = isFavorite.isChecked

        val intent = Intent()
        intent.putExtra("FRIEND_UPDATE", friend)
        setResult(IntentValues.RESPONSE_DETAIL_UPDATE.code, intent)
        finish()
    }

    fun deleteCustomer(view: View) {
        val intent = Intent()
        intent.putExtra("FRIEND_DELETE", friend)
        setResult(IntentValues.RESPONSE_DETAIL_DELETE.code, intent)
        finish()
    }
}