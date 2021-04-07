package com.zoomrx.imagePicker

import com.getcapacitor.*

@NativePlugin
class ImagePicker : Plugin() {
    @PluginMethod
    fun echo(call: PluginCall) {
        val value = call.getString("value")
        val ret = JSObject()
        ret.put("value", value)
        call.success(ret)
    }
}