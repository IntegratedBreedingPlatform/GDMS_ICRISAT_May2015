package org.icrisat.gdms.upload.genotyping;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.GetGermplasmByNameModes;
import org.generationcp.middleware.manager.ManagerFactory;
//import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
//import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmNameDetails;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.DartDataRow;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.MapOptionsListener;
//import org.icrisat.gdms.ui.RetrieveDataForFlapjack;
import org.icrisat.gdms.ui.common.GDMSModel;
//import org.icrisat.gdms.ui.common.OptionWindowForFlapjackMap;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.marker.UploadField;



//import com.itextpdf.text.log.SysoLogger;
//import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
//import com.vaadin.ui.Window.Notification;



public class DARTGenotype implements UploadMarker {

	private GDMSMain _mainHomePage;
	
	private String strFileLocation;
	private Workbook workbook;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
	private HashMap<Integer, String> hmOfColIndexAndGermplasmName;
	private ArrayList<HashMap<String, String>> listOfDataInGIDsSheet;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	
	private Marker addedMarker;
	private DatasetBean dataset;
	private AccessionMetaDataBean accMetadataSet;
	private MarkerMetadataSet markerMetadataSet;
	private GenotypeUsersBean datasetUser;
	private IntArrayBean alleleValues;
	private DArTDetailsBean dartValues;
	
	
	private Session localSession;
	private Session centralSession;
	
	private Session session;
	int intRMarkerId = 1;
	
		
	private Marker[] arrayOfMarkers;
	List<DartDataRow> listOfDArTDataRows; 
	ManagerFactory factory =null;
	String alertGN="no";
    String alertGID="no";
    String notMatchingData="";
    String notMatchingGIDS="";
    String notMatchingDataDB="";
    String notMatchingGIDSDB="";
    String notMatchingDataExists="";
    
    String strErrMsg="";
    
    GermplasmDataManager germManager;		
	GenotypicDataManager genoManager;
	
	ArrayList<String> listOfGermplasmNames = new ArrayList<String>();
	HashMap<Integer, String> hashMapOfGIDandGName = new HashMap<Integer, String>();
	HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
	//HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
	HashMap<Integer, Integer> hashMapOfGIDsandNIDs = new HashMap<Integer, Integer>();
	List<Marker> listOfMarkersFromDB = null;
	//HashMap<String, Integer> hashMapOfMNamesAndMIDs = new HashMap<String, Integer>();
	
	HashMap<String, Integer> hashMapOfGNameandGIDdb = new HashMap<String, Integer>();
	
	Name names = null;
	ArrayList gidL=new ArrayList();
	ArrayList finalList =new ArrayList();
	List<Integer> nameIdsByGermplasmIds =new ArrayList();
	
	List<Marker> listOfMarkersRows;
	List<MarkerMetadataSet>listOfMetadatasetRows;
	ArrayList<String> listOfMarkers = new ArrayList<String>();
	
	  private Transaction tx;
	static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();  
	
	private HashMap<String, String> hmOfDuplicateGermNames;
	List gNames;
	List gDupNameList;
	List gDupNameListV;
	List gList;
	boolean dupGermConfirmation;
	int g=2;
	String strMNameD="";
	List gEntriesList=new ArrayList<String>();
	ArrayList<Integer> previousAccGID=new ArrayList<Integer>();
	
	HashMap<String, Integer> hashMapOfEntryIDandGID = new HashMap<String, Integer>();
	HashMap<String, String> hashMapOfEntryIDandGName =  new HashMap<String, String>();
	List<String> listOfGNamesFromTable=new ArrayList<String>();
	List<Integer> listOfGIDFromTable;
	ArrayList listOfGNames = new ArrayList<String>();
	ArrayList<String> gNamesList=new ArrayList<String>();
	String strErrorMsg ="no";
	private ArrayList<String> listOfMarkersFromTheSheet = new ArrayList<String>();
	
	@Override
	public void readExcelFile() throws GDMSException {
		try {
			//String var1=strFileLocation.substring(strFileLocation.lastIndexOf("."))-1;
			//////System.out.println("............   "+strFileLocation.substring(strFileLocation.lastIndexOf(".")));
			String ext=strFileLocation.substring(strFileLocation.lastIndexOf("."));
			if(ext.equals(".xls")){
				workbook = Workbook.getWorkbook(new File(strFileLocation));
				strSheetNames = workbook.getSheetNames();
			}else{
				throw new GDMSException("Please check the file, it should be in excel format");				
			}
		} catch (BiffException e) {
			throw new GDMSException("Error Reading DART Genotype Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading DART Genotype Sheet - " + e.getMessage());
		}
	}

	@Override
	public String validateDataInExcelSheet() throws GDMSException {

		if (false == strSheetNames[0].equalsIgnoreCase("dart_source")){
			throw new GDMSException("DArT_Source Sheet Name Not Found");
		}

		if (false == strSheetNames[1].equalsIgnoreCase("dart_data")){
			throw new GDMSException("DArT_DataList Sheet Name Not Found");
		}

		/*if (false == strSheetNames[2].equalsIgnoreCase("dart_gids")){
			throw new GDMSException("DArT_DataList Sheet Name Not Found");
		}*/

		//check the template fields
		for(int i = 0; i < strSheetNames.length; i++){
			String strSName = strSheetNames[i].toString();

			if(strSName.equalsIgnoreCase("DArT_Source")) {
				Sheet sName = workbook.getSheet(strSName);

				String strTempColumnNames[] = {"Institute", "Principle investigator", "Dataset Name", "Dataset description", "Genus",
						"Species", "Remark"};

				for(int j = 0; j < strTempColumnNames.length; j++){
					String strColFromSheet = (String)sName.getCell(0, j).getContents().trim();
					if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException(strTempColumnNames[j] + " Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException(strTempColumnNames[j] + " Column Name Not Found");
					}
				}															
			}

			if(strSName.equalsIgnoreCase("DArT_Data")){
				Sheet dataListSheet = workbook.getSheet(strSName);
				int intNoOfRows = dataListSheet.getRows();
				int intNoOfCols = dataListSheet.getColumns();
				String strTempColumnNames[] = {"CloneID", "MarkerName", "Q", "Reproducibility",
						"Call Rate", "PIC", "Discordance"};					 

				for(int j = 0; j < strTempColumnNames.length; j++){
					String strColFromSheet = (String)dataListSheet.getCell(j, 0).getContents().trim();
					if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException(strTempColumnNames[j] + " Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException("Delete Empty Column " + strColFromSheet);
					}
				}

				for(int c = 0; c < 6; c++){
					for(int r = 1; r < intNoOfRows; r++){
						String value = (String)dataListSheet.getCell(c, r).getContents().trim();
						if(c==1){
							if(!listOfMarkersFromTheSheet.contains(value))
								listOfMarkersFromTheSheet.add(value);
						}
						
						String strColumnName = (String)dataListSheet.getCell(c, 0).getContents().trim();
						if(value == null || value == ""){
							String strRowNumber = String.valueOf(dataListSheet.getCell(c, r).getRow()+1);	
							String strErrMsg = "This cell is empty at position " + strColumnName + strRowNumber+".";
							throw new GDMSException(strErrMsg);
						}
					}
				}

				for(int c = 7; c < intNoOfCols; c++){
					for(int r = 0; r < intNoOfRows; r++){
						String value = (String)dataListSheet.getCell(c, r).getContents().trim();
						if(value == null || value == ""){
							String strRowNumber = String.valueOf(dataListSheet.getCell(c, r).getRow()+1);	
							String strColumnName = (String)dataListSheet.getCell(c, 0).getContents().trim();
							String strErrMsg = "This cell is empty at position " + strColumnName + strRowNumber+".";
							throw new GDMSException(strErrMsg);
						}
					}
				}	
				gList=new ArrayList<String>();
				gEntriesList=new ArrayList<String>();
				gDupNameListV=new ArrayList<String>();
				for(int cIndex = 7; cIndex < intNoOfCols; cIndex++){
					String strName = dataListSheet.getCell(cIndex, 0).getContents().toString();
					String gName="";
					if(strName != null){
						if(gList.contains(strName)){							
							gDupNameListV.add(strName);							
						}
						gList.add(strName);
						if(!gEntriesList.contains(strName))
							gEntriesList.add(strName);
					}
				}
				gNamesList=new ArrayList<String>();
				int c=1;
				hmOfDuplicateGermNames=new HashMap<String, String>();
				gDupNameList=new ArrayList<String>();
				hmOfColIndexAndGermplasmName = new HashMap<Integer, String>();
				for(int colIndex = 7; colIndex < intNoOfCols; colIndex++){
					String strName = dataListSheet.getCell(colIndex, 0).getContents().toString();
					String gName="";
					if(strName != null){
						if(gDupNameListV.contains(strName)){
							gName=strName+" (Sample "+c+")";
							c++;
							gDupNameList.add(gName);
							hmOfDuplicateGermNames.put(gName, strName);
						}else{
							gName=strName;
							hmOfDuplicateGermNames.put(gName, strName);
						}
						hmOfColIndexAndGermplasmName.put(colIndex, gName);
						for(int r = 0; r < intNoOfRows; r++){
							String strCellValue = (String)dataListSheet.getCell(colIndex, r).getContents().trim();
							if(strCellValue == null || strCellValue == ""){
								String strRowNumber = String.valueOf(dataListSheet.getCell(colIndex, r).getRow()+1);	
								String strErrMsg = "Please provide a value at cell position " + "[" + colIndex + ", " + strRowNumber + "] in DArT_GIDs sheet.";
								throw new GDMSException(strErrMsg);
							}
						}
						gNamesList.add(strName);
					}
					
						
				}
			}
		}
		
		if(listOfMarkersFromTheSheet.size()>320 || gNamesList.size()>500){
			strErrorMsg = "Data to be uploaded cannot be displayed in the table. Please click on Upload to upload the data directly";			
		}
		
		return strErrorMsg;
		
	}

	@Override
	public void createObjectsToBeDisplayedOnGUI(GDMSMain theMainHomePage) throws GDMSException {
		_mainHomePage = theMainHomePage;
		
		Sheet sourceSheet = workbook.getSheet(0);
		listOfDataInSourceSheet = new ArrayList<HashMap<String,String>>();

		final HashMap<String, String> hmOfDataInSourceSheet = new HashMap<String, String>();
		
		String strInstitute = sourceSheet.getCell(1, 0).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Institute.toString(), strInstitute);

		String strPrincipalInvestigator = sourceSheet.getCell(1, 1).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PrincipleInvestigator.toString(), strPrincipalInvestigator);

		String strDatasetName = sourceSheet.getCell(1, 2).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.DatasetName.toString(), strDatasetName);

		String strDatasetDescription = sourceSheet.getCell(1, 3).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.DatasetDescription.toString(), strDatasetDescription);

		String strGenus = sourceSheet.getCell(1, 4).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Genus.toString(), strGenus);

		String strSpecies = sourceSheet.getCell(1, 5).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Species.toString(), strSpecies);

		String strRemark = sourceSheet.getCell(1, 6).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Remark.toString(), strRemark);

		//CloneID, MarkerName, Q, Reproducibility, Call Rate, PIC, Discordance followed by Marker-Names	
		final Sheet dataSheet = workbook.getSheet(1);
		final int iNumOfRowsInDataSheet = dataSheet.getRows();
		final int iNumOfColumnsInDataSheet = dataSheet.getColumns();

		listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();
		
		//hmOfDuplicateGermNames=new HashMap<String, String>();
		
		g=1;
		strMNameD="";
		
		for (int rIndex = 1; rIndex < iNumOfRowsInDataSheet; rIndex++){
			HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();			
			gNames=new ArrayList<String>();
		//	gDupNameListV=new ArrayList<String>();
			
			for (int cIndex = 7; cIndex < iNumOfColumnsInDataSheet; cIndex++){
				String strMName = hmOfColIndexAndGermplasmName.get(cIndex);
				String strMValue = dataSheet.getCell(cIndex, rIndex).getContents().toString();
				/*if(gNames.contains(strMName)){				
					gDupNameListV.add(strMName);					
				}*/
				
				hmOfDataInDataSheet.put(strMName, strMValue);
				gNames.add(strMName);
				
			}
			
		}
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

				@Override
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
		//}else{
			listOfDataInSourceSheet.add(hmOfDataInSourceSheet);
			int g=1;
			for (int rIndex = 1; rIndex < iNumOfRowsInDataSheet; rIndex++){

				HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();

				String strCloneId = dataSheet.getCell(0, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(UploadField.CloneID.toString(), strCloneId);

				String strMarkerName = dataSheet.getCell(1, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(UploadField.MarkerName.toString(), strMarkerName);

				String strQ = dataSheet.getCell(2, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(UploadField.Q.toString(), strQ);

				String strReproducibility = dataSheet.getCell(3, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(UploadField.Reproducibility.toString(), strReproducibility);

				String strCallRate = dataSheet.getCell(4, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(UploadField.CallRate.toString(), strCallRate);

				String strPIC = dataSheet.getCell(5, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(UploadField.PIC.toString(), strPIC);

				String strDiscordance = dataSheet.getCell(6, rIndex).getContents().toString();
				hmOfDataInDataSheet.put(UploadField.Discordance.toString(), strDiscordance);
				gNames=new ArrayList<String>();
				//gDupNameList=new ArrayList<String>();
				//Inserting the Marker-Names and Marker-Values
				for (int cIndex = 7; cIndex < iNumOfColumnsInDataSheet; cIndex++){
					String strMName = hmOfColIndexAndGermplasmName.get(cIndex);
					String strMValue = dataSheet.getCell(cIndex, rIndex).getContents().toString();
					hmOfDataInDataSheet.put(strMName, strMValue);
					
					/*if(gNames.contains(strMName)){
						strMNameD=strMName+" (Sample "+g+")";
						gDupNameList.add(strMNameD);
						//hmOfDuplicateGermNames.put(strMNameD, strMName);
					}else
						strMNameD=strMName;
					hmOfDataInDataSheet.put(strMNameD, strMValue);
					
					gNames.add(hmOfDuplicateGermNames.get(strMName));
					if(gDupNameList.size()>0)
						g++;*/
				}
				//g=1;
				listOfDataInDataSheet.add(hmOfDataInDataSheet);
			}
			/*System.out.println("gNames:"+gNames);
			System.out.println("gDupNameList:"+gDupNameList);
			System.out.println("listOfDataInDataSheet:"+listOfDataInDataSheet);*/
		}

	@Override
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}

	@Override
	public void setDataToBeUploded(ArrayList<HashMap<String, String>> theListOfSourceDataRows, ArrayList<HashMap<String, String>> listOfDataRows, ArrayList<HashMap<String, String>> listOfGIDRows) {
		listOfDataRowsFromSourceTable = theListOfSourceDataRows;
		listOfDataRowsFromDataTable = listOfDataRows;
		//listOfGIDRowsFromGIDTableForDArT = listOfGIDRows;
	}

	@Override
	public void upload() throws GDMSException {
		validateData();
		createObjectsToBeSavedToDB();
	}

	@Override
	public void validateData() throws GDMSException {

		String strReqColumnNamesInSource[] = {UploadField.Institute.toString(), 
				UploadField.DatasetName.toString(), UploadField.DatasetDescription.toString(), UploadField.Genus.toString()
		};

		HashMap<String, String> hmOfSourceColumnsAndValuesFromGUI = listOfDataRowsFromSourceTable.get(0);
		String strDatasetName = "";
		for(int j = 0; j < strReqColumnNamesInSource.length; j++){
			String strCol = strReqColumnNamesInSource[j];
			if (false == hmOfSourceColumnsAndValuesFromGUI.containsKey(strCol)){
				throw new GDMSException(strCol + " column not found in data DArT_Source table.");
			} else {
				//Institute, Principle-Investigator, Dataset-Name, Dataset-Description, Genus, Species, Remark
				if (strCol.equalsIgnoreCase(UploadField.Institute.toString()) || 
						strCol.equalsIgnoreCase(UploadField.DatasetName.toString()) ||
						strCol.equalsIgnoreCase(UploadField.DatasetDescription.toString()) || 
						strCol.equalsIgnoreCase(UploadField.Genus.toString())){
					String strValue = hmOfSourceColumnsAndValuesFromGUI.get(strCol);
					if (null == strValue || strValue.equals("")){
						throw new GDMSException("Please provide a value for " +  strCol + " column not found in data DArT_Source sheet.");
					}
					
					if (strCol.equalsIgnoreCase(UploadField.DatasetName.toString())){
						strDatasetName = strValue;
					}
				}
			}
		}															

		
		/**
		 * 20130826: Fix for Issue No: 60 - DArT Genotype Upload
		 * 
		 * Check for duplicate Dataset Name before uploading the DArT Genotype
		 * 
		 */
		//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		
		factory=GDMSModel.getGDMSModel().getManagerFactory();
		//////System.out.println("%%%%%%%%%%%%%%%%%%%%%%%  :"+GDMSModel.getGDMSModel().getLocalParams().);
		germManager = factory.getGermplasmDataManager();		
		genoManager=factory.getGenotypicDataManager();
		
	
			if(strDatasetName.trim().length() > 30){
				throw new GDMSException("Dataset Name value exceeds max char size.");
			}

			
			
	
		//20130826: End of fix for Issue No: 60 - DArT Genotype Upload

		String strReqColumnNamesInDataSheet[] = {UploadField.CloneID.toString(), UploadField.MarkerName.toString(),  UploadField.Q.toString(), 
				UploadField.Reproducibility.toString(), UploadField.CallRate.toString(), UploadField.PIC.toString(), UploadField.Discordance.toString()};					 

		for(int colIndex = 0; colIndex < strReqColumnNamesInDataSheet.length; colIndex++) {
			String strColName = strReqColumnNamesInDataSheet[colIndex];
			for (int rowIndex = 0; rowIndex < listOfDataRowsFromDataTable.size(); rowIndex++) {
				HashMap<String, String> hmOfDataColumnsAndValuesFromGUI = listOfDataRowsFromDataTable.get(rowIndex);
				//System.out.println("%%%%%%%%%%%%%%>>>>>>>>>>>:"+hmOfDataColumnsAndValuesFromGUI);
				if(false == hmOfDataColumnsAndValuesFromGUI.containsKey(strColName)){
					throw new GDMSException(strReqColumnNamesInSource[colIndex] + " Column Name Not Found");
				} else {
					String strValue = hmOfDataColumnsAndValuesFromGUI.get(strColName);
					if (null == strValue || strValue.equals("")){
						throw new GDMSException("Please provide a value for " +  strColName + " in data DArT_Data sheet at Row#: " + (rowIndex+1));
					}
				}
			}	
		}
		
		/*String strReqColumnNamesInGIDsSheet[] = {UploadField.GIDs.toString(), UploadField.GermplasmName.toString()};
		HashMap<String, String> hmOfGIDsAndGNamesFromGIDSheet = listOfGIDRowsFromGIDTableForDArT.get(0);
		for(int j = 0; j < strReqColumnNamesInGIDsSheet.length; j++){
			String strCol = strReqColumnNamesInGIDsSheet[j];
			if (false == hmOfGIDsAndGNamesFromGIDSheet.containsKey(strCol)){
				throw new GDMSException(strCol + " column not found in data DArT_GIDs table.");
			} 
		}	*/
	}


	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
		
			germManager = factory.getGermplasmDataManager();
			genoManager=factory.getGenotypicDataManager();
	
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
		 tx=localSession.beginTransaction();
		/** reading from Data sheet of template **/
		int iRowCountInDataTable = listOfDataRowsFromDataTable.size();
		int iGermplasmNamesCount = 0;

		
		String strGermplasmSelected = GDMSModel.getGDMSModel().getGermplasmSelected();
		//System.out.println("..>>>>>>>>....:"+strGermplasmSelected);
		
		String strQuerryGL="select listid from listnms where listname='"+strGermplasmSelected+"'";		
		int list_id=0;		
		List newListL=new ArrayList();		
		List listData=new ArrayList();		
		List newListC=new ArrayList();
		//try {	
		Object obj=null;
		Object objL=null;
		Iterator itListC=null;
		Iterator itListL=null;
		
		
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
		hashMapOfGNameandGID=new HashMap<String, Integer>();
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
		//System.out.println("listOfGNames:"+gNames);
		for(int w=0;w<listData.size();w++){
        	Object[] strMareO= (Object[])listData.get(w);
           // System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]+"   "+strMareO[2]);
        	if(gEntriesList.contains(strGermplasmSelected+"-" + strMareO[0].toString())){
        	 	listEntries.add(strGermplasmSelected+"-" + strMareO[0].toString());
	            listOfGNamesFromTable.add(strMareO[1].toString().trim());
	            listOfGIDFromTable.add(Integer.parseInt(strMareO[2].toString()));
	            hashMapOfGNameandGID.put(strMareO[1].toString().trim(), Integer.parseInt(strMareO[2].toString()));
	            hashMapOfEntryIDandGID.put(strGermplasmSelected+"-" + strMareO[0].toString(), Integer.parseInt(strMareO[2].toString()));
	            hashMapOfEntryIDandGName.put(strGermplasmSelected+"-" + strMareO[0].toString(), strMareO[1].toString());
        	}
 		}
		//System.out.println("listOfGNames:"+gNames);
		//System.out.println("listEntries:"+listEntries);
		//if(listOfDNANamesFromSourceTable.size()!=listEntries.size()){
			for(int k=0;k<gNames.size();k++){
				if(!(listEntries.contains(gNames.get(k)))){
					 nonExistingListItem=nonExistingListItem+gNames.get(k)+"\n";
					 nonExistingListItems="yes";
				}
			}
			if(nonExistingListItems.equalsIgnoreCase("yes")){
				throw new GDMSException("Please verify the List Entries provided doesnot exist in the database\t "+nonExistingListItem );
			}
		
		
		
		
		
		//Checking for the number of Germplasm names provided in the "DArT_Data" sheet
		HashMap<String, String> hashMapOfFirstDataRow = listOfDataRowsFromDataTable.get(0);
		int  iColCountInDataTable = hashMapOfFirstDataRow.size();
		for(int col = 8; col < iColCountInDataTable; col++){
			iGermplasmNamesCount = iGermplasmNamesCount + 1;				
		}

		
		//System.out.println("..................:"+listOfGIDRowsFromGIDTableForDArT);
		//Building the list of GIDs from the DArT_GIDs
		//And building a HashMap of GIDs and GNames from the GID sheet
		Map<Integer, String> hashMapOfGIDAndGNameFromGIDTable = new HashMap<Integer, String>();
		Map<String, Integer> hashMapOfGNameAndGIDFromGIDTable = new HashMap<String, Integer>();
		ArrayList<Integer> listofGIDsFromGIDsTable = new ArrayList<Integer>();
		ArrayList<Integer> listofGIDs = new ArrayList<Integer>();
		ArrayList<String> listofGNamessFromGNamesTable = new ArrayList<String>();
		//if(iGIDsCount == iGermplasmNamesCount){
			/*for (int r = 0; r < iRowsInGIDsTable; r++){
				HashMap<String, String> hashMapOfGIDDataRow = listOfGIDRowsFromGIDTableForDArT.get(r);
				String strGID = hashMapOfGIDDataRow.get(UploadField.GIDs.toString()).toString();
				int iGID = Integer.parseInt(strGID);
				if (false == listofGIDsFromGIDsTable.contains(iGID)){
					listofGIDsFromGIDsTable.add(iGID);
				}
				
				listofGIDs.add(iGID);
				
				String strGermplasmName = hashMapOfGIDDataRow.get(UploadField.GermplasmName.toString()).trim();
				if (false == listofGNamessFromGNamesTable.contains(strGermplasmName)){
					listofGNamessFromGNamesTable.add(strGermplasmName);
					hashMapOfGIDAndGNameFromGIDTable.put(iGID, strGermplasmName);
					
					hashMapOfGNameAndGIDFromGIDTable.put(strGermplasmName, iGID);
				}
				
			}*/
		//}
		//System.out.println("listofGIDs:"+listofGIDs);
		List<Integer> listOfGermplasmIDsFromDB = new ArrayList<Integer>();
		List<Integer> listOfNIDsByGermplasmIds = null;
		ArrayList<String> listOfGermplasmNamesFromDB = null;
		HashMap<Integer, String> hashMapOfGIDandGNameFromDB = null;
		HashMap<Integer, Integer> hashMapOfGIDsandNIDsFromDB = null;
		HashMap<String, Integer> hashMapOfGNamesandGIDsFromDB = null;
		
		ArrayList<String> listOfGermplasmNames = new ArrayList<String>();
		HashMap<Integer, String> hashMapOfGIDandGName = new HashMap<Integer, String>();
		//HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
		HashMap<Integer, Integer> hashMapOfGIDsandNIDs = new HashMap<Integer, Integer>();
		
	//	HashMap<String, Integer> hashMapOfMNamesAndMIDs = new HashMap<String, Integer>();
		
		HashMap<String, Integer> hashMapOfGNameandGIDdb = new HashMap<String, Integer>();
		gidL=new ArrayList();
		
		nameIdsByGermplasmIds =new ArrayList();
		
		//////System.out.println("listofGIDsFromGIDsTable=:"+listofGIDsFromGIDsTable);
		ArrayList gidsDBList = new ArrayList();
		ArrayList gNamesDBList = new ArrayList();
		String markersForQuery="";
		try {			
			hashMap.clear();
			List<GermplasmNameDetails> germplasmList = germManager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.NORMAL);				
			for (GermplasmNameDetails g : germplasmList) {				
	        	if(!(gidsDBList.contains(g.getNameId()))){
	        		gidsDBList.add(g.getNameId());
	        		gNamesDBList.add(g.getNVal());
	        		addValues(g.getNVal(), g.getGermplasmId());					        		
	        	}	
	        	if(!gidL.contains(g.getGermplasmId()))
	            	gidL.add(g.getGermplasmId());
				listOfGermplasmNames.add(g.getNVal());
				hashMapOfGIDandGName.put(g.getGermplasmId(), g.getNVal());
				hashMapOfGIDsandNIDs.put(g.getGermplasmId(), g.getNameId());
				hashMapOfGNameandGIDdb.put(g.getNVal(), g.getGermplasmId());
				nameIdsByGermplasmIds.add( g.getNameId());
	        	
	        }
			
			if(gNamesDBList.size()!=listofGNamessFromGNamesTable.size()){
				germplasmList = germManager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.STANDARDIZED);
				for (GermplasmNameDetails g : germplasmList) {
					if(!(gidsDBList.contains(g.getNameId()))){
		        		gidsDBList.add(g.getNameId());
		        		gNamesDBList.add(g.getNVal());
		        		addValues(g.getNVal(), g.getGermplasmId());					        		
		        	}   
					if(!gidL.contains(g.getGermplasmId()))
		            	gidL.add(g.getGermplasmId());
					listOfGermplasmNames.add(g.getNVal());
					hashMapOfGIDandGName.put(g.getGermplasmId(), g.getNVal());
					hashMapOfGIDsandNIDs.put(g.getGermplasmId(), g.getNameId());
					hashMapOfGNameandGIDdb.put(g.getNVal(), g.getGermplasmId());
					nameIdsByGermplasmIds.add( g.getNameId());
		          
		        }
			}			
			
			if(gNamesDBList.size()!=listofGNamessFromGNamesTable.size()){
				germplasmList = germManager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.SPACES_REMOVED_BOTH_SIDES);
				for (GermplasmNameDetails g : germplasmList) {
					if(!(gidsDBList.contains(g.getNameId()))){
		        		gidsDBList.add(g.getNameId());
		        		gNamesDBList.add(g.getNVal());
		        		addValues(g.getNVal(), g.getGermplasmId());					        		
		        	}   
					if(!gidL.contains(g.getGermplasmId()))
		            	gidL.add(g.getGermplasmId());
					listOfGermplasmNames.add(g.getNVal());
					hashMapOfGIDandGName.put(g.getGermplasmId(), g.getNVal());
					hashMapOfGIDsandNIDs.put(g.getGermplasmId(), g.getNameId());
					hashMapOfGNameandGIDdb.put(g.getNVal(), g.getGermplasmId());
					nameIdsByGermplasmIds.add( g.getNameId());
		          
		        }
			}	
			
		} catch (MiddlewareQueryException e1) {
			throw new GDMSException(e1.getMessage());
		}
			//System.out.println("gidsDBList:"+gidsDBList);
			//System.out.println("gNamesDBList:"+gNamesDBList);

		/*if (0 == gidsDBList.size()) {
			strErrMsg = "The following GID(s) provided do not exist in the database. \n Please upload the relevant germplasm list \n \t" + " \n Please verify the name(s) provided with the following GID(s) which do not match the name(s) present in the database: \n\t ";
			throw new GDMSException(strErrMsg);
		}*/
		int size=0;
		if(gidsDBList.size() > 0){
			for(int n=0;n<listofGNamessFromGNamesTable.size();n++){
     		   if(!gNamesDBList.contains(listofGNamessFromGNamesTable.get(n))){
     			   /*if(!(hashMap.get(listofGNamessFromGNamesTable.get(n).toString()).contains(hashMapOfGNameandGID.get(listofGNamessFromGNamesTable.get(n).toString())))){
     				   notMatchingData=notMatchingData+listofGNamessFromGNamesTable.get(n)+"   "+hashMapOfGNameandGID.get(listofGNamessFromGNamesTable.get(n).toString())+"\n\t";
     				   
     				   notMatchingDataDB=notMatchingDataDB+listofGNamessFromGNamesTable.get(n)+"="+hashMap.get(listofGNamessFromGNamesTable.get(n))+"\t";
		        		   alertGN="yes";
     			   }
     		   }else{*/
     			   //int gid=GIDsMap.get(NamesList.get(n).toString());
     			   alertGID="yes";
     			   size=hashMap.size();
     			   notMatchingGIDS=notMatchingGIDS+listofGNamessFromGNamesTable.get(n).toString()+", ";
     		   }
     	   }	
		}

		if((alertGN.equals("yes"))&&(alertGID.equals("no"))){
     	   //String ErrMsg = "GID(s) ["+notMatchingGIDS.substring(0,notMatchingGIDS.length()-1)+"] of Germplasm(s) ["+notMatchingData.substring(0,notMatchingData.length()-1)+"] being assigned to ["+notMatchingDataExists.substring(0,notMatchingDataExists.length()-1)+"] \n Please verify the template ";
			strErrMsg = "Please verify the name(s) provided \t "+notMatchingData+" which do not match the GID(s) present in the database"+notMatchingDataDB;
			throw new GDMSException(strErrMsg);
        }
        if((alertGID.equals("yes"))&&(alertGN.equals("no"))){	        	   
     	   if(size==0){
     		  strErrMsg = "The Germplasm(s) provided do not exist in the database. \n Please import the relevant germplasm list ";
     	   }else{
     		  strErrMsg = "The following Germplasm(s) provided do not exist in the database. \n Please import the relevant germplasm list \n \t"+notMatchingGIDS;
     		   //ErrMsg = "Please verify the GID/Germplasm(s) provided as some of them do not exist in the database. \n Please upload germplasm information into GMS ";
     	   }	        	   
     	   //ErrMsg = "Please verify the following GID/Germplasm(s) doesnot exists. \n Upload germplasm Information into GMS \n\t"+notMatchingGIDS;
     	  throw new GDMSException(strErrMsg);
        }
		
        if((alertGID.equals("yes"))&&(alertGN.equals("yes"))){
        	strErrMsg = "The following Germplasm(s) provided do not exist in the database. \n Please import the relevant germplasm list \n \t"+notMatchingGIDS+" \n Please verify the name(s) provided "+notMatchingData+" which do not match the GIDS(s) present in the database "+notMatchingDataDB;
     	  throw new GDMSException(strErrMsg); 
        }		
		/** Obtaining the list of Markers from the sheet */	
		List<String> listOfMarkerNamesFromTheDataSheet = new ArrayList<String>();
		for (int iRowCount = 0; iRowCount < iRowCountInDataTable; iRowCount++){
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(iRowCount);
			String strMarkerName = hashMapOfDataRow.get(UploadField.MarkerName.toString());
			if(!listOfMarkerNamesFromTheDataSheet.contains(strMarkerName))
				listOfMarkerNamesFromTheDataSheet.add(strMarkerName);
			
			//markersForQuery=markersForQuery+"'"+markerList.get(ml)+"',";
			
		}
		for(int ml=0;ml<listOfMarkerNamesFromTheDataSheet.size();ml++){
     	   markersForQuery=markersForQuery+"'"+listOfMarkerNamesFromTheDataSheet.get(ml)+"',";
        }
        markersForQuery=markersForQuery.substring(0, markersForQuery.length()-1);
        
        newListL=new ArrayList();
		newListC=new ArrayList();
		//try {	
		obj=null;
		objL=null;
		itListC=null;
		itListL=null;
		//genoManager.getMar
		
		List lstMarkers = new ArrayList();
		HashMap<String, Object> markersMap = new HashMap<String, Object>();	
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
	        	//////System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
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
				//////System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
				if(!lstMarkers.contains(strMareO[1].toString())){
            		lstMarkers.add(strMareO[1].toString());	            		
            		markersMap.put(strMareO[1].toString(), strMareO[0]);	
				}
			}
		}
       
        
        

		/** Retrieving the Marker-IDs for the Markers given in the DataSheet-DArT_Data */
		/*MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
		long countAll = 0;
		List<Marker> listOfMarkersFromDB = null;
		try {
			countAll = markerDAO.countAll();
			List<Integer> listOfMarkerIdsByMarkerNames =genoManager.getMarkerIdsByMarkerNames(listOfMarkerNamesFromTheDataSheet, 0, listOfMarkerNamesFromTheDataSheet.size(), Database.CENTRAL);
			
			
			List<Integer> listOfMarkerIdsByMarkerNames = genotypicDataManagerImpl.getMarkerIdsByMarkerNames(listOfMarkerNamesFromTheDataSheet, 0, (int)countAll, Database.LOCAL);
			if (null != listOfMarkerIdsByMarkerNames){
				listOfMarkersFromDB = genotypicDataManagerImpl.getMarkersByMarkerIds(listOfMarkerIdsByMarkerNames, 0, listOfMarkerIdsByMarkerNames.size());
			}
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}*/


		//Creating the DatasetUsers object first
		Integer iUserId = 0;
		HashMap<String, String> hashMapOfDataRowFromSourceTable = listOfDataRowsFromSourceTable.get(0);
		String strPrincipleInvestigator = hashMapOfDataRowFromSourceTable.get(UploadField.PrincipleInvestigator.toString());

			try {
				
				iUserId=GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId();
				
			} catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			List<Marker> listOfMarkersFromDB = null;
			finalList =new ArrayList();
			
			
		
		for(int a=0;a<listofGIDs.size();a++){
        	int gid1=Integer.parseInt(listofGIDs.get(a).toString());
        	
        	finalList.add(gid1+"~!~"+hashMapOfGIDsandNIDs.get(gid1));	
        	
        }
		
		
		int iUploadedMarkerCount = 0;
		arrayOfMarkers = new Marker[iGermplasmNamesCount];
		Integer iMarkerId = 0;
		//////System.out.println("hashMapOfGNameandGIDdb=:"+hashMapOfGNameandGIDdb);
		//Dataset Fields
		String strDatasetName = hashMapOfDataRowFromSourceTable.get(UploadField.DatasetName.toString());
		String strDatasetDesc = hashMapOfDataRowFromSourceTable.get(UploadField.DatasetDescription.toString());
		String strDatasetType = "DArT";
		String strGenus = hashMapOfDataRowFromSourceTable.get(UploadField.Genus.toString());
		String strSpecies = hashMapOfDataRowFromSourceTable.get(UploadField.Species.toString());
		Date uploadTemplateDate = new Date(System.currentTimeMillis()); 
		String strRemarks = ""; 
		String strDataType = "int"; 
		String strMissingData = null;
		String strMethod = null;
		String strScore = null;
		String strInstitute = hashMapOfDataRowFromSourceTable.get(UploadField.Institute.toString());
		String strEmail = null;
		String strPurposeOfStudy = null;
		// DatasetUser Fields
		Integer iDatasetId = 0; //Will be set/overridden by the function
		long datasetLastId = 0;
		int maxad_Id=0;
		 int maxMid=0;
		 long lastId = 0;
		 long lastAd_Id=0;
		 
		 int iAnId=0;
		 
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
		 
		 /** retrieving maximum marker id from 'marker' table of database **/
			try{
				lastId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_MARKER);
			}catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			maxMid=(int)lastId; 
		 
		 
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
				//ErrMsg = "Dataset Name value exceeds max char size.";
				throw new GDMSException("Dataset Name value exceeds max char size.");
			}
		 try{
			 datasetLastId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_DATASET);
			}catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			int intDatasetId=(int)datasetLastId;
			
			iDatasetId=intDatasetId-1;
			try{
				lastAd_Id = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_DART_VALUES);
			}catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			maxad_Id=(int)lastAd_Id; 
			
			long lastIdMPId=0;
			try{
				lastIdMPId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_ALLELE_VALUES);
			}catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			int maxCHid=(int)lastIdMPId;
			iAnId=maxCHid-1;
			
			
		//dataset = new Dataset(iDatasetId, strDatasetName, strDatasetDesc, strDatasetType, strGenus, strSpecies, uploadTemplateDate, strRemarks, strDataType, strMissingData, strMethod, strScore, strInstitute, strPrincipleInvestigator, strEmail, strPurposeOfStudy);
		/*dataset = new Dataset();
		//dataset.setDatasetId(iDatasetId);
		dataset.setDatasetName(strDatasetName);
		dataset.setDatasetDesc(strDatasetDesc);
		dataset.setDatasetType(strDatasetType);
		dataset.setGenus(strGenus);
		dataset.setSpecies(strSpecies);
		dataset.setUploadTemplateDate(uploadTemplateDate);
		dataset.setRemarks(strRemarks);
		dataset.setDataType(strDataType);
		dataset.setMissingData(strMissingData);
		dataset.setMethod(strMethod);
		dataset.setScore(strScore);*/
		
		dataset = new DatasetBean();
		dataset.setDataset_id(iDatasetId);
		dataset.setDataset_name(strDatasetName);
		dataset.setDataset_desc(strDatasetDesc);
		dataset.setDataset_type(strDatasetType);
		dataset.setGenus(strGenus);
		dataset.setSpecies(strSpecies);
		dataset.setUpload_template_date(curDate);
		dataset.setRemarks(strRemarks);
		dataset.setDatatype(strDataType);
		dataset.setMissing_data(strMissingData);
		dataset.setMethod(strMethod);
		dataset.setScore(strScore);
		localSession.save(dataset);
					
		//datasetUser = new DatasetUsers(iDatasetId, iUserId);
		
		datasetUser = new GenotypeUsersBean();
		datasetUser.setDataset_id(iDatasetId);
		datasetUser.setUser_id(iUserId);
		localSession.save(datasetUser);
		
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
		//System.out.println("finalList:"+finalList);
		ArrayList<Integer> previousAccGID=new ArrayList<Integer>();
		
		 ArrayList mids=new ArrayList();
         
         HashMap<String, Object> finalHashMapMarkerAndIDs = new HashMap<String, Object>();
         maxad_Id=maxad_Id-1;
				
		for (int i = 0; i< iRowCountInDataTable; i++){
			dartValues=new DArTDetailsBean();
			
			HashMap<String, String> hashMapOfDataRowFromDataTable = listOfDataRowsFromDataTable.get(i);
			
			String strCloneID = hashMapOfDataRowFromDataTable.get(UploadField.CloneID.toString());
			Integer iCloneId = Integer.parseInt(strCloneID);

			String strQValue = hashMapOfDataRowFromDataTable.get(UploadField.Q.toString());
			Float fQValue = Float.parseFloat(strQValue);

			String strRerpoducibility = hashMapOfDataRowFromDataTable.get(UploadField.Reproducibility.toString());
			Float fReproducibility = Float.parseFloat(strRerpoducibility);

			String strReproducibility = hashMapOfDataRowFromDataTable.get(UploadField.CallRate.toString());
			Float fCallRate = Float.parseFloat(strReproducibility);

			String strPIC = hashMapOfDataRowFromDataTable.get(UploadField.PIC.toString());
			Float fPicValue = Float.parseFloat(strPIC); 

			String strDiscordance = hashMapOfDataRowFromDataTable.get(UploadField.Discordance.toString());
			Float fDiscordance = Float.parseFloat(strDiscordance); 
			
			String strMarkerFromDArTDataTable = hashMapOfDataRowFromDataTable.get(UploadField.MarkerName.toString()).trim();
			MarkerInfoBean mib=new MarkerInfoBean();
			if(lstMarkers.contains(strMarkerFromDArTDataTable)){
				intRMarkerId=(Integer)(markersMap.get(strMarkerFromDArTDataTable));							
				mids.add(intRMarkerId);
				finalHashMapMarkerAndIDs.put(strMarkerFromDArTDataTable.toLowerCase(), intRMarkerId);
			}else{
				//maxMid=maxMid+1;
				maxMid=maxMid-1;
				intRMarkerId=maxMid;
				finalHashMapMarkerAndIDs.put(strMarkerFromDArTDataTable.toLowerCase(), intRMarkerId);
				mids.add(intRMarkerId);	
				mib.setMarkerId(intRMarkerId);
				mib.setMarker_type("DArT");
				mib.setMarker_name(strMarkerFromDArTDataTable);
				//mib.setCrop(sheetSource.getCell(1,5).getContents());
				mib.setSpecies(strSpecies);
				
				localSession.save(mib);
				
			}
			//int iNumOfColumnsRow = hashMapOfDataRowFromDataTable.size();
			//////System.out.println(markersMap.get(strMarkerFromDArTDataTable.toString())+"    "+strMarkerFromDArTDataTable.toString());
			
			dartValues.setAd_id(maxad_Id);
			dartValues.setDataset_id(iDatasetId);
			dartValues.setMarker_id(intRMarkerId);
			dartValues.setClone_id(iCloneId);
			dartValues.setQvalue(fQValue);
			dartValues.setReproducibility(fReproducibility);
			dartValues.setCall_rate(fCallRate);
			dartValues.setPic_value(fPicValue);
			dartValues.setDiscordance(fDiscordance);
			localSession.save(dartValues);
			if (i % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
			//maxad_Id++;
			maxad_Id=maxad_Id-1;
		}
		ArrayList<Integer> previousGIDA=new ArrayList<Integer>();
		ArrayList<Integer> previousMIDA=new ArrayList<Integer>();
		listOfMarkers=new ArrayList();
		int markerId=0;
		Integer iGId =0;
		Integer accSampleIdA=1;
		Integer markerSampleIdA=1;
		ArrayList<Integer> gidsList=new ArrayList<Integer>();
		//System.out.println("hmOfColIndexAndGermplasmName:"+hmOfColIndexAndGermplasmName);
		for (int row = 0; row < iRowCountInDataTable; row++){
			
			HashMap<String, String> hashMapOfDataRowFromDataTable = listOfDataRowsFromDataTable.get(row);
			
			//System.out.println(row+"="+hashMapOfDataRowFromDataTable);
			
			String strMarkerFromDArTDataTable = hashMapOfDataRowFromDataTable.get(UploadField.MarkerName.toString()).trim();
			
		
			listOfMarkers.add(strMarkerFromDArTDataTable.toLowerCase());
		
			markerId=Integer.parseInt(finalHashMapMarkerAndIDs.get(strMarkerFromDArTDataTable.toLowerCase()).toString());
			//System.out.println("mName:"+strMarkerFromDArTDataTable+" ID:"+finalHashMapMarkerAndIDs.get(strMarkerFromDArTDataTable.toLowerCase()).toString());
			//if(markerId==previousMID){
			if(previousMIDA.contains(markerId)){
				markerSampleIdA=markerSampleIdA+1;
			}else
				markerSampleIdA=1;
			previousGIDA.clear();
			
			int iNumOfColumnsRow = hashMapOfDataRowFromDataTable.size();			
			accSampleIdA=1;
			//System.out.println("hashMapOfGNameandGIDdb:"+hashMapOfGNameandGIDdb);
			for (int g = 7; g < iNumOfColumnsRow; g++) {
				if(hmOfColIndexAndGermplasmName.get(g) != null){
					String strGermplasmName="";
					String strGermName = hmOfColIndexAndGermplasmName.get(g).trim();
					//System.out.println("strGermName***********  :"+strGermName);
					//if(hmOfDuplicateGermNames.containsKey(strGermName))
					if(gDupNameList.contains(strGermName))
						strGermplasmName=hmOfDuplicateGermNames.get(strGermName);
					else
						strGermplasmName=strGermName;
					//System.out.println("strGermplasmName:"+strGermplasmName);
					
					iGId = hashMapOfEntryIDandGID.get(strGermplasmName);
					if(row == 0){
						gidsList.add(iGId);
					}
					if(previousGIDA.contains(iGId)){//.equals(previousGID)){
						accSampleIdA=accSampleIdA+1;
					}else
						accSampleIdA=1;
					
					//////System.out.println("iGid=:"+iGId);
					alleleValues=new IntArrayBean();
					IntArrayCompositeKey cack = new IntArrayCompositeKey();
					cack.setDataset_id(iDatasetId);					
					cack.setAn_id(iAnId);
					alleleValues.setComKey(cack);
					
					String iAlleleBinValue = hashMapOfDataRowFromDataTable.get(strGermName);
				
					String iAlleleRawValue = "";
					
					alleleValues.setGid(iGId);					
					alleleValues.setMarker_id(markerId);
					//chb.setAllele_raw_value((String)sheetData.getCell(j,i).getContents().trim());
					alleleValues.setAllele_bin_value(iAlleleBinValue);
					alleleValues.setAccSampleID(accSampleIdA);
					alleleValues.setMarkerSampleId(markerSampleIdA);
					
					
					//System.out.println("Gid:"+iGId+",MarkerID=:"+markerId+",iAlleleBinValue:"+iAlleleBinValue+",accSampleIdA:"+accSampleIdA);
					
					
					if(!previousGIDA.contains(iGId))
						previousGIDA.add(iGId);
										
					
					
					localSession.save(alleleValues);
					if (g % 1 == 0){
						localSession.flush();
						localSession.clear();
					}

				

				}
				if(!previousMIDA.contains(markerId))
					previousMIDA.add(markerId);
				iAnId=iAnId-1;
			}
			
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
		//listOfMetadatasetRows=new ArrayList<MarkerMetadataSet>();	
		for(int m1=0; m1<listOfMarkers.size();m1++){
			intMaxMarMetaId--;
			int mid=Integer.parseInt(finalHashMapMarkerAndIDs.get(listOfMarkers.get(m1)).toString());
			if(previousMIDMMD.contains(mid)){
				markerSampleIdA=markerSampleIdA+1;
			}else
				markerSampleIdA=1;
		//for(int m1=0;m1<mids.size();m1++){					
			//////System.out.println("gids doesnot Exists    :"+lstgermpName+"   "+gids[l]);
			MarkerMetaDataBean mdb=new MarkerMetaDataBean();					
			//******************   GermplasmTemp   *********************//*	
			mdb.setMarkerMetadatasetId(intMaxMarMetaId);
			mdb.setDatasetId(iDatasetId);
			mdb.setMarkerId(mid);
			mdb.setMarkerSampleId(markerSampleIdA);
			//previousMIDMMD.add(mid);
			if(!previousMIDMMD.contains(mid))
				previousMIDMMD.add(mid);
			
			localSession.save(mdb);
			if (m1 % 1 == 0){
				localSession.flush();
                localSession.clear();
			}			
		}
		
		int  accSampleId=1;
		for(int a=0;a<gidsList.size();a++){	
			
        	//String[] strList=gidsList.get(a).toString().split("~!~");
        	iGId = gidsList.get(a);
        	int nid=hashMapOfGIDsandNIDs.get(iGId);
        	if(previousAccGID.contains(iGId)){//.equals(previousGID)){
				accSampleId=accSampleId+1;
			}else
				accSampleId=1;
        	
        	accMetadataSet=new AccessionMetaDataBean();					
			//******************   GermplasmTemp   *********************//*	
        	
        	accMetadataSet.setAccMetadatasetId(intMaxAccId);
        	accMetadataSet.setDatasetId(iDatasetId);
        	accMetadataSet.setGid(iGId);
        	accMetadataSet.setNid(nid);        	
        	accMetadataSet.setAccSampleId(accSampleId);
        	if(!previousAccGID.contains(iGId))
        		previousAccGID.add(iGId);
        	localSession.save(accMetadataSet);
			
			if (a % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
			intMaxAccId--;
        }
		
		
		
		tx.commit();
	}

	
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDataUploaded() {
		String strDataUploaded = "Uploaded DArT Genotyping dataset";
		/*if (null != arrayOfMarkers && arrayOfMarkers.length > 0){
			String strUploadInfo = "";

			for (int i = 0; i < arrayOfMarkers.length; i++){
				Integer iMarkerId = arrayOfMarkers[i].getMarkerId();
				String strMarkerName = arrayOfMarkers[i].getMarkerName();
				String strGID = arrayOfMarkers[i].getDbAccessionId();
				String strMarker = "Marker: " + iMarkerId + ": " + strMarkerName + " GID: " + strGID;
				strUploadInfo += strMarker + "\n";
			}
			strDataUploaded = "Uploaded SSR Genotype with following Marker(s): \n" + strUploadInfo;
		}*/

		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromSourceSheet() {
		return listOfDataInSourceSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromDataSheet() {
		return listOfDataInDataSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet() {
		return listOfDataInGIDsSheet;
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
