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

public class SSRMarker implements UploadMarker {

	private Sheet sheetMarkerDetails;
	private String strFileLocation;
	private Marker marker;
	private MarkerAlias markerAlias;
	private MarkerDetails markerDetails;
	//private MarkerUserInfo markerUserInfo;
	
	private MarkerUserInfoBean markerUserInfo;
	
	private MarkerUserInfoDetailsBean markerUserDetails;
	
	private Marker[] arrayOfMarkers;
	private ArrayList<HashMap<String, String>> listOfDataRowsToBeUploaded;
	private ArrayList<FieldProperties> listOfColumnsInTheTable;
	private ArrayList<HashMap<String, String>> arrayListOfAllMarkersToBeDisplayed;
	private ArrayList<HashMap<String, String>> listOfNewMarkersToBeSavedToDB;
	private ArrayList<HashMap<String, String>> listOfMarkersToBeSavedToDB;
	private int iCountOfExistingMarkers;
	ManagerFactory factory=null;
	
	GenotypicDataManager genoManager;
	
	private GDMSMain _mainHomePage;
	
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
					if(strSheetNames[i].equalsIgnoreCase("SSRMarkers"))
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
			throw new GDMSException("Error Reading SSR Marker Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading SSR Marker Sheet - " + e.getMessage());
		}
	}


	public String validateDataInExcelSheet() throws GDMSException {
		int iNumOfRowsInExcelSheet = sheetMarkerDetails.getRows();
		String strTempColumnNames[] = {"Marker Name", "Alias (comma separated for multiple names)", "Crop", "Genotype", "Ploidy", "GID", 
				"Principal Investigator", "Contact", "Institute", "Incharge Person", "Assay Type", "Repeat", 
				"No of Repeats", "SSR Type", "Sequence", "Sequence Length", "Min Allele", "Max Allele", "SSR number",
				"Size of Repeat Motif", "Forward Primer", "Reverse Primer", "Product Size", "Primer Length", 
				"Forward Primer Temperature", "Reverse Primer Temperature", "Annealing Temperature", "Elongation Temperature",
				"Fragment Size Expected", "Fragment Size Observed", "Amplification", "Reference"};

		//First checking if the sheet has all the columns require for SSRMarker
		//right columns are present 
		for(int j = 0; j < strTempColumnNames.length; j++) {

			String strMarkerColName = (String)sheetMarkerDetails.getCell(j, 0).getContents().trim();

			if (false == strMarkerColName.equals(strTempColumnNames[j])){
				throw new GDMSException("The provided SSRMarker sheet does not have " + strTempColumnNames[j] + " column " + "at position: " + j);
			}
		}


		//Then checking if values have been provided for the mandatory fields
		//check the Marker Name, Crop Name, Principal Investigator, Institute, Forward Primer and Reverse Primer fields in sheet
		for(int i = 1; i < iNumOfRowsInExcelSheet; i++){
			String strMarkerName = (String)sheetMarkerDetails.getCell(0, i).getContents().trim();
			if(strMarkerName.equals("") || strMarkerName == null){
				ExcelSheetColumnName escn =  new ExcelSheetColumnName();
				String strColName = escn.getColumnName(sheetMarkerDetails.getCell(0, i).getColumn());							
				String strErrMsg = "Provide Marker name at cell position<BR/>" + strColName + (sheetMarkerDetails.getCell(0, i).getRow()+1);
				throw new GDMSException(strErrMsg);
			} else { 	

				String strCropName = (String)sheetMarkerDetails.getCell(2, i).getContents().trim();	
				String strPrincipalInves = (String)sheetMarkerDetails.getCell(6, i).getContents().trim();	
				String strInstitute = (String)sheetMarkerDetails.getCell(8, i).getContents().trim();
				String strForwaredPrimer = (String)sheetMarkerDetails.getCell(20, i).getContents().trim();
				String strReversePrimer = (String)sheetMarkerDetails.getCell(21, i).getContents().trim(); 

				if(strCropName.equals("") || strCropName == null){
					ExcelSheetColumnName escn =  new ExcelSheetColumnName();
					String strColName = escn.getColumnName(sheetMarkerDetails.getCell(2, i).getColumn());							
					String strErrMsg = "Provide the Species derived from for Marker<BR/>" + strMarkerName + " at cell position " + strColName + (sheetMarkerDetails.getCell(2, i).getRow()+1);
					throw new GDMSException(strErrMsg);
				} else if(strPrincipalInves.equals("") || strPrincipalInves == null){
					ExcelSheetColumnName escn =  new ExcelSheetColumnName();
					String strColName = escn.getColumnName(sheetMarkerDetails.getCell(6, i).getColumn());							
					String strErrMsg = "Provide the Principal Investigator for Marker<BR/>" + strMarkerName + " at cell position " + strColName + (sheetMarkerDetails.getCell(6, i).getRow()+1);
					throw new GDMSException(strErrMsg);
				} else if(strInstitute.equals("") || strInstitute == null){
					ExcelSheetColumnName escn =  new ExcelSheetColumnName();
					String strColName = escn.getColumnName(sheetMarkerDetails.getCell(8, i).getColumn());							
					String strErrMsg = "Provide the Institute for Marker<BR/>" + strMarkerName + " at cell position " + strColName + (sheetMarkerDetails.getCell(8, i).getRow()+1);
					throw new GDMSException(strErrMsg);
				} else if(strForwaredPrimer.equals("") || strForwaredPrimer == null){
					ExcelSheetColumnName escn =  new ExcelSheetColumnName();
					String strColName = escn.getColumnName(sheetMarkerDetails.getCell(20, i).getColumn());							
					String strErrMsg = "Provide the Forward Primer value for Marker<BR/>" + strMarkerName + " at cell position " + strColName + (sheetMarkerDetails.getCell(20, i).getRow()+1);
					throw new GDMSException(strErrMsg);
				} else if(strReversePrimer.equals("") || strReversePrimer == null){
					ExcelSheetColumnName escn =  new ExcelSheetColumnName();
					String strColName = escn.getColumnName(sheetMarkerDetails.getCell(21, i).getColumn());							
					String strErrMsg = "Provide the Reverse Primer value for Marker<BR/>" + strMarkerName + " at cell position " + strColName + (sheetMarkerDetails.getCell(21, i).getRow()+1);
					throw new GDMSException(strErrMsg);
				}
			}
		}
		return strErrorMsg;
	}

public void createObjectsToBeDisplayedOnGUI(GDMSMain theMainHomePage) throws GDMSException {
		
		_mainHomePage = theMainHomePage;

		int iNumOfRowsInExcelSheet = sheetMarkerDetails.getRows();
		arrayListOfAllMarkersToBeDisplayed = new ArrayList<HashMap<String,String>>();

		for(int r = 1; r < iNumOfRowsInExcelSheet; r++){

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
			hmOfRowData.put(UploadField.Repeat.toString(), sheetMarkerDetails.getCell(11,r).getContents().trim());
			hmOfRowData.put(UploadField.NoOfRepeats.toString(), sheetMarkerDetails.getCell(12,r).getContents().trim());
			hmOfRowData.put(UploadField.SSRType.toString(), sheetMarkerDetails.getCell(13,r).getContents().trim());
			hmOfRowData.put(UploadField.Sequence.toString(), sheetMarkerDetails.getCell(14,r).getContents().trim());
			hmOfRowData.put(UploadField.SequenceLength.toString(), sheetMarkerDetails.getCell(15,r).getContents().trim());
			hmOfRowData.put(UploadField.MinAllele.toString(), sheetMarkerDetails.getCell(16,r).getContents().trim());
			hmOfRowData.put(UploadField.MaxAllele.toString(), sheetMarkerDetails.getCell(17,r).getContents().trim());
			hmOfRowData.put(UploadField.SSRNumber.toString(), sheetMarkerDetails.getCell(18,r).getContents().trim());
			hmOfRowData.put(UploadField.SizeOfRepeatMotif.toString(), sheetMarkerDetails.getCell(19,r).getContents().trim());
			hmOfRowData.put(UploadField.ForwardPrimer.toString(), sheetMarkerDetails.getCell(20,r).getContents().trim());
			hmOfRowData.put(UploadField.ReversePrimer.toString(), sheetMarkerDetails.getCell(21,r).getContents().trim());
			hmOfRowData.put(UploadField.ProductSize.toString(), sheetMarkerDetails.getCell(22,r).getContents().trim());
			hmOfRowData.put(UploadField.PrimerLength.toString(), sheetMarkerDetails.getCell(23,r).getContents().trim());
			hmOfRowData.put(UploadField.ForwardPrimerTemperature.toString(), sheetMarkerDetails.getCell(24,r).getContents().trim());
			hmOfRowData.put(UploadField.ReversePrimerTemperature.toString(), sheetMarkerDetails.getCell(25,r).getContents().trim());
			hmOfRowData.put(UploadField.AnnealingTemperature.toString(), sheetMarkerDetails.getCell(26,r).getContents().trim());
			hmOfRowData.put(UploadField.ElongationTemperature.toString(), sheetMarkerDetails.getCell(27,r).getContents().trim());
			hmOfRowData.put(UploadField.FragmentSizeExpected.toString(), sheetMarkerDetails.getCell(28,r).getContents().trim());
			hmOfRowData.put(UploadField.FragmentSizeObserved.toString(), sheetMarkerDetails.getCell(29,r).getContents().trim());
			hmOfRowData.put(UploadField.Amplification.toString(), sheetMarkerDetails.getCell(30,r).getContents().trim());
			hmOfRowData.put(UploadField.Reference.toString(), sheetMarkerDetails.getCell(31,r).getContents().trim());

			arrayListOfAllMarkersToBeDisplayed.add(hmOfRowData);
		}
		
	}

	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}


	public void upload() throws GDMSException {
		validateData();
		buildNewListOfMarkersToBeSaved();
		createObjectsToBeSavedToDB();
		//clearForm();
	}

	public void validateData() throws GDMSException {

		String strTempColumnNames[] = {UploadField.MarkerName.toString(), UploadField.Alias.toString(), UploadField.Crop.toString(), 
				UploadField.Genotype.toString(), UploadField.Ploidy.toString(), UploadField.GID.toString(), 
				UploadField.PrincipleInvestigator.toString(), UploadField.Contact.toString(), UploadField.Institute.toString(),
				UploadField.InchargePerson.toString(), UploadField.AssayType.toString(), UploadField.Repeat.toString(), 
				UploadField.NoOfRepeats.toString(), UploadField.SSRType.toString(), UploadField.Sequence.toString(), 
				UploadField.SequenceLength.toString(), UploadField.MinAllele.toString(), UploadField.MaxAllele.toString(), 
				UploadField.SSRNumber.toString(), UploadField.SizeOfRepeatMotif.toString(), UploadField.ForwardPrimer.toString(), 
				UploadField.ReversePrimer.toString(), UploadField.ProductSize.toString(), UploadField.PrimerLength.toString(), 
				UploadField.ForwardPrimerTemperature.toString(), UploadField.ReversePrimerTemperature.toString(), UploadField.AnnealingTemperature.toString(),
				UploadField.ElongationTemperature.toString(), UploadField.FragmentSizeExpected.toString(), 
				UploadField.FragmentSizeObserved.toString(), UploadField.Amplification.toString(), UploadField.Reference.toString()};

		//First checking if all the columns required for SSRMarker columns are present 
		for(int j = 0; j < strTempColumnNames.length; j++) {
			FieldProperties fieldProperties = listOfColumnsInTheTable.get(j+1);
			String strColName = fieldProperties.getFieldName();
			if (false == strColName.equals(strTempColumnNames[j])){
				throw new GDMSException("The provided SSRMarker template does not have " + strTempColumnNames[j] + " column " + "at position: " + (j+1));
			}
		}


		//Then checking if values have been provided for the mandatory fields
		//check the Marker Name, Crop Name, Principal Investigator, Institute, Forward Primer and Reverse Primer fields in sheet
		for(int i = 0; i < listOfDataRowsToBeUploaded.size(); i++){
			HashMap<String,String> hashMap = listOfDataRowsToBeUploaded.get(i);
			String strMarkerName = hashMap.get(UploadField.MarkerName.toString());
			if(strMarkerName.equals("") || strMarkerName == null){
				String strErrMsg = "Provide Marker name at cell position<BR/> in Row#: " + (i+1);
				throw new GDMSException(strErrMsg);
			} else { 	
				String strCropName = hashMap.get(UploadField.Crop.toString());	
				String strPrincipalInves = hashMap.get(UploadField.PrincipleInvestigator.toString());	
				String strInstitute = hashMap.get(UploadField.Institute.toString());
				String strForwaredPrimer = hashMap.get(UploadField.ForwardPrimer.toString());
				String strReversePrimer = hashMap.get(UploadField.ReversePrimer.toString());

				if(strCropName.equals("") || strCropName == null){
					String strErrMsg = "Provide the Species derived from for Marker<BR/>" + strMarkerName + " in Row#: " + (i+1);
					throw new GDMSException(strErrMsg);
				} else if(strPrincipalInves.equals("") || strPrincipalInves == null){
					String strErrMsg = "Provide the Principal Investigator for Marker<BR/>" + strMarkerName + " in Row#: " + (i+1);
					throw new GDMSException(strErrMsg);
				} else if(strInstitute.equals("") || strInstitute == null){
					String strErrMsg = "Provide the Institute for Marker<BR/>" + strMarkerName + " in Row#: " + (i+1);
					throw new GDMSException(strErrMsg);
				} else if(strForwaredPrimer.equals("") || strForwaredPrimer == null){
					String strErrMsg = "Provide the Forward Primer value for Marker<BR/>" + strMarkerName + " in Row#: " + (i+1);
					throw new GDMSException(strErrMsg);
				} else if(strReversePrimer.equals("") || strReversePrimer == null){
					String strErrMsg = "Provide the Reverse Primer value for Marker<BR/>" + strMarkerName + " in Row#: " + (i+1);
					throw new GDMSException(strErrMsg);
				}
			}
		}

	}

	private void buildNewListOfMarkersToBeSaved() throws GDMSException {
		
		HashMap<String, String> hashMapMarkersAndPrimer=new HashMap<String, String>();
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
		///Get the Marker names from template and adding the Marker and Crop names to the List object.
		List<String> listTempMarkerNames= new ArrayList<String>();
		int iNumOfRowsInTable = listOfDataRowsToBeUploaded.size();
		String tempMarkers="";
		HashMap<String, HashMap<String, String>> hashMapOfTempMarkers = new HashMap<String, HashMap<String,String>>();
		for(int i = 0 ; i < iNumOfRowsInTable; i++){
			HashMap<String,String> hashMap = listOfDataRowsToBeUploaded.get(i);
			String strMarkerName = hashMap.get(UploadField.MarkerName.toString().trim());
			String strCrop = hashMap.get(UploadField.Crop.toString());
			//String strMarkerAndCropFromSheet = strMarkerName + "!`!" + strCrop + "!`!ssr";
			//listTempMarkerNames.add(strMarkerAndCropFromSheet.toLowerCase());
			listTempMarkerNames.add(strMarkerName.toLowerCase());
			tempMarkers=tempMarkers+"'"+strMarkerName.toLowerCase()+"',";
			hashMapOfTempMarkers.put(strMarkerName.toLowerCase(), hashMap);
		}
		List<String> listDBMarkers = new ArrayList<String>();
		List<String> listDBMarkersWithOutDet = new ArrayList<String>();
		hashMapMarkerDetExists=new HashMap<String, Integer>();
		/*System.out.println("listTempMarkerNames:"+listTempMarkerNames);
		for(int tm=0;tm<listTempMarkerNames.size();tm++){
			tempMarkers=tempMarkers+"'"+listTempMarkerNames.get(tm)+"',";
			try {
				List<MarkerInfo> marDet=genoManager.getMarkerInfoByMarkerName(listTempMarkerNames.get(tm), 0, (int)genoManager.countMarkerInfoByMarkerName(listTempMarkerNames.get(tm)));
				for(MarkerInfo det:marDet){
					listDBMarkers.add(det.getMarkerName().toLowerCase());
					if(det.getPrincipalInvestigator()==null){
						listDBMarkersWithOutDet.add(det.getMarkerName().toLowerCase());
						hashMapMarkerDetExists.put(det.getMarkerName(), det.getMarkerId());
					}
				}
			} catch (MiddlewareQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		SQLQuery queryL;
		SQLQuery queryC;
		List allelesFromLocal=new ArrayList();	
		List allelesFromCentral=new ArrayList();
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
						
			listDBMarkers.add(strMareO[1].toString().toLowerCase());
			/*if(strMareO[2]==null){
				listDBMarkersWithOutDet.add(strMareO[1].toString().toLowerCase());
				hashMapMarkerDetExists.put(strMareO[1].toString(), Integer.parseInt(strMareO[0].toString()));
			}	*/	
																			
		}
		
		for(int w=0;w<allelesFromLocal.size();w++){
			Object[] strMareO= (Object[])allelesFromLocal.get(w);	
			
			listDBMarkers.add(strMareO[1].toString().toLowerCase());
			if(strMareO[2]==null){
				listDBMarkersWithOutDet.add(strMareO[1].toString().toLowerCase());
				hashMapMarkerDetExists.put(strMareO[1].toString(), Integer.parseInt(strMareO[0].toString()));
			}
																			
		}
		
		
		//System.out.println("select * from gdms_marker_retrieval_info where marker_name in("+tempMarkers.subSequence(0, tempMarkers.length()-1)+") ");
		//System.out.println("listDBMarkersWithDet:"+listDBMarkersWithOutDet);
		//System.out.println("listDBMarkers"+listDBMarkers);
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
		/*Session centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
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
		Iterator<String> itCom;				
		itCom = listTempMarkerNames.iterator();
		listOfNewMarkersToBeSavedToDB = new ArrayList<HashMap<String,String>>();
		listOfMarkersToBeSavedToDB = new ArrayList<HashMap<String,String>>();
		iCountOfExistingMarkers = 0;
		listToUpdate=new ArrayList();
		while(itCom.hasNext()){
			Object objCom = itCom.next();			
			if(listDBMarkers.contains(objCom)){
				if(listDBMarkersWithOutDet.contains(objCom)){
					String strTempMarker = (String)objCom;
					listToUpdate.add(strTempMarker);
					HashMap<String, String> hashMap = hashMapOfTempMarkers.get(strTempMarker);
					listOfMarkersToBeSavedToDB.add(hashMap);
					//updateMarkerInfo="yes";
				}else{					
					iCountOfExistingMarkers++;	
					//updateMarkerInfo="no";
				}
							
			} else {
				//updateMarkerInfo="no";
				String strTempMarker = (String)objCom;
				//System.out.println("$$$$$$$$$:"+strTempMarker);
				HashMap<String, String> hashMap = hashMapOfTempMarkers.get(strTempMarker);
				//System.out.println(".........:"+hashMap);
				listOfNewMarkersToBeSavedToDB.add(hashMap);
				listOfMarkersToBeSavedToDB.add(hashMap);
			}
		}

		//System.out.println("listOfNewMarkersToBeSavedToDB:"+listOfNewMarkersToBeSavedToDB);
		//Message will be displayed when the marker(s) are already exists in the database.
		if(listOfMarkersToBeSavedToDB.size() == 0){
			String strErrMsg = "All the marker(s) already exists in the database";
			throw new GDMSException(strErrMsg);
		}

	}

	public void createObjectsToBeSavedToDB() throws GDMSException {
		Integer mid=0;
		//Integer iMarkerID = 0;
		Integer iMarkerID = null;
		String strMarkerType = "SSR";
		int iUploadedMarkerCount = 0;
		int iNumOfNewMarkers = listOfMarkersToBeSavedToDB.size();
		arrayOfMarkers = new Marker[iNumOfNewMarkers];
		Integer intMaxMAliasId=0;
		try{
			long maxMAliasId=genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_MARKER_ALIAS);
			intMaxMAliasId=(int)maxMAliasId;
			intMaxMAliasId--;
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading SSR Genotype");
		} 
		
		
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

			//Creating the Marker object
			//marker = new Marker();	
			
			
			if(listToUpdate.size()>0){
				String markerName=hashMap.get(UploadField.MarkerName.toString());
				if(listToUpdate.contains(markerName.toLowerCase()))
					iMarkerID=hashMapMarkerDetExists.get(hashMap.get(UploadField.MarkerName.toString()));
			}
			//System.out.println("iMarkerID:"+iMarkerID);
			
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
			marker.setMotif(hashMap.get(UploadField.Motif.toString()));
			marker.setForwardPrimer(hashMap.get(UploadField.ForwardPrimer.toString()));
			marker.setReversePrimer(hashMap.get(UploadField.ReversePrimer.toString()));
			marker.setProductSize(hashMap.get(UploadField.ProductSize.toString()));
*/
			//String strAnnealingTemp = hashMap.get(UploadField.AnnealingTemperature.toString());
			if (false == strAnnealingTemp.equals("")){
				marker.setAnnealingTemp(Float.parseFloat(strAnnealingTemp));
			}

			marker.setAmplification(hashMap.get(UploadField.Amplification.toString()));

			//Creating the MarkerAlias object
			if(listToUpdate.size()>0){
				markerName=hashMap.get(UploadField.MarkerName.toString());
				if(listToUpdate.contains(markerName.toLowerCase()))
					iMarkerID=hashMapMarkerDetExists.get(hashMap.get(UploadField.MarkerName.toString()));
			}
			markerAlias = new MarkerAlias(intMaxMAliasId, iMarkerID, hashMap.get(UploadField.Alias.toString()));
			localSession.saveOrUpdate(markerAlias);
			/*markerAlias.setMarkerId(iMarkerID);
			markerAlias.setAlias(hashMap.get(UploadField.Alias.toString()));*/


			//Creating the MarkerUserInfo object
			/*markerUserInfo = new MarkerUserInfo();	
			if(listToUpdate.size()>0){
				iMarkerID=hashMapMarkerDetExists.get(hashMap.get(UploadField.MarkerName.toString()));
			}					
			markerUserInfo.setMarkerId(iMarkerID);
			markerUserInfo.setPrincipalInvestigator(hashMap.get(UploadField.PrincipleInvestigator.toString()));
			markerUserInfo.setContactValue(hashMap.get(UploadField.Contact.toString()));
			//markerUserInfo.setContact(hashMap.get(UploadField.Contact.toString()));
			markerUserInfo.setInstitute(hashMap.get(UploadField.Institute.toString()));
*/
			//markerUserInfo = new MarkerUserInfo(iMarkerID, principalInvestigator, contact, institute);
			if(listToUpdate.size()>0){
				markerName=hashMap.get(UploadField.MarkerName.toString());
				if(listToUpdate.contains(markerName.toLowerCase()))
					iMarkerID=hashMapMarkerDetExists.get(hashMap.get(UploadField.MarkerName.toString()));
			}
			
			
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
			
			

			//Creating the MarkerDetails object
			markerDetails = new MarkerDetails();
			if(listToUpdate.size()>0){
				markerName=hashMap.get(UploadField.MarkerName.toString());
				if(listToUpdate.contains(markerName.toLowerCase()))
					iMarkerID=hashMapMarkerDetExists.get(hashMap.get(UploadField.MarkerName.toString()));
			}						
			markerDetails.setMarkerId(iMarkerID);

			String strNoOfRepeats = hashMap.get(UploadField.NoOfRepeats.toString());
			if (false == strNoOfRepeats.equals("")){
				markerDetails.setNoOfRepeats(Integer.parseInt(strNoOfRepeats));
			}

			markerDetails.setMotifType(hashMap.get(UploadField.MotifType.toString()));
			markerDetails.setSequence(hashMap.get(UploadField.Sequence.toString()));

			String strSequenceLength = hashMap.get(UploadField.SequenceLength.toString());
			if (false == strSequenceLength.equals("")){
				markerDetails.setSequenceLength(Integer.parseInt(strSequenceLength));
			}

			String strMinAllele = hashMap.get(UploadField.MinAllele.toString());
			if (false == strMinAllele.equals("")){
				markerDetails.setMinAllele(Integer.parseInt(strMinAllele));
			}


			String strMaxAllele = hashMap.get(UploadField.MaxAllele.toString());
			if (false == strMaxAllele.equals("")){
				markerDetails.setMaxAllele(Integer.parseInt(strMaxAllele));
			}

			String strSSRNumber = hashMap.get(UploadField.SSRNumber.toString());
			if (false == strSSRNumber.equals("")){
				markerDetails.setSsrNr(Integer.parseInt(strSSRNumber));
			}

			
			String strFwdPrimerTemp = hashMap.get(UploadField.ForwardPrimerTemperature.toString());
			if (false == strFwdPrimerTemp.equals("")){
				markerDetails.setForwardPrimerTemp(Float.parseFloat(strFwdPrimerTemp));
			}

			
			String strReversePrimerTemp = hashMap.get(UploadField.ReversePrimerTemperature.toString());
			if (false == strReversePrimerTemp.equals("")){
				markerDetails.setReversePrimerTemp(Float.parseFloat(strReversePrimerTemp));
			}

			
			String strElongationTemp = hashMap.get(UploadField.ElongationTemperature.toString());
			if (false == strElongationTemp.equals("")){
				markerDetails.setElongationTemp(Float.parseFloat(strElongationTemp));
			}

			
			String strFragmentSizeExpected = hashMap.get(UploadField.FragmentSizeExpected.toString());
			if (false == strFragmentSizeExpected.equals("")){
				markerDetails.setFragmentSizeExpected(Integer.parseInt(strFragmentSizeExpected));
			}

			
			String strFragmentSizeObserved = hashMap.get(UploadField.FragmentSizeObserved.toString());
			if (false == strFragmentSizeObserved.equals("")){
				markerDetails.setFragmentSizeObserved(Integer.parseInt(strFragmentSizeObserved));
			}

			localSession.saveOrUpdate(markerDetails);
			
			if (r % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
			intMaxMAliasId--;
			
			
			
			//saveSSRMarker();
			//if(listToUpdate.size()>0){
			/*if(listToUpdate.contains(markerName.toLowerCase())){
				updateMarkerInfo();
			}else{
				saveSSRMarker();
			}*/

			int iMarkerCount = iUploadedMarkerCount;
			arrayOfMarkers[iMarkerCount] = marker;
			iUploadedMarkerCount += 1;
		}
		tx.commit();
	}

	/*public void saveSSRMarker() throws GDMSException {
		
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
			genoManager.setSSRMarkers(marker, markerAlias, markerDetails, markerUserInfo);
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading SSR Marker");
		} catch (Throwable th){
			throw new GDMSException("Error uploading SSR Marker", th);
		}
	}
	public void updateMarkerInfo() throws GDMSException {
		
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
			
			genoManager.updateMarkerInfo(marker, markerAlias, markerDetails, markerUserInfo);
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			throw new GDMSException("MWQE: Error Updating Marker Information");
		} catch (Throwable th){
			th.printStackTrace();
			throw new GDMSException("TH EX:  Error Updating Marker Information", th);
			
		}
	}*/
	private void clearForm() throws GDMSException {
		
	}
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		listOfColumnsInTheTable = theListOfColumnsInTheTable;
	}

	public String getDataUploaded() {
		
		String strDataUploaded = "";
		
		if (0 < iCountOfExistingMarkers) {
			strDataUploaded = "Number of SSR Marker(s) already existing are: " + iCountOfExistingMarkers + "\n\n\n";
		}
		
		if (null != arrayOfMarkers && arrayOfMarkers.length > 0){
			String strUploadInfo = "";

			for (int i = 0; i < arrayOfMarkers.length; i++){
				Integer iMarkerId = arrayOfMarkers[i].getMarkerId();
				String strMarkerName = arrayOfMarkers[i].getMarkerName();
				String strMarker = "Marker: " + iMarkerId + ": " + strMarkerName;
				strUploadInfo += strMarker + "\n";
			}
			//strDataUploaded = "Uploaded SSR Marker(s): \n" + strUploadInfo;
			strDataUploaded += "New SSR Marker(s) uploaded: \n" + strUploadInfo;
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
