package zonic.photoagog.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import zonic.photoagog.R;
import zonic.photoagog.adapter.LabelAdapter;
import zonic.photoagog.utils.PackageManagerUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LabelDetectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LabelDetectionFragment extends Fragment {
    private static final String ANDROID_CERT_HEADER = "Zonic-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "Zonic-Android-Package";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String IMAGE_URI_STRING = "ImageUri";


    // TODO: Rename and change types of parameters
    private String mImageUri;
    private View view;
    private List<EntityAnnotation> labels;
    private TextView tvLabel;
    private ProgressBar pbLoader;
    private RecyclerView rvLabel;
    private TextView tvAlert;
    private SharedPreferences settings;


    public LabelDetectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uri Parameter 1.
     * @return A new instance of fragment LabelDetectionFragment.
     */
    public static LabelDetectionFragment newInstance(String uri) {
        LabelDetectionFragment fragment = new LabelDetectionFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URI_STRING, uri);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImageUri = getArguments().getString(IMAGE_URI_STRING);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_label_detection, container, false);
        rvLabel = (RecyclerView) view.findViewById(R.id.rvLabel);
        pbLoader = (ProgressBar) view.findViewById(R.id.pbLoader);
        tvAlert = (TextView) view.findViewById(R.id.tvAlert);
        settings=getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        uploadImage(Uri.parse(mImageUri));
        pbLoader.animate().setInterpolator(new AnticipateOvershootInterpolator()).setDuration(1000).translationYBy(250);
        return view;
    }

    private void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri),
                                1200);

                callCloudVision(bitmap);


            } catch (IOException e) {
                Snackbar snackbar = Snackbar.make(view, R.string.image_picker_error, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } else {
            Snackbar snackbar = Snackbar.make(view, R.string.image_picker_error, Snackbar.LENGTH_LONG);
            snackbar.show();
        }

    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private void callCloudVision(final Bitmap bitmap) {
        new AsyncTask<Object, Void, String>() {
            @Override
            protected void onPostExecute(String result) {
                if (labels!=null&&labels.size()!=0){

                    LabelAdapter adapter=new LabelAdapter(getActivity(),labels,settings);
                rvLabel.setAdapter(adapter);
                rvLabel.setLayoutManager(new LinearLayoutManager(getContext()));
                }
                else{
                  tvAlert.setVisibility(View.VISIBLE);
                }


                pbLoader.setVisibility(View.GONE);
            }

            @Override
            protected String doInBackground(Object... params) {
                try {

                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                    VisionRequestInitializer requestInitializer = new VisionRequestInitializer(getString(R.string.cloud_api_key)) {
                        @Override
                        protected void initializeVisionRequest(VisionRequest<?> request) throws IOException {
                            super.initializeVisionRequest(request);
                            String packageName = getActivity().getPackageName();
                            request.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);
                            String sig = PackageManagerUtils.getSignature(getActivity().getPackageManager(), packageName);
                            request.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                        }
                    };
                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);
                    Vision vision = builder.build();
                    BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {
                        {
                            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                            // Add the image
                            Image base64EncodedImage = new Image();
                            // Convert the bitmap to a JPEG
                            // Just in case it's a format that Android understands but Cloud Vision
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                            byte[] imageBytes = byteArrayOutputStream.toByteArray();

                            // Base64 encode the JPEG
                            base64EncodedImage.encodeContent(imageBytes);
                            annotateImageRequest.setImage(base64EncodedImage);

                            // add the features we want
                            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                                Feature labelDetection = new Feature();
                                labelDetection.setType("LABEL_DETECTION");
                                labelDetection.setMaxResults(15);
                                add(labelDetection);
                            }});
                            add(annotateImageRequest);
                        }
                    });
                    Vision.Images.Annotate annotateRequest;
                    annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                    annotateRequest.setDisableGZipContent(true);

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);
                } catch (GoogleJsonResponseException e) {
                    Snackbar snackbar = Snackbar.make(view, "failed to make request because" +
                            e.getContent(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                } catch (IOException e) {
                    Snackbar snackbar = Snackbar.make(view, "failed to make request becauge of other IOException" +
                            e.getMessage(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                return "Cloud Vision API request failed";

            }


        }.execute();
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";

        labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription());
                message += "\n";
            }
        } else {
            message += "nothing";
        }

        return message;

    }
}
