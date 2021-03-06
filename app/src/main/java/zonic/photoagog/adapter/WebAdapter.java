package zonic.photoagog.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.services.vision.v1.model.WebEntity;
import com.google.api.services.vision.v1.model.WebImage;

import java.util.List;

import zonic.photoagog.R;

/**
 * Created by maithani on 04-09-2017.
 */

public class WebAdapter extends RecyclerView.Adapter<WebAdapter.WebHolder> {
    Activity activity;
    List<WebImage> urlList;

    public WebAdapter(Activity activity, List<WebImage> urlList) {
        this.activity = activity;
        this.urlList = urlList;
    }

    @Override
    public WebHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.web_detection, parent, false);
        return new WebAdapter.WebHolder(view);
    }

    @Override
    public void onBindViewHolder(WebHolder holder, int position) {
        final WebImage webImage = urlList.get(position);
        holder.tvUrl.setText(webImage.getUrl());
        holder.tvScore.setText(String.valueOf(webImage.getScore()));
        holder.tvUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage("https://www.google.com/search?q=+" +
                        webImage.getUrl()  +
                        "+logos&tbm=isch");
            }
        });
    }
    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
        else{
            Toast.makeText(activity, "why not working", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }

    public class WebHolder extends RecyclerView.ViewHolder{

        public final TextView tvUrl;
        public final TextView tvScore;

        public WebHolder(View itemView) {
            super(itemView);
            tvUrl = (TextView) itemView.findViewById(R.id.tvUrl);
            tvScore = (TextView) itemView.findViewById(R.id.tvScore);

        }
    }
}
