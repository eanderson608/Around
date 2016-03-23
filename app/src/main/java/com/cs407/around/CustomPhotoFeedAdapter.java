package com.cs407.around;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PermissionGroupInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Custom adapter that adapts custom_photo_feed_items to a recyclerview
 * to construct mostly followed tutorial at http://javatechig.com/android/android-recyclerview-example
 */
public class CustomPhotoFeedAdapter extends RecyclerView.Adapter<CustomPhotoFeedAdapter.PhotoViewHolder> {

    // the percentage of the screen a photo takes up in the feed
    final double PHOTO_SCREEN_PERCENTAGE = .5;

    private String path = "http://eanderson608.ddns.net/uploads/";
    private ArrayList<Photo> photoArrayList;
    private Context context;
    private boolean isUpvoted;
    private boolean isDownvoted;
    private boolean photoIsFullscreen;
    private long score;
    DisplayMetrics metrics;

    public CustomPhotoFeedAdapter(Context context, ArrayList<Photo> photoArrayList) {
        this.photoArrayList = photoArrayList;
        this.context = context;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {

        protected ImageView imageView;
        protected ImageView userPhoto;
        protected TextView userName;
        protected TextView photoScore;
        protected ImageButton upvoteButton;
        protected ImageButton downvoteButton;
        protected LinearLayout topBar;
        protected LinearLayout bottomBar;

        PhotoViewHolder(View view) {
            super(view);

            this.imageView = (ImageView) view.findViewById(R.id.photo_image_view);
            this.upvoteButton = (ImageButton) view.findViewById(R.id.upvote_image_button);
            this.downvoteButton = (ImageButton) view.findViewById(R.id.downvote_image_button);
            this.userPhoto = (ImageView) view.findViewById(R.id.user_photo_image_view);
            this.userName = (TextView) view.findViewById(R.id.user_name_text_view);
            this.photoScore = (TextView)view.findViewById(R.id.photo_score_text_view);
            this.topBar = (LinearLayout) view.findViewById(R.id.photo_item_top_bar);
            this.bottomBar = (LinearLayout) view.findViewById(R.id.photo_item_bottom_bar);
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
        photoIsFullscreen = false;
        score = photo.getUpvotes() - photo.getDownvotes();

        metrics = Resources.getSystem().getDisplayMetrics();
        final int screenHeight = metrics.heightPixels;
        final int screenWidth = metrics.widthPixels;

        // programatically set image height and width
        final double minImageHeight = screenHeight * PHOTO_SCREEN_PERCENTAGE;
        holder.imageView.setMinimumHeight(((int) minImageHeight));
        holder.imageView.setMaxHeight(((int) minImageHeight));
        holder.imageView.setMinimumWidth(screenWidth);

        // Download image with picasso
        Picasso.with(context).load(path + photo.getFileName())
                .rotate(90)
                .error(R.drawable.error)
                .placeholder(R.drawable.grey_placeholder)
                .into(holder.imageView);

        // Download users profile photo with Picasso
        Picasso.with(context).load(path + photo.getUserId() + ".jpg")
                .error(R.drawable.error)
                .placeholder(R.drawable.grey_square_placeholder)
                .into(holder.userPhoto);

        // Set user name field
        holder.userName.setText(photo.getUserName());

        // Calculate and set score
        holder.photoScore.setText(String.format("%d", score));


        /*
        //TODO enable toggle image to fullscreen
        // toggle photo fullscreen when it is clicked
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!photoIsFullscreen) {
                    holder.topBar.setVisibility(LinearLayout.GONE);
                    holder.bottomBar.setVisibility(LinearLayout.GONE);
                    holder.imageView.setMinimumHeight(screenHeight);
                    holder.imageView.setMaxHeight(screenHeight);
                    holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    photoIsFullscreen = true;
                } else {
                    holder.topBar.setVisibility(LinearLayout.VISIBLE);
                    holder.bottomBar.setVisibility(LinearLayout.VISIBLE);
                    holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    holder.imageView.setMinimumHeight((int) minImageHeight);
                    holder.imageView.setMaxHeight(((int) minImageHeight));

                    photoIsFullscreen = false;
                }


            }
        });
        */

        // handle behavior for upvote
        //TODO add logic to update score on server
        holder.upvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isUpvoted) {
                    int upvoteColor = context.getResources().getColor(R.color.colorUpvote);
                    holder.upvoteButton.setColorFilter(upvoteColor, PorterDuff.Mode.SRC_IN);
                    score++;
                    holder.photoScore.setText(String.format("%d", score));
                    isUpvoted = true;
                    if (isDownvoted) {
                        holder.downvoteButton.setColorFilter(null);
                        score++;
                        holder.photoScore.setText(String.format("%d", score));
                        isDownvoted = false;
                    }
                } else {
                    holder.upvoteButton.setColorFilter(null);
                    score--;
                    holder.photoScore.setText(String.format("%d", score));
                    isUpvoted = false;
                }
            }
        });

        // handle behavior for downvote
        //TODO add logic to update score on server
        holder.downvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isDownvoted) {
                    int downvoteColor = context.getResources().getColor(R.color.colorDownvote);
                    holder.downvoteButton.setColorFilter(downvoteColor, PorterDuff.Mode.SRC_IN);
                    score--;
                    holder.photoScore.setText(String.format("%d", score));
                    isDownvoted = true;
                    if (isUpvoted) {
                        holder.upvoteButton.setColorFilter(null);
                        score--;
                        holder.photoScore.setText(String.format("%d", score));
                        isUpvoted = false;
                    }

                } else {
                    holder.downvoteButton.setColorFilter(null);
                    score++;
                    holder.photoScore.setText(String.format("%d", score));
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
