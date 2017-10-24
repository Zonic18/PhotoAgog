package zonic.photoagog.adapter;

import android.app.Activity;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.List;

import zonic.photoagog.R;

/**
 * Created by Zonic on 24-10-2017.
 */

public class TextAdapter extends  RecyclerView.Adapter<TextAdapter.Holder>{
    Activity activity;
    List<EntityAnnotation> list;

    public TextAdapter(Activity activity, List<EntityAnnotation> list) {
        this.activity = activity;
        this.list = list;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_detection, parent, false);
        return new TextAdapter.Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        EntityAnnotation text=list.get(position);
        holder.tvText.setText(text.getDescription());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        public final TextView tvText;

        public Holder(View itemView) {
            super(itemView);
            tvText = (TextView) itemView.findViewById(R.id.tvText);
        }
    }
}
