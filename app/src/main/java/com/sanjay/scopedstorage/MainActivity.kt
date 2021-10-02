package com.sanjay.scopedstorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sanjay.scopedstorage.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    var binding:ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding?.apply {
            setContentView(this.root)
        }
        
    }
}