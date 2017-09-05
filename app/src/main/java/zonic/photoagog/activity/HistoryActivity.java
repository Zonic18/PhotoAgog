package zonic.photoagog.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import zonic.photoagog.R;
import zonic.photoagog.adapter.HistoryAdapter;
import zonic.photoagog.contract.History;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        List<History> data=fillWithData();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv);
        HistoryAdapter adapter = new HistoryAdapter(data,getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        recyclerView.setItemAnimator(itemAnimator);
    }

    private List<History> fillWithData() {
        List<History> data=new ArrayList<>();
        data.add(new History(R.drawable.image01));
        data.add(new History(R.drawable.image02));
        data.add(new History(R.drawable.image03));
        data.add(new History(R.drawable.image04));
        data.add(new History(R.drawable.image05));
        data.add(new History(R.drawable.image06));
        return data;
    }


}
