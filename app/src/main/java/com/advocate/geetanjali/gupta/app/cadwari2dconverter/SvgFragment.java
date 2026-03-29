package com.advocate.geetanjali.gupta.app.cadwari2dconverter;

import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileInputStream;

public class SvgFragment extends Fragment {

    private static final String ARG_PATH = "svg_path";

    public static SvgFragment newInstance(String filePath) {
        SvgFragment f = new SvgFragment();
        Bundle b = new Bundle();
        b.putString(ARG_PATH, filePath);
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_svg, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WebView webView = view.findViewById(R.id.webView);
        ProgressBar progress = view.findViewById(R.id.progressSvg);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setBuiltInZoomControls(false);
        s.setSupportZoom(false);
        s.setAllowFileAccess(true);
        webView.setBackgroundColor(0xFFFFFFFF);

        String path = getArguments() != null ? getArguments().getString(ARG_PATH) : null;
        if (path == null || !new File(path).exists()) return;

        // Read SVG bytes and Base64 encode — safe to embed in any JS string
        String svgBase64;
        try {
            File f = new File(path);
            byte[] bytes = new byte[(int) f.length()];
            FileInputStream fis = new FileInputStream(f);
            fis.read(bytes);
            fis.close();
            svgBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String html = "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1,user-scalable=no'>"
                + "<style>"
                + "*{margin:0;padding:0;box-sizing:border-box;touch-action:none;}"
                + "html,body{width:100%;height:100%;background:#ffffff;overflow:hidden;}"
                + "#wrap{position:absolute;top:0;left:0;width:100%;height:100%;}"
                + "#wrap svg{position:absolute;top:0;left:0;transform-origin:0 0;}"
                + "</style></head><body>"
                + "<div id='wrap'></div>"
                + "<script>"

                // Decode Base64 → SVG text, inject into DOM
                + "var b64='" + svgBase64 + "';"
                + "var txt=decodeURIComponent(escape(atob(b64)));"
                + "var wrap=document.getElementById('wrap');"
                + "wrap.innerHTML=txt;"
                + "var svg=wrap.querySelector('svg');"
                + "if(!svg){document.body.innerText='No SVG element found';}"

                // State
                + "var scale=1,tx=0,ty=0;"
                + "var lastDist=0,lastX=0,lastY=0,panning=false;"

                + "function apply(){"
                + "  svg.style.transform='translate('+tx+'px,'+ty+'px) scale('+scale+')';"
                + "}"

                // Fit to screen using getBBox (works even without viewBox)
                + "function fit(){"
                + "  try{"
                + "    var bb=svg.getBBox();"
                + "    if(!bb||bb.width<=0)bb={x:0,y:0,width:800,height:600};"
                + "    if(!svg.getAttribute('viewBox'))"
                + "      svg.setAttribute('viewBox',bb.x+' '+bb.y+' '+bb.width+' '+bb.height);"
                + "    var vb=svg.viewBox.baseVal;"
                + "    svg.setAttribute('width',vb.width);"
                + "    svg.setAttribute('height',vb.height);"
                + "    var sx=window.innerWidth/vb.width;"
                + "    var sy=window.innerHeight/vb.height;"
                + "    scale=Math.min(sx,sy)*0.95;"
                + "    tx=(window.innerWidth -vb.width *scale)/2;"
                + "    ty=(window.innerHeight-vb.height*scale)/2;"
                + "    apply();"
                + "  }catch(e){document.body.innerText='Fit error: '+e;}"
                + "}"

                + "setTimeout(fit,100);"

                // Pan — single finger
                + "wrap.addEventListener('touchstart',function(e){"
                + "  if(e.touches.length===1){"
                + "    panning=true;"
                + "    lastX=e.touches[0].clientX;lastY=e.touches[0].clientY;"
                + "  }else if(e.touches.length===2){"
                + "    panning=false;"
                + "    lastDist=Math.hypot("
                + "      e.touches[0].clientX-e.touches[1].clientX,"
                + "      e.touches[0].clientY-e.touches[1].clientY);"
                + "  }"
                + "  e.preventDefault();"
                + "},{passive:false});"

                // Pinch zoom + pan
                + "wrap.addEventListener('touchmove',function(e){"
                + "  if(e.touches.length===1&&panning){"
                + "    tx+=e.touches[0].clientX-lastX;"
                + "    ty+=e.touches[0].clientY-lastY;"
                + "    lastX=e.touches[0].clientX;lastY=e.touches[0].clientY;"
                + "    apply();"
                + "  }else if(e.touches.length===2){"
                + "    var d=Math.hypot("
                + "      e.touches[0].clientX-e.touches[1].clientX,"
                + "      e.touches[0].clientY-e.touches[1].clientY);"
                + "    var mx=(e.touches[0].clientX+e.touches[1].clientX)/2;"
                + "    var my=(e.touches[0].clientY+e.touches[1].clientY)/2;"
                + "    var delta=d/lastDist;"
                + "    tx=mx-delta*(mx-tx);"
                + "    ty=my-delta*(my-ty);"
                + "    scale*=delta;"
                + "    lastDist=d;"
                + "    apply();"
                + "  }"
                + "  e.preventDefault();"
                + "},{passive:false});"

                + "wrap.addEventListener('touchend',function(){panning=false;});"
                + "</script></body></html>";

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView v, String url) {
                progress.setVisibility(View.GONE);
            }
        });

        webView.loadDataWithBaseURL(
                "file:///android_asset/",
                html,
                "text/html",
                "UTF-8",
                null
        );
    }
}