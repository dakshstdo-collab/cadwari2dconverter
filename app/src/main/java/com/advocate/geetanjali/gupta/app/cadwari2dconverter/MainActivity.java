package com.advocate.geetanjali.gupta.app.cadwari2dconverter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.os.Handler;
import android.os.Looper;


public class MainActivity extends AppCompatActivity {

    private static final String TAG        = "CAD";
    private static final String PREFS_NAME = "cad_prefs";
    private static final String KEY_RECENT = "recent_files";
    private static final int    MAX_RECENT = 20;

    private Uri selectedFileUri = null;
    private TextView tvStatus;
    private MaterialButton btnOpen;
    private RecyclerView rvRecent;
    private RecentAdapter adapter;
    private final List<RecentFile> recentList = new ArrayList<>();

    private final Handler dotHandler = new Handler();
    private Runnable dotRunnable;

    private final ActivityResultLauncher filePicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;

                // Validate extension before accepting
                String name = getDisplayName(uri);
                if (name != null) {
                    String lower = name.toLowerCase(Locale.getDefault());
                    if (!lower.endsWith(".dxf") && !lower.endsWith(".dwg")) {
                        // Wrong file type — reject immediately
                        Snackbar.make(
                                findViewById(android.R.id.content),
                                "❌ Only .DXF and .DWG files are supported",
                                Snackbar.LENGTH_LONG
                        ).show();
                        return;  // block conversion
                    }
                }

                selectedFileUri = uri;
                convertFile();
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvStatus  = findViewById(R.id.tvStatus);
        btnOpen   = findViewById(R.id.btnOpen);
        rvRecent  = findViewById(R.id.rvRecent);

        adapter = new RecentAdapter(recentList, this::openRecent);
        rvRecent.setLayoutManager(new LinearLayoutManager(this));
        rvRecent.setAdapter(adapter);

        loadRecentFiles();

        new Thread(() -> {
            extractFonts();
            int r = CadConverter.nativeInit(getFilesDir().getAbsolutePath());
            runOnUiThread(() -> {
                if (r != 0) tvStatus.setText("⚠️ Native init failed: " + r);
            });
        }).start();

        btnOpen.setOnClickListener(v -> filePicker.launch(new String[]{"*/*"}));

        // Handle "Open with" intent from external apps
        Uri incomingUri = getIntent().getData();
        if (incomingUri != null) {
            String name = getDisplayName(incomingUri);
            if (name != null) {
                String lower = name.toLowerCase(Locale.getDefault());
                if (lower.endsWith(".dxf") || lower.endsWith(".dwg")) {
                    selectedFileUri = incomingUri;
                    tvStatus.setText("Opening: " + name);
                    convertFile();
                } else {
                    tvStatus.setText("❌ Only .DXF and .DWG files supported");
                }
            }
        }



    }

    private String getDisplayName(Uri uri) {
        try (Cursor c = getContentResolver().query(
                uri, new String[]{OpenableColumns.DISPLAY_NAME},
                null, null, null)) {
            if (c != null && c.moveToFirst()) return c.getString(0);
        } catch (Exception e) {
            Log.e(TAG, "getDisplayName", e);
        }
        return null;
    }
    // ── Conversion ────────────────────────────────────────────────────────────

    private void convertFile() {

//        tvStatus.setText("Converting...");
        startDotAnimation("Converting");
        btnOpen.setEnabled(false);

        new Thread(() -> {
            try {
                // Resolve display name + extension
                String displayName = "file.dxf";
                String inputExt = ".dxf";
                Cursor c = getContentResolver().query(selectedFileUri,
                        new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
                if (c != null && c.moveToFirst()) {
                    displayName = c.getString(0);
                    if (displayName != null && displayName.lastIndexOf('.') >= 0) {
                        String det = displayName.substring(displayName.lastIndexOf('.')).toLowerCase();
                        if (det.equals(".dxf") || det.equals(".dwg")) inputExt = det;
                    }
                    c.close();
                }
                final String finalName = displayName;

                // Copy URI → cache
                File inputFile = new File(getCacheDir(), "input_cad" + inputExt);
                try (InputStream in = getContentResolver().openInputStream(selectedFileUri);
                     FileOutputStream fos = new FileOutputStream(inputFile)) {
                    byte[] buf = new byte[8192]; int n;
                    while ((n = in.read(buf)) != -1) fos.write(buf, 0, n);
                }

                // Convert to all 3 formats
                File svgOut = new File(getCacheDir(), "output.svg");
                File pdfOut = new File(getCacheDir(), "output.pdf");
                File pngOut = new File(getCacheDir(), "output.png");
                int r1 = CadConverter.nativeConvert(inputFile.getAbsolutePath(), svgOut.getAbsolutePath(), "svg");
                int r2 = CadConverter.nativeConvert(inputFile.getAbsolutePath(), pdfOut.getAbsolutePath(), "pdf");
                int r3 = CadConverter.nativeConvert(inputFile.getAbsolutePath(), pngOut.getAbsolutePath(), "png");
                Log.d(TAG, "svg=" + r1 + " pdf=" + r2 + " png=" + r3);

                if (r1 == 0 || r2 == 0 || r3 == 0) {
                    // Save timestamped copies so recent list keeps working
//                    String ts = String.valueOf(System.currentTimeMillis());
                    String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    File svgSaved = saveOutput(svgOut, r1, ts, "svg");
                    File pdfSaved = saveOutput(pdfOut, r2, ts, "pdf");
                    File pngSaved = saveOutput(pngOut, r3, ts, "png");

                    String svgPath = svgSaved != null ? svgSaved.getAbsolutePath() : "";
                    String pdfPath = pdfSaved != null ? pdfSaved.getAbsolutePath() : "";
                    String pngPath = pngSaved != null ? pngSaved.getAbsolutePath() : "";

                    saveRecentFile(finalName, svgPath, pdfPath, pngPath);

                    runOnUiThread(() -> {
                        btnOpen.setEnabled(true);
                        stopDotAnimation();
                        tvStatus.setText("✅ Done");
                        loadRecentFiles();
                        Intent intent = new Intent(this, ViewerActivity.class);
                        intent.putExtra(ViewerActivity.EXTRA_SVG, svgPath);
                        intent.putExtra(ViewerActivity.EXTRA_PDF, pdfPath);
                        intent.putExtra(ViewerActivity.EXTRA_PNG, pngPath);
                        startActivity(intent);
                    });
                } else {
                    runOnUiThread(() -> {
                        btnOpen.setEnabled(true);
                        stopDotAnimation();
                        tvStatus.setText("❌ All conversions failed");
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "convertFile", e);
                runOnUiThread(() -> {
                    stopDotAnimation();
                    tvStatus.setText("❌ Error: " + e.getMessage());
                    btnOpen.setEnabled(true);
                });
            }
        }).start();
    }

    /** Renames output file to a timestamped path so it persists for recent files */
    private File saveOutput(File src, int result, String ts, String ext) {
        if (result != 0 || !src.exists()) return null;
        File dest = new File(getCacheDir(), "CADwari_" + ts + "." + ext);
        return src.renameTo(dest) ? dest : src;
    }

    // ── Recent files ──────────────────────────────────────────────────────────

    private void openRecent(RecentFile rf) {
        boolean ok = (!rf.svgPath.isEmpty() && new File(rf.svgPath).exists())
                || (!rf.pdfPath.isEmpty() && new File(rf.pdfPath).exists())
                || (!rf.pngPath.isEmpty() && new File(rf.pngPath).exists());
        if (!ok) {
            Snackbar.make(rvRecent, "Cached file no longer available", Snackbar.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ViewerActivity.class);
        intent.putExtra(ViewerActivity.EXTRA_SVG, rf.svgPath);
        intent.putExtra(ViewerActivity.EXTRA_PDF, rf.pdfPath);
        intent.putExtra(ViewerActivity.EXTRA_PNG, rf.pngPath);
        startActivity(intent);
    }

    private void saveRecentFile(String name, String svg, String pdf, String png) {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            JSONArray arr = new JSONArray(prefs.getString(KEY_RECENT, "[]"));
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("svg",  svg);
            obj.put("pdf",  pdf);
            obj.put("png",  png);
            obj.put("time", new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date()));
            JSONArray newArr = new JSONArray();
            newArr.put(obj);
            for (int i = 0; i < arr.length() && i < MAX_RECENT - 1; i++) newArr.put(arr.get(i));
            prefs.edit().putString(KEY_RECENT, newArr.toString()).apply();
        } catch (Exception e) { Log.e(TAG, "saveRecentFile", e); }
    }

    private void loadRecentFiles() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            JSONArray arr = new JSONArray(prefs.getString(KEY_RECENT, "[]"));
            recentList.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                recentList.add(new RecentFile(
                        o.getString("name"), o.getString("svg"),
                        o.getString("pdf"),  o.getString("png"), o.getString("time")));
            }
            if (adapter != null) adapter.notifyDataSetChanged();
        } catch (Exception e) { Log.e(TAG, "loadRecentFiles", e); }
    }

    // ── Font extraction ───────────────────────────────────────────────────────

    private void extractFonts() {
        File dir = new File(getFilesDir(), "fonts");
        if (!dir.exists()) dir.mkdirs();
        try {
            String[] fonts = getAssets().list("fonts");
            if (fonts == null) return;
            for (String font : fonts) {
                File out = new File(dir, font);
                if (out.exists()) continue;
                try (InputStream in = getAssets().open("fonts/" + font);
                     FileOutputStream fos = new FileOutputStream(out)) {
                    byte[] buf = new byte[4096]; int n;
                    while ((n = in.read(buf)) != -1) fos.write(buf, 0, n);
                }
            }
        } catch (Exception e) { Log.e(TAG, "extractFonts", e); }
    }

    // ── Data model ────────────────────────────────────────────────────────────

    static class RecentFile {
        String name, svgPath, pdfPath, pngPath, time;
        RecentFile(String n, String s, String p, String g, String t) {
            name=n; svgPath=s; pdfPath=p; pngPath=g; time=t;
        }
    }

    // ── RecyclerView adapter ──────────────────────────────────────────────────

    static class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.VH> {
        interface OnClick { void onClick(RecentFile rf); }
        private final List<RecentFile> list;
        private final OnClick listener;
        RecentAdapter(List<RecentFile> list, OnClick listener) {
            this.list = list; this.listener = listener;
        }
        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recent_file, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            RecentFile rf = list.get(pos);
            h.tvName.setText(rf.name);
            h.tvTime.setText(rf.time);
            h.itemView.setOnClickListener(v -> listener.onClick(rf));
        }
        @Override public int getItemCount() { return list.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvTime;
            VH(View v) { super(v); tvName=v.findViewById(R.id.tvName); tvTime=v.findViewById(R.id.tvTime); }
        }
    }

    private void startDotAnimation(String baseText) {
        final String[] dots = {"", ".", "..", "..."};
        final int[] i = {0};
        dotRunnable = new Runnable() {
            @Override public void run() {
                tvStatus.setText(baseText + dots[i[0] % 4]);
                i[0]++;
                dotHandler.postDelayed(this, 400);
            }
        };
        dotHandler.post(dotRunnable);
    }

    private void stopDotAnimation() {
        if (dotRunnable != null) {
            dotHandler.removeCallbacks(dotRunnable);
            dotRunnable = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_privacy_policy) {
            // TODO: open privacy_policy.html later
            return true;
        } else if (id == R.id.action_open_source) {
            // TODO: open open_source.html later
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}