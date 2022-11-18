package com.demo.gallery;

import android.net.Uri;

import com.androidx.picker.MediaItem;

import androidx.annotation.Nullable;

/**
 *
 * description: 
 */
public class GalleryItem {
    private final MediaItem media;
    private TitleType titleType;

    public GalleryItem(MediaItem media) {
        this.media = media;
    }

    public GalleryItem(TitleType titleType) {
        this.media = null;
        this.titleType = titleType;
    }

    @Nullable
    public Uri getUri() {
        return media == null ? null : media.getUri();
    }

    public boolean isTitle() {
        return titleType != null && media == null;
    }

    public String getTitle() {
        String title;
        if (titleType == TitleType.EARLIER) {
            title = "Earlier Photos";
        } else if (titleType == TitleType.LAST_7_DAYS) {
            title = "Last 7 Days";
        } else if (titleType == TitleType.TODAY) {
            title = "Today";
        } else {
            title = "";
        }
        return title;
    }

}
