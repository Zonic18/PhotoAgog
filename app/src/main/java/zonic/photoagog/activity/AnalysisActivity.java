package zonic.photoagog.activity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import biz.laenger.android.vpbs.BottomSheetUtils;
import devlight.io.library.ntb.NavigationTabBar;
import zonic.photoagog.R;
import zonic.photoagog.fragments.FaceDetectionFragment;
import zonic.photoagog.fragments.LabelDetectionFragment;
import zonic.photoagog.fragments.SafeSearchDetectionFragment;
import zonic.photoagog.fragments.TextDetectionFragment;
import zonic.photoagog.fragments.WebDetectionFragment;
import zonic.photoagog.utils.InteractionListener;


public class AnalysisActivity extends AppCompatActivity implements InteractionListener {
    private static final String ANDROID_CERT_HEADER = "Zonic-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "Zonic-Android-Package";
    private String[] colors = {"#FFFF4081"};
    private Uri imageuri;
    private TextView tvPopLabel;
    private ImageView img;
    private CoordinatorLayout view;
    private SafeSearchAnnotation safeSearchAnnotation;
    private List<EntityAnnotation> labelAnnotations;
    private JSONObject label;
    private JSONObject face;
    private List<FaceAnnotation> faceAnnotations;
    private String jsonLabel;
    private ViewPager viewPager;
    private NavigationTabBar navigationTabBar;
    private boolean state;
    private ProgressBar pbDataUpload;
    private Button btData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        view = (CoordinatorLayout) findViewById(R.id.containerAnalysis);
        imageuri = getIntent().getData();
        img = (ImageView) findViewById(R.id.img);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.bottomsheet);
        Glide.with(this).load(imageuri).into(img);
        pbDataUpload = (ProgressBar) findViewById(R.id.pbDataUpload);
        btData = (Button) findViewById(R.id.btData);
        navigationTabBar = setNavigationTabBar();
        viewPager = (ViewPager) findViewById(R.id.bottom_sheet_viewpager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            viewPager.setOffscreenPageLimit(4);
        }
        navigationTabBar.setViewPager(viewPager, 0);
        navigationTabBar.setBehaviorEnabled(true);
        BottomSheetUtils.setupViewPager(viewPager);

    }

    @NonNull
    private NavigationTabBar setNavigationTabBar() {
        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_tag_faces_black_24dp),
                        Color.parseColor(colors[0])
                ).title("Faces")
                        .badgeTitle("Faces")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.web),
                        Color.parseColor(colors[0])
                ).title("Web")
                        .badgeTitle("Web")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.labels),
                        Color.parseColor(colors[0])
                ).title("Label")
                        .badgeTitle("Label")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_text_fields_black_24dp),
                        Color.parseColor(colors[0])
                ).title("Text")
                        .badgeTitle("Text")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.safesearch1),
                        Color.parseColor(colors[0])
                ).title("Safe Search")
                        .badgeTitle("Safe Search")
                        .build()
        );
        navigationTabBar.setModels(models);
        return navigationTabBar;
    }

    @Override
    public void hasDataLoaded(boolean state) {
        this.state = state;
        //enable or disable it
        pbDataUpload.setVisibility(View.GONE);
        btData.setVisibility(View.VISIBLE);

    }

    @Override
    public void sendData(Object data) {
        //simple data
    }

    @Override
    public void sendData(List<?> list, String type) {
        switch (type) {
            case "face":
                applyOverlayOnFaces((List<FaceAnnotation>) list);
                break;
            case "web":
                break;
        }
    }

    private void applyOverlayOnFaces(List<FaceAnnotation> list) {

    }

    public class MyAdapter extends FragmentStatePagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                default:
                    return FaceDetectionFragment.newInstance(imageuri.toString());
                case 1:
                    return WebDetectionFragment.newInstance(imageuri.toString());
                case 2:
                    return LabelDetectionFragment.newInstance(imageuri.toString());
                case 3:
                    return TextDetectionFragment.newInstance(imageuri.toString());
                case 4:
                    return SafeSearchDetectionFragment.newInstance(imageuri.toString());
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}
