package com.advocate.geetanjali.gupta.app.cadwari2dconverter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.File;

public class ViewerActivity extends AppCompatActivity {

    public static final String EXTRA_SVG = "path_svg";
    public static final String EXTRA_PDF = "path_pdf";
    public static final String EXTRA_PNG = "path_png";

    private static final String AUTHORITY =
            "com.advocate.geetanjali.gupta.app.cadwari2dconverter.fileprovider";

    private String svgPath, pdfPath, pngPath;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        svgPath = getIntent().getStringExtra(EXTRA_SVG);
        pdfPath = getIntent().getStringExtra(EXTRA_PDF);
        pngPath = getIntent().getStringExtra(EXTRA_PNG);

        viewPager          = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        FloatingActionButton fabShare = findViewById(R.id.fabShare);

        viewPager.setAdapter(new ViewerPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);
        viewPager.setUserInputEnabled(false);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0)      tab.setText("{SVG}");
                    else if (position == 1) tab.setText("[PDF]");
                    else                    tab.setText("{PNG}");
                }
        ).attach();

        fabShare.setOnClickListener(v -> shareCurrentFile());
    }

    private void shareCurrentFile() {
        int tab = viewPager.getCurrentItem();

        String filePath;
        String mimeType;
        if (tab == 0) {
            filePath = svgPath;
            mimeType = "image/svg+xml";
        } else if (tab == 1) {
            filePath = pdfPath;
            mimeType = "application/pdf";
        } else {
            filePath = pngPath;
            mimeType = "image/png";
        }

        if (filePath == null) return;
        File file = new File(filePath);
        if (!file.exists()) return;

        // Prefix filename with CADwari
        String originalName = file.getName();
        String sharedName   = "CADwari_" + originalName;

        Uri uri = FileProvider.getUriForFile(this, AUTHORITY, file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TITLE, sharedName);  // shown in share sheet
        intent.putExtra(Intent.EXTRA_SUBJECT, sharedName);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // ClipData ensures filename metadata is passed to receiving app
        intent.setClipData(android.content.ClipData.newUri(
                getContentResolver(), sharedName, uri));

        startActivity(Intent.createChooser(intent, "Save " + sharedName + " via"));
    }

    private class ViewerPagerAdapter extends FragmentStateAdapter {
        ViewerPagerAdapter(FragmentActivity fa) { super(fa); }

        @Override public int getItemCount() { return 3; }

        @NonNull @Override
        public Fragment createFragment(int position) {
            if (position == 0) return SvgFragment.newInstance(svgPath);
            if (position == 1) return PdfFragment.newInstance(pdfPath);
            return PngFragment.newInstance(pngPath);
        }
    }
}