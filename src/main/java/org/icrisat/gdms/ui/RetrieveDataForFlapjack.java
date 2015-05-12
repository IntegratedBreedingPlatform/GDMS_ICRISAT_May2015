package org.icrisat.gdms.ui;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.dao.gdms.AccMetadataSetDAO;
import org.generationcp.middleware.dao.gdms.AlleleValuesDAO;
import org.generationcp.middleware.dao.gdms.MappingPopDAO;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.dao.gdms.MarkerMetadataSetDAO;
import org.generationcp.middleware.dao.gdms.QtlDAO;
import org.generationcp.middleware.dao.gdms.QtlDetailsDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AccMetadataSet;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.AllelicValueWithMarkerIdElement;
import org.generationcp.middleware.pojos.gdms.CharValues;
import org.generationcp.middleware.pojos.gdms.MapInfo;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.generationcp.middleware.pojos.gdms.MappingPopValues;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.gdms.ParentElement;
import org.generationcp.middleware.pojos.gdms.Qtl;
import org.generationcp.middleware.pojos.gdms.QtlDetailElement;
import org.generationcp.middleware.pojos.gdms.QtlDetails;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
//import org.generationcp.middleware.pojos.gdms.QtlDetailsPK;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFlapjackFileFormatsGermplasmRetrieval;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.ui.Window.Notification;

public class RetrieveDataForFlapjack {

	List<QtlDetailElement> listOfQtlDetailElementByQtlIds = null;
	private String strDatasetName;
	private String strDatasetID;
	private String strDatasetType = "";
	private String strGenotypingType;
	private Session localSession;
	private Session centralSession;
	private List<Integer> listOfMarkerIdsForGivenDatasetID;
	private List<String> listOfMarkerTypeByMarkerID;
	private String strMarkerType;
	private List<Integer> listOfParentAGIDs;
	private ArrayList<Integer> listOfParentBGIDs;
	private String strMappingType;
	private List<ParentElement> listOfParentsByDatasetId;
	private List<Integer> listOfAllParentGIDs;
	private ArrayList<Integer> listOfNIDsForAllelicMappingType;
	private List<Integer> listOfDatasetIDs;
	private GDMSMain _mainHomePage;
	private HashMap<Object, String> hmOfGIdsAndNval;
	private HashMap<String, Object> hmOfNvalAndGIds;
	private List<Integer> markersL=new ArrayList();;
	private ArrayList<Integer> listOfGIDs;
	private ArrayList<Integer> listOfNIDs;
	private ArrayList<Marker> listOfAllMarkersForGivenDatasetID;
	private HashMap<Integer, String> hmOfMIDandMNames;
	
	private HashMap<Integer, String> hmOfQtlPosition;
	
	private HashMap<String, Integer> hmOfQtlNameId;
	
	private ArrayList<String> listOfMarkerNames;
	private ArrayList<AllelicValueWithMarkerIdElement> listOfAllelicValuesForMappingType;
	private ArrayList<AllelicValueWithMarkerIdElement> listOfAllAllelicValuesForSSRandDArtDatasetType;
	private ArrayList<AllelicValueWithMarkerIdElement> listOfAllAllelicValuesForSNPDatasetType;
	private String strSelectedMap;
	private String strSelectedExportType;
	private TreeMap<Object, String> sortedMapOfGIDsAndGNames;
	private TreeMap<String, Object> sortedMapOfGNamesAndGIDs;
	
	private TreeMap<Object, String> sortedMapOfGIDAndGName;
	
	private TreeMap<Integer, String> sortedMapOfMIDsAndMNames;
	private TreeMap<String, Integer> sortedMapOfMNamesAndMIDs;
	
	private ArrayList listOfGIDsToBeExported;
	private ArrayList<String> listOfGNamesToBeExported;
	private Integer iMapId;
	private boolean bQTLExists;
	private ArrayList<QtlDetailElement> listOfAllQTLDetails;
	private ArrayList listOfAllMapInfo;
	private ArrayList<MappingData> listOfAllMappingData;
	private ArrayList<AllelicValueElement> listIfAllelicValueElements;
	private boolean bFlapjackDataBuiltSuccessfully;
	private HashMap<Integer, String> hmOfQtlIdandName;
	private File generatedTextFile;
	private File generatedMapFile;
	private File generatedDatFile;
	private String folderPath;
	private String strQTLExists;
	private ArrayList<String> listOfGermplasmNamesSelectedForGermplasmRetrieval;
	private ArrayList<String> listOfMarkersForGivenGermplasmRetrieval;
	private ArrayList<Integer> listOfGIDsProvidedForGermplasmRetrieval;
	private ArrayList<Integer> listOfNIDsForGivenGIDs;
	//private ArrayList<AllelicValueElement> listOfAllelicValueElementsForGermplasmNames;
	private ArrayList<Integer> listOfMIDsForGivenGermplasmRetrieval;
	private ArrayList<MappingPopValues> listOfAllMappingPopValuesForGermplasmRetrieval;
	private ArrayList<CharValues> listOfAllCharValuesForGermplasmRetrieval;
	private ArrayList<AllelicValueElement> listOfAllelicValueElementsForGermplasmRetrievals;
	private HashMap<Integer, String> hmOfSelectedMIDandMNames2;
	private HashMap<Object, String> hmOfGIdAndNval;
	List<AllelicValueWithMarkerIdElement> alleleValues;
	
	//HashMap<Integer, HashMap<String, Object>> mapEx = new HashMap<Integer, HashMap<String,Object>>();	
	HashMap<Object, HashMap<String, Object>> mapEx = new HashMap<Object, HashMap<String,Object>>();	
	HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
	HashMap marker = new HashMap();
	
	
	ArrayList parentList = new ArrayList();
	ArrayList markerNames = new ArrayList();
	List parentsData=new ArrayList();
	
	HashMap<String, Integer> markerNamesIDsMap=new HashMap<String, Integer>();
	SortedMap mapA = new TreeMap();
	SortedMap mapB = new TreeMap();
	HashMap<Integer, String> markerIDsNamesMap=new HashMap<Integer, String>();
	
	//List<Integer> markers=new ArrayList();;
	int parentANid=0;
	int parentBNid=0;
	
	int parentAGid=0;
	int parentBGid=0;
	
	String mtype="";
	
	SQLQuery queryL;
	SQLQuery queryC;
	String strQuerry="";
	String pgids="";
	String mid="";
	String mids="";
	String gids="";
	
	ArrayList glist = new ArrayList();
	//ArrayList midslist = new ArrayList();
	//String data="";
	
	private HashMap<Integer, String> parentsGIDsNames;
	
	ArrayList listOfMarkersinMap;
	
	private boolean mappingAllelic;
	private String strSelectedMappingType;
	
	List<AllelicValueWithMarkerIdElement> allelicValues;
	ArrayList intAlleleValues=new ArrayList();
	List<MapInfo> results ;
	ManagerFactory factory=null;
	GenotypicDataManager genoManager;
	GermplasmDataManager germManager;
	
	HashMap<Object, String> hmOfGIDs;
	
	public RetrieveDataForFlapjack(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			genoManager=factory.getGenotypicDataManager();
			
			germManager=factory.getGermplasmDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void setGenotypingType(String theGenotypingType) {
		strGenotypingType = theGenotypingType;
	}

	public void setDatasetName(String theDatasetName) {
		strDatasetName = theDatasetName;
	}

	public void setDatasetID(String theDatasetID) {
		strDatasetID = theDatasetID;
	}

	public void setDatasetType(String theDatasetType) {
		strDatasetType = theDatasetType;
	}
	public void setMappingType(boolean selected, String theMappingType) {
		mappingAllelic=selected;
		strSelectedMappingType = theMappingType;
	}
	public void setMapSelected(String theSelectedMap, Integer theMapID) {
		strSelectedMap = theSelectedMap;
		iMapId = theMapID;
	}

	public void setExportType(String theSelectedColumn) {
		strSelectedExportType = theSelectedColumn;
	}


	public void retrieveFlapjackData() {

		bFlapjackDataBuiltSuccessfully = false;
		//System.out.println("strGenotypingType=:"+strGenotypingType);
		if (strGenotypingType.equalsIgnoreCase("Dataset")){

			listOfDatasetIDs = new ArrayList<Integer>();
			listOfDatasetIDs.add(Integer.parseInt(strDatasetID));

			/**
			 * Retrieving the list of all markers for the Dataset selected
			 */
			try {
				markersL=new ArrayList();
				hmOfGIDs = new HashMap<Object, String>();
				//System.out.println("strDatasetType:"+strDatasetType);
				if (strDatasetType.equalsIgnoreCase("mapping")){

					retrieveParentAandParentBGIDs();
					////System.out.println("strMappingType=   :"+strMappingType);
					if (strMappingType.equalsIgnoreCase("allelic")){
						//retrieveNIDsUsingTheParentGIDsList();
						retrieveNIDsUsingTheParentGIDsList(parentList);
					} 

					retrieveNIDsUsingDatasetID();
					markersL= genoManager.getMarkerIdsByDatasetId(Integer.parseInt(strDatasetID));
					
					/*if (strMappingType.equalsIgnoreCase("allelic")){
						retrieveParentGIDsAndGNamesForAllelicType();						
					} else {*/
						Name namesA = null;
						Name namesB = null;						
						parentsGIDsNames= new HashMap<Integer, String>();
						
						namesA=germManager.getGermplasmNameByID(parentANid);
						parentAGid=namesA.getGermplasmId();
						parentsGIDsNames.put(namesA.getGermplasmId(), namesA.getNval());
						parentList.add(parentAGid);
						
						namesB=germManager.getGermplasmNameByID(parentBNid);
						parentBGid=namesB.getGermplasmId();
						parentsGIDsNames.put(namesB.getGermplasmId(), namesB.getNval());
						parentList.add(parentBGid);
						//System.out.println("strMappingType=:"+strMappingType);
						hmOfGIDs.put(namesA.getGermplasmId()+"~~!!~~1", parentAGid+"");
						hmOfGIDs.put(namesB.getGermplasmId()+"~~!!~~1", parentBGid+"");
						
						for(int p=0;p<parentList.size();p++){
							pgids=pgids+parentList.get(p)+",";
						}
						pgids=pgids.substring(0, pgids.length()-1);
						for(int m=0;m<markersL.size();m++){
							mids=mids+markersL.get(m)+",";
						}
						mids=mids.substring(0, mids.length()-1);
						
					//	System.out.println("hmOfGIDs:"+hmOfGIDs);
						List<Integer> markers= genoManager.getMarkerIdsByDatasetId(Integer.parseInt(strDatasetID));
						
						List<MarkerIdMarkerNameElement> marker_Names =genoManager.getMarkerNamesByMarkerIds(markers);
						 for (MarkerIdMarkerNameElement e : marker_Names) {
					            //Debug.println(0, e.getMarkerId() + " : " + e.getMarkerName());
							 markerNames.add(e.getMarkerName());
							 markerNamesIDsMap.put(e.getMarkerName(), e.getMarkerId());
							 markerIDsNamesMap.put(e.getMarkerId(), e.getMarkerName());
					        }
						 	//sortedMapOfMIDsAndMNames = new TreeMap<Integer, String>();
							sortedMapOfMNamesAndMIDs = new TreeMap<String, Integer>();
							Set<Integer> midKeySet = markerIDsNamesMap.keySet();
							Iterator<Integer> midIterator = midKeySet.iterator();
							while (midIterator.hasNext()) {
								Integer mid = midIterator.next();
								String mname = markerIDsNamesMap.get(mid);
								//sortedMapOfMIDsAndMNames.put(mid, mname);
								sortedMapOfMNamesAndMIDs.put(mname, mid);
							}
						List <String> marker_Type=genoManager.getMarkerTypesByMarkerIds(markers);
						mtype=marker_Type.get(0);
						//System.out.println(".................mtype:"+mtype);
						
						if(strMappingType.equalsIgnoreCase("allelic")){
							//System.out.println("strSelectedMappingType:"+strSelectedMappingType);
							try{
								localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
								centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
							}catch (Exception e){
								e.printStackTrace();
							}
							
							List allelesFromLocal=new ArrayList();	
							List allelesFromCentral=new ArrayList();	
							parentsData=new ArrayList();		
							List allelesList=new ArrayList();
							//List gidsList=new ArrayList();
							Object objAL=null;
							Object objAC=null;
							Iterator itListAC=null;
							Iterator itListAL=null;	
							if((mtype.equalsIgnoreCase("SSR"))||(mtype.equalsIgnoreCase("DArT"))){
								strQuerry="select distinct gid,marker_id, allele_bin_value from gdms_allele_values where gid in("+pgids+") and marker_id in("+mids+") order by gid, marker_id asc";	
								queryL=localSession.createSQLQuery(strQuerry);		
								queryL.addScalar("gid",Hibernate.INTEGER);	 
								queryL.addScalar("marker_id",Hibernate.INTEGER);
								queryL.addScalar("allele_bin_value",Hibernate.STRING);				
								
								allelesFromLocal=queryL.list();
								
								
								queryC=centralSession.createSQLQuery(strQuerry);
								queryC.addScalar("gid",Hibernate.INTEGER);	 
								queryC.addScalar("marker_id",Hibernate.INTEGER);
								queryC.addScalar("allele_bin_value",Hibernate.STRING);
								allelesFromCentral=queryC.list();
								for(int w=0;w<allelesFromCentral.size();w++){
									Object[] strMareO= (Object[])allelesFromCentral.get(w);
									parentsData.add(Integer.parseInt(strMareO[0].toString())+","+Integer.parseInt(strMareO[1].toString())+","+strMareO[2].toString());																
								}
								
								for(int w=0;w<allelesFromLocal.size();w++){
									Object[] strMareO= (Object[])allelesFromLocal.get(w);								
									parentsData.add(Integer.parseInt(strMareO[0].toString())+","+Integer.parseInt(strMareO[1].toString())+","+strMareO[2].toString());																	
								}
								
							}else if(mtype.equalsIgnoreCase("SNP")){
								strQuerry="select distinct gid,marker_id, char_value from gdms_char_values where gid in("+pgids+") and marker_id in("+mids+") order by gid, marker_id asc";
								//System.out.println(strQuerry);
								queryL=localSession.createSQLQuery(strQuerry);		
								queryL.addScalar("gid",Hibernate.INTEGER);	 
								queryL.addScalar("marker_id",Hibernate.INTEGER);
								queryL.addScalar("char_value",Hibernate.STRING);				
								
								allelesFromLocal=queryL.list();
								
								
								queryC=centralSession.createSQLQuery(strQuerry);
								queryC.addScalar("gid",Hibernate.INTEGER);	 
								queryC.addScalar("marker_id",Hibernate.INTEGER);
								queryC.addScalar("char_value",Hibernate.STRING);
								allelesFromCentral=queryC.list();
								if(! allelesFromCentral.isEmpty()){
									for(int w=0;w<allelesFromCentral.size();w++){
										Object[] strMareO= (Object[])allelesFromCentral.get(w);
										markerAlleles.put(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
										
										if(!(glist.contains(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1")))
											glist.add(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1");
										parentsData.add(Integer.parseInt(strMareO[0].toString())+","+Integer.parseInt(strMareO[1].toString())+","+strMareO[2].toString());	
										
									}
								}
								if(!allelesFromLocal.isEmpty()){
									for(int w=0;w<allelesFromLocal.size();w++){
										Object[] strMareO= (Object[])allelesFromLocal.get(w);	
										
										markerAlleles.put(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
										
										if(!(glist.contains(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1")))
											glist.add(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1");
										
										parentsData.add(Integer.parseInt(strMareO[0].toString())+","+Integer.parseInt(strMareO[1].toString())+","+strMareO[2].toString());																	
									}
								}
							}					
							//System.out.println("parentsData=:"+parentsData);
							if(strSelectedMappingType.equalsIgnoreCase("abh")){	
								mapA=new TreeMap(); 
								mapB=new TreeMap(); 
								
								
								for(int c=0;c<parentsData.size();c++){
									 String arrP[]=new String[3];
									 StringTokenizer stzP = new StringTokenizer(parentsData.get(c).toString(), ",");
									 int iP=0;
									 while(stzP.hasMoreTokens()){
										 arrP[iP] = stzP.nextToken();
										 iP++;
									 }	
									// System.out.println(arrP);
									 if(Integer.parseInt(arrP[0])==parentAGid)								
										mapA.put(Integer.parseInt(arrP[1]), arrP[2]);
									 else
										mapB.put(Integer.parseInt(arrP[1]), arrP[2]);							
									 
								}
								
								for(int m=0; m<markersL.size(); m++){
									//intAlleleValues.add(parentAGid+"!~!"+markerIDsList.get(m)+"!~!"+"A");
									markerAlleles.put(parentAGid+"~~!!~~"+"1"+"!~!"+markersL.get(m), "A");
									
									if(!(glist.contains(parentAGid+"~~!!~~"+"1")))
										glist.add(parentAGid+"~~!!~~"+"1");
								}
								for(int m=0; m<markersL.size(); m++){
									//intAlleleValues.add(parentBGid+"!~!"+markerIDsList.get(m)+"!~!"+"B");
									markerAlleles.put(parentBGid+"~~!!~~"+"1"+"!~!"+markersL.get(m), "B");
									
									if(!(glist.contains(parentBGid+"~~!!~~"+"1")))
										glist.add(parentBGid+"~~!!~~"+"1");
								}
								List markerKey = new ArrayList();
								markerKey.addAll(markerAlleles.keySet());
								for(int g=0; g<glist.size(); g++){
									for(int i=0; i<markerKey.size();i++){
										 if(!(mapEx.get(glist.get(g))==null)){
											 marker = (HashMap)mapEx.get(glist.get(g));
										 }else{
											 marker = new HashMap();
										 }
										 Object mKey=markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!"));
										 if(glist.get(g).equals(mKey)){
											 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
											 mapEx.put(glist.get(g),(HashMap)marker);
										 }						
									}	
								}							
								
							}else{						
								
								List markerKey = new ArrayList();
								markerKey.addAll(markerAlleles.keySet());
								for(int g=0; g<glist.size(); g++){
									for(int i=0; i<markerKey.size();i++){
										 if(!(mapEx.get(glist.get(g))==null)){
											 marker = (HashMap)mapEx.get(glist.get(g));
										 }else{
											 marker = new HashMap();
										 }
										 Object mKey=markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!"));
										 if(glist.get(g).equals(mKey)){
											 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
											 mapEx.put(glist.get(g),(HashMap)marker);
										 }						
									}	
								}								
							}
							//System.out.println("mapEx:"+mapEx);
						}else{
													
							for(int m=0; m<markersL.size(); m++){
								markerAlleles.put(parentAGid+"~~!!~~"+"1"+"!~!"+markersL.get(m), "A");							
								if(!(glist.contains(parentAGid+"~~!!~~"+"1")))
									glist.add(parentAGid+"~~!!~~"+"1");
							}							
							for(int m=0; m<markersL.size(); m++){
								markerAlleles.put(parentBGid+"~~!!~~"+"1"+"!~!"+markersL.get(m), "B");								
								if(!(glist.contains(parentBGid+"~~!!~~"+"1")))
									glist.add(parentBGid+"~~!!~~"+"1");
							}
							
							List markerKey = new ArrayList();
							markerKey.addAll(markerAlleles.keySet());
							for(int g=0; g<glist.size(); g++){
								for(int i=0; i<markerKey.size();i++){
									 if(!(mapEx.get(glist.get(g))==null)){
										 marker = (HashMap)mapEx.get(glist.get(g));
									 }else{
										 marker = new HashMap();
									 }
									 Object mKey=markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!"));
									 if(glist.get(g).equals(mKey)){
										 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
										 mapEx.put(glist.get(g),(HashMap)marker);
									 }						
								}	
							}
						}												
						retrieveGermplasmNamesByGIDs();	

				} else{
					//If strDatasetType is not equal to mapping 
					////System.out.println("Dataset Type is not mapping.");
					markersL= genoManager.getMarkerIdsByDatasetId(Integer.parseInt(strDatasetID));
					retrieveGermplasmNamesByGIDs();
					//retrieveNIDsFor_SSR_SNP_DArt_DataTypes();
				}
				
				hmOfMIDandMNames = new HashMap<Integer, String>();
				listOfMarkerNames = new ArrayList<String>();
				listOfMarkersForGivenGermplasmRetrieval=new ArrayList<String>();
				List<MarkerIdMarkerNameElement> markerNames =genoManager.getMarkerNamesByMarkerIds(markersL);
				for (MarkerIdMarkerNameElement e : markerNames) {
		            //Debug.println(0, e.getMarkerId() + " : " + e.getMarkerName());
					if(!listOfMarkerNames.contains(e.getMarkerName())){
						listOfMarkersForGivenGermplasmRetrieval.add(e.getMarkerName());
						listOfMarkerNames.add(e.getMarkerName());
						hmOfMIDandMNames.put(e.getMarkerId(), e.getMarkerName());
						markerIDsNamesMap.put(e.getMarkerId(), e.getMarkerName());
					}
		        }
				
				
				sortedMapOfMNamesAndMIDs = new TreeMap<String, Integer>();
				Set<Integer> midKeySet = markerIDsNamesMap.keySet();
				Iterator<Integer> midIterator = midKeySet.iterator();
				while (midIterator.hasNext()) {
					Integer mid = midIterator.next();
					String mname = markerIDsNamesMap.get(mid);
					//sortedMapOfMIDsAndMNames.put(mid, mname);
					sortedMapOfMNamesAndMIDs.put(mname, mid);
				}
				
				if(strDatasetType.equalsIgnoreCase("SNP")){
					retrieveValuesForSNPDatasetType();
				}else if((strDatasetType.equalsIgnoreCase("SSR"))||(strDatasetType.equalsIgnoreCase("DArT"))){
					retrieveValuesForSSRandDArtDatasetType();
				}else if(strDatasetType.equalsIgnoreCase("mapping")){
					retrieveValuesForMappingDatasetType();
				}

				sortedMapOfGIDsAndGNames = new TreeMap<Object, String>();
				sortedMapOfGNamesAndGIDs= new TreeMap<String, Object>();
				Set<Object> gidKeySet = hmOfGIdsAndNval.keySet();
				Iterator<Object> gidIterator = gidKeySet.iterator();
				while (gidIterator.hasNext()) {
					Object gid = gidIterator.next();
					String gname = hmOfGIdsAndNval.get(gid);
					if (strDatasetType.equalsIgnoreCase("mapping")){
						String ParentA=parentsGIDsNames.get(parentAGid);
						String ParentB=parentsGIDsNames.get(parentBGid);
						
						sortedMapOfGIDsAndGNames.put(parentAGid, ParentA);
						sortedMapOfGIDsAndGNames.put(parentBGid, ParentB);
						
						sortedMapOfGNamesAndGIDs.put(ParentA, parentAGid);
						sortedMapOfGNamesAndGIDs.put(ParentB, parentBGid);
						
						
					}
					sortedMapOfGIDsAndGNames.put(gid, gname);
					sortedMapOfGNamesAndGIDs.put(gname, gid);
				}
				//System.out.println("Size of Sorted Map of GIDs and GNames: " + sortedMapOfGIDsAndGNames.size()+"   "+hmOfGIdAndNval);
				sortedMapOfGIDAndGName = new TreeMap<Object, String>();
				//sortedMapOfGNamesAndGIDs= new TreeMap<String, Object>();
				Set<Object> gidKeySet1 = hmOfGIdAndNval.keySet();
				Iterator<Object> gidIterator1 = gidKeySet1.iterator();
				while (gidIterator1.hasNext()) {
					Object gid = gidIterator1.next();
					String gname = hmOfGIdAndNval.get(gid);
					if (strDatasetType.equalsIgnoreCase("mapping")){
						String ParentA=parentsGIDsNames.get(parentAGid);
						String ParentB=parentsGIDsNames.get(parentBGid);
						
						sortedMapOfGIDAndGName.put(parentAGid+"~~!!~~"+"1", ParentA);
						sortedMapOfGIDAndGName.put(parentBGid+"~~!!~~"+"1", ParentB);
						
						/*sortedMapOfGNamesAndGIDs.put(ParentA, parentAGid);
						sortedMapOfGNamesAndGIDs.put(ParentB, parentBGid);*/
						
						
					}
					sortedMapOfGIDAndGName.put(gid, gname);
					//sortedMapOfGNamesAndGIDs.put(gname, gid);
				}
				
				//System.out.println("sortedMapOfGIDAndGName:"+sortedMapOfGIDAndGName);
		
				sortedMapOfMIDsAndMNames = new TreeMap<Integer, String>();
				Set<Integer> midKeySet1 = hmOfMIDandMNames.keySet();
				Iterator<Integer> midIterator1 = midKeySet1.iterator();
				while (midIterator1.hasNext()) {
					Integer mid = midIterator1.next();
					String mname = hmOfMIDandMNames.get(mid);
					sortedMapOfMIDsAndMNames.put(mid, mname);
				}
				////System.out.println("Size of Sorted Map of MIDs and MNames: " + sortedMapOfMIDsAndMNames.size());

				//listOfGIDsToBeExported to be used if exporting based on GIDs
				if (strSelectedExportType.equalsIgnoreCase("GIDs")){
					listOfGIDsToBeExported = new ArrayList();
					
					if (strDatasetType.equalsIgnoreCase("mapping")){
						if (false == listOfGIDsToBeExported.contains(parentAGid)){
							listOfGIDsToBeExported.add(parentAGid);
						}
						if (false == listOfGIDsToBeExported.contains(parentBGid)){
							listOfGIDsToBeExported.add(parentBGid);
						}
					}
					
					Iterator<Object> itrSortedMapGIDs = sortedMapOfGIDsAndGNames.keySet().iterator();
					while (itrSortedMapGIDs.hasNext()){
						Object iGID = itrSortedMapGIDs.next();
						if (false == listOfGIDsToBeExported.contains(iGID)){
							listOfGIDsToBeExported.add(iGID);
						}
					}
				} else if (strSelectedExportType.equalsIgnoreCase("Germplasm Names")){
					listOfGNamesToBeExported = new ArrayList<String>();
					Iterator<Object> itrSortedMapGIDs = sortedMapOfGIDsAndGNames.keySet().iterator();
					while (itrSortedMapGIDs.hasNext()){
						Object iGID = itrSortedMapGIDs.next();
						String strGName = hmOfGIdsAndNval.get(iGID);
						if (strDatasetType.equalsIgnoreCase("mapping")){
							String ParentA=parentsGIDsNames.get(parentAGid);
							String ParentB=parentsGIDsNames.get(parentBGid);
							if (false == listOfGNamesToBeExported.contains(ParentA)){
								listOfGNamesToBeExported.add(ParentA);
							}
							if (false == listOfGNamesToBeExported.contains(ParentB)){
								listOfGNamesToBeExported.add(ParentB);
							}
						}
						if((false == listOfGNamesToBeExported.contains(strGName))&&(strGName!=null)){
							listOfGNamesToBeExported.add(strGName);
						}
					}
				}

				retrieveMapDataForFlapjack();

				retrieveQTLDataForFlapjack();
				//System.out.println("hmOfGIDs>..............=:"+hmOfGIDs);

				bFlapjackDataBuiltSuccessfully = true;

				//ExportFlapjackFileFormats exportFlapjackFileFormats = new ExportFlapjackFileFormats();
				ExportFlapjackFileFormatsGermplasmRetrieval exportFlapjackFileFormats = new ExportFlapjackFileFormatsGermplasmRetrieval();
				//System.out.println("intAlleleValues=:"+sortedMapOfGIDAndGName);
				
				if (strSelectedExportType.equalsIgnoreCase("GIDs")){
					if (strDatasetType.equalsIgnoreCase("SNP")){
						//exportFlapjackFileFormats.generateFlapjackDataFilesByGIDs(_mainHomePage, intAlleleValues, listOfAllMapInfo, listOfGIDsToBeExported, listOfMarkerNames, sortedMapOfMIDsAndMNames, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, strDatasetType);
						exportFlapjackFileFormats.generateFlapjackDataFilesByGIDs(_mainHomePage, listOfAllMapInfo, listOfGIDsToBeExported, listOfMarkerNames, sortedMapOfMNamesAndMIDs, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx, glist, hmOfGIDs);
					} else if (strDatasetType.equalsIgnoreCase("SSR") || strDatasetType.equalsIgnoreCase("DArt")){
						exportFlapjackFileFormats.generateFlapjackDataFilesByGIDs(_mainHomePage, listOfAllMapInfo, listOfGIDsToBeExported, listOfMarkerNames, sortedMapOfMNamesAndMIDs, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId,hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx, glist, hmOfGIDs);
					} else if (strDatasetType.equalsIgnoreCase("Mapping")){
						exportFlapjackFileFormats.generateFlapjackDataFilesByGIDs(_mainHomePage, listOfAllMapInfo, listOfGIDsToBeExported, listOfMarkerNames, sortedMapOfMNamesAndMIDs, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId,hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx, glist, hmOfGIDs);
					}
				} else if (strSelectedExportType.equalsIgnoreCase("Germplasm Names")) {
					if (strDatasetType.equalsIgnoreCase("SNP")){
						//exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, intAlleleValues, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkerNames, sortedMapOfMIDsAndMNames, sortedMapOfGIDsAndGNames, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, strDatasetType);
						exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkerNames, sortedMapOfMNamesAndMIDs, sortedMapOfGIDAndGName, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx, glist);
					} else if (strDatasetType.equalsIgnoreCase("SSR") || strDatasetType.equalsIgnoreCase("DArt")){
						//exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, intAlleleValues, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkerNames, sortedMapOfMIDsAndMNames, sortedMapOfGIDsAndGNames, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, strDatasetType);
						exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkerNames, sortedMapOfMNamesAndMIDs, sortedMapOfGIDAndGName, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx, glist);
					} else if (strDatasetType.equalsIgnoreCase("Mapping")){						
						//exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, intAlleleValues, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkerNames, sortedMapOfMIDsAndMNames, sortedMapOfGIDsAndGNames, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId,hmOfQtlIdandName, strSelectedExportType, bQTLExists, mtype, mapEx);
						exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkerNames, sortedMapOfMNamesAndMIDs, sortedMapOfGIDAndGName, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx, glist);
					}
				}
				generatedTextFile = exportFlapjackFileFormats.getGeneratedTextFile();
				generatedMapFile = exportFlapjackFileFormats.getGeneratedMapFile();
				generatedDatFile = exportFlapjackFileFormats.getGeneratedDatFile();
				
				folderPath=exportFlapjackFileFormats.getStrBMSFilePath();
				strQTLExists=exportFlapjackFileFormats.getQTLExists();
				
			} catch (NumberFormatException e) {
				bFlapjackDataBuiltSuccessfully = false;
				_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving data for the given DatasetID", Notification.TYPE_ERROR_MESSAGE);
				e.printStackTrace();
				return;
			} catch (MiddlewareQueryException e) {
				bFlapjackDataBuiltSuccessfully = false;
				_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving data for the given DatasetID", Notification.TYPE_ERROR_MESSAGE);
				e.printStackTrace();
				return;
			} catch (GDMSException ge){
				bFlapjackDataBuiltSuccessfully = false;
				_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving data for the given DatasetID. " + ge.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
				ge.printStackTrace();
				return;
			}

		} else {
			/** NOT DATASET **/
				
				if (strGenotypingType.equalsIgnoreCase("Germplasm Names") || strGenotypingType.equalsIgnoreCase("GIDs") ||
						strGenotypingType.equalsIgnoreCase("Markers")){
					markersL=new ArrayList();
					
					try{
						markersL=new ArrayList();
						listOfMarkerNames=new ArrayList();
					//	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  :"+listOfMarkersForGivenGermplasmRetrieval);
					//	
					//	System.out.println("........................:"+listOfMIDsForGivenGermplasmRetrieval);
						
						if(null != listOfMIDsForGivenGermplasmRetrieval){
							////System.out.println("null *********************************************** mlist");
							for(int m=0; m<listOfMIDsForGivenGermplasmRetrieval.size();m++){
								if(listOfMIDsForGivenGermplasmRetrieval.get(m)!=null)
								markersL.add(listOfMIDsForGivenGermplasmRetrieval.get(m));
							}
						}
					//	System.out.println("markersL=:"+markersL.size()+"   "+markersL);
						//if(markersL.isEmpty()){
							////System.out.println("if m empty ");
							
							List<Marker> markerIds =genoManager.getMarkersByMarkerNames(listOfMarkersForGivenGermplasmRetrieval, 0, listOfMarkersForGivenGermplasmRetrieval.size(), Database.CENTRAL);
							List<Marker> markerIdsL =genoManager.getMarkersByMarkerNames(listOfMarkersForGivenGermplasmRetrieval, 0, listOfMarkersForGivenGermplasmRetrieval.size(), Database.LOCAL);
							if(!(markerIds.isEmpty())){
								////System.out.println("M from central not empty");
								for (Marker iMarkerID : markerIds){	
									if(!markersL.contains(iMarkerID.getMarkerId()))
									markersL.add(iMarkerID.getMarkerId());
									listOfMarkerNames.add(iMarkerID.getMarkerName());
								}
							}
							if(!(markerIdsL.isEmpty())){
								////System.out.println("MLK not Empty");
								//for(int ml=0; ml<markerIdsL.size();ml++){
								for (Marker markers : markerIdsL){	
									if(!markersL.contains(markers.getMarkerId()))
										markersL.add(markers.getMarkerId());
									listOfMarkerNames.add(markers.getMarkerName());
								}
							}
							
					}catch (MiddlewareQueryException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving data for the given Germplasm Names", Notification.TYPE_ERROR_MESSAGE);
						e.printStackTrace();
						return;
					}
					
					try {						
						
						//retrieveNIDsForGivenGIDs();						
						retrieveDataForFlapjackFormat();		
				
						sortedMapOfGIDAndGName = new TreeMap<Object, String>();
						//sortedMapOfGNamesAndGIDs= new TreeMap<String, Object>();
						Set<Object> gidKeySet1 = hmOfGIdsAndNval.keySet();
						Iterator<Object> gidIterator1 = gidKeySet1.iterator();
						while (gidIterator1.hasNext()) {
							Object gid = gidIterator1.next();
							String gname = hmOfGIdsAndNval.get(gid);
							
							sortedMapOfGIDAndGName.put(gid, gname);
							//sortedMapOfGNamesAndGIDs.put(gname, gid);
						}
						
						
						sortedMapOfMIDsAndMNames = new TreeMap<Integer, String>();
						sortedMapOfMNamesAndMIDs = new TreeMap<String, Integer>();
						Set<Integer> midKeySet = hmOfMIDandMNames.keySet();
						Iterator<Integer> midIterator = midKeySet.iterator();
						while (midIterator.hasNext()) {
							Integer mid = midIterator.next();
							String mname = hmOfMIDandMNames.get(mid);
							sortedMapOfMIDsAndMNames.put(mid, mname);
							sortedMapOfMNamesAndMIDs.put(mname, mid);
						}
						////System.out.println("Size of Sorted Map of MIDs and MNames: " + sortedMapOfMIDsAndMNames.size());

						//listOfGIDsToBeExported to be used if exporting based on GIDs
						if (strSelectedExportType.equalsIgnoreCase("GIDs")){
							listOfGIDsToBeExported = new ArrayList();
							
							Iterator<Object> itrSortedMapGIDs = sortedMapOfGIDAndGName.keySet().iterator();
							while (itrSortedMapGIDs.hasNext()){
								Object iGID = itrSortedMapGIDs.next();
								if (false == listOfGIDsToBeExported.contains(iGID)){
									listOfGIDsToBeExported.add(iGID);
								}
							}
						} else if (strSelectedExportType.equalsIgnoreCase("Germplasm Names")){
							listOfGNamesToBeExported = new ArrayList<String>();
							Iterator<Object> itrSortedMapGIDs = sortedMapOfGIDAndGName.keySet().iterator();
							while (itrSortedMapGIDs.hasNext()){
								Object iGID = itrSortedMapGIDs.next();
								String strGName = hmOfGIdsAndNval.get(iGID);
								if (false == listOfGNamesToBeExported.contains(strGName)){
									listOfGNamesToBeExported.add(strGName);
								}
							}
						}
						////System.out.println("listOfMIDsForGivenGermplasmRetrieval=:"+listOfMIDsForGivenGermplasmRetrieval);
						
							
						//}
						//System.out.println("markers=:"+markersL);
						retrieveMapDataForFlapjack();

						retrieveQTLDataForFlapjack();

						//System.out.println(strGenotypingType+" : mapEx=:"+mapEx);
						//System.out.println("sortedMapOfMNamesAndMIDs:"+sortedMapOfMNamesAndMIDs+"    "+listOfMarkersForGivenGermplasmRetrieval);
						bFlapjackDataBuiltSuccessfully = true;

						ExportFlapjackFileFormatsGermplasmRetrieval exportFlapjackFileFormats = new ExportFlapjackFileFormatsGermplasmRetrieval();
						
						if (strSelectedExportType.equalsIgnoreCase("GIDs")){
							exportFlapjackFileFormats.generateFlapjackDataFilesByGIDs(_mainHomePage, listOfAllMapInfo, listOfGIDsToBeExported, listOfMarkerNames, sortedMapOfMNamesAndMIDs, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx, glist, hmOfGIDs);
							
						} else if (strSelectedExportType.equalsIgnoreCase("Germplasm Names")) {
							//exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkersForGivenGermplasmRetrieval, sortedMapOfMIDsAndMNames, sortedMapOfGNamesAndGIDs, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx);
							exportFlapjackFileFormats.generateFlapjackDataFilesByGermplasmNames(_mainHomePage, listOfAllMapInfo, listOfGNamesToBeExported, listOfMarkerNames, sortedMapOfMNamesAndMIDs, sortedMapOfGIDAndGName, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType, bQTLExists, mapEx, glist);
							
						}
						
						
						generatedTextFile = exportFlapjackFileFormats.getGeneratedTextFile();
						generatedMapFile = exportFlapjackFileFormats.getGeneratedMapFile();
						generatedDatFile = exportFlapjackFileFormats.getGeneratedDatFile();
						folderPath= exportFlapjackFileFormats.getStrBMSFilePath();
						strQTLExists=exportFlapjackFileFormats.getQTLExists();
					} catch (MiddlewareQueryException e) {
						bFlapjackDataBuiltSuccessfully = false;
						_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving data for the given Germplasm Names", Notification.TYPE_ERROR_MESSAGE);
						e.printStackTrace();
						return;
					}
				}
			}
	}


	private void retrieveDataForFlapjackFormat() throws MiddlewareQueryException {
		glist = new ArrayList();	
		
		String mids="";
		
		for(int m=0; m<markersL.size(); m++){
			mids=mids+markersL.get(m)+",";
		}
		//System.out.println("mids:"+mids);
		mids=mids.substring(0, mids.length()-1);
		String gids="";
		for(int g=0;g<listOfGIDs.size();g++){
			gids=gids+listOfGIDs.get(g)+",";
		}
		gids=gids.substring(0, gids.length()-1);
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		List allelesFromLocal=new ArrayList();	
		List allelesFromCentral=new ArrayList();	
		List parentsData=new ArrayList();		
		List allelesList=new ArrayList();
		//List gidsList=new ArrayList();
		Object objAL=null;
		Object objAC=null;
		Iterator itListAC=null;
		Iterator itListAL=null;
		
		markerAlleles= new HashMap<String,Object>();
		strQuerry="select distinct gid,marker_id, char_value,acc_sample_id,marker_sample_id from gdms_char_values where gid in("+gids+") and marker_id in ("+mids+") ORDER BY gid, marker_id,acc_sample_id asc";	
		//System.out.println(strQuerry);
		queryL=localSession.createSQLQuery(strQuerry);		
		queryL.addScalar("gid",Hibernate.INTEGER);	 
		queryL.addScalar("marker_id",Hibernate.INTEGER);
		queryL.addScalar("char_value",Hibernate.STRING);	
		queryL.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryL.addScalar("marker_sample_id",Hibernate.INTEGER);				
		allelesFromLocal=queryL.list();
		
		
		queryC=centralSession.createSQLQuery(strQuerry);
		queryC.addScalar("gid",Hibernate.INTEGER);	 
		queryC.addScalar("marker_id",Hibernate.INTEGER);
		queryC.addScalar("char_value",Hibernate.STRING);
		queryC.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryC.addScalar("marker_sample_id",Hibernate.INTEGER);
		allelesFromCentral=queryC.list();
		marker = new HashMap();
		for(int w=0;w<allelesFromCentral.size();w++){
			Object[] strMareO= (Object[])allelesFromCentral.get(w);
		//	System.out.println(strMareO);
			if(strMareO[3]==null)
				markerAlleles.put(strMareO[0]+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			else
				markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			if(strMareO[3]==null){
				if(!(glist.contains(strMareO[0]+"~~!!~~"+"1")))
					glist.add(strMareO[0]+"~~!!~~"+"1");
			}else{
				if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
					glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);
			}
																			
		}
		
		for(int w=0;w<allelesFromLocal.size();w++){
			Object[] strMareO= (Object[])allelesFromLocal.get(w);	
			
			if(strMareO[3]==null)
				markerAlleles.put(strMareO[0]+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			else
				markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			if(strMareO[3]==null){	
				if(!(glist.contains(strMareO[0]+"~~!!~~"+"1")))
					glist.add(strMareO[0]+"~~!!~~"+"1");
			}else{
				if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
					glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);
			}
																			
		}
		//System.out.println("markerAlleles:"+markerAlleles);
		//System.out.println("glist:"+glist);
		
		strQuerry="select distinct gid,marker_id, allele_bin_value,acc_sample_id,marker_sample_id from gdms_allele_values where gid in("+gids+") and marker_id in ("+mids+") ORDER BY gid, marker_id,acc_sample_id asc";	
		queryL=localSession.createSQLQuery(strQuerry);		
		queryL.addScalar("gid",Hibernate.INTEGER);	 
		queryL.addScalar("marker_id",Hibernate.INTEGER);
		queryL.addScalar("allele_bin_value",Hibernate.STRING);	
		queryL.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryL.addScalar("marker_sample_id",Hibernate.INTEGER);				
		allelesFromLocal=queryL.list();
		
		
		queryC=centralSession.createSQLQuery(strQuerry);
		queryC.addScalar("gid",Hibernate.INTEGER);	 
		queryC.addScalar("marker_id",Hibernate.INTEGER);
		queryC.addScalar("allele_bin_value",Hibernate.STRING);
		queryC.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryC.addScalar("marker_sample_id",Hibernate.INTEGER);
		allelesFromCentral=queryC.list();
		marker = new HashMap();
		for(int w=0;w<allelesFromCentral.size();w++){
			Object[] strMareO= (Object[])allelesFromCentral.get(w);
			/*markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			
			if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
				glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);*/
			
			if(strMareO[3]==null)
				markerAlleles.put(strMareO[0]+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			else
				markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			if(strMareO[3]==null){
				if(!(glist.contains(strMareO[0]+"~~!!~~"+"1")))
					glist.add(strMareO[0]+"~~!!~~"+"1");
			}else{
				if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
					glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);
			}
			
																			
		}
		
		for(int w=0;w<allelesFromLocal.size();w++){
			Object[] strMareO= (Object[])allelesFromLocal.get(w);	
			
			/*markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			
			if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
				glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);*/
			if(strMareO[3]==null)
				markerAlleles.put(strMareO[0]+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			else
				markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			if(strMareO[3]==null){
				if(!(glist.contains(strMareO[0]+"~~!!~~"+"1")))
					glist.add(strMareO[0]+"~~!!~~"+"1");
			}else{
				if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
					glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);
			}
																			
		}
		
		strQuerry="select distinct gid,marker_id, map_char_value,acc_sample_id,marker_sample_id from gdms_mapping_pop_values where gid in("+gids+") and marker_id in ("+mids+") ORDER BY gid, marker_id,acc_sample_id asc";	
		queryL=localSession.createSQLQuery(strQuerry);		
		queryL.addScalar("gid",Hibernate.INTEGER);	 
		queryL.addScalar("marker_id",Hibernate.INTEGER);
		queryL.addScalar("map_char_value",Hibernate.STRING);	
		queryL.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryL.addScalar("marker_sample_id",Hibernate.INTEGER);				
		allelesFromLocal=queryL.list();
		
		
		queryC=centralSession.createSQLQuery(strQuerry);
		queryC.addScalar("gid",Hibernate.INTEGER);	 
		queryC.addScalar("marker_id",Hibernate.INTEGER);
		queryC.addScalar("map_char_value",Hibernate.STRING);
		queryC.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryC.addScalar("marker_sample_id",Hibernate.INTEGER);
		allelesFromCentral=queryC.list();
		marker = new HashMap();
		for(int w=0;w<allelesFromCentral.size();w++){
			Object[] strMareO= (Object[])allelesFromCentral.get(w);
			/*markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			
			if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
				glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);*/
			if(strMareO[3]==null)
				markerAlleles.put(strMareO[0]+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			else
				markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			if(strMareO[3]==null){
				if(!(glist.contains(strMareO[0]+"~~!!~~"+"1")))
					glist.add(strMareO[0]+"~~!!~~"+"1");
			}else{
				if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
					glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);
			}
																			
		}
		
		for(int w=0;w<allelesFromLocal.size();w++){
			Object[] strMareO= (Object[])allelesFromLocal.get(w);	
			
			/*markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			
			if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
				glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);*/
			if(strMareO[3]==null)
				markerAlleles.put(strMareO[0]+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			else
				markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			if(strMareO[3]==null){
				if(!(glist.contains(strMareO[0]+"~~!!~~"+"1")))
					glist.add(strMareO[0]+"~~!!~~"+"1");
			}else{
				if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
					glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);
			}
																			
		}
		
		
		List markerKey = new ArrayList();
		markerKey.addAll(markerAlleles.keySet());
		for(int g=0; g<glist.size(); g++){
			for(int i=0; i<markerKey.size();i++){
				 if(!(mapEx.get(glist.get(g))==null)){
					 marker = (HashMap)mapEx.get(glist.get(g));
				 }else{
					 marker = new HashMap();
				 }
				 Object mKey=markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!"));
				 if(glist.get(g).equals(mKey)){
					 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
					 mapEx.put(glist.get(g),(HashMap)marker);
				 }						
			}	
		}
		
		//System.out.println(mapEx);
		HashMap<Object, Integer> hmGidSampleIdNid=new HashMap<Object, Integer>();
		HashMap<Integer, String> hmNidGermplasmName=new HashMap<Integer, String>();
		ArrayList listOfGNames = new ArrayList<String>();
		ArrayList gDupNameList=new ArrayList<Integer>();
		
		List listOfNIDs = new ArrayList<Integer>();
		List sampleFromLocal=new ArrayList();	
		List sampleFromCentral=new ArrayList();	
		parentsData=new ArrayList();		
		allelesList=new ArrayList();
		//List gidsList=new ArrayList();
		 objAL=null;
		objAC=null;
		 itListAC=null;
		 itListAL=null;	
		 gDupNameList=new ArrayList<Integer>();
		// System.out.println("...........................SRIKALYANI");
		String strQuerry="select distinct gid,nid, acc_sample_id from gdms_acc_metadataset where gid in ("+ gids +") order by gid, nid,acc_sample_id asc";	
		//System.out.println("strQuerry:"+strQuerry);
		queryL=localSession.createSQLQuery(strQuerry);		
		queryL.addScalar("gid",Hibernate.INTEGER);	 
		queryL.addScalar("nid",Hibernate.INTEGER);
		queryL.addScalar("acc_sample_id",Hibernate.INTEGER);				
		
		sampleFromLocal=queryL.list();
		
		
		queryC=centralSession.createSQLQuery(strQuerry);
		queryC.addScalar("gid",Hibernate.INTEGER);	 
		queryC.addScalar("nid",Hibernate.INTEGER);
		queryC.addScalar("acc_sample_id",Hibernate.INTEGER);
		sampleFromCentral=queryC.list();
		for(int w=0;w<sampleFromCentral.size();w++){
			Object[] strMareO= (Object[])sampleFromCentral.get(w);
			parentsData.add(Integer.parseInt(strMareO[0].toString()));	
			//if(!nidsList.contains(Integer.parseInt(strMareO[0].toString())))
			listOfNIDs.add(Integer.parseInt(strMareO[1].toString()));
			if((strMareO[2]==null)||(Integer.parseInt(strMareO[2].toString()) == 0)){
				hmGidSampleIdNid.put(strMareO[0]+"~~!!~~1", Integer.parseInt(strMareO[1].toString()));
			}else{
				hmGidSampleIdNid.put(strMareO[0]+"~~!!~~"+strMareO[2], Integer.parseInt(strMareO[1].toString()));
				if(Integer.parseInt(strMareO[2].toString())>1){
					if(!gDupNameList.contains(Integer.parseInt(strMareO[0].toString())))
					gDupNameList.add(Integer.parseInt(strMareO[0].toString()));	
				}
			}
		}
		
		for(int w=0;w<sampleFromLocal.size();w++){
			Object[] strMareO= (Object[])sampleFromLocal.get(w);								
			parentsData.add(Integer.parseInt(strMareO[0].toString()));	
			//if(!nidsList.contains(Integer.parseInt(strMareO[0].toString())))
			listOfNIDs.add(Integer.parseInt(strMareO[1].toString()));
			if((strMareO[2]==null)||(Integer.parseInt(strMareO[2].toString()) == 0)){
				hmGidSampleIdNid.put(strMareO[0]+"~~!!~~1", Integer.parseInt(strMareO[1].toString()));
			}else{
				hmGidSampleIdNid.put(strMareO[0]+"~~!!~~"+strMareO[2], Integer.parseInt(strMareO[1].toString()));
				if(Integer.parseInt(strMareO[2].toString())>1){
					if(!gDupNameList.contains(Integer.parseInt(strMareO[0].toString())))
						gDupNameList.add(Integer.parseInt(strMareO[0].toString()));		
				}
			}
		}
				
		
		
		int rep=1;
		Name names1 = null;
		String germplasmName="";
		Object gid=""; 
		//ArrayList<String> gNameList=new ArrayList<String>();
		try{
			for(int n=0;n<listOfNIDs.size();n++){			
				names1=genoManager.getNameByNameId(Integer.parseInt(listOfNIDs.get(n).toString()));
				if(names1 != null)
				hmNidGermplasmName.put(names1.getNid(), names1.getNval());				
			}
		} catch (Exception e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Germplasms .", Notification.TYPE_ERROR_MESSAGE);
			/*String strErrMsg = "Error retrieving germplasm names.";
			throw new GDMSException(strErrMsg);*/
		}
		hmOfGIdsAndNval = new HashMap<Object, String>();
		hmOfGIDs= new HashMap<Object, String>();
		//sortedMapOfGNamesAndGIDs= new TreeMap<String, Object>();
		Set<Object> gidKeySet1 = hmGidSampleIdNid.keySet();
		Iterator<Object> gidIterator1 = gidKeySet1.iterator();
		
		while (gidIterator1.hasNext()) {
			String gname="";
			Object gidSampleid = gidIterator1.next();
			Integer nid = hmGidSampleIdNid.get(gidSampleid);
			String strGName=hmNidGermplasmName.get(nid);
			String strGS=gidSampleid.toString();
			String strSampleId=strGS.substring(strGS.indexOf("~~!!~~")+6);
			Integer strGid=Integer.parseInt(strGS.substring(0, strGS.indexOf("~~!!~~")).toString());
			
			if(gDupNameList.contains(strGid)){
				//gidSampleid=gidSampleid;
				gname=strGName+" (Sample "+strSampleId+")";
				gids=strGid+" (Sample "+strSampleId+")";
			}else{
				gname=strGName;
				gids=strGid+"";
			}
			//System.out.println("str:"+gidSampleid+"   "+gname);
		
			hmOfGIdsAndNval.put(gidSampleid, gname);
			hmOfGIDs.put(gidSampleid, gids);
		}
		
		/*try {
			List<AllelicValueElement> allelicValues =genoManager.getAllelicValuesByGidsAndMarkerNames(listOfGIDs, listOfMarkersForGivenGermplasmRetrieval);
			
			//System.out.println(" allelicValues =:"+allelicValues);		
			marker = new HashMap();
			if (null != allelicValues){
				for (AllelicValueElement allelicValueElement : allelicValues){
					
					markerAlleles.put(allelicValueElement.getGid()+"!~!"+allelicValueElement.getMarkerId(), allelicValueElement.getData());
					
					if(!(glist.contains(allelicValueElement.getGid())))
						glist.add(allelicValueElement.getGid());					
				}
				
				List markerKey = new ArrayList();
				markerKey.addAll(markerAlleles.keySet());
				for(int g=0; g<glist.size(); g++){
					for(int i=0; i<markerKey.size();i++){
						 if(!(mapEx.get(Integer.parseInt(glist.get(g).toString()))==null)){
							 marker = (HashMap)mapEx.get(Integer.parseInt(glist.get(g).toString()));
						 }else{
						marker = new HashMap();
						 }
						 if(Integer.parseInt(glist.get(g).toString())==Integer.parseInt(markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!")))){
							 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
							 mapEx.put(Integer.parseInt(glist.get(g).toString()),(HashMap)marker);
						 }						
					}	
				}				
			}
			
		}catch (Exception e) {				
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving list of AllelicValueElement for the selected GIDs and markers required for FLAPJACK Format", Notification.TYPE_ERROR_MESSAGE);
			return;
				
		}*/
		
	}
	
	
	private void retrieveNIDsForGivenGIDs() throws MiddlewareQueryException {
		//rs=stmt.executeQuery("select nid from gdms_acc_metadataset where gid in ("+gid+") order by gid");
		
		String gids="";
		ArrayList listOfGNames = new ArrayList<String>();
		ArrayList gDupNameList=new ArrayList<String>();
				
		for(int g=0;g<listOfGIDs.size();g++){
			gids=gids+listOfGIDs.get(g)+",";
		}
		gids=gids.substring(0, gids.length()-1);
		
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		listOfNIDs = new ArrayList<Integer>();
		List allelesFromLocal=new ArrayList();	
		List allelesFromCentral=new ArrayList();	
		List parentsData=new ArrayList();		
		List allelesList=new ArrayList();
		//List gidsList=new ArrayList();
		Object objAL=null;
		Object objAC=null;
		Iterator itListAC=null;
		Iterator itListAL=null;	
		strQuerry="select distinct gid,nid, acc_sample_id from gdms_acc_metadataset where gid in ("+ gids +") order by gid, nid,acc_sample_id asc";	
	//	System.out.println("strQuerry:"+strQuerry);
		queryL=localSession.createSQLQuery(strQuerry);		
		queryL.addScalar("gid",Hibernate.INTEGER);	 
		queryL.addScalar("nid",Hibernate.INTEGER);
		queryL.addScalar("acc_sample_id",Hibernate.INTEGER);				
		
		allelesFromLocal=queryL.list();
		
		
		queryC=centralSession.createSQLQuery(strQuerry);
		queryC.addScalar("gid",Hibernate.INTEGER);	 
		queryC.addScalar("nid",Hibernate.INTEGER);
		queryC.addScalar("acc_sample_id",Hibernate.INTEGER);
		allelesFromCentral=queryC.list();
		for(int w=0;w<allelesFromCentral.size();w++){
			Object[] strMareO= (Object[])allelesFromCentral.get(w);
			parentsData.add(Integer.parseInt(strMareO[0].toString()));	
			//if(!nidsList.contains(Integer.parseInt(strMareO[0].toString())))
			listOfNIDs.add(Integer.parseInt(strMareO[1].toString()));
		}
		
		for(int w=0;w<allelesFromLocal.size();w++){
			Object[] strMareO= (Object[])allelesFromLocal.get(w);								
			parentsData.add(Integer.parseInt(strMareO[0].toString()));	
			//if(!nidsList.contains(Integer.parseInt(strMareO[0].toString())))
			listOfNIDs.add(Integer.parseInt(strMareO[1].toString()));
		}
		for(int w=0;w<parentsData.size();w++){
			if(listOfGNames.contains(parentsData.get(w))){							
				gDupNameList.add(parentsData.get(w));							
			}
			
			listOfGNames.add(parentsData.get(w));
		
		}
		int rep=1;
		Name names1 = null;
		String germplasmName="";
		Object gid=""; 
		hmOfGIdAndNval=new HashMap<Object, String>();
		for(int n=0;n<listOfNIDs.size();n++){			
			names1=genoManager.getNameByNameId(Integer.parseInt(listOfNIDs.get(n).toString()));
			if(names1 != null){
				if(gDupNameList.contains(names1.getGermplasmId())){
					germplasmName=names1.getNval()+" (Sample "+rep+")";
					gid=names1.getGermplasmId()+"~~!!~~"+rep;
					rep++;
				}else{
					rep=1;
					germplasmName=names1.getNval();
					gid=names1.getGermplasmId()+"~~!!~~"+"1";
				}
				hmOfGIdAndNval.put(gid, germplasmName);
			}
		}	
		//System.out.println("hmOfGIdAndNval:"+hmOfGIdAndNval);
	}


	public File getGeneratedTextFile() {
		return generatedTextFile;
	}

	public File getGeneratedMapFile() {
		return generatedMapFile;
	}

	public File getGeneratedDatFile() {
		return generatedDatFile;
	}

	public String getStrBMSFilePath(){
		return folderPath;
	}
	

	public String getQTLExists(){
		return strQTLExists;
	}
	
	
	private void retrieveAllelicValuesBasedOnMarkerType() throws MiddlewareQueryException {

		listIfAllelicValueElements = new ArrayList<AllelicValueElement>();
		List<AllelicValueElement> listOfAlleleValuesLocal = new ArrayList<AllelicValueElement>();
		List<AllelicValueElement> listOfAlleleValuesCentral = new ArrayList<AllelicValueElement>();
		if (strMarkerType.equalsIgnoreCase("SSR") || strMarkerType.equalsIgnoreCase("DArT")){

			//"select gid,marker_id, allele_bin_value from gdms_allele_values where gid in("+pgids.substring(0,pgids.length()-1)+") and marker_id in("+mid.substring(0,mid.length()-1)+") order by gid, marker_id"
			AlleleValuesDAO alleleValuesDAOLocal = new AlleleValuesDAO();
			alleleValuesDAOLocal.setSession(localSession);
			AlleleValuesDAO alleleValuesDAOCentral = new AlleleValuesDAO();
			alleleValuesDAOCentral.setSession(centralSession);

			long countIntAlleleValuesLocal = alleleValuesDAOLocal.countIntAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs);
			listOfAlleleValuesLocal = alleleValuesDAOLocal.getIntAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs, 0, (int)countIntAlleleValuesLocal);

			long countIntAlleleValuesCentral = alleleValuesDAOCentral.countIntAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs);
			listOfAlleleValuesCentral = alleleValuesDAOCentral.getIntAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs, 0, (int)countIntAlleleValuesCentral);
		} else if (strMarkerType.equalsIgnoreCase("SNP")){

			//"select gid,marker_id, char_value from gdms_char_values where gid in("+pgids.substring(0,pgids.length()-1)+") and marker_id in("+mid.substring(0,mid.length()-1)+") order by gid, marker_id"
			AlleleValuesDAO alleleValuesDAOLocal = new AlleleValuesDAO();
			alleleValuesDAOLocal.setSession(localSession);
			AlleleValuesDAO alleleValuesDAOCentral = new AlleleValuesDAO();
			alleleValuesDAOCentral.setSession(centralSession);

			long countIntAlleleValuesLocal = alleleValuesDAOLocal.countCharAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs);
			listOfAlleleValuesLocal = alleleValuesDAOLocal.getCharAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs, 0, (int)countIntAlleleValuesLocal);

			long countIntAlleleValuesCentral = alleleValuesDAOCentral.countCharAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs);
			listOfAlleleValuesCentral = alleleValuesDAOCentral.getCharAlleleValuesForPolymorphicMarkersRetrieval(listOfGIDs, 0, (int)countIntAlleleValuesCentral);

		}

		for (AllelicValueElement allelicValueElement : listOfAlleleValuesLocal){
			if (false == listIfAllelicValueElements.contains(allelicValueElement)){
				listIfAllelicValueElements.add(allelicValueElement);
			}
		}
		for (AllelicValueElement allelicValueElement : listOfAlleleValuesCentral){
			if (false == listIfAllelicValueElements.contains(allelicValueElement)){
				listIfAllelicValueElements.add(allelicValueElement);
			}
		}

		////System.out.println("Size of list of list of AllelicValueElements for " + strMarkerType + " type are : " + 
				//listIfAllelicValueElements.size());


		for (AllelicValueElement allelicValueElement : listIfAllelicValueElements){
			Integer gid = allelicValueElement.getGid();
			String markerName = allelicValueElement.getMarkerName();
			String alleleBinValue = allelicValueElement.getAlleleBinValue();
			String data = allelicValueElement.getData();
		}

	}

	/*private void retrieveParentGIDsAndGNamesForAllelicType() throws MiddlewareQueryException {

		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);

		List<Integer> listOfNameIdsByGermplasmIdsLocal = accMetadataSetDAOLocal.getNameIdsByGermplasmIds(listOfAllParentGIDs);
		List<Integer> listOfNameIdsByGermplasmIdsCentral = accMetadataSetDAOCentral.getNameIdsByGermplasmIds(listOfAllParentGIDs);

		if (null == listOfGIDs){
			listOfGIDs = new ArrayList<Integer>();
		}

		for (Integer iGID : listOfNameIdsByGermplasmIdsLocal){
			if (false == listOfGIDs.contains(iGID)){
				listOfGIDs.add(iGID);
			}
		}
		for (Integer iGID : listOfNameIdsByGermplasmIdsCentral){
			if (false == listOfGIDs.contains(iGID)){
				listOfGIDs.add(iGID);
			}
		}

		retrieveGermplasmNames(); //Adds GIDs and GNames to hmOfGIdsAndNval

	}*/

	private void retrieveQTLDataForFlapjack() throws MiddlewareQueryException {
		//ResultSet rsMap=st.executeQuery("select qtl_id from gdms_qtl_details where map_id =(select map_id from gdms_map where map_name ='"+mapName+"')");
		QtlDetailsDAO qtlDetailsDAOLocal = new QtlDetailsDAO();
		qtlDetailsDAOLocal.setSession(localSession);
		QtlDetailsDAO qtlDetailsDAOCentral = new QtlDetailsDAO();
		qtlDetailsDAOCentral.setSession(centralSession);

		List<QtlDetails> listOfAllQTLsLocal = qtlDetailsDAOLocal.getAll();
		List<QtlDetails> listOfAllQTLsCentral = qtlDetailsDAOCentral.getAll();
		listOfAllQTLDetails = new ArrayList<QtlDetailElement>();
		ArrayList<Integer> listOfAllQTLIDs = new ArrayList<Integer>();

		hmOfQtlPosition = new HashMap<Integer, String>();
		
		//hmOfQtlIdName = new HashMap<QtlDetailsPK, String>();
		
		ArrayList QTLNames=new ArrayList();
		////System.out.println("...............iMapId:"+iMapId);
		
		
		for (QtlDetails qtlDetails : listOfAllQTLsLocal){
		
			/*QtlDetailsPK qtlPK = qtlDetails.getId();			
			Integer mapId = qtlPK.getMapId();*/
			Integer mapId =qtlDetails.getMapId();
			if (mapId.equals(iMapId)){
				/*if (false == listOfAllQTLDetails.contains(qtlDetails)) {
					listOfAllQTLDetails.add(qtlDetails);
				}*/
				//QTLNames.add(qtlDetails.getId(), qtlDetails.getPosition().toString());
				
				
				//Integer qtlId = qtlPK.getQtlId();
				Integer qtlId =qtlDetails.getQtlId();
				if (false == listOfAllQTLIDs.contains(qtlId)){
					hmOfQtlPosition.put(qtlId, qtlDetails.getPosition().toString());
					listOfAllQTLIDs.add(qtlId);
				}
			}
		}
		for (QtlDetails qtlDetails : listOfAllQTLsCentral){
			/*QtlDetailsPK qtlPK = qtlDetails.getId();
			Integer mapId = qtlPK.getMapId();*/
			Integer mapId=qtlDetails.getMapId();
			if (mapId.equals(iMapId)){
				/*if (false == listOfAllQTLDetails.contains(qtlDetails)) {
					listOfAllQTLDetails.add(qtlDetails);
				}*/
				qtlDetails.getPosition();
				//Integer qtlId = qtlPK.getQtlId();
				Integer qtlId = qtlDetails.getQtlId();
				if (false == listOfAllQTLIDs.contains(qtlId)){
					hmOfQtlPosition.put(qtlId, qtlDetails.getPosition().toString());
					listOfAllQTLIDs.add(qtlId);
				}
			}
		}

		////System.out.println("listOfAllQTLDetails=:"+listOfAllQTLDetails);
		
		QtlDAO qtlDAOLocal = new QtlDAO();
		qtlDAOLocal.setSession(localSession);
		QtlDAO qtlDAOCentral = new QtlDAO();
		qtlDAOCentral.setSession(centralSession);

		List<Qtl> listOfAllQtlsLocal = qtlDAOLocal.getAll();
		List<Qtl> listOfAllQtlsCentral = qtlDAOCentral.getAll();

		hmOfQtlIdandName = new HashMap<Integer, String>();
		hmOfQtlNameId = new HashMap<String, Integer>();
		
		for (Integer iQtlId : listOfAllQTLIDs){
			for (Qtl qtlLocal : listOfAllQtlsLocal){
				Integer qtlId = qtlLocal.getQtlId();
				if (iQtlId.equals(qtlId)){
					String qtlName = qtlLocal.getQtlName();
					if (false == hmOfQtlIdandName.containsKey(qtlId)){
						hmOfQtlNameId.put(qtlName, qtlId);
						hmOfQtlIdandName.put(qtlId, qtlName);
					}
				}
			}

			for (Qtl qtlCentral : listOfAllQtlsCentral){
				Integer qtlId = qtlCentral.getQtlId();
				if (iQtlId.equals(qtlId)){
					String qtlName = qtlCentral.getQtlName();
					if (false == hmOfQtlIdandName.containsKey(qtlId)){
						hmOfQtlNameId.put(qtlName, qtlId);
						hmOfQtlIdandName.put(qtlId, qtlName);
					}
					
				
				}
			}
		}
		//getAllelicValuesByGidsAndMarkerNames
		

		listOfQtlDetailElementByQtlIds = genoManager.getQtlByQtlIds(listOfAllQTLIDs, 0, (int)listOfAllQTLIDs.size());
	//}
		////System.out.println(",,,,,,,,,,,,,,,,,,,:"+listOfAllQTLDetails);
	
	if (null != listOfQtlDetailElementByQtlIds) {
		for (QtlDetailElement qtlDetailElement : listOfQtlDetailElementByQtlIds) {
			String strMapName = qtlDetailElement.getMapName();
			String strTRName = qtlDetailElement.gettRName();
			
			String strChromosome = qtlDetailElement.getChromosome();
			String strMinPosition = String.valueOf(qtlDetailElement.getMinPosition().floatValue());
			String strMaxPosition = String.valueOf(qtlDetailElement.getMaxPosition().floatValue());
			
			/*
			String strQTLData = strQtlName + "!~!" + strMapName + "!~!" + strTRName + "!~!" + strChromosome +
					               "!~!" +  strMinPosition + "!~!" + strMaxPosition ;
			arrayListOfQTLRetrieveDataLocal.add(qtlDetailElement);*/
			////System.out.println(qtlDetailElement);
			listOfAllQTLDetails.add(qtlDetailElement);
		}
	}
		
		
		
		/*for (QtlDetails qtlDetails : listOfAllQTLsLocal){
			QtlDetailsPK qtlPK = qtlDetails.getId();
			Integer mapId = qtlPK.getMapId();
			if (mapId.equals(iMapId)){
				Integer qtlId = qtlPK.getQtlId();
				if (false == listOfAllQtlIDs.contains(qtlId)) {
					listOfAllQtlIDs.add(qtlId);
				}
			}
		}
		for (QtlDetails qtlDetails : listOfAllQTLsCentral){
			QtlDetailsPK qtlPK = qtlDetails.getId();
			Integer mapId = qtlPK.getMapId();
			if (mapId.equals(iMapId)){
				Integer qtlId = qtlPK.getQtlId();
				if (false == listOfAllQtlIDs.contains(qtlId)) {
					listOfAllQtlIDs.add(qtlId);
				}
			}
		}*/

		if (listOfAllQTLDetails.size() > 0){
			bQTLExists = true;
			/*for (Integer iQtlID : listOfAllQtlIDs){
				QtlDetails qtlDetailsLocal = qtlDetailsDAOLocal.getById(iQtlID, false);
				QtlDetails qtlDetailsCentral = qtlDetailsDAOCentral.getById(iQtlID, false);
				if (false == listOfAllQTLDetails.contains(qtlDetailsLocal)){
					listOfAllQTLDetails.add(qtlDetailsLocal);
				}
				if (false == listOfAllQTLDetails.contains(qtlDetailsCentral)){
					listOfAllQTLDetails.add(qtlDetailsLocal);
				}
			}*/



		} else {
			bQTLExists = false;
		}
		//Error generating Flapjack.txt file
		//System.out.println("bQTLExists:"+bQTLExists);
	}

	private void retrieveMapDataForFlapjack() throws MiddlewareQueryException {
		
		listOfAllMapInfo=new ArrayList();
		listOfMarkersinMap=new ArrayList();
		if(strSelectedMap.isEmpty()){
			for(int m=0; m<listOfMarkersForGivenGermplasmRetrieval.size(); m++){				
				listOfAllMapInfo.add(listOfMarkersForGivenGermplasmRetrieval.get(m)+"!~!"+"unmapped"+"!~!"+"0");				
			}			
		        
		}else{
			//System.out.println("map selected ");
			 
			 List<MapInfo> results = genoManager.getMapInfoByMapName(strSelectedMap, Database.CENTRAL);
		     if(results.isEmpty()) {
		    	 results = genoManager.getMapInfoByMapName(strSelectedMap, Database.LOCAL);
		     }
			 
		     listOfAllMapInfo = new ArrayList();
			 //System.out.println("testGetMapInfoByMapName(mapName=" + strSelectedMap + ") RESULTS size: " + results.size());
	        for (MapInfo mapInfo : results){
	        	listOfMarkersinMap.add(mapInfo.getMapName().toLowerCase());	        	
	        	//System.out.println(".................mapInfo."+results);
	        	listOfAllMapInfo.add(mapInfo.getMarkerName()+"!~!"+mapInfo.getLinkageGroup()+"!~!"+mapInfo.getStartPosition());
	        }	
	        
	        for(int m=0; m<listOfMarkersForGivenGermplasmRetrieval.size(); m++){
	        	if(!(listOfMarkersinMap.contains(listOfMarkersForGivenGermplasmRetrieval.get(m).toString().toLowerCase()))){
	        		//listOfAllMapInfo.add(sortedMapOfMIDsAndMNames.get(listOfMarkersForGivenGermplasmRetrieval.get(m))+"!~!"+"unmapped"+"!~!"+"0");		
	        		listOfAllMapInfo.add(listOfMarkersForGivenGermplasmRetrieval.get(m)+"!~!"+"unmapped"+"!~!"+"0");
	        	}
			}
	        //System.out.println("............:"+listOfAllMapInfo);
	        
		}
		
	}


	private void retrieveValuesForSNPDatasetType() throws NumberFormatException, MiddlewareQueryException {
		int iDatasetId = Integer.parseInt(strDatasetID);		
		//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS  :"+iDatasetId+"   "+strDatasetID);
		/*listOfAllelicValuesForMappingType = new ArrayList<AllelicValueWithMarkerIdElement>();
		allelicValues=new ArrayList();
		//System.out.println("@#@#@#   :"+genoManager.getAllelicValuesFromCharValuesByDatasetId(iDatasetId, 0, (int)genoManager.countAllelicValuesFromCharValuesByDatasetId(iDatasetId)));
		allelicValues =genoManager.getAllelicValuesFromCharValuesByDatasetId(iDatasetId, 0, (int)genoManager.countAllelicValuesFromCharValuesByDatasetId(iDatasetId));
		
		marker = new HashMap();
		if (null != allelicValues){
			for (AllelicValueWithMarkerIdElement allelicValueElement : allelicValues){
				if(!(midslist.contains(markerIDsNamesMap.get(allelicValueElement.getMarkerId()))))
					midslist.add(markerIDsNamesMap.get(allelicValueElement.getMarkerId()));
				
				//data=data+allelicValueElement.getGid()+"~!~"+allelicValueElement.getData()+"~!~"+markerIDsNamesMap.get(allelicValueElement.getMarkerId())+"!~!";
				markerAlleles.put(allelicValueElement.getGid()+"!~!"+allelicValueElement.getMarkerId(), allelicValueElement.getData());
				
				if(!(glist.contains(allelicValueElement.getGid())))
					glist.add(allelicValueElement.getGid());					
			}
			
			List markerKey = new ArrayList();
			markerKey.addAll(markerAlleles.keySet());
			for(int g=0; g<glist.size(); g++){
				for(int i=0; i<markerKey.size();i++){
					 if(!(mapEx.get(Integer.parseInt(glist.get(g).toString()))==null)){
						 marker = (HashMap)mapEx.get(Integer.parseInt(glist.get(g).toString()));
					 }else{
					marker = new HashMap();
					 }
					 if(Integer.parseInt(glist.get(g).toString())==Integer.parseInt(markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!")))){
						 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
						 mapEx.put(Integer.parseInt(glist.get(g).toString()),(HashMap)marker);
					 }						
				}	
			}				
		}
		*/
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		List allelesFromLocal=new ArrayList();	
		List allelesFromCentral=new ArrayList();	
		List parentsData=new ArrayList();		
		List allelesList=new ArrayList();
		//List gidsList=new ArrayList();
		Object objAL=null;
		Object objAC=null;
		Iterator itListAC=null;
		Iterator itListAL=null;
		
		markerAlleles= new HashMap<String,Object>();
		strQuerry="select distinct gid,marker_id, char_value,acc_sample_id,marker_sample_id from gdms_char_values where dataset_id="+strDatasetID+" ORDER BY gid, marker_id,acc_sample_id asc";	
		System.out.println("strQuerry:"+strQuerry);
		queryL=localSession.createSQLQuery(strQuerry);		
		queryL.addScalar("gid",Hibernate.INTEGER);	 
		queryL.addScalar("marker_id",Hibernate.INTEGER);
		queryL.addScalar("char_value",Hibernate.STRING);	
		queryL.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryL.addScalar("marker_sample_id",Hibernate.INTEGER);				
		allelesFromLocal=queryL.list();
		
		
		queryC=centralSession.createSQLQuery(strQuerry);
		queryC.addScalar("gid",Hibernate.INTEGER);	 
		queryC.addScalar("marker_id",Hibernate.INTEGER);
		queryC.addScalar("char_value",Hibernate.STRING);
		queryC.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryC.addScalar("marker_sample_id",Hibernate.INTEGER);
		allelesFromCentral=queryC.list();
		marker = new HashMap();
		String acc_id="";
		for(int w=0;w<allelesFromCentral.size();w++){
			
			Object[] strMareO= (Object[])allelesFromCentral.get(w);
			//if(strMareO[3].equals(null)||strMareO[3].equals("null")||strMareO[3].equals(""))
			if(strMareO[3]==null ||strMareO[3]== "null" || strMareO[3]=="")
				acc_id="1";
			else
				acc_id=strMareO[3].toString();
			markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			
			if(!(glist.contains(strMareO[0]+"~~!!~~"+acc_id)))
				glist.add(strMareO[0]+"~~!!~~"+acc_id);
																			
		}
		
		for(int w=0;w<allelesFromLocal.size();w++){
			Object[] strMareO= (Object[])allelesFromLocal.get(w);	
			
			if(strMareO[3]==null ||strMareO[3]== "null" || strMareO[3]=="")
				acc_id="1";
			else
				acc_id=strMareO[3].toString();
			markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			
			if(!(glist.contains(strMareO[0]+"~~!!~~"+acc_id)))
				glist.add(strMareO[0]+"~~!!~~"+acc_id);
																			
		}
		//System.out.println("markerAlleles:"+markerAlleles);
		//System.out.println("glist:"+glist);
		
		List markerKey = new ArrayList();
		markerKey.addAll(markerAlleles.keySet());
		for(int g=0; g<glist.size(); g++){
			for(int i=0; i<markerKey.size();i++){
				 if(!(mapEx.get(glist.get(g))==null)){
					 marker = (HashMap)mapEx.get(glist.get(g));
				 }else{
					 marker = new HashMap();
				 }
				 Object mKey=markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!"));
				 if(glist.get(g).equals(mKey)){
					 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
					 mapEx.put(glist.get(g),(HashMap)marker);
				 }						
			}	
		}
	}

	private void retrieveValuesForSSRandDArtDatasetType() throws MiddlewareQueryException {
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		/*listOfAllelicValuesForMappingType = new ArrayList<AllelicValueWithMarkerIdElement>();
		allelicValues=new ArrayList();
		int iDatasetId = Integer.parseInt(strDatasetID);
		//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSS  :"+iDatasetId+"   "+strDatasetID);
		allelicValues =genoManager.getAllelicValuesFromAlleleValuesByDatasetId(iDatasetId, 0, (int)genoManager.countAllelicValuesFromAlleleValuesByDatasetId(iDatasetId));
		marker = new HashMap();
		if (null != allelicValues){
			for (AllelicValueWithMarkerIdElement allelicValueElement : allelicValues){
				if(!(midslist.contains(markerIDsNamesMap.get(allelicValueElement.getMarkerId()))))
					midslist.add(markerIDsNamesMap.get(allelicValueElement.getMarkerId()));
				
				//data=data+allelicValueElement.getGid()+"~!~"+allelicValueElement.getData()+"~!~"+markerIDsNamesMap.get(allelicValueElement.getMarkerId())+"!~!";
				markerAlleles.put(allelicValueElement.getGid()+"!~!"+allelicValueElement.getMarkerId(), allelicValueElement.getData());
				
				if(!(glist.contains(allelicValueElement.getGid())))
					glist.add(allelicValueElement.getGid());					
			}
			
			List markerKey = new ArrayList();
			markerKey.addAll(markerAlleles.keySet());
			for(int g=0; g<glist.size(); g++){
				for(int i=0; i<markerKey.size();i++){
					 if(!(mapEx.get(Integer.parseInt(glist.get(g).toString()))==null)){
						 marker = (HashMap)mapEx.get(Integer.parseInt(glist.get(g).toString()));
					 }else{
					marker = new HashMap();
					 }
					 if(Integer.parseInt(glist.get(g).toString())==Integer.parseInt(markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!")))){
						 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
						 mapEx.put(Integer.parseInt(glist.get(g).toString()),(HashMap)marker);
					 }						
				}	
			}				
		}
		
		//System.out.println("mapEx:"+mapEx);		
		
		for(AllelicValueWithMarkerIdElement results : allelicValues) {			
			intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+results.getData());
        }		*/
		
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		List allelesFromLocal=new ArrayList();	
		List allelesFromCentral=new ArrayList();	
		List parentsData=new ArrayList();		
		List allelesList=new ArrayList();
		//List gidsList=new ArrayList();
		Object objAL=null;
		Object objAC=null;
		Iterator itListAC=null;
		Iterator itListAL=null;
		
		markerAlleles= new HashMap<String,Object>();
		strQuerry="select distinct gid,marker_id, allele_bin_value,acc_sample_id,marker_sample_id from gdms_allele_values where dataset_id="+strDatasetID+" ORDER BY gid, marker_id,acc_sample_id asc";
		queryL=localSession.createSQLQuery(strQuerry);		
		queryL.addScalar("gid",Hibernate.INTEGER);	 
		queryL.addScalar("marker_id",Hibernate.INTEGER);
		queryL.addScalar("allele_bin_value",Hibernate.STRING);	
		queryL.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryL.addScalar("marker_sample_id",Hibernate.INTEGER);				
		allelesFromLocal=queryL.list();
		
		
		queryC=centralSession.createSQLQuery(strQuerry);
		queryC.addScalar("gid",Hibernate.INTEGER);	 
		queryC.addScalar("marker_id",Hibernate.INTEGER);
		queryC.addScalar("allele_bin_value",Hibernate.STRING);
		queryC.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryC.addScalar("marker_sample_id",Hibernate.INTEGER);
		allelesFromCentral=queryC.list();
		marker = new HashMap();
		String acc_id="0";
		for(int w=0;w<allelesFromCentral.size();w++){
			Object[] strMareO= (Object[])allelesFromCentral.get(w);
			if(strMareO[3]==null ||strMareO[3]== "null" || strMareO[3]=="")
				acc_id="1";
			else
				acc_id=strMareO[3].toString();
			
			markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			
			if(!(glist.contains(strMareO[0]+"~~!!~~"+acc_id)))
				glist.add(strMareO[0]+"~~!!~~"+acc_id);
																			
		}
		
		for(int w=0;w<allelesFromLocal.size();w++){
			Object[] strMareO= (Object[])allelesFromLocal.get(w);	
			if(strMareO[3]==null ||strMareO[3]== "null" || strMareO[3]=="")
				acc_id="1";
			else
				acc_id=strMareO[3].toString();
			markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
			
			if(!(glist.contains(strMareO[0]+"~~!!~~"+acc_id)))
				glist.add(strMareO[0]+"~~!!~~"+acc_id);
																			
		}
		//System.out.println("markerAlleles:"+markerAlleles);
		//System.out.println("glist:"+glist);
		
		List markerKey = new ArrayList();
		markerKey.addAll(markerAlleles.keySet());
		for(int g=0; g<glist.size(); g++){
			for(int i=0; i<markerKey.size();i++){
				 if(!(mapEx.get(glist.get(g))==null)){
					 marker = (HashMap)mapEx.get(glist.get(g));
				 }else{
					 marker = new HashMap();
				 }
				 Object mKey=markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!"));
				 if(glist.get(g).equals(mKey)){
					 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
					 mapEx.put(glist.get(g),(HashMap)marker);
				 }						
			}	
		}
		
		
	}

	private void retrieveValuesForMappingDatasetType() throws MiddlewareQueryException {
		
		System.out.println(",,,,,,,,,,,,,,,,...............................,,,,,,,,,,,,,,,,,,,,,,,,,");
		listOfAllelicValuesForMappingType = new ArrayList<AllelicValueWithMarkerIdElement>();
		int iDatasetId = Integer.parseInt(strDatasetID);
		allelicValues=new ArrayList();	
		
		
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		List allelesFromLocal=new ArrayList();	
		List allelesFromCentral=new ArrayList();	
		List parentsData=new ArrayList();		
		List allelesList=new ArrayList();
		//List gidsList=new ArrayList();
		Object objAL=null;
		Object objAC=null;
		Iterator itListAC=null;
		Iterator itListAL=null;
		
		markerAlleles= new HashMap<String,Object>();
		strQuerry="select distinct gid,marker_id, map_char_value,acc_sample_id,marker_sample_id from gdms_mapping_pop_values where dataset_id="+strDatasetID+" ORDER BY gid, marker_id,acc_sample_id asc";
		queryL=localSession.createSQLQuery(strQuerry);		
		queryL.addScalar("gid",Hibernate.INTEGER);	 
		queryL.addScalar("marker_id",Hibernate.INTEGER);
		queryL.addScalar("map_char_value",Hibernate.STRING);	
		queryL.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryL.addScalar("marker_sample_id",Hibernate.INTEGER);				
		allelesFromLocal=queryL.list();
		
		
		queryC=centralSession.createSQLQuery(strQuerry);
		queryC.addScalar("gid",Hibernate.INTEGER);	 
		queryC.addScalar("marker_id",Hibernate.INTEGER);
		queryC.addScalar("map_char_value",Hibernate.STRING);
		queryC.addScalar("acc_sample_id",Hibernate.INTEGER);
		queryC.addScalar("marker_sample_id",Hibernate.INTEGER);
		allelesFromCentral=queryC.list();
		System.out.println("strSelectedMappingType:"+strSelectedMappingType);
		if(!strSelectedMappingType.isEmpty()){
			System.out.println("NOT EQUAL NULL");
			if(strSelectedMappingType.equalsIgnoreCase("abh")){	
				try {				
					/*alleleValues = genoManager.getAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID), 0, (int)genoManager.countAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID)));
					marker = new HashMap();
					if (null != alleleValues){
						for(AllelicValueWithMarkerIdElement results : alleleValues) {	
							if(!(midslist.contains(results.getMarkerId())))
								midslist.add(results.getMarkerId());
							if((results.getData().equals("-"))||(results.getData().equals("?"))||(results.getData().equals("null"))||(results.getData().equals("NR"))){
								intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+results.getData());
								markerAlleles.put(results.getGid()+"!~!"+results.getMarkerId(), results.getData());
								
							}else{
								if(mapA.containsKey(Integer.parseInt(results.getMarkerId().toString()))){
									if(mapA.get(Integer.parseInt(results.getMarkerId().toString())).equals(results.getData().toString())){	
										//System.out.println("PopAllelevalue is matching with Parent A");
										intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+"A");
										//data=data+results.getGid()+"~!~"+"A"+"~!~"+markerIDsNamesMap.get(results.getMarkerId())+"!~!";
										markerAlleles.put(results.getGid()+"!~!"+results.getMarkerId(), "A");
									}else if(mapB.get(Integer.parseInt(results.getMarkerId().toString())).equals(results.getData().toString())){	
										//System.out.println("PopAllelevalue is matching with Parent B");
										intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+"B");	
										//data=data+results.getGid()+"~!~"+"B"+"~!~"+markerIDsNamesMap.get(results.getMarkerId())+"!~!";
										markerAlleles.put(results.getGid()+"!~!"+results.getMarkerId(), "B");
									}else{	
										//System.out.println("Not matching with both the parents");
										intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+"A/B");
										//data=data+results.getGid()+"~!~"+"A/B"+"~!~"+markerIDsNamesMap.get(results.getMarkerId())+"!~!";
										markerAlleles.put(results.getGid()+"!~!"+results.getMarkerId(), "A/B");
									}	
								}
							}
							if(!(glist.contains(results.getGid())))
								glist.add(results.getGid());
				        }
										
									
					}*/
					
					marker = new HashMap();
					
					for(int w=0;w<allelesFromCentral.size();w++){
						String acc_id="0";
						Object[] strMareO= (Object[])allelesFromCentral.get(w);
						Object strData=strMareO[2];
						if(strMareO[3]==null ||strMareO[3]== "null" || strMareO[3]=="")
							acc_id="1";
						else
							acc_id=strMareO[3].toString();
						if((strData.equals("-"))||(strData.equals("?"))||(strData.equals("null"))||(strData.equals("NR"))){							
							markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());							
						}else{
							if(mapA.containsKey(Integer.parseInt(strMareO[1].toString()))){
								if(mapA.get(Integer.parseInt(strMareO[1].toString())).equals(strData.toString())){										
									markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), "A");
								}else if(mapB.get(Integer.parseInt(strMareO[1].toString())).equals(strData.toString())){										
									markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), "B");
								}else{										
									markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), "A/B");
								}	
							}
						}
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+acc_id)))
							glist.add(strMareO[0]+"~~!!~~"+acc_id);
																						
					}
					
					for(int w=0;w<allelesFromLocal.size();w++){
						String acc_id="0";
						Object[] strMareO= (Object[])allelesFromLocal.get(w);	
						
						Object strData=strMareO[2];
						
						if(strMareO[3]==null ||strMareO[3]== "null" || strMareO[3]=="")
							acc_id="1";
						else
							acc_id=strMareO[3].toString();
						
						if((strData.equals("-"))||(strData.equals("?"))||(strData.equals("null"))||(strData.equals("NR"))){							
							markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());							
						}else{
							if(mapA.containsKey(Integer.parseInt(strMareO[1].toString()))){
								if(mapA.get(Integer.parseInt(strMareO[1].toString())).equals(strData.toString())){										
									markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), "A");
								}else if(mapB.get(Integer.parseInt(strMareO[1].toString())).equals(strData.toString())){										
									markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), "B");
								}else{										
									markerAlleles.put(strMareO[0]+"~~!!~~"+acc_id+"!~!"+Integer.parseInt(strMareO[1].toString()), "A/B");
								}	
							}
						}
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+acc_id)))
							glist.add(strMareO[0]+"~~!!~~"+acc_id);
																						
					}
					/*System.out.println("markerAlleles:"+markerAlleles);
					System.out.println("glist:"+glist);*/
					
					List markerKey = new ArrayList();
					markerKey.addAll(markerAlleles.keySet());
					for(int g=0; g<glist.size(); g++){
						for(int i=0; i<markerKey.size();i++){
							 if(!(mapEx.get(glist.get(g))==null)){
								 marker = (HashMap)mapEx.get(glist.get(g));
							 }else{
								 marker = new HashMap();
							 }
							 Object mKey=markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!"));
							 if(glist.get(g).equals(mKey)){
								 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
								 mapEx.put(glist.get(g),(HashMap)marker);
							 }						
						}	
					}					
				} catch (Exception e) {
					/*String strErrMessage = "Error Retrieving Mapping AllelicValueElements for the selected Dataset";
					throw new GDMSException(strErrMessage);*/
					_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Mapping AllelicValueElements for the selected Dataset");
					return;
				}				
			}else{
				//System.out.println(".............................");
				try {				
					/*alleleValues = genoManager.getAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID), 0, (int)genoManager.countAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID)));
					for(AllelicValueWithMarkerIdElement results : alleleValues) {							
						intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+results.getData());
			        }
					
					marker = new HashMap();
					if (null != alleleValues){
						for (AllelicValueWithMarkerIdElement allelicValueElement : alleleValues){
							markerAlleles.put(allelicValueElement.getGid()+"!~!"+allelicValueElement.getMarkerId(), allelicValueElement.getData());
							
							if(!(glist.contains(allelicValueElement.getGid())))
								glist.add(allelicValueElement.getGid());					
						}
						
						List markerKey = new ArrayList();
						markerKey.addAll(markerAlleles.keySet());
						for(int g=0; g<glist.size(); g++){
							for(int i=0; i<markerKey.size();i++){
								 if(!(mapEx.get(glist.get(g))==null)){
									 marker = (HashMap)mapEx.get(glist.get(g));
								 }else{
								marker = new HashMap();
								 }
								 if(glist.get(g)==markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!"))){
									 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
									 mapEx.put(Integer.parseInt(glist.get(g).toString()),(HashMap)marker);
								 }						
							}	
						}				
					}*/
					String sampleAccID="";
					marker = new HashMap();
					for(int w=0;w<allelesFromCentral.size();w++){
						Object[] strMareO= (Object[])allelesFromCentral.get(w);
						if(strMareO[3]==null)
							sampleAccID="1";
						else
							sampleAccID=strMareO[3].toString();	
						markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+sampleAccID)))
							glist.add(strMareO[0]+"~~!!~~"+sampleAccID);																						
					}
					
					for(int w=0;w<allelesFromLocal.size();w++){
						Object[] strMareO= (Object[])allelesFromLocal.get(w);		
						if(strMareO[3]==null)
							sampleAccID="1";
						else
							sampleAccID=strMareO[3].toString();	
						markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+sampleAccID)))
							glist.add(strMareO[0]+"~~!!~~"+sampleAccID);
																						
					}
					
					List markerKey = new ArrayList();
					markerKey.addAll(markerAlleles.keySet());
					for(int g=0; g<glist.size(); g++){
						for(int i=0; i<markerKey.size();i++){
							 if(!(mapEx.get(glist.get(g))==null)){
								 marker = (HashMap)mapEx.get(glist.get(g));
							 }else{
								 marker = new HashMap();
							 }
							 Object mKey=markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!"));
							 if(glist.get(g).equals(mKey)){
								 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
								 mapEx.put(glist.get(g),(HashMap)marker);
							 }						
						}	
					}									
				} catch (Exception e) {
					/*String strErrMessage = "Error Retrieving Mapping AllelicValueElements for the selected Dataset";
					throw new GDMSException(strErrMessage);*/
					_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Mapping AllelicValueElements for the selected Dataset");
					return;
				}
			}
			
		}else{
			System.out.println("EQUAL NULL");
			System.out.println("MApping ...........");
			//genoManager.getAll
			String sampleAccID="";
			try {				
				alleleValues = genoManager.getAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID), 0, (int)genoManager.countAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID)));
				marker = new HashMap();
				if (null != alleleValues){
					for (AllelicValueWithMarkerIdElement allelicValueElement : alleleValues){
						/*if(!(midslist.contains(allelicValueElement.getMarkerId())))
							midslist.add(allelicValueElement.getMarkerId());
						*/
						//data=data+allelicValueElement.getGid()+"~!~"+allelicValueElement.getData()+"~!~"+markerIDsNamesMap.get(allelicValueElement.getMarkerId())+"!~!";
						
						if(allelicValueElement.getAccSampleId()==null)
							sampleAccID="1";
						else
							sampleAccID=allelicValueElement.getAccSampleId().toString();	
					
						
						markerAlleles.put(allelicValueElement.getGid()+"~~!!~~"+sampleAccID+"!~!"+allelicValueElement.getMarkerId(), allelicValueElement.getData());
						
						if(!(glist.contains(allelicValueElement.getGid()+"~~!!~~"+sampleAccID)))
							glist.add(allelicValueElement.getGid()+"~~!!~~"+sampleAccID);					
					}
														
					List markerKey = new ArrayList();
					markerKey.addAll(markerAlleles.keySet());
					for(int g=0; g<glist.size(); g++){
						for(int i=0; i<markerKey.size();i++){
							 if(!(mapEx.get(glist.get(g))==null)){
								 marker = (HashMap)mapEx.get(glist.get(g));
							 }else{
							marker = new HashMap();
							 }
							 if(glist.get(g).equals(markerKey.get(i).toString().substring(0, markerKey.get(i).toString().indexOf("!~!")))){
								 marker.put(markerKey.get(i), markerAlleles.get(markerKey.get(i)));
								 mapEx.put(glist.get(g),(HashMap)marker);
							 }						
						}	
					}				
				}
				
				
				
				
				/*
				for(AllelicValueWithMarkerIdElement results : alleleValues) {
					intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+results.getData());
		        }*/			
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Mapping AllelicValueElements for the selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				/*String strErrMessage = "Error Retrieving Mapping AllelicValueElements for the selected Dataset";
				throw new GDMSException(strErrMessage);
				*/
				_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Mapping AllelicValueElements for the selected Dataset");
				return;
				
			}
		}
		
		//System.out.println("intAlleleValues=:"+intAlleleValues);
	}
	
	
	
	
	

	private void retrieveMarkersForMarkerIDs() throws MiddlewareQueryException {
		MarkerDAO markerDAOLocal = new MarkerDAO();
		markerDAOLocal.setSession(localSession);
		MarkerDAO markerDAOCentral = new MarkerDAO();
		markerDAOCentral.setSession(centralSession);

		hmOfMIDandMNames = new HashMap<Integer, String>();
		listOfMarkerNames = new ArrayList<String>();


		long countMarkersByIdsLocal = markerDAOLocal.countMarkersByIds(listOfMarkerIdsForGivenDatasetID);
		List<Marker> listOfMarkersByIdsLocal = markerDAOLocal.getMarkersByIds(listOfMarkerIdsForGivenDatasetID, 0, (int)countMarkersByIdsLocal);

		long countMarkersByIdsCentral = markerDAOCentral.countMarkersByIds(listOfMarkerIdsForGivenDatasetID);
		List<Marker> listOfMarkersByIdsCentral = markerDAOCentral.getMarkersByIds(listOfMarkerIdsForGivenDatasetID, 0, (int)countMarkersByIdsCentral);

		listOfAllMarkersForGivenDatasetID = new ArrayList<Marker>();

		for (Marker marker : listOfMarkersByIdsLocal){
			if (false == listOfAllMarkersForGivenDatasetID.contains(marker)){
				listOfAllMarkersForGivenDatasetID.add(marker);
			}
		}
		for (Marker marker : listOfMarkersByIdsCentral){
			if (false == listOfAllMarkersForGivenDatasetID.contains(marker)){
				listOfAllMarkersForGivenDatasetID.add(marker);
			}
		}

		//System.out.println("Size of List of all Markers obtained for given Dataset-ID: " + listOfAllMarkersForGivenDatasetID.size());

		for (Marker marker : listOfAllMarkersForGivenDatasetID){
			Integer markerId = marker.getMarkerId();
			String markerName = marker.getMarkerName();

			if (false == hmOfMIDandMNames.containsKey(markerId)){
				hmOfMIDandMNames.put(markerId, markerName);
			}

			if (false == listOfMarkerNames.contains(markerName)){
				listOfMarkerNames.add(markerName);
			}
		}

		//System.out.println("Size of list of all Marker-Names: " + listOfMarkerNames.size());
		//System.out.println("Size of Hashmap of MIDs and MNames: " + hmOfMIDandMNames.size());
	}

	/*private void retrieveGermplasmNames() throws MiddlewareQueryException {
		NameDAO nameDAOLocal = new NameDAO();
		nameDAOLocal.setSession(localSession);
		NameDAO nameDAOCentral = new NameDAO();
		nameDAOCentral.setSession(centralSession);

		if (null == hmOfGIdsAndNval){
			hmOfGIdsAndNval = new HashMap<Integer, String>();
		}

		List<Name> listOfNamesByNameIdsLocal = nameDAOLocal.getNamesByNameIds(listOfGIDs);
		List<Name> listOfNamesByNameIdsCentral = nameDAOCentral.getNamesByNameIds(listOfGIDs);

		for (Name name : listOfNamesByNameIdsLocal){
			Integer nid = name.getNid();
			if (false == hmOfGIdsAndNval.containsKey(nid)){
				String nval = name.getNval();
				//hmOfGIdsAndNval.put(nid, nval);
				hmOfGIdsAndNval.put(name.getGermplasmId(), nval);
			}
		}

		for (Name name : listOfNamesByNameIdsCentral){
			Integer nid = name.getNid();
			if (false == hmOfGIdsAndNval.containsKey(nid)){
				String nval = name.getNval();
				hmOfGIdsAndNval.put(name.getGermplasmId(), nval);
			}
		}

		//System.out.println("Size of Germplasm Mapping - hashmap: " + hmOfGIdsAndNval.size());
	}
*/
	/*private void retrieveNIDsFor_SSR_SNP_DArt_DataTypes() throws MiddlewareQueryException {
		//"SELECT nid from gdms_acc_metadataset where dataset_id="+datasetId
		//List<Integer> 	getNIDsByDatasetIds(List<Integer> datasetIds, List<Integer> gids, int start, int numOfRows)

		listOfGIDs = new ArrayList<Integer>();

		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);


		long countAll = accMetadataSetDAOLocal.countAll();
		List<Integer> niDsByDatasetIds = accMetadataSetDAOLocal.getNIDsByDatasetIds(listOfDatasetIDs, new ArrayList<Integer>(), 0, (int)countAll);
		List<Integer> niDsByDatasetIds2 = accMetadataSetDAOCentral.getNIDsByDatasetIds(listOfDatasetIDs, new ArrayList<Integer>(), 0, (int)countAll);
		for (Integer iNid : niDsByDatasetIds){
			if (false == listOfGIDs.contains(iNid)){
				listOfGIDs.add(iNid);
			}
		}
		for (Integer iNid : niDsByDatasetIds2){
			if (false == listOfGIDs.contains(iNid)){
				listOfGIDs.add(iNid);
			}
		}

	}*/

	private void retrieveGermplasmNamesByGIDs() throws MiddlewareQueryException {
		
		ArrayList listOfGNames = new ArrayList<String>();
		ArrayList gDupNameList=new ArrayList<String>();
		
		List<Integer> nidsList = new ArrayList<Integer>();
		hmOfGIdsAndNval = new HashMap<Object, String>();
		//List<Integer> nids =genoManager.getNidsFromAccMetadatasetByDatasetIds(listOfDatasetIDs, 0, (int)genoManager.countNidsFromAccMetadatasetByDatasetIds(listOfDatasetIDs));
		List<AccMetadataSet> resAcc =genoManager.getAccMetadatasetsByDatasetIds(listOfDatasetIDs, 0, (int)genoManager.countAccMetadatasetByDatasetIds(listOfDatasetIDs));
		for(AccMetadataSet resAccMetadataset: resAcc){
			//parentsData.add(resAccMetadataset.getGermplasmId());	
			nidsList.add(resAccMetadataset.getNameId());
		}
		Name names = null;
		for(int n=0;n<nidsList.size();n++){
			names=germManager.getGermplasmNameByID(nidsList.get(n));
			hmOfGIdsAndNval.put(names.getGermplasmId(), names.getNval());
		}
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		HashMap<Object, Integer> hmGidSampleIdNid=new HashMap<Object, Integer>();
		List allelesFromLocal=new ArrayList();	
		List allelesFromCentral=new ArrayList();	
		List parentsData=new ArrayList();		
		List allelesList=new ArrayList();
		//List gidsList=new ArrayList();
		Object objAL=null;
		Object objAC=null;
		Iterator itListAC=null;
		Iterator itListAL=null;	
		strQuerry="select distinct gid,nid, acc_sample_id from gdms_acc_metadataset where dataset_id= "+ strDatasetID +" order by gid, nid,acc_sample_id asc";	
		queryL=localSession.createSQLQuery(strQuerry);		
		queryL.addScalar("gid",Hibernate.INTEGER);	 
		queryL.addScalar("nid",Hibernate.INTEGER);
		queryL.addScalar("acc_sample_id",Hibernate.INTEGER);				
		
		allelesFromLocal=queryL.list();
		
		
		queryC=centralSession.createSQLQuery(strQuerry);
		queryC.addScalar("gid",Hibernate.INTEGER);	 
		queryC.addScalar("nid",Hibernate.INTEGER);
		queryC.addScalar("acc_sample_id",Hibernate.INTEGER);
		allelesFromCentral=queryC.list();
		for(int w=0;w<allelesFromCentral.size();w++){
			Object[] strMareO= (Object[])allelesFromCentral.get(w);
			parentsData.add(Integer.parseInt(strMareO[0].toString()));	
			//if(!nidsList.contains(Integer.parseInt(strMareO[0].toString())))
				nidsList.add(Integer.parseInt(strMareO[1].toString()));
				
				if((strMareO[2]==null)||(Integer.parseInt(strMareO[2].toString()) == 0)){
					hmGidSampleIdNid.put(strMareO[0]+"~~!!~~1", Integer.parseInt(strMareO[1].toString()));
				}else{
					hmGidSampleIdNid.put(strMareO[0]+"~~!!~~"+strMareO[2], Integer.parseInt(strMareO[1].toString()));
					if(Integer.parseInt(strMareO[2].toString())==2){
						if(!gDupNameList.contains(Integer.parseInt(strMareO[0].toString())))
						gDupNameList.add(Integer.parseInt(strMareO[0].toString()));	
					}
				}
		}
		
		for(int w=0;w<allelesFromLocal.size();w++){
			Object[] strMareO= (Object[])allelesFromLocal.get(w);								
			parentsData.add(Integer.parseInt(strMareO[0].toString()));	
			//if(!nidsList.contains(Integer.parseInt(strMareO[0].toString())))
				nidsList.add(Integer.parseInt(strMareO[1].toString()));
				
				if((strMareO[2]==null)||(Integer.parseInt(strMareO[2].toString()) == 0)){
					hmGidSampleIdNid.put(strMareO[0]+"~~!!~~1", Integer.parseInt(strMareO[1].toString()));
				}else{
					hmGidSampleIdNid.put(strMareO[0]+"~~!!~~"+strMareO[2], Integer.parseInt(strMareO[1].toString()));
					if(Integer.parseInt(strMareO[2].toString())==2){
						if(!gDupNameList.contains(Integer.parseInt(strMareO[0].toString())))
						gDupNameList.add(Integer.parseInt(strMareO[0].toString()));	
					}
				}
		}
		for(int w=0;w<parentsData.size();w++){
			if(listOfGNames.contains(parentsData.get(w))){							
				gDupNameList.add(parentsData.get(w));							
			}
			
			listOfGNames.add(parentsData.get(w));
		
		}
		HashMap<Integer, String> hmNidGermplasmName=new HashMap<Integer, String>();
		int rep=1;
		Name names1 = null;
		String germplasmName="";
		String gids="";
		Object gid=""; 
		int germplasmIdentifier=0;
		hmOfGIdAndNval=new HashMap<Object, String>();
		
		for(int n=0;n<nidsList.size();n++){			
			names1=genoManager.getNameByNameId(Integer.parseInt(nidsList.get(n).toString()));
			if(names1 != null)
				hmNidGermplasmName.put(names1.getNid(), names1.getNval());	
			
		}
		
		
		Set<Object> gidKeySet1 = hmGidSampleIdNid.keySet();
		Iterator<Object> gidIterator1 = gidKeySet1.iterator();
		//String gids="";
		while (gidIterator1.hasNext()) {
			String gname="";
			Object gidSampleid = gidIterator1.next();
			Integer nid = hmGidSampleIdNid.get(gidSampleid);
			String strGName=hmNidGermplasmName.get(nid);
			String strGS=gidSampleid.toString();
			String strSampleId=strGS.substring(strGS.indexOf("~~!!~~")+6);
			Integer strGid=Integer.parseInt(strGS.substring(0, strGS.indexOf("~~!!~~")).toString());
			
			if(gDupNameList.contains(strGid)){
				//gidSampleid=gidSampleid;
				gname=strGName+" (Sample "+strSampleId+")";
				gids=strGid+" (Sample "+strSampleId+")";
				
			}else{
				gname=strGName;
				gids=strGid+"";
			}
			//System.out.println("str:"+gidSampleid+"   "+gname);
		
			hmOfGIdAndNval.put(gidSampleid, gname);
			hmOfGIDs.put(gidSampleid, gids);
			
		}
		
		
		
		//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$   :"+hmOfGIdAndNval);
	}

	private void retrieveNIDsUsingDatasetID() throws MiddlewareQueryException {

		//"SELECT nid from gdms_acc_metadataset where dataset_id="+datasetId+" and gid not in("+parentsNames+") order by nid"
		//Set<Integer> 	getNIdsByMarkerIdsAndDatasetIdsAndNotGIds(List<Integer> datasetIds, List<Integer> markerIds, List<Integer> gIds)

		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);

		listOfGIDs = new ArrayList<Integer>();
		Set<Integer> nIdsByMarkerIdsAndDatasetIdsAndNotGIdsLocal = accMetadataSetDAOLocal.getNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIDs, listOfMarkerIdsForGivenDatasetID, listOfAllParentGIDs, 0, (int)accMetadataSetDAOLocal.countNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIDs, listOfMarkerIdsForGivenDatasetID, listOfAllParentGIDs));
		Set<Integer> nIdsByMarkerIdsAndDatasetIdsAndNotGIdsCentral = accMetadataSetDAOCentral.getNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIDs, listOfMarkerIdsForGivenDatasetID, listOfAllParentGIDs, 0, (int)accMetadataSetDAOLocal.countNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIDs, listOfMarkerIdsForGivenDatasetID, listOfAllParentGIDs));

		Iterator<Integer> iteratorLocal = nIdsByMarkerIdsAndDatasetIdsAndNotGIdsLocal.iterator();
		while (iteratorLocal.hasNext()){
			Integer next = iteratorLocal.next();
			if (false == listOfGIDs.contains(next)){
				listOfGIDs.add(next);
			}
		}
		Iterator<Integer> iteratorCentral = nIdsByMarkerIdsAndDatasetIdsAndNotGIdsCentral.iterator();
		while (iteratorCentral.hasNext()){
			Integer next = iteratorCentral.next();
			if (false == listOfGIDs.contains(next)){
				listOfGIDs.add(next);
			}
		}
	}
	private void retrieveNIDsUsingTheParentGIDsList( ArrayList parentsList) throws MiddlewareQueryException {
		listOfNIDsForAllelicMappingType = new ArrayList<Integer>();

		

		//List<Integer> getNIDsByDatasetIds(List<Integer> datasetIds, List<Integer> gids, int start, int numOfRows) 
		System.out.println("listOfParentAGIDs:"+listOfParentAGIDs);
		List<AccMetadataSet> res=genoManager.getGdmsAccMetadatasetByGid(parentsList, 0, (int)genoManager.countGdmsAccMetadatasetByGid(parentsList));
		System.out.println("res:"+res);
		for(AccMetadataSet resList:res){
			if (false == listOfNIDsForAllelicMappingType.contains(resList.getNameId())){
				listOfNIDsForAllelicMappingType.add(resList.getNameId());
			}
		}
		
	}
	
	
	/*private void retrieveNIDsUsingTheParentGIDsList() throws MiddlewareQueryException {
		listOfNIDsForAllelicMappingType = new ArrayList<Integer>();

		AccMetadataSetDAO accMetadataSetDAOLocal = new AccMetadataSetDAO();
		accMetadataSetDAOLocal.setSession(localSession);
		AccMetadataSetDAO accMetadataSetDAOCentral = new AccMetadataSetDAO();
		accMetadataSetDAOCentral.setSession(centralSession);

		//List<Integer> getNIDsByDatasetIds(List<Integer> datasetIds, List<Integer> gids, int start, int numOfRows) 

		long countAccMetadataSetByParentAGids1 = accMetadataSetDAOLocal.countAccMetadataSetsByGids(listOfParentAGIDs);
		List<Integer> listOfNIDsByParentAGIDLocal = accMetadataSetDAOLocal.getNIDsByDatasetIds(listOfDatasetIDs, listOfParentAGIDs, 0, (int)countAccMetadataSetByParentAGids1);
		for (Integer iNid : listOfNIDsByParentAGIDLocal){
			if (false == listOfNIDsForAllelicMappingType.contains(iNid)){
				listOfNIDsForAllelicMappingType.add(iNid);
			}
		}

		long countAccMetadataSetByParentAGids2 = accMetadataSetDAOCentral.countAccMetadataSetsByGids(listOfParentAGIDs);
		List<Integer> listOfNIDsByParentAGIDCentral = accMetadataSetDAOCentral.getNIDsByDatasetIds(listOfDatasetIDs, listOfParentAGIDs, 0, (int)countAccMetadataSetByParentAGids2);
		for (Integer iNid : listOfNIDsByParentAGIDCentral){
			if (false == listOfNIDsForAllelicMappingType.contains(iNid)){
				listOfNIDsForAllelicMappingType.add(iNid);
			}
		}

		long countAccMetadataSetByParentBGids1 = accMetadataSetDAOLocal.countAccMetadataSetsByGids(listOfParentBGIDs);
		List<Integer> listOfNIDsByParentBGIDsLocal = accMetadataSetDAOLocal.getNIDsByDatasetIds(listOfDatasetIDs, listOfParentBGIDs, 0, (int)countAccMetadataSetByParentBGids1);
		for (Integer iNid : listOfNIDsByParentBGIDsLocal){
			if (false == listOfNIDsForAllelicMappingType.contains(iNid)){
				listOfNIDsForAllelicMappingType.add(iNid);
			}
		}

		long countAccMetadataSetByParentBGids2 = accMetadataSetDAOLocal.countAccMetadataSetsByGids(listOfParentBGIDs);
		List<Integer> listOfNIDsByParentBGIDsCentral = accMetadataSetDAOLocal.getNIDsByDatasetIds(listOfDatasetIDs, listOfParentBGIDs, 0, (int)countAccMetadataSetByParentBGids2);
		for (Integer iNid : listOfNIDsByParentBGIDsCentral){
			if (false == listOfNIDsForAllelicMappingType.contains(iNid)){
				listOfNIDsForAllelicMappingType.add(iNid);
			}
		}
	}*/

	private void retrieveListOfMarkerIdsForGivenDatasetID() throws NumberFormatException, MiddlewareQueryException {

		listOfMarkerIdsForGivenDatasetID = new ArrayList<Integer>();

		MarkerMetadataSetDAO markerMetadatsSetDAOLocal = new MarkerMetadataSetDAO();
		markerMetadatsSetDAOLocal.setSession(localSession);
		MarkerMetadataSetDAO markerMetadataSetDAOCentral = new MarkerMetadataSetDAO();
		markerMetadataSetDAOCentral.setSession(centralSession);

		List<Integer> markerIdsByDatasetIdLocal = markerMetadatsSetDAOLocal.getMarkerIdByDatasetId(Integer.parseInt(strDatasetID));
		List<Integer> markerIdsByDatasetIdCentral = markerMetadataSetDAOCentral.getMarkerIdByDatasetId(Integer.parseInt(strDatasetID));

		for (Integer iMID : markerIdsByDatasetIdLocal){
			if (false == listOfMarkerIdsForGivenDatasetID.contains(iMID)){
				listOfMarkerIdsForGivenDatasetID.add(iMID);
			}
		}

		for (Integer iMID : markerIdsByDatasetIdCentral){
			if (false == listOfMarkerIdsForGivenDatasetID.contains(iMID)){
				listOfMarkerIdsForGivenDatasetID.add(iMID);
			}
		}
	}


	private void retrieveMarkerTypeByMarkerID() throws MiddlewareQueryException, GDMSException {
		listOfMarkerTypeByMarkerID = new ArrayList<String>();

		MarkerDAO markerDAOLocal = new MarkerDAO();
		markerDAOLocal.setSession(localSession);
		MarkerDAO markerDAOCentral = new MarkerDAO();
		markerDAOCentral.setSession(centralSession);

		List<String> listOfMarkerTypeByMarkerIdsLocal = markerDAOLocal.getMarkerTypeByMarkerIds(listOfMarkerIdsForGivenDatasetID);
		List<String> listOfMarkerTypeByMarkerIdsCentral = markerDAOCentral.getMarkerTypeByMarkerIds(listOfMarkerIdsForGivenDatasetID);

		for (String markerType : listOfMarkerTypeByMarkerIdsLocal){
			if (false == listOfMarkerTypeByMarkerID.contains(markerType)){
				listOfMarkerTypeByMarkerID.add(markerType);
			}
		}
		for (String markerType : listOfMarkerTypeByMarkerIdsCentral){
			if (false == listOfMarkerTypeByMarkerID.contains(markerType)){
				listOfMarkerTypeByMarkerID.add(markerType);
			}
		}
		
		if (0 == listOfMarkerTypeByMarkerID.size()){
			throw new GDMSException("Marker Type could not be obtained");
		}
		
		strMarkerType = listOfMarkerTypeByMarkerID.get(0);
	}

	private void retrieveParentAandParentBGIDs() throws NumberFormatException, MiddlewareQueryException, GDMSException {
		listOfParentsByDatasetId = new ArrayList<ParentElement>();

		MappingPopDAO mappingPopDAOLocal = new MappingPopDAO();
		mappingPopDAOLocal.setSession(localSession);
		MappingPopDAO mappingPopDAOCentral = new MappingPopDAO();
		mappingPopDAOCentral.setSession(centralSession);

		List<ParentElement> parentsByDatasetIdLocal = mappingPopDAOLocal.getParentsByDatasetId(Integer.parseInt(strDatasetID));
		List<ParentElement> parentsByDatasetIdCentral = mappingPopDAOCentral.getParentsByDatasetId(Integer.parseInt(strDatasetID));
		
		List<ParentElement> results = genoManager.getParentsByDatasetId(Integer.parseInt(strDatasetID));
		for (ParentElement parentElement : results){
			////System.out.println(parentElement.getParentANId()+"   "+parentElement.getParentBGId());
			parentANid=parentElement.getParentANId();			
			parentBNid=parentElement.getParentBGId();
		}
		

		for (ParentElement parentElement : parentsByDatasetIdLocal){
			if (false == listOfParentsByDatasetId.contains(parentElement)){
				listOfParentsByDatasetId.add(parentElement);
			}
		}
		for (ParentElement parentElement : parentsByDatasetIdCentral){
			if (false == listOfParentsByDatasetId.contains(parentElement)){
				listOfParentsByDatasetId.add(parentElement);
			}
		}

		/*listOfParentAGIDs = new ArrayList<Integer>();
		listOfParentBGIDs = new ArrayList<Integer>();
		listOfAllParentGIDs = new ArrayList<Integer>(); 

		for (ParentElement parentElement : listOfParentsByDatasetId){
			listOfParentAGIDs.add(parentElement.getParentANId());
			listOfParentBGIDs.add(parentElement.getParentBGId());
		}

		for (Integer parentAGID : listOfParentAGIDs){
			if (false == listOfAllParentGIDs.contains(parentAGID)){
				listOfAllParentGIDs.add(parentAGID);
			}
		}

		for (Integer parentBGID : listOfParentBGIDs){
			if (false == listOfAllParentGIDs.contains(parentBGID)){
				listOfAllParentGIDs.add(parentBGID);
			}
		}*/
		
		if (0 == listOfParentsByDatasetId.size()){
			throw new GDMSException("Mapping Type could not be obtained");
		}
		strMappingType = listOfParentsByDatasetId.get(0).getMappingType();
	}

	public boolean isFlapjackDataBuiltSuccessfully() {
		return bFlapjackDataBuiltSuccessfully;
	}

	public void setListOfGermplasmsProvided(ArrayList<String> theListOfGermplasmNamesSelected) {
		listOfGermplasmNamesSelectedForGermplasmRetrieval = theListOfGermplasmNamesSelected;
	}

	public void setListOfMarkersSelected(ArrayList<String> theListOfMarkersSelected) {
		listOfMarkersForGivenGermplasmRetrieval = theListOfMarkersSelected;
	}

	public void setListOfGIDsSelected(ArrayList<Integer> theListOfGIDsSelected) {
		listOfGIDsProvidedForGermplasmRetrieval = theListOfGIDsSelected;
		listOfGIDs = theListOfGIDsSelected;
	}

	public void setListOfMIDsSelected(ArrayList<Integer> listOfAllMIDsSelected) {
		listOfMIDsForGivenGermplasmRetrieval = listOfAllMIDsSelected;
	}

	public void setHashmapOfSelectedMIDsAndMNames(HashMap<Integer, String> hmOfSelectedMIDandMNames) {
		hmOfMIDandMNames = hmOfSelectedMIDandMNames;
	}

	public void setHashmapOfSelectedGIDsAndGNames(
			HashMap<Object, String> hmOfSelectedGIDsAndGNames) {
		hmOfGIdsAndNval = hmOfSelectedGIDsAndGNames;
	}

}
