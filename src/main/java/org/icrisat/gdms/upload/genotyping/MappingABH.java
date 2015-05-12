package org.icrisat.gdms.upload.genotyping;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.generationcp.middleware.dao.UserDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.GetGermplasmByNameModes;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmNameDetails;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
//import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.generationcp.middleware.pojos.workbench.WorkbenchRuntimeData;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.MapOptionsListener;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.genotyping.MarkerInfoBean;
import org.icrisat.gdms.upload.genotyping.MapCharArrayCompositeKey;
import org.icrisat.gdms.upload.genotyping.MappingPopCharValuesBean;
import org.icrisat.gdms.upload.genotyping.MarkerMetaDataBean;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.marker.UploadField;

import com.vaadin.ui.Window;


public class MappingABH implements UploadMarker {
	private String strFileLocation;
	private Workbook workbook;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
	private HashMap<Integer, String> hmOfColIndexAndMarkerName;
	private AccessionMetaDataBean accMetadataSet;
	private MarkerMetaDataBean markerMetadataSet;
	private GenotypeUsersBean datasetUser;
	private MappingPopulationBean mappingPop;
	private MappingPopCharValuesBean mappingPopValues;
	private DatasetBean dataset;
	private MarkerInfoBean addedMarker;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	//private MappingPopValues[] arrayOfMappingPopValues;
	private ManagerFactory factory;
	private GenotypicDataManager genoManager;
	
	
	private GermplasmDataManager manager;
	

	private Session localSession;
	private Session centralSession;
	
	private Session session;
	private Transaction tx;
	
	/*List<MappingAllelicSSRDArTRow> listOfMPSSRDataRows; 
	List<MappingAllelicSNPRow> listOfMPSNPDataRows; 
	List<MappingABHRow> listOfMPABHDataRows; */
	
	 ArrayList markersList=new ArrayList();
	String notMatchingDataDB="";
	String alertGN="no";
    String alertGID="no";
    String notMatchingData="";
    int size=0;
    String notMatchingGIDS="";
    
	String ErrMsg ="";
	Integer iDatasetId = null;

	String strCharValue = "A";
	int gid=0;
	int nid=0;
	
	String strMapDataDescription = "";
	String strScore = null;
	String strInstitute = null;
	String strEmail = null;
	String strPurposeOfStudy = null;
	
	int intRMarkerId = 1;
	int intRAccessionId = 1;
	//String str=
	int maxMid=0;
	int mid=0;
	
	
	int map_id=0;
	String strMapping_Type="abh";
	int iMpId = 0;  //Will be set/overridden by the function
	String strMapCharValue = "-";
    static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();
    
    SQLQuery query;
    
    Integer iACId = null;
	SortedMap mapN = new TreeMap();
	////System.out.println(",,,,,,,,,,,,,,,,,gNames="+gNames);
	ArrayList finalList =new ArrayList();
	ArrayList gidL=new ArrayList();
	Integer markerSampleIdA=1;
	
	private GDMSMain _mainHomePage;
	private HashMap<String, String> hmOfDuplicateGermNames;
	private HashMap<String, String> hmOfDuplicateGNames;
	List gNames;
	List gDupNameList;
	List gDupNameListV;
	
	ArrayList<String> dupGermplasmsList=new ArrayList<String>();
	HashMap<String, Integer> hashMapOfEntryIDandGID = new HashMap<String, Integer>();
	List<String> listOfGNamesFromTable=new ArrayList<String>();
	List<Integer> listOfGIDFromTable;
	ArrayList listOfGNames = new ArrayList<String>();
	
	boolean dupGermConfirmation;
	
	String strErrorMsg="no";
	private ArrayList<String> listOfMarkersFromTheSheet = new ArrayList<String>();
	
	@Override
	public void readExcelFile() throws GDMSException {
		try {
			String ext=strFileLocation.substring(strFileLocation.lastIndexOf("."));
			if(ext.equals(".xls")){
				workbook = Workbook.getWorkbook(new File(strFileLocation));
				strSheetNames = workbook.getSheetNames();
			}else{
				throw new GDMSException("Please check the file, it should be in excel format");				
			}
		} catch (BiffException e) {
			throw new GDMSException("Error Reading Mapping Genotype Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading Mapping Genotype Sheet - " + e.getMessage());
		}
	}

	@Override
	public String validateDataInExcelSheet() throws GDMSException {

		if (false == strSheetNames[0].equalsIgnoreCase("mapping_source")){
			throw new GDMSException("Mapping_Source Sheet Name Not Found");
		}

		if (false == strSheetNames[1].equalsIgnoreCase("mapping_datalist")){
			throw new GDMSException("Mapping_DataList Sheet Name Not Found");
		}

		//check the template fields
		for(int i = 0; i < strSheetNames.length; i++){

			String strSName = strSheetNames[i].toString();

			if(strSName.equalsIgnoreCase("Mapping_Source")) {
				Sheet sourceSheet = workbook.getSheet(strSName);
				int iNumOfRowsinSourceSheet = sourceSheet.getRows();

				String strArrayOfSourceColumns[] = {"Institute", "Principle investigator", "Email contact", "Dataset Name", "Dataset description", 
						"Genus", "Species", "Population ID", "Parent A GID", "Parent A", 
						"Parent B GID", "Parent B", "Population Size", "Population Type",
						"Purpose of the study", "Scoring Scheme",
						"Missing Data", "Creation Date", "Remark"};

				//Checking if all the columns are present
				for(int j = 0; j < strArrayOfSourceColumns.length; j++){
					String strColFromSheet = (String)sourceSheet.getCell(0, j).getContents().trim();
					if(!strArrayOfSourceColumns[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException(strArrayOfSourceColumns[j] + " Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException(strArrayOfSourceColumns[j] + " Column Name Not Found");
					}
				}	

				//Checking if values have been provided for the required columns
				for(int r = 0; r < iNumOfRowsinSourceSheet; r++){
					String strColumnName = sourceSheet.getCell(0, r).getContents().trim();
					if (strColumnName.equalsIgnoreCase("Institute") || strColumnName.equals("Principle investigator") || strColumnName.equals("Dataset Name") ||
							strColumnName.equalsIgnoreCase("Dataset description") || strColumnName.equalsIgnoreCase("Genus") || strColumnName.equalsIgnoreCase("Species") ||
							strColumnName.equalsIgnoreCase("Population ID") || strColumnName.equalsIgnoreCase("Parent A GID") || strColumnName.equalsIgnoreCase("Parent A") ||
							strColumnName.equalsIgnoreCase("Parent B GID") || strColumnName.equalsIgnoreCase("Parent B") || strColumnName.equalsIgnoreCase("Purpose of the study") ||
							strColumnName.equalsIgnoreCase("Missing Data") || strColumnName.equalsIgnoreCase("Creation Date")) {

						String strFieldValue = sourceSheet.getCell(1, r).getContents().trim();

						if(strFieldValue == null || strFieldValue == ""){
							throw new GDMSException("Please provide value for Required Field " + strColumnName + " at cell position [1," + r + "]");
						}
					}
				}
			}

			if (strSName.equalsIgnoreCase("Mapping_DataList")) {
				Sheet dataListSheet = workbook.getSheet(strSName);
				int intNoOfRows = dataListSheet.getRows();
				int intNoOfCols = dataListSheet.getColumns();
				String strArrayOfReqColumnNames[] = {"Alias", "Line"};	

				//Checking if all the columns are present in the DataList
				for(int j = 0; j < strArrayOfReqColumnNames.length; j++){
					String strColFromSheet = (String)dataListSheet.getCell(j, 0).getContents().trim();
					if(!strArrayOfReqColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException(strArrayOfReqColumnNames[j] + " Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException("Delete Empty Column at position " + j);
					}
				}

				//Checking if GIDs and Lines have been provided in all the rows
				for(int r = 1; r < intNoOfRows; r++){
					/*String strGIDalue = (String)dataListSheet.getCell(1, r).getContents().trim();
					if(strGIDalue == null || strGIDalue == ""){
						String strRowNumber = String.valueOf(dataListSheet.getCell(1, r).getRow()+1);	
						String strErrMsg = "Please provide a value at cell position " + "[1" + ", " + strRowNumber + "] in Mapping_DataList sheet.";
						throw new GDMSException(strErrMsg);
					}*/
					String strLine = (String)dataListSheet.getCell(1, r).getContents().trim();
					if(strLine == null || strLine == ""){
						String strRowNumber = String.valueOf(dataListSheet.getCell(1, r).getRow()+1);	
						String strErrMsg = "Please provide a value at cell position " + "[1" + ", " + strRowNumber + "] in Mapping_DataList sheet.";
						throw new GDMSException(strErrMsg);
					}
				}

				hmOfColIndexAndMarkerName = new HashMap<Integer, String>();
				for(int colIndex = 2; colIndex < intNoOfCols; colIndex++){
					String strMarkerName = dataListSheet.getCell(colIndex, 0).getContents().toString();
					if(!listOfMarkersFromTheSheet.contains(strMarkerName))
						listOfMarkersFromTheSheet.add(strMarkerName);
					hmOfColIndexAndMarkerName.put(colIndex, strMarkerName);
					for(int r = 0; r < intNoOfRows; r++){
						String strCellValue = (String)dataListSheet.getCell(colIndex, r).getContents().trim();
						if(strCellValue == null || strCellValue == ""){
							String strRowNumber = String.valueOf(dataListSheet.getCell(colIndex, r).getRow()+1);	
							String strErrMsg = "Please provide a value at cell position " + "[" + colIndex + ", " + strRowNumber + "] in Mapping_DataList sheet.";
							throw new GDMSException(strErrMsg);
						}
					}
				}
				listOfGNames = new ArrayList<String>();
				gDupNameList=new ArrayList<String>();
				for(int colIndex = 1; colIndex < 2; colIndex++){
					for(int r = 1; r < intNoOfRows; r++){
						String strCellValue = (String)dataListSheet.getCell(colIndex, r).getContents().trim();
						
						if(listOfGNames.contains(strCellValue)){							
							gDupNameList.add(strCellValue);							
						}
						
						listOfGNames.add(strCellValue);
					}
					
				}
				
				
				int c=1;
				ArrayList<String> gNamesList=new ArrayList<String>();
				//int c=2;
				hmOfDuplicateGermNames=new HashMap<String, String>();
				hmOfDuplicateGNames=new HashMap<String, String>();
				dupGermplasmsList=new ArrayList<String>();
				int rep=1;
				for(int d = 0; d <listOfGNames.size();d++){
					String gName="";
					if(gDupNameList.contains(listOfGNames.get(d))){
						String strGName=listOfGNames.get(d)+" (Sample "+rep+")";
						
						hmOfDuplicateGNames.put(listOfGNames.get(d).toString()+rep, strGName);
						hmOfDuplicateGermNames.put(strGName, listOfGNames.get(d).toString());
						dupGermplasmsList.add(strGName);
						rep++;
					}
				}
				
				
				
				
			}			 
		}
		
		if(listOfMarkersFromTheSheet.size()>256 || listOfGNames.size()>500){
			//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			strErrorMsg = "Data to be uploaded cannot be displayed in the table. Please click on Upload to upload the data directly";
			
		}
		
		
		return strErrorMsg;
	}


	@Override
	public void createObjectsToBeDisplayedOnGUI(GDMSMain theMainHomePage) throws GDMSException {

		_mainHomePage = theMainHomePage;
		
		//Creating a ArrayList of HashMap of fields and values from Mapping_Source sheet
		Sheet sourceSheet = workbook.getSheet(0);
		listOfDataInSourceSheet = new ArrayList<HashMap<String,String>>();

		HashMap<String, String> hmOfDataInSourceSheet = new HashMap<String, String>();

		String strInstitue = sourceSheet.getCell(1, 0).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Institute.toString(), strInstitue);

		String strPrincipalInvestigator = sourceSheet.getCell(1, 1).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PrincipleInvestigator.toString(), strPrincipalInvestigator);

		String strEmailContact = sourceSheet.getCell(1, 2).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.EmailContact.toString(), strEmailContact);

		String strDatasetName = sourceSheet.getCell(1, 3).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.DatasetName.toString(), strDatasetName);

		String strDatasetDescription = sourceSheet.getCell(1, 4).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.DatasetDescription.toString(), strDatasetDescription);

		String strGenus = sourceSheet.getCell(1, 5).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Genus.toString(), strGenus);

		String strSpecies = sourceSheet.getCell(1, 6).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Species.toString(), strSpecies);

		String strPopulationID = sourceSheet.getCell(1, 7).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PopulationID.toString(), strPopulationID);

		String strParentAGID = sourceSheet.getCell(1, 8).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ParentAGID.toString(), strParentAGID);

		String strParentA = sourceSheet.getCell(1, 9).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ParentA.toString(), strParentA);

		String strParentBGID = sourceSheet.getCell(1, 10).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ParentBGID.toString(), strParentBGID);

		String strParentB = sourceSheet.getCell(1, 11).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ParentB.toString(), strParentB);

		String strPopulationSize = sourceSheet.getCell(1, 12).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PopulationSize.toString(), strPopulationSize);

		String strPopulationType = sourceSheet.getCell(1, 13).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PopulationType.toString(), strPopulationType);

		String strPurposeOfTheStudy = sourceSheet.getCell(1, 14).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PurposeOfTheStudy.toString(), strPurposeOfTheStudy);

		String strScoringScheme = sourceSheet.getCell(1, 15).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.ScoringScheme.toString(), strScoringScheme);

		String strMissingData = sourceSheet.getCell(1, 16).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.MissingData.toString(), strMissingData);

		String strCreationDate = sourceSheet.getCell(1, 17).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.CreationDate.toString(), strCreationDate);

		String strRemark = sourceSheet.getCell(1, 18).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Remark.toString(), strRemark);

		listOfDataInSourceSheet.add(hmOfDataInSourceSheet);

		if(gDupNameList.size()>0){			
			dupGermConfirmation = false;
			//ConfirmDuplicatesInGenotypingDataUpload confirmationWindowForDupGermplasms = new ConfirmDuplicatesInGenotypingDataUpload(gDupNameListV);
			ConfirmDuplicatesInGenotypingDataUpload confirmationWindowForDupGermplasms = new ConfirmDuplicatesInGenotypingDataUpload();
			final Window messageWindow = new Window("Confirmation");
			if (null != confirmationWindowForDupGermplasms) {
				messageWindow.setContent(confirmationWindowForDupGermplasms);
				messageWindow.setWidth("500px");
				messageWindow.setClosable(true);
				messageWindow.center();
				
				if (false == _mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
					_mainHomePage.getMainWindow().addWindow(messageWindow);
				}
				messageWindow.setModal(true);
				messageWindow.setVisible(true);
			}
			
			confirmationWindowForDupGermplasms.addConfirmDuplicatesListener(new MapOptionsListener() {

				
				public void isMapRequiredOption(boolean bMapRequired) {
					//System.out.println("bMapRequired:"+bMapRequired);
					if (bMapRequired) {	
						//System.out.println("********************** no.............bMapRequired:"+bMapRequired);
						dupGermConfirmation = false;
						if (_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
							_mainHomePage.getMainWindow().removeWindow(messageWindow);
						}
						
						
					} else {
						//System.out.println(".............YES..................bMapRequired:"+bMapRequired);
						dupGermConfirmation = true;
						
						if (_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
							_mainHomePage.getMainWindow().removeWindow(messageWindow);
						}						

						
					}				
				}
				
			});
		}
		
		
		
		//Creating a ArrayList of HashMap of fields and values from Mapping_DataList sheet
		Sheet dataSheet = workbook.getSheet(1);
		int iNumOfRowsInDataSheet = dataSheet.getRows();
		int iNumOfColumnsInDataSheet = dataSheet.getColumns();
		listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();
		int r=1;
		for (int rIndex = 1; rIndex < iNumOfRowsInDataSheet; rIndex++){

			HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();

			String strAliasValue = dataSheet.getCell(0, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Alias.toString(), strAliasValue);

			/*String strGIDValue = dataSheet.getCell(1, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.GID.toString(), strGIDValue);*/

			//String strLineValue = dataSheet.getCell(2, rIndex).getContents().toString();
			String strLineValue = dataSheet.getCell(1, rIndex).getContents().toString();
			
			/*System.out.println("strLineValue:"+strLineValue);
			System.out.println(">>>>>>>>>>>>>>>>>hmOfDuplicateGNames:"+hmOfDuplicateGNames);
			System.out.println("gDupNameList:"+gDupNameList);*/
			if(gDupNameList.contains(strLineValue)){
				hmOfDataInDataSheet.put(UploadField.Line.toString(), hmOfDuplicateGNames.get(strLineValue+r));
				r++;
			}else			
				hmOfDataInDataSheet.put(UploadField.Line.toString(), strLineValue);

			//Inserting the Marker-Names and Marker-Values
			for (int cIndex = 3; cIndex < iNumOfColumnsInDataSheet; cIndex++){
				String strMarkerName = hmOfColIndexAndMarkerName.get(cIndex);
				String strMarkerValue = dataSheet.getCell(cIndex, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(strMarkerName, strMarkerValue);
			}

			listOfDataInDataSheet.add(hmOfDataInDataSheet);
		}

	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromSourceSheet() {
		return listOfDataInSourceSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromDataSheet(){
		return listOfDataInDataSheet;
	}

	@Override
	public void setDataToBeUploded(ArrayList<HashMap<String, String>> theListOfSourceDataRows, ArrayList<HashMap<String, String>> listOfDataRows, ArrayList<HashMap<String, String>> listOfGIDRows) {
		listOfDataRowsFromSourceTable = theListOfSourceDataRows;
		listOfDataRowsFromDataTable = listOfDataRows;
		listOfGIDRowsFromGIDTableForDArT = listOfGIDRows;
	}

	@Override
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}

	@Override
	public void upload() throws GDMSException {
		validateData();
		createObjectsToBeSavedToDB();
	}

	@Override
	public void validateData() throws GDMSException {

		//Checking for Column-Names and Values provided in the Mapping_Source table on the GUI
		String strColsInSourceSheet[] = {UploadField.Institute.toString(), UploadField.PrincipleInvestigator.toString(), 
				UploadField.DatasetName.toString(), UploadField.DatasetDescription.toString(), 
				UploadField.Genus.toString(), UploadField.Species.toString(), UploadField.PopulationID.toString(),
				UploadField.ParentAGID.toString(), UploadField.ParentA.toString(), UploadField.ParentBGID.toString(),
				UploadField.ParentB.toString(),
				UploadField.PurposeOfTheStudy.toString(),
				UploadField.MissingData.toString()};

		HashMap<String, String> hmOfSourceColumnsAndValuesFromGUI = listOfDataRowsFromSourceTable.get(0);
		for(int j = 0; j < strColsInSourceSheet.length; j++){
			String strCol = strColsInSourceSheet[j];
			if (false == hmOfSourceColumnsAndValuesFromGUI.containsKey(strCol)){
				throw new GDMSException(strCol + " column not found in data Mapping_Source table.");
			} else {
				//Institute, Principle-Investigator, Dataset-Name, Dataset-Description, Genus, 
				//Species, Population-ID, Parent-A-GID,  Parent-A, Parent-B-GID, Parent-B,
				//Purpose-Of-The-Study, Missing-Data, Creation-Date, 
				if (strCol.equalsIgnoreCase(UploadField.Institute.toString()) || 
						strCol.equalsIgnoreCase(UploadField.PrincipleInvestigator.toString()) || 
						strCol.equalsIgnoreCase(UploadField.DatasetName.toString()) ||
						strCol.equalsIgnoreCase(UploadField.DatasetDescription.toString()) || 
						strCol.equalsIgnoreCase(UploadField.Genus.toString()) ||  
						strCol.equalsIgnoreCase(UploadField.Species.toString()) || 
						strCol.equalsIgnoreCase(UploadField.PopulationID.toString()) || 
						strCol.equalsIgnoreCase(UploadField.ParentAGID.toString()) || 
						strCol.equalsIgnoreCase(UploadField.ParentA.toString()) || 
						strCol.equalsIgnoreCase(UploadField.ParentBGID.toString()) ||
						strCol.equalsIgnoreCase(UploadField.ParentB.toString()) || 
						strCol.equalsIgnoreCase(UploadField.PurposeOfTheStudy.toString()) ||
						strCol.equalsIgnoreCase(UploadField.MissingData.toString()) ||
						strCol.equalsIgnoreCase(UploadField.CreationDate.toString())){
					String strValue = hmOfSourceColumnsAndValuesFromGUI.get(strCol);
					if (null == strValue || strValue.equals("")){
						throw new GDMSException("Please provide a value for " +  strCol + " in data Mapping_Source sheet.");
					}
				}
			}
		}

		//Checking for Column-Names and Values provided in the Mapping_DataList table on the GUI
		/*String[] strColsInDataSheet = {UploadField.Alias.toString(), UploadField.GID.toString(), 
				UploadField.Line.toString()};*/
		String[] strColsInDataSheet = {UploadField.Alias.toString(), UploadField.Line.toString()};

		for(int cIndex = 0; cIndex < strColsInDataSheet.length; cIndex++){
			String strCol = strColsInDataSheet[cIndex];
			////System.out.println(",,,,,,,,, :"+strColsInDataSheet[cIndex]);
			for (int rIndex = 0; rIndex < listOfDataRowsFromDataTable.size(); rIndex++){
				HashMap<String, String> hmOfDataColumnsAndValuesFromGUI = listOfDataRowsFromDataTable.get(rIndex);
				if (false == hmOfDataColumnsAndValuesFromGUI.containsKey(strCol)){
					throw new GDMSException(strCol + " column not found in data Mapping_DataList table.");
				} else {
					//GID, Line
					/*if (strCol.equalsIgnoreCase(UploadField.GID.toString()) || 
							strCol.equalsIgnoreCase(UploadField.Line.toString())){*/
					if (strCol.equalsIgnoreCase(UploadField.Line.toString())){
						String strValue = hmOfDataColumnsAndValuesFromGUI.get(strCol);
						if (null == strValue || strValue.equals("")){
							throw new GDMSException("Please provide a value for " +  strCol + " column in data Mapping_DataList sheet.");
						}
					}
				}
			}	
		}
	}


	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {
		
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
		
			manager = factory.getGermplasmDataManager();
			genoManager=factory.getGenotypicDataManager();
	
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		/*sessionL=localSession.getSessionFactory().openSession();	
        sessionC=centralSession.getSessionFactory().openSession();
		
		*/
		String strGermplasmSelected = GDMSModel.getGDMSModel().getGermplasmSelected();
		System.out.println("..>>>>>>>>....:"+strGermplasmSelected);
		
		
		int strPopulationSize =0;
		Date uploadTemplateDate = new Date(System.currentTimeMillis()); 
		String strMapSelectedOnTheUI = GDMSModel.getGDMSModel().getMapSelected();
		String strMarkerForMap = GDMSModel.getGDMSModel().getMarkerForMap();
		
		HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
		//Institute, Principle investigator, Email contact, Dataset Name, Dataset description, Genus, Species, Population ID, Parent A GID, 
		//Parent A, Parent B GID, Parent B, Population Size, Population Type, Purpose of the study, 
		//Scoring Scheme, Missing Data, Creation Date, Remark
		String dataset_type="mapping";
		HashMap<String, String> hmOfSourceFieldsAndValues = listOfDataRowsFromSourceTable.get(0);
		
		Sheet sheetDataList=workbook.getSheet(1);
		Sheet sheetSource = workbook.getSheet(0);
		int rowCount=sheetDataList.getRows();
		int colCount=sheetDataList.getColumns();
		
		//String strInstitute = hmOfSourceFieldsAndValues.get(UploadField.Institute.toString());
		String strPrincipleInvestigator = hmOfSourceFieldsAndValues.get(UploadField.PrincipleInvestigator.toString());
		//String strEmailContact = hmOfSourceFieldsAndValues.get(UploadField.EmailContact.toString());
		String strDatasetName = hmOfSourceFieldsAndValues.get(UploadField.DatasetName.toString());
		String strDatasetDescription = hmOfSourceFieldsAndValues.get(UploadField.DatasetDescription.toString());
		String strGenus = hmOfSourceFieldsAndValues.get(UploadField.Genus.toString());
		String strSpecies = hmOfSourceFieldsAndValues.get(UploadField.Species.toString());
		//String strPopulationID = hmOfSourceFieldsAndValues.get(UploadField.PopulationID.toString());
		String strParentAGID = hmOfSourceFieldsAndValues.get(UploadField.ParentAGID.toString());
		String strParentA = hmOfSourceFieldsAndValues.get(UploadField.ParentA.toString());
		String strParentBGID = hmOfSourceFieldsAndValues.get(UploadField.ParentBGID.toString());
		String strParentB = hmOfSourceFieldsAndValues.get(UploadField.ParentB.toString());
		if(hmOfSourceFieldsAndValues.get(UploadField.PopulationSize.toString())=="")
			strPopulationSize=0;
		else
			strPopulationSize = Integer.parseInt(hmOfSourceFieldsAndValues.get(UploadField.PopulationSize.toString()));
		String strPopulationType = hmOfSourceFieldsAndValues.get(UploadField.PopulationType.toString());
		String strPurposeOfTheStudy = hmOfSourceFieldsAndValues.get(UploadField.PurposeOfTheStudy.toString());
		String strScoringScheme = hmOfSourceFieldsAndValues.get(UploadField.ScoringScheme.toString());
		String strMissingData = hmOfSourceFieldsAndValues.get(UploadField.MissingData.toString());
		String strCreationDate = hmOfSourceFieldsAndValues.get(UploadField.CreationDate.toString());
		String strRemark = hmOfSourceFieldsAndValues.get(UploadField.Remark.toString());
				
		String mon="";
		Calendar cal = new GregorianCalendar();
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if(month>=10) 
			mon=String.valueOf(month+1);
		else 
			mon="0"+(month+1);
		  
		 String curDate=year+"-"+mon+"-"+day;
		 
		 long datasetLastId = 0;
		 long lastId = 0;
		 
		 try{
			 datasetLastId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_DATASET);
			}catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			int intDatasetId=(int)datasetLastId;
			
			iDatasetId=intDatasetId-1;
		 
		//Assigning the User Id value based on the Principle investigator's value in the Mapping_Source sheet
		Integer iUserId = 0;
		
		
		try {
			
			iUserId=GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId();
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
		Integer iMapId = 0;
		
		
		if(null == strMapSelectedOnTheUI){
			////System.out.println(" empty");
			iMapId=0;
			
		}else{
			////System.out.println("..................    not Empty");
			try{
				long mapId = genoManager.getMapIdByName(strMapSelectedOnTheUI.toString());
				iMapId = (int)mapId;
			}catch (MiddlewareQueryException e) {
				throw new GDMSException("Error uploading Map");
			}
			//20131111: Tulasi - Used the above code to get Map Id for the Map selected on the GUI for both Allelic and ABH data
		}
		
		////System.out.println("iMapId:"+iMapId);

		String strMarkerTypeSelected = GDMSModel.getGDMSModel().getMarkerForMap(); //SSR/SNP/DArT 
		String mappingType = GDMSModel.getGDMSModel().getMappingType();
		
		/*if(mappingType.equalsIgnoreCase("AllelicData"))		
			strMapping_Type="allelic";
		else*/
		strMapping_Type="abh";
		
			
		String gids1="";
		int gidsCount=0;
		String marker="";
		String marker_type="UA";
		/*if (sheetDataList==null){
				//System.out.println("Empty Sheet");		
		}else{*/
			int intNR=sheetDataList.getRows();
			int intColRowEmpty=0;
			//int rows=sheetDataList.getRows();
			for(int i=0;i<intNR;i++){
				Cell c=sheetDataList.getCell(0,i);
				String s=c.getContents();
				if(!s.equals("")){
					intColRowEmpty=intColRowEmpty + 1;
					
				}
			}
			
			////System.out.println("listOfDataRowsFromDataTable=:"+listOfDataRowsFromDataTable);
			ArrayList listOfMarkers = new ArrayList();
			ArrayList<String> listOfMarkerNamesFromSourceTable = new ArrayList<String>();
			for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){
				HashMap<String,String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
				Iterator<String> iterator = hashMapOfDataRow.keySet().iterator();
				while(iterator.hasNext()){
					String strMarkerNameFromSourceTable = iterator.next();
					/*if (false == (strMarkerNameFromSourceTable.equals(UploadField.GID.toString()) ||
	                    strMarkerNameFromSourceTable.equals(UploadField.Line.toString()))){*/
					if (false == strMarkerNameFromSourceTable.equals(UploadField.Line.toString())){
						if( (false ==  listOfMarkerNamesFromSourceTable.contains(strMarkerNameFromSourceTable))&&(strMarkerNameFromSourceTable != "SNo")&&(strMarkerNameFromSourceTable != "Alias")&&(strMarkerNameFromSourceTable != "Line")){
							listOfMarkerNamesFromSourceTable.add(strMarkerNameFromSourceTable);
							markersList.add(strMarkerNameFromSourceTable);
							marker = marker +"'"+ strMarkerNameFromSourceTable+"',";
						}
						if( (strMarkerNameFromSourceTable != "SNo")&&(strMarkerNameFromSourceTable != "Alias")&&(strMarkerNameFromSourceTable != "Line")){
							listOfMarkers.add(strMarkerNameFromSourceTable.toLowerCase());						
						}
					}
				}
			}
			
			
			List newListL=new ArrayList();
			List newListC=new ArrayList();
			//try {	
			Object obj=null;
			Object objL=null;
			Iterator itListC=null;
			Iterator itListL=null;
			//genoManager.getMar
			
			List lstMarkers = new ArrayList();
			String markersForQuery="";
			/** retrieving maximum marker id from 'marker' table of database **/
			//int maxMarkerId=uptMId.getMaxIdValue("marker_id","gdms_marker",session);
			
			HashMap<String, Object> markersMap = new HashMap<String, Object>();	
			markersForQuery=marker.substring(0, marker.length()-1);
			
			String strQuerry="select distinct marker_id, marker_name from gdms_marker where Lower(marker_name) in ("+markersForQuery.toLowerCase()+")";
			
			//sessionC=centralSession.getSessionFactory().openSession();			
			SQLQuery queryC=centralSession.createSQLQuery(strQuerry);	
			queryC.addScalar("marker_id",Hibernate.INTEGER);	
			queryC.addScalar("marker_name",Hibernate.STRING);	
			newListC=queryC.list();			
			itListC=newListC.iterator();			
			while(itListC.hasNext()){
				obj=itListC.next();
				if(obj!=null){	
					Object[] strMareO= (Object[])obj;
		        	////System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
					lstMarkers.add(strMareO[1].toString());
					markersMap.put(strMareO[1].toString(), strMareO[0]);
					
				}
			}
					

			//sessionL=localSession.getSessionFactory().openSession();			
			SQLQuery queryL=localSession.createSQLQuery(strQuerry);		
			queryL.addScalar("marker_id",Hibernate.INTEGER);	
			queryL.addScalar("marker_name",Hibernate.STRING);	       
			newListL=queryL.list();
			itListL=newListL.iterator();			
			while(itListL.hasNext()){
				objL=itListL.next();
				if(objL!=null)	{			
					Object[] strMareO= (Object[])objL;
					////System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
					if(!lstMarkers.contains(strMareO[1].toString())){
	            		lstMarkers.add(strMareO[1].toString());	            		
	            		markersMap.put(strMareO[1].toString(), strMareO[0]);	
					}
				}
			}
			

			String strQuerryGL="select listid from listnms where listname='"+strGermplasmSelected+"'";		
			 int list_id=0;		
			newListL=new ArrayList();		
			List listData=new ArrayList();		
			newListC=new ArrayList();
			//try {	
			obj=null;
			objL=null;
			itListC=null;
			itListL=null;
			
			
			//listOfGermplasmLists.clear();
			
			//sessionL=localSession.getSessionFactory().openSession();			
			SQLQuery queryGL=localSession.createSQLQuery(strQuerryGL);		
			queryGL.addScalar("listid",Hibernate.INTEGER);	  
			
			newListL=queryGL.list();
			itListL=newListL.iterator();			
			while(itListL.hasNext()){
				objL=itListL.next();
				if(objL!=null)
					list_id=Integer.parseInt(objL.toString());					
			}
				
				
			if(list_id==0){
				//sessionC=centralSession.getSessionFactory().openSession();			
				SQLQuery queryGC=centralSession.createSQLQuery(strQuerryGL);		
				queryGC.addScalar("listid",Hibernate.INTEGER);;	
				newListC=queryGC.list();			
				itListC=newListC.iterator();			
				while(itListC.hasNext()){
					obj=itListC.next();
					if(obj!=null)		
						list_id=Integer.parseInt(obj.toString());				
				}
					
			
			}	
			
			String nonExistingListItem="";
			String nonExistingListItems="no";
			ArrayList listEntries=new ArrayList();
			String querryListData="SELECT entryid, desig, gid FROM listdata WHERE listid="+list_id+" order by entryid";
			//System.out.println(querryListData);
			if(list_id>0)
				session=centralSession.getSessionFactory().openSession();		
			else
				session=localSession.getSessionFactory().openSession();			
			SQLQuery query=session.createSQLQuery(querryListData);		
			query.addScalar("entryid",Hibernate.INTEGER);	  
			query.addScalar("desig",Hibernate.STRING);	  
			query.addScalar("gid",Hibernate.INTEGER);	  
			
			listData=query.list();
			listOfGNamesFromTable=new ArrayList<String>();
			listOfGIDFromTable=new ArrayList<Integer>();
			for(int w=0;w<listData.size();w++){
	        	Object[] strMareO= (Object[])listData.get(w);
	           // System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]+"   "+strMareO[2]);
	        	if(listOfGNames.contains(strGermplasmSelected+"-" + strMareO[0].toString())){
	        	 	listEntries.add(strGermplasmSelected+"-" + strMareO[0].toString());
		            listOfGNamesFromTable.add(strMareO[1].toString().trim());
		            listOfGIDFromTable.add(Integer.parseInt(strMareO[2].toString()));
		            hashMapOfGNameandGID.put(strMareO[1].toString().trim(), Integer.parseInt(strMareO[2].toString()));
		            hashMapOfEntryIDandGID.put(strGermplasmSelected+"-" + strMareO[0].toString(), Integer.parseInt(strMareO[2].toString()));
	        	}
	 		}
			System.out.println("listOfGNames:"+listOfGNames);
			System.out.println("listEntries:"+listEntries);
			//if(listOfDNANamesFromSourceTable.size()!=listEntries.size()){
				for(int k=0;k<listOfGNames.size();k++){
					if(!(listEntries.contains(listOfGNames.get(k)))){
						 nonExistingListItem=nonExistingListItem+listOfGNames.get(k)+"\n";
						 nonExistingListItems="yes";
					}
				}
				if(nonExistingListItems.equalsIgnoreCase("yes")){
					throw new GDMSException("Please verify the List Entries provided doesnot exist in the database\t "+nonExistingListItem );
				}
			
			
			
			
			String exists="";
			ArrayList pGidsList=new ArrayList();
			ArrayList pGNamesList=new ArrayList();
			ArrayList pNamesList=new ArrayList();
			HashMap<String, Integer> GIDsMapP = new HashMap<String, Integer>();
			
			String parentGids=sheetSource.getCell(1,8).getContents().trim()+","+sheetSource.getCell(1,10).getContents().trim();
			String gidsForQuery = "";
			HashMap<String, Integer> GIDsMap = new HashMap<String, Integer>();
			String gNames="";
			ArrayList gidsAList=new ArrayList();
			ArrayList gidNamesList=new ArrayList();
			ArrayList NamesList=new ArrayList();
			////System.out.println("rowCount=:"+rowCount);
			for(int r=1;r<rowCount;r++){	
				//gidsForQuery = gidsForQuery + sheetDataList.getCell(1,r).getContents().trim()+",";
				gNames=gNames+"'"+sheetDataList.getCell(1,r).getContents().trim()+"',";
				//gids1=gids1+sheetDataList.getCell(1,r).getContents().trim()+"!~!"+sheetDataList.getCell(2,r).getContents().trim()+",";
				//GIDsMap.put(sheetDataList.getCell(2,r).getContents().trim(),Integer.parseInt(sheetDataList.getCell(1,r).getContents().trim()));
				
				/*if(!gidNamesList.contains(Integer.parseInt(sheetDataList.getCell(1,r).getContents().trim())))
					gidNamesList.add(Integer.parseInt(sheetDataList.getCell(1,r).getContents().trim())+","+sheetDataList.getCell(2,r).getContents().trim());
				*/
				//if(!gidsAList.contains(Integer.parseInt(sheetDataList.getCell(1,r).getContents().trim())))
					//gidsAList.add(Integer.parseInt(sheetDataList.getCell(1,r).getContents().trim()));
				
				//if(!NamesList.contains(sheetDataList.getCell(1,r).getContents().trim()))
					NamesList.add(sheetDataList.getCell(1,r).getContents().trim());
				
				
				//gidsCount=gidsCount+1;
			}
			String gidsO="";
			int gnCount=0;
			
			/*for(int gn=1;gn<rowCount;gn++){					
				gids1=gids1+sheetDataList.getCell(2,gn).getContents().trim()+",";
				gnCount=gnCount+1;
			}*/
			int s=0;
			//String fGids="";
			ArrayList fGids=new ArrayList();
			String gidsRet="";
			
			//HashMap<Integer, String> GIDsMap = new HashMap<Integer, String>();
			/** arranging gid's with respect to germplasm name in order to insert into allele_values table */
			//if(gidsCount==gnCount){			
				
	           // gidsForQuery=gidsForQuery.substring(0, gidsForQuery.length()-1);
	           
	            SortedMap map = new TreeMap();
	            List lstgermpName = new ArrayList();
	            manager = factory.getGermplasmDataManager();
				//List<Name> names = null;
				
	            ArrayList gidsDBList = new ArrayList();
				ArrayList gNamesDBList = new ArrayList();
				hashMap.clear();
				System.out.println("listOfGNamesFromTable*******************:"+listOfGNamesFromTable);
				
				gidL=new ArrayList();
				mapN = new TreeMap();
				
				try{
					
					List<GermplasmNameDetails> germplasmList = manager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.NORMAL);				
					for (GermplasmNameDetails g : germplasmList) {				
			        	if(!(gidsDBList.contains(g.getNameId()))){
			        		gidsDBList.add(g.getNameId());
			        		gNamesDBList.add(g.getNVal());
			        		addValues(g.getNVal(), g.getGermplasmId());					        		
			        	}	
			        	if(!gidL.contains(g.getGermplasmId()))
			            	gidL.add(g.getGermplasmId());
			            mapN.put(g.getGermplasmId(), g.getNameId());
			            GIDsMap.put(g.getNVal().toLowerCase(), g.getGermplasmId());
			        }
					
					if(gNamesDBList.size()!=NamesList.size()){
						germplasmList = manager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.STANDARDIZED);
						for (GermplasmNameDetails g : germplasmList) {
							if(!(gidsDBList.contains(g.getNameId()))){
				        		gidsDBList.add(g.getNameId());
				        		gNamesDBList.add(g.getNVal());
				        		addValues(g.getNVal(), g.getGermplasmId());					        		
				        	}   
							if(!gidL.contains(g.getGermplasmId()))
				            	gidL.add(g.getGermplasmId());
				            mapN.put(g.getGermplasmId(), g.getNameId());
				            GIDsMap.put(g.getNVal().toLowerCase(), g.getGermplasmId());
				        }
					}
					
					if(gNamesDBList.size()!=listOfGNamesFromTable.size()){
						germplasmList = manager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.SPACES_REMOVED_BOTH_SIDES);
						for (GermplasmNameDetails g : germplasmList) {
							if(!(gidsDBList.contains(g.getNameId()))){
				        		gidsDBList.add(g.getNameId());
				        		gNamesDBList.add(g.getNVal().toLowerCase());
				        		addValues(g.getNVal().toLowerCase(), g.getGermplasmId());				        		
				        	}        	
							if(!gidL.contains(g.getGermplasmId()))
				            	gidL.add(g.getGermplasmId());
														
							mapN.put(g.getGermplasmId(), g.getNameId());
				            GIDsMap.put(g.getNVal().toLowerCase(), g.getGermplasmId());						
				        }
					}
					
				} catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
	          
	            /*
	            //System.out.println("lstgermpName="+lstgermpName);*/			           
	           if(gNamesDBList.size()==0){
	        	   alertGID="yes";
	        	   size=0;
	           }
	          
	           int gidToCompare=0;
	           String gNameToCompare="";
	           String gNameFromMap="";
	           
			/** retrieving maximum marker id from 'marker' table of database **/
			try{
				lastId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_MARKER);
			}catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			maxMid=(int)lastId;
			int parentA_nid=0;
			int parentB_nid=0;
			
			int parentAGid=Integer.parseInt(sheetSource.getCell(1,8).getContents().trim());
			String parentA=sheetSource.getCell(1,9).getContents().trim();
			
			int parentBGid=Integer.parseInt(sheetSource.getCell(1,10).getContents().trim());
			String parentB=sheetSource.getCell(1,11).getContents().trim();
			try{
			Name namesPA = null;
			namesPA=manager.getNameByGIDAndNval(parentAGid, parentA, GetGermplasmByNameModes.STANDARDIZED);
			if(namesPA==null){
				namesPA=manager.getNameByGIDAndNval(parentAGid, parentA, GetGermplasmByNameModes.NORMAL);
			}
			
			parentA_nid=namesPA.getNid();
			
			
			Name namesPB = null;
			namesPB=manager.getNameByGIDAndNval(parentBGid, parentB, GetGermplasmByNameModes.STANDARDIZED);
			if(namesPB==null){
				namesPB=manager.getNameByGIDAndNval(parentBGid, parentB, GetGermplasmByNameModes.NORMAL);
			}
			parentB_nid=namesPB.getNid();
			
			//System.out.println("parentA_nid:"+parentA_nid+"   parentB_nid:"+parentB_nid);
			
			
			} catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			try{
				List<DatasetElement> results =genoManager.getDatasetDetailsByDatasetName(strDatasetName, Database.CENTRAL);
				if(results.isEmpty()){			
					results =genoManager.getDatasetDetailsByDatasetName(strDatasetName, Database.LOCAL);
					if(results.size()>0)
						throw new GDMSException("Dataset Name already exists.");
				}else 
					throw new GDMSException("Dataset Name already exists.");
			
			} catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			if(strDatasetName.length()>30){
				ErrMsg = "Dataset Name value exceeds max char size.";
				throw new GDMSException(ErrMsg);
			}
			
			
			String purposeOfStudy=sheetSource.getCell(1,14).getContents().trim();
			/*scoringScheme=sheetSource.getCell(1,15).getContents().trim();
			missingData=sheetSource.getCell(1,16).getContents().trim();	*/			
			boolean dFormat=isValidDate(sheetSource.getCell(1,17).getContents().trim());
			if(dFormat==false){
				ErrMsg = "Creation Date should be in yyyy-mm-dd format";
				throw new GDMSException(ErrMsg);
			}else{
				 uploadTemplateDate = uploadTemplateDate;
			}
			
			 tx=localSession.beginTransaction();
			//remarks=sheetSource.getCell(1,18).getContents().trim();
			dataset=new DatasetBean();
			
			dataset.setDataset_id(iDatasetId);
			dataset.setDataset_name(strDatasetName);
			dataset.setDataset_desc(strDatasetDescription);
			dataset.setDataset_type(dataset_type);
			dataset.setGenus(strGenus);
			dataset.setSpecies(strSpecies);
			dataset.setUpload_template_date(curDate);
			dataset.setRemarks(strRemark);
			dataset.setDatatype("map");
			dataset.setMissing_data(strMissingData);
			//dataset.setMethod(strMethod);
			dataset.setInstitute(strInstitute);
			dataset.setPrincipal_investigator(strPrincipleInvestigator);
			dataset.setEmail(strEmail);
			dataset.setPurpose_of_study(strPurposeOfStudy);
			localSession.save(dataset);
			
			datasetUser = new GenotypeUsersBean();
			datasetUser.setDataset_id(iDatasetId);
			datasetUser.setUser_id(iUserId);
			localSession.save(datasetUser);

			////System.out.println("strMapping_Type:"+strMapping_Type);
			int iPopulationSize = strPopulationSize;
			mappingPop = new MappingPopulationBean();
			mappingPop.setDataset_id(iDatasetId);
			mappingPop.setMapping_type(strMapping_Type);
			mappingPop.setParent_a_nid(parentA_nid);
			mappingPop.setParent_b_nid(parentB_nid);
			mappingPop.setPopulation_size(iPopulationSize);
			mappingPop.setPopulation_type(strPopulationType);
			mappingPop.setMapdata_desc(strMapDataDescription);
			mappingPop.setScoring_scheme(strScoringScheme);
			mappingPop.setMap_id(iMapId);
			localSession.save(mappingPop);
			
			
			//mappingPop = new MappingPop(iDatasetId, strMapping_Type, parentA_nid, parentB_nid, iPopulationSize, strPopulationType, strMapDataDescription, strScoringScheme, iMapId);
			/*ArrayList gidL=new ArrayList();
			SortedMap mapN = new TreeMap();*/
			//System.out.println(",,,,,,,,,,,,,,,,,gNames="+gNames);
			ArrayList finalList =new ArrayList();
			
			
	        for(int a=0;a<listOfGNames.size();a++){
	        	String strGenotype=listOfGNames.get(a).toString();
				Integer strGID =hashMapOfEntryIDandGID.get(strGenotype);
	        	//String 
	        	//int gid1=Integer.parseInt(gidsAList.get(a).toString());
	        	//if(gidL.contains(gid1)){
	        		finalList.add(strGID+"~!~"+mapN.get(strGID));	
	        	//}
	        }
            	      
            ArrayList gids=new ArrayList();
            String gidsList="";
            int rows=0;
            int cols=0;
           
            for (int l=3;l<rowCount;l++){				
				gids.add(sheetDataList.getCell(1,l).getContents().trim());		
				gidsList = gidsList +"'"+ sheetDataList.getCell(1,l).getContents().trim().toString()+"',";
			}
            
            int intMaxAccId=0;
    		Object objAcc=null;
    		Iterator itList=null;
    		List listValues=null;
    		Query queryAcc=localSession.createSQLQuery("select min(acc_metadataset_id) from gdms_acc_metadataset");
    		
    		listValues=queryAcc.list();
    		itList=listValues.iterator();
    					
    		while(itList.hasNext()){
    			objAcc=itList.next();
    			if(objAcc!=null)
    				intMaxAccId=Integer.parseInt(objAcc.toString());
    		}
    		intMaxAccId=intMaxAccId-1;
    		//trackId=intMaxVal-1;
    		System.out.println("finalList:"+finalList);
    		ArrayList<Integer> previousAccGID=new ArrayList<Integer>();
    		int  accSampleId=1;
            
            for(int i=0;i<finalList.size();i++){	
            	String[] strList=finalList.get(i).toString().split("~!~");
            	
            	Integer iGId = Integer.parseInt(strList[0].toString());
            	int nid=Integer.parseInt(strList[1].toString());
            	if(previousAccGID.contains(iGId)){//.equals(previousGID)){
    				//accSampleId=2;
            		accSampleId=accSampleId+1;
    			}else
    				accSampleId=1;
            	
            	AccessionMetaDataBean amdb=new AccessionMetaDataBean();					
				//******************   GermplasmTemp   *********************//*	
            	amdb.setAccMetadatasetId(intMaxAccId);
				amdb.setDatasetId(iDatasetId);
				amdb.setGid(iGId);
				amdb.setNid(nid);
				amdb.setAccSampleId(accSampleId);
				if(!previousAccGID.contains(iGId))
	        		previousAccGID.add(iGId);
				localSession.save(amdb);
				
				if (i % 1 == 0){
					localSession.flush();
					localSession.clear();
				}
				intMaxAccId--;
            }
            
            ArrayList mids=new ArrayList();
            
            HashMap<String, Object> finalHashMapMarkerAndIDs = new HashMap<String, Object>();
            
            
			//if(lstMarIdNames.size()==0){
				for(int f=0; f<markersList.size();f++){
					MarkerInfoBean mib=new MarkerInfoBean();
					if(lstMarkers.contains(markersList.get(f))){
						intRMarkerId=(Integer)(markersMap.get(markersList.get(f)));							
						mids.add(intRMarkerId);
						finalHashMapMarkerAndIDs.put(markersList.get(f).toString().toLowerCase(), intRMarkerId);
					}else{
						//maxMid=maxMid+1;
						maxMid=maxMid-1;
						intRMarkerId=maxMid;
						finalHashMapMarkerAndIDs.put(markersList.get(f).toString().toLowerCase(), intRMarkerId);
						mids.add(intRMarkerId);	
						mib.setMarkerId(intRMarkerId);
						mib.setMarker_type(marker_type);
						mib.setMarker_name(markersList.get(f).toString());
						//mib.setCrop(sheetSource.getCell(1,5).getContents());
						mib.setSpecies(sheetSource.getCell(1,5).getContents());
						
						localSession.save(mib);
						if (f % 1 == 0){
							localSession.flush();
							localSession.clear();
						}
					}
					
					
				}
            
            
            String charData="";    		
			int gi=0;			
			String strData1="";
			
			long lastIdMPId=0;
			try{
				lastIdMPId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_MAPPING_POP_VAUES);
			}catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			int maxMPid=(int)lastIdMPId;
			iMpId=maxMPid;
			ArrayList<Integer> previousGIDA=new ArrayList<Integer>();
			ArrayList<Integer> previousMIDA=new ArrayList<Integer>();
			//listOfMarkers=new ArrayList();
			int markerId=0;
			Integer iGId =0;
			Integer accSampleIdA=1;
			Integer markerSampleIdA=1;
			//ArrayList<Integer> gidsList=new ArrayList<Integer>();	
			for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){			
				HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);	
				String strGenotype="";
				
				String strEntry = hashMapOfDataRow.get(UploadField.Line.toString());
				if(hmOfDuplicateGermNames.containsKey(strEntry))
					strGenotype=hmOfDuplicateGermNames.get(strEntry);
				else
					strGenotype=strEntry;
				
				System.out.println("strGenotype:"+strGenotype);
				Integer strGID =hashMapOfEntryIDandGID.get(strGenotype);
				System.out.println("strGID:"+strGID);
				//Integer strGID = Integer.parseInt(hashMapOfDataRow.get(UploadField.Line.toString()).toString());
				if(previousGIDA.contains(strGID)){//.equals(previousGID)){
					//accSampleIdA=2;
					accSampleIdA=accSampleIdA+1;
				}else
					accSampleIdA=1;
				iMpId--;
				previousMIDA=new ArrayList<Integer>();
				for (int j = 0; j < markersList.size(); j++) {					
					strCharValue=hashMapOfDataRow.get(markersList.get(j));					
					String strMarkerName = markersList.get(j).toString();							
						
					markerId=Integer.parseInt(finalHashMapMarkerAndIDs.get(strMarkerName.toLowerCase()).toString());
					//System.out.println("mName:"+strMarkerFromDArTDataTable+" ID:"+finalHashMapMarkerAndIDs.get(strMarkerFromDArTDataTable.toLowerCase()).toString());
					//if(markerId==previousMID){
					if(previousMIDA.contains(markerId)){
						//markerSampleIdA=2;
						markerSampleIdA=markerSampleIdA+1;
					}else
						markerSampleIdA=1;
					
					mappingPopValues = new MappingPopCharValuesBean();
							
					MapCharArrayCompositeKey Mcack = new MapCharArrayCompositeKey();
					Mcack.setDataset_id(iDatasetId);
					Mcack.setMp_id(iMpId);
					
					mappingPopValues.setMapComKey(Mcack);					
					mappingPopValues.setMap_char_value(strCharValue);
					mappingPopValues.setGid(strGID);					
					mappingPopValues.setMarker_id(markerId);
					mappingPopValues.setAccSampleID(accSampleIdA);
					mappingPopValues.setMarkerSampleId(markerSampleIdA);
					localSession.save(mappingPopValues);
					
					if (j % 1 == 0){
						localSession.flush();
						localSession.clear();
					}
					
					if(!previousMIDA.contains(markerId))
						previousMIDA.add(markerId);
					
					
					iMpId--;	
				}	
				if(!previousGIDA.contains(strGID))
					previousGIDA.add(strGID);
			}
			int intMaxMarMetaId=0;
			Object objMMD=null;
			Iterator itListMMD=null;
			List listValuesMMD=null;
			Query queryMMD=localSession.createSQLQuery("select min(marker_metadataset_id) from gdms_marker_metadataset");
			
			listValuesMMD=queryMMD.list();
			itListMMD=listValuesMMD.iterator();
						
			while(itListMMD.hasNext()){
				objMMD=itListMMD.next();
				if(objMMD!=null)
					intMaxMarMetaId=Integer.parseInt(objMMD.toString());
			}
			
			//System.out.println("listOfMarkers:"+listOfMarkers);
			
			markerSampleIdA=1;
			ArrayList<Integer> previousMIDMMD=new ArrayList<Integer>();
			for(int m1=0;m1<mids.size();m1++){	
				intMaxMarMetaId--;
				int mid=Integer.parseInt(mids.get(m1).toString());
				if(previousMIDMMD.contains(mid)){
					//markerSampleIdA=2;
					markerSampleIdA=markerSampleIdA+1;
				}else
					markerSampleIdA=1;
				
				////System.out.println("gids doesnot Exists    :"+lstgermpName+"   "+gids[l]);
				MarkerMetaDataBean mdb=new MarkerMetaDataBean();					
				//******************   GermplasmTemp   *********************//*	
				mdb.setMarkerMetadatasetId(intMaxMarMetaId);
				mdb.setDatasetId(iDatasetId);
				mdb.setMarkerId(Integer.parseInt(mids.get(m1).toString()));
				mdb.setMarkerSampleId(markerSampleIdA);
				if(!previousMIDMMD.contains(mid))
					previousMIDMMD.add(mid);
				localSession.save(mdb);
				if (m1 % 1 == 0){
					localSession.flush();
					localSession.clear();
				}			
			}	
			
			tx.commit();
			
		}

	
	
	

	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
	}


	@Override
	public String getDataUploaded() {
		String strDataUploaded = "";
		/*if (null != arrayOfMappingPopValues && arrayOfMappingPopValues.length > 0){
			String strUploadInfo = "";
			for (int i = 0; i < arrayOfMappingPopValues.length; i++){
				Integer iMpId = arrayOfMappingPopValues[i].getMpId();
				String strGID = String.valueOf(arrayOfMappingPopValues[i].getGid());
				String strMarkerId = String.valueOf(arrayOfMappingPopValues[i].getMarkerId());
				String strMappingGenotype = "Map: " + iMpId + " GID: " + strGID +
						" Marker-Id: " + strMarkerId;
				strUploadInfo += strMappingGenotype + "\n";
			}
			strDataUploaded = "Uploaded Mapping Genotype(s): \n" + strUploadInfo;
		}*/
		
		strDataUploaded = "Uploaded Mapping Genotype(s)";
		
		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet() {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean isValidDate(String inDate) {

		if (inDate == null)
			return false;

		//set the format to use as a constructor argument
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		if (inDate.trim().length() != dateFormat.toPattern().length())
			return false;

		dateFormat.setLenient(false);

		try {
			//parse the inDate parameter
			dateFormat.parse(inDate.trim());
		}
		catch (ParseException pe) {
			return false;
		}
		return true;
	}
	private static void addValues(String key, Integer value){
		ArrayList<Integer> tempList = null;
		if(hashMap.containsKey(key)){
			tempList=hashMap.get(key);
			if(tempList == null)
				tempList = new ArrayList<Integer>();
			tempList.add(value);
		}else{
			tempList = new ArrayList();
			tempList.add(value);
		}
		hashMap.put(key,tempList);
	}
	

	
}
