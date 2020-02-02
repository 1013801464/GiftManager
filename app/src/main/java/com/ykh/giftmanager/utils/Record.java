package com.ykh.giftmanager.utils;

import android.text.Html;
import android.text.Spanned;

import java.util.Arrays;

public class Record {
    private int index;
    private String fullName;
    private Spanned cachedHTML;
    private String accurateName;
    private double[] datas;

    public Record makeCopy() {
        Record r = new Record();
        r.index = this.index;
        r.fullName = this.fullName;
        r.accurateName = this.accurateName;
        r.datas = new double[this.datas.length];
        System.arraycopy(this.datas, 0, r.datas, 0, this.datas.length);
        return r;
    }

    public String getAccurateName() {
        return accurateName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        this.accurateName = calcAccurateName();
    }

    public double[] getDatas() {
        return datas;
    }

    public void setDatas(double[] datas) {
        this.datas = datas;
    }

    @Override
    public String toString() {
        return "Record{" +
                "index=" + index +
                ", name='" + fullName + '\'' +
                ", datas=" + Arrays.toString(datas) +
                '}';
    }

    public Spanned toHTML(boolean forced) {
        if (!forced && cachedHTML != null) return cachedHTML;

        String[] headers = ExcelUtil.getInstance().getHeaders();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < headers.length; i++) {
            if (datas[i - 1] > 0.) {
                String strData = Double.toString(datas[i - 1]);
                if (strData.endsWith(".0")) {
                    strData = strData.substring(0, strData.length() - 2);
                }
                if (sb.length() > 0) sb.append(" ");
                sb.append(headers[i].replaceAll("\n", "")).append("<font color=\"#FF6A00\">").append(strData).append("</font>").append("元");
            }
        }
        cachedHTML = Html.fromHtml(sb.toString());
        return cachedHTML;
    }

    private String calcAccurateName() {
        int index = fullName.indexOf('(');
        if (index == -1) {
            index = fullName.indexOf("（");
            if (index == -1) {
                return fullName;
            }
        }
        return fullName.substring(0, index).trim();
    }
}
