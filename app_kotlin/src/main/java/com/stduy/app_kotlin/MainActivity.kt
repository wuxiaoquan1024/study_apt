package com.stduy.app_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.stduy.lib_annotations.BindView
import com.stduy.lib_annotations.OnClickListener
import com.stduy.lib_core.ButterKniffer

class MainActivity : AppCompatActivity() {

    @BindView(R.id.helloworld)
    lateinit var helloWorld: TextView

    @BindView(R.id.button)
    lateinit var button: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKniffer.bindView(this)
        helloWorld.text = "绑定成功"
    }

    @OnClickListener(ids = [R.id.button, R.id.button2])
    fun onClick(v: View) {
        when(v.id) {
            R.id.button2,
            R.id.button-> {
                Toast.makeText(this, "被点击了${v.id}", Toast.LENGTH_LONG).show();
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ButterKniffer.unbind(this)
    }
}
