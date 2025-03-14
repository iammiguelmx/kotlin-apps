package com.mx.twitter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_ticket.view.*
import kotlinx.android.synthetic.main.tweets_ticket.view.*
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
        ListTweets.add(Ticket("0", "him", "url", "add"))

         adapter=MyTweetAdpater(this, ListTweets)
        lvTweets.adapter=adapter
        LoadPost()
        MobileAds.initialize(this) {}
    }

    inner class  MyTweetAdpater: BaseAdapter {
        var listNotesAdpater = ArrayList<Ticket>()
        var context: Context? = null
        constructor(context: Context, listNotesAdpater: ArrayList<Ticket>) : super() {
            this.listNotesAdpater = listNotesAdpater
            this.context = context
        }


        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var mytweet = listNotesAdpater[position]

            if (mytweet.tweetPersonUID.equals("add")) {
                var myView = layoutInflater.inflate(R.layout.add_ticket, null)
                myView.iv_attach.setOnClickListener(
                    View.OnClickListener {
                        loadImage()
                    })
                myView.iv_post.setOnClickListener(
                    View.OnClickListener {
                        //upload server
                        myRef.child("posts").push().setValue(
                            PostInfo(
                                UserUID!!,
                                myView.etPost.text.toString(), DownloadURL!!
                            )
                        )
                        myView.etPost.setText("")
                    })
                return myView
            } else if (mytweet.tweetPersonUID.equals("loading")) {
                var myView = layoutInflater.inflate(R.layout.loading_ticket, null)
                return myView
            } else if (mytweet.tweetPersonUID.equals("ads")){
                var myView=layoutInflater.inflate(R.layout.ads_ticket, null)
                var mAdView = myView.findViewById(R.id.adView) as AdView
                val adRequest = AdRequest.Builder().build()
                mAdView.loadAd(adRequest)
                return myView
            } else {
                //Load tweet
                //TODO: work
                var myView = layoutInflater.inflate(R.layout.tweets_ticket, null)
                myView.txt_tweet.setText(mytweet.tweetText)

                //myView.tweet_picture.setImageURI(mytweet.tweetImageURL)
                Picasso.with(context).load(mytweet.tweetImageURL).into(myView.tweet_picture)

                myRef.child("Users").child(mytweet.tweetPersonUID!!)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            try {
                                var td= dataSnapshot!!.value as HashMap<String,Any>
                                for(key in td.keys){
                                    var userInfo= td[key] as String
                                    if(key.equals("ProfileImage")){
                                        Picasso.with(context).load(userInfo).into(myView.picture_path)
                                    }else{
                                        myView.txtUserName.text = userInfo
                                    }
                                }
                            } catch (ex: Exception) {
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                return myView
            }
        }

        override fun getCount(): Int = listNotesAdpater.size

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
        var intent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CODE && data != null && resultCode == RESULT_OK){
            val SELECTED_IMAGE = data.data
            val FILE_PATH_COLUM = arrayOf(MediaStore.Images.Media.DATA)
            val CURSOR = contentResolver.query(SELECTED_IMAGE!!, FILE_PATH_COLUM, null, null, null)
            CURSOR!!.moveToFirst()
            val COULOM_INDEX = CURSOR.getColumnIndex(FILE_PATH_COLUM[0])
            val PICTURE_PATH = CURSOR.getString(COULOM_INDEX)
            CURSOR.close()
            UploadImage(BitmapFactory.decodeFile(PICTURE_PATH))
        }
    }


    var DownloadURL:String?=""
    fun UploadImage(bitmap: Bitmap){
        ListTweets.add(0, Ticket("0", "him", "url", "loading"))
        adapter!!.notifyDataSetChanged()

        val STORAGE = FirebaseStorage.getInstance()
        val STORAGE_REF = STORAGE.getReferenceFromUrl("gs://twitter-d2cff.appspot.com")
        val DF = SimpleDateFormat("ddMMyyHHmmss")
        val DATAOBJ = Date()
        val IMAGE_PATH = SplitString(myemail!!)+ "." + DF.format(DATAOBJ)+ ".jpg"
        val IMAGE_REG = STORAGE_REF.child("imagePost/" + IMAGE_PATH)
        val BAOS = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, BAOS)
        val DATA = BAOS.toByteArray()
        val UPLOAD_TASK = IMAGE_REG.putBytes(DATA)
        UPLOAD_TASK.addOnFailureListener{

            Toast.makeText(applicationContext, "Fail to upload", Toast.LENGTH_LONG).show()

        }.addOnSuccessListener { taskSnapshot ->

             DownloadURL = taskSnapshot.storage.downloadUrl.toString()!!
             ListTweets.removeAt(0)
             adapter!!.notifyDataSetChanged()
        }
    }


    fun SplitString(email: String):String{
        val split= email.split("@")
        return split[0]
    }

    fun LoadPost(){
        myRef.child("posts")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    try {
                        ListTweets.clear()
                        ListTweets.add(Ticket("0", "him", "url", "add"))
                        ListTweets.add(Ticket("0", "him", "url", "ads"))
                        var td = dataSnapshot!!.value as HashMap<String, Any>

                        for (key in td.keys) {
                            var post = td[key] as HashMap<String, Any>
                            ListTweets.add(
                                Ticket(
                                    key,
                                    post["text"] as String,
                                    post["postImage"] as String,
                                    post["userUID"] as String
                                )
                            )
                        }
                        adapter!!.notifyDataSetChanged()
                    } catch (ex: Exception) {
                    }


                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

}