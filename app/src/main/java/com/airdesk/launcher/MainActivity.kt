package com.airdesk.launcher

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.webkit.WebViewAssetLoader
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web=WebView(this)
        web.settings.javaScriptEnabled=true
        web.settings.domStorageEnabled=true
        web.webViewClient=object:WebViewClient(){
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }
        web.webChromeClient=object:WebChromeClient(){
            override fun onPermissionRequest(r:PermissionRequest){
                runOnUiThread {
                    if (r.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                        r.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                    else r.deny()
                }
            }
        }
        web.addJavascriptInterface(JSBridge(),"AndroidAirDesk")
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
        setContentView(web)
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA),10)
    }

    inner class JSBridge {
        @JavascriptInterface fun getApps():String {
            val intent=Intent(Intent.ACTION_MAIN,null).apply{addCategory(Intent.CATEGORY_LAUNCHER)}
            val pm=packageManager
            val arr=JSONArray()
            pm.queryIntentActivities(intent,PackageManager.MATCH_ALL)
                .map{it.activityInfo.applicationInfo}
                .distinctBy{it.packageName}
                .sortedBy{pm.getApplicationLabel(it).toString().lowercase()}
                .forEach {
                    arr.put(JSONObject().apply{
                        put("label",pm.getApplicationLabel(it).toString())
                        put("packageName",it.packageName)
                    })
                }
            return arr.toString()
        }

        @JavascriptInterface fun openApp(pkg:String) {
            runOnUiThread { packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) } }
        }
    }
}
