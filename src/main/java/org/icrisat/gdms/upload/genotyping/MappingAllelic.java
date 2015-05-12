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

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.generationcp.middleware.dao.gdms.MarkerMetadataSetDAO;
import org.generationcp.middleware.domain.conformity.ConformityGermplasmInput;
import org.generationcp.middleware.domain.conformity.UploadInput;
import org.generationcp.middleware.domain.conformity.util.ConformityInputTransformer;
import org.generationcp.middleware.exceptions.ConformityException;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.GetGermplasmByNameModes;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.pojos.GermplasmNameDetails;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AccMetadataSet;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.DartValues;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.MappingABHRow;
import org.generationcp.middleware.pojos.gdms.MappingAllelicSNPRow;
import org.generationcp.middleware.pojos.gdms.MappingAllelicSSRDArTRow;
import org.generationcp.middleware.pojos.gdms.MappingPopValues;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.service.ConformityTestingServiceImpl;
import org.generationcp.middleware.service.api.ConformityTestingService;
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

public class MappingAllelic implements UploadMarker {
	private String strFileLocation;
	private Workbook workbook;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
	private HashMap<Integer, String> hmOfColIndexAndMarkerName;
	
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	public ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	
	private AccessionMetaDataBean accMetadataSet;
	private MarkerMetaDataBean markerMetadataSet;
	private GenotypeUsersBean datasetUser;
	private MappingPopulationBean mappingPop;
	private MappingPopCharValuesBean mappingPopValues;
	private DatasetBean dataset;
	private MarkerInfoBean addedMarker;
	
	
	
	private MappingPopValues[] arrayOfMappingPopValues;
	private ManagerFactory factory;
	private GenotypicDataManager genoManager;
	//private AlleleValues alleleValues;
	private IntArrayBean alleleValues;
	private CharArrayBean charValues;
	private GermplasmDataManager manager;
	private DartValues dartValues;
	
	private Session localSession;
	private Session centralSession;
	
	private Session session;
	private Transaction tx;
	
	private Session sessionL;
	private Session sessionC;
	
	List<MappingAllelicSSRDArTRow> listOfMPSSRDataRows; 
	List<MappingAllelicSNPRow> listOfMPSNPDataRows; 
	List<MappingABHRow> listOfMPABHDataRows; 
	
	ArrayList markersList=new ArrayList();
	
	String notMatchingDataPDB="";
	String notMatchingDataP="";   
    String notMatchingGIDSP="";
	
	String notMatchingDataDB="";
	String notMatchingData="";   
    String notMatchingGIDS="";
    
	String alertGN="no";
    String alertGID="no";
    String alertPGN="no";
    String alertPGID="no";
    
    int size=0;
    
    
	String ErrMsg ="";
	int iDatasetId = 0;
	int intRMarkerId = 1;
	
	int maxMid=0;
	int mid=0;
	
	int gid=0;
	int nid=0;
	
	String strMapDataDescription = "";
	String strScore = null;
	String strInstitute = null;
	String strEmail = null;
	String strPurposeOfStudy = null;
	
	int map_id=0;
	String strMappingType="allelic";
	Integer iMpId = null;  //Will be set/overridden by the function
	String strMapCharValue = "-";
    static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();
    
    static Map<String, ArrayList<String>> hashMapParents = new HashMap<String,  ArrayList<String>>();
	
    
    private ArrayList<Map<String, String>> parentDatahashMapOfDataRow;
    private ArrayList<Map<String, String>> populationDatahashMapOfDataRow;
    
    public ArrayList<Map<String, String>> parentData;
    public ArrayList<Map<String, String>> populationData;
    
    /*private ArrayList<HashMap<String, String>> parentDataRow;
    private ArrayList<HashMap<String, String>> populationDataRow;*/
    
    private Map<String, String>[] parentDataRow;
    private Map<String, String>[] populationDataRow;
    
    
    String nonExistingMarkers="";
    ArrayList markerforParentsList=new ArrayList();
    SortedMap mapN = new TreeMap();
	////System.out.println(",,,,,,,,,,,,,,,,,gNames="+gNames);
	ArrayList finalList =new ArrayList();
	ArrayList gidL=new ArrayList();
	String gidsForQuery = "";
	HashMap<String, Integer> GIDsMap = new HashMap<String, Integer>();
	String gNames="";
	ArrayList gidsAList=new ArrayList();
	ArrayList gidNamesList=new ArrayList();
	ArrayList NamesList=new ArrayList();
	
	
	int parentA_nid=0;
	int parentB_nid=0;
	
	private GDMSMain _mainHomePage;
	private HashMap<String, String> hmOfDuplicateGermNames;
	//private HashMap<String, String> hmOfDuplicateGNames;
	//List gNames;
	List gDupNameList;
	List gDupNameListV;
	
	ArrayList<String> dupGermplasmsList=new ArrayList<String>();
	
	HashMap<String, Integer> hashMapOfEntryIDandGID = new HashMap<String, Integer>();
	HashMap<String, String> hashMapOfEntryIDandGName =  new HashMap<String, String>();
	List<String> listOfGNamesFromTable=new ArrayList<String>();
	List<Integer> listOfGIDFromTable;
	ArrayList listOfGNames = new ArrayList<String>();
	//ArrayList listOfGNames = new ArrayList<String>();
	
	boolean dupGermConfirmation;
	
	private ConformityTestingService conformityTestingService;
	ConformityGermplasmInput entry;
	private PedigreeDataManager pedigreeDataManager;
	
	
	//private ArrayList<HashMap<String, String>> listOfParentsDataRowsFromDB;
	private HashMap<String, String> listOfParentsDataRowsFromDB;
	
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
				//String strArrayOfReqColumnNames[] = {"Alias", "GID", "Line"};	
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

				
				listOfGNames = new ArrayList<String>();
				gDupNameList=new ArrayList<String>();
				//Checking if GIDs and Lines have been provided in all the rows				
				for(int r = 1; r < intNoOfRows; r++){
					/*String strGIDalue = (String)dataListSheet.getCell(1, r).getContents().trim();
					if(strGIDalue == null || strGIDalue == ""){
						String strRowNumber = String.valueOf(dataListSheet.getCell(1, r).getRow()+1);	
						String strErrMsg = "Please provide a value at cell position " + "[1" + ", " + strRowNumber + "] in Mapping_DataList sheet.";
						throw new GDMSException(strErrMsg);
					}*/
					String strLine = (String)dataListSheet.getCell(1, r).getContents().trim();
					if(listOfGNames.contains(strLine)){							
						gDupNameList.add(strLine);							
					}					
					listOfGNames.add(strLine);
					
					if(strLine == null || strLine == ""){
						String strRowNumber = String.valueOf(dataListSheet.getCell(1, r).getRow()+1);	
						String strErrMsg = "Please provide a value at cell position " + "[1" + ", " + strRowNumber + "] in Mapping_DataList sheet.";
						throw new GDMSException(strErrMsg);
					}
				}
				//System.out.println("listOfGNames:"+listOfGNames);
				//System.out.println(",,,,,,,,,,,,,,:"+gDupNameList);
				//int c=1;
			//	ArrayList<String> gNamesList=new ArrayList<String>();
				//int c=2;
				//hmOfDuplicateGermNames=new HashMap<String, String>();
				//hmOfDuplicateGNames=new HashMap<String, String>();
				//dupGermplasmsList=new ArrayList<String>();
				//int rep=1;
				//String gName="";
				//String strName="";
				//HashMap repGermplasms=new HashMap<String, Integer>();
				/*for(int d = 0; d <listOfGNames.size();d++){
					strName=listOfGNames.get(d).toString();
					
					if(!gName.equalsIgnoreCase(strName)){
						rep=1;
						System.out.println("gName!=strName"+gName+"  "+strName);
					}
					String strGName="";
					if(gDupNameList.contains(strName)){
						if(repGermplasms.containsKey(strName))
							rep=Integer.parseInt(repGermplasms.get(strName).toString())+1;
						
							
							
						strGName=strName+" (Sample "+rep+")";
						
						System.out.println("gDupNameList.contains(strName)..:"+gDupNameList.contains(strName)+"  "+strGName);
						//.put(strName+"!~!"+rep, strGName);
						hmOfDuplicateGermNames.put(strGName, strName);
						dupGermplasmsList.add(strGName);
						gName=strName;
						
						repGermplasms.put(strName, rep);
						//rep++;
					}else{
						rep=1;
					}
				}*/
				//System.out.println("%%%%%%%%%%%%%:"+);
				
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
		
		String strGermplasmSelected = GDMSModel.getGDMSModel().getGermplasmSelected();
		//System.out.println("..>>>....................>>>>>....:"+hmOfDuplicateGNames);

		//Creating a ArrayList of HashMap of fields and values from Mapping_DataList sheet
		Sheet dataSheet = workbook.getSheet(1);
		int iNumOfRowsInDataSheet = dataSheet.getRows();
		int iNumOfColumnsInDataSheet = dataSheet.getColumns();
		listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();
		
		//ArrayList strList=new ArrayList();
		String strLine="";
		HashMap<String, Integer> strMap=new HashMap<String, Integer>();
		
		int r=1;int r1=0;
		hmOfDuplicateGermNames=new HashMap<String, String>();
		for (int rIndex = 1; rIndex < iNumOfRowsInDataSheet; rIndex++){

			HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();

			String strAliasValue = dataSheet.getCell(0, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Alias.toString(), strAliasValue);

			/*String strGIDValue = dataSheet.getCell(1, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.GID.toString(), strGIDValue);
*/
			//String strLineValue = dataSheet.getCell(2, rIndex).getContents().toString();
			String strLineValue = dataSheet.getCell(1, rIndex).getContents().toString();
			/*System.out.println("....strLineValue:"+strLineValue);
			System.out.println("strMap:"+strMap);*/
			
			if(gDupNameList.contains(strLineValue)){
				if(strLine!=strLineValue)
					r=1;
				if(strMap.containsKey(strLineValue))
					r= Integer.parseInt(strMap.get(strLineValue).toString())+1;
						
				String strLine1=strLineValue+" (Sample "+r+")";
				hmOfDuplicateGermNames.put(strLine1, strLineValue);
				hmOfDataInDataSheet.put(UploadField.Line.toString(), strLine1);
				
				//strList.add(strLineValue);
				strMap.put(strLineValue, r);
				strLine=strLineValue;
				
				//r++;
			}else{			
				hmOfDataInDataSheet.put(UploadField.Line.toString(), strLineValue);
				r=1;
				strLine=strLineValue;
			}
			//hmOfDataInDataSheet.put(UploadField.Line.toString(), strLineValue);
			//System.out.println("%%%%%%%%%%%%:"+hmOfDataInDataSheet);
			//Inserting the Marker-Names and Marker-Values
			for (int cIndex = 2; cIndex < iNumOfColumnsInDataSheet; cIndex++){
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
			for (int rIndex = 0; rIndex < listOfDataRowsFromDataTable.size(); rIndex++){
				//System.out.println("listOfDataRowsFromDataTable.get(rIndex):"+listOfDataRowsFromDataTable.get(rIndex));
				HashMap<String, String> hmOfDataColumnsAndValuesFromGUI = listOfDataRowsFromDataTable.get(rIndex);
				if (false == hmOfDataColumnsAndValuesFromGUI.containsKey(strCol)){
					throw new GDMSException(strCol + " column not found in data Mapping_DataList table.");
				} else {
					//GID, Line
					//if(strCol.equalsIgnoreCase(UploadField.GID.toString()))
					/*if (strCol.equalsIgnoreCase(UploadField.GID.toString()) || 
							strCol.equalsIgnoreCase(UploadField.Line.toString())){*/
					if (strCol.equalsIgnoreCase(UploadField.Line.toString())){
						String strValue = hmOfDataColumnsAndValuesFromGUI.get(strCol);
						//System.out.println("$$$$$$$$$$$$$$$$$$  :"+strValue);
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
		List newListL=new ArrayList();
		List newListC=new ArrayList();
		//try {	
		Object obj=null;
		Object objL=null;
		Iterator itListC=null;
		Iterator itListL=null;
		//genoManager.getMar
		HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
		List lstMarkers = new ArrayList();
		String markersForQuery="";
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
		
			manager = factory.getGermplasmDataManager();
			genoManager=factory.getGenotypicDataManager();
			
			pedigreeDataManager = factory.getPedigreeDataManager();
		    conformityTestingService = new ConformityTestingServiceImpl(genoManager, pedigreeDataManager);
	
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
		//System.out.println("listOfGNames:"+listOfGNames);
		for(int w=0;w<listData.size();w++){
        	Object[] strMareO= (Object[])listData.get(w);
            //System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]+"   "+strMareO[2]);
        	if(listOfGNames.contains(strGermplasmSelected+"-" + strMareO[0].toString())){
        	 	listEntries.add(strGermplasmSelected+"-" + strMareO[0].toString());
	            listOfGNamesFromTable.add(strMareO[1].toString().trim());
	            listOfGIDFromTable.add(Integer.parseInt(strMareO[2].toString()));
	            hashMapOfGNameandGID.put(strMareO[1].toString().trim(), Integer.parseInt(strMareO[2].toString()));
	            hashMapOfEntryIDandGID.put(strGermplasmSelected+"-" + strMareO[0].toString(), Integer.parseInt(strMareO[2].toString()));
	            hashMapOfEntryIDandGName.put(strGermplasmSelected+"-" + strMareO[0].toString(), strMareO[1].toString());
        	}
 		}
		
		int strPopulationSize =0;
		Date uploadTemplateDate = new Date(System.currentTimeMillis()); 
		String strMapSelectedOnTheUI = GDMSModel.getGDMSModel().getMapSelected();
		String strMarkerForMap = GDMSModel.getGDMSModel().getMarkerForMap();
		
		
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
		

		
		
		parentData= new ArrayList<Map<String, String>>();
		populationData=new ArrayList<Map<String, String>>();
		
		//System.out.println("listOfDataRowsFromDataTable:"+listOfDataRowsFromDataTable);
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){			
			Map<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);	
			//System.out.println(hashMapOfDataRow.get(UploadField.SNo.toString()));
			int strSNo = Integer.parseInt(hashMapOfDataRow.get(UploadField.SNo.toString()));			
			if(strSNo<=2){
				parentData.add(hashMapOfDataRow);
			}else{
				populationData.add(hashMapOfDataRow);
			}
			
		}
		/*System.out.println("$$$$$$$$$$$   :"+parentData);
		System.out.println("%%%%%%%%%%%  :"+populationData);*/
		/*parentDataRow=new Map[2];		
		for(int p1=0;p1<parentData.size();p1++){
			Map<String, String> hashMapOfDataRowP = parentData.get(p1);	
			//System.out.println(".......:"+hashMapOfDataRowP.get("Line"));
			String strLine = hashMapOfDataRowP.get(UploadField.Line.toString());			
			hashMapOfDataRowP.put("GID", hashMapOfEntryIDandGID.get(strLine).toString());
			hashMapOfDataRowP.put("Line", hashMapOfEntryIDandGName.get(strLine).toString());
			parentDataRow[p1]=hashMapOfDataRowP;
			//parentDatahashMapOfDataRow.add(hashMapOfDataRow);
		}
		//System.out.println("/./././././parentDatahashMapOfDataRow:"+parentDatahashMapOfDataRow);
		populationDataRow=new Map[populationData.size()];
		for(int p2=0;p2<populationData.size();p2++){
			String strGenotype="";
			Map<String, String> hashMapOfDataRowPop = populationData.get(p2);	
			String strLine = hashMapOfDataRowPop.get(UploadField.Line.toString());
			if(hmOfDuplicateGermNames.containsKey(strLine))
				strGenotype=hmOfDuplicateGermNames.get(strLine);
			else
				strGenotype=strLine;			
			
			hashMapOfDataRowPop.put("GID", hashMapOfEntryIDandGID.get(strGenotype).toString());
			hashMapOfDataRowPop.put("Line", hashMapOfEntryIDandGName.get(strGenotype).toString());
			//System.out.println("hashMapOfDataRowPop:"+hashMapOfDataRowPop);
			populationDataRow[p2]=hashMapOfDataRowPop;
			//populationDatahashMapOfDataRow.add(hashMapOfDataRowPop);
		}
		*/
	
		//System.out.println("///////////populationDataRow:"+populationDataRow);
		
		//System.out.println("populationDatahashMapOfDataRow:"+populationDatahashMapOfDataRow);
		
		
		//Assigning the User Id value based on the Principle investigator's value in the Mapping_Source sheet
		Integer iUserId = 0;		
		try {
			
			iUserId=GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId();
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
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
		 
		
		Integer iMapId = 0;
		
		////System.out.println("strMapSelectedOnTheUI:"+strMapSelectedOnTheUI);
		
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
		
		

		String strMarkerTypeSelected = GDMSModel.getGDMSModel().getMarkerForMap(); //SSR/SNP/DArT 
		String mappingType = GDMSModel.getGDMSModel().getMappingType();
			
		strMappingType="allelic";
		
		String gids1="";
		int gidsCount=0;
		String marker="";
		String marker_type="";
		System.out.println("2.. listOfDataRowsFromDataTable:"+listOfDataRowsFromDataTable);
			ArrayList<String> listOfMarkerNamesFromSourceTable = new ArrayList<String>();
			for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){
				HashMap<String,String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
				Iterator<String> iterator = hashMapOfDataRow.keySet().iterator();
				while(iterator.hasNext()){
					String strMarkerNameFromSourceTable = iterator.next();
					/*if (false == (strMarkerNameFromSourceTable.equals(UploadField.GID.toString()) ||
	                    strMarkerNameFromSourceTable.equals(UploadField.Line.toString()))){*/
					if (false == strMarkerNameFromSourceTable.equals(UploadField.Line.toString())){
						if( (false ==  listOfMarkerNamesFromSourceTable.contains(strMarkerNameFromSourceTable))&&(strMarkerNameFromSourceTable != "SNo")&&(strMarkerNameFromSourceTable != "Alias")&&(strMarkerNameFromSourceTable != "Line")&&(strMarkerNameFromSourceTable != "GID")){
							
							listOfMarkerNamesFromSourceTable.add(strMarkerNameFromSourceTable);
							markersList.add(strMarkerNameFromSourceTable);
							marker = marker +"'"+ strMarkerNameFromSourceTable+"',";
						}
					}
				}
			}
			//System.out.println(",,,,,,,,,,,,,,,  :"+markersList);
			
			/** retrieving maximum marker id from 'marker' table of database **/
			//int maxMarkerId=uptMId.getMaxIdValue("marker_id","gdms_marker",session);
			
			HashMap<String, Object> markersMap = new HashMap<String, Object>();	
			markersForQuery=marker.substring(0, marker.length()-1);
			
			String strQuerry="select distinct marker_id, marker_name from gdms_marker where Lower(marker_name) in ("+markersForQuery.toLowerCase()+")";
			
			sessionC=centralSession.getSessionFactory().openSession();			
			SQLQuery queryC=sessionC.createSQLQuery(strQuerry);	
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
					

			sessionL=localSession.getSessionFactory().openSession();			
			SQLQuery queryL=sessionL.createSQLQuery(strQuerry);		
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
			
			//System.out.println("listOfGNames:"+listOfGNames);
			//System.out.println("listEntries:"+listEntries);
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
			ArrayList<Integer> pGidsList=new ArrayList<Integer>();
			
			marker_type=strMarkerTypeSelected;
				String parentGids=strParentAGID+","+strParentBGID;
				
				if(!(pGidsList.contains(Integer.parseInt(strParentAGID.toString())))){
					pGidsList.add(Integer.parseInt(strParentAGID.toString()));					
				}
				if(!(pGidsList.contains(Integer.parseInt(strParentBGID.toString())))){
					pGidsList.add(Integer.parseInt(strParentBGID.toString()));					
				}
				////System.out.println("**************:"+pGidsList);
				SortedMap mapP = new TreeMap();
	            List lstgermpNameP = new ArrayList();
	            //manager = factory.getGermplasmDataManager();
				//List<Name> names = null;
				
				ArrayList gidsDBList = new ArrayList();
				ArrayList gNamesDBListP = new ArrayList();
				hashMap.clear();
				
				int parentAGid=Integer.parseInt(sheetSource.getCell(1,8).getContents().trim());
				String parentA=sheetSource.getCell(1,9).getContents().trim();
				
				int parentBGid=Integer.parseInt(sheetSource.getCell(1,10).getContents().trim());
				String parentB=sheetSource.getCell(1,11).getContents().trim();
				/*System.out.println("parentAGid:"+parentAGid+"  parentA:"+parentA);
				System.out.println("parentBGid:"+parentBGid+"  parentB:"+parentB);*/
				try{
					Name namesPA = null;					
					namesPA=manager.getNameByGIDAndNval(parentAGid, parentA, GetGermplasmByNameModes.STANDARDIZED);
					if(namesPA==null){
						namesPA=manager.getNameByGIDAndNval(parentAGid, parentA, GetGermplasmByNameModes.NORMAL);
					}					
					//System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^:"+namesPA);
					if(namesPA == null){
						ErrMsg = "Please verify the Parent A provided in the source sheet\t ";
			        	   throw new GDMSException(ErrMsg);	
					}else
						parentA_nid=namesPA.getNid();
					
					//namesPA.get
				} catch (MiddlewareQueryException e) {
					//throw new GDMSException(e.getMessage());
					ErrMsg = "Please verify the Parent A provided in the source sheet\t ";
		        	   throw new GDMSException(ErrMsg);	
				}
				try{
					Name namesPB = null;
					namesPB=manager.getNameByGIDAndNval(parentBGid, parentB, GetGermplasmByNameModes.STANDARDIZED);
					if(namesPB==null){
						namesPB=manager.getNameByGIDAndNval(parentBGid, parentB, GetGermplasmByNameModes.NORMAL);
					}
					//System.out.println("&&&&&&&&&&&&&&:"+namesPB);
					if(namesPB == null){
						ErrMsg = "Please verify the Parent B provided in the source sheet\t ";
			        	   throw new GDMSException(ErrMsg);	
					}else
						parentB_nid=namesPB.getNid();
				
				} catch (MiddlewareQueryException e) {
					//throw new GDMSException(e.getMessage());
					ErrMsg = "Please verify the Parent B provided in the source sheet\t ";
		        	   throw new GDMSException(ErrMsg);	
				}
				
				parentGids=sheetSource.getCell(1,8).getContents().trim()+","+sheetSource.getCell(1,10).getContents().trim();
				gidsForQuery = "";
				GIDsMap = new HashMap<String, Integer>();
				gNames="";
				gidsAList=new ArrayList();
				gidNamesList=new ArrayList();
				NamesList=new ArrayList();
				//System.out.println("rowCount=:"+rowCount);
				for(int r=3;r<rowCount;r++){	
					//gidsForQuery = gidsForQuery + sheetDataList.getCell(1,r).getContents().trim()+",";
					
					gNames=gNames+"'"+sheetDataList.getCell(1,r).getContents().trim()+"',";					
					if(!NamesList.contains(sheetDataList.getCell(1,r).getContents().trim()))
						NamesList.add(sheetDataList.getCell(1,r).getContents().trim());
					
				}
				
				/** arranging gid's with respect to germplasm name in order to insert into allele_values table */
				//if(gidsCount==gnCount){			
					
		            //gidsForQuery=gidsForQuery.substring(0, gidsForQuery.length()-1);
		           
		            SortedMap map = new TreeMap();
		            //List lstgermpName = new ArrayList();
		            manager = factory.getGermplasmDataManager();
					//List<Name> names = null;
					mapN = new TreeMap();
					////System.out.println(",,,,,,,,,,,,,,,,,gNames="+gNames);
					
					gidL=new ArrayList<Integer>();
					
					
		           gidsDBList = new ArrayList<Integer>();
					ArrayList<String> gNamesDBList = new ArrayList<String>();
					hashMap.clear();
					//System.out.println("NamesList*******************:"+NamesList);
					try{
						
						List<GermplasmNameDetails> germplasmList = manager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.NORMAL);				
						for (GermplasmNameDetails g : germplasmList) {				
				        	if(!(gidsDBList.contains(g.getNameId()))){
				        		gidsDBList.add(g.getNameId());
				        		gNamesDBList.add(g.getNVal().toLowerCase());
				        		addValues(g.getNVal(), g.getGermplasmId());					        		
				        	}	
				        	
				        	if(!gidL.contains(g.getGermplasmId()))
				            	gidL.add(g.getGermplasmId());
				            mapN.put(g.getGermplasmId(), g.getNameId());
				        	
				        }
						
						if(gNamesDBList.size()!=NamesList.size()){
							germplasmList = manager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.STANDARDIZED);
							for (GermplasmNameDetails g : germplasmList) {
								if(!(gidsDBList.contains(g.getNameId()))){
					        		gidsDBList.add(g.getNameId());
					        		gNamesDBList.add(g.getNVal().toLowerCase());
					        		addValues(g.getNVal(), g.getGermplasmId());					        		
					        	}
								if(!gidL.contains(g.getGermplasmId()))
					            	gidL.add(g.getGermplasmId());
					            mapN.put(g.getGermplasmId(), g.getNameId());
					        }
						}					
						if(gNamesDBList.size()!=NamesList.size()){
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
								
								mapN.put(g.getGermplasmId(), g.getNameId());
					            GIDsMap.put(g.getNVal().toLowerCase(), g.getGermplasmId());						
					        }
						}
					} catch (MiddlewareQueryException e) {
						throw new GDMSException(e.getMessage());
					}
		         
		           if(gNamesDBList.size()==0){
		        	   alertGID="yes";
		        	   size=0;
		           }
		          
		           int gidToCompare=0;
		           String gNameToCompare="";
		           String gNameFromMap="";
		          
		          /* UploadInput input= new UploadInput();
		           try{
		        	  input = ConformityInputTransformer.transformInput(parentDataRow, populationDataRow);
		        	  // System.out.println("%%%%%%%%%%%%   :"+input.isParentInputAvailable());
		        	  
		           }catch(ConformityException ce){
		        	   ce.printStackTrace();
		           }
		           try {
		        	   Map<Integer, Map<String, String>> output = conformityTestingService.testConformity(input);
		               //System.out.println("Output of testConformity.....:"+output);
		           }catch(MiddlewareQueryException m){
		        	   m.printStackTrace();
		           }  catch(ConformityException ce){
		        	   ce.printStackTrace();
		           }*/
			   	  
			   	   //System.out.println("populationDatahashMapOfDataRow:"+populationDatahashMapOfDataRow);
			   	   String strLine = "";
			   	   int  entryId=1; 
			   	   
				
				List datasetIdsL=new ArrayList();
				List datasetIdsC=new ArrayList();
				List datasetIdsList=new ArrayList();
								
				try{
					//genoManager.getGdmsAccMetadatasetByGid(pGidsList, 0, (int)genoManager.countGdmsAccMetadatasetByGid(pGidsList));
					
					//System.out.println("pGidsList:"+pGidsList);
					//genoManager.getGDMS
					List<AccMetadataSet> dataset_Ids=genoManager.getGdmsAccMetadatasetByGid(pGidsList, 0, (int)genoManager.countGdmsAccMetadatasetByGid(pGidsList));
					//List<AccMetadataSet> dataset_Ids=genoManager.getGdmsAccMetadatasetByGid(pGidsList, 0, 2);
					for (AccMetadataSet name : dataset_Ids){
						datasetIdsList.add(name.getDatasetId());
					}
				} catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				
				
				if(datasetIdsList.size()==0)
					exists="no";
				HashMap<Integer, String> hashMapMarkerIDName=new HashMap<Integer, String>();
				try{
					List<Marker> markerIdsC = genoManager.getMarkersByMarkerNames(markersList, 0, markersList.size(),Database.CENTRAL);
					List<Marker> markerIdsL = genoManager.getMarkersByMarkerNames(markersList, 0, markersList.size(),Database.LOCAL);
					
					List<Integer> markerIdsList=new ArrayList();
					
					if(markerIdsC.size()>0){
						for(Marker markersC: markerIdsC){
							if(!(markerIdsList.contains(markersC.getMarkerId()))){
								markerIdsList.add(markersC.getMarkerId());
								hashMapMarkerIDName.put(markersC.getMarkerId(), markersC.getMarkerName());
							}
						}
					}
					if(markerIdsL.size()>0){
						for(Marker markersL: markerIdsL){
							if(!(markerIdsList.contains(markersL.getMarkerId()))){
								markerIdsList.add(markersL.getMarkerId());
								hashMapMarkerIDName.put(markersL.getMarkerId(), markersL.getMarkerName());
							}
						}
					}
					//System.out.println("$$$$$$$$$$$$$  :"+genoManager.getAllelicValuesByGidsAndMarkerNames(pGidsList, markersList));
					listOfParentsDataRowsFromDB= new HashMap<String, String>();
					List markerFromDB=new ArrayList();
					List markerFromDBFinalList=new ArrayList();
					markerforParentsList=new ArrayList();
					
					hashMapParents.clear();
					
					List<AllelicValueElement> mapParentAllelicValues = genoManager.getAllelicValuesByGidsAndMarkerNames(pGidsList, markersList);
					System.out.println("!!!!!!!!!!!!!!!....from DB:"+mapParentAllelicValues);
					if(mapParentAllelicValues.isEmpty()){
						exists="no";							
						markerforParentsList=markersList;
						//nonExistingMarkers=nonExistingMarkers+markersList.get(m).toString()+",";
					}else{
						for (AllelicValueElement res : mapParentAllelicValues){	
							//System.out.println("#######:"+res);
							exists="yes";
							if(!markerFromDB.contains(res.getMarkerName().toString().toLowerCase()))
								markerFromDB.add(res.getMarkerName().toString().toLowerCase());
							listOfParentsDataRowsFromDB.put(res.getGid()+"!~!"+res.getMarkerName().toLowerCase(), res.getData());
							
							addParentsValues(res.getGid()+"!~!"+res.getMarkerName().toLowerCase(), res.getData());		
							
						}	
						if(markerFromDB.size() < markersList.size()){
							for(int m=0;m<markersList.size();m++){							
								if(!markerFromDB.contains(markersList.get(m).toString().toLowerCase())){
									markerforParentsList.add(markersList.get(m).toString().toLowerCase());
								}							
							}
						}
						
						//System.out.println("!!!!....."+listOfParentsDataRowsFromDB);
						for (int i = 0; i < parentData.size(); i++){	
							Map<String, String> hashMapOfDataRow = parentData.get(i);						
							//System.out.println(i+"....:"+hashMapOfDataRow);
							String strGenotype="";
							
							String strEntry = hashMapOfDataRow.get(UploadField.Line.toString());
							//System.out.println("strEntry:"+strEntry);
							if(hmOfDuplicateGermNames.containsKey(strEntry))
								strGenotype=hmOfDuplicateGermNames.get(strEntry);
							else
								strGenotype=strEntry;
							
							//System.out.println("hashMapParents:"+hashMapParents);
							Integer strGID =hashMapOfEntryIDandGID.get(strGenotype);
							//Integer strGID =hashMapOfGNameandGID.get(strGenotype);
							for (int j = 0; j < listOfMarkerNamesFromSourceTable.size(); j++) {	
								//System.out.println(j+",,,,,,,:"+strGenotype+"  , gid=:"+strGID+"     "+listOfMarkerNamesFromSourceTable.get(j)+"    "+hashMapOfDataRow.get(listOfMarkerNamesFromSourceTable.get(j)));
								String strCharValue=hashMapOfDataRow.get(listOfMarkerNamesFromSourceTable.get(j));					
								String strMarkerName = listOfMarkerNamesFromSourceTable.get(j);			
								
								//System.out.println(strGID+"!~!"+strMarkerName+"="+strCharValue+"   ,"+listOfParentsDataRowsFromDB.get(strGID+"!~!"+strMarkerName.toLowerCase()));
								
								String str1=listOfParentsDataRowsFromDB.get(strGID+"!~!"+strMarkerName.toLowerCase());
								//System.out.println("^^^^^ :"+hashMapParents.get(strGID+"!~!"+strMarkerName.toLowerCase()));
								
								/*if(str1.contains("/")) System.out.println("***************************   STR1   ************************");
								if(strCharValue.contains("/")) System.out.println("$$$$$$$$$$$$$$$$$$$   STRCHARVALUE   $$$$$$$$$$$$$$$$$$$$$$$$$$$$");*/
								
								//System.out.println(str1+"   "+strCharValue);
								String strCV="";
								String str11="";
								String key=strGID+"!~!"+strMarkerName.toLowerCase();
								if(hashMapParents.containsKey(key)){
									//String str1=hashMapParents.get(strGID+"!~!"+strMarkerName.toLowerCase());
									/*if((strCharValue.contains("/"))&&()){
										String[] strCHV=strCharValue.split("/");
										String strCHV1=strCHV[0];
										for(int v=1;v<strCHV.length;v++){
											String strCHV2=strCHV[v];
											if(!strCHV2.equals(strCHV1)){
												strCV=strCV+strCHV1+"/"+strCHV2;
											}										
										}
									}									
									*/
									
									if(!(hashMapParents.get(strGID+"!~!"+strMarkerName.toLowerCase()).contains(strCharValue))){
									//if(!str1.equals(strCharValue)){
										 ErrMsg = "Parents Information for the following markers doesnot match with the value : "+strMarkerName;
										 throw new GDMSException(ErrMsg);
										 //return;
									}
								}else{
									exists="no";							
									markerforParentsList.add(strMarkerName);
								}
								
							}
						}
						
						
					}
					
					nonExistingMarkers="";
					
					//System.out.println("markerforParentsList doesnot exist :"+markerforParentsList);
					//System.out.println("%%%%%%%%%%%%%:"+listOfParentsDataRowsFromDB);
				} catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				
				//System.out.println("Parents Info exists="+exists);
				//if(!markerforParentsList.isEmpty()){
					//if(exists.equalsIgnoreCase("no")){
					String strDLParentA=hashMapOfEntryIDandGName.get(sheetDataList.getCell(1,1).getContents().trim().toString());
					String strDLParentB=hashMapOfEntryIDandGName.get(sheetDataList.getCell(1,2).getContents().trim().toString());
						if((!(strDLParentA.equals(strParentA)))&&(!(strDLParentB.equals(strParentB)))){
							 String strRowNumber1 = String.valueOf(sheetDataList.getCell(1, 1).getRow()+1);	
							 String strRowNumber2 = String.valueOf(sheetDataList.getCell(1, 2).getRow()+1);	
							 ErrMsg = "Parents Information for the following markers doesnot exist : "+nonExistingMarkers.substring(0, nonExistingMarkers.length()-1)+" \nPlease provide Parents Information first followed by population in Mapping_DataList sheet.\n  The row position is "+strRowNumber1+" & "+strRowNumber2;
							 throw new GDMSException(ErrMsg);
						}
						
					//}
				//}
			//}
			
			
			/* List lstMarkers = new ArrayList();
			String markersForQuery="";*/
			
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
				ErrMsg = "Dataset Name value exceeds max char size.";
				throw new GDMSException(ErrMsg);
			}
			
			//String purposeOfStudy=sheetSource.getCell(1,14).getContents().trim();
			String purposeOfStudy=hmOfSourceFieldsAndValues.get(UploadField.PurposeOfTheStudy.toString());
			
			/*scoringScheme=sheetSource.getCell(1,15).getContents().trim();
			missingData=sheetSource.getCell(1,16).getContents().trim();	*/			
			boolean dFormat=isValidDate(sheetSource.getCell(1,17).getContents().trim());
			if(dFormat==false){
				ErrMsg = "Creation Date should be in yyyy-mm-dd format";
				throw new GDMSException(ErrMsg);
			}else{
				 uploadTemplateDate = uploadTemplateDate;
			}
			
			 tx=sessionL.beginTransaction();
			 int iACId = 0;
			 
		        if(marker_type.equalsIgnoreCase("snp")){
					long lastIdMPId=0;
					try{
						lastIdMPId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_CHAR_VALUES);
					}catch (MiddlewareQueryException e) {
						throw new GDMSException(e.getMessage());
					}
					int maxCHid=(int)lastIdMPId;
					iACId=maxCHid;
		        }else if((marker_type.equalsIgnoreCase("ssr"))||(marker_type.equalsIgnoreCase("DArT"))){
					
					long lastIdMPId=0;
					try{
						lastIdMPId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_ALLELE_VALUES);
					}catch (MiddlewareQueryException e) {
						throw new GDMSException(e.getMessage());
					}
					int maxCHid=(int)lastIdMPId;
					iACId=maxCHid;
		        }
		        iACId=iACId-1;
		        long lastIdMPId=0;
				try{
					lastIdMPId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_MAPPING_POP_VAUES);
				}catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				int maxMPid=(int)lastIdMPId;
				iMpId=maxMPid-1;
			
				finalList =new ArrayList();
								
		       /* for(int a=0;a<gidsAList.size();a++){
		        	int gid1=Integer.parseInt(gidsAList.get(a).toString());
		        	if(gidL.contains(gid1)){
		        		finalList.add(gid1+"~!~"+mapN.get(gid1));	
		        	}
		        }*/
		        for(int a=0;a<listOfGNames.size();a++){
		        	String strGenotype=listOfGNames.get(a).toString();
					Integer strGID =hashMapOfEntryIDandGID.get(strGenotype);
		        	//String 
		        	//int gid1=Integer.parseInt(gidsAList.get(a).toString());
		        	//if(gidL.contains(gid1)){
		        		finalList.add(strGID+"~!~"+mapN.get(strGID));	
		        	//}
		        }
				
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
			sessionL.save(dataset);
			
			datasetUser = new GenotypeUsersBean();
			datasetUser.setDataset_id(iDatasetId);
			datasetUser.setUser_id(iUserId);
			sessionL.save(datasetUser);

			////System.out.println("strMapping_Type:"+strMapping_Type);
			int iPopulationSize = strPopulationSize;
			mappingPop = new MappingPopulationBean();
			mappingPop.setDataset_id(iDatasetId);
			mappingPop.setMapping_type(strMappingType);
			mappingPop.setParent_a_nid(parentA_nid);
			mappingPop.setParent_b_nid(parentB_nid);
			mappingPop.setPopulation_size(iPopulationSize);
			mappingPop.setPopulation_type(strPopulationType);
			mappingPop.setMapdata_desc(strMapDataDescription);
			mappingPop.setScoring_scheme(strScoringScheme);
			mappingPop.setMap_id(iMapId);
			sessionL.save(mappingPop);	
			
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
    		int  accSampleId=1;
	        //System.out.println("finalList="+finalList);
	        int r=1;
	        int strLine1=0;
			HashMap<Integer, Integer> strMap=new HashMap<Integer, Integer>();
			
	        for(int i=0;i<finalList.size();i++){	
            	String[] strList=finalList.get(i).toString().split("~!~");
            	
            	Integer iGId = Integer.parseInt(strList[0].toString());
            	int nid=Integer.parseInt(strList[1].toString());
            	
            	if(strLine1!=iGId)
            		accSampleId=1;
            	
            	if(strMap.containsKey(iGId))
            		accSampleId= Integer.parseInt(strMap.get(iGId).toString())+1;
            	
            	strMap.put(iGId, accSampleId);
				strLine1=iGId;
				
				
            	/*if(previousAccGID.contains(iGId)){//.equals(previousGID)){
    				//accSampleId=2;
            		accSampleId=accSampleId+1;
    			}else
    				accSampleId=1;*/
            	
            	//System.out.println("GID="+iGId+"   "+accSampleId);
            	
            	AccessionMetaDataBean amdb=new AccessionMetaDataBean();					
				//******************   GermplasmTemp   *********************//*	
				amdb.setAccMetadatasetId(intMaxAccId);
				amdb.setDatasetId(iDatasetId);
				amdb.setGid(iGId);
				amdb.setNid(nid);
				amdb.setAccSampleId(accSampleId);
				if(!previousAccGID.contains(iGId))
	        		previousAccGID.add(iGId);
				sessionL.save(amdb);
				
				if (i % 1 == 0){
					sessionL.flush();
					sessionL.clear();
				}
				intMaxAccId--;
            }
            
            ArrayList mids=new ArrayList();
            
            HashMap<String, Object> finalHashMapMarkerAndIDs = new HashMap<String, Object>();
                      
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
					
					sessionL.save(mib);
					if (f % 1 == 0){
						sessionL.flush();
						sessionL.clear();
					}
					
				}
				
				
			}
	      /* System.out.println("hashMapOfEntryIDandGID:"+hashMapOfEntryIDandGID);
	       System.out.println("hashMapOfEntryIDandGName:"+hashMapOfEntryIDandGName);*/
	       /*System.out.println("............listOfDataRowsFromDataTable:"+listOfDataRowsFromDataTable);
	       System.out.println("parentData:"+parentData);
	       System.out.println("populationData:"+populationData);*/
			/*parentDatahashMapOfDataRow= new ArrayList<Map<String, String>>();
			populationDatahashMapOfDataRow=new ArrayList<Map<String, String>>();			
			for(int p1=0;p1<parentData.size();p1++){
				Map<String, String> hashMapOfDataRow = parentData.get(p1);	
				String strL = hashMapOfDataRow.get(UploadField.Line.toString());
				System.out.println(",,,,,,,strL:"+strL);
				
				hashMapOfDataRow.put("GID", hashMapOfEntryIDandGID.get(strL).toString());
				hashMapOfDataRow.put("Line", hashMapOfEntryIDandGName.get(strL).toString());
				
				parentDatahashMapOfDataRow.add(hashMapOfDataRow);
			}
			//System.out.println("/./././././parentDatahashMapOfDataRow:"+parentDatahashMapOfDataRow);
			//populationDataRow=new Map[populationData.size()];
			for(int p2=0;p2<populationData.size();p2++){
				String strGenotype="";
				Map<String, String> hashMapOfDataRowPop = populationData.get(p2);	
				String strL = hashMapOfDataRowPop.get(UploadField.Line.toString());
				if(hmOfDuplicateGermNames.containsKey(strL))
					strGenotype=hmOfDuplicateGermNames.get(strL);
				else
					strGenotype=strLine;			
				
				hashMapOfDataRowPop.put("GID", hashMapOfEntryIDandGID.get(strGenotype).toString());
				hashMapOfDataRowPop.put("Line", hashMapOfEntryIDandGName.get(strGenotype).toString());
				//System.out.println("hashMapOfDataRowPop:"+hashMapOfDataRowPop);
				
				populationDatahashMapOfDataRow.add(hashMapOfDataRowPop);
			}
			
			
			
			//String markersList="";
			//System.out.println("finalHashMapMarkerAndIDs=:"+finalHashMapMarkerAndIDs);
			for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){			
				HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);	
				//System.out.println("^^^^^^^^^^^^^  :"+hashMapOfDataRow);
			}*/
            ArrayList gids=new ArrayList();
            String gidsList="";
            int rows=0;
            int cols=0;
            if(strMappingType.equalsIgnoreCase("allelic")){
            	rows=3;
            }else{
            	rows=1;
            }
            for (int l=rows;l<rowCount;l++){				
				gids.add(sheetDataList.getCell(1,l).getContents().trim());		
				gidsList = gidsList +"'"+ sheetDataList.getCell(1,l).getContents().trim().toString()+"',";
			}
            
            String strCharValue = "";
            String charData="";
            //listOfMPABHDataRows = new ArrayList<MappingABHRow>();
    		listOfMPSSRDataRows = new ArrayList<MappingAllelicSSRDArTRow>();
    		listOfMPSNPDataRows = new ArrayList<MappingAllelicSNPRow>();
			//if(strMappingType.equalsIgnoreCase("allelic")){
    		//if(exists.equalsIgnoreCase("no")){
    		
    		System.out.println("listOfParentsDataRowsFromDB:"+listOfParentsDataRowsFromDB);
    		
    		if(!markerforParentsList.isEmpty()){
    			Integer accSampleID=1;
    			Integer markerSampleId=1;
				if(marker_type.equalsIgnoreCase("snp")){
					//System.out.println("parentDatahashMapOfDataRow for inserting:"+parentDatahashMapOfDataRow);
					for (int i = 0; i < parentData.size(); i++){			
						Map<String, String> hashMapOfDataRow = parentData.get(i);						
						//String strGID = hashMapOfDataRow.get(UploadField.GID.toString());
						String strGenotype="";
						
						String strEntry = hashMapOfDataRow.get(UploadField.Line.toString());
						//System.out.println("strEntry:"+strEntry);
						//System.out.println(hmOfDuplicateGermNames+".containsKey("+strEntry);
						if(hmOfDuplicateGermNames.containsKey(strEntry))
							strGenotype=hmOfDuplicateGermNames.get(strEntry);
						else
							strGenotype=strEntry;
						
						//System.out.println("strGenotype:"+strGenotype);
						Integer strGID =hashMapOfEntryIDandGID.get(strGenotype);
						//Integer strGID =hashMapOfGNameandGID.get(strGenotype);
						//String strGenotype = hashMapOfDataRow.get(UploadField.Genotype.toString());
						//System.out.println(listOfMarkerNamesFromSourceTable+"   888888888888:"+hashMapOfDataRow);
						for (int j = 0; j < listOfMarkerNamesFromSourceTable.size(); j++) {	
							//System.out.println(",,,,,:"+listOfMarkerNamesFromSourceTable.get(j));
							strCharValue=hashMapOfDataRow.get(listOfMarkerNamesFromSourceTable.get(j));					
							String strMarkerName = listOfMarkerNamesFromSourceTable.get(j);							
							if(markerforParentsList.contains(strMarkerName.toLowerCase())){
								CharArrayBean charValues=new CharArrayBean();
								CharArrayCompositeKey cack = new CharArrayCompositeKey();
								
								//**************** writing to char_values tables........
								cack.setDataset_id(iDatasetId);
								cack.setAc_id(iACId);
								charValues.setComKey(cack);
								charValues.setGid(strGID);
								
								if(strCharValue.length()>2){
									String charStr=strCharValue;
									//System.out.println("charStr:"+charStr);
									if(charStr.contains(":")){
										String str1="";
										String str2="";
										//String charStr=str.get(s);
										str1=charStr.substring(0, charStr.length()-2);
										str2=charStr.substring(2, charStr.length());
										charData=str1+"/"+str2;
									}else if(charStr.contains("/")){
										charData=charStr;
									}else{										
										throw new GDMSException("Heterozygote data representation should be either : or /");										
									}
									
								}else if(strCharValue.length()==2){
									String str1="";
									String str2="";
									String charStr=strCharValue;
									str1=charStr.substring(0, charStr.length()-1);
									str2=charStr.substring(1);
									charData=str1+"/"+str2;
									//System.out.println(".....:"+str.get(s).substring(1));
								}else if(strCharValue.length()==1){
									if(strCharValue.equalsIgnoreCase("A")){
										charData="A/A";	
									}else if(strCharValue.equalsIgnoreCase("C")){	
										charData="C/C";
									}else if(strCharValue.equalsIgnoreCase("G")){
										charData="G/G";
									}else if(strCharValue.equalsIgnoreCase("T")){
										charData="T/T";
									}else{
										charData=strCharValue;
									}							
								}
								
								
								charValues.setChar_value(charData);
								charValues.setMarker_id(Integer.parseInt(finalHashMapMarkerAndIDs.get(strMarkerName.toString().toLowerCase()).toString()));
								charValues.setAccSampleID(accSampleID);
								charValues.setMarkerSampleId(markerSampleId);
								sessionL.save(charValues);
								
								if (j % 1 == 0){
									sessionL.flush();
									sessionL.clear();
								}
								iACId--;
							}
						}
						
						
					}
					
				}else if(marker_type.equalsIgnoreCase("ssr")){
					Integer intAnID = null;					
					//System.out.println("..........................SSR..........................");
					for (int i = 0; i < parentData.size(); i++){	
						Map<String, String> hashMapOfDataRow = parentData.get(i);						
						//String strGID = hashMapOfDataRow.get(UploadField.GID.toString());
						//mcount=0;
						//iACId--;
						String strGenotype="";
						
						String strEntry = hashMapOfDataRow.get(UploadField.Line.toString());
						//System.out.println("strEntry:"+strEntry);
						if(hmOfDuplicateGermNames.containsKey(strEntry))
							strGenotype=hmOfDuplicateGermNames.get(strEntry);
						else
							strGenotype=strEntry;
						
						//System.out.println("strGenotype:"+strGenotype);
						Integer strGID =hashMapOfEntryIDandGID.get(strGenotype);
						//Integer strGID =hashMapOfGNameandGID.get(strGenotype);
						for (int j = 0; j < listOfMarkerNamesFromSourceTable.size(); j++) {	
							strCharValue=hashMapOfDataRow.get(listOfMarkerNamesFromSourceTable.get(j));					
							String strMarkerName = listOfMarkerNamesFromSourceTable.get(j);			
							System.out.println(strGID+"!~!"+strMarkerName+"="+strCharValue+"   ,"+listOfParentsDataRowsFromDB.get(strGID+"!~!"+strMarkerName.toLowerCase()));
							if(markerforParentsList.contains(strMarkerName.toLowerCase())){
								//alleleValues = new AlleleValues();
								alleleValues = new IntArrayBean();
								IntArrayCompositeKey cack = new IntArrayCompositeKey();
								
								//**************** writing to allele_values tables........
								cack.setDataset_id(iDatasetId);
								cack.setAn_id(iACId);
								alleleValues.setComKey(cack);								
								alleleValues.setGid(strGID);
								//alleleValues.setAllele_bin_value(strCharValue);
								alleleValues.setMarker_id(Integer.parseInt(finalHashMapMarkerAndIDs.get(strMarkerName.toString().toLowerCase()).toString()));
								String charStr=strCharValue;
								String charData1="";
								if(strCharValue.length()>5){
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
									
									//charData=charStr+"/"+charStr;
								
								}	
								
								//System.out.println("....parents.........charData:"+charData);
								alleleValues.setAllele_bin_value(charData);
								alleleValues.setAccSampleID(accSampleID);
								alleleValues.setMarkerSampleId(markerSampleId);
								sessionL.save(alleleValues);
								if (j % 1 == 0){
									sessionL.flush();
									sessionL.clear();
								}
								iACId--;
								
							}
						}
						
					}
				}else if(marker_type.equalsIgnoreCase("DArT")){
					Integer intAnID = null;					
					
					for (int i = 0; i < parentData.size(); i++){	
						Map<String, String> hashMapOfDataRow = parentData.get(i);						
						//String strGID = hashMapOfDataRow.get(UploadField.GID.toString());
						//mcount=0;
						//iACId--;
						String strGenotype="";
						
						String strEntry = hashMapOfDataRow.get(UploadField.Line.toString());
						if(hmOfDuplicateGermNames.containsKey(strEntry))
							strGenotype=hmOfDuplicateGermNames.get(strEntry);
						else
							strGenotype=strEntry;
						
						//System.out.println("strGenotype:"+strGenotype);
						Integer strGID =hashMapOfEntryIDandGID.get(strGenotype);
						//Integer strGID =hashMapOfGNameandGID.get(strGenotype);
						for (int j = 0; j < listOfMarkerNamesFromSourceTable.size(); j++) {	
							strCharValue=hashMapOfDataRow.get(listOfMarkerNamesFromSourceTable.get(j));					
							String strMarkerName = listOfMarkerNamesFromSourceTable.get(j);							
							if(markerforParentsList.contains(strMarkerName)){
								//alleleValues = new AlleleValues();
								alleleValues = new IntArrayBean();
								IntArrayCompositeKey cack = new IntArrayCompositeKey();
								
								//**************** writing to allele_values tables........
								cack.setDataset_id(iDatasetId);
								cack.setAn_id(iACId);
								alleleValues.setComKey(cack);								
								alleleValues.setGid(strGID);
								alleleValues.setAllele_bin_value(strCharValue);
								alleleValues.setMarker_id(Integer.parseInt(finalHashMapMarkerAndIDs.get(strMarkerName.toString().toLowerCase()).toString()));
								alleleValues.setAccSampleID(accSampleID);
								alleleValues.setMarkerSampleId(markerSampleId);
								sessionL.save(alleleValues);
								if (j % 1 == 0){
									sessionL.flush();
									sessionL.clear();
								}
								iACId--;
								
							}
						}
						
					}
				}
    		}		
    		
    		
			//}	
			int gi=0;
			//mp_id=mp_id+1;
			//mp_id=mp_id-1;
			String strData1="";
			
			ArrayList<Integer> previousGIDA=new ArrayList<Integer>();
			ArrayList<Integer> previousMIDA=new ArrayList<Integer>();
			//listOfMarkers=new ArrayList();
			int markerId=0;
			Integer iGId =0;
			Integer accSampleIdA=1;
			Integer markerSampleIdA=1;
			 int strLineMP=0;
			 HashMap<Integer, Integer> strMapMP=new HashMap<Integer, Integer>();
				
			for (int i = 0; i < populationData.size(); i++){			
				Map<String, String> hashMapOfDataRow = populationData.get(i);						
				//Integer strGID = Integer.parseInt(hashMapOfDataRow.get(UploadField.GID.toString()).toString());
				String strGenotype="";
				
				String strEntry = hashMapOfDataRow.get(UploadField.Line.toString());
				if(hmOfDuplicateGermNames.containsKey(strEntry))
					strGenotype=hmOfDuplicateGermNames.get(strEntry);
				else
					strGenotype=strEntry;
				
				//System.out.println("strGenotype:"+strGenotype);
				Integer strGID =hashMapOfEntryIDandGID.get(strGenotype);
				//Integer strGID =hashMapOfGNameandGID.get(strGenotype);
				//System.out.println(".....:"+accSampleIdA+"   "+strGID+"   "+strMapMP);
				if(strLineMP!=strGID)
					accSampleIdA=1;
            	
            	if(strMapMP.containsKey(strGID))
            		accSampleIdA= Integer.parseInt(strMapMP.get(strGID).toString())+1;
            	
            	
				/*System.out.println("&&&&&&&&&&&:"+strMapMP);
				System.out.println(strGenotype+"   "+strGID+"   "+accSampleIdA);*/
				
				previousMIDA=new ArrayList<Integer>();
				
				for (int j = 0; j < listOfMarkerNamesFromSourceTable.size(); j++) {					
					strCharValue=hashMapOfDataRow.get(listOfMarkerNamesFromSourceTable.get(j));					
					String strMarkerName = listOfMarkerNamesFromSourceTable.get(j);							
					
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
					
					mappingPopValues.setGid(strGID);
					if(marker_type.equalsIgnoreCase("snp")){
						if(strCharValue.length()>2){
							String charStr=strCharValue;
							if(charStr.contains(":")){
								String str1="";
								String str2="";
								//String charStr=str.get(s);
								str1=charStr.substring(0, charStr.length()-2);
								str2=charStr.substring(2, charStr.length());
								charData=str1+"/"+str2;
							}else if(charStr.contains("/")){
								charData=charStr;
							}else{								
								throw new GDMSException("Heterozygote data representation should be either : or /");
								
							}
							
						}else if(strCharValue.length()==2){
							String str1="";
							String str2="";
							String charStr=strCharValue;
							str1=charStr.substring(0, charStr.length()-1);
							str2=charStr.substring(1);
							charData=str1+"/"+str2;
							//System.out.println(".....:"+str.get(s).substring(1));
						}else if(strCharValue.length()==1){
							if(strCharValue.equalsIgnoreCase("A")){
								charData="A/A";	
							}else if(strCharValue.equalsIgnoreCase("C")){	
								charData="C/C";
							}else if(strCharValue.equalsIgnoreCase("G")){
								charData="G/G";
							}else if(strCharValue.equalsIgnoreCase("T")){
								charData="T/T";
							}else{
								charData=strCharValue;
							}							
						}
					
					}else if(marker_type.equalsIgnoreCase("ssr")){
						String charStr=strCharValue;
						
						if(strCharValue.length()>5){
							if(charStr.contains(":")){
								String str1="";
								String str2="";
								//String charStr=str.get(s);
								str1=charStr.substring(0, charStr.length()-2);
								str2=charStr.substring(2, charStr.length());
								charData=str1+"/"+str2;
							}else if(charStr.contains("/")){
								charData=charStr;
							}else{
								throw new GDMSException("Heterozygote data representation should be either : or /");
							}
						}else{
							if(strCharValue.equals("-")){
								charStr="0";
							}
								charData=charStr+"/"+charStr;
							
						
						}	
					}else{
						String charStr=strCharValue;
						charData=charStr;
					}
					mappingPopValues.setMap_char_value(charData);
					mappingPopValues.setMarker_id(markerId);
					mappingPopValues.setAccSampleID(accSampleIdA);
					mappingPopValues.setMarkerSampleId(markerSampleIdA);
				
					sessionL.save(mappingPopValues);
					
					if (j % 1 == 0){
						sessionL.flush();
						sessionL.clear();
					}
					
					if(!previousMIDA.contains(markerId))
						previousMIDA.add(markerId);
					
					
					iMpId--;
				}
				strMapMP.put(strGID, accSampleIdA);
				strLineMP=strGID;
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
					markerSampleIdA=2;
				}else
					markerSampleIdA=1;
				////System.out.println("gids doesnot Exists    :"+lstgermpName+"   "+gids[l]);
				MarkerMetaDataBean mdb=new MarkerMetaDataBean();					
				//******************   GermplasmTemp   *********************//*
				mdb.setMarkerMetadatasetId(intMaxMarMetaId);
				mdb.setDatasetId(iDatasetId);
				mdb.setMarkerId(mid);
				mdb.setMarkerSampleId(markerSampleIdA);
				if(!previousMIDMMD.contains(mid))
					previousMIDMMD.add(mid);
				sessionL.save(mdb);
				if (m1 % 1 == 0){
                    sessionL.flush();
                    sessionL.clear();
				}			
			}	
			
			
			tx.commit();
			

	}


	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		
	}


	@Override
	public String getDataUploaded() {
		String strDataUploaded = "";
		if (null != arrayOfMappingPopValues && arrayOfMappingPopValues.length > 0){
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
		}
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
	
	private static void addParentsValues(String key, String value){
		ArrayList<String> tempList = null;
		if(hashMapParents.containsKey(key)){
			tempList=hashMapParents.get(key);
			if(tempList == null)
				tempList = new ArrayList<String>();
			tempList.add(value);
		}else{
			tempList = new ArrayList();
			tempList.add(value);
		}
		hashMapParents.put(key,tempList);
	}
	
	

}
