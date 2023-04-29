package com.gvvp.roadcrackdetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{
    private final View mWindow;
    private Context mContext;

    public CustomInfoWindowAdapter(MapsActivity context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.marker_info_layout,null);
    }

    private Bitmap decodeBase64ToBitmap(String base64) {
        byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private void rendowWindowText(Marker marker, View view){
        String imageString = (String) marker.getTag();
        ImageView imageView = view.findViewById(R.id.crackimage);
        if (imageString != null && !imageString.isEmpty()) {
            Bitmap image = decodeBase64ToBitmap(imageString);
            if (image != null) {
                imageView.setImageBitmap(image);
            }
        }
        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.cracktitle);
        if(!title.equals("")){
            tvTitle.setText(title);
        }
        String snippet = marker.getSnippet();
        TextView tvSnippet = view.findViewById(R.id.crackinfo);
        if(!snippet.equals("")){
            tvSnippet.setText(snippet);
        }
    }
    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        rendowWindowText(marker,mWindow);
        return null;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        rendowWindowText(marker,mWindow);
        return mWindow;
    }
}
