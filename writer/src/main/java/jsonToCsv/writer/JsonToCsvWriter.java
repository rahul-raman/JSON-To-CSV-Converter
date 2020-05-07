package jsonToCsv.writer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class flattens JSON and then writes that JSON to different CSVs as per
 * CXDB requirement
 * 
 * @author rahul.raman
 * 
 */
public class JsonToCsvWriter {

	HashMap<String, Csvs> csvLists = new HashMap<String, Csvs>();
	HashMap<String,Object> abc = new HashMap<String,Object>();
	List<String> keyNameofArray = new ArrayList<String>();
	// ArrayList aa = new Arraylist

	class Csvs {
		public Set<String> headers = new LinkedHashSet<String>();
		public ArrayList<LinkedHashMap<String, String>> rowData = new ArrayList<LinkedHashMap<String, String>>();

		public void dumprecords(String fileName) {
			for(int k=0;k<rowData.size();k++)
			{
				headers = collectOrderedHeaders(rowData);
				for (LinkedHashMap<String, String> row : rowData) {

					writeLargeFile(rowData, ",", fileName, headers);
					System.out.println(row);
				}
				
			}

		}
		
		/**
		 * Get the CSV header.
		 * 
		 * @author rahul.raman
		 * @param flatJson Flattened JSON Obtained
		 *
		 * @return a Set of headers
		 */
		public Set<String> collectOrderedHeaders(List<LinkedHashMap<String, String>> flatJson) {
	        Set<String> headers = new LinkedHashSet<String>();
	        for (Map<String, String> map : flatJson) {
	        	headers.addAll(map.keySet());
	        }
	        return headers;
	    }    
		
		/**
		 * @author rahul.raman Get separated columns used a separator (comma, semi
		 *         column, tab).
		 *
		 * @param headers The CSV headers
		 * @param map     Map of key-value pairs contains the header and the value
		 *
		 * @return a string composed of columns separated by a specific separator.
		 */
		   private String getSeperatedColumns(Set<String> headers, Map<String, String> map, String separator) {
		        List<String> items = new ArrayList<String>();
		        for (String header : headers) {
		            String value = map.get(header) == null ? "" : map.get(header).replaceAll(",", "||"); 
		            items.add(value);
		        }

		        return StringUtils.join(items.toArray(), separator);
		    }
		   
		   /**
			 * @author rahul.raman Write the given CSV from a flat json to the given file.
			 * 
			 * @param flatJson  Flattened JSON Obtained
			 * @param separator how do you want to separate the values
			 * @param fileName  fileName of the newly created CSVs
			 * @param headers
			 */
		   public void writeLargeFile(List<LinkedHashMap<String, String>> flatJson, String separator, String fileName, Set<String> headers){
		    	String csvString;
		        csvString = StringUtils.join(headers.toArray(), separator) + "\n";
		        File file = new File("files/"+fileName);
		        //File file = new File(fileName);
		        
		        try {
		            // ISO8859_1 char code to Latin alphabet
		            FileUtils.write(file, csvString, "ISO8859_1");
		        	
		            
		            for (Map<String, String> map : flatJson) {
		            	csvString = "";
		            	csvString = getSeperatedColumns(headers, map, separator) + "\n";
		            	//Files.write(Paths.get(fileName), csvString.getBytes("ISO8859_1"), StandardOpenOption.APPEND);
		            	Files.write(Paths.get("files/"+fileName), csvString.getBytes("ISO8859_1"), StandardOpenOption.APPEND);
		            }   
		                     
		        } catch (IOException e) {
		            //LOGGER.error("CSVWriter#writeLargeFile(flatJson, separator, fileName, headers) IOException: ", e);
		        }
		    }    
	}

	String fileContent = "";

	// Main Loop write your logic here
	/**
	 * @author rahul.raman Start Flattening JSON Once Flattening is done, call
	 *         generateCSV method to finally create repective CSVs
	 *
	 * @param CXDB Support ticket number, like the naming convention we provide for
	 *             Key in our Csvs
	 */
	public void parseJSON(String CXDBv_01_SUPNumber,String JsonFileName,String parentCSVFileName) {

		System.out.println("Started.");
		try {
			// read the JSON Files
			fileContent = FileUtils.readFileToString(new File("files/"+JsonFileName+".json"));
			JSONObject mainJson = new JSONObject(fileContent);
			parseRootNode(CXDBv_01_SUPNumber,parentCSVFileName+".csv", mainJson, 0);
			countOfJsonArray(CXDBv_01_SUPNumber,keyNameofArray,mainJson);
			generatecsv();
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author rahul.raman
	 * 
	 *         Flatten the root node the main Parent JSON
	 *
	 * @param fileName   The fileName.csv of csv
	 * @param jsonObject JSON Object containing the whole Json
	 * @param CXDB       Support ticket number, like the naming convention we
	 *                   provide for Key in our Csvs
	 * @param rowNumber  On which to write in the CSV.
	 */
	public void parseRootNode(String CXDBv_01_SUPNumber,String fileName, JSONObject jsonObject, int rowNumber) {

		if (!csvLists.containsKey(fileName)) {
			csvLists.put(fileName, new Csvs());
		}

		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			String keyName = keys.next();
			Object node = jsonObject.get(keyName);

			if (node instanceof JSONObject) {

				System.out.println("parent: " + keyName);
				parseJsonNode(CXDBv_01_SUPNumber, fileName, keyName, jsonObject.getJSONObject(keyName), rowNumber);

			} else if (node instanceof JSONArray) {
				// handle array here
				keyNameofArray.add(keyName);
				abc.put(keyName, node);

			} else {
				// it is simple key then
				if (!csvLists.containsKey(fileName)) {
					csvLists.put(fileName, new Csvs());
				}
				System.out.println("parentKey: " + keyName);
				// write this to CSV data
				if (csvLists.containsKey(fileName)) {
					Csvs tmpCsv = csvLists.get(fileName);
					// if empty
					if (tmpCsv.rowData.size() == 0) {
						tmpCsv.rowData.add(new LinkedHashMap<String, String>());
					}
					// insert the record
					// get the row count
					HashMap<String, String> tmpRow = tmpCsv.rowData.get(rowNumber);
					//tmpRow.put(keyName + "-" + node.toString(), jsonObject.get(keyName).toString());
					if(!tmpRow.containsKey("Key")||!tmpRow.containsKey("Parent"))
					{
						tmpRow.put("Key", "");
						tmpRow.put("Parent", CXDBv_01_SUPNumber);						
					}
					tmpRow.put(keyName, jsonObject.get(keyName).toString());
					
				}

			}

		}

	}

	/**
	 * @author rahul.raman
	 * 
	 *         Parse JSON Object of parent JSON
	 *
	 * @param fileName   The fileName.csv of csv
	 * @param parentname Parent Name of the JSON Object
	 * @param jsonObject JSON Object containing the whole Json
	 * @param CXDB       Support ticket number, like the naming convention we
	 *                   provide for Key in our Csvs
	 * @param rowNumber  On which to write in the CSV.
	 */
	public void parseJsonNode(String CXDBv_01_SUPNumber,String fileName, String parentname, JSONObject jsonObject, int rowNumber) {

		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			String keyName = keys.next();
			Object node = jsonObject.get(keyName);

			if (node instanceof JSONObject) {

				// System.out.println("childObject: "+parentname+"=>"+keyName);
				parseJsonNode(CXDBv_01_SUPNumber,fileName, parentname + "-" + keyName, jsonObject.getJSONObject(keyName), rowNumber);

			} 
			else if (node instanceof JSONArray) {
				// handle array here
				keyNameofArray.add(keyName);
				abc.put(keyName, node);
				//node.le

			}

			else {

				System.out.println("childKey: " + parentname + "-" + keyName);
				// write this to CSV data
				if (csvLists.containsKey(fileName)) {
					Csvs tmpCsv = csvLists.get(fileName);
					// if empty
					if (tmpCsv.rowData.size() == 0) {
						tmpCsv.rowData.add(new LinkedHashMap<String, String>());
					}
					// insert the record
					// get the row count
					HashMap<String, String> tmpRow = tmpCsv.rowData.get(rowNumber);
					if(!tmpRow.containsKey("Key")||!tmpRow.containsKey("Parent"))
					{
						tmpRow.put("Key", "");
						tmpRow.put("Parent", CXDBv_01_SUPNumber);						
					}
					tmpRow.put(parentname + "-" + keyName, jsonObject.get(keyName).toString());
					
				}

			}
		}

	}

	/**
	 * * @author rahul.raman
	 * 
	 * Count the number of Json Array in parent JSON and the JSON arrays present in
	 * JSON objects
	 * 
	 * @param keyNameofArray COntains the Key Name of JSON Arrays present
	 * @param jsonObject     JSON Object containing the whole Json
	 */
	public void countOfJsonArray(String CXDBv_01_SUPNumber,List<String> keyNameofArray, JSONObject jsonObject)
	{
		int lengthofArray = keyNameofArray.size();
		for(int i=0;i<lengthofArray;i++)
		{
			String key = keyNameofArray.get(i);
			String fileName = key+".csv";
			JSONArray node = (JSONArray) abc.get(key);
			//parseJsonArray(fileName, key,jsonObject.getJSONArray(key), 0);
			parseJsonArray(CXDBv_01_SUPNumber, fileName, key, node, 0);
			
		}
	}
	/**
	 * @author rahul.raman
	 * 
	 *         Parse JSON Array of whole JSON
	 *
	 * @param fileName   The fileName.csv of csv
	 * @param parentname Parent Name of the JSON Object
	 * @param jsonArray  JSON Array containing the whole Json Array
	 * @param rowNumber  On which to write in the CSV.
	 */
	public void parseJsonArray(String CXDBv_01_SUPNumber, String fileName, String parentName,JSONArray jsonArray, int rowNumber) {
		int length = jsonArray.length();
		for (int i = 0; i < length; i++) {
			if (jsonArray.get(i) instanceof JSONObject) {
				// parseJsonNode(fileName,keyName,jsonObject.getJSONObject(keyName),rowNumber);
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);
				String keyName = parentName;
				parseJsonObjectofArray(CXDBv_01_SUPNumber,fileName, parentName, jsonObject, i);

			}
		}
	}
	
	/**
	 * @author rahul.raman
	 * 
	 *         Parse JSON Objects of JSON Array of whole JSON
	 *
	 * @param fileName   The fileName.csv of csv
	 * @param parentname Parent Name of the JSON Object
	 * @param jsonObject Reading JSON on by one
	 * @param rowNumber  On which to write in the CSV.
	 */
	public void parseJsonObjectofArray(String CXDBv_01_SUPNumber,String fileName,String parentName,JSONObject jsonObject,int rowNumber)
	{
		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			String arrayKeyName = keys.next();
			Object node = jsonObject.get(arrayKeyName);

			if (node instanceof JSONObject) {

				// System.out.println("childObject: "+parentname+"=>"+keyName);
				parseJsonObjectofArray(CXDBv_01_SUPNumber, fileName, parentName + "-" + arrayKeyName, jsonObject.getJSONObject(arrayKeyName), rowNumber);

			} 
			else if (node instanceof JSONArray) {
				// handle array here
				keyNameofArray.add(arrayKeyName);
				abc.put(arrayKeyName, node);
				

			}

			else {

				if (!csvLists.containsKey(fileName)) {
					csvLists.put(fileName, new Csvs());
				}
				System.out.println("childKey: " + arrayKeyName);
				// write this to CSV data
				if (csvLists.containsKey(fileName)) {
					Csvs tmpCsv = csvLists.get(fileName);
					// if empty

					while(tmpCsv.rowData.size()==rowNumber)
					{
						tmpCsv.rowData.add(new LinkedHashMap<String, String>());
					}
					// insert the record
					// get the row count
					HashMap<String, String> tmpRow = tmpCsv.rowData.get(rowNumber);
					if(!tmpRow.containsKey("Key")||!tmpRow.containsKey("Parent"))
					{
						tmpRow.put("Key", "");
						tmpRow.put("Parent", CXDBv_01_SUPNumber);					
					}
					tmpRow.put(arrayKeyName, jsonObject.get(arrayKeyName).toString());
				}

			}
		}

	}
	
	/**
	 * @author rahul.raman
	 * 
	 *         Generate CSV on by one according the .csv key present in csvLists
	 *
	 */
	public void generatecsv() {

		for (int i = 0; i < csvLists.keySet().toArray().length; i++) {
			String fileName = (String) csvLists.keySet().toArray()[i];
			Csvs tmpcsv = csvLists.get(csvLists.keySet().toArray()[i]);
			tmpcsv.dumprecords(fileName);
		}

	}
//endregion

	public static void main(String[] args) throws Exception {

		JsonToCsvWriter obj = new JsonToCsvWriter();
		obj.parseJSON("CXDBv75_01_SUP-61591","ElizaAdams","PersonADD");
	}

}