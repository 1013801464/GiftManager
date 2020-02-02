package com.ykh.giftmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.qmuiteam.qmui.layout.QMUIButton;
import com.qmuiteam.qmui.util.QMUIKeyboardHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.ykh.giftmanager.utils.ExcelUtil;
import com.ykh.giftmanager.utils.Record;

import java.util.List;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private boolean isDebuging = true;
    private QMUIGroupListView lvResultList;
    private View.OnClickListener itemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof QMUICommonListItemView) {
                Record tag = (Record) v.getTag();
                if (tag.getIndex() > 0) {
                    Intent intent = new Intent(SearchActivity.this, EditItemActivity.class);
                    intent.putExtra("index", tag.getIndex());
                    startActivity(intent);
                }
            }
        }
    };
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        QMUIStatusBarHelper.translucent(this);
        QMUIStatusBarHelper.setStatusBarLightMode(this);
        topbarConfig();
        etSearchConfig();
        btnCancelConfig();
        lvResultListConfig();
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    @Override
    protected void onPause() {
        QMUIKeyboardHelper.hideKeyboard(etSearch);
        super.onPause();
    }

    private void lvResultListConfig() {
        lvResultList = findViewById(R.id.lv_result_list);
//        QMUICommonListItemView item1 = lvResultList.createItemView("Item 1");
//        item1.setOrientation(QMUICommonListItemView.VERTICAL); //默认文字在左边
//
//        QMUICommonListItemView item2 = lvResultList.createItemView("Item 2");
//        item2.setDetailText("在右方的详细信息");//默认文字在左边   描述文字在右边
//
//        QMUICommonListItemView item3 = lvResultList.createItemView("Item 3");
//        item3.setOrientation(QMUICommonListItemView.VERTICAL);
//        item3.setDetailText("在标题下方的详细信息");//默认文字在左边   描述文字在标题下边
//
//        QMUICommonListItemView item4 = lvResultList.createItemView("Item 4");
//        item4.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);//默认文字在左边   右侧更多按钮
//
//        QMUICommonListItemView item5 = lvResultList.createItemView("Item 5");
//        item5.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_SWITCH);
//        item5.getSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Toast.makeText(SearchActivity.this, "checked = " + isChecked, Toast.LENGTH_SHORT).show();
//            }
//        });//默认文字在左边   右侧选择按钮
//
//        QMUICommonListItemView item6 = lvResultList.createItemView("Item 6");
//        item6.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
//        QMUILoadingView loadingView = new QMUILoadingView(SearchActivity.this);
//        item6.addAccessoryCustomView(loadingView);
//        View.OnClickListener onClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (v instanceof QMUICommonListItemView) {
//                    CharSequence text = ((QMUICommonListItemView) v).getText();
//                    Toast.makeText(SearchActivity.this, text + " is Clicked", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };//默认文字在左边   自定义加载框按钮
//
//        QMUIGroupListView.newSection(SearchActivity.this)
//                .setTitle("Section 1: 默认提供的样式")
//                .setDescription("Section 1 的描述")
//                .addItemView(item1, onClickListener)
//                .addItemView(item2, onClickListener)
//                .addItemView(item3, onClickListener)
//                .addItemView(item4, onClickListener)
//                .addItemView(item5, onClickListener)
//                .addTo(lvResultList);
//
//        QMUIGroupListView.newSection(SearchActivity.this)
//                .setTitle("Section 2: 自定义右侧 View")
//                .addItemView(item6, onClickListener)
//                .addTo(lvResultList);
    }

    private void btnCancelConfig() {
        final QMUIButton btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                finish();
            }
        });
    }

    private void etSearchConfig() {
        etSearch = findViewById(R.id.etSearch);
        etSearch.requestFocus();
        etSearch.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getApplication().getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etSearch, InputMethodManager.SHOW_FORCED);
                } else {
                    if (isDebuging) Log.e(TAG, "etSearchConfig: 弹出键盘失败!");
                }
            }
        }, 200);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                lvResultList.removeAllViews();
                if (editable.length() == 0) return;
                List<List<Record>> lists = ExcelUtil.getInstance().find(editable.toString());
                for (List<Record> list : lists) {
                    QMUIGroupListView.Section section = QMUIGroupListView.newSection(SearchActivity.this);
                    section.setTitle(list.get(0).getAccurateName());
                    for (Record record : list) {
                        QMUICommonListItemView item = lvResultList.createItemView(record.getFullName());
                        item.setOrientation(QMUICommonListItemView.VERTICAL);
                        item.getDetailTextView().setSingleLine(true);
                        item.setDetailText(record.toHTML(false));
                        item.setTag(record);
                        section.addItemView(item, itemOnClickListener);
                    }
                    section.addTo(lvResultList);
                }
            }
        });
    }

    private void topbarConfig() {
        QMUITopBar topbar = findViewById(R.id.topbar);
        topbar.setTitle(R.string.app_name);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
