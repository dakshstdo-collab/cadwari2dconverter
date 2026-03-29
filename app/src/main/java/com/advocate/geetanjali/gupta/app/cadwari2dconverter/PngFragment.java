package com.advocate.geetanjali.gupta.app.cadwari2dconverter;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import java.io.File;
import java.util.concurrent.Executors;

public class PngFragment extends Fragment {

    private static final String ARG_PATH = "png_path";

    public static PngFragment newInstance(String filePath) {
        PngFragment f = new PngFragment();
        Bundle b = new Bundle();
        b.putString(ARG_PATH, filePath);
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_png, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SubsamplingScaleImageView imageView = view.findViewById(R.id.imageView);
        ProgressBar progress = view.findViewById(R.id.progressPng);

        // RGB_565 = half memory of ARGB_8888, 2x faster decode
        // Perfect for CAD: black lines on white, no transparency needed
        imageView.setBitmapDecoderClass(com.davemorrissey.labs.subscaleview.decoder.SkiaImageDecoder.class);
        imageView.setPreferredBitmapConfig(Bitmap.Config.RGB_565);

        // Larger tiles = fewer decode operations during zoom pan
        imageView.setTileBackgroundColor(android.graphics.Color.WHITE);

        // Use 2 threads for tile decoding — smoother on multi-core
        imageView.setExecutor(Executors.newFixedThreadPool(2));

        // Scale settings
        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
        imageView.setMaxScale(32f);
        imageView.setMinScale(0.1f);

        // Smooth pan/zoom animation
        imageView.setEagerLoadingEnabled(true);  // pre-load adjacent tiles

        imageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
            @Override
            public void onReady() {
                progress.setVisibility(View.GONE);
            }
            @Override
            public void onImageLoadError(Exception e) {
                progress.setVisibility(View.GONE);
            }
        });

        String path = getArguments() != null ? getArguments().getString(ARG_PATH) : null;
        if (path != null && new File(path).exists()) {
            imageView.setImage(ImageSource.uri(path));
        }
    }
}