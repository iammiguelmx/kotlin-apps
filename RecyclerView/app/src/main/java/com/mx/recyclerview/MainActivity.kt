package com.mx.recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mx.recyclerview.model.Animal
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRecyclerView()
    }


    private fun setupRecyclerView(){
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val listAnimales = listOf(
            Animal("Cat","https://www.purina-latam.com/sites/g/files/auxxlc391/files/styles/facebook_share/public/Purina%C2%AE%20Como%20disciplinar%20a%20tu%20gato.jpg?itok=xitPK9Si"),
            Animal("Dog","https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcSZJrPhOghJR4SgQUCCnC_p_HU8n8sF4gXy5w&usqp=CAU"))
        recyclerView.adapter = RecyclerAdapter(this, listAnimales)
    }

}