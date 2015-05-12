package org.icrisat.gdms.upload.genotyping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.GetGermplasmByNameModes;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.GermplasmNameDetails;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.SNPDataRow;
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
import com.vaadin.ui.Window.Notification;


public class SNPGenotype  implements  UploadMarker {

	
	private GDMSMain _mainHomePage;
	
	private String strFileLocation;
	private String strPI;
	private String strDatasetName;
	private String strDatasetDescription;
	private String strGenus;
	private String strSpecies;
	private String strMissingData;
	private int iGIDCount;
	private String[] strArrayOfGIDs;
	private int iGCount;
	private String[] strArrayOfGenotypes;
	private ArrayList<String> listOfGIDsAndGNamesFromFile;
	private ArrayList<String> listOfGIDsFromTheFile;
	private ArrayList<String> listOfGNames;
	
	private AccessionMetaDataBean accMetadataSet;
	private MarkerMetaDataBean markerMetadataSet;
	private GenotypeUsersBean datasetUser;
	
	
	private MarkerInfoBean addedMarker;
	private DatasetBean dataset;
	
	
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	private ArrayList<HashMap<String, String>> listOfDataInSourceLines;
	private ArrayList<HashMap<String, String>> listOfDataInDataLines;
	private ArrayList<String> listOfMarkersFromTheSheet = new ArrayList<String>();
	private String strInstitute;
	private String strEmail;
	private String strInchargePerson;
	private String strPurposeOfStudy;
	private String strCreationDate;
	private List<List<String>> listOfGenoData = new ArrayList<List<String>>();
	private HashMap<Integer, String> hashMapOfGIDsAndGNamesFromFile;
	BufferedReader bReader = null;
	private Marker[] arrayOfMarkers;
	
	List<SNPDataRow> listOfSNPDataRows; 
	GenotypicDataManager genoManager;
	GermplasmDataManager manager;
	ArrayList<String> listOfMarkerNamesFromSourceTable = new ArrayList<String>();
	
	String strMarkerType="SNP";
	ManagerFactory factory =null;
	
	String notMatchingDataDB="";
	String alertGN="no";
    String alertGID="no";
    String notMatchingData="";
    int size=0;
    String notMatchingGIDS="";
    int intRMarkerId = 1;
	int intRAccessionId = 1;
	//String str=
	int maxMid=0;
	int mid=0;
	String charData="";
    private Session localSession;
	private Session centralSession;
	
	private Session session;
	private Transaction tx;
	/*
	private Session sessionL;
	private Session sessionC;
       */
	String strErrorMsg ="no";
	ArrayList<String> gNamesList=new ArrayList<String>();
	
	//ArrayList<String> listOfGermplasmNames = new ArrayList<String>();
	//HashMap<Integer, String> hashMapOfGIDandGName = new HashMap<Integer, String>();		
	HashMap<Integer, Integer> hashMapOfGIDsandNIDs = new HashMap<Integer, Integer>();
	List<Marker> listOfMarkersFromDB = null;
	HashMap<String, Integer> hashMapOfMNamesAndMIDs = new HashMap<String, Integer>();
	Name names = null;
	ArrayList gidL=new ArrayList();
	List<Integer> nameIdsByGermplasmIds =new ArrayList();
	
	ArrayList<Integer> previousAccGID;
	
	private HashMap<String, String> hmOfDuplicateGermNames;
	private HashMap<String, String> hmOfDuplicateGNames;
	List gNames;
	List gDupNameList;
	List gDupNameListV;
	private HashMap<Integer, String> hmOfColIndexAndGermplasmName;
	HashMap<String, Integer> hashMapOfEntryIDandGID = new HashMap<String, Integer>();
	
	ArrayList<String> dupGermplasmsList=new ArrayList<String>();
	
	
	boolean dupGermConfirmation;
    
    static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();
	@Override
	public void readExcelFile() throws GDMSException {
		try {			
			String ext=strFileLocation.substring(strFileLocation.lastIndexOf("."));
			if(ext.equals(".txt")){
				bReader = new BufferedReader(new FileReader(strFileLocation));
			}else{
				throw new GDMSException("Please check the file, it should be a tab delimited .txt file");				
			}
		} catch (FileNotFoundException e) {
			throw new GDMSException(e.getMessage());
			//throw new GDMSException("Error Reading DART Genotype Sheet - " + e.getMessage());
		} 
	}
	
	@Override
	public String validateDataInExcelSheet() throws GDMSException {
		//_mainHomePage = theMainHomePage;
		//System.out.println("READING EXCEL FILE DONE...............");
		String strLine = "";
		try {
			
			
			while ((strLine = bReader.readLine()) != null) {
				String[] strArrayOfTokens = strLine.split("\t");	
				int iNumOfTokens = strArrayOfTokens.length;		

				if(strLine.startsWith("Institute")){
					strInstitute = strArrayOfTokens[1];
				}
				if(strLine.startsWith("PI")){
					if(iNumOfTokens == 2) {
						strPI = strArrayOfTokens[1];
					}
					if(iNumOfTokens == 1){
						throw new GDMSException("Please provide the PI ");
					}
					if(iNumOfTokens > 2){ 	
						throw new GDMSException("There are extra tabs at line PI");
					}
				}
				if(strLine.startsWith("Email")){
					strEmail = strArrayOfTokens[1];
				}
				if(strLine.startsWith("Incharge_Person")){
					strInchargePerson = strArrayOfTokens[1];
				}
				if(strLine.startsWith("Dataset_Name")){
					if(iNumOfTokens == 2){
						strDatasetName = strArrayOfTokens[1];
					}else if(iNumOfTokens == 1){
						throw new GDMSException("Please provide the Dataset Name ");
					}else if(iNumOfTokens>2){ 	
						throw new GDMSException("There are extra tabs at line Dataset Name");
					}
				}
				if(strLine.startsWith("Purpose_Of_Study")){
					strPurposeOfStudy = strArrayOfTokens[1];
				}
				if(strLine.startsWith("Dataset_Description")){
					if(iNumOfTokens == 2) {
						strDatasetDescription = strArrayOfTokens[1];		
					} else if(iNumOfTokens == 1){
						throw new GDMSException("Please provide the Description");
					} else if(iNumOfTokens > 2){ 	
						throw new GDMSException("There are extra tabs at line Description");
					}
				}

				if(strLine.startsWith("Genus")){
					if(iNumOfTokens == 2){
						strGenus = strArrayOfTokens[1];	
					} else if(iNumOfTokens == 1){
						throw new GDMSException("Please provide the Genus");
					} else if(iNumOfTokens > 2){ 	
						throw new GDMSException("There are extra tabs at line Genus");
					}
				}	
				if(strLine.startsWith("Species")){
					if(iNumOfTokens == 2) {
						strSpecies = strArrayOfTokens[1];
					} else if(iNumOfTokens == 1){
						throw new GDMSException("Please provide the Species");
					} else if(iNumOfTokens > 2){ 	
						throw new GDMSException("There are extra tabs at line Species");
					}
				}				
				if(strLine.startsWith("Missing_Data")){
					if(iNumOfTokens == 2) {
						strMissingData = strArrayOfTokens[1];
					} else if(iNumOfTokens==1){
						throw new GDMSException("Please provide the Missing_Data");
					} else if(iNumOfTokens > 2){ 	
						throw new GDMSException("There are extra tabs at line Missing_Data");
					}
				}
				if(strLine.startsWith("Creation_Date")){
					strCreationDate = strArrayOfTokens[1];
				}
				////System.out.println("iNumOfTokens=:"+iNumOfTokens+"   "+strArrayOfTokens);
				/*if((strArrayOfTokens[0].startsWith("gid's") && (iNumOfTokens >= 2))){					
					iNumOfTokens = strArrayOfTokens.length;
					iGIDCount = iNumOfTokens;
					for(int g = 1; g < iNumOfTokens; g++){						
						strArrayOfGIDs = strArrayOfTokens;							
					}					
				}*/
				
				//System.out.println("strArrayOfGIDs:"+strArrayOfGIDs);
				
				if((strArrayOfTokens[0].startsWith("Marker\\Genotype")) && (iNumOfTokens >= 2)){	
					iNumOfTokens = strArrayOfTokens.length;
					iGCount = iNumOfTokens;
					for(int g = 1; g < iNumOfTokens; g++){
						strArrayOfGenotypes = strArrayOfTokens;							
					}					
				}
				//if((!(strArrayOfTokens[0].equals("Marker\\Genotype"))) && (!(strArrayOfTokens[0].equals("gid's"))) && (iNumOfTokens > 2)){
				if((!(strArrayOfTokens[0].equals("Marker\\Genotype"))) && (iNumOfTokens > 2)){
					listOfGenoData.add(Arrays.asList(strArrayOfTokens)); 			
					listOfMarkersFromTheSheet.add(strArrayOfTokens[0]);
				}	
			}
			
			
			/*if(iGIDCount < iGCount){
				throw new GDMSException("The number of GIDs is less than the number of Germplasm names provided");
			}else if(iGCount < iGIDCount){
				throw new GDMSException("The number of GIDs is more than the number of Germplasm names provided");
			}*/

			listOfGIDsAndGNamesFromFile = new ArrayList<String>();
			hashMapOfGIDsAndGNamesFromFile = new HashMap<Integer, String>();
			listOfGIDsFromTheFile = new ArrayList<String>();
			listOfGNames = new ArrayList<String>();
			int c=2;
			gNamesList=new ArrayList<String>();
			//int c=2;
			hmOfDuplicateGermNames=new HashMap<String, String>();
			hmOfDuplicateGNames=new HashMap<String, String>();
			gDupNameList=new ArrayList<String>();
			//hmOfColIndexAndGermplasmName = new HashMap<Integer, String>();
			if (null != strArrayOfGenotypes){
				for(int d = 1; d < strArrayOfGenotypes.length; d++){	  
					String gName="";
					
					listOfGNames.add(strArrayOfGenotypes[d]);
					if(gNamesList.contains(strArrayOfGenotypes[d])){						
						gDupNameList.add(strArrayOfGenotypes[d]);						
					}
					gNamesList.add(strArrayOfGenotypes[d]);
				}
			}
			/*System.out.println("READING EXCEL FILE DONE..............."+gNamesList);
			System.out.println("gDupNameList:"+gDupNameList);*/
			//String strGName="";
			int rep=1;
			
			dupGermplasmsList=new ArrayList<String>();
			String strLine1="";
			HashMap<String, Integer> strMap=new HashMap<String, Integer>();
			//System.out.println(gDupNameList+"............gNamesList:"+gNamesList);
			for(int g=0;g<gNamesList.size();g++){
				if(gDupNameList.contains(gNamesList.get(g))){
					//System.out.println("strMap:"+strMap);
					//System.out.println("...:::::...strLine1:"+strLine1);
					if(strLine1.equalsIgnoreCase(gNamesList.get(g).toString()))
						rep=1;
					if(strMap.containsKey(gNamesList.get(g)))
						rep= Integer.parseInt(strMap.get(gNamesList.get(g)).toString())+1;
							
					String strGName=gNamesList.get(g)+" (Sample "+rep+")";
					hmOfDuplicateGNames.put(gNamesList.get(g)+rep, strGName);
					hmOfDuplicateGermNames.put(strGName, gNamesList.get(g));
					dupGermplasmsList.add(strGName);
					//hmOfDataInDataSheet.put(UploadField.Line.toString(), strLine1);
					
					//strList.add(strLineValue);
					strMap.put(gNamesList.get(g), rep);
					strLine1=gNamesList.get(g);
					
				}else{
					rep=1;
					strLine1=gNamesList.get(g);
				}
				//System.out.println("hmOfDuplicateGermNames:"+hmOfDuplicateGermNames);
				
				
			}
			
			if(listOfMarkersFromTheSheet.size()>320 || gNamesList.size()>500){
				//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				strErrorMsg = "Data to be uploaded cannot be displayed in the table. Please click on Upload to upload the data directly";
				
			}
			
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
		return strErrorMsg;
		
	}

	@Override
	public void createObjectsToBeDisplayedOnGUI(GDMSMain theMainHomePage) throws GDMSException {
		
		_mainHomePage = theMainHomePage;

		listOfDataInSourceLines = new ArrayList<HashMap<String,String>>();
		listOfDataInDataLines = new ArrayList<HashMap<String,String>>();
		System.out.println("******************strDatasetName:"+strDatasetName);
		HashMap<String, String> hashMapOfSourceFields = new HashMap<String, String>();
		hashMapOfSourceFields.put(UploadField.Institute.toString(), strInstitute);
		hashMapOfSourceFields.put(UploadField.PI.toString(), strPI);
		hashMapOfSourceFields.put(UploadField.Email.toString(), strEmail);
		hashMapOfSourceFields.put(UploadField.InchargePerson.toString(), strInchargePerson);
		hashMapOfSourceFields.put(UploadField.DatasetName.toString(), strDatasetName);
		hashMapOfSourceFields.put(UploadField.PurposeOfTheStudy.toString(), strPurposeOfStudy);
		hashMapOfSourceFields.put(UploadField.DatasetDescription.toString(), strDatasetDescription);
		hashMapOfSourceFields.put(UploadField.Genus.toString(), strGenus);
		hashMapOfSourceFields.put(UploadField.Species.toString(), strSpecies);
		hashMapOfSourceFields.put(UploadField.MissingData.toString(), strMissingData);
		hashMapOfSourceFields.put(UploadField.CreationDate.toString(), strCreationDate);
		listOfDataInSourceLines.add(hashMapOfSourceFields);
		
		
		System.out.println(".............................listOfDataInSourceLines:"+listOfDataInSourceLines);
		
		/*System.out.println("@@@@@@@@@@@@@@@@@   Creating data List   @@@@@@@@@@@@@@@@:"+gDupNameList);
		System.out.println("hmOfDuplicateGermNames+:"+hmOfDuplicateGermNames);
		*/
		System.out.println(gNamesList.size()+"          "+listOfMarkersFromTheSheet.size());
		/*if(gNamesList.size() > 240 || listOfMarkersFromTheSheet.size() > 240){
			 String strErrorMessage = "Data to be uploaded cannot be displayed in the table." + 
                     " Please click on upload to upload the data directly";
			 _mainHomePage.getMainWindow().getWindow().showNotification(strErrorMessage, Notification.TYPE_ERROR_MESSAGE);
			//return;
		}
		*/
		
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
		
		
	/*	System.out.println("listOfGIDsFromTheFile:"+listOfGIDsFromTheFile);
		System.out.println("listOfMarkersFromTheSheet:"+listOfMarkersFromTheSheet);
		System.out.println("listOfGenoData:"+listOfGenoData);*/
		//System.out.println("hmOfDuplicateGNames:"+hmOfDuplicateGNames);
		int rCount=1;
		String stLine="";
		HashMap<String, Integer> stMap=new HashMap<String, Integer>();
		for (int i = 0; i < listOfGNames.size(); i++){
			
			HashMap<String, String> hashMapOfGenoDataLine = new HashMap<String, String>();
			
			/*String strGID = listOfGIDsFromTheFile.get(i);
			hashMapOfGenoDataLine.put(UploadField.GID.toString(), listOfGIDsFromTheFile.get(i));
			
			String strGName = hashMapOfGIDsAndGNamesFromFile.get(Integer.parseInt(strGID));*/
			String strGName = listOfGNames.get(i);
			/*System.out.println("strGName:"+strGName);
			System.out.println("#####################################:"+rCount);
			*/
			if(gDupNameList.contains(strGName)){
				if(stLine!=strGName)
					rCount=1;
				if(stMap.containsKey(strGName))
					rCount= Integer.parseInt(stMap.get(strGName).toString())+1;
				//System.out.println("@@@@@@@@@@@@@@@@...........:"+strGName+rCount);
				hashMapOfGenoDataLine.put(UploadField.Genotype.toString(), hmOfDuplicateGNames.get(strGName+rCount));
				
				stMap.put(strGName, rCount);
				stLine=strGName;
				rCount++;
			}else{		
				hashMapOfGenoDataLine.put(UploadField.Genotype.toString(), strGName);
				rCount=1;
				stLine=strGName;
				//System.out.println("strGName:"+strGName);
			}	
			//System.out.println("rCount"+rCount);
			for (int j = 0; j < listOfMarkersFromTheSheet.size(); j++){
				String strMarkerName = listOfMarkersFromTheSheet.get(j);
				for (int k = 0; k < listOfGenoData.size(); k++){
					//System.out.println("........:"+listOfGenoData.get(k)+"    "+strMarkerName);
					List<String> list = listOfGenoData.get(k);
					if (list.contains(strMarkerName)){
						String strMName = list.get(0);
						//System.out.println("%%%%%%%%%%%%%%%%  :"+strMName);
						if (strMName.equals(strMarkerName)) {
							String string = list.get(i+1);
							//System.out.println("$$$$$$$$$$$$$$$$$  :"+string);
							hashMapOfGenoDataLine.put(strMarkerName, string);
							//System.out.println("hashMapOfGenoDataLine:"+hashMapOfGenoDataLine);
							break;
						}
					}
				}
			}
			
			listOfDataInDataLines.add(hashMapOfGenoDataLine);
		}	
		System.out.println("listOfDataInDataLines   &&&&&&&&&&&&&&  Created...........");
		
	}



	@Override
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}

	@Override
	public void setDataToBeUploded(ArrayList<HashMap<String, String>> theListOfSourceDataRows, ArrayList<HashMap<String, String>> listOfDataRows,ArrayList<HashMap<String, String>> listOfGIDRows) {
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
		
		/*ArrayList<Integer> listOfGIDFromTable = new ArrayList<Integer>();
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++) {
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
			String strGID = hashMapOfDataRow.get(UploadField.GID.toString());
			listOfGIDFromTable.add(Integer.parseInt(strGID));
		}
		
		if (0 == listOfGIDFromTable.size()){
			throw new GDMSException("Please provide list of GIDs to be uploaded.");
		}
		*/

		/*listOfMarkerNamesFromSourceTable = new ArrayList<String>();
		
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){
		
			HashMap<String,String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
			Iterator<String> iterator = hashMapOfDataRow.keySet().iterator();
			while(iterator.hasNext()){
				String strMarkerNameFromSourceTable = iterator.next();
				if (false == (strMarkerNameFromSourceTable.equals(UploadField.GID.toString()) ||
                    strMarkerNameFromSourceTable.equals(UploadField.Genotype.toString()))){
				if (false == strMarkerNameFromSourceTable.equals(UploadField.Genotype.toString())){
					
					if (false ==  listOfMarkerNamesFromSourceTable.contains(strMarkerNameFromSourceTable)){
						listOfMarkerNamesFromSourceTable.add(strMarkerNameFromSourceTable);
					}
					
				}
			}
		}

		if (0 == listOfMarkerNamesFromSourceTable.size()) {
			throw new GDMSException("Please provide list of MarkerNames to be uploaded.");
		}*/
		
	}

	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {	
		
		System.out.println("******************** objects to be saved");
		
		
		HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
		String strGermplasmSelected = GDMSModel.getGDMSModel().getGermplasmSelected();
		//System.out.println("..>>>>>>>>....:"+strGermplasmSelected);
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
		
			manager = factory.getGermplasmDataManager();
			genoManager=factory.getGenotypicDataManager();
	
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
		
		
		
		String strGenotypeName="";
		ArrayList<Integer> listOfGIDFromTable = new ArrayList<Integer>();
		ArrayList<String> listOfGNamesFromTable = new ArrayList<String>();
		if(! (listOfMarkersFromTheSheet.size()>320 || gNamesList.size()>500)){
			for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++) {
				HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
				/*String strGID = hashMapOfDataRow.get(UploadField.GID.toString());
				if(!(listOfGIDFromTable.contains(strGID))){
					listOfGIDFromTable.add(Integer.parseInt(strGID));*/
					strGenotypeName=hashMapOfDataRow.get(UploadField.Genotype.toString()).trim();
					if(dupGermplasmsList.contains(strGenotypeName)){
						strGenotypeName=hmOfDuplicateGermNames.get(strGenotypeName.toString());
					}
						listOfGNamesFromTable.add(strGenotypeName);
				//}
				//hashMapOfGNameandGID.put(strGenotypeName, Integer.parseInt(strGID));
				
				////System.out.println("germplasm name=:"+hashMapOfDataRow.get(UploadField.Genotype.toString())+"      gid=:"+hashMapOfDataRow.get(UploadField.GID.toString()));
			}
		}else{
			
		}
		String marker="";
		ArrayList<String> listOfMarkerNamesFromSourceTable = new ArrayList<String>();
		ArrayList<String> listOfMarkerNamesInLoweCase = new ArrayList<String>();
		
		ArrayList<String> listOfMarkers = new ArrayList<String>();
		if(! (listOfMarkersFromTheSheet.size()>320 || gNamesList.size()>500)){
			for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){
				HashMap<String,String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
				Iterator<String> iterator = hashMapOfDataRow.keySet().iterator();
				while(iterator.hasNext()){
					String strMarkerNameFromSourceTable = iterator.next();
					/*if (false == (strMarkerNameFromSourceTable.equals(UploadField.GID.toString()) ||
	                    strMarkerNameFromSourceTable.equals(UploadField.Genotype.toString()))){*/
					if (false == strMarkerNameFromSourceTable.equals(UploadField.Genotype.toString())){
						if(strMarkerNameFromSourceTable != "SNo"){
							listOfMarkers.add(strMarkerNameFromSourceTable.toLowerCase());
						}
						if( (false ==  listOfMarkerNamesFromSourceTable.contains(strMarkerNameFromSourceTable))&&(strMarkerNameFromSourceTable != "SNo")&&(strMarkerNameFromSourceTable != "GID")){
							listOfMarkerNamesFromSourceTable.add(strMarkerNameFromSourceTable);
							listOfMarkerNamesInLoweCase.add(strMarkerNameFromSourceTable.toLowerCase());
							marker = marker +"'"+ strMarkerNameFromSourceTable+"',";
						}
					}
				}
			}
		
		}else{
			for(int m=0;m<listOfMarkersFromTheSheet.size();m++){
				listOfMarkerNamesFromSourceTable.add(listOfMarkersFromTheSheet.get(m));
				listOfMarkerNamesInLoweCase.add(listOfMarkersFromTheSheet.get(m).toLowerCase());
				marker=marker +"'"+listOfMarkersFromTheSheet.get(m)+"',";
			}
		}
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
		SQLQuery queryL=localSession.createSQLQuery(strQuerryGL);		
		queryL.addScalar("listid",Hibernate.INTEGER);	  
		
		newListL=queryL.list();
		itListL=newListL.iterator();			
		while(itListL.hasNext()){
			objL=itListL.next();
			if(objL!=null)
				list_id=Integer.parseInt(objL.toString());					
		}
			
			
		if(list_id==0){
			//sessionC=centralSession.getSessionFactory().openSession();			
			SQLQuery queryC=centralSession.createSQLQuery(strQuerryGL);		
			queryC.addScalar("listid",Hibernate.INTEGER);;	
			newListC=queryC.list();			
			itListC=newListC.iterator();			
			while(itListC.hasNext()){
				obj=itListC.next();
				if(obj!=null)		
					list_id=Integer.parseInt(obj.toString());				
			}
				
		
		}	
		
		
		
		newListL=new ArrayList();
		 newListC=new ArrayList();
		//try {	
		obj=null;
		objL=null;
		itListC=null;
		itListL=null;
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
				lstMarkers.add(strMareO[1].toString().toLowerCase());
				markersMap.put(strMareO[1].toString().toLowerCase(), strMareO[0]);
				
			}
		}
				

		//sessionL=localSession.getSessionFactory().openSession();			
		queryL=localSession.createSQLQuery(strQuerry);		
		queryL.addScalar("marker_id",Hibernate.INTEGER);	
		queryL.addScalar("marker_name",Hibernate.STRING);      
		newListL=queryL.list();
		itListL=newListL.iterator();			
		while(itListL.hasNext()){
			objL=itListL.next();
			if(objL!=null)	{			
				Object[] strMareO= (Object[])objL;
				////System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
				if(!lstMarkers.contains(strMareO[1].toString().toLowerCase())){
            		lstMarkers.add(strMareO[1].toString().toLowerCase());	            		
            		markersMap.put(strMareO[1].toString().toLowerCase(), strMareO[0]);	
				}
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
		/*System.out.println("listOfGNames:"+listOfGNames);
		System.out.println("listEntries:"+listEntries);*/
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
		
		
		ArrayList gidsDBList = new ArrayList();
		ArrayList gNamesDBList = new ArrayList();
		hashMap.clear();
		//System.out.println("@@@@@@@@@@:"+listOfGNamesFromTable);
	
		
		//listOfGermplasmNames = new ArrayList<String>();
		//hashMapOfGIDandGName = new HashMap<Integer, String>();		
		hashMapOfGIDsandNIDs = new HashMap<Integer, Integer>();
		listOfMarkersFromDB = null;
		hashMapOfMNamesAndMIDs = new HashMap<String, Integer>();
		names = null;
		gidL=new ArrayList();
		nameIdsByGermplasmIds =new ArrayList();
		//System.out.println("listOfGNamesFromTable:"+listOfGNamesFromTable);
		try{
			List<GermplasmNameDetails> germplasmList = manager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.NORMAL);				
			for (GermplasmNameDetails g : germplasmList) {				
	        	if(!(gidsDBList.contains(g.getNameId()))){
	        		gidsDBList.add(g.getNameId());
	        		gNamesDBList.add(g.getNVal().toLowerCase());
	        		addValues(g.getNVal().toLowerCase(), g.getGermplasmId());					        		
	        	}	
	        	if(!gidL.contains(g.getGermplasmId()))
	            	gidL.add(g.getGermplasmId());
				//listOfGermplasmNames.add(names.getNval());
				hashMapOfGIDsandNIDs.put(g.getGermplasmId(), g.getNameId());
				hashMapOfGNameandGID.put(g.getNVal().toLowerCase(), g.getGermplasmId());
				nameIdsByGermplasmIds.add(g.getNameId());
	        }
			
			if(gNamesDBList.size()!=listOfGNamesFromTable.size()){
				germplasmList = manager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.STANDARDIZED);
				for (GermplasmNameDetails g : germplasmList) {
					if(!(gidsDBList.contains(g.getNameId()))){
		        		gidsDBList.add(g.getNameId());
		        		gNamesDBList.add(g.getNVal().toLowerCase());
		        		addValues(g.getNVal().toLowerCase(), g.getGermplasmId());				        		
		        	}        	
					if(!gidL.contains(g.getGermplasmId()))
		            	gidL.add(g.getGermplasmId());
					//listOfGermplasmNames.add(names.getNval());
					hashMapOfGIDsandNIDs.put(g.getGermplasmId(), g.getNameId());
					hashMapOfGNameandGID.put(g.getNVal().toLowerCase(), g.getGermplasmId());
					nameIdsByGermplasmIds.add(g.getNameId());
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
					hashMapOfGIDsandNIDs.put(g.getGermplasmId(), g.getNameId());
					hashMapOfGNameandGID.put(g.getNVal().toLowerCase(), g.getGermplasmId());
					nameIdsByGermplasmIds.add(g.getNameId());
		        }
			}
			
		} catch (MiddlewareQueryException e1) {
			throw new GDMSException(e1.getMessage());
		}		
		//System.out.println("hashMap:"+hashMap);
		/*if(gNamesDBList.size()>0){
			for(int n=0;n<listOfGNamesFromTable.size();n++){	
				//System.out.println(n+" listOfGNamesFromTable.get(n):"+listOfGNamesFromTable.get(n));
	 		   if(gNamesDBList.contains(listOfGNamesFromTable.get(n).toLowerCase())){
	 			   if(!(hashMap.get(listOfGNamesFromTable.get(n).toString().toLowerCase()).contains(hashMapOfGNameandGID.get(listOfGNamesFromTable.get(n).toString().toLowerCase())))){
	 				   notMatchingData=notMatchingData+listOfGNamesFromTable.get(n)+"   "+hashMapOfGNameandGID.get(listOfGNamesFromTable.get(n).toString())+"\n\t";
	 				   notMatchingDataDB=notMatchingDataDB+listOfGNamesFromTable.get(n)+"="+hashMap.get(listOfGNamesFromTable.get(n))+"\t";
		        		   alertGN="yes";
	 			   }
	 		   }else{
	 			   //System.out.println(n+"..................:"+listOfGNamesFromTable.get(n));
	 			   alertGID="yes";
	     		   size=hashMap.size();
	     		   notMatchingGIDS=notMatchingGIDS+listOfGNamesFromTable.get(n).toString()+", ";
	 		   }
	 	   }
	    }
	    if((alertGN.equals("yes"))&&(alertGID.equals("no"))){
	 	   //String ErrMsg = "GID(s) ["+notMatchingGIDS.substring(0,notMatchingGIDS.length()-1)+"] of Germplasm(s) ["+notMatchingData.substring(0,notMatchingData.length()-1)+"] being assigned to ["+notMatchingDataExists.substring(0,notMatchingDataExists.length()-1)+"] \n Please verify the template ";
	    	throw new GDMSException("Please verify the name(s) provided \t "+notMatchingData+" which do not match the GID(s) present in the database"+notMatchingDataDB);	 	   
	    }
	    if((alertGID.equals("yes"))&&(alertGN.equals("no"))){	        	   
	 	   if(size==0){
	 		   //ErrMsg = "The GIDs provided do not exist in the database. \n Please upload the relevant germplasm information to the GMS ";
	 		  throw new GDMSException("The Germplasm(s) provided do not exist in the database. \n Please import the relevant germplasm list ");
	 	   }else{
	 		  throw new GDMSException("The following Germplasm(s) provided do not exist in the database. \n Please import the relevant germplasm list \n \t"+notMatchingGIDS);
	 		   //ErrMsg = "Please verify the GID/Germplasm(s) provided as some of them do not exist in the database. \n Please upload germplasm information into GMS ";
	 	   } 	   
	    }
		
	    if((alertGID.equals("yes"))&&(alertGN.equals("yes"))){
	    	throw new GDMSException("The following Germplasm(s) provided do not exist in the database. \n Please import the relevant germplasm list \n \t"+notMatchingGIDS+" \n Please verify the name(s) provided "+notMatchingData+" which do not match the GIDS(s) present in the database "+notMatchingDataDB);	 	  
	    }
		
		*/
		
		ArrayList finalList =new ArrayList();
		
		/*for(int a=0;a<listOfGIDFromTable.size();a++){
        	int gid1=Integer.parseInt(listOfGIDFromTable.get(a).toString());
        	if(gidL.contains(gid1)){
        		finalList.add(gid1+"~!~"+hashMapOfGIDsandNIDs.get(gid1));	
        	}
        }*/
		for(int a=0;a<listOfGNames.size();a++){
			String strEntry=listOfGNames.get(a);
			int gid1=hashMapOfEntryIDandGID.get(strEntry);
        	//int gid1=Integer.parseInt(listOfGIDFromTable.get(a).toString());
        	if(gidL.contains(gid1)){
        		finalList.add(gid1+"~!~"+hashMapOfGIDsandNIDs.get(gid1));	
        	}
        }	
			/** 
			 * 20130813
			 */
			if (null == nameIdsByGermplasmIds){
				throw new GDMSException("Error retrieving list of NIDs for given GIDs. Please provide valid GIDs.");
			} 
			
			if (0 == nameIdsByGermplasmIds.size()){
				throw new GDMSException("Name IDs do not exist for given GIDs. Please provide valid GIDs.");
			}
			

		//Integer iDatasetId = 0; //Will be set/overridden by the function
		String strDataType = "char"; 
		Date uploadTemplateDate = new Date(System.currentTimeMillis());
		String strCharValue = "CV";
		String strDatasetType = "SNP";
		String method = null;
		String strScore = null;
		Integer iACId = null;
		String strRemarks = "";
		String strPIFromSourceTable = "";
		String strDatasetNameFromSourceTable = "";
		String strDatasetDescFromSourceTable = "";
		String strGenusFromSourceTable = "";
		String strSpeciesFromSourceTable = "";
		String strMissingDataFromSourceTable ="";
		if(! (listOfMarkersFromTheSheet.size()>320 || gNamesList.size()>500)){
			HashMap<String, String> hashMapOfSourceDataRow = listOfDataRowsFromSourceTable.get(0);
			strPIFromSourceTable = hashMapOfSourceDataRow.get(UploadField.PI.toString());
			strDatasetNameFromSourceTable = hashMapOfSourceDataRow.get(UploadField.DatasetName.toString());
			strDatasetDescFromSourceTable = hashMapOfSourceDataRow.get(UploadField.DatasetDescription.toString());
			strGenusFromSourceTable = hashMapOfSourceDataRow.get(UploadField.Genus.toString());
			strSpeciesFromSourceTable = hashMapOfSourceDataRow.get(UploadField.Species.toString());
			strMissingDataFromSourceTable = hashMapOfSourceDataRow.get(UploadField.MissingData.toString());
		}else{
			strPIFromSourceTable = strPI;
			strDatasetNameFromSourceTable = strDatasetName;
			strDatasetDescFromSourceTable =strDatasetDescription;
			strGenusFromSourceTable = strGenus;
			strSpeciesFromSourceTable = strSpecies;
			strMissingDataFromSourceTable = strMissingData;
		}
		int marker_id=0;
		Database instance = Database.LOCAL;
		
		Integer iUserId = 0;
		
		try {
			
			iUserId=GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId();
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
		try{
			List<DatasetElement> results =genoManager.getDatasetDetailsByDatasetName(strDatasetNameFromSourceTable, Database.CENTRAL);
			if(results.isEmpty()){			
				results =genoManager.getDatasetDetailsByDatasetName(strDatasetNameFromSourceTable, Database.LOCAL);
				if(results.size()>0)
					throw new GDMSException("Dataset Name already exists.");
			}else 
				throw new GDMSException("Dataset Name already exists.");
		
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
	//	System.out.println("strDatasetNameFromSourceTable:"+strDatasetNameFromSourceTable);
		if(strDatasetNameFromSourceTable.length()>30){
			//ErrMsg = "Dataset Name value exceeds max char size.";
			throw new GDMSException("Dataset Name value exceeds max char size.");
		}
		
		int iUploadedMarkerCount = 0;
		
		////System.out.println("listOfDataRowsFromDataTable:"+listOfDataRowsFromDataTable);
		 
		 long datasetLastId = 0;
		 long lastId = 0;
		 
		int iNumOfMarkers = listOfMarkerNamesFromSourceTable.size();
		int iNumOfGIDs = listOfGIDFromTable.size();
		arrayOfMarkers = new Marker[iNumOfMarkers*iNumOfGIDs];
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
					lastIdMPId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_CHAR_VALUES);
				}catch (MiddlewareQueryException e) {
					throw new GDMSException(e.getMessage());
				}
				int maxCHid=(int)lastIdMPId;
				iACId=maxCHid;
		 
		 
		 
		String score="";
		Integer accSampleId=1;
		Integer markerSampleId=1;
		int intMaxAccId=0;
		Object objAcc=null;
		Iterator itList=null;
		List listValues=null;
		Query query1=localSession.createSQLQuery("select min(acc_metadataset_id) from gdms_acc_metadataset");
		
		listValues=query1.list();
		itList=listValues.iterator();
					
		while(itList.hasNext()){
			objAcc=itList.next();
			if(objAcc!=null)
				intMaxAccId=Integer.parseInt(objAcc.toString());
		}
		intMaxAccId=intMaxAccId-1;
		
		dataset = new DatasetBean();
		dataset.setDataset_id(iDatasetId);
		dataset.setDataset_name(strDatasetNameFromSourceTable);
		dataset.setDataset_desc(strDatasetDescFromSourceTable);
		dataset.setDataset_type(strDatasetType);
		dataset.setGenus(strGenusFromSourceTable);
		dataset.setSpecies(strSpeciesFromSourceTable);
		dataset.setUpload_template_date(curDate);
		dataset.setRemarks(strRemarks);
		dataset.setDatatype(strDataType);
		dataset.setMissing_data(strMissingDataFromSourceTable);
		dataset.setMethod(method);
		dataset.setScore(strScore);
		localSession.save(dataset);
		
		datasetUser = new GenotypeUsersBean();
		datasetUser.setDataset_id(iDatasetId);
		datasetUser.setUser_id(iUserId);
		localSession.save(datasetUser);
		
		previousAccGID=new ArrayList<Integer>();
		int  accSampleIdA=1;
		for(int i=0;i<finalList.size();i++){	
        	String[] strList=finalList.get(i).toString().split("~!~");
        	Integer iGId =Integer.parseInt(strList[0].toString());
        	if(previousAccGID.contains(iGId)){
        		//accSampleIdA=2;
        		accSampleIdA=accSampleIdA+1;
			}else{
				accSampleIdA=1;
			}
        	accMetadataSet=new AccessionMetaDataBean();			
        	accMetadataSet.setAccMetadatasetId(intMaxAccId);
        	accMetadataSet.setDatasetId(iDatasetId);
        	accMetadataSet.setGid(Integer.parseInt(strList[0].toString()));
        	accMetadataSet.setNid(Integer.parseInt(strList[1].toString()));
        	accMetadataSet.setAccSampleId(accSampleIdA);			
         	localSession.save(accMetadataSet);
         	
         	if(!previousAccGID.contains(iGId))
        		previousAccGID.add(iGId);
         	
			if (i % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
			intMaxAccId--;
        
        }
		 ArrayList mids=new ArrayList();
         
         HashMap<String, Object> finalHashMapMarkerAndIDs = new HashMap<String, Object>();
       System.out.println("listOfMarkerNamesFromSourceTable:"+listOfMarkerNamesFromSourceTable.size()+"   "+lstMarkers.size());
		for(int f=0; f<listOfMarkerNamesFromSourceTable.size();f++){
			MarkerInfoBean mib=new MarkerInfoBean();
			if(lstMarkers.contains(listOfMarkerNamesInLoweCase.get(f))){
				intRMarkerId=(Integer)(markersMap.get(listOfMarkerNamesInLoweCase.get(f)));							
				mids.add(intRMarkerId);
				finalHashMapMarkerAndIDs.put(listOfMarkerNamesFromSourceTable.get(f).toString().toLowerCase(), intRMarkerId);
			}else{
				//maxMid=maxMid+1;
				maxMid=maxMid-1;
				intRMarkerId=maxMid;
				finalHashMapMarkerAndIDs.put(listOfMarkerNamesFromSourceTable.get(f).toString().toLowerCase(), intRMarkerId);
				mids.add(intRMarkerId);	
				mib.setMarkerId(intRMarkerId);
				mib.setMarker_type("SNP");
				mib.setMarker_name(listOfMarkerNamesFromSourceTable.get(f).toString());
				//mib.setCrop(sheetSource.getCell(1,5).getContents());
				mib.setSpecies(strSpeciesFromSourceTable);
				
				localSession.save(mib);
				if (f % 1 == 0){
					localSession.flush();
					localSession.clear();
				}
			}
			
			
		}
		
		ArrayList<Integer> previousGID=new ArrayList<Integer>();
		ArrayList<Integer> previousMID=new ArrayList<Integer>();
		//listOfSNPDataRows = new ArrayList<SNPDataRow>();
		/*System.out.println("listOfDataRowsFromDataTable..............=:"+listOfDataRowsFromDataTable);
		System.out.println("hashMapOfEntryIDandGID:"+hashMapOfEntryIDandGID);*/
		listOfMarkers=new ArrayList();
		String strGenotype="";
		
		if(! (listOfMarkersFromTheSheet.size()>320 || gNamesList.size()>500)){		
			for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){			
				HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);						
				//Integer strGID = Integer.parseInt(hashMapOfDataRow.get(UploadField.GID.toString()));
				//String entry=hashMapOfDataRow.get(UploadField.Genotype.toString());
				String strEntry = hashMapOfDataRow.get(UploadField.Genotype.toString());
				if(hmOfDuplicateGermNames.containsKey(strEntry))
					strGenotype=hmOfDuplicateGermNames.get(strEntry);
				else
					strGenotype=strEntry;
				Integer strGID = hashMapOfEntryIDandGID.get(strGenotype);
				//System.out.println("entry:"+strGenotype+"   strGID=:"+strGID);
				Integer iNameId = hashMapOfGIDsandNIDs.get(strGID);
			//	int datasetId = -10;*/
				iACId--;
				
				if(previousGID.contains(strGID)){//.equals(previousGID)){
					//accSampleId=2;
					accSampleId=accSampleId+1;
				}else
					accSampleId=1;
				
				//accMetadataSet1 = new AccMetadataSet(new AccMetadataSetPK(datasetId, Integer.parseInt(strGID), iNameId));	
				
				previousMID=new ArrayList<Integer>();
				for (int j = 0; j < listOfMarkerNamesFromSourceTable.size(); j++) {					
					strCharValue=hashMapOfDataRow.get(listOfMarkerNamesFromSourceTable.get(j));					
					String strMarkerName = listOfMarkerNamesFromSourceTable.get(j).toLowerCase();							
					
					if(i==0){
						listOfMarkers.add(strMarkerName.toLowerCase());
					}
					
					int markerId=Integer.parseInt(finalHashMapMarkerAndIDs.get(strMarkerName).toString());
					//if(markerId==previousMID){
					if(previousMID.contains(markerId)){
						//markerSampleId=2;
						markerSampleId=markerSampleId+1;
					}else
						markerSampleId=1;
					
					CharArrayBean charValues=new CharArrayBean();
					CharArrayCompositeKey cack = new CharArrayCompositeKey();
					
					//**************** writing to char_values tables........
					cack.setDataset_id(iDatasetId);
					cack.setAc_id(iACId);
					charValues.setComKey(cack);
					charValues.setGid(strGID);
					//System.out.println("strCharValue:"+strCharValue+" "+strMarkerName+"   "+strGenotype);
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
							/*
							 ErrMsg = "Heterozygote data representation should be either : or /";
							 request.getSession().setAttribute("indErrMsg", ErrMsg);
							 return "ErrMsg";*/	 
						}
						//^^^^^^^^^^^^^^^^^^^^^^^^   listOfDataColumnFields	
					}else if(strCharValue.length()==2){
						String str1="";
						String str2="";
						String charStr=strCharValue;
						str1=charStr.substring(0, charStr.length()-1);
						str2=charStr.substring(1);
						charData=str1+"/"+str2;
						////System.out.println(".....:"+str.get(s).substring(1));
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
					charValues.setMarker_id(Integer.parseInt(finalHashMapMarkerAndIDs.get(strMarkerName).toString()));
					charValues.setAccSampleID(accSampleId);
					charValues.setMarkerSampleId(markerSampleId);
	
					if(!previousGID.contains(strGID))
						previousGID.add(strGID);
					if(!previousMID.contains(markerId))
						previousMID.add(markerId);
					localSession.save(charValues);
					
					if (j % 1 == 0){
						localSession.flush();
						localSession.clear();
					}
					iACId--;
				}
				
				
			}
		}else{
			//System.out.println(listOfGNames.size()+"   "+listOfGenoData.size());
			
			previousMID=new ArrayList<Integer>();
			iACId--;
			for(int d=0;d<listOfGenoData.size();d++){					
			
				List str=listOfGenoData.get(d);						
				//System.out.println(str.size()+"   "+str);
				int g=0;
				previousGID=new ArrayList<Integer>();
				for(int s=0;s<str.size();s++){
					String strEntry ="";
					if(s==0){
						//System.out.println("markersMap:"+markersMap);
						marker=str.get(0).toString().toLowerCase();		
						//System.out.println(d+" : marker="+marker);
						marker_id=Integer.parseInt(finalHashMapMarkerAndIDs.get(marker).toString());
						listOfMarkers.add(marker);
						if(previousMID.contains(marker_id)){
							//markerSampleId=2;
							markerSampleId=markerSampleId+1;
						}else
							markerSampleId=1;
					}
					
					
					if(s!=0){
						if(g<str.size()-1)
							strEntry = listOfGNames.get(g);
							
							if(hmOfDuplicateGermNames.containsKey(strEntry))
								strGenotype=hmOfDuplicateGermNames.get(strEntry);
							else
								strGenotype=strEntry;
							
												
							Integer strGID = hashMapOfEntryIDandGID.get(strGenotype);
							if(previousGID.contains(strGID)){//.equals(previousGID)){
								//accSampleId=2;
								accSampleId=accSampleId+1;
							}else
								accSampleId=1;
							//System.out.println(strGenotype+"  "+strGID);
						CharArrayBean charValues=new CharArrayBean();
						CharArrayCompositeKey cack = new CharArrayCompositeKey();
						
						//**************** writing to char_values tables........
						cack.setDataset_id(iDatasetId);
						cack.setAc_id(iACId);
						charValues.setComKey(cack);
						//charValues.setGid(strGID);
						strCharValue=str.get(s).toString();
						
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
							//^^^^^^^^^^^^^^^^^^^^^^^^   listOfDataColumnFields	
						}else if(strCharValue.length()==2){
							String str1="";
							String str2="";
							String charStr=strCharValue;
							str1=charStr.substring(0, charStr.length()-1);
							str2=charStr.substring(1);
							charData=str1+"/"+str2;
							////System.out.println(".....:"+str.get(s).substring(1));
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
						
						//System.out.println(charData+"   "+gids[s]+"   "+intRMarkerId+"   "+genotype[s]);
						charValues.setChar_value(charData);
						charValues.setGid(strGID);
						
						charValues.setMarker_id(marker_id);
						charValues.setAccSampleID(accSampleId);
						charValues.setMarkerSampleId(markerSampleId);
		
						if(!previousGID.contains(strGID))
							previousGID.add(strGID);
						if(!previousMID.contains(marker_id))
							previousMID.add(marker_id);
						
						localSession.save(charValues);
						
						iACId--;
												
						if (d % 1 == 0){
							localSession.flush();
							localSession.clear();
						}
						g++;
					}				
					
				}				
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
		
		int markerSampleIdMMD=1;
		ArrayList<Integer> previousMIDMMD=new ArrayList<Integer>();
		for(int m1=0;m1<listOfMarkers.size();m1++){	
			intMaxMarMetaId--;
			
			int mid=Integer.parseInt(finalHashMapMarkerAndIDs.get(listOfMarkers.get(m1)).toString());
			if(previousMIDMMD.contains(mid)){
				markerSampleIdMMD=2;
			}else
				markerSampleIdMMD=1;
			
			////System.out.println("gids doesnot Exists    :"+lstgermpName+"   "+gids[l]);
			MarkerMetaDataBean mdb=new MarkerMetaDataBean();					
			//******************   GermplasmTemp   *********************//*	
			
			mdb.setMarkerMetadatasetId(intMaxMarMetaId);
			
			mdb.setDatasetId(iDatasetId);
			mdb.setMarkerId(mid);
			mdb.setMarkerSampleId(markerSampleIdMMD);
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
		
	}

	public String getDataUploaded() {
		////System.out.println("arrayOfMarkers:"+arrayOfMarkers);
		String strDataUploaded = "";
		strDataUploaded = "Uploaded SNP Genotyping dataset "; 
		//}
		
		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromSourceSheet() {
		return listOfDataInSourceLines;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromDataSheet() {
		return listOfDataInDataLines;
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
	

}
