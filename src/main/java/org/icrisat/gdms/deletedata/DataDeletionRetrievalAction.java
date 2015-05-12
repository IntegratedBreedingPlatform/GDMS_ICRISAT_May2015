package org.icrisat.gdms.deletedata;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.dao.gdms.DatasetDAO;
import org.generationcp.middleware.dao.gdms.MapDAO;
import org.generationcp.middleware.manager.GdmsType;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.Map;
import org.hibernate.Session;
import org.icrisat.gdms.ui.common.GDMSModel;

public class DataDeletionRetrievalAction {

	
	public ArrayList<String> getGenotypingDataList()
			throws Exception {
		ArrayList<String> gList = new ArrayList<String>();
		try{
			Session session = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			DatasetDAO datasetDAO = new DatasetDAO();
			datasetDAO.setSession(session);
			List<Dataset> allDataset = datasetDAO.getAll();
			List<String> datasetNames = datasetDAO.getDatasetNames(0, allDataset.size());
			for (String string : datasetNames) {
				gList.add(string);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return gList;
	}
	
	public ArrayList<String> getCentralGenotypingDataList() throws Exception {
		ArrayList<String> gList = new ArrayList<String>();
		try{
			Session session =GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			DatasetDAO datasetDAO = new DatasetDAO();
			datasetDAO.setSession(session);
			List<Dataset> allDataset = datasetDAO.getAll();
			List<String> datasetNames = datasetDAO.getDatasetNames(0, allDataset.size());
			for (String string : datasetNames) {
				gList.add(string);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return gList;
	}
	
	public ArrayList<String> getMapsList()
			throws Exception {
		ArrayList<String> mList = new ArrayList<String>();
		try{
			Session session = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();

			MapDAO mapDAO = new MapDAO();
			mapDAO.setSession(session);
			List<Map> allMaps = mapDAO.getAll();
			for (Map map2 : allMaps) {
				mList.add(map2.getMapName());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return mList;
	}
	
	public ArrayList<String> getCentralMapsList() throws Exception {
		ArrayList<String> mList = new ArrayList<String>();
		try{
			Session session =GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();

			MapDAO mapDAO = new MapDAO();
			mapDAO.setSession(session);
			List<Map> allMaps = mapDAO.getAll();
			for (Map map2 : allMaps) {
				mList.add(map2.getMapName());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return mList;
	}
	
	
	public ArrayList<String> getQTLInfoList() throws Exception {
		ArrayList<String> qList = new ArrayList<String>();
		try {
			Session session = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			DatasetDAO datasetDAO = new DatasetDAO();
			datasetDAO.setSession(session);
			List<Dataset> allDataset = datasetDAO.getAll();

			for (Dataset dataset : allDataset) {
				if(dataset.getDatasetType().equalsIgnoreCase("qtl")) {
					qList.add(dataset.getDatasetName());
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return qList;
	}
	
	public ArrayList<String> getCentralQTLInfoList() throws Exception {
		ArrayList<String> qList = new ArrayList<String>();
		try {
			Session session = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			DatasetDAO datasetDAO = new DatasetDAO();
			datasetDAO.setSession(session);
			List<Dataset> allDataset = datasetDAO.getAll();

			for (Dataset dataset : allDataset) {
				if(dataset.getDatasetType().equalsIgnoreCase("qtl")) {
					qList.add(dataset.getDatasetName());
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return qList;
	}
	
	public ArrayList<String> getMTAInfoList() throws Exception {
		ArrayList<String> mList = new ArrayList<String>();
		try {
			
			System.out.println(">>>>>>>>>>>>>..............:"+GDMSModel.getGDMSModel().getManagerFactory().getGenotypicDataManager().getDatasetsByType(GdmsType.TYPE_MTA));
			
			Session session = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			DatasetDAO datasetDAO = new DatasetDAO();
			datasetDAO.setSession(session);
			//List<Dataset> allDataset = datasetDAO.getAll();
			List<Dataset> allDataset=GDMSModel.getGDMSModel().getManagerFactory().getGenotypicDataManager().getDatasetsByType(GdmsType.TYPE_MTA);
			for (Dataset dataset : allDataset) {
				if(dataset.getDatasetId()<1)
					mList.add(dataset.getDatasetName());
				
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return mList;
	}
	
	public ArrayList<String> getCentralMTAInfoList() throws Exception {
		ArrayList<String> mList = new ArrayList<String>();
		try {
			Session session = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			DatasetDAO datasetDAO = new DatasetDAO();
			datasetDAO.setSession(session);
			//List<Dataset> allDataset = datasetDAO.getAll();
			List<Dataset> allDataset=GDMSModel.getGDMSModel().getManagerFactory().getGenotypicDataManager().getDatasetsByType(GdmsType.TYPE_MTA);
			for (Dataset dataset : allDataset) {
				if(dataset.getDatasetId()>0)
					mList.add(dataset.getDatasetName());
				
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return mList;
	}
	
	
	
}
