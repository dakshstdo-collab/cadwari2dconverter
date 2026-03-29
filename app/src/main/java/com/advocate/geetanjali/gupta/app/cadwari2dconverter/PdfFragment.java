package com.advocate.geetanjali.gupta.app.cadwari2dconverter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import java.io.File;

public class PdfFragment extends Fragment {

    private static final String ARG_PATH = "pdf_path";

    public static PdfFragment newInstance(String path) {
        PdfFragment f = new PdfFragment();
        Bundle b = new Bundle();
        b.putString(ARG_PATH, path);
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pdf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PDFView     pdfView  = view.findViewById(R.id.pdfView);
        ProgressBar progress = view.findViewById(R.id.progressPdf);

        String path = getArguments() != null ? getArguments().getString(ARG_PATH) : null;
        if (path == null || !new File(path).exists()) {
            progress.setVisibility(View.GONE);
            return;
        }

        // Also override the pinch hard cap
        com.github.barteksc.pdfviewer.util.Constants.Pinch.MAXIMUM_ZOOM = 32f;

        pdfView.fromFile(new File(path))
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false)
                .enableAntialiasing(false)
                .pageFitPolicy(FitPolicy.BOTH)
                .scrollHandle(new DefaultScrollHandle(requireContext()))
                .spacing(6)
                .onLoad(pageCount -> progress.setVisibility(View.GONE))
                .onError(t -> progress.setVisibility(View.GONE))
                .load();

        pdfView.setMinZoom(0.5f);
        pdfView.setMidZoom(4f);
        pdfView.setMaxZoom(32f); //Double Tap Zoom



    }
}