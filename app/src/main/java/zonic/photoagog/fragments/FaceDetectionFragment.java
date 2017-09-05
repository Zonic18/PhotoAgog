package zonic.photoagog.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import zonic.photoagog.R;
import zonic.photoagog.adapter.FaceAdapter;
import zonic.photoagog.adapter.LabelAdapter;
import zonic.photoagog.utils.InteractionListener;
import zonic.photoagog.utils.PackageManagerUtils;

import static zonic.photoagog.R.id.rv;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FaceDetectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FaceDetectionFragment extends Fragment {
    private static final String IMAGE_URI_STRING = "ImageUri";
    private static final String ANDROID_CERT_HEADER = "Zonic-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "Zonic-Android-Package";
    InteractionListener interaction;
    private String mImageUri;
    private View view;
    private List<FaceAnnotation> faceAnnotations;
    private RecyclerView rvface;
    private TextView tvfaceAlert;
    private ProgressBar pbface;

    public FaceDetectionFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FaceDetectionFragment newInstance(String uri) {
        FaceDetectionFragment fragment = new FaceDetectionFragment();
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

        view = inflater.inflate(R.layout.fragment_face_detection, container, false);
        rvface = (RecyclerView) view.findViewById(R.id.rvFace);
        tvfaceAlert = (TextView) view.findViewById(R.id.tvfaceAlert);
        uploadImage(Uri.parse(mImageUri));
        pbface = (ProgressBar) view.findViewById(R.id.pbFace);
        pbface.animate().setInterpolator(new AnticipateOvershootInterpolator()).setDuration(1000).translationYBy(250);
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
                //Log.d("hahah",result.toLowerCase());
                if (faceAnnotations != null && faceAnnotations.size() != 0) {
                    FaceAdapter adapter = new FaceAdapter(getActivity(), faceAnnotations);
                    rvface.setLayoutManager(new LinearLayoutManager(getContext()));
                    rvface.setAdapter(adapter);
                    interaction.sendData(faceAnnotations,"face");
                } else {
                    tvfaceAlert.setVisibility(View.VISIBLE);
                }
                pbface.setVisibility(View.GONE);
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
                                Feature faceDetection = new Feature();
                                faceDetection.setType("FACE_DETECTION");
                                faceDetection.setMaxResults(15);
                                add(faceDetection);
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
                    e.printStackTrace();
                    snackbar.show();
                } catch (IOException e) {
                    Snackbar snackbar = Snackbar.make(view, "failed to make request becauge of other IOException" +
                            e.getMessage(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    e.printStackTrace();
                }
                return "Cloud Vision API request failed";

            }


        }.execute();
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        faceAnnotations = response.getResponses().get(0).getFaceAnnotations();

        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        interaction = (InteractionListener) getActivity();
    }

}
