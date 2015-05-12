package org.icrisat.gdms.retrieve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.dao.gdms.QtlDAO;
import org.generationcp.middleware.dao.gdms.QtlDetailsDAO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.gdms.Mta;
import org.generationcp.middleware.pojos.gdms.Qtl;
import org.generationcp.middleware.pojos.gdms.QtlDetailElement;
import org.generationcp.middleware.pojos.gdms.QtlDetails;
import org.generationcp.middleware.util.Debug;
import org.hibernate.Session;
import org.icrisat.gdms.ui.common.GDMSModel;
//import org.generationcp.middleware.dao.TraitDAO;
//import org.generationcp.middleware.pojos.Trait;

public class RetrieveQTL {

	private Session localSession;
	private Session centralSession;
	
	ManagerFactory factory=null;
	OntologyDataManager om;
	GenotypicDataManager genoManager;
	public RetrieveQTL() {
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			om=factory.getOntologyDataManager();
			genoManager=factory.getGenotypicDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
		/*localSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession();
		centralSession = GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession();
		
		factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		om=factory.getOntologyDataManager();*/
		
	}
	

	/*private List<QtlDetailElement> getCentralQTLByName(String strQTLName) throws MiddlewareQueryException {
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			om=factory.getOntologyDataManager();
			genoManager=factory.getGenotypicDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		long countQtlDetailsByName = qtlDAO.getAll().size();
		//List<QtlDetailElement> listOfQTLDetailElementsByName = qtlDAO.getQtlDetailsByName(strQTLName, 0, (int)countQtlDetailsByName);
		List<QtlDetailElement> listOfQTLDetailElementsByName =qtlDAO.getQtlAndQtlDetailsByName(strQTLName, 0, (int)countQtlDetailsByName);
		System.out.println("........listOfQTLDetailElementsByName:"+listOfQTLDetailElementsByName);
		return listOfQTLDetailElementsByName;
		
	}*/

	/*private List<QtlDetailElement> getLocalQTLByName(String strQTLName) throws MiddlewareQueryException {
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			om=factory.getOntologyDataManager();
			genoManager=factory.getGenotypicDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
		QtlDAO qtlDAOL = new QtlDAO();
		qtlDAOL.setSession(localSession);
		long countQtlDetailsByName = qtlDAOL.getAll().size();
		//List<QtlDetailElement> listOfQTLDetailElementsByName = qtlDAOL.getQtlDetailsByName(strQTLName, 0, (int)countQtlDetailsByName);
		//List<QtlDetailElement> listOfQTLDetailElementsByName =qtlDAOL.getQtlAndQtlDetailsByName(strQTLName, 0, (int)countQtlDetailsByName);
		List<QtlDetailElement> listOfQTLDetailElementsByName =genoManager.getQtlByName(strQTLName, 0, (int)genoManager.countAllQtl());
		
		System.out.println("KALYANI>>>>>>>>>>:"+genoManager.getQtlByName(strQTLName, 0, (int)genoManager.countAllQtl()));
		
		
		System.out.println("&&&&&&&&&&&&&&&&&  :"+genoManager.getQtlByName(strQTLName, 0, (int)genoManager.countAllQtl()));
		System.out.println(strQTLName+":   listOfQTLDetailElementsByName:"+listOfQTLDetailElementsByName);
		return listOfQTLDetailElementsByName;
	}*/

	public List<QtlDetailElement> retrieveQTLByName(String strQTLName) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		/*List<Integer> qtlIds=new ArrayList();
		qtlIds.add(1);
		qtlIds.add(2);*/
		//System.out.println("^|^  ................  ^|^:"+genoManager.getQtlByQtlIds(qtlIds, 0, (int)genoManager.countAllQtl()));//getQtlByName(strQTLName,  0, (int)genoManager.countAllQtl()));
		//System.out.println("^|^  ................  ^|^:"+genoManager.getQtlByName(strQTLName, 0, (int)genoManager.countAllQtl()));
		
		listToReturn=genoManager.getQtlByName(strQTLName, 0, (int)genoManager.countAllQtl());
		
		/*List<QtlDetailElement> localQTLByName = getLocalQTLByName(strQTLName);
		System.out.println("localQTLByName:"+localQTLByName);
		if(null != localQTLByName) {
			listToReturn.addAll(localQTLByName);
		}

		List<QtlDetailElement> centralQTLByName = getCentralQTLByName(strQTLName);
		if(null != centralQTLByName) {
			listToReturn.addAll(centralQTLByName);
		}
		System.out.println("listToReturn:"+listToReturn);*/
		return listToReturn;
	}
	
	public List<Integer> retrieveAllTraits() throws MiddlewareQueryException {
		/*List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		List<QtlDetailElement> localQTL = getLocalQTLDetails();*/
		List<Integer> datasetIds=new ArrayList<Integer>();
		List<Integer> traitsListToReturn = new ArrayList<Integer>();
		List<Qtl> qtls = genoManager.getAllQtl(0, (int) genoManager.countAllQtl());
        //Debug.println(0, "testGetAllQtl() RESULTS: " + qtls.size());
        for (Qtl qtl : qtls){
            //Debug.println(0, "    " + qtl.getDatasetId());
            datasetIds.add(qtl.getDatasetId());
        }
        
        //Integer datasetId = 1;		// Crop tested: Groundnut
        for(int t=0;t<datasetIds.size();t++){
        	List<Integer> results = genoManager.getQtlTraitsByDatasetId(datasetIds.get(t), 0, (int) genoManager.countQtlTraitsByDatasetId(datasetIds.get(t)));
        	for(int r=0;r<results.size();r++){
        		traitsListToReturn.add(results.get(r));
        	}
        }
        /*List<Integer> results = genoManager.getQtlTraitsByDatasetId(datasetId, 0, 
                (int) genoManager.countQtlTraitsByDatasetId(datasetId));
        Debug.println(0, "testGetQtlTraitsByDatasetId() RESULTS: ......" + results);*/
		/*
		if(null != localQTL) {
			listToReturn.addAll(localQTL);
		}
		List<QtlDetailElement> getcentrailQTL = getcentralQTLDetails();
		if(null != getcentrailQTL) {
			listToReturn.addAll(getcentrailQTL);
		}*/
		return traitsListToReturn;
	}
	
	
	
	
	
	public List<QtlDetailElement> retrieveQTLDetails() throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		List<QtlDetailElement> localQTL = getLocalQTLDetails();
		List<Qtl> qtls = genoManager.getAllQtl(0, (int) genoManager.countAllQtl());
        //Debug.println(0, "testGetAllQtl() RESULTS: " + qtls.size());
        for (Qtl qtl : qtls){
            //Debug.println(0, "    " + qtl);
        }
        
        Integer datasetId = 1;		// Crop tested: Groundnut
        List<Integer> results = genoManager.getQtlTraitsByDatasetId(datasetId, 0, 
                (int) genoManager.countQtlTraitsByDatasetId(datasetId));
        //Debug.println(0, "testGetQtlTraitsByDatasetId() RESULTS: ......" + results);
		
		if(null != localQTL) {
			listToReturn.addAll(localQTL);
		}
		List<QtlDetailElement> getcentrailQTL = getcentralQTLDetails();
		if(null != getcentrailQTL) {
			listToReturn.addAll(getcentrailQTL);
		}
		return listToReturn;
	}

	private List<QtlDetailElement> getcentralQTLDetails() throws MiddlewareQueryException {
		//genoManager.getAllQtl(arg0, arg1)
		
		
		
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			String qtlName = qtl.getQtlName();
			boolean bFound = false;
			for (QtlDetailElement qtlDetailElement : listToReturn) {
				if(qtlDetailElement.getQtlName().equals(qtlName)) {
					bFound = true;
					break;
				}
			}
			if(bFound) {
				continue;
			}
			//List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlDetailsByName(qtlName, 0, all.size());
			List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlAndQtlDetailsByName(qtlName, 0, all.size());
			if(null != qtlDetailsByName) {
				listToReturn.addAll(qtlDetailsByName);
			}
		}
		return listToReturn;
	}

	private List<QtlDetailElement> getLocalQTLDetails() throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			String qtlName = qtl.getQtlName();
			boolean bFound = false;
			for (QtlDetailElement qtlDetailElement : listToReturn) {
				if(qtlDetailElement.getQtlName().equals(qtlName)) {
					bFound = true;
					break;
				}
			}
			if(bFound) {
				continue;
			}
			//List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlDetailsByName(qtlName, 0, all.size());
			List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlAndQtlDetailsByName(qtlName, 0, all.size());
			if(null != qtlDetailsByName) {
				listToReturn.addAll(qtlDetailsByName);
			}
		}
		return listToReturn;
	}

	public List<QtlDetailElement> retrieveQTLDetailsStartsWith(String theStartWith) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		List<QtlDetailElement> localQTL = getLocalQTLDetailsStartsWith(theStartWith);
		if(null != localQTL) {
			listToReturn.addAll(localQTL);
		}
		List<QtlDetailElement> getcentrailQTL = getcentralQTLDetailsStartsWith(theStartWith);
		if(null != getcentrailQTL) {
			listToReturn.addAll(getcentrailQTL);
		}
		return listToReturn;
	}

	private List<QtlDetailElement> getcentralQTLDetailsStartsWith(String theStartWith) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			String qtlName = qtl.getQtlName();
			boolean bFound = false;
			for (QtlDetailElement qtlDetailElement : listToReturn) {
				if(qtlDetailElement.getQtlName().equals(qtlName)) {
					bFound = true;
					break;
				}
			}
			if(bFound) {
				continue;
			}
			if(qtlName.startsWith(theStartWith)) {
				//List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlDetailsByName(qtlName, 0, all.size());
				List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlAndQtlDetailsByName(qtlName, 0, all.size());
				if(null != qtlDetailsByName) {
					listToReturn.addAll(qtlDetailsByName);
				}
			}
		}
		return listToReturn;
	}

	private List<QtlDetailElement> getLocalQTLDetailsStartsWith(String theStartWith) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			String qtlName = qtl.getQtlName();
			boolean bFound = false;
			for (QtlDetailElement qtlDetailElement : listToReturn) {
				if(qtlDetailElement.getQtlName().equals(qtlName)) {
					bFound = true;
					break;
				}
			}
			if(bFound) {
				continue;
			}
			if(qtlName.startsWith(theStartWith)) {
				//List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlDetailsByName(qtlName, 0, all.size());
				List<QtlDetailElement> qtlDetailsByName = qtlDAO.getQtlAndQtlDetailsByName(qtlName, 0, all.size());
				if(null != qtlDetailsByName) {
					listToReturn.addAll(qtlDetailsByName);
				}
			}
		}
		return listToReturn;
	}

	public List<String> retrieveQTLNames() throws MiddlewareQueryException {
		List<String> listOfQTLNames = new ArrayList<String>();
		List<Qtl> localQTL = getLocalQTL();
		for (Qtl qtl : localQTL) {
			if(false == listOfQTLNames.contains(qtl.getQtlName())) {
				listOfQTLNames.add(qtl.getQtlName());
			}
		}
		List<Qtl> centralQTL = getCentralQTL();
		for (Qtl qtl : centralQTL) {
			if(false == listOfQTLNames.contains(qtl.getQtlName())) {
				listOfQTLNames.add(qtl.getQtlName());
			}
		}
		
		return listOfQTLNames;
	}

	private List<Qtl> getCentralQTL() throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		List<Qtl> all = qtlDAO.getAll();
		return all;
	}

	private List<Qtl> getLocalQTL() throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		List<Qtl> all = qtlDAO.getAll();
		return all;
	}

	/*public List<QtlDetailElement> retrieveTrait(String strTraitName) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		List<QtlDetailElement> localTrait = getLocalTrait(strTraitName);
		if(null != localTrait) {
			listToReturn.addAll(localTrait);
		}
		List<QtlDetailElement> centralTrait = getCentralTrait(strTraitName);
		if(null != centralTrait) {
			listToReturn.addAll(centralTrait);
		}
		return listToReturn;
	}
*/
	public List<QtlDetailElement> retrieveTrait(String strTraitName) throws MiddlewareQueryException {
		List<QtlDetailElement> listToReturn = new ArrayList<QtlDetailElement>();
		
		
		
		Integer traitIdByTraitName = getTraitIdByTraitName(strTraitName);
		
		if (null == traitIdByTraitName){
			return listToReturn;
		}
		//System.out.println("traitIdByTraitName=:"+traitIdByTraitName);
		List<QtlDetailElement> localTrait = getLocalTrait(traitIdByTraitName);
		//System.out.println(".............................   :"+localTrait);
		if(null != localTrait) {
			listToReturn.addAll(localTrait);
		}
		List<QtlDetailElement> centralTrait = getCentralTrait(traitIdByTraitName);
		//System.out.println("''''''''''''''''''''''''''''''''   :"+centralTrait);
		if(null != centralTrait) {
			listToReturn.addAll(centralTrait);
		}
		return listToReturn;
	}

	public List<Mta> retrieveMTA(String strTraitName) throws MiddlewareQueryException {
		List<Mta> listToReturn = new ArrayList<Mta>();
		
		
		
		Integer traitIdByTraitName = getTraitIdByTraitName(strTraitName);
		
		if (null == traitIdByTraitName){
			return listToReturn;
		}
		System.out.println("traitIdByTraitName=:"+traitIdByTraitName);
		List<Mta> listMTAByTrait = genoManager.getMTAsByTrait(traitIdByTraitName);
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   :"+genoManager.getMTAsByTrait(traitIdByTraitName));
		
		
		
		//List<QtlDetailElement> localTrait = getLocalTrait(traitIdByTraitName);
		System.out.println(".............................   :"+listMTAByTrait);
		if(null != listMTAByTrait) {
			listToReturn.addAll(listMTAByTrait);
		}
		
		System.out.println("listToReturn=:"+listToReturn);
		return listToReturn;
	}
	
	
	
	/*private List<QtlDetailElement> getCentralTrait(String strTraitName) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		int size = qtlDAO.getAll().size();
		List<Integer> listOfQTLIdsByTrait = qtlDAO.getQtlByTrait(strTraitName, 0, size);
		if(null == listOfQTLIdsByTrait || 0 == listOfQTLIdsByTrait.size()) {
			return new ArrayList<QtlDetailElement>();
		}
		return qtlDAO.getQtlDetailsByQTLIDs(listOfQTLIdsByTrait, 0, size);
	}*/
	
	private Integer getTraitIdByTraitName(String strTraitName) throws MiddlewareQueryException {

		Integer localTraitId = getLocalTrait(strTraitName);
		if (null != localTraitId){
			return localTraitId;
		} else {
			Integer centralTraitId = getCentralTrait(strTraitName);
			return centralTraitId;
		}
	}
	


	private Integer getLocalTrait(String strTraitName) throws MiddlewareQueryException {
		int cvtermId=0;
		StandardVariable stdVariable = new StandardVariable();
		Set<StandardVariable> standardVariables = om.findStandardVariablesByNameOrSynonym(strTraitName);
		//System.out.println("%%%%%%%%%%%%%%%   :"+om.getTermById(22396));
		//System.out.println("%%%%%%%..........%%%%%%%%   :"+ om.findStandardVariablesByNameOrSynonym("HI"));
		//assertTrue(standardVariables.size() == 1);
		for (StandardVariable stdVar : standardVariables) {
			//System.out.println(stdVar);
			//System.out.println("!~!~!~!~!!!!!!!!!!!!!!!!~~~~~~~~~"+stdVar.getId()+"~~~~~~~~~~~  :"+stdVar.getProperty().getName()+"   ontology ID=:"+stdVar.getCropOntologyId());
			cvtermId=stdVar.getId();
			//return stdVar.getId();
		}		
		
		return cvtermId;
	}


	private Integer getCentralTrait(String strTraitName) throws MiddlewareQueryException {
		int cvtermId=0;
		Set<StandardVariable> standardVariables = om.findStandardVariablesByNameOrSynonym(strTraitName);
		//assertTrue(standardVariables.size() == 1);
		for (StandardVariable stdVar : standardVariables) {
			//System.out.println(stdVar.getId()+"   "+stdVar.getNameSynonyms()+"   "+stdVar.getName());
			cvtermId=stdVar.getId();
			//return stdVar.getId();
		}	
		
		return cvtermId;
	}


	private List<QtlDetailElement> getCentralTrait(Integer iTraitId) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		int size = qtlDAO.getAll().size();
		
		List<Integer> listOfQTLIdsByTrait = qtlDAO.getQtlByTrait(iTraitId, 0, size);
		//System.out.println("^^^^^^^^^^^^^ From Central ^^^^^^^^^^   :"+size+"   "+listOfQTLIdsByTrait);
		if(null == listOfQTLIdsByTrait || 0 == listOfQTLIdsByTrait.size()) {
			return new ArrayList<QtlDetailElement>();
		}
		//return qtlDAO.getQtlDetailsByQTLIDs(listOfQTLIdsByTrait, 0, size);
		return qtlDAO.getQtlAndQtlDetailsByQtlIds(listOfQTLIdsByTrait, 0, size);
	}

/*	private List<QtlDetailElement> getLocalTrait(String strTraitName) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		int size = qtlDAO.getAll().size();
		List<Integer> listOfQTLIdsByTrait = qtlDAO.getQtlByTrait(strTraitName, 0, size);
		if(null == listOfQTLIdsByTrait || 0 == listOfQTLIdsByTrait.size()) {
			return new ArrayList<QtlDetailElement>();
		}
		return qtlDAO.getQtlDetailsByQTLIDs(listOfQTLIdsByTrait, 0, size);

	}*/
	
	private List<QtlDetailElement> getLocalTrait(Integer iTraitId) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		int size = qtlDAO.getAll().size();
		
		List<Integer> listOfQTLIdsByTrait = qtlDAO.getQtlByTrait(iTraitId, 0, size);
		//System.out.println("^^^^^^^^^^^^^ From Local ^^^^^^^^^^   :"+size+"   "+listOfQTLIdsByTrait);
		if(null == listOfQTLIdsByTrait || 0 == listOfQTLIdsByTrait.size()) {
			return new ArrayList<QtlDetailElement>();
		}
		//return qtlDAO.getQtlDetailsByQTLIDs(listOfQTLIdsByTrait, 0, size);
		List<QtlDetailElement> retrieveQTLDetails =genoManager.getQtlByQtlIds(listOfQTLIdsByTrait, 0, size);
		
		return retrieveQTLDetails;
	}
	

	public QtlDetailElement retrieveTraitNameStartWith(String strTraitSearchName) throws MiddlewareQueryException {
		List<QtlDetailElement> retrieveQTLDetails = retrieveQTLDetails();
		for (QtlDetailElement qtlDetailElement : retrieveQTLDetails) {
			if(qtlDetailElement.gettRName().startsWith(strTraitSearchName)) {
				return qtlDetailElement;
			}
		}
		return null;
	}

	public List<QtlDetails> retrieveQTLDetailsWithQTLDetailsPK() throws MiddlewareQueryException {
		List<QtlDetails> listOfQTLDetails = new ArrayList<QtlDetails>();
		List<QtlDetails> localQTLDetailsWithQTLDetailsPK = getLocalQTLDetailsWithQTLDetailsPK();
		if(null != localQTLDetailsWithQTLDetailsPK) {
			listOfQTLDetails.addAll(localQTLDetailsWithQTLDetailsPK);
		}
		List<QtlDetails> centralQTLDetailsWithQTLDetailsPK = getCentralQTLDetailsWithQTLDetailsPK();
		if(null != centralQTLDetailsWithQTLDetailsPK) {
			listOfQTLDetails.addAll(centralQTLDetailsWithQTLDetailsPK);
		}
		return listOfQTLDetails;
	}

	private List<QtlDetails> getLocalQTLDetailsWithQTLDetailsPK() throws MiddlewareQueryException {
		QtlDetailsDAO dao = new QtlDetailsDAO();
		dao.setSession(localSession);
		return dao.getAll();
	}

	private List<QtlDetails> getCentralQTLDetailsWithQTLDetailsPK() throws MiddlewareQueryException {
		QtlDetailsDAO dao = new QtlDetailsDAO();
		dao.setSession(centralSession);
		return dao.getAll();
	}

}
