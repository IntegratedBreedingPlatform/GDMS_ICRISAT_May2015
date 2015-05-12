package org.icrisat.gdms.retrieve.marker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.dao.gdms.AccMetadataSetDAO;
import org.generationcp.middleware.dao.gdms.MappingPopDAO;
import org.generationcp.middleware.dao.gdms.MarkerMetadataSetDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GetGermplasmByNameModes;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.GermplasmNameDetails;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.MappingPop;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.generationcp.middleware.pojos.gdms.ParentElement;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.GDMSModel;
//import org.generationcp.middleware.pojos.GidNidElement;

public class RetrievePolymorphicMarker {
	private Session centralSession = null;
	private Session localSession = null;
	private String strSelectedPolymorphicType;
	ManagerFactory factory=null;
	GenotypicDataManager genoManager;
	GermplasmDataManager manager;
	List<String> genotypeList=new ArrayList<String>();
	List<Integer> listofGids = new ArrayList<Integer>();
	List<Integer> nidsFromAccMetadatasetByDatasetIds =new ArrayList<Integer>();
	List<Integer> nidByMarkerIdsAndDatasetIdsAndNotGIds;
	public RetrievePolymorphicMarker() {
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			genoManager=factory.getGenotypicDataManager();
			manager=factory.getGermplasmDataManager();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
		
		/*localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		genoManager=factory.getGenotypicDataManager();
		manager=factory.getGermplasmDataManager();*/
	}

	public List<Name> getNames(String theSelectedGName, String strSelectedPolymorphicType) throws GDMSException {
		try {
			/*String strName = name;//.getNval();
			List<String> strNames = new ArrayList<String>();
			strNames.add(strName);*/
			//String str="POKKALI(sample)(2923)";
			genotypeList = new ArrayList<String>();
			listofGids = new ArrayList<Integer>();
			//System.out.println("........:"+str.substring(0, str.lastIndexOf("(")));
			if(theSelectedGName.contains("(")){
				genotypeList.add(theSelectedGName.substring(0, theSelectedGName.lastIndexOf("(")));
			}else{
				genotypeList.add(theSelectedGName);
			}
			String gidsList1="";
			List<Integer> listOfDatasetIds = new ArrayList<Integer>();
			List<Integer> listOfMatchingDatasetIds = new ArrayList<Integer>();
			Integer gid = null;
			Integer nid = null;
			//genotypeList.add(theSelectedGName);
			System.out.println("theSelectedGName:"+theSelectedGName);
			List<GermplasmNameDetails> results=manager.getGermplasmNameDetailsByGermplasmNames(genotypeList,GetGermplasmByNameModes.NORMAL);
			for(GermplasmNameDetails germNames:results){
				gidsList1=gidsList1+germNames.getGermplasmId()+",";
				listofGids.add(germNames.getGermplasmId());
			}
			
			gidsList1=gidsList1.substring(0, gidsList1.length()-1);
			System.out.println("^^^^^^^^^^^^^^^^   :"+listofGids);
			
			String strNidsQuerry="SELECT dataset_id FROM gdms_acc_metadataset WHERE gid IN("+gidsList1+")";
			System.out.println("strNidsQuerry:"+strNidsQuerry);
			List datasetIdsFromLocal=new ArrayList();	
			List datasetIdsFromCentral=new ArrayList();	
			//List markersList=new ArrayList();		
			List datasetIDSList=new ArrayList();
			//List gidsList=new ArrayList();
			Object objNL=null;
			Object objNC=null;
			Iterator itListNC=null;
			Iterator itListNL=null;						
			//try{
				
				try{
					localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
					centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
				}catch (Exception e){
					e.printStackTrace();
				}
				
				SQLQuery queryNidsC=centralSession.createSQLQuery(strNidsQuerry);		
				queryNidsC.addScalar("dataset_id",Hibernate.INTEGER);	  
				
				datasetIdsFromCentral=queryNidsC.list();
				itListNC=datasetIdsFromCentral.iterator();			
				while(itListNC.hasNext()){
					objNC=itListNC.next();
					if(objNC!=null){
						if(! listOfDatasetIds.contains(Integer.parseInt(objNC.toString()))){
							listOfDatasetIds.add(Integer.parseInt(objNC.toString()));										
						}
					}
				}
				
				SQLQuery queryNidsL=localSession.createSQLQuery(strNidsQuerry);		
				queryNidsL.addScalar("dataset_id",Hibernate.INTEGER);	  
				
				datasetIdsFromLocal=queryNidsL.list();
				itListNL=datasetIdsFromLocal.iterator();			
				while(itListNL.hasNext()){
					objNL=itListNL.next();
					if(objNL!=null){
						if(! listOfDatasetIds.contains(Integer.parseInt(objNL.toString()))){
							listOfDatasetIds.add(Integer.parseInt(objNL.toString()));	
							//markers=markers+objL.toString()+",";
						}
					}
				}
				
			
			
			/*List<AccMetadataSetPK> accMetadataSets = genoManager.getGdmsAccMetadatasetByGid(listofGids, 0, 
	                (int) genoManager.countGdmsAccMetadatasetByGid(listofGids));
	        System.out.println("testGetGdmsAccMetadatasetByGid() RESULTS: ");
	        for (AccMetadataSetPK accMetadataSet : accMetadataSets) {
	            //System.out.println("@@@@@@@@@@@@@@@@@@@@  :"+accMetadataSet.toString());
	            Integer datasetId = accMetadataSet.getDatasetId();
	            gid = accMetadataSet.getGermplasmId();
				nid = accMetadataSet.getNameId();
				if(false == listOfDatasetIds.contains(datasetId)) {
					listOfDatasetIds.add(datasetId);
				}
	            
	            
	        }*/
			
	        System.out.println("listOfDatasetIds:"+listOfDatasetIds);
	        
	        List<Integer> midsList= new ArrayList();
	        for(int d=0;d<listOfDatasetIds.size();d++){
	        	midsList=genoManager.getMarkerIdsByDatasetId(listOfDatasetIds.get(d));
	        }
	        System.out.println("midsList=:"+midsList);
	        
	        System.out.println(genoManager.getAllFromMarkerMetadatasetByMarkers(midsList));
	        List<Integer> listDataSetIds=new ArrayList();
	        List<MarkerMetadataSet> result = genoManager.getAllFromMarkerMetadatasetByMarkers(midsList);
	        if (result != null) {
                for (MarkerMetadataSet elem : result) {
                    //Debug.println(4, elem.toString());
                	if(!listDataSetIds.contains(elem.getDatasetId()))
                		listDataSetIds.add(elem.getDatasetId());
                }
            }
	        
	        
	       /* if(listOfMatchingDatasetIds.size() > listOfDatasetIds.size()){
	        	
	        }*/
	       
	       /* for(int d=0;d<listOfMatchingDatasetIds.size();d++){
	        	if(!listOfDatasetIds.contains(listOfMatchingDatasetIds.get(d))){
	        		listDataSetIds.add(listOfMatchingDatasetIds.get(d));
	        	}
	        }*/
	        
	        //genoManager.getdata
	        /*for(int l=0;l<listDataSetIds.size();l++){
	        	System.out.println("#####:"+ genoManager.getDatasetById(listDataSetIds.get(l)));
	        }*/
	       // System.out.println("$$$$$$$$$$$$$$$$ :"+genoManager.getDatasetDetailsByDatasetIds(listDataSetIds));
	        List dType=new ArrayList();
	        List <Dataset> resDatasetDetails=genoManager.getDatasetDetailsByDatasetIds(listDataSetIds);
	        System.out.println("..:"+resDatasetDetails);
	        for(Dataset resD:resDatasetDetails){
	        	if(resD.getDatasetType().toString().equalsIgnoreCase("mapping")){
	        		dType.add(resD.getDatasetId());
	        	}
	        }
	        List parents=new ArrayList();
	        System.out.println("dType");
	        nidByMarkerIdsAndDatasetIdsAndNotGIds=new ArrayList();
	        for(int d=0;d<dType.size();d++){
	        	MappingPop resMappingPop= genoManager.getMappingPopByDatasetId(Integer.parseInt(dType.get(d).toString()));
	        	if(resMappingPop.getMappingType().equalsIgnoreCase("allelic")){
	        		parents.add(resMappingPop.getParentANId());
	        		parents.add(resMappingPop.getParentBNId());
	        		nidByMarkerIdsAndDatasetIdsAndNotGIds.add(resMappingPop.getParentANId());
	        		nidByMarkerIdsAndDatasetIdsAndNotGIds.add(resMappingPop.getParentBNId());
	        	}
	        	
	        
	        //System.out.println("!!!!!!!!!!!!!!!!  :"+genoManager.getMappingPopByDatasetId(Integer.parseInt(dType.get(d).toString())));
	        }
	        //genoManager.getAllFromMarkerMetadatasetByMarker(arg0)
			//System.out.println(nid+"  listOfMatchingDatasetIds="+listOfMatchingDatasetIds);
			//if()
			nidByMarkerIdsAndDatasetIdsAndNotGIds = getNidByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIds, midsList, listofGids);
			
			//System.out.println("nidByMarkerIdsAndDatasetIdsAndNotGIds^^^^^^^^^^^^^^^  :"+nidByMarkerIdsAndDatasetIdsAndNotGIds.size()+" "+nidByMarkerIdsAndDatasetIdsAndNotGIds);
			List<Name> namesByNIds = getNamesByNIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
			//System.out.println("%%%%%%%%%%%%%  :"+namesByNIds);
			return namesByNIds;
			
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		//return new ArrayList<Name>();
	}

	private List<Name> getNamesByNIds(
			List<Integer> nidByMarkerIdsAndDatasetIdsAndNotGIds)
			throws MiddlewareQueryException {
		
		List<Name> listOfNames = new ArrayList<Name>();
		List<Name> localNamesByNIds = getLocalNamesByNIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		for (Name name : localNamesByNIds) {
			listOfNames.add(name);
		}
		List<Name> centralNamesByNIds = getCentralNamesByNIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		for (Name name : centralNamesByNIds) {
			listOfNames.add(name);
		}
		return listOfNames;
	}

	private List<Name> getCentralNamesByNIds(
			List<Integer> nidByMarkerIdsAndDatasetIdsAndNotGIds) throws MiddlewareQueryException {
		/*NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		return namesByNameIds;*/
		try{
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(centralSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		
		return namesByNameIds;
	}

	private List<Name> getLocalNamesByNIds(
			List<Integer> nidByMarkerIdsAndDatasetIdsAndNotGIds)
			throws MiddlewareQueryException {
		/*for (Integer integer : nidByMarkerIdsAndDatasetIdsAndNotGIds) {
			NameDAO nameDAO = new NameDAO();
			nameDAO.setSession(localSession);
			Name nameByNameId = nameDAO.getNameByNameId(integer);
			System.out.println(nameByNameId);
		}
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		return namesByNameIds;*/
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nidByMarkerIdsAndDatasetIdsAndNotGIds);
		return namesByNameIds;
	}

	private List<Integer> getNidByMarkerIdsAndDatasetIdsAndNotGIds(List<Integer> listOfDatasetIds, List<Integer> markerMetadataSet, List<Integer> listofGids) throws MiddlewareQueryException {
		List<Integer> listOfNids = new ArrayList<Integer>();
		/*Set<Integer> localNidByMarkerIdsandDatasetIdsAndNotGIds = getLocalNidByMarkerIdsandDatasetIdsAndNotGIds(listOfDatasetIds, markerMetadataSet, listofGids);
		for (Integer integer : localNidByMarkerIdsandDatasetIdsAndNotGIds) {
			listOfNids.add(integer);
		}
		Set<Integer> centralNidByMarkerIdsandDatasetIdsAndNotGIds = getCentralNidByMarkerIdsandDatasetIdsAndNotGIds(listOfDatasetIds, markerMetadataSet, listofGids);
		for (Integer integer : centralNidByMarkerIdsandDatasetIdsAndNotGIds) {
			listOfNids.add(integer);
		}*/
		
		List<Integer> centralNidByMarkerIdsandDatasetIdsAndNotGIds = getCentralNidByMarkerIdsandDatasetIdsAndNotGIds(listOfDatasetIds, markerMetadataSet, listofGids);
		for (Integer integer : centralNidByMarkerIdsandDatasetIdsAndNotGIds) {
			listOfNids.add(integer);
		}
		
		return listOfNids;
		
	}

	private List<Integer> getCentralNidByMarkerIdsandDatasetIdsAndNotGIds(
			List<Integer> listOfDatasetIds, List<Integer> markerMetadataSet,
			List<Integer> listofGids) throws MiddlewareQueryException {
		/*AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(centralSession);*/
		//Set<Integer> nIdsByMarkerIdsAndDatasetIdsAndNotGIds = accMetadataSetDAO.getNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIds, markerMetadataSet, listofGids);
		//getNIdsByMarkerIdsAndDatasetIds(datasetIdList, markerIDList, 0, manager1.countNIdsByMarkerIdsAndDatasetIds(datasetIdList, markerIDList))
		/*try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
		} catch (Exception e){
			e.printStackTrace();
			
		}*/
		//GenotypicDataManagerImpl genotypicDataManagerImpl = new GenotypicDataManagerImpl(localSession, centralSession);
		int countNIdsByMarkerIdsAndDatasetIds = genoManager.countNIdsByMarkerIdsAndDatasetIds(listOfDatasetIds, markerMetadataSet);
		List<Integer> nIdsByMarkerIdsAndDatasetIds2 = genoManager.getNIdsByMarkerIdsAndDatasetIds(listOfDatasetIds, markerMetadataSet, 0, countNIdsByMarkerIdsAndDatasetIds);
		//System.out.println("nIdsByMarkerIdsAndDatasetIds2=:"+nIdsByMarkerIdsAndDatasetIds2);
		return nIdsByMarkerIdsAndDatasetIds2;
		//return nIdsByMarkerIdsAndDatasetIdsAndNotGIds;
	}

	/*private Set<Integer> getLocalNidByMarkerIdsandDatasetIdsAndNotGIds(
			List<Integer> listOfDatasetIds, List<Integer> markerMetadataSet,
			List<Integer> listofGids) throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(localSession);
		Set<Integer> nIdsByMarkerIdsAndDatasetIdsAndNotGIds = accMetadataSetDAO.getNIdsByMarkerIdsAndDatasetIdsAndNotGIds(listOfDatasetIds, markerMetadataSet, listofGids);
		return nIdsByMarkerIdsAndDatasetIdsAndNotGIds;
		Set<Integer> nIdsByMarkerIdsAndDatasetIds = accMetadataSetDAO.getNIdsByMarkerIdsAndDatasetIds(markerMetadataSet, listOfDatasetIds);
		return nIdsByMarkerIdsAndDatasetIds;
	}*/

	private List<Integer> getMarkerMetadataSet(List<Integer> listOfDatasetIds,
			Integer gid) throws MiddlewareQueryException {
		List<Integer> listOfMarkerMetadataSet = new ArrayList<Integer>();
		List<Integer> localMarkerMetadataSet = getLocalMarkerMetadataSet(listOfDatasetIds, gid);
		for (Integer integer : localMarkerMetadataSet) {
			listOfMarkerMetadataSet.add(integer);
		}
		
		List<Integer> centralMarkerMetadataSet = getCentralMarkerMetadataSet(listOfDatasetIds, gid);
		for (Integer integer : centralMarkerMetadataSet) {
			listOfMarkerMetadataSet.add(integer);
		}
		return listOfMarkerMetadataSet;
	}

	private List<Integer> getCentralMarkerMetadataSet(
			List<Integer> listOfDatasetIds, Integer gid) throws MiddlewareQueryException {
		try{
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		
		MarkerMetadataSetDAO markerMetadataSetDAO = new MarkerMetadataSetDAO();
		markerMetadataSetDAO.setSession(centralSession);
		List<Integer> markersByGidAndDatasetIds = markerMetadataSetDAO.getMarkersByGidAndDatasetIds(gid, listOfDatasetIds, 0, (int) markerMetadataSetDAO.countAll());
		return markersByGidAndDatasetIds;
	}

	private List<Integer> getLocalMarkerMetadataSet(List<Integer> listOfDatasetIds,
			Integer gid) throws MiddlewareQueryException {
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		MarkerMetadataSetDAO markerMetadataSetDAO = new MarkerMetadataSetDAO();
		markerMetadataSetDAO.setSession(localSession);
		List<Integer> markersByGidAndDatasetIds = markerMetadataSetDAO.getMarkersByGidAndDatasetIds(gid, listOfDatasetIds, 0, (int) markerMetadataSetDAO.countAll());
		return markersByGidAndDatasetIds;
	}

	/*private List<AccMetadataSetPK> getAccMetaDataSetByGids(List<Integer> listofGids)
			throws MiddlewareQueryException {
		List<AccMetadataSetPK> listOfAccMetadataSetDAO = new ArrayList<AccMetadataSetPK>();
		List<AccMetadataSetPK> localAccMetaDataSetByGids = getLocalAccMetaDataSetByGids(listofGids);
		for (AccMetadataSetPK accMetadataSetPK : localAccMetaDataSetByGids) {
			listOfAccMetadataSetDAO.add(accMetadataSetPK);
		}
		List<AccMetadataSetPK> centralAccMetaDataSetByGids = getCentralAccMetaDataSetByGids(listofGids);
		for (AccMetadataSetPK accMetadataSetPK : centralAccMetaDataSetByGids) {
			listOfAccMetadataSetDAO.add(accMetadataSetPK);
		}
		return listOfAccMetadataSetDAO;
		
	}

	private List<AccMetadataSetPK> getCentralAccMetaDataSetByGids(List<Integer> listofGids) throws MiddlewareQueryException {
		try{
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(centralSession);
		List<AccMetadataSetPK> accMetadataSetByGids = accMetadataSetDAO.getAccMetadataSetByGids(listofGids, 0, (int)accMetadataSetDAO.countAll());
		return accMetadataSetByGids;
	}

	private List<AccMetadataSetPK> getLocalAccMetaDataSetByGids(List<Integer> listofGids)
			throws MiddlewareQueryException {
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(localSession);
		List<AccMetadataSetPK> accMetadataSetByGids = accMetadataSetDAO.getAccMetadataSetByGids(listofGids, 0, (int)accMetadataSetDAO.countAll());
		return accMetadataSetByGids;
	}*/

	private List<Integer> getGIDAndNidByGermplasmNames(List<String> strNames)
			throws MiddlewareQueryException {
		List<Integer> listofGids = new ArrayList<Integer>();
		
		List<GermplasmNameDetails> results=manager.getGermplasmNameDetailsByGermplasmNames(strNames,GetGermplasmByNameModes.NORMAL);
		for(GermplasmNameDetails germNames:results){
			listofGids.add(germNames.getGermplasmId());
		}
		
		
		/*List<GidNidElement> localGidAndNidByGermplasmNames = getLocalGidAndNidByGermplasmNames(strNames);
		for (GidNidElement gidNidElement : localGidAndNidByGermplasmNames) {
			Integer germplasmId = gidNidElement.getGermplasmId();
			listofGids.add(germplasmId);
		}
		
		List<GidNidElement> centralGidAndNidByGermplasmNames = getCentralGidAndNidByGermplasmNames(strNames);
		for (GidNidElement gidNidElement : centralGidAndNidByGermplasmNames) {
			Integer germplasmId = gidNidElement.getGermplasmId();
			listofGids.add(germplasmId);
		}*/

		return listofGids;
	}

	/*private List<GidNidElement> getCentralGidAndNidByGermplasmNames(
			List<String> strNames) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		try{
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		nameDAO.setSession(centralSession);
		List<GidNidElement> gidAndNidByGermplasmNames = nameDAO.getGidAndNidByGermplasmNames(strNames);
		return gidAndNidByGermplasmNames;
	}

	private List<GidNidElement> getLocalGidAndNidByGermplasmNames(
			List<String> strNames) throws MiddlewareQueryException {
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		List<GidNidElement> gidAndNidByGermplasmNames = nameDAO.getGidAndNidByGermplasmNames(strNames);
		return gidAndNidByGermplasmNames;
	}*/

	public List<Name> getNamesForRetrievePolymorphic() throws GDMSException {
		List<Integer> listOfParentsFromMappingPopulation = new ArrayList<Integer>();
		List<Integer> listofDatasetId = new ArrayList<Integer>();
		List<Integer> niDsByDatasetIds =null;
		List<Name> namesByNameIds =null;
		try {
			//genoManager.getDatasetIdsForFingerPrinting(arg0, arg1)
			listofDatasetId = getDatasetIDs();
			System.out.println("dataset IDS:"+listofDatasetId);
			listOfParentsFromMappingPopulation = getMappingPop();
			System.out.println(".......parents:"+listOfParentsFromMappingPopulation);
			
			/** Commented by Kalyani on 23 OCT 2013 while testing the functionality   **/
			//List<Integer> niDsByDatasetIds = getAccMetadataSetByParentMappingPopulationAndDatasetId(listOfParentsFromMappingPopulation, listofDatasetId);
			if(! listofDatasetId.isEmpty()){
				niDsByDatasetIds = getAccMetaDatasetFromBothCentralAndLocal(listofDatasetId);
			
				namesByNameIds = getNamesByNID(niDsByDatasetIds);
			}
			return namesByNameIds;
		} catch (Throwable e) {
			//e.printStackTrace();
			throw new GDMSException(e.getMessage());
		}
		//return null;
	}

	private List<Name> getNamesByNID(List<Integer> niDsByDatasetIds)
			throws MiddlewareQueryException {
		List<Name> namesByNameIds = new ArrayList<Name>();
		List<Name> localNamesByNameIds = getLocalName(niDsByDatasetIds);
		for (Name name : localNamesByNameIds) {
			namesByNameIds.add(name);
		}
		List<Name> centralNamesByNameIds = getCentralName(niDsByDatasetIds);
		for (Name name : centralNamesByNameIds) {
			namesByNameIds.add(name);
		}
		return namesByNameIds;
	}

	private List<Name> getCentralName(List<Integer> niDsByDatasetIds) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		try{
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		nameDAO.setSession(centralSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(niDsByDatasetIds);
		return namesByNameIds;
	}

	private List<Name> getLocalName(List<Integer> niDsByDatasetIds) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		nameDAO.setSession(localSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(niDsByDatasetIds);
		return namesByNameIds;
	}

	private List<Integer> getAccMetadataSetByParentMappingPopulationAndDatasetId(List<Integer> listOfParentsFromMappingPopulation, List<Integer> listofDatasetId) throws MiddlewareQueryException {
		List<Integer> niDsByDatasetIds = new ArrayList<Integer>();
		/*List<Integer> localNiDsByDatasetIds = getLocalAccMetadataSet(listOfParentsFromMappingPopulation, listofDatasetId);
		for (Integer integer : localNiDsByDatasetIds) {
			if(false == niDsByDatasetIds.contains(integer)) {
				niDsByDatasetIds.add(integer);
			}
		}
		List<Integer> centralNiDsByDatasetIds = getCentralAccMetadataSet(listOfParentsFromMappingPopulation, listofDatasetId);
		for (Integer integer : centralNiDsByDatasetIds) {
			if(false == niDsByDatasetIds.contains(integer)) {
				niDsByDatasetIds.add(integer);
			}
		}*/
		
		
		niDsByDatasetIds = getAccMetaDatasetFromBothCentralAndLocal(listofDatasetId);
		
		return niDsByDatasetIds;
	}

	private List<Integer> getAccMetaDatasetFromBothCentralAndLocal(
			List<Integer> listofDatasetId) throws MiddlewareQueryException {
		String datasetID="";
		 nidsFromAccMetadatasetByDatasetIds =new ArrayList<Integer>();
		try{			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();			
		}catch (Exception e){
			e.printStackTrace();
		}
		for(int d=0;d<listofDatasetId.size();d++){
			datasetID=datasetID+listofDatasetId.get(d)+",";
			
		}
		String strQuery="select nid from gdms_acc_metadataset where dataset_id in("+datasetID.substring(0, datasetID.length()-1)+")";
		
		List snpsFromLocal=new ArrayList();		
		List markersList=new ArrayList();		
		List snpsFromCentral=new ArrayList();
	
		Object obj=null;
		Object objL=null;
		Iterator itListC=null;
		Iterator itListL=null;		
			
		SQLQuery queryC=centralSession.createSQLQuery(strQuery);		
		queryC.addScalar("nid",Hibernate.INTEGER);	
		snpsFromCentral=queryC.list();			
		itListC=snpsFromCentral.iterator();			
		while(itListC.hasNext()){
			obj=itListC.next();
			if(obj!=null){		
				if(! nidsFromAccMetadatasetByDatasetIds.contains(Integer.parseInt(obj.toString())))
					nidsFromAccMetadatasetByDatasetIds.add(Integer.parseInt(obj.toString()));
			}
		}	
		SQLQuery queryL=localSession.createSQLQuery(strQuery);		
		queryL.addScalar("nid",Hibernate.INTEGER);		  
		
		snpsFromLocal=queryL.list();
		itListL=snpsFromLocal.iterator();			
		while(itListL.hasNext()){
			objL=itListL.next();
			if(objL!=null){
				if(! nidsFromAccMetadatasetByDatasetIds.contains(Integer.parseInt(objL.toString())))
					nidsFromAccMetadatasetByDatasetIds.add(Integer.parseInt(objL.toString()));
			}
		}	
		long countDatasetIdsForMapping = 0l;
		//System.out.println("..............  strSelectedPolymorphicType=:"+strSelectedPolymorphicType+"    "+listofDatasetId);
		if (strSelectedPolymorphicType.equalsIgnoreCase("Mapping")) {
			countDatasetIdsForMapping = genoManager.countDatasetIdsForMapping();
		} else {
			countDatasetIdsForMapping = genoManager.countDatasetIdsForFingerPrinting();
			//System.out.println("count=:"+countDatasetIdsForMapping);
		}
		//System.out.println("listofDatasetId:"+listofDatasetId);
		//List<Integer> nidsFromAccMetadatasetByDatasetIds = genoManager.getNidsFromAccMetadatasetByDatasetIds(listofDatasetId, 0, (int)genoManager.countNidsFromAccMetadatasetByDatasetIds(listofDatasetId));
		//genotypicDataManagerImpl.countn
		//List<Integer> nidsFromAccMetadatasetByDatasetIds = genotypicDataManagerImpl.getNidsFromAccMetadatasetByDatasetIds(listofDatasetId, 0, (int)genotypicDataManagerImpl.countNidsFromAccMetadatasetByDatasetIds(listofDatasetId));
		//System.out.println("^^^^^^^^^^^^^^^^^^^^^:"+nidsFromAccMetadatasetByDatasetIds);
		return nidsFromAccMetadatasetByDatasetIds;
		
	}

	/*private List<Integer> getLocalAccMetadataSet(List<Integer> listOfParentsFromMappingPopulation, List<Integer> listofDatasetId) throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(localSession);
		List<Integer> niDsByDatasetIds = accMetadataSetDAO.getNIDsByDatasetIds(listofDatasetId, listOfParentsFromMappingPopulation, 0, (int)accMetadataSetDAO.countAll());
		return niDsByDatasetIds;
		
	}
	
	private List<Integer> getCentralAccMetadataSet(List<Integer> listOfParentsFromMappingPopulation, List<Integer> listofDatasetId) throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(centralSession);
		List<Integer> niDsByDatasetIds = accMetadataSetDAO.getNIDsByDatasetIds(listofDatasetId, listOfParentsFromMappingPopulation, 0, (int)accMetadataSetDAO.countAll());
		return niDsByDatasetIds;
	}*/


	private List<Integer> getMappingPop()
			throws MiddlewareQueryException {
		List<Integer> listOfParentsFromMappingPopulation = new ArrayList<Integer>();
		List<ParentElement> localAllParentsFromMappingPopulation = getLocalMappingPop();
		List<ParentElement> centralAllParentsFromMappingPopulation = getCentralMappingPop();
		
		for (ParentElement parentElement : localAllParentsFromMappingPopulation) {
			if(false == listOfParentsFromMappingPopulation.contains(parentElement.getParentANId())) {
				listOfParentsFromMappingPopulation.add(parentElement.getParentANId());
			}
			if(false == listOfParentsFromMappingPopulation.contains(parentElement.getParentBGId())) {
				listOfParentsFromMappingPopulation.add(parentElement.getParentBGId());
			}
		}
		
		for (ParentElement parentElement : centralAllParentsFromMappingPopulation) {
			if(false == listOfParentsFromMappingPopulation.contains(parentElement.getParentANId())) {
				listOfParentsFromMappingPopulation.add(parentElement.getParentANId());
			}
			if(false == listOfParentsFromMappingPopulation.contains(parentElement.getParentBGId())) {
				listOfParentsFromMappingPopulation.add(parentElement.getParentBGId());
			}
		}
		return listOfParentsFromMappingPopulation;
	}

	private List<ParentElement> getCentralMappingPop() throws MiddlewareQueryException {
		MappingPopDAO mappingPopDAO = new MappingPopDAO();
		try{
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		mappingPopDAO.setSession(centralSession);
		//List<ParentElement> allParentsFromMappingPopulation = mappingPopDAO.getAllParentsFromMappingPopulation(0, (int)mappingPopDAO.countAll());
		List<ParentElement> allParentsFromMappingPopulation = genoManager.getAllParentsFromMappingPopulation(0, (int)mappingPopDAO.countAll());

		return allParentsFromMappingPopulation;
	}

	private List<ParentElement> getLocalMappingPop()
			throws MiddlewareQueryException {
		MappingPopDAO mappingPopDAO = new MappingPopDAO();
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
		
		} catch (Exception e){
			e.printStackTrace();
			
		}
		mappingPopDAO.setSession(localSession);
		
		//List<ParentElement> allParentsFromMappingPopulation = mappingPopDAO.getAllParentsFromMappingPopulation(0, (int)mappingPopDAO.countAll());
		List<ParentElement> allParentsFromMappingPopulation = genoManager.getAllParentsFromMappingPopulation(0, (int)mappingPopDAO.countAll());
		
		return allParentsFromMappingPopulation;
	}

	private List<Integer> getDatasetIDs() throws MiddlewareQueryException {
		List<Integer> listofDatasetId = new ArrayList<Integer>();
		System.out.println(",,,,,,,,,,,,,,,,  strSelectedPolymorphicType"+strSelectedPolymorphicType);
		if (strSelectedPolymorphicType.equalsIgnoreCase("Mapping")) {
			listofDatasetId=genoManager.getDatasetIdsForMapping(0, (int)genoManager.countDatasetIdsForMapping());
			//datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForMapping(0, (int)datasetDAO.countAll());
		} else {
			//datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForFingerPrinting(0, (int)datasetDAO.countAll());
			listofDatasetId=genoManager.getDatasetIdsForFingerPrinting(0, (int)genoManager.countDatasetIdsForFingerPrinting());
		}
		System.out.println("listofDatasetId;"+listofDatasetId);
		/*List<Integer> localDatasetIdsForMapping = getLocalDataset();
		List<Integer> centraldatasetIdsForMapping = getCentralDataset();
		for (Integer integerDatasetid : localDatasetIdsForMapping) {
			if(false == listofDatasetId.contains(integerDatasetid)) {
				listofDatasetId.add(integerDatasetid);
			}
		}
		
		for (Integer integerDatasetid : centraldatasetIdsForMapping) {
			if(false == listofDatasetId.contains(integerDatasetid)) {
				listofDatasetId.add(integerDatasetid);
			}
		}*/
		
		return listofDatasetId;
	}

	/*private List<Integer> getLocalDataset()
			throws MiddlewareQueryException {
		DatasetDAO datasetDAO = new DatasetDAO();
		datasetDAO.setSession(localSession);
		
		//20130830: Fix for Issue No: 70
		//List<Integer> datasetIdsForMapping = datasetDAO.getDatasetIdsForMapping(0, (int)datasetDAO.countAll());
		
		List<Integer> datasetIdsForPolymorphic;
		System.out.println("strSelectedPolymorphicType=:"+strSelectedPolymorphicType);
		if (strSelectedPolymorphicType.equalsIgnoreCase("Mapping")) {
			//datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForMapping(0, (int)datasetDAO.countAll());
			datasetIdsForPolymorphic =genoManager.get
		} else {
			datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForFingerPrinting(0, (int)datasetDAO.countAll());
		}
		//20130830: Fix for Issue No: 70
		System.out.println("datasetIdsForPolymorphic    LOCAL   :"+datasetIdsForPolymorphic);
		return datasetIdsForPolymorphic;
	}
	
	private List<Integer> getCentralDataset()
			throws MiddlewareQueryException {
		DatasetDAO datasetDAO = new DatasetDAO();
		datasetDAO.setSession(centralSession);
		
		//20130830: Fix for Issue No: 70
		//List<Integer> datasetIdsForMapping = datasetDAO.getDatasetIdsForMapping(0, (int)datasetDAO.countAll());
		
		List<Integer> datasetIdsForPolymorphic;
		
		if (strSelectedPolymorphicType.equalsIgnoreCase("Mapping")) {
			datasetIdsForPolymorphic=genoManager.getDatasetIdsForMapping(0, (int)genoManager.countDatasetIdsForMapping());
			//datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForMapping(0, (int)datasetDAO.countAll());
		} else {
			//datasetIdsForPolymorphic = datasetDAO.getDatasetIdsForFingerPrinting(0, (int)datasetDAO.countAll());
			datasetIdsForPolymorphic=genoManager.getDatasetIdsForFingerPrinting(0, (int)genoManager.countDatasetIdsForFingerPrinting());
		}
		//20130830: Fix for Issue No: 70
		
		return datasetIdsForPolymorphic;
	}*/

	public void setPolymorphicType(String theSelectedPolymorphicType) {
		strSelectedPolymorphicType = theSelectedPolymorphicType;
	}

}
