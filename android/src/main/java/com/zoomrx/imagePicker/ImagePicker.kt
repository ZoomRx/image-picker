package com.zoomrx.imagePicker

import android.graphics.Color
import android.net.Uri
import com.getcapacitor.*
import com.zoomrx.imagepicker.ImagePicker
import com.zoomrx.imagepicker.modal.CameraParams
import com.zoomrx.imagepicker.modal.EditorParams
import com.zoomrx.imagepicker.modal.GalleryParams
import com.zoomrx.imagepicker.modal.ImageFileParams
import com.zoomrx.imagepicker.NativeCallbackInterface
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@NativePlugin
class ImagePicker : Plugin() {

    @PluginMethod
    fun pick(call: PluginCall) {
        val source = call.getInt("source")
        val settings = call.getObject("settings")

        var toDirectory: String? = settings.getString("path")
        if (toDirectory == null) {
            call.reject(ImagePicker.ErrorCodes.INSUFFICIENT_DATA.toString(), "Insufficient data")
            return
        }
        if (toDirectory.startsWith("file://")) {
            toDirectory = toDirectory.substringAfter("file://")
        }

        val imageFileParams = ImageFileParams(toDirectory)

        imageFileParams.fileNamePrefix = settings.getString("fileNamePrefix", imageFileParams.fileNamePrefix)
        settings.optString("relativeDirectory").let {
            if (it.isNotEmpty())
                imageFileParams.relativeDirectory = it
        }

        val allowEditing = settings.getBoolean("allowEditing", true)

        val editorParams = if (allowEditing) {
            EditorParams(
                    if (source == ImagePicker.PhotoSource.CAMERA)
                        EditorParams.EditFlow.FROM_CUSTOM_CAMERA
                    else if (source == ImagePicker.PhotoSource.GALLERY)
                        EditorParams.EditFlow.FROM_GALLERY
                    else {
                        call.reject(ImagePicker.ErrorCodes.INSUFFICIENT_DATA.toString(), "Insufficient data")
                        return
                    }
            ).also {
                it.captionPlaceHolder = settings.getString("captionPlaceHolderText", it.captionPlaceHolder)
                it.maxSelection = settings.getInteger("maxSelection", it.maxSelection)
                it.allowCaption = settings.getBoolean("allowCaption", it.allowCaption)
                it.allowDeletion = settings.getBoolean("allowDeletion", it.allowDeletion)
                settings.optJSONArray("navBarTintColor")?.let { rgbArray ->
                    if (rgbArray.length() == 3) {
                        it.navBarTint = Color.rgb(rgbArray.getInt(0), rgbArray.getInt(1), rgbArray.getInt(2))
                    }
                }
                settings.optJSONArray("navButtonTintColor")?.let { rgbArray ->
                    if (rgbArray.length() == 3) {
                        it.navButtonTint = Color.rgb(rgbArray.getInt(0), rgbArray.getInt(1), rgbArray.getInt(2))
                    }
                }
            }
        } else null

        val imagePicker = ImagePicker(
                context, imageFileParams, editorParams,
                object : NativeCallbackInterface {

                    override fun resolve(filePathArray: ArrayList<String>, captionArray: ArrayList<String>?) {
                        val imagesData = JSONArray()
                        filePathArray.withIndex().forEach {
                            val jsonObject = JSONObject()
                            jsonObject.put("imageURL", FileUtils.getPortablePath(context, bridge.localUrl, Uri.fromFile(File(filePathArray[it.index]))))
                            if (captionArray != null) {
                                jsonObject.put("caption", captionArray[it.index])
                            }
                            imagesData.put(it.index, jsonObject)
                        }

                        call.resolve(JSObject.fromJSONObject(JSONObject().put("images", imagesData)))
                    }

                    override fun reject(message: String, code: Int) {
                        call.reject(message, code.toString())
                    }
                }
        )

        if (source == ImagePicker.PhotoSource.CAMERA) {
            val cameraParams = CameraParams(call.getBoolean("saveToGallery", true))
            imagePicker.startCameraWorkFlow(cameraParams)
        } else {
            val galleryParams = GalleryParams(call.getBoolean("allowMultiple", true))
            imagePicker.startGalleryWorkFlow(galleryParams)
        }
    }
}