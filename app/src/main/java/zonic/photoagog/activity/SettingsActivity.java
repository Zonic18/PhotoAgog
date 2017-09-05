package zonic.photoagog.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.common.io.LineReader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import zonic.photoagog.R;


public class SettingsActivity extends BaseActivity {

    private FirebaseUser user;
    private String email;
    private String displayName;
    private Uri photoUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        user = FirebaseAuth.getInstance().getCurrentUser();
        displayName = user.getDisplayName();
        photoUrl = user.getPhotoUrl();
        email = user.getEmail();
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.container);
        ImageView profileImage = (ImageView) findViewById(R.id.profileImage);
        Glide.with(this)
                .load(photoUrl)
                .into(profileImage);
        TextView tvDisplayName = (TextView) findViewById(R.id.tvDisplayName);
        TextView tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvEmail.setText(email);
        tvDisplayName.setText(displayName);
        Switch switchTTs = (Switch) findViewById(R.id.switchTTs);
        Switch switchInternet = (Switch) findViewById(R.id.switchInternet);
        final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        final SharedPreferences.Editor edit = settings.edit();
        switchTTs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Snackbar snackbar = Snackbar.make(linearLayout, getString(R.string.ttsOn), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    edit.putBoolean("ttsOn", true);
                    edit.apply();

                } else {
                    Snackbar snackbar = Snackbar.make(linearLayout, getString(R.string.ttsoff), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    edit.putBoolean("ttsOn", false);
                    edit.apply();
                }
            }
        });
        switchInternet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Snackbar snackbar = Snackbar.make(linearLayout, getString(R.string.icOn), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    edit.putBoolean("internetOn", true);
                    edit.apply();
                } else {
                    Snackbar snackbar = Snackbar.make(linearLayout, getString(R.string.icOff), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    edit.putBoolean("internetOn", false);
                    edit.apply();
                }
            }
        });
        CardView cProfile = (CardView) findViewById(R.id.cProfile);
        cProfile.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                showAlert("Log Out", "Do You Really Want To Log Out", "log Out", "Cancel", 0);
                return true;
            }
        });
        TextView tvFeedback = (TextView) findViewById(R.id.tvFeedback);
        findViewById(R.id.cFeedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                LayoutInflater inflater = SettingsActivity.this.getLayoutInflater();
                email = user.getEmail();
                View view = inflater.inflate(R.layout.feedback_dialog, null);
                EditText etFeEmail = (EditText) view.findViewById(R.id.etFeEmail);
                final EditText feedback = (EditText) view.findViewById(R.id.etContent);
                builder.setView(view);
                etFeEmail.setText(email);
                builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] adrress = {"rishabh33.maithani@gmail.com"};
                        composeEmail(adrress, "Name: " +
                                user.getDisplayName() +
                                "\n\nMessage:" +
                                feedback.getText(), "send Feedback");
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create();
                builder.show();
            }
        });
    }

    public void composeEmail(String[] addresses, String feedback, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, feedback);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


}
