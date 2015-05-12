package org.icrisat.gdms.upload.marker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerAlias;
import org.generationcp.middleware.pojos.gdms.MarkerDetails;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.ExcelSheetColumnName;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.genotyping.MarkerUserInfoBean;
import org.icrisat.gdms.upload.genotyping.MarkerUserInfoDetailsBean;

public class SNPMarker implements UploadMarker {

	private String strFileLocation;
	private Sheet sheetMarkerDetails;
	private int iExistingMarkersCountInExcelSheet;
	private ArrayList<HashMap<String, String>> arrayListOfAllMarkersToBeDisplayed;
	private Marker marker;
	private MarkerAlias markerAlias;
	private MarkerDetails markerDetails;
	private MarkerUserInfoBean markerUserInfo;
	
	private MarkerUserInfoDetailsBean markerUserDetails;
	
	private Marker[] arrayOfMarkers;
	private ArrayList<HashMap<String, String>> listOfDataRowsToBeUploaded;
	private ArrayList<FieldProperties> listOfColumnsInTheTable;
	
	private ArrayList<HashMap<String, String>> listOfNewMarkersToBeSavedToDB;
	private ArrayList<HashMap<String, String>> listOfMarkersToBeSavedToDB;
	
	private int iCountOfExistingMarkers;
	ManagerFactory factory;
	
	private GDMSMain _mainHomePage;
	
	GenotypicDataManager genoManager;

	private Session centralSession;
	private Session localSession;
	
	private Session session;
	private Transaction tx;
	
	HashMap<String, Integer> hashMapMarkerDetExists;
	List listToUpdate;
	String strErrorMsg="no";
	public void readExcelFile() throws GDMSException {
		Workbook workbook;
		try {
			String ext=strFileLocation.substring(strFileLocation.lastIndexOf("."));
			if(ext.equals(".xls")){
				workbook = Workbook.getWorkbook(new File(strFileLocation));
				String[] strSheetNames = workbook.getSheetNames();
				String strSheetName = "";
				for (int i=0;i<strSheetNames.length;i++){
					if(strSheetNames[i].equalsIgnoreCase("SNPMarkers"))
						strSheetName = strSheetNames[i];
				}
				sheetMarkerDetails = workbook.getSheet(strSheetName);
				if(strSheetName.equals("")){
					throw new GDMSException("Marker Sheet Name not found");
				}
			}else{
				throw new GDMSException("Please check the file, it should be in excel format");				
			}
		} catch (BiffException e) {
			throw new GDMSException("Error Reading SNP Marker Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading SNP Marker Sheet - " + e.getMessage());
		}
	}

	//@Override
	public String validateDataInExcelSheet() throws GDMSException {
		int iNumOfRowsInExcelSheet = sheetMarkerDetails.getRows();
		String strTempColumnNames[] = {"Marker Name", "Alias (comma separated for multiple names)", "Crop", "Genotype", "Ploidy", "GID",
				"Principal Investigator", "Contact", "Institute", "Incharge Person", "Assay Type", "Forward Primer",
				"Reverse Primer", "Product Size", "Expected Product Size", "Position on Refrence Sequence",
				"Motif", "Annealing Temperature", "Sequence", "Reference"};

		//First checking if the sheet has all the columns require for SNPMarker
		//right columns are present 
		for(int j = 0; j < strTempColumnNames.length; j++) {
			String strColName = (String)sheetMarkerDetails.getCell(j, 0).getContents().trim();
			if (false == strColName.equals(strTempColumnNames[j])){
				throw new GDMSException("The provided SNPMarker sheet does not have " + strTempColumnNames[j] + " column " + "at position: " + j);
			}
		}


		//Then checking if values have been provided for the mandatory fields
		//check the Marker Name, Crop Name, Principal Investigator, Institute, Forward Primer and Reverse Primer fields in sheet
		for(int i = 1; i < iNumOfRowsInExcelSheet; i++){

			String strMarkerName = (String)sheetMarkerDetails.getCell(0, i).getContents().trim();

			if(strMarkerName.equals("")||strMarkerName==null){
				ExcelSheetColumnName escn =  new ExcelSheetColumnName();
				String strColName = escn.getColumnName(sheetMarkerDetails.getCell(0, i).getColumn());							
				String strErrMsg = " Provide Marker name at cell position " + strColName + (sheetMarkerDetails.getCell(0, i).getRow()+1);
				throw new GDMSException(strErrMsg);
			} else { 

				String strCropName = (String)sheetMarkerDetails.getCell(2, i).getContents().trim();	
				String strPrincipalInves=(String)sheetMarkerDetails.getCell(6, i).getContents().trim();	
				String strInstitute=(String)sheetMarkerDetails.getCell(8, i).getContents().trim();
				//String strForwaredPrimer=(String)sheetMarkerDetails.getCell(20, i).getContents().trim();
				//String strReversePrimer=(String)sheetMarkerDetails.getCell(21, i).getContents().trim();

				if(strCropName.equals("")||strCropName==null){
					ExcelSheetColumnName escn =  new ExcelSheetColumnName();
					String strColName = escn.getColumnName(sheetMarkerDetails.getCell(2, i).getColumn());							
					String strErrMsg = "Provide the Species derived from for Marker " + strMarkerName + " at cell position " + strColName + (sheetMarkerDetails.getCell(2, i).getRow()+1);
					throw new GDMSException(strErrMsg);						
				}else if(strPrincipalInves.equals("")||strPrincipalInves==null){
					ExcelSheetColumnName escn =  new ExcelSheetColumnName();
					String strColName = escn.getColumnName(sheetMarkerDetails.getCell(6, i).getColumn());							
					String strErrMsg = "Provide the Principal Investigator for Marker " + strMarkerName + " at cell position " + strColName + (sheetMarkerDetails.getCell(6, i).getRow()+1);
					throw new GDMSException(strErrMsg);
				}else if(strInstitute.equals("")||strInstitute==null){
					ExcelSheetColumnName escn =  new ExcelSheetColumnName();
					String strColName = escn.getColumnName(sheetMarkerDetails.getCell(8, i).getColumn());							
					String strErrMsg = "Provide the Institute for Marker " + strMarkerName + " at cell position " + strColName + (sheetMarkerDetails.getCell(8, i).getRow()+1);
					throw new GDMSException(strErrMsg);
				}
			}
		}
		return strErrorMsg;
	}

	
	@Override
public void createObjectsToBeDisplayedOnGUI(GDMSMain theMainHomePage) throws GDMSException {
		
		_mainHomePage = theMainHomePage;

		int iNewRowCount = iExistingMarkersCountInExcelSheet + 1;
		int iNumOfRowsInExcelSheet = sheetMarkerDetails.getRows();
		arrayListOfAllMarkersToBeDisplayed = new ArrayList<HashMap<String,String>>();

		for(int r = iNewRowCount; r < iNumOfRowsInExcelSheet; r++){
			
			HashMap<String, String> hmOfRowData = new HashMap<String, String>();
			
			hmOfRowData.put(UploadField.MarkerName.toString(), sheetMarkerDetails.getCell(0,r).getContents().trim());
			hmOfRowData.put(UploadField.Alias.toString(), sheetMarkerDetails.getCell(1,r).getContents().trim());
			hmOfRowData.put(UploadField.Crop.toString(), sheetMarkerDetails.getCell(2,r).getContents().trim());
			hmOfRowData.put(UploadField.Genotype.toString(), sheetMarkerDetails.getCell(3,r).getContents().trim());
			hmOfRowData.put(UploadField.Ploidy.toString(), sheetMarkerDetails.getCell(4,r).getContents().trim());
			hmOfRowData.put(UploadField.GID.toString(), sheetMarkerDetails.getCell(5,r).getContents().trim());
			hmOfRowData.put(UploadField.PrincipleInvestigator.toString(), sheetMarkerDetails.getCell(6,r).getContents().trim());
			hmOfRowData.put(UploadField.Contact.toString(), sheetMarkerDetails.getCell(7,r).getContents().trim());
			hmOfRowData.put(UploadField.Institute.toString(), sheetMarkerDetails.getCell(8,r).getContents().trim());
			hmOfRowData.put(UploadField.InchargePerson.toString(), sheetMarkerDetails.getCell(9,r).getContents().trim());
			hmOfRowData.put(UploadField.AssayType.toString(), sheetMarkerDetails.getCell(10,r).getContents().trim());
			hmOfRowData.put(UploadField.ForwardPrimer.toString(), sheetMarkerDetails.getCell(11,r).getContents().trim());
			hmOfRowData.put(UploadField.ReversePrimer.toString(), sheetMarkerDetails.getCell(12,r).getContents().trim());
			hmOfRowData.put(UploadField.ProductSize.toString(), sheetMarkerDetails.getCell(13,r).getContents().trim());
			hmOfRowData.put(UploadField.ExpectedProductSize.toString(), sheetMarkerDetails.getCell(14,r).getContents().trim());
			hmOfRowData.put(UploadField.PositionOnReferenceSequence.toString(), sheetMarkerDetails.getCell(15,r).getContents().trim());
			hmOfRowData.put(UploadField.Motif.toString(), sheetMarkerDetails.getCell(16,r).getContents().trim());
			hmOfRowData.put(UploadField.AnnealingTemperature.toString(), sheetMarkerDetails.getCell(17,r).getContents().trim());
			hmOfRowData.put(UploadField.Sequence.toString(), sheetMarkerDetails.getCell(18,r).getContents().trim());
			hmOfRowData.put(UploadField.Reference.toString(), sheetMarkerDetails.getCell(19,r).getContents().trim());
			
			arrayListOfAllMarkersToBeDisplayed.add(hmOfRowData);
		}
	}
	

	@Override
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}


	@Override
	public void upload() throws GDMSException {
		validateData();
		buildNewListOfMarkersToBeSaved();
		createObjectsToBeSavedToDB();
	}
	

	@Override
	public void validateData() throws GDMSException {

		String strTempColumnNames[] = {UploadField.MarkerName.toString(), UploadField.Alias.toString(), UploadField.Crop.toString(), 
				UploadField.Genotype.toString(), UploadField.Ploidy.toString(), UploadField.GID.toString(), 
				UploadField.PrincipleInvestigator.toString(), UploadField.Contact.toString(), UploadField.Institute.toString(),
				UploadField.InchargePerson.toString(), UploadField.AssayType.toString(), UploadField.ForwardPrimer.toString(),
				UploadField.ReversePrimer.toString(), UploadField.ProductSize.toString(), UploadField.ExpectedProductSize.toString(), 
				UploadField.PositionOnReferenceSequence.toString(), UploadField.Motif.toString(), UploadField.AnnealingTemperature.toString(),
				UploadField.Sequence.toString(), UploadField.Reference.toString()};

		//First checking if all the columns required for SNPMarker are present 
		for(int j = 0; j < strTempColumnNames.length; j++) {
			FieldProperties fieldProperties = listOfColumnsInTheTable.get(j+1);
			String strColName = fieldProperties.getFieldName();
			if (false == strColName.equals(strTempColumnNames[j])){
				throw new GDMSException("The provided SNPMarker sheet does not have " + strTempColumnNames[j] + " column " + "at position: " + (j+1));
			}
		}


		//Then checking if values have been provided for the mandatory fields
		//check the Marker Name, Crop Name, Principal Investigator, Institute, Forward Primer and Reverse Primer fields in sheet
		for(int i = 0; i < listOfDataRowsToBeUploaded.size(); i++){

			HashMap<String,String> hashMap = listOfDataRowsToBeUploaded.get(i);
			String strMarkerName = hashMap.get(UploadField.MarkerName.toString());

			if(strMarkerName.equals("")||strMarkerName==null){
				String strErrMsg = "Provide Marker name at cell position<BR/> in Row#: " + (i+1);
				throw new GDMSException(strErrMsg);
			} else { 

				String strCropName = hashMap.get(UploadField.Crop.toString());	
				String strPrincipalInves = hashMap.get(UploadField.PrincipleInvestigator.toString());	
				String strInstitute = hashMap.get(UploadField.Institute.toString());
				//String strForwaredPrimer = hashMap.get(UploadField.ForwardPrimer.toString());
				//String strReversePrimer = hashMap.get(UploadField.ReversePrimer.toString());

				if(strCropName.equals("")||strCropName==null){
					String strErrMsg = "Provide the Species derived from for Marker<BR/>" + strMarkerName + " in Row#: " + (i+1);
					throw new GDMSException(strErrMsg);						
				}else if(strPrincipalInves.equals("")||strPrincipalInves==null){
					String strErrMsg = "Provide the Principal Investigator for Marker<BR/>" + strMarkerName + " in Row#: " + (i+1);
					throw new GDMSException(strErrMsg);
				}else if(strInstitute.equals("")||strInstitute==null){
					String strErrMsg = "Provide the Institute for Marker<BR/>" + strMarkerName + " in Row#: " + (i+1);
					throw new GDMSException(strErrMsg);
				}
			}
		}
	}
	
	private void buildNewListOfMarkersToBeSaved() throws GDMSException {
		try{			
			factory=GDMSModel.getGDMSModel().getManagerFactory();			
			genoManager=factory.getGenotypicDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		HashMap<String, String> hashMapMarkersAndPrimer=new HashMap<String, String>();
		
		///Get the Marker names from template and adding the Marker and Crop names to the List object.
		List<String> listTempMarkerNames= new ArrayList<String>();
		int iNumOfRowsInTable = listOfDataRowsToBeUploaded.size();
		String tempMarkers="";
		HashMap<String, HashMap<String, String>> hashMapOfTempMarkers = new HashMap<String, HashMap<String,String>>();	
		for(int i = 0 ; i < iNumOfRowsInTable; i++){
			HashMap<String,String> hashMap = listOfDataRowsToBeUploaded.get(i);
			String strMarkerName = hashMap.get(UploadField.MarkerName.toString().trim());
			String strCrop = hashMap.get(UploadField.Crop.toString());
			//String strMarkerAndCropFromSheet = strMarkerName + "!`!" + strCrop + "!`!snp";
			//listTempMarkerNames.add(strMarkerAndCropFromSheet.toLowerCase());
			listTempMarkerNames.add(strMarkerName.toLowerCase());
			tempMarkers=tempMarkers+"'"+strMarkerName.toLowerCase()+"',";
			hashMapOfTempMarkers.put(strMarkerName.toLowerCase(), hashMap);
		}
		List<String> listDBMarkers = new ArrayList<String>();
		List<String> listDBMarkersWithOutDet = new ArrayList<String>();
		hashMapMarkerDetExists=new HashMap<String, Integer>();
		//System.out.println("listTempMarkerNames:"+listTempMarkerNames);
	//	System.out.println("listTempMarkerNames.size:"+listTempMarkerNames.size());
		/*try{
			List<ExtendedMarkerInfo> marDet=genoManager.getMarkerInfoByMarkerNames(listTempMarkerNames);
			
			for(ExtendedMarkerInfo det:marDet){
				listDBMarkers.add(det.getMarkerName().toLowerCase().toString());
				if(det.getPrincipalInvestigator()==null){
					listDBMarkersWithOutDet.add(det.getMarkerName().toLowerCase().toString());
					hashMapMarkerDetExists.put(det.getMarkerName(), det.getMarkerId());
				}
			}
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		SQLQuery queryL;
		SQLQuery queryC;
		List allelesFromLocal=new ArrayList();	
		List allelesFromCentral=new ArrayList();
		//System.out.println("All Markers:"+tempMarkers);
		String strQuery="select marker_id, marker_name, principal_investigator from gdms_marker_retrieval_info where marker_name in("+tempMarkers.substring(0, tempMarkers.length()-1)+")";
		//System.out.println("....:"+strQuery);
		
		List snpsFromLocal=new ArrayList();		
		List markersList=new ArrayList();		
		List snpsFromCentral=new ArrayList();

		Object obj=null;
		Object objL=null;
		Iterator itListC=null;
		Iterator itListL=null;		
			
		
		queryL=localSession.createSQLQuery(strQuery);		
		queryL.addScalar("marker_id",Hibernate.INTEGER);	 
		queryL.addScalar("marker_name",Hibernate.STRING);
		queryL.addScalar("principal_investigator",Hibernate.STRING);
		allelesFromLocal=queryL.list();
		
		
		queryC=centralSession.createSQLQuery(strQuery);
		queryC.addScalar("marker_id",Hibernate.INTEGER);	 
		queryC.addScalar("marker_name",Hibernate.STRING);
		queryC.addScalar("principal_investigator",Hibernate.STRING);		
		allelesFromCentral=queryC.list();
		
	
		for(int w=0;w<allelesFromCentral.size();w++){
			Object[] strMareO= (Object[])allelesFromCentral.get(w);
			//System.out.println("************Marker From Central...........:"+strMareO[0]+"  "+strMareO[1]+"  "+strMareO[2]);	
			listDBMarkers.add(strMareO[1].toString().toLowerCase());
			/*if(strMareO[2]==null){
				listDBMarkersWithOutDet.add(strMareO[1].toString().toLowerCase());
				hashMapMarkerDetExists.put(strMareO[1].toString(), Integer.parseInt(strMareO[0].toString()));
			}	*/	
																			
		}
		//System.out.println("allelesFromLocal:"+allelesFromLocal);
		for(int w=0;w<allelesFromLocal.size();w++){
			Object[] strMareO= (Object[])allelesFromLocal.get(w);	
			//System.out.println("************Marker From Local.............:"+strMareO[0]+"  "+strMareO[1]+"  "+strMareO[2]);
			listDBMarkers.add(strMareO[1].toString().toLowerCase());
			if(strMareO[2]==null){
				listDBMarkersWithOutDet.add(strMareO[1].toString().toLowerCase());
				hashMapMarkerDetExists.put(strMareO[1].toString(), Integer.parseInt(strMareO[0].toString()));
			}
																			
		}
		
		
		
		
		/*for(int tm=0;tm<listTempMarkerNames.size();tm++){
			tempMarkers=tempMarkers+"'"+listTempMarkerNames.get(tm)+"',";
			try {
				List<MarkerInfo> marDet1=genoManager.getMarkerInfoByMarkerName(listTempMarkerNames.get(tm), 0, (int)genoManager.countMarkerInfoByMarkerName(listTempMarkerNames.get(tm)));
				for(MarkerInfo det:marDet){
					listDBMarkers.add(det.getMarkerName().toLowerCase().toString());
					if(det.getPrincipalInvestigator()==null){
						listDBMarkersWithOutDet.add(det.getMarkerName().toLowerCase().toString());
						hashMapMarkerDetExists.put(det.getMarkerName(), det.getMarkerId());
					}
				}
			} catch (MiddlewareQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		//System.out.println("select * from gdms_marker_retrieval_info where marker_name in("+tempMarkers.subSequence(0, tempMarkers.length()-1)+") ");
		/*System.out.println("listDBMarkersWithOutDet:"+listDBMarkersWithOutDet);
		System.out.println("listDBMarkers"+listDBMarkers);*/
		/** 
		 * Obtaining the list of existing Markers from the database using the MarkerDAO object 
		 */
		/*Session session = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(session);
		List<Marker> listOfAllExistingMarkersFromDB;
		List<String> listDBMarkerNames = new ArrayList<String>();				
		try {
			listOfAllExistingMarkersFromDB = markerDAO.getAll();
			for (int i = 0; i < listOfAllExistingMarkersFromDB.size(); i++){
				Marker marker = listOfAllExistingMarkersFromDB.get(i);
				String strMarkerAndCropFromDB = marker.getMarkerName()+ "!`!" + marker.getSpecies() + "!`!" + marker.getMarkerType();
				listDBMarkerNames.add(strMarkerAndCropFromDB.toLowerCase());	
			}
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
		/***
		 * 20130813: Fix for Issue#: 53 on Trello
		 * 
		 * Checking for duplicates Marker information in Central database as well
		 *  
		 */
		/*Session centralSession =GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		MarkerDAO markerDAOCentral = new MarkerDAO();
		markerDAOCentral.setSession(centralSession);
		try {
			listOfAllExistingMarkersFromDB = markerDAOCentral.getAll();
			for (int i = 0; i < listOfAllExistingMarkersFromDB.size(); i++){
				Marker marker = listOfAllExistingMarkersFromDB.get(i);
				String strMarkerAndCropFromDB = marker.getMarkerName()+ "!`!" + marker.getSpecies() + "!`!" + marker.getMarkerType();
				listDBMarkerNames.add(strMarkerAndCropFromDB.toLowerCase());	
			}
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		//20130813: End of fix for Issue#: 53 on Trello
		


		/** 
		 * Comparing the Markers from the Database and the Template file 
		 * and creating the list of new markers to be uploaded
		 */
		
		//System.out.println("listTempMarkerNames:"+listTempMarkerNames);
		//System.out.println("listDBMarkersWithOutDet:"+listDBMarkersWithOutDet);
		
		Iterator<String> itCom;				
		itCom = listTempMarkerNames.iterator();
		listOfNewMarkersToBeSavedToDB = new ArrayList<HashMap<String,String>>();
		
		listOfMarkersToBeSavedToDB = new ArrayList<HashMap<String,String>>();
		
		iCountOfExistingMarkers = 0;
		listToUpdate=new ArrayList();
		while(itCom.hasNext()){
			Object objCom = itCom.next();
			//System.out.println("objCom:"+objCom);
			if(listDBMarkers.contains(objCom)){
				//System.out.println(" obj  exists in listDBMarkers................");
				if(listDBMarkersWithOutDet.contains(objCom)){
					//System.out.println(" ,,,,,,,,,,,,,,,,,,  obj  exists in listDBMarkersWithOutDet................");
					String strTempMarker = (String)objCom;
					//System.out.println("strTempMarker:"+strTempMarker);
					listToUpdate.add(strTempMarker);
					HashMap<String, String> hashMap = hashMapOfTempMarkers.get(strTempMarker);
					listOfMarkersToBeSavedToDB.add(hashMap);
					//updateMarkerInfo="yes";
				}else{
					//System.out.println(" ,,,,,,,,,  else ,,,,,,,,,  obj  exists in listDBMarkersWithOutDet................");
					iCountOfExistingMarkers++;	
					//updateMarkerInfo="no";
				}
							
			} else {
				//updateMarkerInfo="no";
				//System.out.println(" else .....  obj  exists in listDBMarkers................");
				String strTempMarker = (String)objCom;
				HashMap<String, String> hashMap = hashMapOfTempMarkers.get(strTempMarker);
				listOfNewMarkersToBeSavedToDB.add(hashMap);
				listOfMarkersToBeSavedToDB.add(hashMap);
			}
		}

		//System.out.println("Markers to be Inserted/Updated :"+listOfMarkersToBeSavedToDB);
		//System.out.println("listToUpdate:"+listToUpdate);
		//System.out.println("new Markers:"+listOfNewMarkersToBeSavedToDB);

		//Message will be displayed when the marker(s) are already exists in the database.
		if(listOfMarkersToBeSavedToDB.size() == 0){
			String strErrMsg = "All the marker(s) already exists in the database";
			throw new GDMSException(strErrMsg);
		}

	}

	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {

		//Integer iMarkerID = 0;
		Integer mid=0;
		
		Integer iMarkerID = null;
		String strMarkerType = "SNP";
		int iUploadedMarkerCount = 0;
		int iNumOfNewMarkers = listOfMarkersToBeSavedToDB.size();
		arrayOfMarkers = new Marker[iNumOfNewMarkers];
		/*localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
		
		centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();*/
		Integer intMaxMAliasId=0;
		try{
			long maxMAliasId=genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_MARKER_ALIAS);
			intMaxMAliasId=(int)maxMAliasId;
			intMaxMAliasId--;
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading SSR Genotype");
		} 
		//System.out.println("listToUpdate:"+listToUpdate);
		
		
		try{
			mid = (int)genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_MARKER);
		}catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
			
		iMarkerID=mid;
		Integer contactID=null;
		
		int intMaxVal=0;
		int intMaxValTM=0;
		Object obj=null;
		Iterator itList=null;
		List listValues=null;
		Query query=localSession.createSQLQuery("select min(contact_id) from gdms_marker_user_info_details");		
		listValues=query.list();
		itList=listValues.iterator();
					
		while(itList.hasNext()){
			obj=itList.next();
			if(obj!=null)
				intMaxVal=Integer.parseInt(obj.toString());
		}
		
		contactID=intMaxVal;
		
		
		 tx=localSession.beginTransaction();
		
		for(int r = 0; r < iNumOfNewMarkers; r++){	
			iMarkerID--;
			contactID--;
			HashMap<String, String> hashMap = listOfMarkersToBeSavedToDB.get(r);
			//System.out.println("%%%%%%%%%%:"+hashMap);
			//Creating Marker object
			//marker = new Marker();
			
			if(listToUpdate.size()>0){
				String markerName=hashMap.get(UploadField.MarkerName.toString());
				if(listToUpdate.contains(markerName.toLowerCase()))
					iMarkerID=hashMapMarkerDetExists.get(hashMap.get(UploadField.MarkerName.toString()));
			}
			//System.out.println("iMarkerID:"+iMarkerID+"   MName:"+hashMap.get(UploadField.MarkerName.toString()));
			
			String principalInvestigator=hashMap.get(UploadField.PrincipleInvestigator.toString());
			String contact=hashMap.get(UploadField.Contact.toString());
			String institute=hashMap.get(UploadField.Institute.toString());
			
			String markerName=hashMap.get(UploadField.MarkerName.toString());
			String species=hashMap.get(UploadField.Crop.toString());
			String dbAccessionId=hashMap.get(UploadField.GID.toString());
			String reference=hashMap.get(UploadField.Reference.toString());
			String genotype=hashMap.get(UploadField.Genotype.toString());
			String ploidy=hashMap.get(UploadField.Ploidy.toString());
			String primerId="";
			String remarks="";
			String assayType=hashMap.get(UploadField.AssayType.toString());
			String motif=hashMap.get(UploadField.Motif.toString());
			String forwardPrimer=hashMap.get(UploadField.ForwardPrimer.toString());
			String reversePrimer=hashMap.get(UploadField.ReversePrimer.toString());
			String productSize=hashMap.get(UploadField.ProductSize.toString());
			//String annealingTemp=
			Float fAnnealingTemp=0f;
			String strAnnealingTemp = hashMap.get(UploadField.AnnealingTemperature.toString());
			if (false == strAnnealingTemp.equals("")){
				fAnnealingTemp=(Float.parseFloat(strAnnealingTemp));
			}		
					
            String amplification=hashMap.get(UploadField.Amplification.toString());
            marker = new Marker(iMarkerID, strMarkerType, markerName, species, dbAccessionId, reference, genotype,
                    ploidy, primerId, remarks, assayType, motif, forwardPrimer, reversePrimer, productSize, fAnnealingTemp,
                    amplification);
            localSession.saveOrUpdate(marker);
            
					
			/*marker.setMarkerId(iMarkerID);
			marker.setMarkerType(strMarkerType);
			
			marker.setMarkerName(hashMap.get(UploadField.MarkerName.toString()));
			marker.setSpecies(hashMap.get(UploadField.Crop.toString()));
			marker.setDbAccessionId(hashMap.get(UploadField.GID.toString()));
			marker.setReference(hashMap.get(UploadField.Reference.toString()));
			marker.setGenotype(hashMap.get(UploadField.Genotype.toString()));
			marker.setPloidy(hashMap.get(UploadField.Ploidy.toString()));
			marker.setAssayType(hashMap.get(UploadField.AssayType.toString()));
			marker.setForwardPrimer(hashMap.get(UploadField.ForwardPrimer.toString()));
			marker.setReversePrimer(hashMap.get(UploadField.ReversePrimer.toString()));
			marker.setProductSize(hashMap.get(UploadField.ProductSize.toString()));
			marker.setMotif(hashMap.get(UploadField.Motif.toString()));*/
			
			//String strAnnealingTemp = hashMap.get(UploadField.AnnealingTemperature.toString());
			/*if (false == strAnnealingTemp.equals("")){
				marker.setAnnealingTemp(Float.parseFloat(strAnnealingTemp));
			}
			*/
			
          
            
			if(listToUpdate.size()>0){
				markerName=hashMap.get(UploadField.MarkerName.toString());
				if(listToUpdate.contains(markerName.toLowerCase()))
					iMarkerID=hashMapMarkerDetExists.get(hashMap.get(UploadField.MarkerName.toString()));
			}
			//Creating MarkerAlias object
			//if(hashMap.get(UploadField.Alias.toString())!=null){
				markerAlias = new MarkerAlias(intMaxMAliasId, iMarkerID, hashMap.get(UploadField.Alias.toString()));
				localSession.saveOrUpdate(markerAlias);
			//}
			/*
			
			markerAlias.setMarkerId(iMarkerID);
			markerAlias.setAlias(hashMap.get(UploadField.Alias.toString()));*/						
			
			//markerUserInfo = new MarkerUserInfo();	
			if(listToUpdate.size()>0){
				markerName=hashMap.get(UploadField.MarkerName.toString());
				if(listToUpdate.contains(markerName.toLowerCase()))
					iMarkerID=hashMapMarkerDetExists.get(hashMap.get(UploadField.MarkerName.toString()));
			}
			//contactID=-1;
			
			/*if(listToUpdate.size()>0){
				markerName=hashMap.get(UploadField.MarkerName.toString());
				if(listToUpdate.contains(markerName.toLowerCase()))
				iMarkerID=hashMapMarkerDetExists.get(hashMap.get(UploadField.MarkerName.toString()));
			}					
			markerUserInfo.setMarkerId(iMarkerID);
			markerUserInfo.setPrincipalInvestigator(hashMap.get(UploadField.PrincipleInvestigator.toString()));
			markerUserInfo.setContactValue(hashMap.get(UploadField.Contact.toString()));
			//markerUserInfo.setContact(hashMap.get(UploadField.Contact.toString()));
			markerUserInfo.setInstitute(hashMap.get(UploadField.Institute.toString()));

*/
			 
			 markerUserDetails= new MarkerUserInfoDetailsBean();
			 markerUserDetails.setContact_id(contactID);
			 markerUserDetails.setPrincipal_investigator(principalInvestigator);
			 markerUserDetails.setInstitute(institute);
			 markerUserDetails.setContact(contact);
			 localSession.saveOrUpdate(markerUserDetails);
			 
			 
			 markerUserInfo = new MarkerUserInfoBean();
			 markerUserInfo.setUserinfo_id(contactID);
			 markerUserInfo.setMarker_id(iMarkerID);
			 markerUserInfo.setContact_id(contactID);
				
				//markerUserInfo = new MarkerUserInfo(iMarkerID,contactID, principalInvestigator, contact, institute);
			 localSession.saveOrUpdate(markerUserInfo);
			 
			// contactID--;
			String alias = "testalias";
	        Integer noOfRepeats = 0;
	        String motifType = "";
	        String sequence = "";
	        Integer sequenceLength = 0;
	        Integer minAllele = 0;
	        Integer maxAllele = 0;
	        Integer ssrNr = 0;
	        Float forwardPrimerTemp = 0f;
	        Float reversePrimerTemp = 0f;
	        Float elongationTemp = 0f;
	        Integer fragmentSizeExpected = 0;
	        Integer fragmentSizeObserved = 0;
	        Integer expectedProductSize = 0;
	        Integer positionOnReferenceSequence = 0;
	        String restrictionEnzymeForAssay = null;
			
			//Creating MarkerDetails object
			//markerDetails = new MarkerDetails();
			if(listToUpdate.size()>0){
				markerName=hashMap.get(UploadField.MarkerName.toString());
				if(listToUpdate.contains(markerName.toLowerCase()))
				iMarkerID=hashMapMarkerDetExists.get(hashMap.get(UploadField.MarkerName.toString()));
			}
			//markerDetails.setMarkerId(iMarkerID);
		
			/*String strExpectedProductSize = hashMap.get(UploadField.ExpectedProductSize.toString());
			if (false == strExpectedProductSize.equals("")){
				markerDetails.setExpectedProductSize(Integer.parseInt(strExpectedProductSize));
			}
			
			String strPositionOnRefSeq = hashMap.get(UploadField.PositionOnReferenceSequence.toString());
			if (false == strPositionOnRefSeq.equals("")){
				markerDetails.setPositionOnReferenceSequence(Integer.parseInt(strPositionOnRefSeq));
			}
			
			markerDetails.setSequence(hashMap.get(UploadField.Sequence.toString()));
			*/
			String strExpectedProductSize = hashMap.get(UploadField.ExpectedProductSize.toString());
			if (strExpectedProductSize.equals(""))
				expectedProductSize = 0;
			else
				expectedProductSize = Integer.parseInt(hashMap.get(UploadField.ExpectedProductSize.toString()));		
			
			
			String strPositionOnRefSeq = hashMap.get(UploadField.PositionOnReferenceSequence.toString());
			if (strPositionOnRefSeq.equals(""))
				positionOnReferenceSequence = 0;
			else
				positionOnReferenceSequence = Integer.parseInt(hashMap.get(UploadField.PositionOnReferenceSequence.toString()));
			
			sequence=hashMap.get(UploadField.Sequence.toString());
			
			markerDetails = new MarkerDetails(iMarkerID, noOfRepeats, motifType, sequence, sequenceLength,
	                minAllele, maxAllele, ssrNr, forwardPrimerTemp, reversePrimerTemp, elongationTemp,
	                fragmentSizeExpected, fragmentSizeObserved, expectedProductSize, positionOnReferenceSequence,
	                restrictionEnzymeForAssay);
			
			localSession.saveOrUpdate(markerDetails);
			 
			
			
			if (r % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
			intMaxMAliasId--;
			
			//Creating MarkerUserInfo object
			/*if(listToUpdate.size()>0){
				markerName=hashMap.get(UploadField.MarkerName.toString());
				if(listToUpdate.contains(markerName.toLowerCase()))
				iMarkerID=hashMapMarkerDetExists.get(hashMap.get(UploadField.MarkerName.toString()));
			}	
			markerUserInfo = new MarkerUserInfo();						
			markerUserInfo.setMarkerId(iMarkerID);
			markerUserInfo.setPrincipalInvestigator(hashMap.get(UploadField.PrincipleInvestigator.toString()));
			markerUserInfo.setContactValue(hashMap.get(UploadField.Contact.toString()));
			markerUserInfo.setInstitute(hashMap.get(UploadField.Institute.toString()));
			System.out.println("markerId=:"+iMarkerID);*/
			
			//saveSNPMarker();
			//if(listToUpdate.size()>0){
			/*if(listToUpdate.contains(markerName.toLowerCase())){
				updateMarkerInfo();
			}else{
				saveSNPMarker();
			}*/
			
			int iMarkerCount = iUploadedMarkerCount;
			arrayOfMarkers[iMarkerCount] = marker;
			iUploadedMarkerCount += 1;
		}
		tx.commit();
	}
	
	/*public void saveSNPMarker() throws GDMSException {
		
		factory=GDMSModel.getGDMSModel().getManagerFactory();
		genoManager=factory.getGenotypicDataManager();

		try {
			genoManager.setSNPMarkers(marker, markerAlias, markerDetails, markerUserInfo);
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading SNP Marker");
		} catch (Throwable th){
			throw new GDMSException("Error uploading SNP Marker", th);
		}
	}*/
	
	/*public void updateMarkerInfo() throws GDMSException {
		
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			
			genoManager=factory.getGenotypicDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
		try {
			System.out.println("Entered  updating loop......................");
			genoManager.updateMarkerInfo(marker, markerAlias, markerDetails, markerUserInfo);
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			throw new GDMSException("MWQE: Error Updating Marker Information");
		} catch (Throwable th){
			th.printStackTrace();
			throw new GDMSException("TH EX:  Error Updating Marker Information", th);
			
		}
	}*/
	

	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		listOfColumnsInTheTable = theListOfColumnsInTheTable;
	}

	@Override
	public String getDataUploaded() {
		
		String strDataUploaded = "";
		
		if (0 < iCountOfExistingMarkers) {
			strDataUploaded = "Number of SNP Marker(s) already existing are: " + iCountOfExistingMarkers + "\n\n\n";
		}
		
		if (null != arrayOfMarkers && arrayOfMarkers.length > 0){
			String strUploadInfo = "";
			for (int i = 0; i < arrayOfMarkers.length; i++){
				Integer iMarkerId = arrayOfMarkers[i].getMarkerId();
				String strMarkerName = arrayOfMarkers[i].getMarkerName();
				String strMarker = "Marker: " + iMarkerId + ": " + strMarkerName;
				strUploadInfo += strMarker + "\n";
			}
			strDataUploaded += "Uploaded SNP Marker(s): \n" + strUploadInfo;
		}
		
		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromSourceSheet() {
		return arrayListOfAllMarkersToBeDisplayed;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromDataSheet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDataToBeUploded(
			ArrayList<HashMap<String, String>> theListOfSourceDataRows,
			ArrayList<HashMap<String, String>> listOfDataRows,
			ArrayList<HashMap<String, String>> listOfGIDRows) {
		listOfDataRowsToBeUploaded = theListOfSourceDataRows;
	}

}
