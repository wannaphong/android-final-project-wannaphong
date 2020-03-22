package th.ac.kku.cis.mobileapp.wwwlink.ui.home

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jsoup.Jsoup
import th.ac.kku.cis.mobileapp.wwwlink.Cleanurl
import th.ac.kku.cis.mobileapp.wwwlink.R
import th.ac.kku.cis.mobileapp.wwwlink.URLItem
import th.ac.kku.cis.mobileapp.wwwlink.UserLink


class HomeFragment : Fragment() {
    lateinit var mDB: DatabaseReference

    private lateinit var homeViewModel: HomeViewModel
    lateinit var auth: FirebaseAuth
    var todoItemList: MutableList<URLItem>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        mDB = FirebaseDatabase.getInstance().reference
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        textView.text = "Hi"
        val addbtn:Button = root.findViewById(R.id.AddURL)
        addbtn.setOnClickListener {
            addNewItem()
        }
        return root
    }
    fun addNewItem(){
        var c:Cleanurl = Cleanurl()
        val dialog = AlertDialog.Builder(activity)

        val context: Context? = this.context
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val et = EditText(context)
        et.hint = "URL"
        layout.addView(et) // Notice this is an add method

        val descriptionBox = EditText(context)
        descriptionBox.hint = "Note"
        layout.addView(descriptionBox) // Another add method
        dialog.setView(layout)

        dialog.setPositiveButton("Submit"){
                dialog,positiveButton ->

            var newURL = URLItem.create()
            var url:String =c.clean(et.text.toString())
            var note:String = descriptionBox.text.toString()
            newURL.url = url
            Log.w("URL",newURL.url)
            val newURL2user:UserLink = UserLink.create()
            var key:String? = ""
            newURL2user.Note = note
            mDB.child("URL_item").orderByChild("url").equalTo(url).limitToFirst(1).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val children = dataSnapshot!!.children
                    key=children.first().key.toString()
                    Log.w("Firebase",key)
                    newURL2user.URLobj=key
                    addDB(newURL,newURL2user)
                }

                override fun onCancelled(error: DatabaseError) {
                     Log.e("Firebase","Failed to read value: ${error.toException()}")
                }
            })

            Log.w("Key Key",key)


            dialog.dismiss()
        }
        dialog.show()
    }
    fun addDB(newURL:URLItem,newURL2user:UserLink){
        var uid:String = auth.currentUser!!.uid
        if(newURL2user.URLobj==null){
            val newItemDB = mDB.child("URL_item").push()
            newURL.objID = newItemDB.key
            newItemDB.setValue(newURL)
            newURL2user.URLobj=newItemDB.key
        }
        val newItemDB2 = mDB.child("URL").child(uid).push()
        newURL2user.objID = newItemDB2.key
        newURL2user.status = false
        newURL2user.URL = newURL.url

        newItemDB2.setValue(newURL2user)

    }
}
