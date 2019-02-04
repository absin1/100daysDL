package day1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SentimentClassifier {
	private static final String xslx = "/home/absin/Downloads/nlpdataset/isear.xlsx";

public static void main(String[] args) throws IOException {
		if (!new File(xslx).exists())
			convertIsearToExcel();
		// Now we have our excel sheet let's get all the emotions
		HashSet<String> emotionsSet = new HashSet<>();
		HashMap<String, String> phraseEmotions = getDistinctPhraseEmotions(emotionsSet);
		System.out.println(phraseEmotions.keySet().size());
		// Now we have a map of key against values like "I am very excited" and "joy".
		// So basically emotion detection can be viewed as a classification job, for
		// every phrase we get whether it belongs to joy, or sad category and given a
		// new phrase we predict what can it be.
		// This means that our output will always be a probability score across a fixed
		// size emotion array.
		// Let's get all the possible emotions in a list for easy ordered access.
		ArrayList<String> emotions = new ArrayList<>(emotionsSet);
		System.out.println(emotions);
		// Now let's build a configuration object for our neural network
		
	}

	private static HashMap<String, String> getDistinctPhraseEmotions(HashSet<String> emotions) throws IOException {
		HashMap<String, String> phraseEmotions = new HashMap<String, String>();
		Workbook workbook = new XSSFWorkbook(xslx);
		Sheet sheet = workbook.getSheet("isear");
		Iterator<Row> rowIterator = sheet.rowIterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			emotions.add(row.getCell(0).getStringCellValue());
			phraseEmotions.put(row.getCell(1).getStringCellValue(), row.getCell(0).getStringCellValue());
		}
		workbook.close();
		return phraseEmotions;
	}

	private static void convertIsearToExcel() {
		try (BufferedReader br = new BufferedReader(
				new FileReader(new File("/home/absin/Downloads/nlpdataset/isear.csv")))) {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("isear");
			Row headerRow = sheet.createRow(0);
			Cell cell = headerRow.createCell(0);
			cell.setCellValue("emotion");
			cell = headerRow.createCell(1);
			cell.setCellValue("phrase");

			int lineCount = 0;
			String line = br.readLine();
			while (line != null) {
				if (lineCount == 0) {
					line = br.readLine();
					lineCount++;
					continue;
				}
				// System.out.println(line);
				String[] split = line.split("\\|");
				System.out.println(split[36] + ">>" + split[40]);
				if (split[40].toLowerCase().contains("no response")) {
					line = br.readLine();
					continue;
				}
				Row row = sheet.createRow(lineCount);
				cell = row.createCell(0);
				cell.setCellValue(split[36]);
				cell = row.createCell(1);
				cell.setCellValue(split[40]);
				line = br.readLine();
				lineCount++;
			}
			FileOutputStream fileOut = new FileOutputStream(xslx);
			workbook.write(fileOut);
			fileOut.close();
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
