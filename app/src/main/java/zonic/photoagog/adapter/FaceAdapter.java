package zonic.photoagog.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.services.vision.v1.model.FaceAnnotation;

import java.util.List;

import zonic.photoagog.R;

/**
 * Created by maithani on 04-09-2017.
 */

public class FaceAdapter extends RecyclerView.Adapter<FaceAdapter.Faceholder> {
    Activity activity;
    List<FaceAnnotation> faceList;

    public FaceAdapter(Activity activity, List<FaceAnnotation> faceList) {
        this.activity = activity;
        this.faceList = faceList;
    }

    @Override
    public Faceholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.face_detection, parent, false);
        return new FaceAdapter.Faceholder(view);
    }

    @Override
    public void onBindViewHolder(Faceholder holder, int position) {
        FaceAnnotation face=faceList.get(position);
        holder.tvAnger.setText(face.getAngerLikelihood());
        holder.tvExposed.setText(face.getUnderExposedLikelihood());
        holder.tvHeadwear.setText(face.getHeadwearLikelihood());
        holder.tvJoy.setText(face.getJoyLikelihood());
        holder.tvSorrow.setText(face.getSorrowLikelihood());
        holder.tvSurprise.setText(face.getSurpriseLikelihood());
        holder.tvBlurred.setText(face.getBlurredLikelihood());
        holder.pbConfidence.setProgress((int) Math.ceil(face.getDetectionConfidence()*100));
        holder.tvConfidence.setText(String.valueOf((int) Math.ceil(face.getDetectionConfidence()*100))+"%");
    }

    @Override
    public int getItemCount() {
        return faceList.size();
    }

    public class Faceholder extends RecyclerView.ViewHolder{

        public final TextView tvAnger;
        public final TextView tvJoy;
        public final TextView tvSorrow;
        public final TextView tvSurprise;
        public final TextView tvExposed;
        public final TextView tvHeadwear;
        public final ProgressBar pbConfidence;
        public final TextView tvBlurred;
        public final TextView tvConfidence;

        public Faceholder(View itemView) {
            super(itemView);

            tvAnger = (TextView) itemView.findViewById(R.id.tvAnger);
            tvConfidence = (TextView) itemView.findViewById(R.id.tvConfidence);
            tvJoy = (TextView) itemView.findViewById(R.id.tvJoy);
            tvSorrow = (TextView) itemView.findViewById(R.id.tvSorrow);
            tvSurprise = (TextView) itemView.findViewById(R.id.tvSurprise);
            tvExposed = (TextView) itemView.findViewById(R.id.tvExposed);
            tvHeadwear = (TextView) itemView.findViewById(R.id.tvHeadwear);
            pbConfidence = (ProgressBar) itemView.findViewById(R.id.pbConfidence);
            tvBlurred = (TextView) itemView.findViewById(R.id.tvBlurred);
        }
    }
}
