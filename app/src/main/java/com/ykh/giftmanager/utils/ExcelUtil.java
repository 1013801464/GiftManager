package com.ykh.giftmanager.utils;

import android.content.Context;
import android.util.Log;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

//import_excel org.apache.poi.ss.usermodel.;

public class ExcelUtil {
    private static final String TAG = "ExcelUtil";
    private static ExcelUtil instance;
    private List<Record> records = new ArrayList<>(1000);
    private String[] headers;
    private String filePathInData = null;
    private boolean isDebugging = true;

    public static ExcelUtil getInstance() {
        if (instance == null)
            instance = new ExcelUtil();
        return instance;
    }

    public void init(Context context) throws IOException {
        InputStream src = context.getAssets().open("books.xlsx");
        filePathInData = context.getFilesDir() + File.separator + "data.xlsx";
        File fileInData = new File(filePathInData);
        if (!fileInData.exists()) {
            copyFile(src, fileInData);
        }
    }

    // 修改或添加记录
    public void Modify(Record newRecord) throws IOException {
        if (newRecord.getIndex() == 0) {   // 表示要添加记录
            newRecord.setIndex(records.size() + 1);
            records.add(newRecord);
        } else if (newRecord.getIndex() <= records.size()) { // 例如, records 中有4条记录, index=1~4, 此时 record 的 index<=4 ...
            Record oldRecord = records.get(newRecord.getIndex() - 1);
            oldRecord.setFullName(newRecord.getFullName());
            for (int i = 0; i < newRecord.getDatas().length; i++) {
                oldRecord.getDatas()[i] = newRecord.getDatas()[i];
            }
            oldRecord.toHTML(true);
        } else {
            return;
        }
        save();
    }

    public int getRecordCount() {
        return records.size();
    }

    public Record find(int index) {
        if (index >= 1 && index <= records.size()) {
            return records.get(index - 1);
        } else {
            return null;
        }
    }

    public List<List<Record>> find(String name) {
        String[] keys = name.split(" ");
        boolean[] matchingKeys = new boolean[records.size()];
        for (int i = 0; i < records.size(); i++) {
            matchingKeys[i] = true;
        }
        for (String key : keys) {
            for (int i = 0; i < records.size(); i++) {
                matchingKeys[i] &= records.get(i).getFullName().contains(key);
            }
        }
        LinkedList<List<Record>> groupedResult = new LinkedList<>();
        List<Record> bestMatchRecords = null;
        for (int i = 0; i < records.size(); i++) {
            if (matchingKeys[i]) {
                Record record = records.get(i);
                // 如果本条记录是精确解, 则添加到 groupedResult 的头部
                if (record.getAccurateName().equals(name)) {
                    if (bestMatchRecords == null) {
                        bestMatchRecords = new ArrayList<>(5);
                        groupedResult.addFirst(bestMatchRecords);
                    }
                    bestMatchRecords.add(record);
                } else { // 如果本条记录是近似解,
                    boolean exists = false;
                    for (List<Record> group : groupedResult) {
                        if (group.get(0).getAccurateName().equals(record.getAccurateName())) {
                            group.add(record);
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        List<Record> group = new LinkedList<>();
                        group.add(record);
                        groupedResult.add(group);
                    }
                }
            }
        }
        return groupedResult;
    }

    public String[] getHeaders() {
        return headers;
    }

    public boolean tryLoad(String filePath) throws IOException, InvalidFormatException, NumberFormatException {
        File f = new File(filePath);
        FileInputStream fis = new FileInputStream(f);
        Workbook wb = WorkbookFactory.create(fis);
        fis.close();
        Sheet sheet = wb.getSheetAt(0);

        // Get Headers
        Row row0 = sheet.getRow(0);
        int nCol0 = row0.getLastCellNum();
        int i, j;
        for (j = 0; j < nCol0; j++) {
            Cell cell = row0.getCell(j);
            if (cell == null || cell.getRichStringCellValue().toString().trim().isEmpty()) break;
        }
        int headerLength = j;

        // Get All Rows
        int nRow = sheet.getLastRowNum();
        for (i = 1; i <= nRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) break;
            Cell cell0 = row.getCell(0);
            if (cell0 != null) {
                if (cell0.getCellType() != Cell.CELL_TYPE_STRING) return false;
                String name = cell0.getStringCellValue().trim();
                if (name.length() == 0) break;
                for (j = 1; j < headerLength; j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        int cellType = cell.getCellType();
                        if (cellType == Cell.CELL_TYPE_STRING) {
                            String value = cell.getStringCellValue();
                            Integer.parseInt(value);
                        } else if (cellType == Cell.CELL_TYPE_NUMERIC || cellType == Cell.CELL_TYPE_FORMULA) {
                            cell.getNumericCellValue();
                        }
                    }
                }
            } else {
                break;
            }
        }
        return headerLength > 0;
    }

    public void load() throws IOException {
        File fileInData = new File(filePathInData);
        Log.e(TAG, "load: " + filePathInData);
        FileInputStream fis = new FileInputStream(fileInData);
        Workbook wb = null;
        try {
            wb = WorkbookFactory.create(fis);
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        fis.close();
        Sheet sheet = wb.getSheetAt(0);
        records.clear();

        // Get Headers
        Row row0 = sheet.getRow(0);
        int nCol0 = row0.getLastCellNum();
        headers = new String[nCol0];
        int i, j;
        for (j = 0; j < nCol0; j++) {
            Cell cell = row0.getCell(j);
            if (cell != null)
                headers[j] = cell.getRichStringCellValue().toString().trim();
            if (cell == null || headers[j].isEmpty()) break;
        }
        headers = Arrays.copyOfRange(headers, 0, j);

        // Get All Rows
        int nRow = sheet.getLastRowNum();
        for (i = 1; i <= nRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) break;
            Cell cell0 = row.getCell(0);
            if (cell0 != null) {
                String name = cell0.getStringCellValue().trim();
                if (name.length() == 0) break;
                Record record = new Record();
                record.setIndex(i);
                record.setFullName(name);
                record.setDatas(new double[headers.length - 1]);
                for (j = 1; j < headers.length; j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        int cellType = cell.getCellType();
                        if (cellType == Cell.CELL_TYPE_STRING) {
                            String value = cell.getStringCellValue();
                            try {
                                record.getDatas()[j - 1] = Integer.parseInt(value);
                            } catch (NumberFormatException e) {
                                record.getDatas()[j - 1] = 0;
                            }
                        } else if (cellType == Cell.CELL_TYPE_NUMERIC || cellType == Cell.CELL_TYPE_FORMULA) {
                            double value = cell.getNumericCellValue();
                            record.getDatas()[j - 1] = value;
                        }
                    } else {
                        record.getDatas()[j - 1] = 0;
                    }
                }
                records.add(record);
            } else {
                break;
            }
        }
    }

    private void save() throws IOException {
        File file = new File(filePathInData);
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet sheet = workbook.createSheet();

        // 创建标题行
        Row row0 = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row0.createCell(i);
            cell.setCellValue(headers[i]);
        }
        for (int i = 0; i < records.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Cell cell = row.createCell(0);
            cell.setCellValue(records.get(i).getFullName());
            double[] datas = records.get(i).getDatas();
            for (int j = 0; j < datas.length && j < headers.length; j++) {
                if (datas[j] > 0.)
                    row.createCell(j + 1).setCellValue(datas[j]);
            }
        }

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
    }

    private void copyFile(InputStream src, File dest) throws IOException {
        boolean res = dest.createNewFile();
        Log.e(TAG, "copyFile: " + res);
        FileOutputStream fos = new FileOutputStream(dest);
        byte[] buffer = new byte[1024 * 1024];
        int byteCount;
        while ((byteCount = src.read(buffer)) != -1) {
            Log.e(TAG, "copyFile: " + byteCount);
            fos.write(buffer, 0, byteCount);
        }
        fos.flush();
        src.close();
        fos.close();
    }

    public void makeAllHTML() {
        for (Record r : records) {
            r.toHTML(true);
        }
    }

    public void addColumn(String colName) throws IOException {
        String[] newHeaders = new String[headers.length + 1];
        System.arraycopy(headers, 0, newHeaders, 0, headers.length);
        newHeaders[headers.length] = colName;
        headers = newHeaders;
        save();
    }

    public void exportExcel(File dest) throws IOException {
        if (!dest.exists()) {
            boolean newFile = dest.createNewFile();
            if (!newFile) {
                throw new IOException("创建文件失败");
            }
        }
        File fileInData = new File(filePathInData);
        if (isDebugging) Log.e(TAG, "exportExcel: " + filePathInData);
        InputStream fis = new FileInputStream(fileInData);
        copyFile(fis, dest);
    }

    public void importExcel(File src) throws IOException {
        File fileInData = new File(filePathInData);
        InputStream fis = new FileInputStream(src);
        copyFile(fis, fileInData);
    }
}
