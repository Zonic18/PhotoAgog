package zonic.photoagog.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.List;

import zonic.photoagog.contract.History;
import zonic.photoagog.R;

/**
 * Created by maithani on 25-08-2017.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {
    public List<History> list;
    Context context;

    public HistoryAdapter(List<History> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.imageView.setImageResource(list.get(position).getDrawable());
        animate(holder);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    //insert a new item to recycler view
    public void insert(History data, int position){
        list.add(position, data);
        notifyItemInserted(position);
    }
    public void remove(History data){
        int position=list.indexOf(data);
        list.remove(position);
        notifyItemRemoved(position);
    }
    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.shake);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
    }

    public class Holder extends RecyclerView.ViewHolder {

        CardView cv;
        ImageView imageView;

        Holder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cardView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }
}
