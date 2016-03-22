package com.cs407.around;

import android.content.Context;
import android.content.pm.PermissionGroupInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Custom adapter that adapts custom_photo_feed_items to a recyclerview
 * to construct mostly followed tutorial at http://javatechig.com/android/android-recyclerview-example
 */
public class CustomPhotoFeedAdapter extends RecyclerView.Adapter<CustomPhotoFeedAdapter.PhotoViewHolder> {

    private String path = "http://eanderson608.ddns.net/uploads/";
    private ArrayList<Photo> photoArrayList;
    private Context context;
    private boolean isUpvoted;
    private boolean isDownvoted;

    public CustomPhotoFeedAdapter(Context context, ArrayList<Photo> photoArrayList) {
        this.photoArrayList = photoArrayList;
        this.context = context;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {

        protected ImageView imageView;
        protected ImageButton upvoteButton;
        protected ImageButton downvoteButton;

        PhotoViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.photo_image_view);
            this.upvoteButton = (ImageButton) view.findViewById(R.id.upvote_image_button);
            this.downvoteButton = (ImageButton) view.findViewById(R.id.downvote_image_button);

        }
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_photo_feed_item, null);
        PhotoViewHolder viewHolder = new PhotoViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, int position) {
        Photo photo = photoArrayList.get(position);

        isUpvoted = false;
        isDownvoted = false;

        Log.v("onBindViewHolder", photo.getFileName());

        // Download image with picasso
        Picasso.with(context).load(path + photo.getFileName())
                .rotate(90)
                .error(R.drawable.error)
                .placeholder(R.drawable.white_placeholder)
                .into(holder.imageView);

        // handle behavior for upvote
        holder.upvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isUpvoted) {
                    int upvoteColor = context.getResources().getColor(R.color.colorUpvote);
                    holder.upvoteButton.setColorFilter(upvoteColor, PorterDuff.Mode.SRC_ATOP);
                    isUpvoted = true;
                    if (isDownvoted) {
                        holder.downvoteButton.setColorFilter(null);
                        isDownvoted = false;
                    }
                } else {
                    holder.upvoteButton.setColorFilter(null);
                    isUpvoted = false;
                }
            }
        });

        // handle behavior for downvote
        holder.downvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isDownvoted) {
                    int downvoteColor = context.getResources().getColor(R.color.colorDownvote);
                    holder.downvoteButton.setColorFilter(downvoteColor, PorterDuff.Mode.SRC_IN);
                    isDownvoted = true;
                    if (isUpvoted) {
                        holder.upvoteButton.setColorFilter(null);
                        isUpvoted = false;
                    }

                } else {
                    holder.downvoteButton.setColorFilter(null);
                    isDownvoted = false;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != photoArrayList ? photoArrayList.size() : 0);
    }

}
