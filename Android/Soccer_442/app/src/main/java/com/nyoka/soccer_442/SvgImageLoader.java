package com.nyoka.soccer_442;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.caverock.androidsvg.SVG;

public class SvgImageLoader {

    public static void loadSvgImage(Context context, String imageUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions()
                .fitCenter();  // You can customize RequestOptions based on your needs

        Glide.with(context)
                .as(PictureDrawable.class)
                .apply(requestOptions)
                .load(imageUrl)
                .into(new SvgImageViewTarget(imageView));
    }

    private static class SvgImageViewTarget extends CustomTarget<PictureDrawable> {

        private final ImageView imageView;

        public SvgImageViewTarget(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public void onResourceReady(PictureDrawable resource, Transition<? super PictureDrawable> transition) {
            imageView.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null);
            imageView.setImageDrawable(resource);
        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
            // Implement if needed
        }
    }
}

