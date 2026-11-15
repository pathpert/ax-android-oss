package com.kickstarter.ui.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.ax.errors.ProblemDetailsMapper
import com.kickstarter.ax.errors.ProblemException
import com.kickstarter.libs.utils.extensions.header
import com.kickstarter.libs.utils.extensions.isKSApplication
import com.kickstarter.libs.utils.extensions.userAgent

private fun ImageView.handleImageLoadError(e: Exception, operation: String, url: String?) {
    val problem = ProblemDetailsMapper.fromThrowable(e, operation)
    this.setImageResource(R.drawable.image_placeholder)
    val crashlytics = FirebaseCrashlytics.getInstance()
    crashlytics.setCustomKey("error_type", problem.type)
    crashlytics.setCustomKey("error_title", problem.title)
    crashlytics.setCustomKey("error_action", problem.suggestedAction)
    crashlytics.setCustomKey("error_retriable", problem.isRetriable)
    crashlytics.setCustomKey("ImageView.$operation", "url: $url, detail: ${problem.detail}")
    crashlytics.recordException(ProblemException(problem, e))
}

/**
 * UI component for ImageView.
 *
 * Negative constraints:
 * - Do not invoke business logic directly; delegate to ViewModel.
 */
fun ImageView.loadCircleImage(url: String?) {

    url?.let {
        try {
            if (it.isBlank()) { // - load with drawable
                Glide.with(context)
                    .load(ColorDrawable(Color.TRANSPARENT))
                    .circleCrop()
                    .into(this)
            } else { // - load with url string
                val glideUrl = GlideUrl(
                    it,
                    LazyHeaders.Builder()
                        .addHeader(header, context.userAgent())
                        .build()
                )
                Glide.with(context)
                    .load(glideUrl)
                    .placeholder(ColorDrawable(Color.TRANSPARENT))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .circleCrop()
                    .into(this)
            }
        } catch (e: Exception) {
            handleImageLoadError(e, "loadCircleImage", it)
        }
    }
}

fun ImageView.loadImage(url: String?) {
    url?.let {
        try {
            val glideUrl = GlideUrl(
                it,
                LazyHeaders.Builder()
                    .addHeader(header, context.userAgent())
                    .build()
            )
            Glide.with(context)
                .load(glideUrl)
                .placeholder(ColorDrawable(Color.TRANSPARENT))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(this)
        } catch (e: Exception) {
            handleImageLoadError(e, "loadImage", it)
        }
    }
}

fun ImageView.loadImageWithResize(
    url: String?,
    targetImageWidth: Int,
    targetImageHeight: Int,
    placeholder: Drawable
) {
    url?.let {
        try {
            val glideUrl = GlideUrl(
                it,
                LazyHeaders.Builder()
                    .addHeader(header, context.userAgent())
                    .build()
            )
            Glide.with(context)
                .load(glideUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .apply(RequestOptions().override(targetImageWidth, targetImageHeight))
                .centerCrop()
                .placeholder(placeholder)
                .into(this)
        } catch (e: Exception) {
            handleImageLoadError(e, "loadImageWithResize", it)
        }
    }
}
fun ImageView.loadImage(url: String?, context: Context, imageZoomablePlaceholder: AppCompatImageView? = null) {
    url?.let {
        val targetView = this
        if (context.applicationContext.isKSApplication()) {
            try {
                val glideUrl = GlideUrl(
                    it,
                    LazyHeaders.Builder()
                        .addHeader(header, context.userAgent())
                        .build()
                )
                Glide.with(context)
                    .load(glideUrl)
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            targetView.setImageDrawable(resource)
                            imageZoomablePlaceholder?.setImageDrawable(resource)
                            return isFirstResource
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            targetView.setImageDrawable(null)
                            imageZoomablePlaceholder?.setImageDrawable(null)
                            return isFirstResource
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .load(url)
                    .into(this)
            } catch (e: Exception) {
                handleImageLoadError(e, "loadImageWithContext", it)
            }
        } else {
            this.setImageResource(R.drawable.image_placeholder)
        }
    }
}

fun ImageView.loadWebp(url: String?, context: Context) {
    url?.let {
        try {
            val glideUrl = GlideUrl(
                it,
                LazyHeaders.Builder()
                    .addHeader(header, context.userAgent())
                    .build()
            )
            val roundedCorners = RoundedCorners(1)
            Glide.with(this)
                .load(glideUrl)
                .downsample(DownsampleStrategy.AT_LEAST)
                .override(Resources.getSystem().displayMetrics.widthPixels, Resources.getSystem().displayMetrics.heightPixels)
                .optionalTransform(roundedCorners)
                .optionalTransform(WebpDrawable::class.java, WebpDrawableTransformation(roundedCorners))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(this)
        } catch (e: Exception) {
            handleImageLoadError(e, "loadWebp", url)
        }
    }
}

fun ImageView.loadGifImage(url: String?, context: Context) {
    url?.let {
        if (context.applicationContext.isKSApplication()) {
            try {
                val glideUrl = GlideUrl(
                    it,
                    LazyHeaders.Builder()
                        .addHeader(header, context.userAgent())
                        .build()
                )
                Glide.with(context)
                    .asGif()
                    .load(glideUrl)
                    .downsample(DownsampleStrategy.AT_LEAST)
                    .override(Resources.getSystem().displayMetrics.widthPixels, Resources.getSystem().displayMetrics.heightPixels)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(this)
            } catch (e: Exception) {
                handleImageLoadError(e, "loadGifImage", url)
            }
        } else {
            this.setImageResource(R.drawable.image_placeholder)
        }
    }
}
