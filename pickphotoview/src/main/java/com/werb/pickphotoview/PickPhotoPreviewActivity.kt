package com.werb.pickphotoview

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.annotation.Keep
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.werb.eventbus.EventBus
import com.werb.eventbus.Subscriber
import com.werb.pickphotoview.event.PickPreviewEvent
import com.werb.pickphotoview.extensions.alphaColor
import com.werb.pickphotoview.extensions.color
import com.werb.pickphotoview.extensions.string
import com.werb.pickphotoview.util.PickConfig
import com.werb.pickphotoview.util.PickPhotoHelper
import com.werb.pickphotoview.widget.PreviewImage
import kotlinx.android.synthetic.main.pick_activty_preview_photo.*
import kotlinx.android.synthetic.main.pick_widget_my_toolbar.*


/**
 * Created by wanbo on 2017/1/4.
 */
@Keep
class PickPhotoPreviewActivity : BasePickActivity() {

    private var path: String? = null
    private var dir: String? = null
    private val selectImages = PickPhotoHelper.selectImages
    private val allImages: List<String> by lazy { allImage() }
    private val imageViews: List<PreviewImage> by lazy { imageViews() }
    private var full = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.register(this)
        setContentView(R.layout.pick_activty_preview_photo)
        path = intent.getStringExtra("path")
        dir = intent.getStringExtra("dir")

        initToolbar()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.unRegister(this)
    }

    private fun initToolbar() {
        GlobalData.model?.let {
            toolbar.setBackgroundColor(color(it.toolbarColor))
            statusBar.setBackgroundColor(color(it.statusBarColor))
            midTitle.setTextColor(color(it.toolbarTextColor))
            cancel.setTextColor(color(it.toolbarTextColor))
            if (selectImages.size > 0) {
                sure.setTextColor(color(it.toolbarTextColor))
            } else {
                sure.setTextColor(alphaColor(color(it.toolbarTextColor)))
            }
            sure.text = String.format(string(R.string.pick_photo_sure), selectImages.size)
            sure.setOnClickListener { add() }
            cancel.setOnClickListener { finish() }

            path?.let {
                val index = allImages.indexOf(it)
                val all = allImages.size
                val current = index + 1
                midTitle.text = "$current/$all"
                viewPager.adapter = ListPageAdapter()
                viewPager.currentItem = index
                viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                    }

                    override fun onPageSelected(position: Int) {
                        val i = position + 1
                        midTitle.text = "$i/$all"
                    }

                    override fun onPageScrollStateChanged(state: Int) {

                    }
                })
            }

        }
    }

    private fun imageViews(): List<PreviewImage> = arrayListOf<PreviewImage>().apply {
        for (index in 1..4) {
            add(PreviewImage(this@PickPhotoPreviewActivity))
        }
    }

    private fun add() {
        if (selectImages.isNotEmpty()) {
            finishForResult()
        }
    }

    private fun allImage(): List<String> {
        val groupImage = PickPhotoHelper.groupImage
        return groupImage?.mGroupMap?.get(dir) ?: emptyList()
    }

    private fun full() {
        full = !full
        hideOrShowToolbar()
        if (full) {
            window.decorView.systemUiVisibility = View.INVISIBLE
        } else {
            window.decorView.systemUiVisibility = View.VISIBLE
            GlobalData.model?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (it.lightStatusBar) {
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    }
                }
            }
        }
    }

    private fun hideOrShowToolbar() {
        appbar.animate()
                .translationY(if (!full) 0f else -appbar.height.toFloat())
                .setInterpolator(DecelerateInterpolator())
                .start()
    }

    @Subscriber()
    private fun select(event: PickPreviewEvent) {
        GlobalData.model?.let {
            if (selectImages.size > 0) {
                sure.setTextColor(color(it.toolbarTextColor))
            } else {
                sure.setTextColor(alphaColor(color(it.toolbarTextColor)))
            }
            sure.text = String.format(string(R.string.pick_photo_sure), selectImages.size)
        }
        if (full) full()
    }

    //通过ViewPager实现滑动的图片
    private inner class ListPageAdapter : PagerAdapter() {

        override fun getCount(): Int {
            return allImages.size
        }

        override fun isViewFromObject(view: View, any: Any): Boolean {
            return view === any
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val i = position % 4
            val pic = imageViews[i]
            val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val path = allImages[position]
            pic.setImage(path, this@PickPhotoPreviewActivity::full)
            container.addView(pic, params)
            return pic
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val i = position % 4
            val imageView = imageViews[i]
            container.removeView(imageView)
            imageView.clear()
        }
    }

    companion object {
        fun startActivity(activity: Activity, requestCode: Int, currentPath: String, dirName: String) {
            val intent = Intent(activity, PickPhotoPreviewActivity::class.java)
            intent.putExtra("path", currentPath)
            intent.putExtra("dir", dirName)
            activity.startActivityForResult(intent, requestCode)
            activity.overridePendingTransition(R.anim.activity_anim_right_to_current, R.anim.activity_anim_not_change)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_anim_not_change, R.anim.activity_anim_current_to_right)
    }

    private fun finishForResult() {
        val intent = Intent()
        intent.setClass(this@PickPhotoPreviewActivity, PickPhotoActivity::class.java)
        setResult(PickConfig.PREVIEW_PHOTO_DATA, intent)
        finish()
    }
}
