package com.example.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    //private final String title;
    //private final String snippet;

    /*public MyItem(double lat, double lng, String title, String snippet) {
        this.mPosition = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
    }*/
    public MyItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }
    @NonNull
    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }
}
