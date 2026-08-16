package com.airdesk.launcher

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web=WebView(this)
        web.settings.javaScriptEnabled=true
        web.settings.domStorageEnabled=true
        web.webViewClient=WebViewClient()
        web.webChromeClient=object:WebChromeClient(){
            override fun onPermissionRequest(r:PermissionRequest){
                runOnUiThread {
                    if (r.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                        r.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                    else r.deny()
                }
            }
        }
        web.addJavascriptInterface(Air
