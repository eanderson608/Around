package com.cs407.around;

import android.content.Context;
import android.content.pm.PermissionGroupInfo;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Custom adapter that adapts custom_photo_feed_items to a recyclerview
 * to construct mostly followed tutorial at http://javatechig.com/android/android-recyclerview-example
 */
public class CustomPhotoFeedAdapter extends RecyclerView.Adapter<CustomPhotoFeedAdapter.PhotoViewHolder> {

    String path = "http://eanderson608.ddns.net/uploads/";
    private ArrayList<Photo> photoArrayList;
    private Context context;

    public CustomPhotoFeedAdapter(Context context, ArrayList<Photo> photoArrayList) {
        this.photoArrayList = photoArrayList;
        this.context = context;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {

        protected ImageView imageView;

        PhotoViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.photo_image_view);

        }
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_photo_feed_item, null);
        PhotoViewHolder viewHolder = new PhotoViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {
        Photo photo = photoArrayList.get(position);

        Log.v("onBindViewHolder", photo.getFileName());

        // Download image with picasso
        Picasso.with(context).load(path + photo.getFileName())
                .rotate(90)
                .error(R.drawable.error)
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return (null != photoArrayList ? photoArrayList.size() : 0);
    }
}
