package com.example.testwatermark;

import android.util.Log;

public class StringUtils {
    static final int MAX_LOG = 800;
    public static void HaoLog(String data) {
        HaoLog(data, 4);
    }

    public static void HaoLog(String data, int showC) {
        StackTraceElement[] stes = Thread.currentThread().getStackTrace();

        // 確保showC不超過stes的長度
        showC = Math.min(showC, stes.length - 1);

        // 忽略 "MainAppCompatActivity.java" 和 "MessageBaseActivity.java" 的訊息
        while (showC < stes.length &&
                (stes[showC].getFileName().equals("MainAppCompatActivity.java") ||
                        stes[showC].getFileName().equals("MessageBaseActivity.java"))) {
            showC++;
        }

        if (showC < stes.length) {
            String tag = "HaoLog (" + stes[showC].getFileName() + ":" + stes[showC].getLineNumber() + ") ";
            tag += stes[showC].getMethodName() + " Thread=" + Thread.currentThread().getName() + "　 ";

            // 數據為null的情況
            if (data == null) {
                Log.d(tag, "null");
            }
            // 數據長度超過最大限制的情況
            else if (data.length() > MAX_LOG) {
                // 將數據分段輸出
                int startIndex = 0;
                while (startIndex < data.length()) {
                    int endIndex = Math.min(startIndex + MAX_LOG, data.length());
                    Log.d(tag, data.substring(startIndex, endIndex));
                    startIndex = endIndex;
                }
            } else {
                // 正常輸出數據的情況
                Log.d(tag, data);
            }
        }
    }
}
