package com.example.testwatermark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
            PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();

            // 修改此處，加載自訂字體
            // 注意將 "path/to/NotoSansSC-Regular.otf" 換為你的字體文件路徑
            PDFont font = PDType0Font.load(document, assetManager.open("SentyDew.ttf"));
            float fontSize = 36.0f;
            for (PDPage page : pages) {
                PDRectangle mediaBox = page.getMediaBox();
                PDPageContentStream cs = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);

                // 定義用於添加到 PDF 的內容流
                PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
                contentStream.beginText();
                graphicsState.setNonStrokingAlphaConstant(0.1f); // 透明度
                contentStream.setGraphicsStateParameters(graphicsState);
                contentStream.setNonStrokingColor(15, 38, 192);
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("這是浮水印");
                contentStream.endText();
                contentStream.close();

                // 計算水印文字的位置
                float stringWidth = font.getStringWidth("這是浮水印") * fontSize / 1000f;  // 注意，字體大小需與浮水印的字體大小一致
                float startX = (mediaBox.getWidth() - stringWidth) / 2;
                float startY = (mediaBox.getHeight() - fontSize) / 2;

                // 計算圖像的位置
                float imageWidth = 100.0f;
                float imageHeight = 100.0f;
                float imageX = (mediaBox.getWidth() - imageWidth) / 2; // 居中顯示
                float imageY = mediaBox.getLowerLeftY() + 10; // 距離底部 10 個單位
                InputStream in = assetManager.open("falcon.jpg");
                PDImageXObject pdImage = JPEGFactory.createFromStream(document, in);
                cs.drawImage(pdImage, imageX, imageY, imageWidth, imageHeight);
                graphicsState.setNonStrokingAlphaConstant(0.1f); // 透明度
                cs.setGraphicsStateParameters(graphicsState);
                cs.close();

            }

            // 將最終的 pdf 文檔保存到文件中
            String path = root.getAbsolutePath() + "/robert.pdf";
            document.save(path);
            document.close();
            tv.setText("已成功將 PDF 寫入" + path);
        } catch (IOException e) {
            StringUtils.HaoLog("PdfBox-Android"+" Error loading or editing pdf" + e);
        }
    }
}