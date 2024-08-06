package SQDManagement;

import java.util.Scanner;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import java.io.File; 
import java.io.FileInputStream; 
import java.io.IOException; 
//import org.apache.poi.xssf.usermodel.XSSFSheet; 
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.apache.commons.compress.utils.ArchiveUtils;
import org.apache.poi.ss.usermodel.*;

import com.change_vision.jude.api.inf.model.INamedElement;

class ImportDataHolder {
	private final static String TAG = "ImportDataHolder";
	private String mFilePath = "";
	
	class SequenceInfo {
		HashMap<String, String> data = new HashMap<String, String>();
		public String toString() {
			String res = "";
			for (Map.Entry<String, String> entry : data.entrySet()) {
				res += "{" + entry.getKey() + "," + entry.getValue() + "} ";
			}
			return res;
		}
		
		String get(String key) {
			if (data.containsKey(key)) {
				return data.get(key);
			}
			return null;
		}
	}
	private HashMap<String, SequenceInfo> mHolder = null;
	
	
	/* =========================================================================================================
	 * contructor and init function
	* =========================================================================================================
	*/
	ImportDataHolder (String filePath) throws Exception {
		mFilePath = filePath;
		mHolder = new HashMap<>();
		init();
	}
	
	private void init() throws Exception {
		readInputFile();
	}
	
	boolean isSeqExisted(String sqName) {
		return mHolder.containsKey(sqName);
	}
	
	SequenceInfo getSeqInfo (String sqName) {
		if (isSeqExisted(sqName)) {
			return mHolder.get(sqName);
		}
		return null;
	}
	
	ArrayList<String> getNewImportedSequence(INamedElement[] seqDgs) {
		if (seqDgs == null) {
			return null;
		}
		ArrayList<String> ret = new ArrayList<>();
		for (String importedItem : mHolder.keySet()) {
			boolean isExisted = false;
			for (INamedElement seq : seqDgs) {
				if (importedItem.equals(Utils.trim(seq.getName()))) {
					isExisted = true;
					break;
				}
			}
			if (!isExisted) {
				ret.add(importedItem);
			}
		}
		return ret;
	}
	
	// function for testing
	@Override
	public String toString() {
		Log.d(TAG, "DataHolder.toString():");
		for (Map.Entry<String, SequenceInfo> entry : mHolder.entrySet()) {
			Log.d(TAG, entry.getKey() + ": " + entry.getValue().toString());
		}
		return "";
	}
	
	/*
	 * ======================================================================================
	 * 						Read.xlsx
	 * ======================================================================================
	 */
	private static final int READ_STEP_UNKOWN 					= 0;
	private static final int READ_STEP_LIFELINE 				= 1;
	private static final int READ_STEP_MESSAGES					= 2;
	private static final int READ_STEP_COMBINEFRAGMENT			= 3;
	private static final int READ_STEP_COMMENT					= 4;
	
	static String getCellValue (Cell cell) {
		if (cell == null) return "";
		String res = "";
		CellType type = cell.getCellType();
		if (type == CellType.STRING) {
			res = Utils.trim(cell.getStringCellValue());
		} else if (type == CellType.NUMERIC) {
			res = String.valueOf(cell.getNumericCellValue());
			if (res.endsWith(".0")) {
				res = res.substring(0, res.length() - 2);
			}
		} else if (type == CellType.BOOLEAN) {
			res = String.valueOf(cell.getBooleanCellValue());
		} else {
			res = "";
		}
		return res;
	}
	
	private void readInputFile() throws Exception
	{
		if (Utils.isEmpty(mFilePath)) {
			Log.e(TAG, "readInputFile() filepath is empty");
			return;
		}
		try {
			Log.d(TAG, "readInputFile() file: " + mFilePath);
			FileInputStream file = new FileInputStream(new File(mFilePath)); 
			XSSFWorkbook workbook = new XSSFWorkbook(file); 
			XSSFSheet sheet = workbook.getSheetAt(0);
			if (sheet == null) {
				Log.e(TAG, "readInputFile() there is no any sheet");
				return;
			}
			Iterator<Row> rowIterator = sheet.iterator();
			
			String sqName = "";
			int readStep = READ_STEP_UNKOWN;
			Row tagRow = null;
			int tagSize = 0;
			Cell cell = null;
			String tag = null;
			String value = null;
			SequenceInfo seqInfo = null;
			
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (row.getRowNum() == 0) {
					continue;
				}
				else if (row.getRowNum() == 1) {
					tagRow = row;
					tagSize = tagRow.getLastCellNum() + 0;	 // exept column 0
					continue;
				}
				
				// get sequence name
				cell = row.getCell(0);
				if (Utils.isEmpty(getCellValue(cell)) == false) {
					sqName = getCellValue(cell);
					if (mHolder.containsKey(sqName)) {
						Log.d(TAG, "readInputFile() WARNING duplicate sequence name (" + sqName + "). ignore this information");
						continue;
					}
					seqInfo = new SequenceInfo();
					mHolder.put(sqName, seqInfo);
				}
				else {
					Log.e(TAG, "readInputFile() sqName is empty at " + row.getRowNum());
					continue;		// ignore this row
				}
				
				// read all row
				for (int i = 1; i < tagSize; i++) {
					tag = getCellValue(tagRow.getCell(i));
					value = getCellValue(row.getCell(i));
					if (Utils.isEmpty(tag)) {
						Log.e(TAG, "readInputFile() KEY tag is empty at {" + row.getRowNum() + ", " + i + "}");
						continue;	// ignore this colmn
					}
					seqInfo.data.put(tag, value);
				}
			}
			file.close(); 
			toString();
			Log.d(TAG, "readInputFile() DONE");
        }
        catch (Exception e) { 
            Log.e(TAG, "readInputFile() exeption " + e.getMessage());
			StackTraceElement[] traces = e.getStackTrace();
			for (StackTraceElement element : traces) {
				Log.e("ExportSeqDataAction", element.getClassName() + ":" + element.getLineNumber() + " " + element.getMethodName());
			}
            throw e;
        } 
	}
}