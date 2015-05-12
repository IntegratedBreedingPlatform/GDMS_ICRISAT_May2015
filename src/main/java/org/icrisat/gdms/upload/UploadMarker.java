package org.icrisat.gdms.upload;

import java.util.ArrayList;
import java.util.HashMap;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.GDMSMain;

public interface UploadMarker {
	
	public void readExcelFile() throws GDMSException;
	
//	public String checkDataSize(GDMSMain theMainHomePage) throws GDMSException;
	
	public String validateDataInExcelSheet() throws GDMSException;

	public void createObjectsToBeDisplayedOnGUI(GDMSMain theMainHomePage) throws GDMSException;	
	
	//public ArrayList<HashMap<String, String>> getListOfMarkers();
	
	public void setFileLocation(String theAbsolutePath);
	
	public void setDataToBeUploded(ArrayList<HashMap<String, String>> theListOfSourceDataRows, ArrayList<HashMap<String, String>> listOfDataRows, ArrayList<HashMap<String, String>> listOfGIDRows);
	
	public void upload() throws GDMSException;
	
	public void validateData() throws GDMSException;
	
	public void createObjectsToBeSavedToDB() throws GDMSException;
	
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable);
	
	public String getDataUploaded();

	public ArrayList<HashMap<String, String>> getDataFromSourceSheet();

	public ArrayList<HashMap<String, String>> getDataFromDataSheet();
	
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet();
	
}
