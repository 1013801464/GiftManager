package com.ykh.giftmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.ykh.giftmanager.utils.ExcelUtil;
import com.ykh.giftmanager.utils.Record;

import java.io.IOException;

public class EditItemActivity extends AppCompatActivity {
    private static final String TAG = "EditItemActivity";
    private Record record;
    private String[] headers;
    private boolean modified = false;
    private boolean isDebuging = true;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final QMUICommonListItemView item = (QMUICommonListItemView) view;
            final int loc = (int) item.getTag();
            if (loc == 0) {
                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(EditItemActivity.this);
                builder.setPlaceholder(record.getFullName())
                        .setDefaultText(record.getFullName())
                        .setTitle(headers[0])
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                String nameNew = builder.getEditText().getText().toString().trim();
                                if (isDebuging) Log.e(TAG, "onClick: new Name = " + nameNew);
                                if (isDebuging)
                                    Log.e(TAG, "record.getFullName() = " + record.getFullName());
                                if (nameNew.length() != 0 && !nameNew.equals(record.getFullName())) {
                                    record.setFullName(nameNew);
                                    item.getDetailTextView().setText(Html.fromHtml("<big><font color=\"black\">" +
                                            (nameNew) + "</font></big>"));
                                    modified = true;
                                }
                                QMUIKeyboardHelper.hideKeyboard(builder.getEditText());
                                dialog.dismiss();
                            }
                        })
                        .addAction("取消", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                QMUIKeyboardHelper.hideKeyboard(builder.getEditText());
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            } else {
                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(EditItemActivity.this);
                builder.setPlaceholder(Double.toString(record.getDatas()[loc - 1]))
                        .setDefaultText("")
                        .setTitle(headers[loc])
                        .setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
                        .addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                String dataNew = builder.getEditText().getText().toString().trim();
                                if (dataNew.length() > 0) {
                                    try {
                                        double v = Double.parseDouble(dataNew);
                                        if (v != record.getDatas()[loc - 1]) {
                                            record.getDatas()[loc - 1] = v;
                                            if (v > 0)
                                                item.getDetailTextView().setText(Html.fromHtml("<b><big><font color=\"#FF6A00\">" + v + "</font></big></b>"));
                                            else
                                                item.getDetailTextView().setText(Double.toString(v));
                                            modified = true;
                                        }
                                    } catch (NumberFormatException e) {
                                        final QMUITipDialog tip = new QMUITipDialog.Builder(EditItemActivity.this)
                                                .setTipWord("输入的内容不是有效数字")
                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                .create(true);
                                        tip.show();
                                        item.getDetailTextView().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                tip.dismiss();
                                            }
                                        }, 3000);
                                    }
                                }
                                QMUIKeyboardHelper.hideKeyboard(builder.getEditText());
                                dialog.dismiss();
                            }
                        })
                        .addAction("取消", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                QMUIKeyboardHelper.hideKeyboard(builder.getEditText());
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        QMUIStatusBarHelper.translucent(this);
        QMUIStatusBarHelper.setStatusBarLightMode(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Intent intent = getIntent();
        int index = intent.getIntExtra("index", 0);
        headers = ExcelUtil.getInstance().getHeaders();
        if (index == 0) {
            record = new Record();
            record.setIndex(0);
            record.setFullName("");
            record.setDatas(new double[headers.length - 1]);
        } else {
            record = ExcelUtil.getInstance().find(index).makeCopy();
        }
        topbarConfig(index);
        lvEditListConfig();
    }


    private void lvEditListConfig() {
        QMUIGroupListView lvEditList = findViewById(R.id.lv_edit_list);
        QMUIGroupListView.Section section = QMUIGroupListView.newSection(this);
        for (int i = 0; i < headers.length; i++) {
            QMUICommonListItemView item = lvEditList.createItemView(headers[i]);
            if (i == 0) {
                if (record.getFullName().length() == 0) {
                    item.setDetailText(" ");
                } else {
                    item.setDetailText(Html.fromHtml("<big><font color=\"black\">"
                            + (record.getFullName()) + "</font></big>"));
                }
            } else {
                double value = record.getDatas()[i - 1];
                if (value > 0.)
                    item.setDetailText(Html.fromHtml("<b><big><font color=\"#FF6A00\">" + value + "</font></big></b>"));
                else
                    item.setDetailText(Double.toString(value));
            }
            item.setTag(i);
            section.addItemView(item, clickListener);
        }
        section.setTitle("第 " + (record.getIndex() > 0 ? record.getIndex() : ExcelUtil.getInstance().getRecordCount() + 1) + " 条记录");
        section.setShowSeparator(false);
        section.addTo(lvEditList);
    }

    @Override
    public void onBackPressed() {
        if (modified) {
            new QMUIDialog.MenuDialogBuilder(EditItemActivity.this)
                    .addItem("保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveRecord();
                            modified = false;
                            onBackPressed();
                        }
                    })
                    .addItem("放弃", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            modified = false;
                            onBackPressed();
                        }
                    })
                    .setTitle("是否保存修改?")
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .create()
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void topbarConfig(int index) {
        QMUITopBar topbar = findViewById(R.id.topbar);
        if (index == 0)
            topbar.setTitle("添加");
        else
            topbar.setTitle("编辑");
        Button btnCancel = topbar.addLeftTextButton("取消", R.id.qmui_popup_close_btn_id);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        final Button btnSave = topbar.addRightTextButton("保存", R.id.qmui_popup_close_btn_id);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final QMUITipDialog dialog;
                if (modified) {
                    if (saveRecord()) {
                        modified = false;
                        dialog = new QMUITipDialog.Builder(EditItemActivity.this)
                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                .setTipWord("保存成功")
                                .create();
                        dialog.show();
                    } else {
                        dialog = new QMUITipDialog.Builder(EditItemActivity.this)
                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                .setTipWord("保存失败, 请检查存储空间是否已满")
                                .create();
                        dialog.show();
                    }
                } else {
                    dialog = new QMUITipDialog.Builder(EditItemActivity.this)
                            .setTipWord("没有进行修改")
                            .create();
                    dialog.show();
                }
                btnSave.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 3000);
            }
        });
    }

    private boolean saveRecord() {
        try {
            ExcelUtil.getInstance().Modify(record);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
