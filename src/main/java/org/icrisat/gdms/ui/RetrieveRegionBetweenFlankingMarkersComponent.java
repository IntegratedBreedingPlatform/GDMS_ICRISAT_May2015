package org.icrisat.gdms.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jxl.write.WriteException;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.gdms.MapInfo;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.gdms.MarkerInfo;
import org.generationcp.middleware.pojos.gdms.MarkerOnMap;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

public class RetrieveRegionBetweenFlankingMarkersComponent implements Component.Listener{
	
private static final long serialVersionUID = 1L;
	
	ManagerFactory factory=null;
	private Session localSession;
	private Session centralSession;
	GermplasmDataManager manager;
	GenotypicDataManager genoManager;
	private GDMSMain _mainHomePage;
	private TabSheet _tabsheetForHaps;
	private Component buildResultsComponent;
	String str2MarkerQuerry2="";
	
	String strMarker1 ="";
	String strMarker2 ="";
	List markerNamesList=new ArrayList();
	
	List markerFromLocal=new ArrayList();	
	List markerFromCentral=new ArrayList();
	List markersList=new ArrayList();	
	List markersListH=new ArrayList();
	List nIDs=new ArrayList();
	List gidsList=new ArrayList();
	Object obj=null;
	Object objL=null;
	Iterator itListC=null;
	Iterator itListL=null;	
	List maps=new ArrayList();
	
	List markerId=new ArrayList();
	String dataExists="no";
	private Table _regionTable;
	
		List<String>listOfNamesForMarker1= new ArrayList();
		
	//List<MarkerIdMarkerNameElement>listOfNamesForMarker1= new ArrayList();
	List<MarkerIdMarkerNameElement>listOfNamesForMarker2= new ArrayList();
	List<MarkerInfo> markerInfo=new ArrayList();
	List ch=new ArrayList();
	
	HashMap<Integer, String> hashMapMarkerInfo=new HashMap<Integer, String>();
	
	HashMap<Integer, String> hashMapMarkerIdsNames=new HashMap<Integer, String>();
	HashMap<Integer, String> hashMapMapIdsNames=new HashMap<Integer, String>();
	List mapInfoMarkersList=new ArrayList();
	public RetrieveRegionBetweenFlankingMarkersComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;		
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();		
			genoManager=factory.getGenotypicDataManager();
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			manager=factory.getGermplasmDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}		
	}
	
	public HorizontalLayout buildTabbedComponentForFlankingMarkers() {
		
		HorizontalLayout horizontalLayout = new HorizontalLayout();

		_tabsheetForHaps = new TabSheet();
		_tabsheetForHaps.setWidth("50em");
		
		
		Component buildMarkerComponent = buildFlankingMarkersComponent();
		buildMarkerComponent.setSizeFull();

		buildResultsComponent = buildResultsComponent();
		buildResultsComponent.setSizeFull();

		_tabsheetForHaps.addComponent(buildMarkerComponent);
		_tabsheetForHaps.addComponent(buildResultsComponent);

		_tabsheetForHaps.getTab(0).setEnabled(true);
		_tabsheetForHaps.getTab(1).setEnabled(false);
		horizontalLayout.addComponent(_tabsheetForHaps);
		
		return horizontalLayout;
		
	}
	private Component buildResultsComponent() {
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setCaption("Results");
		verticalLayout.setMargin(true, true, true, true);
		verticalLayout.setSpacing(true);
		String lbl1="";
		lbl1="'"+strMarker1+" and "+strMarker2+"'";
		if (0 != mapInfoMarkersList.size()){
			//mapInfoMarkersList
			//Label lblMarkersFound = new Label("'"+mapInfoMarkersList.size() + "' Marker's found between the provided flanking markers "+lbl1+" on different Maps");
			Label lblMarkersFound = new Label("Region between the provided flanking markers "+lbl1);
			lblMarkersFound.setStyleName(Reindeer.LABEL_H2);
			verticalLayout.addComponent(lblMarkersFound);
			verticalLayout.setComponentAlignment(lblMarkersFound, Alignment.TOP_CENTER);
			
			Table tableResults = buildMarkersInSelectedRegionTable(verticalLayout);
			tableResults.setWidth("100%");
			tableResults.setPageLength(10);
			tableResults.setSelectable(true);
			tableResults.setColumnCollapsingAllowed(false);
			tableResults.setColumnReorderingAllowed(true);
			tableResults.setStyleName("strong");
			
			verticalLayout.addComponent(tableResults);
			verticalLayout.setComponentAlignment(tableResults, Alignment.MIDDLE_CENTER);
			
			
		
			HorizontalLayout layoutForExportTypes = new HorizontalLayout();
			layoutForExportTypes.setSpacing(true);
			
			ThemeResource themeResource = new ThemeResource("images/excel.gif");
			Button excelButton = new Button();
			excelButton.setIcon(themeResource);
			excelButton.setStyleName(Reindeer.BUTTON_LINK);
			excelButton.setDescription("EXCEL Format");
			
			excelButton.addListener(new Button.ClickListener() {
			
				private static final long serialVersionUID = 1L;
	
				@Override
				public void buttonClick(ClickEvent event) {
					List<String[]> listOfData = new ArrayList<String[]>();
					String PI, motifType, motif, forPrimer, revPrimer, contact, institute="";
					if (null != mapInfoMarkersList){			
						for (int h = 0; h < mapInfoMarkersList.size(); h++){
							//System.out.println("mapInfoMarkersList.get(h):"+mapInfoMarkersList.get(h));
							
							String[] strRegion= mapInfoMarkersList.get(h).toString().split("!~!");
							//System.out.println(strRegion[0]+","+strRegion[1]+","+strRegion[2]+","+strRegion[3]);
							String[] markerInformation=hashMapMarkerInfo.get(Integer.parseInt(strRegion[0].toString())).split("!~!");
							if(markerInformation[2].toString().equalsIgnoreCase("null"))
								motifType=" ";
							else
								motifType=markerInformation[2].toString();
							
							if(markerInformation[3].toString().equalsIgnoreCase("null"))
								motif=" ";
							else
								motif=markerInformation[3].toString();
							
							if(markerInformation[4].toString().equalsIgnoreCase("null"))
								forPrimer=" ";
							else
								forPrimer=markerInformation[4].toString();
							
							if(markerInformation[5].toString().equalsIgnoreCase("null"))
								revPrimer=" ";
							else
								revPrimer=markerInformation[5].toString();	
							
							if(markerInformation[7].toString().equalsIgnoreCase("null"))
								PI=" ";
							else
								PI=markerInformation[7].toString();
							
							if(markerInformation[8].toString().equalsIgnoreCase("null"))
								contact=" ";
							else
								contact=markerInformation[8].toString();
							if(markerInformation[9].toString().equalsIgnoreCase("null"))
								institute=" ";
							else
								institute=markerInformation[9].toString();
							
							
							String markerName=hashMapMarkerIdsNames.get(Integer.parseInt(strRegion[0].toString()));				
							String mapName = hashMapMapIdsNames.get(Integer.parseInt(strRegion[1].toString()));		
							listOfData.add(new String[] {markerName, markerInformation[0], markerInformation[1], mapName,strRegion[2],strRegion[3],motifType,motif,forPrimer,revPrimer, markerInformation[6], PI, contact , institute});
							      
							//_regionTable.addItem(new Object[] {markerName, markerInformation[0], markerInformation[1], mapName,strRegion[2],strRegion[3],markerInformation[2],markerInformation[3],markerInformation[4],markerInformation[5], markerInformation[6], PI, markerInformation[8] , markerInformation[9]}, new Integer(h));
						}
						String[] strArrayOfColNames = {"MARKER","MARKER TYPE", "SPECIES", "MAP NAME", "LINKAGE GROUP", "POSITION", "MOTIF TYPE", "MOTIF", "FORWARD PRIMER", "REVERSE PRIMER", "ANNEALING TEMPERATURE", "PRINCIPAL INVESTIGATOR", "CONTACT", "INSTITUTE"};
						listOfData.add(0, strArrayOfColNames);
						ExportFileFormats exportFileFormats = new ExportFileFormats();
						try {
							exportFileFormats.exportMap(_mainHomePage, listOfData, "tmp");
						} catch (WriteException e) {
							
						} catch (IOException e) {
							
						}
					}
					
				}
			});
		
		
			layoutForExportTypes.addComponent(excelButton);	
			
			verticalLayout.addComponent(layoutForExportTypes);
			verticalLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
		}
		return verticalLayout;
	}
	
	private Table buildMarkersInSelectedRegionTable(final VerticalLayout resultsLayout) {
		_regionTable = new Table();
		_regionTable.setStyleName("markertable");
		_regionTable.setPageLength(10);
		_regionTable.setSelectable(true);
		_regionTable.setColumnCollapsingAllowed(true);
		_regionTable.setColumnReorderingAllowed(true);

		
		String[] strArrayOfColNames = {"MARKER","MARKER TYPE", "SPECIES", "MAP NAME", "LINKAGE GROUP", "POSITION", "MOTIF TYPE", "MOTIF", "FORWARD PRIMER", "REVERSE PRIMER", "ANNEALING TEMPERATURE", "PRINCIPAL INVESTIGATOR", "CONTACT", "INSTITUTE"};		
		
		for (int i = 0; i < strArrayOfColNames.length; i++){			
			_regionTable.addContainerProperty(strArrayOfColNames[i], String.class, null);			
		}
		
		String PI, motifType, motif, forPrimer, revPrimer, contact, institute="";
		if (null != mapInfoMarkersList){			
			for (int h = 0; h < mapInfoMarkersList.size(); h++){
				//System.out.println("mapInfoMarkersList.get(h):"+mapInfoMarkersList.get(h));
				
				String[] strRegion= mapInfoMarkersList.get(h).toString().split("!~!");
				//System.out.println(strRegion[0]+","+strRegion[1]+","+strRegion[2]+","+strRegion[3]);
				//System.out.println(hashMapMarkerInfo.get(Integer.parseInt(strRegion[0].toString())));
				String[] markerInformation=hashMapMarkerInfo.get(Integer.parseInt(strRegion[0].toString())).split("!~!");
				
				if(markerInformation[2].toString().equalsIgnoreCase("null"))
					motifType=" ";
				else
					motifType=markerInformation[2].toString();
				
				if(markerInformation[3].toString().equalsIgnoreCase("null"))
					motif=" ";
				else
					motif=markerInformation[3].toString();
				
				if(markerInformation[4].toString().equalsIgnoreCase("null"))
					forPrimer=" ";
				else
					forPrimer=markerInformation[4].toString();
				
				if(markerInformation[5].toString().equalsIgnoreCase("null"))
					revPrimer=" ";
				else
					revPrimer=markerInformation[5].toString();	
				
				if(markerInformation[7].toString().equalsIgnoreCase("null"))
					PI=" ";
				else
					PI=markerInformation[7].toString();
				
				if(markerInformation[8].toString().equalsIgnoreCase("null"))
					contact=" ";
				else
					contact=markerInformation[8].toString();
				if(markerInformation[9].toString().equalsIgnoreCase("null"))
					institute=" ";
				else
					institute=markerInformation[9].toString();
				
				
				String markerName=hashMapMarkerIdsNames.get(Integer.parseInt(strRegion[0].toString()));				
				String mapName = hashMapMapIdsNames.get(Integer.parseInt(strRegion[1].toString()));				
				_regionTable.addItem(new Object[] {markerName, markerInformation[0], markerInformation[1], mapName,strRegion[2],strRegion[3],motifType,motif,forPrimer,revPrimer, markerInformation[6], PI, contact , institute}, new Integer(h));
			}
		}
		
		return _regionTable;
	}
	
	
	private Component buildFlankingMarkersComponent() {
		
		HorizontalLayout selectMainLayout= new HorizontalLayout();
		//selectMainLayout..setComponentAlignment(childComponent, alignment)
		selectMainLayout.setCaption("Select Markers");
		//selectMainLayout.setWidth(90, UNITS_PERCENTAGE);
		//selectMainLayout.setwi
		selectMainLayout.setWidth("400px");
		selectMainLayout.setHeight("300px");
		
		VerticalLayout selectMarkersLayout = new VerticalLayout();
		//selectMarkersLayout.setCaption("Select Markers");
		selectMarkersLayout.setMargin(true, true, true, true);
		selectMarkersLayout.setSpacing(true);
		selectMarkersLayout.setWidth("950px");
		Label lblSearch = new Label("Search region between flanking markers");
		lblSearch.setStyleName(Reindeer.LABEL_H2);
		selectMarkersLayout.addComponent(lblSearch);
		selectMarkersLayout.setComponentAlignment(lblSearch, Alignment.TOP_CENTER);
		//selectMarkersLayout.setHeight("205px");
		
		HorizontalLayout horiMarkersLayout = new HorizontalLayout();
		final ComboBox selectMarker1 = new ComboBox();
		Object itemId1 = selectMarker1.addItem();
		selectMarker1.setItemCaption(itemId1, "Select Marker-1");
		selectMarker1.setValue(itemId1);
		selectMarker1.setNullSelectionAllowed(false);
		selectMarker1.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
		selectMarker1.setImmediate(true);
		Label lblAnd = new Label("and");
		lblAnd.setStyleName(Reindeer.LABEL_H2);
		try {
			listOfNamesForMarker1 = retrieveMarkers();
			//System.out.println("......................  :"+listOfNamesForLine1);
			
			if (null != selectMarker1 && 0 != selectMarker1.getItemIds().size()) {
				selectMarker1.removeAllItems();
				itemId1 = selectMarker1.addItem();
				selectMarker1.setItemCaption(itemId1, "Select Marker-1");
				selectMarker1.setValue(itemId1);
			}
			
			if (null != listOfNamesForMarker1){
				for (int m=0;m<listOfNamesForMarker1.size();m++) {
					//selectMarker1.addItem(listOfNamesForMarker1.get(m).getMarkerName());
					selectMarker1.addItem(listOfNamesForMarker1.get(m));
				}
			}else{
				String strErrorMessage = "No Mapping datasets available";
				_mainHomePage.getMainWindow().getWindow().showNotification(strErrorMessage, Notification.TYPE_ERROR_MESSAGE);
				//return;
			}
			
		} catch (MiddlewareQueryException e1) {
			String strErrorMessage = "Error retrieving data for Marker-1.";
			/*if (null != e1.getExceptionMessage()){
				strErrorMessage = e1.getExceptionMessage();
			}*/
			_mainHomePage.getMainWindow().getWindow().showNotification(strErrorMessage, Notification.TYPE_ERROR_MESSAGE);
			//return;
		}
		horiMarkersLayout.addComponent(selectMarker1);
		horiMarkersLayout.setWidth("440px");
		horiMarkersLayout.addComponent(lblAnd);
		horiMarkersLayout.setComponentAlignment(lblAnd, Alignment.MIDDLE_LEFT);
		
		final ComboBox selectMarker2 = new ComboBox();
		Object itemId2 = selectMarker2.addItem();
		selectMarker2.setItemCaption(itemId2, "Select Marker-2");
		selectMarker2.setValue(itemId2);
		selectMarker2.setNullSelectionAllowed(false);
		selectMarker2.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
		
		
		selectMarker1.addListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				String strSelectedNameValue = "";
				if (null != selectMarker1.getValue()) {
					strSelectedNameValue = selectMarker1.getValue().toString();
				}
				Integer iGermplasmId = 0;
				//System.out.println("@@@@@@@@@@@@@@@@FFFFFFFFFFFFFFFFFF>>>>>>>>>>>>>>>>>>>>:"+strSelectedNameValue);
				try{
					listOfNamesForMarker2=retrieveMarkersForSecondList(strSelectedNameValue);
				}catch(MiddlewareQueryException e){
					e.printStackTrace();
				}
				//System.out.println("secont list of markers:"+listOfNamesForMarker2);
				
				if (null != selectMarker2 && 0 != selectMarker2.getItemIds().size()) {
					selectMarker2.removeAllItems();
					Object itemId2 = selectMarker2.addItem();
					selectMarker2.setItemCaption(itemId2, "Select Marker-2");
					selectMarker2.setValue(itemId2);
				}
				
				if (null != listOfNamesForMarker2){
					for (MarkerIdMarkerNameElement name2 : listOfNamesForMarker2) {
						String nval = name2.getMarkerName();
						if (false == nval.equals(strSelectedNameValue)) {
							selectMarker2.addItem(name2.getMarkerName());
						}
					}
				}
			}
		});
		
		horiMarkersLayout.addComponent(selectMarker2);
		horiMarkersLayout.setComponentAlignment(selectMarker2, Alignment.MIDDLE_RIGHT);
		
		VerticalLayout layoutForButton = new VerticalLayout();
		Button btnNext = new Button("Next");
		layoutForButton.addComponent(btnNext);
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_LEFT);
		layoutForButton.setWidth("450px");
		layoutForButton.setMargin(true, false, true, true);
		final List<String> markerList=new ArrayList();
		final List<Integer> markerIdsList=new ArrayList();
		final HashMap<String, Integer> hashMapMarkerNamesIDs= new HashMap<String, Integer>();
		btnNext.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				List markerIdsList=new ArrayList();
				String mids="";
				strMarker1 = selectMarker1.getValue().toString();			
				strMarker2 = selectMarker2.getValue().toString();
				//System.out.println("strMarker1="+strMarker1+"  strMarker2 =:"+strMarker2);
				markerList.add(strMarker1);
				markerList.add(strMarker2);
				try{
					List<Marker> MarkerIdsC=genoManager.getMarkersByMarkerNames(markerList, 0, markerList.size(), Database.CENTRAL);
					List<Marker> MarkerIdsL=genoManager.getMarkersByMarkerNames(markerList, 0, markerList.size(), Database.LOCAL);
					if(MarkerIdsC!=null){
						for(Marker mCRes:MarkerIdsC){
							markerIdsList.add(mCRes.getMarkerId());
							hashMapMarkerNamesIDs.put(mCRes.getMarkerName(), mCRes.getMarkerId());
						}
					}
					if(MarkerIdsL!=null){
						for(Marker mLRes:MarkerIdsL){
							markerIdsList.add(mLRes.getMarkerId());
							hashMapMarkerNamesIDs.put(mLRes.getMarkerName(), mLRes.getMarkerId());
						}
					}
					for(int m=0;m<markerIdsList.size();m++){
						mids=mids+markerIdsList.get(m)+",";
					}
					//System.out.println(genoManager.getMapAndMarkerCountByMarkers(markerIdsList));
					//genoManager.getMap
					
					List<MarkerOnMap> markersOnMap = genoManager.getMarkersOnMapByMarkerIds(markerIdsList);
					
					markersListH=new ArrayList();
					maps=new ArrayList();
					
					for(MarkerOnMap resMarkers:markersOnMap){
						maps.add(resMarkers.getMapId());
						markersListH.add(resMarkers.getMarkerId()+"!~!"+resMarkers.getMapId()+"!~!"+resMarkers.getLinkageGroup()+"!~!"+resMarkers.getStartPosition());
						
					}
					try{
						localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
						centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
					}catch (Exception e){
						e.printStackTrace();
					}
					
					/*String strHapQuerry="SELECT marker_id, map_id, linkage_group, start_position from gdms_markers_onmap where marker_id IN("+mids.substring(0, mids.length()-1)+") order by map_id, Linkage_group, start_position";			
					//System.out.println(strHapQuerry);	
					
					markerNamesList=new ArrayList();
					
					markerFromLocal=new ArrayList();	
					markerFromCentral=new ArrayList();
					markersList=new ArrayList();	
					markersListH=new ArrayList();
					nIDs=new ArrayList();
					gidsList=new ArrayList();
					obj=null;
					objL=null;
					itListC=null;
					itListL=null;	
					
					
					markerId=new ArrayList();
					try{
						localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
						centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
					}catch (Exception e){
						e.printStackTrace();
					}*/
					
					/*
					
					SQLQuery queryC=centralSession.createSQLQuery(strHapQuerry);
					queryC.addScalar("marker_id",Hibernate.INTEGER);	
					queryC.addScalar("map_id",Hibernate.INTEGER);	
					queryC.addScalar("linkage_group",Hibernate.STRING);	
					queryC.addScalar("start_position",Hibernate.DOUBLE);						
					markerFromCentral=queryC.list();					
					if(markerFromCentral.size()!=0){
						itListL=markerFromCentral.iterator();	
						for(int wl=0;wl<markerFromCentral.size();wl++){
							Object[] strMareO= (Object[])markerFromCentral.get(wl);
							maps.add(strMareO[1]);
							markersListH.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]);							
						}
					}
					
					SQLQuery queryL=localSession.createSQLQuery(strHapQuerry);		
					queryL.addScalar("marker_id",Hibernate.INTEGER);	
					queryL.addScalar("map_id",Hibernate.INTEGER);	
					queryL.addScalar("linkage_group",Hibernate.STRING);	
					queryL.addScalar("start_position",Hibernate.DOUBLE);					
					markerFromLocal=queryL.list();
					if(markerFromLocal.size()!=0){
						itListL=markerFromLocal.iterator();			
						for(int wl=0;wl<markerFromLocal.size();wl++){
							Object[] strMareO= (Object[])markerFromLocal.get(wl);
						   // System.out.println("W=....."+wl+"    "+strMareO[0]+"   "+strMareO[1]+"   "+strMareO[1]+"   "+strMareO[3]);
							maps.add(strMareO[1]);
						    markersListH.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]);
							
						}
					}*/
					//System.out.println("markersListH:"+markersListH);
					
					
						String position="";
						ch=new ArrayList();
						for(int m=0;m<markersListH.size();m++){
							
							String[] str=markersListH.get(m).toString().split("!~!");
							//System.out.println("str[0]:"+str[0]+"  strMarker1=:"+hashMapMarkerNamesIDs.get(strMarker1)+" strMarker2:"+hashMapMarkerNamesIDs.get(strMarker2));
							if(Integer.parseInt(str[0])==hashMapMarkerNamesIDs.get(strMarker1)){
								String pos=str[3];
								String map=str[1];
								String chr=str[2];
								m++;
								if(m!=markersListH.size()){
									String[] str1=markersListH.get(m).toString().split("!~!");
									//System.out.println(Integer.parseInt(str1[0])+"=="+hashMapMarkerNamesIDs.get(strMarker2)+")&&("+str1[1]+".equalsIgnoreCase("+map+"))&&("+str1[2]+".equalsIgnoreCase("+chr);
									if(Integer.parseInt(str1[0])==hashMapMarkerNamesIDs.get(strMarker2)){
										if(str1[1].equalsIgnoreCase(map)&&(str1[2].equalsIgnoreCase(chr))){									
											dataExists="Yes";
											ch.add(str1[1]+"!~!"+str1[2]+"!~!"+pos+"!~!"+str1[3]);
												
										}else{
											m++;
										}
									}else{
										
										m--;
									}
								}
							}else if(Integer.parseInt(str[0])==hashMapMarkerNamesIDs.get(strMarker2)){
								String pos=str[3];
								String map=str[1];
								String chr=str[2];
								m++;
								if(m!=markersListH.size()){
									String[] str1=markersListH.get(m).toString().split("!~!");
									if(Integer.parseInt(str1[0])==hashMapMarkerNamesIDs.get(strMarker1)){
										if(str1[1].equalsIgnoreCase(map)&&(str1[2].equalsIgnoreCase(chr))){									
											dataExists="Yes";
											ch.add(str1[1]+"!~!"+str1[2]+"!~!"+pos+"!~!"+str1[3]);
												
										}else{
											m++;
										}
									}else{								
										m--;
									}
								}
							}
						}
					
					if(ch.isEmpty()){
						//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
						_mainHomePage.getMainWindow().getWindow().showNotification("No information exists for the provided markers", Notification.TYPE_ERROR_MESSAGE);
					}else{					
						//genoManager.getMapInfoByMapChromosomeAndPosition(arg0, arg1, arg2)
						mapInfoMarkersList=new ArrayList();
						ArrayList mapInfoMarkersList1=new ArrayList();
						hashMapMapIdsNames=new HashMap<Integer, String>();
						hashMapMarkerIdsNames=new HashMap<Integer, String>();
						hashMapMarkerInfo=new HashMap<Integer, String>();
						String marker_IDs="";
						List mapsList=new ArrayList();
						for(int i=0;i<ch.size();i++){
							String[] params=ch.get(i).toString().split("!~!");
							float sPos=Float.parseFloat(params[2].toString());
							float ePos=Float.parseFloat(params[3].toString());
							
							double startPos=0; double endPos=0;
							mapsList.add(params[0]);
							
							if(sPos > ePos){
								startPos=ePos;
								endPos=sPos;
							}else if(sPos < ePos){
								startPos=sPos;
								endPos=ePos;
							}else if(sPos == ePos){
								startPos=sPos;
								endPos=ePos;
							}
							System.out.println("@@@@@@@@@@@@@@@@@  :"+genoManager.getMarkerOnMaps(mapsList, params[1], startPos, endPos));
							List<MarkerOnMap> markersOnMap1 =genoManager.getMarkerOnMaps(mapsList, params[1], startPos, endPos);
							for(MarkerOnMap res:markersOnMap1){
								markerIdsList.add(res.getMarkerId());
								marker_IDs=marker_IDs+res.getMarkerId()+",";
								mapInfoMarkersList.add(res.getMarkerId()+"!~!"+res.getMapId()+"!~!"+res.getLinkageGroup()+"!~!"+res.getStartPosition());	
							}
							//System.out.println(i+":"+mapInfoMarkersList1);
							/*String strQuerry="SELECT marker_id, map_id, linkage_group, start_position from gdms_markers_onmap where map_id="+params[0]+" and linkage_group='"+params[1]+"' and start_position between "+startPos+" and "+endPos+" order by map_id, Linkage_group, start_position";			
							
							//System.out.println(strQuerry);						
							
							List mapInfoFromLocal=new ArrayList();	
							List mapInfoFromCentral=new ArrayList();
								
							
							Object obj1=null;
							Object obj1L=null;
							Iterator itList1C=null;
							Iterator itList1L=null;						
							
							
							SQLQuery query1C=centralSession.createSQLQuery(strQuerry);
							query1C.addScalar("marker_id",Hibernate.INTEGER);	
							query1C.addScalar("map_id",Hibernate.INTEGER);	
							query1C.addScalar("linkage_group",Hibernate.STRING);	
							query1C.addScalar("start_position",Hibernate.DOUBLE);						
							mapInfoFromCentral=query1C.list();					
							if(mapInfoFromCentral.size()!=0){
								itListL=mapInfoFromCentral.iterator();	
								for(int wl=0;wl<mapInfoFromCentral.size();wl++){
									Object[] strMareO= (Object[])mapInfoFromCentral.get(wl);
									markerIdsList.add(strMareO[0]);
									marker_IDs=marker_IDs+strMareO[0]+",";
									mapInfoMarkersList.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]);							
								}
							}
							
							
							SQLQuery query1L=localSession.createSQLQuery(strQuerry);
							query1L.addScalar("marker_id",Hibernate.INTEGER);	
							query1L.addScalar("map_id",Hibernate.INTEGER);	
							query1L.addScalar("linkage_group",Hibernate.STRING);	
							query1L.addScalar("start_position",Hibernate.DOUBLE);						
							mapInfoFromLocal=query1L.list();					
							if(mapInfoFromLocal.size()!=0){
								itListL=mapInfoFromLocal.iterator();	
								for(int wl=0;wl<mapInfoFromLocal.size();wl++){
									Object[] strMareO= (Object[])mapInfoFromLocal.get(wl);
									markerIdsList.add(strMareO[0]);
									marker_IDs=marker_IDs+strMareO[0]+",";
									mapInfoMarkersList.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]);							
								}
							}	*/					
						}
						marker_IDs=marker_IDs.substring(0, marker_IDs.length()-1);
						//genoManager.getallM
						String mapIds="";
						for(int map=0;map<maps.size();map++){
							mapIds=mapIds+maps.get(map)+",";
						}
						
						mapIds=mapIds.substring(0, mapIds.length()-1);
						for(int map=0;map<maps.size();map++){
							List<MapInfo> results =genoManager.getMapInfoByMapName(maps.get(map).toString());
							for(MapInfo rMInfo:results){
								hashMapMapIdsNames.put(rMInfo.getMapId(), rMInfo.getMapName());
							}
						}
						/*String strQuerry2="SELECT map_id, map_name from gdms_map where map_id in("+mapIds+")";			
						//System.out.println(strQuerry);						
						
						List mapsFromLocal=new ArrayList();	
						List mapsFromCentral=new ArrayList();
							
						List markersListH=new ArrayList();
						List nIDs=new ArrayList();
						List gidsList=new ArrayList();
						Object strObj1=null;
						Object strObj1L=null;
						Iterator it1C=null;
						Iterator it1L=null;	
						
						
						
						SQLQuery query2C=centralSession.createSQLQuery(strQuerry2);					
						query2C.addScalar("map_id",Hibernate.INTEGER);	
						query2C.addScalar("map_name",Hibernate.STRING);										
						mapsFromCentral=query2C.list();					
						if(mapsFromCentral.size()!=0){
							it1C=mapsFromCentral.iterator();	
							for(int wl=0;wl<mapsFromCentral.size();wl++){
								Object[] strMareO= (Object[])mapsFromCentral.get(wl);
								
								hashMapMapIdsNames.put(Integer.parseInt(strMareO[0].toString()), strMareO[1].toString());
							}
						}
						
						
						SQLQuery query2L=localSession.createSQLQuery(strQuerry2);				
						query2L.addScalar("map_id",Hibernate.INTEGER);	
						query2L.addScalar("map_name",Hibernate.STRING);	
											
						mapsFromLocal=query2L.list();					
						if(mapsFromLocal.size()!=0){
							it1L=mapsFromLocal.iterator();	
							for(int wl=0;wl<mapsFromLocal.size();wl++){
								Object[] strMareO= (Object[])mapsFromLocal.get(wl);							
								hashMapMapIdsNames.put(Integer.parseInt(strMareO[0].toString()), strMareO[1].toString());
							}
						}				
						*/
						List<MarkerIdMarkerNameElement> resMarkers=genoManager.getMarkerNamesByMarkerIds(markerIdsList);
						for(MarkerIdMarkerNameElement res:resMarkers){
							hashMapMarkerIdsNames.put(res.getMarkerId(), res.getMarkerName());
						}
						
						//List<MarkerInfo> markerInfo=genoManager.getMarkerInfoByMarkerIds(markerIdsList);
						try{
							localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
							centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
						}catch (Exception e){
							e.printStackTrace();
						}
						
						List<MarkerInfo> results=genoManager.getMarkerInfoByMarkerIds(markerIdsList);
						for(MarkerInfo resM:results){
							//String value=strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11];
							String value=resM.getMarkerType()+"!~!"+resM.getSpecies()+"!~!"+resM.getPloidy()+"!~!"+resM.getMotif()+"!~!"+resM.getForwardPrimer()+"!~!"+resM.getReversePrimer()+"!~!"+resM.getAnnealingTemp()+"!~!"+resM.getPrincipalInvestigator()+"!~!"+resM.getContact()+"!~!"+resM.getInstitute();
							hashMapMarkerInfo.put(resM.getMarkerId(), value);
						}
						
						/*
						String strQuerryLG="select * from gdms_marker_retrieval_info where marker_id in("+marker_IDs+")";	
						
						System.out.println(strQuerryLG);			
						List markerFromLocal=new ArrayList();		
						ArrayList markersDetailsList=new ArrayList();		
						List markerFromCentral=new ArrayList();
					
						
						SQLQuery queryL_LG=localSession.createSQLQuery(strQuerryLG);		
						queryL_LG.addScalar("marker_id",Hibernate.INTEGER);
						queryL_LG.addScalar("marker_name",Hibernate.STRING);
						queryL_LG.addScalar("marker_type",Hibernate.STRING);
						queryL_LG.addScalar("species",Hibernate.STRING);
						queryL_LG.addScalar("ploidy",Hibernate.STRING);
						queryL_LG.addScalar("motif",Hibernate.STRING);
						queryL_LG.addScalar("forward_primer",Hibernate.STRING);
						queryL_LG.addScalar("reverse_primer",Hibernate.STRING);
						queryL_LG.addScalar("annealing_temp",Hibernate.FLOAT);
						queryL_LG.addScalar("principal_investigator",Hibernate.STRING);
						queryL_LG.addScalar("contact",Hibernate.STRING);
						queryL_LG.addScalar("institute",Hibernate.STRING);
						
						markerFromLocal=queryL_LG.list();
						System.out.println("markerFromLocal:"+markerFromLocal);
						for(int w=0;w<markerFromLocal.size();w++){
							Object[] strMareO= (Object[])markerFromLocal.get(w);
							//if(! markerIdsByQtl.contains(Integer.parseInt(strMareO[0].toString()))){
								//markerIdsByQtl.add(Integer.parseInt(strMareO[0].toString()));
								//markersDetailsList.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11]);
							//}
								String value=strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11];
								hashMapMarkerInfo.put(Integer.parseInt(strMareO[0].toString()), value);
						}	
							
							
						SQLQuery queryC_LG=centralSession.createSQLQuery(strQuerryLG);		
						queryC_LG.addScalar("marker_id",Hibernate.INTEGER);
						queryC_LG.addScalar("marker_name",Hibernate.STRING);
						queryC_LG.addScalar("marker_type",Hibernate.STRING);
						queryC_LG.addScalar("species",Hibernate.STRING);
						queryC_LG.addScalar("ploidy",Hibernate.STRING);
						queryC_LG.addScalar("motif",Hibernate.STRING);
						queryC_LG.addScalar("forward_primer",Hibernate.STRING);
						queryC_LG.addScalar("reverse_primer",Hibernate.STRING);
						queryC_LG.addScalar("annealing_temp",Hibernate.FLOAT);
						queryC_LG.addScalar("principal_investigator",Hibernate.STRING);
						queryC_LG.addScalar("contact",Hibernate.STRING);
						queryC_LG.addScalar("institute",Hibernate.STRING);
						markerFromCentral=queryC_LG.list();			
						for(int w=0;w<markerFromCentral.size();w++){
							Object[] strMareO= (Object[])markerFromCentral.get(w);
							if(! markerIdsByQtl.contains(Integer.parseInt(strMareO[0].toString()))){
								markerIdsByQtl.add(Integer.parseInt(strMareO[0].toString()));	
								markersDetailsList.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11]);
							}
							
							String value=strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11];
							hashMapMarkerInfo.put(Integer.parseInt(strMareO[0].toString()), value);
							
						}*/
						/*for(MarkerInfo res: markerInfo){
							String value=res.getMarkerType()+"!~!"+res.getSpecies()+"!~!"+res.getPloidy()+"!~!"+res.getMotif()+"!~!"+res.getForwardPrimer()+"!~!"+res.getReversePrimer()+"!~!"+res.getAnnealingTemp()+"!~!"+res.getPrincipalInvestigator()+"!~!"+res.getContact()+"!~!"+res.getInstitute();
							hashMapMarkerInfo.put(res.getMarkerId(), value);
							
						}*/
					}
					//System.out.println("markerInfo=:"+markerInfo);
				} catch (MiddlewareQueryException e) {					
					//_mainHomePage.getMainWindow().getWindow().showNotification("No information exists for the provided markers", Notification.TYPE_ERROR_MESSAGE);					
					//	return;
				}
				//System.out.println("dataExists=:"+dataExists);
				Component newRegionResultsPanel = buildResultsComponent();
				if(dataExists.equalsIgnoreCase("yes")){
					_tabsheetForHaps.replaceComponent(buildResultsComponent, newRegionResultsPanel);
					_tabsheetForHaps.requestRepaint();
					newRegionResultsPanel = newRegionResultsPanel;
					_tabsheetForHaps.getTab(1).setEnabled(true);
					_tabsheetForHaps.setSelectedTab(1);
				}/*else{
					_mainHomePage.getMainWindow().getWindow().showNotification("Couldnot retrieve region between the selected markers", Notification.TYPE_ERROR_MESSAGE);
				}*/
				
			}
			
		});
		
		
		selectMarkersLayout.addComponent(horiMarkersLayout);
		selectMarkersLayout.addComponent(layoutForButton);
		selectMarkersLayout.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);
		selectMarkersLayout.setMargin(true);
		selectMainLayout.addComponent(selectMarkersLayout);
		selectMainLayout.setComponentAlignment(selectMarkersLayout, Alignment.MIDDLE_CENTER);
		return selectMainLayout;
	}
	
	
	
	public void componentEvent(Event event) {
		// TODO Auto-generated method stub
		event.getComponent().requestRepaint();
	}
	
	public List<String> retrieveMarkers() throws MiddlewareQueryException {
		
		List<String> markerNames =new ArrayList<String>();
		/*List<MarkerIdMarkerNameElement> listToReturn = new ArrayList<MarkerIdMarkerNameElement>();
		String strHapQuerry="SELECT DISTINCT marker_id FROM gdms_markers_onmap";			
		//System.out.println(strHapQuerry);	

		List markerNamesList=new ArrayList();
		
		List markerFromLocal=new ArrayList();	
		List markerFromCentral=new ArrayList();
		List markersList=new ArrayList();	
		List markersListH=new ArrayList();
		List nIDs=new ArrayList();
		List gidsList=new ArrayList();
		Object obj=null;
		Object objL=null;
		Iterator itListC=null;
		Iterator itListL=null;	
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		
		SQLQuery queryC=centralSession.createSQLQuery(strHapQuerry);		
		queryC.addScalar("marker_id",Hibernate.INTEGER);	  
		
		markerFromCentral=queryC.list();
		itListL=markerFromCentral.iterator();			
		while(itListL.hasNext()){
			objL=itListL.next();
			if(objL!=null){
				if(! markersListH.contains(objL.toString())){
					markersListH.add(Integer.parseInt(objL.toString()));	
					//markers=markers+objL.toString()+",";
				}
			}
		}
		
		SQLQuery queryL=localSession.createSQLQuery(strHapQuerry);		
		queryL.addScalar("marker_id",Hibernate.INTEGER);	  
		
		markerFromLocal=queryL.list();
		itListL=markerFromLocal.iterator();			
		while(itListL.hasNext()){
			objL=itListL.next();
			if(objL!=null){
				if(! markersListH.contains(objL.toString())){
					markersListH.add(Integer.parseInt(objL.toString()));	
					//markers=markers+objL.toString()+",";
				}
			}
		}
	
		listToReturn = genoManager.getMarkerNamesByMarkerIds(markersListH);*/		
		markerNames =genoManager.getAllMarkerNamesFromMarkersOnMap();
		
		return markerNames;
	}
	
	public List<MarkerIdMarkerNameElement> retrieveMarkersForSecondList(String strMarkerSelected) throws MiddlewareQueryException {
		
		ArrayList markerIdsList=new ArrayList();
	
		List<String> mNamesList= new ArrayList<String>();
		List<Marker> marker1=new ArrayList();
		int markerId=0;
		mNamesList.add(strMarkerSelected);
		List<MarkerIdMarkerNameElement> listToReturn = new ArrayList<MarkerIdMarkerNameElement>();
		
		marker1=genoManager.getMarkersByMarkerNames(mNamesList, 0, mNamesList.size(), Database.CENTRAL);
		if(!marker1.isEmpty()){
			for(Marker m1:marker1){
				markerId=m1.getMarkerId();
			}
		}else{
			marker1=genoManager.getMarkersByMarkerNames(mNamesList, 0, mNamesList.size(), Database.LOCAL);
			for(Marker m1:marker1){
				markerId=m1.getMarkerId();
			}		
		}
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		//System.out.println("markerId=:"+markerId);
		markerIdsList.add(markerId);
		List markerNamesList=new ArrayList();
		
		List mapDetailsC=new ArrayList();
		List mapDetailsL=new ArrayList();
		List finalMapDetails=new ArrayList();
		List mDetailsL=new ArrayList();
		List mDetailsC=new ArrayList();
		
		List markerFromLocal=new ArrayList();	
		List markerFromCentral=new ArrayList();
		List markersList=new ArrayList();	
		List markersListH=new ArrayList();
		
		Object obj=null;
		Object objL=null;
		Iterator itListC=null;
		Iterator itListL=null;
		Iterator itList2C=null;
		Iterator itList2L=null;	
		
		List<MarkerOnMap> markersOnMap = genoManager.getMarkersOnMapByMarkerIds(markerIdsList);
		for(MarkerOnMap resMap:markersOnMap){
			if(! finalMapDetails.contains(resMap.getMapId()+"!~!"+resMap.getLinkageGroup())){
				finalMapDetails.add(resMap.getMapId()+"!~!"+resMap.getLinkageGroup());				
			}
		}
		
		
		/*
		//
		String str2MarkerQuerry1="SELECT * FROM gdms_markers_onmap WHERE marker_id="+markerId;
		SQLQuery queryC=centralSession.createSQLQuery(str2MarkerQuerry1);		
		queryC.addScalar("map_id",Hibernate.INTEGER);	 
		queryC.addScalar("linkage_group",Hibernate.STRING);
		mDetailsC=queryC.list();
		//System.out.println("mapDetailsC.size()=:"+mDetailsC.size());
		if(mDetailsC.size()!=0){
			for(int wc=0;wc<mDetailsC.size();wc++){				
				Object[] strMareOC= (Object[])mDetailsC.get(wc);
			   // System.out.println("W=....."+w+"    "+strMareOC[0]+"   "+strMareOC[1]);
				if(! mapDetailsC.contains(strMareOC[0]+"!~!"+strMareOC[1])){
					mapDetailsC.add(strMareOC[0]+"!~!"+strMareOC[1]);				
				}			
			}
		}
		SQLQuery queryL=localSession.createSQLQuery(str2MarkerQuerry1);		
		queryL.addScalar("map_id",Hibernate.INTEGER);	 
		queryL.addScalar("linkage_group",Hibernate.STRING);
		mDetailsL=queryL.list();
		if(mDetailsL.size()!=0){
			itListL=mDetailsL.iterator();			
			for(int wl=0;wl<mDetailsL.size();wl++){
				Object[] strMareO= (Object[])mDetailsL.get(wl);
			    //System.out.println("W=....."+wl+"    "+strMareO[0]+"   "+strMareO[1]);
				if(! mapDetailsL.contains(strMareO[0]+"!~!"+strMareO[1])){
					mapDetailsL.add(strMareO[0]+"!~!"+strMareO[1].toString());				
				}
				//System.out.println("%%%%%%%%%%%%%%%  :"+mapDetailsL);
			}
		}
		
		if(mapDetailsC.size()>0){
			for(int mc=0;mc<mapDetailsC.size();mc++){
				if(!finalMapDetails.contains(mapDetailsC.get(mc)))
					finalMapDetails.add(mapDetailsC.get(mc));
			}
		}
		if(mapDetailsL.size()>0){
			for(int ml=0;ml<mapDetailsL.size();ml++){
				if(!finalMapDetails.contains(mapDetailsL.get(ml)))
					finalMapDetails.add(mapDetailsL.get(ml));
			}
		}*/
		for(int f=0;f<finalMapDetails.size();f++){
			String[] strAgr=finalMapDetails.get(f).toString().split("!~!");
			str2MarkerQuerry2="SELECT * FROM gdms_markers_onmap WHERE map_id="+strAgr[0]+" AND linkage_group='"+strAgr[1]+"' AND marker_id !="+markerId;
			SQLQuery query2C=centralSession.createSQLQuery(str2MarkerQuerry2);		
			query2C.addScalar("marker_id",Hibernate.INTEGER);	  
			
			markerFromCentral=query2C.list();
			itList2C=markerFromCentral.iterator();			
			while(itList2C.hasNext()){
				obj=itList2C.next();
				if(obj!=null){
					if(! markersListH.contains(Integer.parseInt(obj.toString()))){
						markersListH.add(Integer.parseInt(obj.toString()));	
						//markers=markers+objL.toString()+",";
					}
				}
			}
			
			SQLQuery query2L=localSession.createSQLQuery(str2MarkerQuerry2);		
			query2L.addScalar("marker_id",Hibernate.INTEGER);	  
			
			markerFromLocal=query2L.list();
			itList2L=markerFromLocal.iterator();			
			while(itList2L.hasNext()){
				objL=itList2L.next();
				if(objL!=null){
					if(! markersListH.contains(Integer.parseInt(objL.toString()))){
						markersListH.add(Integer.parseInt(objL.toString()));	
						//markers=markers+objL.toString()+",";
					}
				}
			}
			
		}
		
	
		listToReturn = genoManager.getMarkerNamesByMarkerIds(markersListH);
		
		
		return listToReturn;
	}
	
	
	
}
