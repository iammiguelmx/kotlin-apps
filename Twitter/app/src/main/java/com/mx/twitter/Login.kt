package com.mx.twitter

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Login : AppCompatActivity() {

    private var mAuth:FirebaseAuth?=null

    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()

        ivImagePerson.setOnClickListener(View.OnClickListener {
            checkPermission()
        })


    }

    fun LoginToFireBase(email:String,password:String){

        mAuth!!.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this){ task ->

            if(task.isSuccessful){

                Toast.makeText(applicationContext,"Successful login", Toast.LENGTH_LONG).show()


                SaveImageInFireBase()

            }else{
                Toast.makeText(applicationContext,"Fail login", Toast.LENGTH_LONG).show()
            }
        }
    }

    //Method for save the image in fire base
    fun SaveImageInFireBase(){

        var currentUser = mAuth!!.currentUser

        val EMAIL:String = currentUser!!.email.toString()
        val STORAGE = FirebaseStorage.getInstance()
        val STORAGE_REF = STORAGE.getReferenceFromUrl("gs://twitter-d2cff.appspot.com")
        val DF = SimpleDateFormat("ddMMyyHHmmss")
        val DATAOBJ = Date()
        val IMAGE_PATH = SplitString(EMAIL)+ "." + DF.format(DATAOBJ)+ ".jpg"
        val IMAGE_REG = STORAGE_REF.child("images/" + IMAGE_PATH)
        ivImagePerson.isDrawingCacheEnabled=true
        ivImagePerson.buildDrawingCache()

        val DRAWABLE = ivImagePerson.drawable as BitmapDrawable
        val BITMAP = DRAWABLE.bitmap
        val BAOS = ByteArrayOutputStream()
        BITMAP.compress(Bitmap.CompressFormat.JPEG,100,BAOS)
        val DATA = BAOS.toByteArray()
        val UPLOAD_TASK = IMAGE_REG.putBytes(DATA)
        UPLOAD_TASK.addOnFailureListener{

            Toast.makeText(applicationContext,"Fail to upload", Toast.LENGTH_LONG).show()

        }.addOnSuccessListener {taskSnapshot ->

            var downloadURL = taskSnapshot.getMetadata()!!.getReference()!!.getDownloadUrl().toString()

            myRef.child("User").child(currentUser.uid).child("email").setValue(currentUser.email)
            myRef.child("User").child(currentUser.uid).child("ProfileImage").setValue(downloadURL)
            LoadTweets()
        }
    }

    //Method for split the email
    fun SplitString(EMAIL:String):String{
        val SPLIT = EMAIL.split("@")
        return SPLIT[0]
    }

    override fun onStart(){
        super.onStart()
        LoadTweets()
    }

    //Method for loading email and uid
    fun LoadTweets(){
        var currentUser = mAuth!!.currentUser

        if(currentUser != null){

            var intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email",currentUser.email)
            intent.putExtra("uid",currentUser.uid)

            startActivity(intent)
        }
    }


    //Method for check permission the read external storage in the mobile
    val READ_IMAGE: Int = 253
    fun checkPermission(){
        if (Build.VERSION.SDK_INT>=23){
            if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.
                READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),READ_IMAGE)
                return
            }
        }
        loadImage()
    }

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
            ivImagePerson.setImageBitmap(BitmapFactory.decodeFile(PICTURE_PATH))
        }
    }

    //Method returning the permit result
    fun onRequestPermissionResult(requestCode: Int, permission: Array<out String>, grantResults: IntArray){

        when(requestCode){
            READ_IMAGE->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    loadImage()
                }else{
                    Toast.makeText(applicationContext,"Cannot access your images",Toast.LENGTH_LONG).show()
                }
            }
            else-> super.onRequestPermissionsResult(requestCode,permission,grantResults)
        }

    }

    //Method for Login
    fun buLogin(view:View) {
        LoginToFireBase(etEmail.text.toString(),etPassword.text.toString())
    }

}//end class