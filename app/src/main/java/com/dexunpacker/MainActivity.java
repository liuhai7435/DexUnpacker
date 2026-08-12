package com.dexunpacker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.List;

/**
 * 主界面 - 演示 DexUnpacker 使用
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView tvStatus;
    private TextView tvResult;
    private Button btnUnpack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 简单界面
        tvStatus = new TextView(this);
        tvStatus.setText("DexUnpacker - 免 Root 脱壳工具");
        tvStatus.setTextSize(18);
        
        tvResult = new TextView(this);
        tvResult.setText("状态：等待操作...\n");
        tvResult.setTextSize(14);
        
        btnUnpack = new Button(this);
        btnUnpack.setText("开始脱壳");
        btnUnpack.setOnClickListener(v -> startUnpacking());
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.addView(tvStatus);
        layout.addView(tvResult);
        layout.addView(btnUnpack);
        
        setContentView(layout);
        
        // 检查权限
        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 需要 MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "需要文件管理权限", Toast.LENGTH_LONG).show();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-10 需要运行时权限
            if (ContextCompat.checkSelfPermission(this, 
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                   Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "权限被拒绝，无法保存文件", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startUnpacking() {
        btnUnpack.setEnabled(false);
        tvResult.setText("状态：初始化脱壳引擎...\n");
        
        new Thread(() -> {
            try {
                // 1. 初始化
                DexUnpacker.init();
                runOnUiThread(() -> tvResult.append("✓ 引擎初始化完成\n"));
                
                // 2. 获取 dex cookies
                long[] cookies = DexUnpacker.getDexCookies();
                runOnUiThread(() -> tvResult.append("✓ 发现 " + cookies.length + " 个 Dex 文件\n"));
                
                if (cookies.length == 0) {
                    runOnUiThread(() -> {
                        tvResult.append("⚠ 未找到可脱壳的 Dex 文件\n");
                        tvResult.append("提示：此工具只能脱壳当前应用进程内的 Dex\n");
                        tvResult.append("如需脱壳其他应用，请使用 VirtualApp 框架加载目标 APK\n");
                        btnUnpack.setEnabled(true);
                    });
                    return;
                }
                
                // 3. 准备输出目录
                File outputDir = new File(getExternalFilesDir("unpacked"), 
                                         "dex_" + System.currentTimeMillis());
                outputDir.mkdirs();
                runOnUiThread(() -> tvResult.append("✓ 输出目录：" + outputDir.getAbsolutePath() + "\n"));
                
                // 4. 执行脱壳
                runOnUiThread(() -> tvResult.append("正在脱壳...\n"));
                List<String> results = DexUnpacker.dumpAll(cookies, outputDir.getAbsolutePath());
                
                // 5. 显示结果
                runOnUiThread(() -> {
                    tvResult.append("✓ 脱壳完成！成功 " + results.size() + " 个文件:\n");
                    for (String path : results) {
                        tvResult.append("  → " + path + "\n");
                    }
                    btnUnpack.setEnabled(true);
                    Toast.makeText(this, "脱壳完成！", Toast.LENGTH_LONG).show();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Unpack failed", e);
                runOnUiThread(() -> {
                    tvResult.append("✗ 错误：" + e.getMessage() + "\n");
                    btnUnpack.setEnabled(true);
                });
            }
        }).start();
    }
}
