package com.example.digitalvouchers.Ui

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.bumptech.glide.module.AppGlideModule
import com.caverock.androidsvg.SVG
import java.io.InputStream

class SvgHelper {

}


class StreamToSvg : ResourceDecoder<InputStream, SVG> {
    override fun handles(source: InputStream, options: Options) = true
    override fun decode(
        source: InputStream,
        width: Int,
        height: Int,
        options: Options
    ): Resource<SVG> {
        return SimpleResource(SVG.getFromInputStream(source))
    }
}


class SvgToDrawable : ResourceTranscoder<SVG, PictureDrawable> {
    override fun transcode(
        toTranscode: Resource<SVG>,
        options: Options
    ): Resource<PictureDrawable> {
        val picture = toTranscode.get().renderToPicture()
        return SimpleResource(PictureDrawable(picture))
    }
}


fun loadSvg(imageView: ImageView, url: String) {
    Glide.with(imageView.context)
        .`as`(PictureDrawable::class.java)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .into(imageView)
}