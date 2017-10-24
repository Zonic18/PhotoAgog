package zonic.photoagog.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.services.vision.v1.model.WebEntity;

import java.util.List;
import java.util.Locale;

import zonic.photoagog.R;

/**
 * Created by maithani on 09-09-2017.
 */

public class WebLabelAdapter extends RecyclerView.Adapter<WebLabelAdapter.LabelHolder> {
    Activity activity;
    List<WebEntity> list;

    public WebLabelAdapter(Activity activity, List<WebEntity> list) {
        this.activity = activity;
        this.list = list;
    }

    @Override
    public LabelHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.labels_web, parent, false);
        return new WebLabelAdapter.LabelHolder(view);
    }

    @Override
    public void onBindViewHolder(LabelHolder holder, int position) {
        final WebEntity entity=list.get(position);
        holder.tvLabel.setText(entity.getDescription());
        holder.tvScore.setText(String.format(Locale.US,"%.2f",entity.getScore()));
        holder.tvLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage("https://www.google.com/search?q=+" +
                        entity.getDescription()  +
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
        return list.size();
    }

    public class LabelHolder extends RecyclerView.ViewHolder{

        public final TextView tvLabel;
        public final TextView tvScore;

        public LabelHolder(View itemView) {
            super(itemView);
            tvLabel = (TextView) itemView.findViewById(R.id.tvLabelWeb);
            tvScore = (TextView) itemView.findViewById(R.id.tvLabelScore);
        }
    }

}
