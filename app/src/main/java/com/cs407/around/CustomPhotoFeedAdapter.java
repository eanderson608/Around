package com.cs407.around;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PermissionGroupInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.zip.Inflater;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Custom adapter that adapts custom_photo_feed_items to a recyclerview
 * to construct mostly followed tutorial at http://javatechig.com/android/android-recyclerview-example
 */
public class CustomPhotoFeedAdapter extends RecyclerView.Adapter<CustomPhotoFeedAdapter.PhotoViewHolder> {

    // the percentage of the screen a photo takes up in the feed
    final double PHOTO_SCREEN_PERCENTAGE = .7;

    private String path = "http://eanderson608.ddns.net/uploads/";
    private ArrayList<Photo> photoArrayList;
    private Context context;
    private boolean photoIsFullscreen;
    private long score;
    private DisplayMetrics metrics;
    private Location currentLocation;
    private User me;
    private PreferencesHelper prefs;
    private ImageView fullscreen;

    public CustomPhotoFeedAdapter(Context context, ArrayList<Photo> photoArrayList, Location location) {
        this.photoArrayList = photoArrayList;
        this.context = context;
        this.currentLocation = location;
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {

        protected ImageView imageView;
        protected ImageView userPhoto;
        protected TextView userName;
        protected TextView photoScore;
        protected ImageButton upvoteButton;
        protected ImageButton downvoteButton;
        protected RelativeLayout topBar;
        protected LinearLayout bottomBar;
        protected TextView photoDistance;
        protected TextView photoTime;
        protected ImageButton moreButton;

        PhotoViewHolder(View view) {
            super(view);

            this.imageView = (ImageView) view.findViewById(R.id.photo_image_view);
            this.upvoteButton = (ImageButton) view.findViewById(R.id.upvote_image_button);
            this.downvoteButton = (ImageButton) view.findViewById(R.id.downvote_image_button);
            this.userPhoto = (ImageView) view.findViewById(R.id.user_photo_image_view);
            this.userName = (TextView) view.findViewById(R.id.user_name_text_view);
            this.photoScore = (TextView)view.findViewById(R.id.photo_score_text_view);
            this.topBar = (RelativeLayout) view.findViewById(R.id.photo_item_top_bar);
            this.bottomBar = (LinearLayout) view.findViewById(R.id.photo_item_bottom_bar);
            this.photoDistance = (TextView) view.findViewById(R.id.photo_distance_text_view);
            this.photoTime = (TextView) view.findViewById(R.id.photo_time_text_view);
            this.moreButton = (ImageButton) view.findViewById(R.id.more_image_button);
        }
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_photo_feed_item, null);
        PhotoViewHolder viewHolder = new PhotoViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, final int position) {
        final Photo photo = photoArrayList.get(position);

        photoIsFullscreen = false;
        score = photo.getScore();

        metrics = Resources.getSystem().getDisplayMetrics();
        final int screenHeight = metrics.heightPixels;
        final int screenWidth = metrics.widthPixels;

        // programatically set image height and width
        final double minImageHeight = screenHeight * PHOTO_SCREEN_PERCENTAGE;
        holder.imageView.setMinimumHeight(((int) minImageHeight));
        holder.imageView.setMaxHeight(((int) minImageHeight));
        holder.imageView.setMinimumWidth(screenWidth);

        // load me from preferences
        prefs = new PreferencesHelper(context);
        String json = prefs.getPreferences("me");
        Log.d("JSON ME BIND", json);
        Gson gson = new Gson();
        me = gson.fromJson(json, User.class);

        // set color of upvote/downvote buttons
        if (me.hasDownvoted(photo.get_id())) { // change appearence of downvote button if it has already been downvoted
            int downvoteColor = context.getResources().getColor(R.color.colorDownvote);
            holder.downvoteButton.setColorFilter(downvoteColor, PorterDuff.Mode.SRC_IN);
        }
        else { holder.downvoteButton.setColorFilter(null); }

        if (me.hasUpvoted(photo.get_id())) { // change appearence of upvote button if it has already been upvoted
            int upvoteColor = context.getResources().getColor(R.color.colorUpvote);
            holder.upvoteButton.setColorFilter(upvoteColor, PorterDuff.Mode.SRC_IN);
        }
        else { holder.upvoteButton.setColorFilter(null); }

        // Download image with picasso
        Picasso.with(context).load(path + photo.getFileName())
                .rotate(90)
                .resize(screenWidth / 2, screenHeight / 2)
                .centerInside()
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

        // set score
        holder.photoScore.setText(String.format("%d", score));

        // Set elapsed time
        holder.photoTime.setText(getElapsedTime(photo.getTime()));

        // set distance from photo
        // parameter for Location constructor is "provider", no idea what that is
        try {
            Location photoLocation = new Location("photo");
            photoLocation.setLongitude(photo.getLocation()[0]);
            photoLocation.setLatitude(photo.getLocation()[1]);
            holder.photoDistance.setText(getDistanceAsString(currentLocation.distanceTo(photoLocation)));
        } catch (NullPointerException e) {
            holder.photoDistance.setText("");
        }


        //TODO enable toggle image to fullscreen
        // toggle photo fullscreen when it is clicked
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                /*
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
                */
            }
        });

        // handle logic for photo upvotes
        // TODO - photo and user score is correctly being calculated and updating to the server
        // TODO - but is incorrectly displayed until feed is refreshed.
        holder.upvoteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String photoId = photo.get_id();
                String userId = photo.getUserId();
                score = photo.getScore();
                int upvoteColor = context.getResources().getColor(R.color.colorUpvote);
                if (me.hasUpvoted(photoId)) { // remove an upvote
                    holder.upvoteButton.setColorFilter(null);
                    me.removeUpvote(photoId);
                    incrementUserScoreRetro(userId, -1);
                    --score;
                    incrementPhotoVoteRetro(photoId, "upvotes", -1);
                } else { // add an upvote
                    holder.upvoteButton.setColorFilter(upvoteColor);
                    ++score;
                    me.addUpvote(photoId);
                    incrementUserScoreRetro(userId, 1);
                    incrementPhotoVoteRetro(photoId, "upvotes", 1);
                    if (me.hasDownvoted(photoId)) { // remove downvote if necessary
                        holder.downvoteButton.setColorFilter(null);
                        ++score;
                        me.removeDownvote(photoId);
                        incrementUserScoreRetro(userId, 1);
                        incrementPhotoVoteRetro(photoId, "downvotes", -1);
                    }
                }
                holder.photoScore.setText(String.format("%d", score));
                Gson gson = new Gson();
                prefs = new PreferencesHelper(context);
                String json = gson.toJson(me);
                prefs.savePreferences("me", json);
                me.updateRetro(context);
            }
        });

        // handle logic for photo downvotes
        // TODO - photo and user score is correctly being calculated and updating to the server
        // TODO - but is incorrectly displayed until feed is refreshed.
        holder.downvoteButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String photoId = photo.get_id();
                String userId = photo.getUserId();
                score = photo.getScore();
                int downvoteColor = context.getResources().getColor(R.color.colorDownvote);
                if (me.hasDownvoted(photoId)) { // remove a downvote
                    holder.downvoteButton.setColorFilter(null);
                    me.removeDownvote(photoId);
                    incrementUserScoreRetro(userId, 1);
                    ++score;
                    incrementPhotoVoteRetro(photoId, "downvotes", -1);
                } else { // add a downvote
                    holder.downvoteButton.setColorFilter(downvoteColor);
                    --score;
                    me.addDownvote(photoId);
                    incrementUserScoreRetro(userId, -1);
                    incrementPhotoVoteRetro(photoId, "downvotes", 1);
                    if (me.hasUpvoted(photoId)) { // remove upvote if necessary
                        holder.upvoteButton.setColorFilter(null);
                        --score;
                        me.removeUpvote(photoId);
                        incrementUserScoreRetro(userId, -1);
                        incrementPhotoVoteRetro(photoId, "upvotes", -1);
                        Log.d("REMOVE UPVOTE", me.getUpvotes().toString());
                    }
                }
                holder.photoScore.setText(String.format("%d", score));
                Gson gson = new Gson();
                prefs = new PreferencesHelper(context);
                String json = gson.toJson(me);
                prefs.savePreferences("me", json);
                me.updateRetro(context);
            }
        });

        // handle behavior for photo options/settings popup menu button
        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu menu = new PopupMenu(context, holder.moreButton);
                menu.getMenuInflater().inflate(R.menu.menu_photo_item_overflow, menu.getMenu());

                // remove delete option if photo does not belong to user
                if (!photo.getUserId().equals(me.getUserId())) menu.getMenu().removeItem(R.id.action_photo_delete);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {

                            // Open Camera
                            case R.id.action_photo_delete:

                                new AlertDialog.Builder(context)
                                        .setCancelable(true)
                                        .setTitle(R.string.delete_photo_alert_dialog)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                // delete the photo
                                                deletePhotoRetro(photo.get_id(), position);

                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                            }
                                        })
                                        .show();

                                break;

                            default:
                                break;
                        }
                        return true;
                    }
                });

                menu.show(); //showing popup menu
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != photoArrayList ? photoArrayList.size() : 0);
    }

    // get the time that has elapsed since photo was taken, returns a formatted string
    private String getElapsedTime(long timePosted) {

        long elapsedMillis = System.currentTimeMillis() - timePosted;

        long elapsedSeconds = elapsedMillis / 1000;
        if (elapsedSeconds < 60) {
            if (elapsedSeconds == 1) return String.format("%d second ago", elapsedSeconds);
            else return String.format("%d seconds ago", elapsedSeconds);
        }

        long elapsedMinutes = elapsedSeconds / 60;
        if (elapsedMinutes < 60) {
            if (elapsedMinutes == 1) return String.format("%d minute ago", elapsedMinutes);
            else return String.format("%d minutes ago", elapsedMinutes);
        }

        long elapsedHours = elapsedMinutes / 60;
        if (elapsedHours < 24) {
            if (elapsedHours == 1) return String.format("%d hour ago", elapsedHours);
            else return String.format("%d hours ago", elapsedHours);
        }

        long elapsedDays = elapsedHours / 24;
        if (elapsedDays == 1) return String.format("%d day ago", elapsedDays);
        else return String.format("%d days ago", elapsedDays);
    }

    // takes a distance as a float as input and returns a formatted string
    // TODO provide an option to display distances in metric?
    private String getDistanceAsString(float distanceInMeters) {
        float distanceInFeet = distanceInMeters * ((float) 3.28084);
        if (distanceInFeet < 100) {
            return String.format("less than 100 feet away", distanceInFeet);
        }
        else if (distanceInFeet < 1000) {
            if (distanceInFeet == 1) return String.format("%.0f foot away", distanceInFeet);
            else return String.format("%.0f feet away", distanceInFeet);
        } else {
            float distanceInMiles = distanceInFeet / 5280;
            if (Math.round(distanceInMiles * 100.0) / 100.0 == 1)
                return String.format("%.0f mile away", distanceInMiles);
            if (distanceInMiles < 3) return String.format("%.2f miles away", distanceInMiles);
            return String.format("%.0f miles away", distanceInMiles);
        }
    }

    private void incrementPhotoVoteRetro(String id, String field, int amount) {

        PhotoClient client = ServiceGenerator.createService(PhotoClient.class);
        Call<ResponseBody> call = client.incrementPhotoVote(id, field, amount);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccess()) {
                    Log.d("HTTP_GET_RESPONSE", response.raw().toString());

                } else {
                    // error response, no access to resource?
                    Log.d("HTTP_GET_RESPONSE", response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.d("Error", t.getMessage());
                Toast.makeText(context, R.string.no_connection_to_server, Toast.LENGTH_LONG).show();

            }
        });
    }

    private void incrementUserScoreRetro(String userId, int amount) {

        UserClient client = ServiceGenerator.createService(UserClient.class);
        Call<ResponseBody> call = client.incrementUserScore(userId, amount);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccess()) {
                    Log.d("HTTP_GET_RESPONSE", response.raw().toString());

                } else {
                    // error response, no access to resource?
                    Log.d("HTTP_GET_RESPONSE", response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.d("Error", t.getMessage());
                Toast.makeText(context, R.string.no_connection_to_server, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deletePhotoRetro(String photoId, final int position) {

        PhotoClient client = ServiceGenerator.createService(PhotoClient.class);
        Call<ResponseBody> call = client.deletePhoto(photoId);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccess()) {
                    Log.d("HTTP_GET_RESPONSE", response.raw().toString());
                    photoArrayList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "photo deleted", Toast.LENGTH_LONG).show();

                } else {
                    // error response, no access to resource?
                    Log.d("HTTP_GET_RESPONSE", response.raw().toString());
                    Toast.makeText(context, "could not delete photo", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.d("Error", t.getMessage());
                Toast.makeText(context, R.string.no_connection_to_server, Toast.LENGTH_LONG).show();
            }
        });
    }
}
