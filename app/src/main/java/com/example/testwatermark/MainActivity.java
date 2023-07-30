package com.example.testwatermark;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.artifex.mupdf.viewer.DocumentActivity;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import com.tom_roush.pdfbox.util.Matrix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    File root;
    AssetManager assetManager;
    TextView tv;
    Button watermarkFunction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 啟用 Android 資源加載
        PDFBoxResourceLoader.init(getApplicationContext());
        // 找到外部存儲的根目錄。
        root = getApplicationContext().getCacheDir();
        assetManager = getAssets();
        tv = findViewById(R.id.statusTextView);
        watermarkFunction = findViewById(R.id.watermarkFunction);
        watermarkFunction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                watermarkFunction();
            }
        });
    }

    public void watermarkFunction() {
        try {
            PDDocument document = PDDocument.load(assetManager.open("example-pdf.pdf"));
            PDPageTree pages = document.getPages();
            PDExtendedGraphicsState textGraphicsState = new PDExtendedGraphicsState();
            PDExtendedGraphicsState imageGraphicsState = new PDExtendedGraphicsState();
            // 修改此處，加載自訂字體
            // 注意將 "path/to/NotoSansSC-Regular.otf" 換為你的字體文件路徑
            PDFont font = PDType0Font.load(document, assetManager.open("SentyDew.ttf"));
            float fontSize = 36.0f;
            for (PDPage page : pages) {
                PDRectangle mediaBox = page.getMediaBox();
                PDPageContentStream pageContentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);



                // 計算水印文字的位置
                float stringWidth = font.getStringWidth("這是浮水印") * fontSize / 1000f;  // 注意，字體大小需與浮水印的字體大小一致
                float startX = (mediaBox.getWidth() - stringWidth) / 2;
                float startY = (mediaBox.getHeight() - fontSize) / 2;

                // 添加浮水印文字
                double percent = 0.3;
                double radians = Math.PI / 2 * percent;
                pageContentStream.beginText();
                pageContentStream.setTextMatrix(Matrix.getRotateInstance(radians , startX, startY));
                textGraphicsState.setNonStrokingAlphaConstant(0.7f); // 透明度
                pageContentStream.setGraphicsStateParameters(textGraphicsState);
                pageContentStream.setNonStrokingColor(15, 38, 192);
                pageContentStream.setFont(font, 12);
                pageContentStream.setTextTranslation(startX, startY);
                pageContentStream.showText("這是浮水印");
                pageContentStream.endText();

                // 計算圖像的位置
                float imageWidth = 100.0f;
                float imageHeight = 100.0f;
                float imageX = (mediaBox.getWidth() - imageWidth) / 2; // 居中顯示
                //float imageY = mediaBox.getLowerLeftY() + 10; // 距離底部 10 個單位
                // 從頁面的上邊緣開始，並向下移動一半的頁面高度，然後再減去圖片高度的一半。這將圖片放在頁面的正中央。
                // 先獲取頁面的上邊緣位置（mediaBox.getUpperRightY()），然後除以2來獲取頁面的垂直中心。然後它減去圖片高度的一半（imageHeight / 2），這樣將圖片的中心對齊到頁面的中心。
                float imageY = mediaBox.getUpperRightY() / 2 - imageHeight / 2;

                InputStream in = assetManager.open("falcon.jpg");
                imageGraphicsState.setNonStrokingAlphaConstant(0.5f); // 透明度
                pageContentStream.setGraphicsStateParameters(imageGraphicsState);
                PDImageXObject pdImage = JPEGFactory.createFromStream(document, in);
                pageContentStream.drawImage(pdImage, imageX, imageY, imageWidth, imageHeight);
                pageContentStream.close();
            }

            // 將最終的 pdf 文檔保存到文件中
            String path = root.getAbsolutePath() + "/example-pdf.pdf";
            document.save(path);
            document.close();
            tv.setText("已成功將 PDF 寫入" + path);
            muPdfView(path);
        } catch (IOException e) {
            StringUtils.HaoLog("PdfBox-Android"+" Error loading or editing pdf" + e);
        }
    }

    private void muPdfView(String path){
        String fileType = null;
        int index = path.lastIndexOf(".");
        if (index > 0) {
            fileType = path.substring(index);
        }
        Intent intent = new Intent(this, DocumentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), fileType);
        intent.putExtra(getComponentName().getPackageName() + ".ReturnToLibraryActivity", 1);
        startActivity(intent);
    }
}