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
import java.util.SortedMap;
import java.util.TreeMap;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.GetGermplasmByNameModes;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmNameDetails;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.generationcp.middleware.pojos.gdms.SSRDataRow;
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
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.marker.UploadField;

import com.vaadin.ui.Window;

public class SSRGenotype implements  UploadMarker {
	private String strFileLocation;
	private Workbook workbook;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
		
	private Marker addedMarker;
	private DatasetBean dataset;
	private AccessionMetaDataBean accMetadataSet;
	private MarkerMetadataSet markerMetadataSet;
	private GenotypeUsersBean datasetUser;
	private IntArrayBean alleleValues;
	
	private Session localSession;
	private Session centralSession;
	
	private Session session;
	
	private Marker[] arrayOfMarkers;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	
	List<SSRDataRow> listOfSSRTDataRows; 
	int intDataOrderIndex =0;
	ManagerFactory factory =null;
	GermplasmDataManager manager;
	GenotypicDataManager genoManager;
	
	GermplasmListManager listManager;
	
	CheckNumericDatatype cnd = new CheckNumericDatatype();
	String notMatchingDataDB="";
	String alertGN="no";
    String alertGID="no";
    String notMatchingData="";
    String notMatchingGIDS="";
    int size=0;
    String ErrMsg="";
    String strMarkerType="SSR";
    
    int iDatasetId = 0;
    int intRMarkerId = 1;
    int maxMid=0;
	int mid=0;
    private Transaction tx;
    
    SortedMap map = new TreeMap();
    SortedMap finalMarkersMap = new TreeMap();
    SortedMap mapN = new TreeMap();

	SortedMap hashMapOfGIDsandNIDs = new TreeMap();
	////System.out.println(",,,,,,,,,,,,,,,,,gNames="+gNames);
	ArrayList finalList =new ArrayList();
	ArrayList gidL=new ArrayList();
	
	Name name = null;
    
	private GDMSMain _mainHomePage;
	
	List gDupNameListV;
	private HashMap<String, String> hmOfDuplicateGNames;
	private HashMap<Integer, String> hmOfColIndexAndMarkerName;
	private HashMap<String, String> hmOfDuplicateGermNames;
	//private HashMap<String, String> hmOfDuplicateGNames;
	ArrayList listOfGNames = new ArrayList<String>();
	List gDupNameList;
	ArrayList<String> dupGermplasmsList=new ArrayList<String>();
	
	HashMap<String, Integer> hashMapOfEntryIDandGID = new HashMap<String, Integer>();
	
	HashMap<String, Integer> hashMapOfEntryIDGID = new HashMap<String, Integer>();
	
	HashMap<String, String> hashMapOfEntryIDandGName = new HashMap<String, String>();
	
	List<String> listOfGNamesFromTable=new ArrayList<String>();
	List<Integer> listOfGIDFromTable;
	HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
	boolean dupGermConfirmation;
	static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();
	
	String strErrorMsg="no";
	private ArrayList<String> listOfMarkersFromTheSheet = new ArrayList<String>();
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
			throw new GDMSException("Error Reading SSR Genotype Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading SSR Genotype Sheet - " + e.getMessage());
		}
	}

	@Override
	public String validateDataInExcelSheet() throws GDMSException {

		if (false == strSheetNames[0].equalsIgnoreCase("SSR_Source")){
			throw new GDMSException("SSR_Source Sheet Name Not Found");
		}

		if (false == strSheetNames[1].equalsIgnoreCase("SSR_Data List")){
			throw new GDMSException("SSR_Data List Sheet Name Not Found");
		}

		//check the template fields in source sheet
		for(int i = 0; i < strSheetNames.length; i++){
			String strSheetName = strSheetNames[i].toString();
			if(strSheetName.equalsIgnoreCase("SSR_Source")){
				Sheet sName = workbook.getSheet(strSheetName);
				String strTempColumnNames[] = {"Institute", " Principle investigator", "Dataset Name", "Dataset description", "Genus", "Species", "Ploidy", "Missing Data", "Remark"};
				for(int j = 0; j < strTempColumnNames.length; j++){
					String strColFromSheet = (String)sName.getCell(0, j).getContents().trim();
					if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException("Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException("Delete Empty Rows");
					}
				}															
			}


			//SSR_DataList fields validation
			if(strSheetName.equalsIgnoreCase("SSR_Data List")){
				Sheet sName = workbook.getSheet(strSheetName);
				String strTempColumnNames[] = {"Accession"};
				for(int j = 0; j < strTempColumnNames.length; j++){
					String strColFromSheet = (String)sName.getCell(j, 0).getContents().trim();
					if(!strTempColumnNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
						throw new GDMSException("Column Name Not Found");
					}
					if(strColFromSheet == null || strColFromSheet == ""){
						throw new GDMSException("Delete Empty Columns");
					}
				}
			}
		}

		//check the required fields in SSR_Source;
		for(int i = 0; i < strSheetNames.length; i++){
			Sheet sName = workbook.getSheet(i);
			int intNoOfRows = sName.getRows();
			if(strSheetNames[i].equalsIgnoreCase("SSR_Source")){
				for(int j = 0; j < intNoOfRows; j++){
					String strFieldsName = sName.getCell(0, j).getContents().trim();
					if(strFieldsName.equalsIgnoreCase("Institute") || strFieldsName.equalsIgnoreCase("Dataset Name") || strFieldsName.equalsIgnoreCase("Dataset description") || strFieldsName.equalsIgnoreCase("genus") || strFieldsName.equalsIgnoreCase("Ploidy") || strFieldsName.equalsIgnoreCase("missing data")){
						String strFieldValue = sName.getCell(1, j).getContents().trim();
						if(strFieldValue == null || strFieldValue == ""){
							throw new GDMSException("Please provide values for Required Fields");
						}
					}
				}
			}

			int intNoOfColumns = sName.getColumns();
			//GID, Accession, Marker and Amount fields from ssr_data list.
			if(strSheetNames[i].equalsIgnoreCase("SSR_Data List")){
				
				String strArrayOfReqColumnNames[] = {"Accession"};	

				//Checking if all the columns are present in the DataList
				for(int j = 0; j < strArrayOfReqColumnNames.length; j++){
					String strColFromSheet = (String)sName.getCell(j, 0).getContents().trim();
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
					String strLine = (String)sName.getCell(1, r).getContents().trim();
					if(strLine == null || strLine == ""){
						String strRowNumber = String.valueOf(sName.getCell(1, r).getRow()+1);	
						String strErrMsg = "Please provide a value at cell position " + "[1" + ", " + strRowNumber + "] in SSR_DataList sheet.";
						throw new GDMSException(strErrMsg);
					}
				}

				hmOfColIndexAndMarkerName = new HashMap<Integer, String>();
				for(int colIndex = 1; colIndex < intNoOfColumns; colIndex++){
					String strMarkerName = sName.getCell(colIndex, 0).getContents().toString();
					if(!listOfMarkersFromTheSheet.contains(strMarkerName))
						listOfMarkersFromTheSheet.add(strMarkerName);
					hmOfColIndexAndMarkerName.put(colIndex, strMarkerName);
					for(int r = 0; r < intNoOfRows; r++){
						String strCellValue = (String)sName.getCell(colIndex, r).getContents().trim();
						if(strCellValue == null || strCellValue == ""){
							String strRowNumber = String.valueOf(sName.getCell(colIndex, r).getRow()+1);	
							String strErrMsg = "Please provide a value at cell position " + "[" + colIndex + ", " + strRowNumber + "] in Mapping_DataList sheet.";
							throw new GDMSException(strErrMsg);
						}
					}
				}
				//System.out.println("hmOfColIndexAndMarkerName:"+hmOfColIndexAndMarkerName);
				listOfGNames = new ArrayList<String>();
				gDupNameList=new ArrayList<String>();
				for(int colIndex = 0; colIndex < 1; colIndex++){
					for(int r = 1; r < intNoOfRows; r++){
						String strCellValue = (String)sName.getCell(colIndex, r).getContents().trim();
						
						if(listOfGNames.contains(strCellValue)){							
							gDupNameList.add(strCellValue);							
						}
						
						listOfGNames.add(strCellValue);
					}
					
				}
				//System.out.println("listOfGNames:"+listOfGNames);
				//System.out.println("gDupNameList:"+gDupNameList);
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
		if(listOfMarkersFromTheSheet.size()>320 || listOfGNames.size()>500){
			strErrorMsg = "Data to be uploaded cannot be displayed in the table. Please click on Upload to upload the data directly";			
		}
		
		
		return strErrorMsg;
	}

	@Override
	public void createObjectsToBeDisplayedOnGUI(GDMSMain theMainHomePage) throws GDMSException {
		
		_mainHomePage = theMainHomePage;

		Sheet sourceSheet = workbook.getSheet(0);
		listOfDataInSourceSheet = new ArrayList<HashMap<String,String>>();

		
		HashMap<String, String> hmOfDataInSourceSheet = new HashMap<String, String>();

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
		
		String strPloidy = sourceSheet.getCell(1, 6).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Ploidy.toString(), strPloidy);
		
		String strMissingData = sourceSheet.getCell(1, 7).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.MissingData.toString(), strMissingData);

		String strRemark = sourceSheet.getCell(1, 8).getContents().toString();
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
		String strLine="";
		HashMap<String, Integer> strMap=new HashMap<String, Integer>();
		Sheet dataSheet = workbook.getSheet(1);
		int iNumOfRowsInDataSheet = dataSheet.getRows();
		int iNumOfColumnsInDataSheet = dataSheet.getColumns();
		listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();
		int r=1;
		for (int rIndex = 1; rIndex < iNumOfRowsInDataSheet; rIndex++){

			HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();

			
			String strLineValue = dataSheet.getCell(0, rIndex).getContents().toString();
			
			/*if(gDupNameList.contains(strLineValue)){
				hmOfDataInDataSheet.put(UploadField.Accession.toString(), hmOfDuplicateGNames.get(strLineValue+r));
				r++;
			}else*/			
				//hmOfDataInDataSheet.put(UploadField.Accession.toString(), strLineValue);
				if(gDupNameList.contains(strLineValue)){
					if(strLine!=strLineValue)
						r=1;
					if(strMap.containsKey(strLineValue))
						r= Integer.parseInt(strMap.get(strLineValue).toString())+1;
							
					String strLine1=strLineValue+" (Sample "+r+")";
					hmOfDuplicateGermNames.put(strLine1, strLineValue);
					hmOfDataInDataSheet.put(UploadField.Accession.toString(), strLine1);
					
					//strList.add(strLineValue);
					strMap.put(strLineValue, r);
					strLine=strLineValue;
					
					//r++;
				}else{			
					hmOfDataInDataSheet.put(UploadField.Accession.toString(), strLineValue);
					r=1;
					strLine=strLineValue;
				}
			//Inserting the Marker-Names and Marker-Values
			for (int cIndex = 1; cIndex < iNumOfColumnsInDataSheet; cIndex++){
				String strMarkerName = hmOfColIndexAndMarkerName.get(cIndex);
				String strMarkerValue = dataSheet.getCell(cIndex, rIndex).getContents().toString();
				//System.out.println(strLineValue+"   "+strMarkerName+","+strMarkerValue);
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
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}

	@Override
	public void setDataToBeUploded(ArrayList<HashMap<String, String>> theListOfSourceDataRows, ArrayList<HashMap<String, String>> listOfDataRows, ArrayList<HashMap<String, String>> listOfGIDRows) {
		listOfDataRowsFromSourceTable = theListOfSourceDataRows;
		listOfDataRowsFromDataTable = listOfDataRows;
		listOfGIDRowsFromGIDTableForDArT = listOfGIDRows;
	}

	@Override
	public void upload() throws GDMSException {
		validateData();
		createObjectsToBeSavedToDB();
	}

	@Override
	public void validateData() throws GDMSException {

		String strColsInSourceTable[] = {UploadField.Institute.toString(), UploadField.PrincipleInvestigator.toString(), 
				UploadField.DatasetName.toString(), UploadField.DatasetDescription.toString(), 
				UploadField.Genus.toString(), UploadField.Species.toString(), UploadField.Ploidy.toString(),
				UploadField.MissingData.toString(), UploadField.Remark.toString()};
		
		HashMap<String, String> hmOfSourceColumnsAndValuesFromGUI = listOfDataRowsFromSourceTable.get(0);
		
		for(int j = 0; j < strColsInSourceTable.length; j++){
			String strCol = strColsInSourceTable[j];
			if (false == hmOfSourceColumnsAndValuesFromGUI.containsKey(strCol)){
				throw new GDMSException(strCol + " column not found in data SSR_Source sheet.");
			} else {
				//Institute, Dataset-Name, Dataset-Description, Genus, Missing-Data are required columns
				if (strCol.equalsIgnoreCase(UploadField.Institute.toString()) || strCol.equalsIgnoreCase(UploadField.DatasetName.toString()) ||
						strCol.equalsIgnoreCase(UploadField.DatasetDescription.toString()) || strCol.equalsIgnoreCase(UploadField.Genus.toString()) || strCol.equalsIgnoreCase(UploadField.Ploidy.toString()) ||  
						strCol.equalsIgnoreCase(UploadField.MissingData.toString())) {
					String strValue = hmOfSourceColumnsAndValuesFromGUI.get(strCol);
					if (null == strValue || strValue.equals("")){
						throw new GDMSException("Please provide a value for " +  strCol + " column in data SSR_Source sheet.");
					}
				}
			}
		}
		
		String strColsInDataTable[] = {UploadField.Accession.toString()};
		for (int i = 0; i < strColsInDataTable.length; i++){
			String strCol = strColsInDataTable[i];
			for (int j = 0; j < listOfDataRowsFromDataTable.size(); j++){
				HashMap<String, String> hmOfDataColumnsAndValuesFromGUI = listOfDataRowsFromDataTable.get(j);
				
				if (false == hmOfDataColumnsAndValuesFromGUI.containsKey(strCol)){
					throw new GDMSException(strCol + " column not found in data SSR_Data List table.");
				}else {
					//GID, Accession, Marker, Amount are required columns
					/*if (strCol.equalsIgnoreCase(UploadField.GID.toString()) || strCol.equalsIgnoreCase(UploadField.Accession.toString()) ||
							strCol.equalsIgnoreCase(UploadField.Marker.toString()) || strCol.equalsIgnoreCase(UploadField.Amount.toString())) {*/
					if (strCol.equalsIgnoreCase(UploadField.Accession.toString()) ) {
						String strValue = hmOfDataColumnsAndValuesFromGUI.get(strCol);
						if (null == strValue || strValue.equals("")){
							throw new GDMSException("Please provide a value for " +  strCol + " column in data SSR_Data List table.");
						}
					}
				}
				/*if(strCol.equalsIgnoreCase(UploadField.Amount.toString())){
					String strValue = hmOfDataColumnsAndValuesFromGUI.get(strCol);
					float parseFloat = Float.parseFloat(strValue);
					if (0 > parseFloat || 1 < parseFloat){
						throw new GDMSException("Amount value must be greater than 0 and less than 1");
					}
				}*/
			}
		}
	}

	
	public void createObjectsToBeSavedToDB() throws GDMSException {		
		
		ArrayList<String> listOfMarkersProvided = new ArrayList<String>();
		
		String curAcc ="";String preAcc = "";
		String strPreAmount="";
		List newListL=new ArrayList();
		List newListC=new ArrayList();
		//try {	
		Object obj=null;
		Object objL=null;
		Iterator itListC=null;
		Iterator itListL=null;
		
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
		
			manager = factory.getGermplasmDataManager();
			genoManager=factory.getGenotypicDataManager();
			
			listManager=factory.getGermplasmListManager();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
		String strGermplasmSelected = GDMSModel.getGDMSModel().getGermplasmSelected();
		//System.out.println("..>>>>>>>>....:"+strGermplasmSelected);
		
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
		
		String marker="";
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
		/*try{
		System.out.println(listManager.getGermplasmListById(list_id));
		System.out.println("^^^^^^^^^^^^^^^  :"+listManager.getGermplasmListDataByListId(list_id, 0, 100));
	} catch (MiddlewareQueryException e) {
		throw new GDMSException(e.getMessage());
	}*/
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
	            hashMapOfEntryIDandGID.put(strGermplasmSelected+"-" + strMareO[0].toString()+"!~!"+strMareO[1].toString().trim(), Integer.parseInt(strMareO[2].toString()));
	            hashMapOfEntryIDGID.put(strGermplasmSelected+"-" + strMareO[0].toString(), Integer.parseInt(strMareO[2].toString()));
	            hashMapOfEntryIDandGName.put(strGermplasmSelected+"-" + strMareO[0].toString(), strMareO[1].toString());
       	}
		}
		System.out.println("......:"+hashMapOfEntryIDandGID);
		System.out.println(":::::::"+hashMapOfGNameandGID);
		System.out.println("^^^^^^^^^^^^  :"+hashMapOfEntryIDandGName);
		System.out.println("listOfGNames:"+listOfGNames);
		/*System.out.println("listEntries:"+listEntries);*/
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
			 String markersForQuery="";
	         ArrayList markerList = new ArrayList();
	         ArrayList<String> markerLowerCaseList = new ArrayList<String>();
			String strMarCheck = listOfDataRowsFromDataTable.get(0).get(UploadField.Marker.toString());
			//System.out.println("listOfDataRowsFromDataTable:"+listOfDataRowsFromDataTable);
			ArrayList<String> listOfMarkerNamesInLoweCase = new ArrayList<String>();
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
						if( (false ==  listOfMarkerNamesFromSourceTable.contains(strMarkerNameFromSourceTable))&&(strMarkerNameFromSourceTable != "SNo")&&(strMarkerNameFromSourceTable != "Accession")){
							listOfMarkerNamesFromSourceTable.add(strMarkerNameFromSourceTable);
							markerList.add(strMarkerNameFromSourceTable);
							marker = marker +"'"+ strMarkerNameFromSourceTable+"',";
						}
						if( (strMarkerNameFromSourceTable != "SNo")&&(strMarkerNameFromSourceTable != "Accession")){
							listOfMarkers.add(strMarkerNameFromSourceTable.toLowerCase());	
							
						}
					}
				}
			}
			
			 for(int ml=0;ml<markerList.size();ml++){
	        	   markersForQuery=markersForQuery+"'"+markerList.get(ml)+"',";
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
			        	////System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
						lstMarkers.add(strMareO[1].toString().toLowerCase());
						markersMap.put(strMareO[1].toString().toLowerCase(), strMareO[0]);
						
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
		            		lstMarkers.add(strMareO[1].toString().toLowerCase());	            		
		            		markersMap.put(strMareO[1].toString().toLowerCase(), strMareO[0]);	
						}
					}
				}
				
	           
	           
		String gidsString="";
				
			//String gidsForQuery = "";
			ArrayList gidsForQuery=new ArrayList();
			String gNames="";
			//HashMap<Integer, String> GIDsMap = new HashMap<Integer, String>();
			HashMap<String, Integer> GIDsMap1 = new HashMap<String, Integer>();
			HashMap<String, Integer> GIDsMap = new HashMap<String, Integer>();
			ArrayList gidNamesList=new ArrayList();
			
            List lstgermpName = new ArrayList();
            
			List<Name> names = null;
		
            ArrayList gidsDBList = new ArrayList();
			ArrayList gNamesDBList = new ArrayList();
			hashMap.clear();
			//System.out.println("gnamesList=:"+gnamesList);
			map = new TreeMap();
	        finalMarkersMap = new TreeMap();
	        hashMapOfGIDsandNIDs = new TreeMap();
	        gidL=new ArrayList();
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
			        	mapN.put(g.getGermplasmId()+"!~!"+g.getNVal(), g.getNameId());
			            GIDsMap.put(g.getNVal().toLowerCase(), g.getGermplasmId());
			        }
					
					if(gNamesDBList.size()!=listOfGNamesFromTable.size()){
						germplasmList = manager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.STANDARDIZED);
						for (GermplasmNameDetails g : germplasmList) {
							if(!(gidsDBList.contains(g.getNameId()))){
				        		gidsDBList.add(g.getNameId());
				        		gNamesDBList.add(g.getNVal());
				        		addValues(g.getNVal(), g.getGermplasmId());					        		
				        	}   
							if(!gidL.contains(g.getGermplasmId()))
				            	gidL.add(g.getGermplasmId());
							mapN.put(g.getGermplasmId()+"!~!"+g.getNVal(), g.getNameId());
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
							//listOfGermplasmNames.add(names.getNval());
							
							mapN.put(g.getGermplasmId()+"!~!"+g.getNVal(), g.getNameId());
				            GIDsMap.put(g.getNVal().toLowerCase(), g.getGermplasmId());						
				        }
					}
					
					
				} catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
			
			
			System.out.println("mapN:"+mapN);
           if(gNamesDBList.size()==0){
        	   alertGID="yes";
        	   size=0;
           }
           int gidToCompare=0;
           String gNameToCompare="";
          ArrayList gNameFromMap=new ArrayList();
          
		finalList =new ArrayList();
		
		
		 for(int a=0;a<listOfGNames.size();a++){
	        	String strGenotype=listOfGNames.get(a).toString();
	        	String strGName=hashMapOfEntryIDandGName.get(strGenotype);
				Integer strGID =hashMapOfEntryIDandGID.get(strGenotype+"!~!"+strGName);
				System.out.println("%%%%%%%%%%%   :"+strGID+"!~!"+strGName);
	        	
	        	finalList.add(strGID+"~!~"+mapN.get(strGID+"!~!"+strGName));	
	        	
	        }
		 
		 System.out.println("finalList:"+finalList);
		 
        /*for(int a=0;a<gidsList.size();a++){
        	int gid1=Integer.parseInt(gidsList.get(a).toString());
        	if(gidL.contains(gid1)){
        		finalList.add(gid1+"~!~"+hashMapOfGIDsandNIDs.get(gid1));	
        	}
        }*/
        ////System.out.println("******************  "+finalList);
		
        HashMap<String, String> hmOfSourceFieldsAndValues = listOfDataRowsFromSourceTable.get(0);
		String strInstitute = hmOfSourceFieldsAndValues.get(UploadField.Institute.toString());
		String strPrincipleInvestigator = hmOfSourceFieldsAndValues.get(UploadField.PrincipleInvestigator.toString());
		String strDatasetName = hmOfSourceFieldsAndValues.get(UploadField.DatasetName.toString());
		if(strDatasetName.length()>30){
			ErrMsg = "Dataset Name value exceeds max char size.";
			throw new GDMSException(ErrMsg); 
		}
		
		String strDatasetDescription = hmOfSourceFieldsAndValues.get(UploadField.DatasetDescription.toString());
		String strGenus = hmOfSourceFieldsAndValues.get(UploadField.Genus.toString());
		String strSpecies = hmOfSourceFieldsAndValues.get(UploadField.Species.toString());
		String strMissingData = hmOfSourceFieldsAndValues.get(UploadField.MissingData.toString());
		String strRemark = hmOfSourceFieldsAndValues.get(UploadField.Remark.toString());

		//Creating the DatasetUsers object first
		Integer iUserId = 0;
		

		try {
			
			iUserId=GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId();
			
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
			//ErrMsg = "Dataset Name value exceeds max char size.";
			throw new GDMSException("Dataset Name value exceeds max char size.");
		}
		
		int intAnID=0;
		Integer iMarkerId = 0;
		String strDatasetType = "SSR";
		String strDataType = "int";
		Date uploadTemplateDate = new Date(System.currentTimeMillis()); 
		String strRemarks = ""; 
		String strMethod = null;
		String strScore = null;
		int iUploadedMarkerCount = 0;
		int iNumOfMarkers = listOfMarkersProvided.size();
		arrayOfMarkers = new Marker[iNumOfMarkers];
		Integer iNewDatasetId = null; //Test class says iNewDatasetId must be zero

		long datasetLastId = 0;
		 long lastId = 0;
		 
		//int iNumOfMarkers = listOfMarkerNamesFromSourceTable.size();
		//int iNumOfGIDs = listOfGIDFromTable.size();
		//arrayOfMarkers = new Marker[iNumOfMarkers*iNumOfGIDs];
		int iDatasetId = 0;
		 tx=localSession.beginTransaction();
		 
		 /** retrieving maximum marker id from 'marker' table of database **/
			try{
				lastId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_MARKER);
			}catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
			maxMid=(int)lastId; 
		 
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
			
			 
			 try{
				 datasetLastId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_DATASET);
				}catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				int intDatasetId=(int)datasetLastId;
				
				iDatasetId=intDatasetId-1;
		 
				long lastIdMPId=0;
				try{
					lastIdMPId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_ALLELE_VALUES);
				}catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				int maxCHid=(int)lastIdMPId;
				intAnID=maxCHid-1;
		 
		 
		
		
		dataset = new DatasetBean();
		dataset.setDataset_id(iDatasetId);
		dataset.setDataset_name(strDatasetName);
		dataset.setDataset_desc(strDatasetDescription);
		dataset.setDataset_type(strDatasetType);
		dataset.setGenus(strGenus);
		dataset.setSpecies(strSpecies);
		dataset.setUpload_template_date(curDate);	
		dataset.setRemarks(strRemarks);
		dataset.setDatatype(strDataType);
		dataset.setMissing_data(strMissingData);
		localSession.save(dataset);
		
		datasetUser = new GenotypeUsersBean();
		datasetUser.setDataset_id(iDatasetId);
		datasetUser.setUser_id(iUserId);
		localSession.save(datasetUser);
		
		
		int intMaxAccId=0;
		Object objAcc=null;
		Iterator itList=null;
		List listValues=null;
		Query queryA=localSession.createSQLQuery("select min(acc_metadataset_id) from gdms_acc_metadataset");
		
		listValues=queryA.list();
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
		int  accSampleId=1;
		
		for(int a=0;a<finalList.size();a++){	
			
        	String[] strList=finalList.get(a).toString().split("~!~");
        	
        	Integer iGId = Integer.parseInt(strList[0].toString());
        	int nid=Integer.parseInt(strList[1].toString());
        	if(previousAccGID.contains(iGId)){//.equals(previousGID)){
				//accSampleId=2;
        		accSampleId=accSampleId+1;
			}else
				accSampleId=1;      	
        	//System.out.println("..........a:"+a+"   "+finalList.get(a).toString()+"   "+accSampleId);
        	accMetadataSet=new AccessionMetaDataBean();					
			//******************   GermplasmTemp   *********************//*
        	accMetadataSet.setAccMetadatasetId(intMaxAccId);
        	accMetadataSet.setDatasetId(iDatasetId);
        	accMetadataSet.setGid(iGId);
        	accMetadataSet.setNid(nid);
        	accMetadataSet.setAccSampleId(accSampleId);			
        	localSession.save(accMetadataSet);
        	if(!previousAccGID.contains(iGId))
        		previousAccGID.add(iGId);
        	intMaxAccId--;
			if (a % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
        
        }
		 ArrayList mids=new ArrayList();
         
         HashMap<String, Object> finalHashMapMarkerAndIDs = new HashMap<String, Object>();
       int ml=0;
		for(int f=0; f<markerList.size();f++){
			MarkerInfoBean mib=new MarkerInfoBean();
			if(lstMarkers.contains(listOfMarkers.get(ml))){
				//System.out.println("markersMap:"+markersMap);
				//System.out.println("markerList.get(f)L:"+markerList.get(f)+"  "+markersMap.get(markerLowerCaseList.get(ml)));
				intRMarkerId=(Integer)(markersMap.get(listOfMarkers.get(ml)));							
				mids.add(intRMarkerId);
				finalHashMapMarkerAndIDs.put(markerList.get(f).toString().toLowerCase(), intRMarkerId);
			}else{
				//maxMid=maxMid+1;
				maxMid=maxMid-1;
				intRMarkerId=maxMid;
				finalHashMapMarkerAndIDs.put(markerList.get(f).toString().toLowerCase(), intRMarkerId);
				mids.add(intRMarkerId);	
				mib.setMarkerId(intRMarkerId);
				mib.setMarker_type(strMarkerType);
				mib.setMarker_name(markerList.get(f).toString());
				//mib.setCrop(sheetSource.getCell(1,5).getContents());
				mib.setSpecies(strSpecies);
				
				localSession.save(mib);
				if (f % 1 == 0){
					localSession.flush();
					localSession.clear();
				}
			}
			
			ml++;
		}
		
		//listOfSSRTDataRows = new ArrayList<SSRDataRow>();
		//System.out.println("listOfDataRowsFromDataTable:"+listOfDataRowsFromDataTable);
		//String marker="";
		int m=0;	
		
		ArrayList<Integer> previousGIDA=new ArrayList<Integer>();
		ArrayList<Integer> previousMIDA=new ArrayList<Integer>();
		//listOfMarkers=new ArrayList();
		int markerId=0;
		Integer iGId =0;
		Integer accSampleIdA=1;
		Integer markerSampleIdA=1;
		
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){			
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);	
			String strGenotype="";
			
			String strEntry = hashMapOfDataRow.get(UploadField.Accession.toString());
			if(hmOfDuplicateGermNames.containsKey(strEntry))
				strGenotype=hmOfDuplicateGermNames.get(strEntry);
			else
				strGenotype=strEntry;
			
			//System.out.println("strGenotype:"+strGenotype);
			Integer strGID =hashMapOfEntryIDGID.get(strGenotype);
			//System.out.println("strGID:"+strGID);
			//Integer strGID = Integer.parseInt(hashMapOfDataRow.get(UploadField.Line.toString()).toString());
			if(previousGIDA.contains(strGID)){//.equals(previousGID)){
				//accSampleIdA=2;
				accSampleIdA=accSampleIdA+1;
			}else
				accSampleIdA=1;
			//iMpId--;
			//System.out.println("strGID:"+strGID+"   "+accSampleIdA);
			previousMIDA=new ArrayList<Integer>();
			String charData="";
			for (int j = 0; j < markerList.size(); j++) {					
			//	strCharValue=hashMapOfDataRow.get(markerList.get(j));					
				String strMarkerName = markerList.get(j).toString();							
					String strValue1="";
					String strRValue1="0.0/0.0";
				markerId=Integer.parseInt(finalHashMapMarkerAndIDs.get(strMarkerName.toLowerCase()).toString());
				//System.out.println("mName:"+strMarkerFromDArTDataTable+" ID:"+finalHashMapMarkerAndIDs.get(strMarkerFromDArTDataTable.toLowerCase()).toString());
				//if(markerId==previousMID){
				if(previousMIDA.contains(markerId)){
					//markerSampleIdA=2;
					markerSampleIdA=markerSampleIdA+1;
				}else
					markerSampleIdA=1;
			
				alleleValues = new IntArrayBean();
				IntArrayCompositeKey cack = new IntArrayCompositeKey();
				
				//**************** writing to char_values tables........
				cack.setDataset_id(iDatasetId);
				cack.setAn_id(intAnID);
				alleleValues.setComKey(cack);
							
				alleleValues.setGid(strGID);			
				strValue1=hashMapOfDataRow.get(markerList.get(j));
				String charStr=strValue1;
				String charData1="";
				if(strValue1.length()>5){
					//System.out.println(strMarkerName+": charStr:"+charStr);
					if(charStr.contains(":")){
						String str1="";
						String str2="";
						//String charStr=str.get(s);
						
						String[] strchar=charStr.split(":");
						for(int s=0;s<strchar.length;s++){
							charData1=charData1+strchar[s]+"/";
						}
						charData=charData1.substring(0, charData1.length()-1);
						
						/*str1=charStr.substring(0, charStr.length()-2);
						str2=charStr.substring(2, charStr.length());
						charData=str1+"/"+str2;*/
					}else if(charStr.contains("/")){
						charData=charStr;
					}else{
						throw new GDMSException("Heterozygote data representation should be either : or /");
					}
				}else{		
					if((!charStr.contains(":"))||(!charStr.contains("/")))
						charData=charStr+"/"+charStr;
					else
						charData=charStr;
				
				}	
					
				alleleValues.setAllele_bin_value(charData);					
				alleleValues.setAllele_raw_value(strRValue1);	
					
				
				alleleValues.setMarker_id(markerId);
				alleleValues.setAccSampleID(accSampleIdA);
				alleleValues.setMarkerSampleId(markerSampleIdA);
				localSession.save(alleleValues);
				if(!previousMIDA.contains(markerId))
					previousMIDA.add(markerId);
				if (i % 1 == 0){
					localSession.flush();
					localSession.clear();
				}
				intAnID--;
				//m++;
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
			////System.out.println("gids doesnot Exists    :"+lstgermpName+"   "+gids[l]);
			intMaxMarMetaId--;
			int mid=Integer.parseInt(mids.get(m1).toString());
			if(previousMIDMMD.contains(mid)){
				//markerSampleIdA=2;
				markerSampleIdA=markerSampleIdA+1;
			}else
				markerSampleIdA=1;
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
			
		//saveSSRGenotype();
				
		tx.commit();
		//}
		
		
	}

	

	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDataUploaded() {
		String strDataUploaded = "";
		//if (null != arrayOfMarkers && arrayOfMarkers.length > 0){
			String strUploadInfo = "";

			/*for (int i = 0; i < arrayOfMarkers.length; i++){
				Integer iMarkerId = arrayOfMarkers[i].getMarkerId();
				String strMarkerName = arrayOfMarkers[i].getMarkerName();
				String strGID = arrayOfMarkers[i].getDbAccessionId();
				String strMarker = "Marker: " + iMarkerId + ": " + strMarkerName + " GID: " + strGID;
				strUploadInfo += strMarker + "\n";
			}*/
			strDataUploaded = "Uploaded SSR Genotype ";
		//}
		
		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet() {
		// TODO Auto-generated method stub
		return null;
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
	public double roundThree(double in){		
		return Math.round(in*1000.0)/1000.0;
	}
}
