package jsonToCsv.writer;

import java.io.File;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonToCsvWriter {

	HashMap<String, Csvs> csvLists = new HashMap<String, Csvs>();
	HashMap<String,Object> abc = new HashMap<String,Object>();
	List<String> keyNameofArray = new ArrayList<String>();
	// ArrayList aa = new Arraylist

	class Csvs {
		// public ArrayList<String> headers = new ArrayList<>(); //for control use this
		public ArrayList<LinkedHashMap<String, String>> rowData = new ArrayList<LinkedHashMap<String, String>>();

		public void dumprecords() {
			for (HashMap<String, String> row : rowData) {

				System.out.println(row);
			}

		}
	}

	String fileContent = "";

	// Main Loop write your logic here
	public void parseJSON(String CXDBv_01_SUPNumber) {

		System.out.println("Started.");
		try {
			// read the JSON Files
			fileContent = FileUtils.readFileToString(new File("files/ElizaAdams.json"));
			JSONObject mainJson = new JSONObject(fileContent);
			parseRootNode(CXDBv_01_SUPNumber,"PersonADD.csv", mainJson, 0);
			countOfJsonArray(CXDBv_01_SUPNumber,keyNameofArray,mainJson);
			generatecsv();
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//region parseJSON
//=====================================================================================
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

//=============================================================================
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
//=============================================================================	
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
//=============================================================================
//endregion

//region generate csv
	public void generatecsv() {

		for (int i = 0; i < csvLists.keySet().toArray().length; i++) {
	//		csvLists.get
			Csvs tmpcsv = csvLists.get(csvLists.keySet().toArray()[i]);
			tmpcsv.dumprecords();
		}

	}
//endregion

	public static void main(String[] args) throws Exception {

		JsonToCsvWriter obj = new JsonToCsvWriter();
		obj.parseJSON("CXDBv75_01_SUP-61591");
	}

}