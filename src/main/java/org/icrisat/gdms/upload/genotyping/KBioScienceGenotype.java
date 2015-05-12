package org.icrisat.gdms.upload.genotyping;

import java.io.BufferedReader;
import java.io.File;
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




import org.generationcp.middleware.dao.UserDAO;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.GenotypicDataManagerImpl;
import org.generationcp.middleware.manager.GetGermplasmByNameModes;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.GermplasmNameDetails;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.gdms.AccMetadataSet;
//import org.generationcp.middleware.pojos.gdms.AccMetadataSetPK;
import org.generationcp.middleware.pojos.gdms.CharValues;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.DatasetUsers;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.generationcp.middleware.pojos.gdms.SNPDataRow;
import org.generationcp.middleware.pojos.workbench.Project;
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
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.marker.UploadField;

import com.vaadin.ui.Window;

public class KBioScienceGenotype implements  UploadMarker {
	
	private GDMSMain _mainHomePage;
	
	
	private String strFileLocation;
	//private Workbook workbook;
	//private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet = new ArrayList<HashMap<String,String>>();
	
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	private ArrayList<HashMap<String, String>> listOfDataInSourceLines;
	private ArrayList<HashMap<String, String>> listOfDataInDataLines;
	
	
	private HashMap<String, String> hmOfDataInSourceSheet = new HashMap<String, String>();
	private BufferedReader bReader;
	private HashMap<Integer, String> hmOfColIndexAndGermplasmName;
	private HashMap<String, String> hmOfData;
	private int iDataRowIndex;
	
	private Marker addedMarker;
	private DatasetBean dataset;
	private AccessionMetaDataBean accMetadataSet;
	//private AccMetadataSet accMetadataSet1;
	private MarkerMetadataSet markerMetadataSet;
	private GenotypeUsersBean datasetUser;
	private CharValues charValues;
	
	private Session localSession;
	private Session centralSession;
	
	private Session session;
	
	/*private Session sessionL;
	private Session sessionC;
*/
	ManagerFactory factory =null;
	GenotypicDataManager genoManager;
	List<SNPDataRow> listOfSNPDataRows; 
	ArrayList<String> listOfMarkers = new ArrayList<String>();
	
	ArrayList<String> listOfMarkersFromTemplate = new ArrayList<String>();
	ArrayList<String> listOfEntriesFromTemplate = new ArrayList<String>();
	private List<List<String>> listOfGenoData = new ArrayList<List<String>>();
	private String[] strArrayOfGenotypes;
	List gDupNameList;
	GermplasmDataManager manager ;
	String notMatchingDataDB="";
	String alertGN="no";
    String alertGID="no";
    String notMatchingData="";
    int size=0;
    String notMatchingGIDS="";
    int iDatasetId = 0;
    int maxMid=0;
	int mid=0;
    private Transaction tx;
    ArrayList finalList =new ArrayList();
	int iUploadedMarkerCount = 0;
	String charData="";
	 int intRMarkerId = 1;
	 int intMaxAccId=0;
		int intMaxMarMetaId=0;
	// ArrayList<String> listOfGermplasmNames = new ArrayList<String>();
		//HashMap<Integer, String> hashMapOfGIDandGName = new HashMap<Integer, String>();		
		HashMap<Integer, Integer> hashMapOfGIDsandNIDs = new HashMap<Integer, Integer>();
		List<Marker> listOfMarkersFromDB = null;		
		Name names = null;
		ArrayList gidL=new ArrayList();
		List<Integer> nameIdsByGermplasmIds =new ArrayList();
	
	 HashMap<String, Object> markersMap = new HashMap<String, Object>();	
	 
	static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>();
	
	ArrayList<Integer> previousAccGID;
	//ArrayList<String> listOfMarkers = new ArrayList<String>();
	
	private HashMap<String, String> hmOfDuplicateGermNames;
	private HashMap<String, String> hmOfDuplicateGNames;
	List gNames;
	//List gDupNameList;
	List gDupNameListV;
	
	//private List<List<String>> listOfGenoData = new ArrayList<List<String>>();
	ArrayList<String> dupGermplasmsList=new ArrayList<String>();
	
	String strErrorMsg="no";
	
	boolean dupGermConfirmation;
	@Override
	public void readExcelFile() throws GDMSException {
		
		try {
			String ext=strFileLocation.substring(strFileLocation.lastIndexOf("."));
			if(ext.equals(".csv")){
				bReader = new BufferedReader(new FileReader(strFileLocation));
			
			}else{
				throw new GDMSException("Please check the file, it should be a LGC Genomics grid file");
				//return;
			}
		} catch (FileNotFoundException e) {
			throw new GDMSException(e.getMessage());
		} 
	}

	@Override
	public String validateDataInExcelSheet() throws GDMSException {		
		String strLine = "";
		boolean bDataStarts = false;
		ArrayList<String> germNames=new ArrayList<String>();
		int c=2;
		ArrayList<String> gNamesList=new ArrayList<String>();
		//int c=2;
		hmOfDuplicateGermNames=new HashMap<String, String>();
		hmOfDuplicateGNames=new HashMap<String, String>();
		gDupNameList=new ArrayList<String>();
		int r=1;
		
		try {
			while ((strLine = bReader.readLine()) != null) {
				
				if (strLine.startsWith("Project number")) {
					//3System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");	
					
					if (strArrayOfTokens.length > 1){
						hmOfDataInSourceSheet.put(UploadField.ProjectNumber.toString(), strArrayOfTokens[1]);
					}
				} else if (strLine.startsWith("Order number")) {
					//System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");
					if (strArrayOfTokens.length > 1){
						hmOfDataInSourceSheet.put(UploadField.OrderNumber.toString(), strArrayOfTokens[1]);
					}
				} else if (strLine.startsWith("Plates")) {
					//System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");
					if (strArrayOfTokens.length > 1){
						hmOfDataInSourceSheet.put(UploadField.Plates.toString(), strArrayOfTokens[1]);
					}
				}
				int iNumOfTokens=0;
				if(strLine.startsWith("DNA\\Assay") || strLine.startsWith("DNA \\ Assay") || strLine.startsWith("Sample Name")) {					
					//System.out.println(strLine);
					String[] strArrayOfTokens = strLine.split(",");	
					
					hmOfColIndexAndGermplasmName = new HashMap<Integer, String>();
					for(int iColIndex = 0; iColIndex < strArrayOfTokens.length; iColIndex++){
						String strMarkerName = strArrayOfTokens[iColIndex];
						hmOfColIndexAndGermplasmName.put(iColIndex, strMarkerName);
						if(!(strMarkerName.equalsIgnoreCase("DNA\\Assay") || strMarkerName.equalsIgnoreCase("DNA \\ Assay") || strMarkerName.equalsIgnoreCase("Sample Name")) )					
							listOfMarkersFromTemplate.add(strMarkerName);
					}					
					bDataStarts = true;					
				} else {	
					//System.out.println("hmOfColIndexAndGermplasmName:"+hmOfColIndexAndGermplasmName);
					/*if (null == hmOfData) {
						hmOfData = new HashMap<String, String>();
						listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();
					}*/
					
					if (bDataStarts) {
						//System.out.println(strLine);
						String[] strArrayOfTokens = strLine.split(",");
						String strDNAName = strArrayOfTokens[0]; 
						
						if(listOfEntriesFromTemplate.contains(strDNAName)){							
							gDupNameList.add(strDNAName);							
						}
						listOfEntriesFromTemplate.add(strDNAName);
						iNumOfTokens = strArrayOfTokens.length;
						//iGCount = iNumOfTokens;
						for(int g = 1; g < iNumOfTokens; g++){
							strArrayOfGenotypes = strArrayOfTokens;							
						}
						
						listOfGenoData.add(Arrays.asList(strArrayOfTokens)); 
						/*HashMap<String, String> hmOfData = new HashMap<String, String>();
						
						hmOfData.put(UploadField.DNA.toString(), strDNAName);
						
						for(int iColIndex = 1; iColIndex < strArrayOfTokens.length; iColIndex++){
							String strValue = strArrayOfTokens[iColIndex];
							String strDNA = hmOfColIndexAndGermplasmName.get(iColIndex);
							hmOfData.put(strDNA, strValue);
						}
						
						listOfDataInDataSheet.add(hmOfData);*/
					}
				}
			}
			//System.out.println("hmOfDataInSourceSheet:"+hmOfDataInSourceSheet);
			String strGName="";
			int rep=1;
			hmOfDuplicateGermNames=new HashMap<String, String>();
			hmOfDuplicateGNames=new HashMap<String, String>();
			for(int g=0;g<listOfEntriesFromTemplate.size();g++){
				if(gDupNameList.contains(listOfEntriesFromTemplate.get(g))){
					strGName=listOfEntriesFromTemplate.get(g)+" (Sample "+rep+")";
					
					hmOfDuplicateGNames.put(listOfEntriesFromTemplate.get(g)+rep, strGName);
					hmOfDuplicateGermNames.put(strGName, listOfEntriesFromTemplate.get(g));
					//dupGermplasmsList.add(strGName);
					rep++;
				}
			}
			listOfDataInSourceSheet.add(hmOfDataInSourceSheet);
			
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}	
		/*System.out.println("hmOfDuplicateGermNames:"+hmOfDuplicateGermNames);
		System.out.println("hmOfDuplicateGNames:"+hmOfDuplicateGNames);
		System.out.println("listOfEntriesFromTemplate:"+listOfEntriesFromTemplate);*/
		
		if(listOfMarkersFromTemplate.size()>256 || listOfEntriesFromTemplate.size()>256){
			//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			strErrorMsg = "Data to be uploaded cannot be displayed in the table. Please click on Upload to upload the data directly";
			
		}
		
		
		return strErrorMsg;
	}

	@Override
	public void createObjectsToBeDisplayedOnGUI(GDMSMain theMainHomePage) throws GDMSException {
		_mainHomePage = theMainHomePage;
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
		//System.out.println("gDupNameList:"+gDupNameList);
			listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();
			int d=0;
			int r=1;
			for (int i = 0; i < listOfEntriesFromTemplate.size(); i++){
				String strDNAName=listOfEntriesFromTemplate.get(i);
				//System.out.println("strDNAName:"+strDNAName);
				HashMap<String, String> hmOfData = new HashMap<String, String>();
								
				hmOfData.put(UploadField.DNA.toString(), strDNAName);
				
				if(gDupNameList.contains(strDNAName)){
					hmOfData.put(UploadField.DNA.toString(), hmOfDuplicateGNames.get(strDNAName+r));
					r++;
				}else		
					hmOfData.put(UploadField.DNA.toString(), strDNAName);
				
				
				List<String> list=listOfGenoData.get(d);
				for(int iColIndex = 1; iColIndex < hmOfColIndexAndGermplasmName.size(); iColIndex++){
					String strValue = list.get(iColIndex);
					String strDNA = hmOfColIndexAndGermplasmName.get(iColIndex);
					hmOfData.put(strDNA, strValue);
				}
				
				listOfDataInDataSheet.add(hmOfData);
				d++;
			}
			//System.out.println("listOfDataInDataSheet:"+listOfDataInDataSheet);
		
		
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
		// TODO Auto-generated method stub
		/*System.out.println("theListOfSourceDataRows:"+theListOfSourceDataRows);
		System.out.println("listOfDataRows:"+listOfDataRows);
		System.out.println("listOfGIDRows:"+listOfGIDRows);
		*/
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
		// TODO Auto-generated method stub
	}

	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {
		//GDMSModel.getGDMSModel().setDatasetSelected
		String strDatasetSelected = GDMSModel.getGDMSModel().getDatasetSelected();
		String strGermplasmSelected = GDMSModel.getGDMSModel().getGermplasmSelected();
		// TODO Auto-generated method stub
		/*localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		
		*/
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		//sessionL=localSession.getSessionFactory().openSession();	
        //sessionC=centralSession.getSessionFactory().openSession();
		
		//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%  :"+GDMSModel.getGDMSModel().getLocalParams().);
		manager = factory.getGermplasmDataManager();
		genoManager=factory.getGenotypicDataManager();
		//GermplasmListManager listM = factory.getGermplasmListManager();
		HashMap<String, Integer> hashMapOfEntryIDandGID = new HashMap<String, Integer>();
		HashMap<String, Integer> hashMapOfGNameandGID = new HashMap<String, Integer>();
		
		ArrayList<Integer> listOfGIDFromTable = new ArrayList<Integer>();
		ArrayList<String> listOfGNamesFromTable = new ArrayList<String>();
		
		tx=localSession.beginTransaction();
		int list_Id=0;
		int listID_C=0;
		int listID_L=0;
		ArrayList listEntries=new ArrayList();
		//System.out.println("strGermplasmSelected:"+strGermplasmSelected);
		//try{
		
			String strQuerry="select listid from listnms where listname='"+strGermplasmSelected+"'";
			
			
			
			//System.out.println(strQuerry);
			//ArrayList<String> listOfGermplasmLists = new ArrayList<String>();
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
			SQLQuery queryL=localSession.createSQLQuery(strQuerry);		
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
				SQLQuery queryC=centralSession.createSQLQuery(strQuerry);		
				queryC.addScalar("listid",Hibernate.INTEGER);;	
				newListC=queryC.list();			
				itListC=newListC.iterator();			
				while(itListC.hasNext()){
					obj=itListC.next();
					if(obj!=null)		
						list_id=Integer.parseInt(obj.toString());				
				}
					
			
			}	
			
			
		/*List<GermplasmList> listsC = listM.getGermplasmListByName(strGermplasmSelected, 0, 5, Operation.EQUAL, Database.CENTRAL);
		System.out.println("testGetGermplasmListByName(" + listsC + ") RESULTS: ");
		for (GermplasmList list : listsC) {
			System.out.println("  " + list);
			listID_C=list.getId();
	    }
		List<GermplasmList> listsL = listM.getGermplasmListByName(strGermplasmSelected, 0, 5, Operation.EQUAL, Database.LOCAL);
		System.out.println("testGetGermplasmListByName(" + listsL + ") RESULTS: ");
		for (GermplasmList list : listsL) {
			System.out.println("  " + list.getId()+"   "+list.getType());
			listID_L=list.getId();
	    }
		if(listID_C != 0)
			list_Id=listID_C;
		else if(listID_L != 0)
			list_Id=listID_L;*/
		//list_Id=list_id;
		
		
		//System.out.println("hashMapOfGNameandGID=:"+hashMapOfGNameandGID);
		String nonExistingListItem="";
		String nonExistingListItems="no";
		String markers ="";
		ArrayList<String> listOfMarkerNamesFromSourceTable = new ArrayList<String>();
		ArrayList<String> listOfDNANamesFromSourceTable = new ArrayList<String>();
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){
			HashMap<String,String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);
			//System.out.println("hashMapOfDataRow=:"+hashMapOfDataRow);
			listOfDNANamesFromSourceTable.add(hashMapOfDataRow.get(UploadField.DNA.toString()));
			//System.out.println("listOfDNANamesFromSourceTable=:"+listOfDNANamesFromSourceTable);
			Iterator<String> iterator = hashMapOfDataRow.keySet().iterator();
			while(iterator.hasNext()){
				String strMarkerNameFromSourceTable = iterator.next();
				//System.out.println("888888888888888888888:"+UploadField.DNA.toString());
				if (false == (strMarkerNameFromSourceTable.equals(UploadField.DNA.toString()) )){
					if( (false ==  listOfMarkerNamesFromSourceTable.contains(strMarkerNameFromSourceTable))&&(strMarkerNameFromSourceTable != "SNo")){
						listOfMarkerNamesFromSourceTable.add(strMarkerNameFromSourceTable);
						markers = markers +"'"+ strMarkerNameFromSourceTable+"',";
					}
				}
			}
		}
		
		
		//System.out.println("list_Id=:"+list_id);
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
		for(int w=0;w<listData.size();w++){
        	Object[] strMareO= (Object[])listData.get(w);
           // System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]+"   "+strMareO[2]);
        	/*if(hmOfDuplicateGermNames.containsKey(listOfDNANamesFromSourceTable.get(k)))
				strEntry=hmOfDuplicateGermNames.get(listOfDNANamesFromSourceTable.get(k));
			else
				strEntry=listOfDNANamesFromSourceTable.get(k);*/
        	if(listOfEntriesFromTemplate.contains(strGermplasmSelected+"-" + strMareO[0].toString())){
        	 	listEntries.add(strGermplasmSelected+"-" + strMareO[0].toString());
	            listOfGNamesFromTable.add(strMareO[1].toString().trim());
	            listOfGIDFromTable.add(Integer.parseInt(strMareO[2].toString()));
	            hashMapOfGNameandGID.put(strMareO[1].toString().trim(), Integer.parseInt(strMareO[2].toString()));
	            hashMapOfEntryIDandGID.put(strGermplasmSelected+"-" + strMareO[0].toString(), Integer.parseInt(strMareO[2].toString()));
        	}
 		}
		/*System.out.println("hashMapOfGNameandGID:"+hashMapOfGNameandGID);
		System.out.println("hashMapOfEntryIDandGID:"+hashMapOfEntryIDandGID);
		System.out.println("........:"+listOfGNamesFromTable);*/
		String strEntry="";
		//if(listOfDNANamesFromSourceTable.size()!=listEntries.size()){
			for(int k=0;k<listOfDNANamesFromSourceTable.size();k++){
				strEntry="";
				if(hmOfDuplicateGermNames.containsKey(listOfDNANamesFromSourceTable.get(k)))
					strEntry=hmOfDuplicateGermNames.get(listOfDNANamesFromSourceTable.get(k));
				else
					strEntry=listOfDNANamesFromSourceTable.get(k);
				if(!(listEntries.contains(strEntry))){
					 nonExistingListItem=nonExistingListItem+listOfDNANamesFromSourceTable.get(k)+"\n";
					 nonExistingListItems="yes";
				}
			}
			if(nonExistingListItems.equalsIgnoreCase("yes")){
				throw new GDMSException("Please verify the List Entries provided doesnot exist in the database\t "+nonExistingListItem );
			}
		//}
						
			List newMarkersListL=new ArrayList();
			List newMarkersListC=new ArrayList();
			//try {	
			Object objM=null;
			Object objML=null;
			Iterator itListMC=null;
			Iterator itListML=null;
			//genoManager.getMar
			
			List lstMarkers = new ArrayList();
			String markersForQuery="";
			/** retrieving maximum marker id from 'marker' table of database **/
			//int maxMarkerId=uptMId.getMaxIdValue("marker_id","gdms_marker",session);
			
			HashMap<String, Object> markersMap = new HashMap<String, Object>();	
			markersForQuery=markers.substring(0, markers.length()-1);
			
			String strQuerryM="select distinct marker_id, marker_name from gdms_marker where Lower(marker_name) in ("+markersForQuery.toLowerCase()+")";
			
			//sessionC=centralSession.getSessionFactory().openSession();			
			SQLQuery queryMC=centralSession.createSQLQuery(strQuerryM);	
			queryMC.addScalar("marker_id",Hibernate.INTEGER);	
			queryMC.addScalar("marker_name",Hibernate.STRING);	
			newMarkersListC=queryMC.list();			
			itListMC=newMarkersListC.iterator();			
			while(itListMC.hasNext()){
				objM=itListMC.next();
				if(objM!=null){	
					Object[] strMareO= (Object[])objM;
		        	//System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
					lstMarkers.add(strMareO[1].toString());
					markersMap.put(strMareO[1].toString(), strMareO[0]);
					
				}
			}
					

			//sessionL=localSession.getSessionFactory().openSession();			
			SQLQuery queryML=localSession.createSQLQuery(strQuerryM);		
			queryML.addScalar("marker_id",Hibernate.INTEGER);	
			queryML.addScalar("marker_name",Hibernate.STRING);      
			newMarkersListL=queryML.list();
			itListML=newMarkersListL.iterator();			
			while(itListML.hasNext()){
				objML=itListML.next();
				if(objML!=null)	{			
					Object[] strMareO= (Object[])objML;
					//System.out.println("W=....."+w+"    "+strMareO[0]+"   "+strMareO[1]);
					if(!lstMarkers.contains(strMareO[1].toString())){
	            		lstMarkers.add(strMareO[1].toString());	            		
	            		markersMap.put(strMareO[1].toString(), strMareO[0]);	
					}
				}
			}
			
			/*try{
				System.out.println("$$$$$$  >>>>>>>>>>>>   :"+manager.getGidAndNidByGermplasmNames(listOfGNamesFromTable));
			} catch (MiddlewareQueryException e1) {
				throw new GDMSException(e1.getMessage());
			}*/
		ArrayList gidsDBList = new ArrayList();
		ArrayList gNamesDBList = new ArrayList();
		hashMap.clear();
		
		//listOfGermplasmNames = new ArrayList<String>();
	//	hashMapOfGIDandGName = new HashMap<Integer, String>();		
		hashMapOfGIDsandNIDs = new HashMap<Integer, Integer>();
		listOfMarkersFromDB = null;		
		names = null;
		gidL=new ArrayList();
		nameIdsByGermplasmIds =new ArrayList();
		
		
		try{
			List<GermplasmNameDetails> germplasmList = manager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.NORMAL);				
			for (GermplasmNameDetails g : germplasmList) {				
	        	if(!(gidsDBList.contains(g.getNameId()))){
	        		gidsDBList.add(g.getNameId());
	        		gNamesDBList.add(g.getNVal());
	        		addValues(g.getNVal(), g.getGermplasmId());		
	        		gidL.add( g.getGermplasmId());
	        		hashMapOfGIDsandNIDs.put( g.getGermplasmId(), g.getNameId());
	        	}	          
	        }
			
			if(gNamesDBList.size()!=listOfGNamesFromTable.size()){
				germplasmList = manager.getGermplasmNameDetailsByGermplasmNames(listOfGNamesFromTable, GetGermplasmByNameModes.STANDARDIZED);
				for (GermplasmNameDetails g : germplasmList) {
					if(!(gidsDBList.contains(g.getNameId()))){
		        		gidsDBList.add(g.getNameId());
		        		gNamesDBList.add(g.getNVal());
		        		addValues(g.getNVal(), g.getGermplasmId());	
		        		gidL.add( g.getGermplasmId());
		        		hashMapOfGIDsandNIDs.put( g.getGermplasmId(), g.getNameId());
		        	}        	
		          
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
					//hashMapOfGNameandGID.put(g.getNVal().toLowerCase(), g.getGermplasmId());
					//nameIdsByGermplasmIds.add(g.getNameId());
		        }
			}
			
			
		} catch (MiddlewareQueryException e1) {
			throw new GDMSException(e1.getMessage());
		}	
		
		
		//System.out.println(gNamesDBList);
		/*if(gNamesDBList.size()>0){
			for(int n=0;n<listOfGNamesFromTable.size();n++){
	 		   if(gNamesDBList.contains(listOfGNamesFromTable.get(n))){
	 			   if(!(hashMap.get(listOfGNamesFromTable.get(n).toString()).contains(hashMapOfGNameandGID.get(listOfGNamesFromTable.get(n).toString())))){
	 				   notMatchingData=notMatchingData+listOfGNamesFromTable.get(n)+"   "+hashMapOfGNameandGID.get(listOfGNamesFromTable.get(n).toString())+"\n\t";
	 				   notMatchingDataDB=notMatchingDataDB+listOfGNamesFromTable.get(n)+"="+hashMap.get(listOfGNamesFromTable.get(n))+"\t";
		        		   alertGN="yes";
	 			   }
	 		   }else{
	 			   //int gid=GIDsMap.get(gnamesList.get(n).toString());
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
		
	    for(int a=0;a<listOfEntriesFromTemplate.size();a++){
			String strEntry1=listOfEntriesFromTemplate.get(a);
			int gid1=hashMapOfEntryIDandGID.get(strEntry1);
        	
        		finalList.add(gid1+"~!~"+hashMapOfGIDsandNIDs.get(gid1));	
        
        }	
		
		/*for(int a=0;a<listOfGIDFromTable.size();a++){
        	int gid1=Integer.parseInt(listOfGIDFromTable.get(a).toString());
        	if(gidL.contains(gid1)){
        		finalList.add(gid1+"~!~"+hashMapOfGIDsandNIDs.get(gid1));	
        	}
        }
		*/
			//System.out.println("hashMapOfGIDsandNIDs:"+hashMapOfGIDsandNIDs);
			/** 
			 * 20130813
			 */
			/*if (null == nameIdsByGermplasmIds){
				throw new GDMSException("Error retrieving list of NIDs for given GIDs. Please provide valid GIDs.");
			} 
			
			if (0 == nameIdsByGermplasmIds.size()){
				throw new GDMSException("Name IDs do not exist for given GIDs. Please provide valid GIDs.");
			}*/
			

		//Integer iDatasetId = 0; //Will be set/overridden by the function
		String strDataType = "char"; 
		Date uploadTemplateDate = new Date(System.currentTimeMillis());
		String strCharValue = "CV";
		String strDatasetType = "SNP";
		String method = null;
		String strScore = null;
		Integer iACId = null;
		String strRemarks = "";

		HashMap<String, String> hashMapOfSourceDataRow = listOfDataRowsFromSourceTable.get(0);
		
		String strPIFromSourceTable = hashMapOfSourceDataRow.get(UploadField.PI.toString());
		//String strDatasetNameFromSourceTable = hashMapOfSourceDataRow.get(UploadField.DatasetName.toString());
		String strDatasetDescFromSourceTable = hashMapOfSourceDataRow.get(UploadField.DatasetDescription.toString());
		String strGenusFromSourceTable = "Groundnut";
		String strSpeciesFromSourceTable = "Groundnut";
		String strMissingDataFromSourceTable = "-";
		
		//GDMSModel.getGDMSModel().getWorkbenchDataManager().get
		
		
		int marker_id=0;
		Database instance = Database.LOCAL;
		 long datasetLastId = 0;
		 long lastId = 0;
		
		Integer iUserId = 0;
		
		try {
			
			iUserId=GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId();
			Project resW = GDMSModel.getGDMSModel().getWorkbenchDataManager().getLastOpenedProject(GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId());
			//System.out.println(",,,,,,,,,,,,,,,,,,:"+resW.getCropType().getCropName());
			strSpeciesFromSourceTable=resW.getCropType().getCropName();
			strGenusFromSourceTable=resW.getCropType().getCropName();
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		//System.out.println("listOfDataRowsFromDataTable:"+listOfDataRowsFromDataTable);
		
		try{
			List<DatasetElement> results =genoManager.getDatasetDetailsByDatasetName(strDatasetSelected, Database.CENTRAL);
			if(results.isEmpty()){			
				results =genoManager.getDatasetDetailsByDatasetName(strDatasetSelected, Database.LOCAL);
				if(results.size()>0)
					throw new GDMSException("Dataset Name already exists.");
			}else 
				throw new GDMSException("Dataset Name already exists.");
		
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		if(strDatasetSelected.length()>30){
			//ErrMsg = "Dataset Name value exceeds max char size.";
			throw new GDMSException("Dataset Name value exceeds max char size.");
		}
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
	 
	 
		
		
		
		int iNumOfMarkers = listOfMarkerNamesFromSourceTable.size();
		int iNumOfGIDs = listOfGIDFromTable.size();
		//arrayOfMarkers = new Marker[iNumOfMarkers*iNumOfGIDs];
		
		Integer accSampleId=1;
		Integer markerSampleId=1;
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
		
		

		//System.out.println("strDataType=:"+strDataType);
		dataset = new DatasetBean();
		dataset.setDataset_id(iDatasetId);
		dataset.setDataset_name(strDatasetSelected);
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
		//System.out.println(".........................  finalList:"+finalList);
		
		previousAccGID=new ArrayList<Integer>();
		int  accSampleIdA=1;
		
		for(int i=0;i<finalList.size();i++){	
        	String[] strList=finalList.get(i).toString().split("~!~");
        	Integer iGId =Integer.parseInt(strList[0].toString());
        	if(previousAccGID.contains(iGId)){
        		//accSampleIdA=2;
        		accSampleId=accSampleId+1;
			}else{
				accSampleIdA=1;
			}
        	
        	
        	accMetadataSet=new AccessionMetaDataBean();					
			//******************   GermplasmTemp   *********************//*
        	accMetadataSet.setAccMetadatasetId(intMaxAccId);
        	accMetadataSet.setDatasetId(iDatasetId);
        	accMetadataSet.setGid(Integer.parseInt(strList[0].toString()));
        	accMetadataSet.setNid(Integer.parseInt(strList[1].toString()));
        	accMetadataSet.setAccSampleId(accSampleIdA);
        	localSession.save(accMetadataSet);
			
			if (i % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
        	if(!previousAccGID.contains(iGId))
        		previousAccGID.add(iGId);
        	intMaxAccId--; 
        }
		 ArrayList mids=new ArrayList();
         
         HashMap<String, Object> finalHashMapMarkerAndIDs = new HashMap<String, Object>();
       
		for(int f=0; f<listOfMarkerNamesFromSourceTable.size();f++){
			MarkerInfoBean mib=new MarkerInfoBean();
			if(lstMarkers.contains(listOfMarkerNamesFromSourceTable.get(f))){
				intRMarkerId=(Integer)(markersMap.get(listOfMarkerNamesFromSourceTable.get(f)));							
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
		
		
		
		
		//System.out.println("hashMapOfEntryIDandGID=:"+hashMapOfEntryIDandGID);
		//listOfSNPDataRows = new ArrayList<SNPDataRow>();
		//System.out.println("listOfDataRowsFromDataTable=:"+listOfDataRowsFromDataTable.size());
		
		ArrayList<Integer> previousGID=new ArrayList<Integer>();
		ArrayList<Integer> previousMID=new ArrayList<Integer>();		
		listOfMarkers=new ArrayList();
		
		for (int i = 0; i < listOfDataRowsFromDataTable.size(); i++){	
			String strGenotype="";
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(i);	
			
			//Integer strGID = Integer.parseInt(hashMapOfEntryIDandGID.get(hashMapOfDataRow.get(UploadField.DNA.toString())).toString());
			
			strEntry = hashMapOfDataRow.get(UploadField.DNA.toString());
			if(hmOfDuplicateGermNames.containsKey(strEntry))
				strGenotype=hmOfDuplicateGermNames.get(strEntry);
			else
				strGenotype=strEntry;
			Integer strGID = hashMapOfEntryIDandGID.get(strGenotype);
			Integer iNameId = hashMapOfGIDsandNIDs.get(strGID);
			if(previousGID.contains(strGID)){//.equals(previousGID)){
				//accSampleId=2;
				accSampleId=accSampleId+1;
			}else
				accSampleId=1;
			
			iACId--;	
			previousMID=new ArrayList<Integer>();
			for (int j = 0; j < listOfMarkerNamesFromSourceTable.size(); j++) {					
				strCharValue=hashMapOfDataRow.get(listOfMarkerNamesFromSourceTable.get(j));					
				String strMarkerName = listOfMarkerNamesFromSourceTable.get(j);	
				
				if(i==0){
					listOfMarkers.add(strMarkerName.toLowerCase());
				}
				
				int markerId=Integer.parseInt(finalHashMapMarkerAndIDs.get(strMarkerName.toLowerCase()).toString());
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
				//System.out.println(strCharValue+"   "+strMarkerName+"   "+hashMapOfDataRow.get(UploadField.DNA.toString()));
				if(strCharValue.length()>2){
					String charStr=strCharValue;
					if(charStr.contains(":")){
						String str1="";
						String str2="";
						//String charStr=strCharValue;
						str1=charStr.substring(0, charStr.length()-2);
						str2=charStr.substring(2, charStr.length());
						charData=str1+"/"+str2;
					}else if(charStr.contains("/")){
						charData=charStr;
					}else if((charStr.equalsIgnoreCase("DUPE"))||(charStr.equalsIgnoreCase("BAD"))){
						charData="?";
					}else{
						throw new GDMSException("Heterozygote data representation should be either : or /"+charStr);
						/* ErrMsg = "Heterozygote data representation should be either : or /"+charStr;
						 request.getSession().setAttribute("indErrMsg", ErrMsg);
						 return "ErrMsg";*/	 
					}
					
				}else if(strCharValue.length()==2){
					String str1="";
					String str2="";
					String charStr=strCharValue;
					str1=charStr.substring(0, charStr.length()-1);
					str2=charStr.substring(1);
					charData=str1+"/"+str2;
					//System.out.println(".....:"+strCharValue.substring(1));
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
				charValues.setMarker_id(markerId);
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
		
		int markerSampleIdMMD=1;
		ArrayList<Integer> previousMIDMMD=new ArrayList<Integer>();
		for(int m1=0;m1<listOfMarkers.size();m1++){	
			intMaxMarMetaId--;
			
			int mid=Integer.parseInt(finalHashMapMarkerAndIDs.get(listOfMarkers.get(m1)).toString());
			if(previousMIDMMD.contains(mid)){
				//markerSampleIdMMD=2;
				markerSampleIdMMD=markerSampleIdMMD+1;
			}else
				markerSampleIdMMD=1;
			//System.out.println("gids doesnot Exists    :"+lstgermpName+"   "+gids[l]);
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

	
	/*protected void saveSNPGenotype() throws GDMSException {
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		
		genoManager=factory.getGenotypicDataManager();
		try {
			//genoManager.setSNP(accMetadataSet1, markerMetadataSet, datasetUser, charValues, dataset, addedMarker);
			//genotypicDataManagerImpl.setSNP(accMetadataSet, markerMetadataSet, datasetUser, charValues, dataset);
			
			setSNP(Dataset dataset,
		               DatasetUsers datasetUser,
		               List<SNPDataRow> rows)
		               throws MiddlewareQueryException
			
			//20131214: Tulasi
			genoManager.setSNP(dataset, datasetUser, listOfSNPDataRows);
		             
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading SNP Genotype");
		} catch(Exception e1) {
			e1.printStackTrace();
			
		}catch (Throwable th){
			throw new GDMSException("Error uploading SNP Genotype", th);
		} 
		
	}*/
	
	
	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDataUploaded() {
		String strDataUploaded = "";
		/*if (null != arrayOfMarkers && arrayOfMarkers.length > 0){
			String strUploadInfo = "";

			for (int i = 0; i < arrayOfMarkers.length; i++){
				Integer iMarkerId = arrayOfMarkers[i].getMarkerId();
				String strMarkerName = arrayOfMarkers[i].getMarkerName();
				String strGID = arrayOfMarkers[i].getDbAccessionId();
				String strMarker = "Marker: " + iMarkerId + ": " + strMarkerName + " GID: " + strGID;
				strUploadInfo += strMarker + "\n";
			}*/
			//strDataUploaded = "Uploaded SNP Genotyping dataset \n " +listOfMarkerNamesFromSourceTable;
		strDataUploaded = "Uploaded SNP Genotyping dataset";
		//}
		
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
