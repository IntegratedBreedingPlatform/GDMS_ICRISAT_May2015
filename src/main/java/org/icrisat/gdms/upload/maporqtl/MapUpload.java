package org.icrisat.gdms.upload.maporqtl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.generationcp.middleware.dao.gdms.DatasetDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.MapDetailElement;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerInfo;
import org.generationcp.middleware.pojos.gdms.MarkerOnMap;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.genotyping.MapBean;
import org.icrisat.gdms.upload.genotyping.MapMarkersBean;
import org.icrisat.gdms.upload.genotyping.MarkerInfoBean;
import org.icrisat.gdms.upload.marker.UploadField;

public class MapUpload implements UploadMarker {
	
	private GDMSMain _mainHomePage;

	private String strFileLocation;
	private Workbook workbook;
	private Sheet sheetMapDetails;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInSourceRowsFromSheet;
	private ArrayList<HashMap<String, String>> listOfDataInDataRowsFromSheet;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	private Marker marker;
	private MarkerOnMap markerOnMap;
	private Map map;
	private Marker[] arrayOfMarkers;
	private MarkerOnMap[] arrayOfMarkersOnMap;
	private Map[] arrayOfMaps;

	String strMapName ="";
	List chList = new ArrayList();
	int iNumOfRowsFromDataTable =0;
	int intRMarkerId=1;
	ManagerFactory factory;
    GenotypicDataManager genoManager;
    int maxMid=0;
    private Transaction tx;
	 int markeronMapId=0;
	private Session localSession;
	private Session centralSession;
	Integer datasetId = null;
	private ArrayList<String> markersListFromTemplate=new ArrayList<String>();
	String strErrorMsg="no";
	@Override
	public void readExcelFile() throws GDMSException {
		try {
			String ext=strFileLocation.substring(strFileLocation.lastIndexOf("."));
			if(ext.equals(".xls")){
				workbook = Workbook.getWorkbook(new File(strFileLocation));
				sheetMapDetails = workbook.getSheet(0);
				strSheetNames = workbook.getSheetNames();
			}else{
				throw new GDMSException("Please check the file, it should be in excel format");				
			}
		} catch (BiffException e) {
			throw new GDMSException("Error Reading Map Upload Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading Map Upload Sheet - " + e.getMessage());
		}
	}

	
	@Override
	public String validateDataInExcelSheet() throws GDMSException {

		if (false == strSheetNames[0].equalsIgnoreCase("map")){
			throw new GDMSException("Map Sheet Name Not Found");
		}

		Sheet mapSheet = workbook.getSheet(strSheetNames[0]);
		String strArrayOfReqColNames[] = {"Map Name", "Map Description", "Crop", "Map Unit"};

		for(int j = 0; j < strArrayOfReqColNames.length; j++){
			String strColFromSheet = (String)mapSheet.getCell(0, j).getContents().trim();
			if(!strArrayOfReqColNames[j].toLowerCase().contains(strColFromSheet.toLowerCase())){
				throw new GDMSException(strColFromSheet + " column not found");
			}
			if(strColFromSheet == null || strColFromSheet == ""){
				throw new GDMSException("Delete empty column " + strColFromSheet);
			}
		}	

		String strArrayOfReqCols2[] = {"Marker Name", "Linkage Group", "Position"};
		int intNoOfRows = mapSheet.getRows();
		for(int j = 0; j < strArrayOfReqCols2.length; j++){
			String strColNameFromSheet = (String)mapSheet.getCell(j, 5).getContents().trim();
			if(!strArrayOfReqCols2[j].toLowerCase().contains(strColNameFromSheet.toLowerCase())){
				throw new GDMSException(strColNameFromSheet + " column name not found.");
			}
			if(strColNameFromSheet==null || strColNameFromSheet==""){
				throw new GDMSException(strColNameFromSheet + " information required.");
			}
		}


		for(int j = 7; j < intNoOfRows; j++){
			String strMFieldValue = mapSheet.getCell(0, j).getContents().trim();
			String strLGFieldValue = mapSheet.getCell(1, j).getContents().trim();
			String strPosition = mapSheet.getCell(2, j).getContents().trim();
			if(strMFieldValue.equals("") && !strLGFieldValue.equals("")){
				throw new GDMSException("Marker Name at row " +  j + " is required field");
			}
			if(!strMFieldValue.equals("") && strLGFieldValue.equals("")){
				throw new GDMSException("Linkage Group at row " + j + " is required field");
			}
			if(!strMFieldValue.equals("") && strPosition.equals("")){
				throw new GDMSException("Position at row " + j + " is required field");
			}
			if(strMFieldValue.equals("") && strLGFieldValue.equals("") && strPosition.equals("")){
				String strRowNumber = String.valueOf(mapSheet.getCell(1, j).getRow()+1);								 
				String strErrMsg = "There is an empty row at position " + strRowNumber + ".\nPlease delete it.";
				throw new GDMSException(strErrMsg);
			}							 
		}
		return strErrorMsg;
	}

	@Override
	public void createObjectsToBeDisplayedOnGUI(GDMSMain theMainHomePage) throws GDMSException {
		
		_mainHomePage = theMainHomePage;
		
		listOfDataInSourceRowsFromSheet = new ArrayList<HashMap<String,String>>();

		HashMap<String, String> hmOfDataInSourceRows = new HashMap<String, String>();

		String strMapName = sheetMapDetails.getCell(1, 0).getContents().toString();
		hmOfDataInSourceRows.put(UploadField.MapName.toString(), strMapName);

		String strMapDescription = sheetMapDetails.getCell(1, 1).getContents().toString();
		hmOfDataInSourceRows.put(UploadField.MapDescription.toString(), strMapDescription);

		String strCrop = sheetMapDetails.getCell(1, 2).getContents().toString();
		hmOfDataInSourceRows.put(UploadField.Crop.toString(), strCrop);

		String strMapUnit = sheetMapDetails.getCell(1, 3).getContents().toString();
		hmOfDataInSourceRows.put(UploadField.MapUnit.toString(), strMapUnit);
		
		listOfDataInSourceRowsFromSheet.add(hmOfDataInSourceRows);

		
		int iNumOfRows = sheetMapDetails.getRows();
		
		listOfDataInDataRowsFromSheet = new ArrayList<HashMap<String,String>>();

		for(int i = 6; i < iNumOfRows; i++){
			
			HashMap<String, String> hashMapOfMapDataRow = new HashMap<String, String>();

			String strMarkerName = (String)sheetMapDetails.getCell(0, i).getContents().trim();
			hashMapOfMapDataRow.put(UploadField.MarkerName.toString(), strMarkerName);
			
			String strLinkageGroup = (String)sheetMapDetails.getCell(1, i).getContents().trim();
			hashMapOfMapDataRow.put(UploadField.LinkageGroup.toString(), strLinkageGroup);
			
			String strPosition = (String)sheetMapDetails.getCell(2, i).getContents().trim();
			hashMapOfMapDataRow.put(UploadField.Position.toString(), strPosition);
			
			listOfDataInDataRowsFromSheet.add(hashMapOfMapDataRow);
		}
	}

	@Override
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}

	@Override
	public void setDataToBeUploded(
			ArrayList<HashMap<String, String>> theListOfSourceDataRows,
			ArrayList<HashMap<String, String>> listOfDataRows,
			ArrayList<HashMap<String, String>> listOfGIDRows) {
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

		String strArrayOfReqColNames[] = {UploadField.MapName.toString(), UploadField.Crop.toString(), 
				UploadField.MapUnit.toString()};

		HashMap<String, String> hashMapOfSourceFieldsAndValues = listOfDataRowsFromSourceTable.get(0);
		for(int j = 0; j < strArrayOfReqColNames.length; j++) {
			String strReqSourceCol = strArrayOfReqColNames[j];
			if(false == hashMapOfSourceFieldsAndValues.containsKey(strReqSourceCol)){
				throw new GDMSException(strReqSourceCol + " column not found");
			} else {
				String strReqColValue = hashMapOfSourceFieldsAndValues.get(strReqSourceCol);
				if (null == strReqColValue || strReqColValue.equals("")){
					throw new GDMSException("Please provide value for " + strReqSourceCol);
				}
			}
		}	
		

		for(int j = 0; j < listOfDataRowsFromDataTable.size(); j++){
			
			HashMap<String, String> hashMapDataRows = listOfDataRowsFromDataTable.get(j);
			
			String strMFieldValue = hashMapDataRows.get(UploadField.MarkerName.toString());
			String strLGFieldValue = hashMapDataRows.get(UploadField.LinkageGroup.toString());
			String strPosition = hashMapDataRows.get(UploadField.Position.toString());
			markersListFromTemplate.add(strMFieldValue);
			if(strMFieldValue.equals("") && !strLGFieldValue.equals("")) {
				throw new GDMSException("Marker Name at row " +  (j+1) + " is required field");
			}
			if(!strMFieldValue.equals("") && strLGFieldValue.equals("")) {
				throw new GDMSException("Linkage Group at row " + (j+1) + " is required field");
			}
			if(!strMFieldValue.equals("") && strPosition.equals("")) {
				throw new GDMSException("Position at row " + (j+1) + " is required field");
			}
		}
	}

	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {
		List<DatasetElement> listOfDetailsByName ;
		 HashMap<String, Object> markersMap = new HashMap<String, Object>();
         //ArrayList lstMarIdNames=new ArrayList();
         List lstMarkers = new ArrayList();
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			genoManager=factory.getGenotypicDataManager();
	
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		 tx=localSession.beginTransaction();
		String strdatasetSelectedOnTheUI = GDMSModel.getGDMSModel().getDatasetSelected();
		int marker_id=0;
		Database instance = Database.LOCAL;
		try{
			long lastId = genoManager.getLastId(instance, GdmsTable.GDMS_MARKER);
			////System.out.println("testGetLastId(" + GdmsTable.GDMS_MARKER + ") in " + instance + " = " + lastId);
			marker_id=(int)lastId;
		}catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading Map");
		} 
		
		int intMaxVal=0;
		Object obj1=null;
		Iterator itList1=null;
		List listValues1=null;
		Query query1=localSession.createSQLQuery("select min(markeronmap_id) from gdms_markers_onmap");
		
		listValues1=query1.list();
		itList1=listValues1.iterator();
					
		while(itList1.hasNext()){
			obj1=itList1.next();
			if(obj1!=null)
				intMaxVal=Integer.parseInt(obj1.toString());
		}
		
		markeronMapId=intMaxVal-1;
		
		int maxLinkageMapId=0;	
		List listValues=new ArrayList();
		
		Object obj=null;
		
		Iterator itList=null;	
		
		try{
			List<Marker> resCenMarker=genoManager.getMarkersByMarkerNames(markersListFromTemplate, 0, markersListFromTemplate.size(), Database.CENTRAL);
			if(resCenMarker!=null){
				for(Marker resC:resCenMarker){
					if(!lstMarkers.contains(resC.getMarkerName())){
						lstMarkers.add(resC.getMarkerName());
		            	markersMap.put(resC.getMarkerName(), resC.getMarkerId());
					}
				}
			}
			List<Marker> resLocMarker=genoManager.getMarkersByMarkerNames(markersListFromTemplate, 0, markersListFromTemplate.size(), Database.LOCAL);
			if(resLocMarker!=null){
				for(Marker resL:resLocMarker){
					if(!lstMarkers.contains(resL.getMarkerName())){
						lstMarkers.add(resL.getMarkerName());
		            	markersMap.put(resL.getMarkerName(), resL.getMarkerId());
					}
				}
			}
		}catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading Map");
		}
		try{
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		Query query=localSession.createSQLQuery("select min(map_id) from gdms_map");
		
		listValues=query.list();
		itList=listValues.iterator();
					
		while(itList.hasNext()){
			obj=itList.next();
			if(obj!=null)
				maxLinkageMapId=Integer.parseInt(obj.toString());
		}
		
		
		
		int linkageMapID=maxLinkageMapId-1;
		
		String datasetSelectedOnTheUI = GDMSModel.getGDMSModel().getDatasetSelected();
		DatasetDAO datasetDAO = new DatasetDAO();
		datasetDAO.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession());
		
	//	System.out.println("datasetSelectedOnTheUI:"+datasetSelectedOnTheUI);
		
		if(null == datasetSelectedOnTheUI){
			////System.out.println(" empty");
			datasetId=0;
			
		}else{
			////System.out.println("..................    not Empty");
			try{
				 listOfDetailsByName = genoManager.getDatasetDetailsByDatasetName(datasetSelectedOnTheUI.toString(), Database.CENTRAL);
				if (listOfDetailsByName.isEmpty()) {
					listOfDetailsByName = genoManager.getDatasetDetailsByDatasetName(datasetSelectedOnTheUI.toString(), Database.LOCAL);
				}
				for(DatasetElement datasetElement: listOfDetailsByName){
					datasetId = datasetElement.getDatasetId();
				}
				
			}catch (MiddlewareQueryException e) {
				throw new GDMSException("Error uploading Map");
			}
			//20131111: Tulasi - Used the above code to get Map Id for the Map selected on the GUI for both Allelic and ABH data
		}
		
		
		iNumOfRowsFromDataTable = listOfDataRowsFromDataTable.size();

		ArrayList<String> listOfMarkersFromDataTable = new ArrayList<String>();
		for(int mCount = 0; mCount < iNumOfRowsFromDataTable; mCount++){
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(mCount);
			String strMarkerName = hashMapOfDataRow.get(UploadField.MarkerName.toString());
			listOfMarkersFromDataTable.add(strMarkerName);
		}
		
		arrayOfMarkersOnMap = new MarkerOnMap[iNumOfRowsFromDataTable];
		int iUploadedMarkerOnMapCount = 0;
		
		arrayOfMaps = new Map[iNumOfRowsFromDataTable];
		int iUploadedMapCount = 0;
		
		HashMap<String, String> hashMapOfDataFromSourceTable = listOfDataRowsFromSourceTable.get(0);
		
		strMapName = hashMapOfDataFromSourceTable.get(UploadField.MapName.toString());
		
		try{
			List<MapDetailElement> results =genoManager.getMapDetailsByName(strMapName, 0, (int)genoManager.countAllMapDetails());
			//if(results.isEmpty()){			
				//results =genoManager.getDatasetDetailsByDatasetName(strMapName, Database.LOCAL);
				if(! results.isEmpty())
					throw new GDMSException("Map already exists.");
			
		
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
		if(strMapName.length()>30){
			//ErrMsg = "Dataset Name value exceeds max char size.";
			throw new GDMSException("Map Name value exceeds max char size.");
		}
		
		
		
		String strMapUnit = hashMapOfDataFromSourceTable.get(UploadField.MapUnit.toString());
		String strMapType = "genetic";
		
		String mUnit="";
		if (strMapUnit.equalsIgnoreCase("cm")){
			strMapType="genetic";
			mUnit="cM";
		}else if (strMapUnit.equalsIgnoreCase("bp")){
			strMapType="sequence\\physical";
			mUnit="bp";
		}else{
			//String ErrMsg = "Error : Invalid Map Unit at cell position B4";
			throw new GDMSException("Error : Invalid Map Unit at cell position B4");
		}
		
		
		//System.out.println("datasetId:"+datasetId);
		String strMapDescription = UploadField.MapDescription.toString();
		MapBean map = new MapBean();
		map.setMap_id(linkageMapID);
		map.setMap_name(strMapName);
		map.setMap_type(strMapType);
		
		map.setMap_desc(strMapDescription);
		map.setMap_unit(mUnit);
		map.setMp_id(datasetId);
	
		localSession.save(map);
		
		int maxMarkerId=0;
		try{
			maxMarkerId=(int)genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_MARKER);
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading Map");
		} 
		maxMid=(int)maxMarkerId;
		HashMap<String, Object> finalHashMapMarkerAndIDs = new HashMap<String, Object>();
		// Marker Fields
		Integer iMarkerId = null; // Value will be set/overriden by the function 
		String strMarkerType = "UA";
		String strSpecies = hashMapOfDataFromSourceTable.get(UploadField.Crop.toString());
        
		//if(lstMarIdNames.size()==0){
			for(int f=0; f<markersListFromTemplate.size();f++){
				MarkerInfoBean mib=new MarkerInfoBean();
				if(lstMarkers.contains(markersListFromTemplate.get(f))){
					intRMarkerId=(Integer)(markersMap.get(markersListFromTemplate.get(f)));							
					//mids.add(intRMarkerId);
					finalHashMapMarkerAndIDs.put(markersListFromTemplate.get(f).toString(), intRMarkerId);
				}else{
					//maxMid=maxMid+1;
					maxMid=maxMid-1;
					intRMarkerId=maxMid;
					finalHashMapMarkerAndIDs.put(markersListFromTemplate.get(f).toString(), intRMarkerId);
					//mids.add(intRMarkerId);	
					mib.setMarkerId(intRMarkerId);
					mib.setMarker_type(strMarkerType);
					mib.setMarker_name(markersListFromTemplate.get(f).toString());
					//mib.setCrop(sheetSource.getCell(1,5).getContents());
					mib.setSpecies(strSpecies);
					
					localSession.save(mib);
					if (f % 1 == 0){
						localSession.flush();
						localSession.clear();
					}
				}
				
				
			}
		
		
		////System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		for(int i = 0; i < iNumOfRowsFromDataTable; i++){

			HashMap<String, String> hashMapOfDataFromDataTable = listOfDataRowsFromDataTable.get(i);
			MapMarkersBean marker_linkage = new MapMarkersBean();
			
			
			String strMarkerName = hashMapOfDataFromDataTable.get(UploadField.MarkerName.toString());
			String strLinkageGroup = hashMapOfDataFromDataTable.get(UploadField.LinkageGroup.toString());
			
			// MarkerOnMap Fields
			String strPosition = hashMapOfDataFromDataTable.get(UploadField.Position.toString());
			Float fStartPosition = Float.parseFloat(strPosition);
			Float fEndPosition = Float.parseFloat(strPosition);
			//System.out.println("fStartPosition="+fStartPosition+"   fEndPosition="+fEndPosition);
			if(!chList.contains(strLinkageGroup))
				chList.add(strLinkageGroup);
			
			marker_linkage.setMarkeronmap_id(markeronMapId);
			marker_linkage.setMarkerId(Integer.parseInt(finalHashMapMarkerAndIDs.get(strMarkerName).toString()));				
			marker_linkage.setMap_id(linkageMapID);
			marker_linkage.setLinkage_group(strLinkageGroup);			
			marker_linkage.setStart_position(fStartPosition);
			marker_linkage.setEnd_position(fEndPosition);	
			
			localSession.save(marker_linkage);
			markeronMapId--;
			if (i % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
			
			
			int iMarkersOnMapCount = iUploadedMarkerOnMapCount;
			arrayOfMarkersOnMap[iMarkersOnMapCount] = markerOnMap;
			iUploadedMarkerOnMapCount += 1;
			
			int iMapCount = iUploadedMapCount;
			//arrayOfMaps[iMapCount] = map;
			iUploadedMapCount += 1;
			////System.out.println("####################################################################################");
		}
		tx.commit();
		
	}
	
	@Override
	public void setListOfColumns(	ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDataUploaded() {
		String strDataUploaded = "";
		
		
		strDataUploaded="Uploaded MAP '"+ strMapName+"' with "+iNumOfRowsFromDataTable+" marker(s) on "+chList.size()+" chromosome(s)";
		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromSourceSheet() {
		return listOfDataInSourceRowsFromSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromDataSheet() {
		return listOfDataInDataRowsFromSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet() {
		// TODO Auto-generated method stub
		return null;
	}

}
