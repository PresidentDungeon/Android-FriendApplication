package com.easv.aepm.listviewrecylerview.GUI

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.easv.aepm.listviewrecylerview.R
import com.easv.aepm.listviewrecylerview.data.BEFriend
import com.easv.aepm.listviewrecylerview.data.IntentValues
import kotlinx.android.synthetic.main.activity_detail.*
import java.text.SimpleDateFormat
import java.util.*


class DetailActivity : AppCompatActivity() {

    lateinit var friend: BEFriend
    val myCalendar: Calendar = Calendar.getInstance()

    var date =
        OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
            myCalendar[Calendar.YEAR] = year
            myCalendar[Calendar.MONTH] = monthOfYear
            myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
            updateLabel()
        }

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

        imgCallFriend.setOnClickListener { view -> var intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${friend.number}")); startActivity(intent);}
        imgTextFriend.setOnClickListener { view -> var intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${friend.number}")); startActivity(intent);}
        imgMailFriend.setOnClickListener { view -> sendMail()}
        imgLinkFriend.setOnClickListener { view -> goToLink()}
        tvBirthday.setOnClickListener { view -> openPopup()}

        tvLink.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { validateFriend() }
        })

        if(intent.extras == null){
            imgCallFriend.isVisible = false
            imgTextFriend.isVisible = false
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
        tvLink.setText(friend.url)
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

    fun sendMail(){
        var emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "plain/text"
        val receivers = arrayOf("${friend.mail}")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, receivers)
        startActivity(emailIntent)
    }

    fun goToLink(){
        var url = this.friend.url;
        if (!url!!.startsWith("http://") && !url.startsWith("https://")){url = "http://" + url}
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse("${url}"))
        startActivity(intent)
    }

    fun openPopup(){
        DatePickerDialog(
            this, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
            myCalendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun updateLabel() {
        val myFormat = "dd/MM/yy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.GERMAN)
        tvBirthday.setText(sdf.format(myCalendar.time))
    }

}