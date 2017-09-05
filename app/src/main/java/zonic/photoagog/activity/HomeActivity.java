package zonic.photoagog.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.api.services.vision.v1.model.Image;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapzen.speakerbox.Speakerbox;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import zonic.photoagog.R;
import zonic.photoagog.utils.AlbumStorageDirFactory;
import zonic.photoagog.utils.BaseAlbumDirFactory;
import zonic.photoagog.utils.FroyoAlbumDirFactory;


public class HomeActivity extends BaseActivity {

    private static final int REQUEST_CODE_TAKE_PHOTO = 21;
    private static final int RESULT_LOAD_IMAGE = 211;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    File mCurrentPhotoFile;
    Uri mCurrentPhotoUri;
    private boolean isPermitted = false;
    private File imagefile;
    private String mCurrentPhotoPath;
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    private File f;
    private Uri contentUri;
    private SharedPreferences settings;
    private boolean internetOn;
    private boolean ttsOn;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_signout:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                Intent tomain = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(tomain);
                finish();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homemenu, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermitted = true;
            } else {
                isPermitted = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 23);
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isPermitted = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 23);
        setContentView(R.layout.activity_home);
        settings = getSharedPreferences("settings", MODE_PRIVATE);
        showPreference();


        if (internetOn) {
            //checkConnectivity
        }
        CardView cvHistory = (CardView) findViewById(R.id.cvHistory);
        CardView cvSettings = (CardView) findViewById(R.id.cvSettings);
        CardView cvGallery = (CardView) findViewById(R.id.cvGallery);
        CardView cvCamera = (CardView) findViewById(R.id.cvCamera);
        ImageView imgHistory= (ImageView) findViewById(R.id.imgHistory);
        ImageView imgGallery= (ImageView) findViewById(R.id.imgGallery);
        ImageView imgSettings= (ImageView) findViewById(R.id.imgSettings);
        ImageView imgCamera= (ImageView) findViewById(R.id.imgCamera);
        Glide.with(this).load(R.drawable.camera).into(imgCamera);
        Glide.with(this).load(R.drawable.gallery).into(imgGallery);
        Glide.with(this).load(R.drawable.history).into(imgHistory);
        Glide.with(this).load(R.drawable.settings).into(imgSettings);

        cvHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
        cvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermitted) {
                    dispatchTakePictureIntent(REQUEST_CODE_TAKE_PHOTO);
                } else {
                    Toast.makeText(context, "Permission Not Granted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cvSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                HomeActivity.this.overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
            }
        });
        cvGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });
    }


    private void showPreference() {
        ttsOn = settings.getBoolean("ttsOn", true);
        internetOn = settings.getBoolean("internetOn", true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent;
            switch (requestCode) {
                case RESULT_LOAD_IMAGE:
                    if (ttsOn) {
                        //give a audio cue
                    }
                    Uri selectedImage = data.getData();
                    intent = new Intent(HomeActivity.this, AnalysisActivity.class);
                    intent.setData(selectedImage);
                    startActivity(intent);
                    break;
                case REQUEST_CODE_TAKE_PHOTO:
                    galleryAddPic();
                    intent = new Intent(HomeActivity.this, AnalysisActivity.class);
                    intent.setData(contentUri);
                    startActivity(intent);
                    break;
            }
        } else {
            message("not ok");
        }
    }


    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        f = null;

        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

            } else {
                File file = new File(Uri.fromFile(f).getPath());
                Uri photoUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            }

        } catch (IOException e) {
            e.printStackTrace();
            mCurrentPhotoPath = null;
        }
        startActivityForResult(takePictureIntent, actionCode);
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        message(" failed to create directory");
                        return null;
                    }
                }
            }
        }
        return storageDir;
    }

    private File setUpPhotoFile() throws IOException {

        imagefile = createImageFile();
        mCurrentPhotoPath = imagefile.getAbsolutePath();

        return imagefile;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


}

