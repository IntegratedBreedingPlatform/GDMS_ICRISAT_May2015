package org.icrisat.gdms.upload.maporqtl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.genotyping.GDMSTrackDataBean;
import org.icrisat.gdms.upload.genotyping.GDMSTrackMarkersBean;

public class HaplotypeUpload {
	ManagerFactory factory;
    GenotypicDataManager genoManager;
    String str="";
    ArrayList markers=new ArrayList();
    private GDMSTrackMarkersBean gdmsTrackMarkers;
    private GDMSTrackDataBean gdmsTrackData;
     
    
    Integer iUserId = 0;
    Integer trackId=0;
    
    Integer trackMarkerId=0;
    
    private Transaction tx;
	
	private Session localSession;
	//private Session centralSession;
	public String createObjectsToBeSavedToDB(String hapValue, ArrayList markersList) throws GDMSException {
		//System.out.println("%%%%%%%%%%%:"+hapValue);
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			genoManager=factory.getGenotypicDataManager();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			tx=localSession.beginTransaction();
			
			List<Marker> markersCentral=genoManager.getMarkersByMarkerNames(markersList, 0, markersList.size(), Database.CENTRAL);
			if(!markersCentral.isEmpty()){
				for (Marker iMarkerID : markersCentral){				
					if(!markers.contains(iMarkerID.getMarkerId()))
						markers.add(iMarkerID.getMarkerId());
				}
			}
			List<Marker> markersLocal=genoManager.getMarkersByMarkerNames(markersList, 0, markersList.size(), Database.LOCAL);
			if(!markersLocal.isEmpty()){
				for (Marker iMarkerID : markersLocal){	
					if(!markers.contains(iMarkerID.getMarkerId()))
						markers.add(iMarkerID.getMarkerId());
				}
			}
			/*List<Integer> markersCentral=genoManager.getMarkerIdsByMarkerNames(markersList, 0, markersList.size(), Database.CENTRAL);
			if(!markersCentral.isEmpty()){
				for (Integer iMarkerID : markersCentral){				
					if(!markers.contains(markersCentral.get(iMarkerID)))
						markers.add(markersCentral.get(iMarkerID));
				}
			}
			List<Integer> markersLocal=genoManager.getMarkerIdsByMarkerNames(markersList, 0, markersList.size(), Database.LOCAL);
			if(!markersLocal.isEmpty()){
				for (Integer iMarkerID : markersLocal){	
					if(!markers.contains(markersLocal.get(iMarkerID)))
						markers.add(markersLocal.get(iMarkerID));
				}
			}*/
		}catch (Exception e){
			e.printStackTrace();
		}
		//System.out.println("Selected markers for haplotype:"+markers);
		try {
			
			iUserId=GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId();
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		int intMaxVal=0;
		int intMaxValTM=0;
		Object obj=null;
		Iterator itList=null;
		List listValues=null;
		Query query=localSession.createSQLQuery("select min(track_id) from gdms_track_data");
		
		listValues=query.list();
		itList=listValues.iterator();
					
		while(itList.hasNext()){
			obj=itList.next();
			if(obj!=null)
				intMaxVal=Integer.parseInt(obj.toString());
		}
		
		trackId=intMaxVal-1;
		
		/**
		 * retrieving last id for Track Marker id
		 */
		int intMaxTrackMarkerVal=0;
		Object objTM=null;
		Iterator itListTM=null;
		List listValuesTM=null;
		Query queryTM=localSession.createSQLQuery("select min(tmarker_id) from gdms_track_markers");
		
		listValuesTM=queryTM.list();
		itListTM=listValuesTM.iterator();
					
		while(itListTM.hasNext()){
			objTM=itListTM.next();
			if(objTM!=null)
				intMaxValTM=Integer.parseInt(objTM.toString());
		}
		
		trackMarkerId=intMaxValTM-1;		
		
		gdmsTrackData= new GDMSTrackDataBean();
		gdmsTrackData.setTrack_id(trackId);
		gdmsTrackData.setTrack_name(hapValue);
		gdmsTrackData.setUser_id(iUserId);
		localSession.save(gdmsTrackData);
		
		for(int m=0;m<markers.size();m++){
			gdmsTrackMarkers= new GDMSTrackMarkersBean();
			gdmsTrackMarkers.settMarkerId(trackMarkerId);
			gdmsTrackMarkers.setTrackId(trackId);
			gdmsTrackMarkers.setMarkerId(Integer.parseInt(markers.get(m).toString()));
			gdmsTrackMarkers.setMarkerSampleId(1);
			localSession.save(gdmsTrackMarkers);
			
			if (m % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
			trackMarkerId--;
		}
		tx.commit();
		str= "successfull";
		return str;
	}
	
}
