package com.example.winterdb1001

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.winterdb1001.databinding.AboutUsBinding

class AboutUs : AppCompatActivity() {
    private val binding : AboutUsBinding by lazy{
        AboutUsBinding.inflate(layoutInflater);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(binding.root);
    }

}