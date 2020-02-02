package com.ykh.giftmanager;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.ykh.giftmanager.utils.CallbackBundle;
import com.ykh.giftmanager.utils.ExcelUtil;
import com.ykh.giftmanager.utils.OpenFileDialog;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private boolean isDebugging = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QMUIStatusBarHelper.translucent(this);
        QMUIStatusBarHelper.setStatusBarLightMode(this);
        topbarTest();
        loadExcel(0, null);
        buttonsConfig();
    }

    private void loadExcel(final int option, final String data) {
        String[] tips = new String[]{"正在加载数据", "正在保存", "正在导入数据"};
        final QMUITipDialog dialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(tips[option])
                .create();
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (option == 1)
                        ExcelUtil.getInstance().addColumn(data);
                    else if (option == 2)
                        ExcelUtil.getInstance().importExcel(new File(data));
                    ExcelUtil.getInstance().init(MainActivity.this);
                    ExcelUtil.getInstance().load();
                    ExcelUtil.getInstance().makeAllHTML();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                }
            }
        }).start();
    }

    private void topbarTest() {
        final QMUITopBar topbar = findViewById(R.id.topbar);
        topbar.setTitle(R.string.app_name);
        final QMUIAlphaImageButton backButton = topbar.addLeftBackImageButton();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final QMUITipDialog dialog = new QMUITipDialog.Builder(MainActivity.this)
//                        .setIconType(QMUITipDialog.Builder.ICON)
                        .setTipWord("操作成功")
                        .create();
                dialog.show();
                backButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 2000);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void searchButtonClick(View view) {
        startActivity(new Intent(this, SearchActivity.class));
        overridePendingTransition(0, 0);
    }

    private void buttonsConfig() {
        int width = QMUIDisplayHelper.getScreenWidth(this);
        int button_size = (int) (width * 0.8 / 4);
        QMUIButton btnAddRow = findViewById(R.id.btn_add_row);
        QMUIButton btnAddCol = findViewById(R.id.btn_add_col);
        QMUIButton btnImportExcel = findViewById(R.id.btn_import_excel);
        QMUIButton btnExportExcel = findViewById(R.id.btn_export_excel);
        QMUIButton[] buttons = new QMUIButton[]{btnAddRow, btnAddCol, btnImportExcel, btnExportExcel};
        int[] icon_res = new int[]{
                R.drawable.add_row,
                R.drawable.add_colum,
                R.drawable.import_excel,
                R.drawable.export_excel};
        for (int i = 0; i < buttons.length; i++) {
            QMUIButton button = buttons[i];
            ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
            layoutParams.width = button_size;
            layoutParams.height = button_size;
            button.setLayoutParams(layoutParams);
            Drawable drawable = getResources().getDrawable(icon_res[i]);
            drawable.setBounds(0, (int) (button_size * 0.1), (int) (button_size * 0.6), (int) (button_size * 0.7));
            button.setCompoundDrawables(null, drawable, null, null);
        }
    }

    public void addRow(View view) {
        startActivity(new Intent(this, EditItemActivity.class));
    }

    public void addCol(View view) {
        final QMUIDialog.EditTextDialogBuilder dialogBuilder = new QMUIDialog.EditTextDialogBuilder(this);
        QMUIDialog dialog = dialogBuilder
                .setTitle("添加 1 列")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        String colName = dialogBuilder.getEditText().getText().toString().trim();
                        if (colName.length() > 0 && colName.length() < 33) {
                            loadExcel(1, colName);
                        } else {
                            final QMUITipDialog tip = new QMUITipDialog.Builder(MainActivity.this)
                                    .setTipWord(colName.length() == 0 ? "没有输入内容" : "输入的内容不得超过32个字符")
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .create(true);
                            tip.show();
                            dialogBuilder.getEditText().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tip.dismiss();
                                }
                            }, 3000);
                        }
                        QMUIKeyboardHelper.hideKeyboard(dialogBuilder.getEditText());
                        dialog.dismiss();
                    }
                })
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        QMUIKeyboardHelper.hideKeyboard(dialogBuilder.getEditText());
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
    }

    public void importExcel(View view) {
        String[] PERMISSIONS = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"};
        //检测是否有写的权限
        int permission = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        if (permission != PackageManager.PERMISSION_GRANTED) return;

        Map<String, Integer> images = new HashMap<>();
        images.put("xlsx", R.drawable.icon_excel);
        images.put("empty_folder", R.drawable.icon_empty_folder);
        images.put(OpenFileDialog.sRoot, R.drawable.icon_disk);    // 根目录图标
        images.put(OpenFileDialog.sParent, R.drawable.icon_last_level_folder);    //返回上一层的图标
        images.put(OpenFileDialog.sFolder, R.drawable.icon_folder);    //文件夹图标
        images.put(OpenFileDialog.sEmpty, R.drawable.icon_unknown);
        View titleView = getLayoutInflater().inflate(R.layout.file_dialog_title, null);
        Dialog dialog = OpenFileDialog.createDialog(this, titleView, new CallbackBundle() {
            @Override
            public void callback(Bundle bundle) {
                final String path = bundle.getString("path");
                final QMUITipDialog dialog = new QMUITipDialog.Builder(MainActivity.this)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord("正在导入文件")
                        .create();
                dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String message = null;
                        try {
                            boolean b = ExcelUtil.getInstance().tryLoad(path);
                            if (!b) message = "文件不符合规范";
                        } catch (IOException e) {
                            message = e.getMessage();
                        } catch (InvalidFormatException e) {
                            e.printStackTrace();
                            message = "文件不符合规范";
                        } finally {
                            final String finalMessage = message;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    if (finalMessage == null)
                                        loadExcel(2, path);
                                    else {
                                        Toast.makeText(MainActivity.this, finalMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                }).start();

            }
        }, ".xlsx;", images);
        dialog.show();
        Log.e(TAG, "importExcel: " + "显示文件对话框");
    }

    public void exportExcel(View view) {
        String[] PERMISSIONS = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"};
        //检测是否有写的权限
        int permission = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        if (permission != PackageManager.PERMISSION_GRANTED) return;
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "礼金管理");
        boolean success = false;
        String error = null;
        if (!folder.exists()) {
            success = folder.mkdirs();
            if (isDebugging) Log.e(TAG, "exportExcel: " + folder.getAbsolutePath());

        } else {
            if (folder.isFile()) {
                success = folder.delete();
                success = folder.mkdirs();
            } else {
                success = true;
            }
        }
        File file = null;
        if (!success) {
            error = "无法在内部存储中创建文件夹";
        } else {
            file = new File(folder, "礼金数据" + System.currentTimeMillis() + ".xlsx");
            try {
                ExcelUtil.getInstance().exportExcel(file);
                if (isDebugging) Log.e(TAG, "exportExcel: " + "复制文件结束");
                openAndroidFile(file.getAbsolutePath());
            } catch (IOException e) {
                error = e.getMessage();
            }
        }
        if (error != null) {
            final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(this)
                    .setTipWord(error)
                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                    .create();
            qmuiTipDialog.show();
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    qmuiTipDialog.dismiss();
                }
            }, 3000);
        } else {
            Toast.makeText(this, "文件保存至: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    public void openAndroidFile(String filepath) {
        Intent intent = new Intent();
        File file = new File(filepath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//设置标记
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_VIEW);//动作，查看
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            Uri uri = FileProvider.getUriForFile(this, "com.ykh.giftmanager.fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//设置类型
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//设置类型
        }
        startActivity(intent);
    }
}
