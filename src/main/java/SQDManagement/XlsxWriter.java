package SQDManagement;

import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import java.io.File;
import java.io.FileInputStream;

class XlsxWriter {
	private static final String TAG = "XlsxWriter";
	private String mPath = null;
	private String mSeqName = null;
	private Workbook mWorkbook = null;
	private Sheet mCurrentSheet = null;
	private Row mHeaderRow = null;
	private Row mDataRow = null;
	private int mColIndex = 0;
	private CellStyle mHeaderStyle = null;
	private CellStyle mDataCellStyle = null;
	private int mHeaderColorId = 0;
		
	XlsxWriter (String path, String seqName) throws Exception {
		mPath = path;
		mSeqName = seqName;
		initWorkbook();
	}

	@Override
	protected void finalize() throws Exception {
		if (mWorkbook != null) {
			mWorkbook.close();
			mWorkbook = null;
		}
		mHeaderStyle = null;
		mDataCellStyle = null;
	}
	
	private void initWorkbook () throws Exception {
		if (Utils.isEmpty(mPath) ||  Utils.isEmpty(mSeqName)) {
			Log.e(TAG, "initWorkbook() path and seq name is empty");
			return;
		}
		mWorkbook = new XSSFWorkbook();
		
		// init sheet
		mCurrentSheet = mWorkbook.createSheet(mSeqName);
		
		// init styles
		mHeaderStyle = mWorkbook.createCellStyle();
		mHeaderStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		mHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		mHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		mHeaderStyle.setWrapText(true);
		mHeaderStyle.setBorderBottom(BorderStyle.THIN);
		mHeaderStyle.setBorderTop(BorderStyle.THIN);
		mHeaderStyle.setBorderLeft(BorderStyle.THIN);
		mHeaderStyle.setBorderRight(BorderStyle.THIN);
		
		mDataCellStyle = mWorkbook.createCellStyle();
		mDataCellStyle.setAlignment(HorizontalAlignment.LEFT);
		mDataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		mDataCellStyle.setWrapText(true);
		mDataCellStyle.setBorderBottom(BorderStyle.THIN);
		mDataCellStyle.setBorderTop(BorderStyle.THIN);
		mDataCellStyle.setBorderLeft(BorderStyle.THIN);
		mDataCellStyle.setBorderRight(BorderStyle.THIN);

		// first row
		Row fistRow = mCurrentSheet.createRow(0);
		Cell cell = fistRow.createCell(0);
		cell.setCellValue("EXPORTED DATA FOR SEQUENCE {" + mSeqName + "}");
		
		// second row and 3nd row
		mHeaderRow = mCurrentSheet.createRow(1);
		mDataRow = mCurrentSheet.createRow(2);
	}
	
	void setNextColorForHeader() {
		mHeaderColorId += 1;
		if (mHeaderColorId == 1) {
			mHeaderStyle.setFillBackgroundColor(IndexedColors.LIGHT_BLUE.getIndex());
			mHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			mHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		}
		else if (mHeaderColorId == 2) {
			mHeaderStyle.setFillBackgroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
			mHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			mHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
		}
		else {
			mHeaderStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			mHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			mHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());	
		}
	}
	
	// write data to the cell
	void writeData(String tag, String content) {
		if (mWorkbook == null) return;
		
		mCurrentSheet.setColumnWidth(mColIndex, 20 * 256);
		
		Cell tagCell = mHeaderRow.createCell(mColIndex);
		tagCell.setCellValue(tag);
		tagCell.setCellStyle(mHeaderStyle);
		tagCell.setCellType(CellType.STRING);
		
		Cell dataCell = mDataRow.createCell(mColIndex);
		dataCell.setCellValue(content);
		dataCell.setCellStyle(mDataCellStyle);
		dataCell.setCellType(CellType.STRING);
		mColIndex++;
	}
	
	void save () throws Exception {
		if (mWorkbook == null) {
			return;
		}
		FileOutputStream outStream = new FileOutputStream(mPath + "/[McdcAstahTool]_" + mSeqName + ".xlsx");

		mWorkbook.write(outStream);
		outStream.close();
		mWorkbook.close();
	}
}
