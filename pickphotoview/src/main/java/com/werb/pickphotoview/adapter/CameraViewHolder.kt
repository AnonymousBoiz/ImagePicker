package com.werb.pickphotoview.adapter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.werb.library.MoreViewHolder
import com.werb.pickphotoview.GlobalData
import com.werb.pickphotoview.util.PickConfig
import com.werb.pickphotoview.util.PickUtils
import java.io.File
import java.io.IOException

/** Created by wanbo <werbhelius@gmail.com> on 2017/10/19. */

class CameraViewHolder(containerView: View) : MoreViewHolder<String>(containerView) {

    private val context = containerView.context

    init {
        GlobalData.model?.let {
            val screenWidth = PickUtils.getInstance(context.applicationContext).widthPixels
            val space = PickUtils.getInstance(context.applicationContext).dp2px(PickConfig.ITEM_SPACE.toFloat())
            val scaleSize = (screenWidth - (it.spanCount + 1) * space) / it.spanCount
            val params = containerView.layoutParams
            params.width = scaleSize
            params.height = scaleSize
        }
    }

    override fun bindData(data: String, payloads: List<Any>) {
        containerView.setOnClickListener {
            try {
                val photoFile = PickUtils.getInstance(context.applicationContext).createImageFile(context)
//                photoFile.delete()
                if (photoFile == null) return@setOnClickListener
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                Log.d("55555555555", photoFile.path)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, PickUtils.getInstance(context.applicationContext).getUri(photoFile))
                if (context is Activity) {
                    context.startActivityForResult(intent, PickConfig.CAMERA_PHOTO_DATA)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}