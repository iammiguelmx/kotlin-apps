package com.mx.twitter

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_ticket.view.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private var database= FirebaseDatabase.getInstance()
    private var myRef=database.reference

    var ListTweets=ArrayList<Ticket>()
    var adapter:MyTweetAdpater?=null
    var myemail:String?=null
    var UserUID:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var b:Bundle=intent.extras!!
        myemail=b.getString("email")
        UserUID=b.getString("uid")
        //Dummy data
        ListTweets.add(Ticket("0","him","url","add"))
        ListTweets.add(Ticket("0","him","url","hussein"))


        ListTweets.add(Ticket("0","him","url","add"))
        ListTweets.add(Ticket("0","him","url","hussein"))


         adapter=MyTweetAdpater(this,ListTweets)
        lvTweets.adapter=adapter
    }

    inner class  MyTweetAdpater: BaseAdapter {
        var listNotesAdpater = ArrayList<Ticket>()
        var context: Context? = null

        constructor(context: Context, listNotesAdpater: ArrayList<Ticket>) : super() {
            this.listNotesAdpater = listNotesAdpater
            this.context = context
        }


        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var myView = listNotesAdpater[position]

            if (myView.tweetPersonUID.equals("add")) {
                var myView = layoutInflater.inflate(R.layout.add_ticket, null)
                myView.iv_attach.setOnClickListener(
                    View.OnClickListener {
                        loadImage()
                    })

                myView.iv_post.setOnClickListener(
                    View.OnClickListener {
                        //uploas server

                        myRef.child("posts").setValue(
                            PostInfo(UserUID!!,
                            myView.etPost.text.toString(),DownloadURL!!))
                        myView.etPost.setText("")
                    })
                return myView
            } else {
                //Load tweet
                //TODO: work
                var myView = layoutInflater.inflate(R.layout.tweets_ticket, null)
                return myView
            }
        }

        override fun getCount(): Int {
            return listNotesAdpater.size
        }

        override fun getItem(p0: Int): Any {
            return listNotesAdpater[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }
    }

    //Load image
    //Method for load Image from the mobile
    //Method for load Image from the mobile
    val PICK_IMAGE_CODE = 123
    fun loadImage() {
        var intent = Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CODE && data != null && resultCode == RESULT_OK){
            val SELECTED_IMAGE = data.data
            val FILE_PATH_COLUM = arrayOf(MediaStore.Images.Media.DATA)
            val CURSOR = contentResolver.query(SELECTED_IMAGE!!,FILE_PATH_COLUM,null,null,null)
            CURSOR!!.moveToFirst()
            val COULOM_INDEX = CURSOR.getColumnIndex(FILE_PATH_COLUM[0])
            val PICTURE_PATH = CURSOR.getString(COULOM_INDEX)
            CURSOR.close()
            UploadImage(BitmapFactory.decodeFile(PICTURE_PATH))
        }
    }


    var DownloadURL:String?=null
    fun UploadImage(bitmap: Bitmap){

        val STORAGE = FirebaseStorage.getInstance()
        val STORAGE_REF = STORAGE.getReferenceFromUrl("gs://twitter-d2cff.appspot.com")
        val DF = SimpleDateFormat("ddMMyyHHmmss")
        val DATAOBJ = Date()
        val IMAGE_PATH = SplitString(myemail!!)+ "." + DF.format(DATAOBJ)+ ".jpg"
        val IMAGE_REG = STORAGE_REF.child("imagePost/" + IMAGE_PATH)
        val BAOS = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,BAOS)
        val DATA = BAOS.toByteArray()
        val UPLOAD_TASK = IMAGE_REG.putBytes(DATA)
        UPLOAD_TASK.addOnFailureListener{

            Toast.makeText(applicationContext,"Fail to upload", Toast.LENGTH_LONG).show()

        }.addOnSuccessListener { taskSnapshot ->

             DownloadURL =
                taskSnapshot.getMetadata()!!.getReference()!!.getDownloadUrl().toString()

        }
    }


    fun SplitString(email:String):String{
        val split= email.split("@")
        return split[0]
    }

}