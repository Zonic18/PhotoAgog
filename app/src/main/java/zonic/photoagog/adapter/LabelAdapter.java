package zonic.photoagog.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.List;
import java.util.Locale;

import zonic.photoagog.R;

/**
 * Created by maithani on 01-09-2017.
 */

public class LabelAdapter extends RecyclerView.Adapter<LabelAdapter.Holder> {

    Activity activity;
    List<EntityAnnotation> lableList;

    public LabelAdapter(Activity activity, List<EntityAnnotation> lableList) {
        this.activity = activity;
        this.lableList = lableList;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.label_detection, parent, false);
        return new LabelAdapter.Holder(view);

    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        EntityAnnotation label = lableList.get(position);
        holder.tvScore.setText(String.valueOf((int) Math.ceil(label.getScore()*100))+"%");
        holder.tvLabelName.setText(label.getDescription());
        holder.pbScore.setProgress((int) Math.ceil(label.getScore()*100));
        //holder.pbScore.setProgress(Integer.parseInt(String.valueOf(label.getScore())));

    }

    @Override
    public int getItemCount() {
        return lableList.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        public final ProgressBar pbScore;
        public final TextView tvLabelName;
        public final TextView tvScore;

        public Holder(View itemView) {
            super(itemView);
            tvLabelName = (TextView) itemView.findViewById(R.id.tvLabelName);

            tvScore = (TextView) itemView.findViewById(R.id.tvScore);
            pbScore= (ProgressBar) itemView.findViewById(R.id.pbScore);

        }
    }
}
