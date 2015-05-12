package org.icrisat.gdms.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jxl.write.WriteException;

import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.dao.gdms.QtlDetailsDAO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.gdms.MarkerInfo;
import org.generationcp.middleware.pojos.gdms.Mta;
import org.generationcp.middleware.pojos.gdms.QtlDataElement;
import org.generationcp.middleware.pojos.gdms.QtlDetailElement;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.retrieve.RetrieveQTL;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;


public class RetrieveTraitInformationComponent implements Component.Listener {

	private static final long serialVersionUID = 1L;

	private TabSheet _tabsheetForTrait;
	private Component buildTraitResultsComponent;
	private GDMSMain _mainHomePage;
	//private List<Integer> listOfQTLIdsByTrait;
	private List<QtlDetailElement> listOfQTLDetailsByQTLIDs;

	private List emptyList=new ArrayList();
	private Table _qtlTable;
	
	private Table _mtaTable;
	
	String marker_IDs="";
	
	private Session centralSession;
	private Session localSession;
	
	private Session sessionL;
	private Session sessionC;
	List markersList=new ArrayList();
	List mapsList=new ArrayList();
	int mapId=0;
	String mapName="";
	String strMsg="";
	
	private Table _tableWithAllTraits;
	ManagerFactory factory=null;
	OntologyDataManager om;
	GenotypicDataManager genoManager;
	Button btnNext;
	private HashMap<Integer, String> hmOfMarkerIDAndNames;
	
	private HashMap<Integer, String> hmOfMapIDAndNames;
	private HashMap<Integer, String> hmOfTraitIDAndTraitNames;
	
	List<Mta> finalListOfMappingData= new ArrayList<Mta>();
	
	public RetrieveTraitInformationComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			genoManager=factory.getGenotypicDataManager();
			om=factory.getOntologyDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Building the entire Tabbed Component required for Trait data
	 * 
	 */
	public HorizontalLayout buildTabbedComponentForTrait() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();

		_tabsheetForTrait = new TabSheet();
		_tabsheetForTrait.setWidth("700px");

		Component buildTraitSearchComponent = buildMapSearchComponent();

		buildTraitResultsComponent = buildTraitResultsComponent();
		
		buildTraitSearchComponent.setSizeFull();
		buildTraitResultsComponent.setSizeFull();
		

		_tabsheetForTrait.addComponent(buildTraitSearchComponent);
		_tabsheetForTrait.addComponent(buildTraitResultsComponent);
		
		_tabsheetForTrait.getTab(1).setEnabled(false);

		horizontalLayout.addComponent(_tabsheetForTrait);

		return horizontalLayout;
	}


	private Component buildMapSearchComponent() {
		VerticalLayout searchTraitsLayout = new VerticalLayout();
		searchTraitsLayout.setCaption("Search");
		searchTraitsLayout.setMargin(true, true, true, true);
		searchTraitsLayout.setSpacing(true);

		Label lblSearch = new Label("Search Traits");
		lblSearch.setStyleName(Reindeer.LABEL_H2);
		searchTraitsLayout.addComponent(lblSearch);
		searchTraitsLayout.setComponentAlignment(lblSearch, Alignment.TOP_CENTER);

		Label lblMAPNames = new Label("Trait Names");
		lblMAPNames.setStyleName(Reindeer.LABEL_SMALL);

		final TextField txtFieldSearch = new TextField();
		txtFieldSearch.setWidth("500px");
		txtFieldSearch.setNullRepresentation("");

		ThemeResource themeResource = new ThemeResource("images/find-icon.png");
		Button searchButton = new Button();
		searchButton.setIcon(themeResource);

		//final GridLayout gridLayout = new GridLayout();
		final HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		
		searchButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				String strSearchString = txtFieldSearch.getValue().toString();
				
				if (strSearchString.trim().equals("")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a search string.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (false == strSearchString.endsWith("*")){
					if (false == strSearchString.equals(""))
						strSearchString = strSearchString + "*";
				}
				if(strSearchString.equals("*")) {
					/*buildOnLoad(gridLayout, strSearchString);
					gridLayout.requestRepaint();*/
					buildOnLoad(horizontalLayout, strSearchString);
					horizontalLayout.requestRepaint();
					txtFieldSearch.setValue("");
				} else if(strSearchString.endsWith("*")) {
					strSearchString = strSearchString.substring(0, strSearchString.length() - 1);
					/*buildOnLoad(gridLayout, strSearchString);
					gridLayout.requestRepaint();*/
					buildOnLoad(horizontalLayout, strSearchString);
					horizontalLayout.requestRepaint();
					txtFieldSearch.setValue("");
				} else {
					/*buildOnLoad(gridLayout, strSearchString);
					gridLayout.requestRepaint();*/
					buildOnLoad(horizontalLayout, strSearchString);
					horizontalLayout.requestRepaint();
					txtFieldSearch.setValue("");
				}
				
				if (null == _tableWithAllTraits){
					_mainHomePage.getMainWindow().getWindow().showNotification("There are no Traits to be displayed.", Notification.TYPE_ERROR_MESSAGE);
					btnNext.setEnabled(false);
					return;
				}
				
				if (null != _tableWithAllTraits && 0 == _tableWithAllTraits.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("There are no Traits to be displayed.", Notification.TYPE_ERROR_MESSAGE);
					btnNext.setEnabled(false);
					return;
				}
			}
		});
		
		
		HorizontalLayout layoutForTextSearch = new HorizontalLayout();
		layoutForTextSearch.setSpacing(true);
		layoutForTextSearch.addComponent(lblMAPNames);
		layoutForTextSearch.addComponent(txtFieldSearch);
		layoutForTextSearch.addComponent(searchButton);
		searchTraitsLayout.addComponent(layoutForTextSearch);
		searchTraitsLayout.setMargin(true, true, true, true);

		VerticalLayout layoutForButton = new VerticalLayout();
		btnNext = new Button("Next");
		btnNext.setEnabled(false);
		layoutForButton.addComponent(btnNext);
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {

			/**
			 * http://www.cropontology.org/terms/CO_337:0000059/Harvest%20index
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				listOfQTLDetailsByQTLIDs = new ArrayList<QtlDetailElement>();
				String strTraitName = txtFieldSearch.getValue().toString();
				List<String> listOfTraitNames = new ArrayList<String>();

				if(false == strTraitName.equals("*") && false == strTraitName.endsWith("*")) {
					
					int iNumOfQTLs = _tableWithAllTraits.size();
					for (int i = 0; i < iNumOfQTLs; i++) {
						Item item = _tableWithAllTraits.getItem(new Integer(i));
						Property itemProperty = item.getItemProperty("Select");
						CheckBox checkBox = (CheckBox) itemProperty.getValue();
						if (checkBox.booleanValue() == true) {
							//String strSelectedQTL = item.getItemProperty("QTL Name").toString();
							String strSelectedQTL = item.getItemProperty("Trait Name").toString();
							listOfTraitNames.add(strSelectedQTL);
						}
					}
				}
				//System.out.println("listOfTraitNames=:"+listOfTraitNames);
					try {
						if (null != strTraitName && (false == strTraitName.equals(""))){
							//System.out.println("IF LOOP ............");
							if (strTraitName.equals("*")){
								getAllTraitDetails();
							} else if(strTraitName.endsWith("*")) {
								getAllTraitDetailsStartsWith(strTraitName);
							} else {
								for (String string : listOfTraitNames) {
									if(false == string.equals("*") && false == string.endsWith("*")) {
										RetrieveQTL retrieveQTL = new RetrieveQTL();
										
										
										List<QtlDetailElement> retrieveTrait = retrieveQTL.retrieveTrait(string);
										if(null != retrieveTrait) {
											listOfQTLDetailsByQTLIDs.addAll(retrieveTrait);
										}
									}
								}
							}

						} else {
							//System.out.println("ELSE LOOP *********************");
							try{			
								localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
								centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();				
							}catch (Exception e){
								e.printStackTrace();
							}
							int cvtermId=0;
							for (String strTrait : listOfTraitNames) {
								if(false == strTrait.equals("*") && false == strTrait.endsWith("*")) {
																	
									RetrieveQTL retrieveQTL = new RetrieveQTL();
									List<QtlDetailElement> retrieveTrait = retrieveQTL.retrieveTrait(strTrait);
									//System.out.println("%%%%%%%%%%%%   :"+retrieveTrait);
									if(null != retrieveTrait) {
										listOfQTLDetailsByQTLIDs.addAll(retrieveTrait);
									}
									
									/*List<Mta> retrieveMTAs = retrieveQTL.retrieveMTA(strTrait);
									System.out.println("retrieveMTAs=:"+retrieveMTAs);*/
									hmOfTraitIDAndTraitNames=new HashMap<Integer, String>();
									StandardVariable stdVariable = new StandardVariable();
									Set<StandardVariable> standardVariables = om.findStandardVariablesByNameOrSynonym(strTrait);
									System.out.println("standardVariables:"+standardVariables);
									for (StandardVariable stdVar : standardVariables) {
										cvtermId=stdVar.getId();	
										hmOfTraitIDAndTraitNames.put(cvtermId, stdVar.getName());
									}
									
									//String strQuerry="SELECT distinct gdms_markers_onmap.marker_id, gdms_map.map_name, gdms_markers_onmap.start_position, gdms_markers_onmap.linkage_group, gdms_map.map_unit FROM gdms_map join gdms_markers_onmap on gdms_map.map_id=gdms_markers_onmap.map_id where gdms_markers_onmap.marker_id in ("+marker_ids.substring(0, marker_ids.length()-1)+") and gdms_map.map_id="+mapID+" order BY gdms_map.map_name, gdms_markers_onmap.linkage_group, gdms_markers_onmap.start_position asc";
									String strQuerry="select * from gdms_mta where tid="+cvtermId;
									finalListOfMappingData = new ArrayList<Mta>();
									Mta mtaPOJO;
									
									System.out.println(strQuerry);
									//listOfMapData = new ArrayList<String>();
									markersList=new ArrayList();
									mapsList=new ArrayList();
									List newListL=new ArrayList();
									List newListC=new ArrayList();
									//try {	
									Object obj=null;
									Object objL=null;
									Iterator itListC=null;
									Iterator itListL=null;
									
									//sessionC=centralSession.getSessionFactory().getCurrentSession();		
									//centralSession.getSessionFactory().getCurrentSession();
									SQLQuery queryC=centralSession.createSQLQuery(strQuerry);		
									queryC.addScalar("marker_id",Hibernate.INTEGER);
									queryC.addScalar("map_id",Hibernate.INTEGER);	
									queryC.addScalar("chromosome",Hibernate.STRING);
									queryC.addScalar("position",Hibernate.DOUBLE);									
									queryC.addScalar("hv_allele",Hibernate.STRING);
									queryC.addScalar("experiment",Hibernate.STRING);	
									queryC.addScalar("effect",Hibernate.STRING);
									queryC.addScalar("score_value",Hibernate.DOUBLE);									
									queryC.addScalar("r_square",Hibernate.STRING);
									queryC.addScalar("tid",Hibernate.INTEGER);
									newListC=queryC.list();	
									System.out.println("newListC=:"+newListC);
									itListC=newListC.iterator();			
									while(itListC.hasNext()){
										obj=itListC.next();
										if(obj!=null){		
											Object[] strMareO= (Object[])obj;
											if(!markersList.contains(Integer.parseInt(strMareO[0].toString()))){
												markersList.add(Integer.parseInt(strMareO[0].toString()));
											}
											if(!mapsList.contains(Integer.parseInt(strMareO[1].toString()))){
												mapsList.add(Integer.parseInt(strMareO[1].toString()));
											}
											//listOfMapData.add(obj.toString());	
											mtaPOJO = new Mta();
											mtaPOJO.setMapId(Integer.parseInt(strMareO[1].toString()));
											mtaPOJO.setMarkerId(Integer.parseInt(strMareO[0].toString()));
											mtaPOJO.setChromosome(strMareO[2].toString());
											mtaPOJO.setPosition(Float.parseFloat(strMareO[3].toString()));
											/*mtaPOJO.setExperiment(strMareO[5].toString());
											mtaPOJO.setHvAllele(strMareO[4].toString());
											mtaPOJO.setEffect(Integer.parseInt(strMareO[6].toString()));*/
											mtaPOJO.setScoreValue(Float.parseFloat(strMareO[7].toString()));
											mtaPOJO.setrSquare(Float.parseFloat(strMareO[8].toString()));
											mtaPOJO.settId(Integer.parseInt(strMareO[9].toString()));
											
											
											finalListOfMappingData.add(mtaPOJO);
											//listOfMapData.add(hashMapOfMarkerIDsAndNames.get(Integer.parseInt(strMareO[0].toString())).toString()+"!~!"+strMareO[1].toString()+"!~!"+strMareO[3].toString()+"!~!"+Float.parseFloat(strMareO[2].toString())+"!~!"+strMareO[0].toString());
										}
										//listOfAllMappingData.
										
									}
									
									
									//sessionL=localSession.getSessionFactory().getCurrentSession();			
									SQLQuery queryL=localSession.createSQLQuery(strQuerry);		
									queryL.addScalar("marker_id",Hibernate.INTEGER);
									queryL.addScalar("map_id",Hibernate.INTEGER);	
									queryL.addScalar("chromosome",Hibernate.STRING);
									queryL.addScalar("position",Hibernate.DOUBLE);									
									queryL.addScalar("hv_allele",Hibernate.STRING);
									queryL.addScalar("experiment",Hibernate.STRING);	
									queryL.addScalar("effect",Hibernate.STRING);
									queryL.addScalar("score_value",Hibernate.DOUBLE);									
									queryL.addScalar("r_square",Hibernate.STRING);
									queryL.addScalar("tid",Hibernate.INTEGER);
									newListL=queryL.list();		
									System.out.println("newListL=:"+newListL);
									itListL=newListL.iterator();			
									while(itListL.hasNext()){
										objL=itListL.next();
										if(objL!=null){		
											Object[] strMareO= (Object[])objL;
											if(!markersList.contains(Integer.parseInt(strMareO[0].toString()))){
												markersList.add(Integer.parseInt(strMareO[0].toString()));
											}
											if(!mapsList.contains(Integer.parseInt(strMareO[1].toString()))){
												mapsList.add(Integer.parseInt(strMareO[1].toString()));
											}
											//listOfMapData.add(obj.toString());	
											mtaPOJO = new Mta();
											mtaPOJO.setMapId(Integer.parseInt(strMareO[1].toString()));
											mtaPOJO.setMarkerId(Integer.parseInt(strMareO[0].toString()));
											mtaPOJO.setChromosome(strMareO[2].toString());
											mtaPOJO.setPosition(Float.parseFloat(strMareO[3].toString()));
											/*mtaPOJO.setExperiment(strMareO[5].toString());
											mtaPOJO.setHvAllele(strMareO[4].toString());
											mtaPOJO.setEffect(Integer.parseInt(strMareO[6].toString()));*/
											mtaPOJO.setScoreValue(Float.parseFloat(strMareO[7].toString()));
											mtaPOJO.setrSquare(Float.parseFloat(strMareO[8].toString()));
											mtaPOJO.settId(Integer.parseInt(strMareO[9].toString()));
											
											
											finalListOfMappingData.add(mtaPOJO);
											//listOfMapData.add(hashMapOfMarkerIDsAndNames.get(Integer.parseInt(strMareO[0].toString())).toString()+"!~!"+strMareO[1].toString()+"!~!"+strMareO[3].toString()+"!~!"+Float.parseFloat(strMareO[2].toString())+"!~!"+strMareO[0].toString());
										}
									
									
									
								}
								}
							}
							System.out.println(finalListOfMappingData);
							hmOfMapIDAndNames=new HashMap<Integer, String>();
							//genoManager.getMap
							System.out.println("mapsList=:"+mapsList);
							for(int m=0;m<mapsList.size();m++){
								String strQuerry1="select * from gdms_map where map_id="+mapsList.get(m);
								System.out.println(strQuerry1);
								List nListL=new ArrayList();
								List nListC=new ArrayList();
								//try {	
								Object obj=null;
								Object objL=null;
								Iterator itListC=null;
								Iterator itListL=null;
								SQLQuery queryC=centralSession.createSQLQuery(strQuerry1);									
								queryC.addScalar("map_id",Hibernate.INTEGER);	
								queryC.addScalar("map_name",Hibernate.STRING);
								nListC=queryC.list();		
								System.out.println("newListC=:"+nListC);
								itListC=nListC.iterator();			
								while(itListC.hasNext()){
									obj=itListC.next();
									if(obj!=null){		
										Object[] strMareO= (Object[])obj;
										System.out.println("$#@#!@#!@  :"+strMareO[0]+strMareO[1]);
										hmOfMapIDAndNames.put(Integer.parseInt(strMareO[0].toString()), strMareO[1].toString());
									}
								}
								
								
								SQLQuery queryL=localSession.createSQLQuery(strQuerry1);	
								queryL.addScalar("map_id",Hibernate.INTEGER);
								queryL.addScalar("map_name",Hibernate.STRING);
								
								
								nListL=queryL.list();		
								System.out.println("newListL=:"+nListL);
								itListL=nListL.iterator();			
								while(itListL.hasNext()){
									objL=itListL.next();
									if(objL!=null){		
										Object[] strMareO= (Object[])objL;
										hmOfMapIDAndNames.put(Integer.parseInt(strMareO[0].toString()), strMareO[1].toString());
									}
								}
								
							}
							hmOfMarkerIDAndNames=new HashMap<Integer, String>();
							List<MarkerIdMarkerNameElement> markerNames= genoManager.getMarkerNamesByMarkerIds(markersList);
							
							for(MarkerIdMarkerNameElement markers:markerNames){
								hmOfMarkerIDAndNames.put(markers.getMarkerId(), markers.getMarkerName());
							}
							
						}

					} catch (MiddlewareQueryException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Trait Details.",  Notification.TYPE_ERROR_MESSAGE);
						return;
					}

					if (0 == listOfQTLDetailsByQTLIDs.size()){
						_mainHomePage.getMainWindow().getWindow().showNotification("No Traits to be displayed.",  Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				Component newTraitResultsPanel = buildTraitResultsComponent();
				_tabsheetForTrait.replaceComponent(buildTraitResultsComponent, newTraitResultsPanel);
				_tabsheetForTrait.requestRepaint();
				buildTraitResultsComponent = newTraitResultsPanel;
				_tabsheetForTrait.getTab(1).setEnabled(true);
				_tabsheetForTrait.setSelectedTab(1);
			}

			private void getAllTraitDetailsStartsWith(String strTraitName)
					throws MiddlewareQueryException {
				RetrieveQTL retrieveQTL = new RetrieveQTL();
				strTraitName = strTraitName.substring(0, strTraitName.length() - 1);
				QtlDetailElement retrieveTraitNameStartWith = retrieveQTL.retrieveTraitNameStartWith(strTraitName);
				if(null != retrieveTraitNameStartWith) {
					String strTrait = "";
					Integer iTraitId = retrieveTraitNameStartWith.getTraitId();
					if (null != iTraitId){
						/*TraitDAO traitDAOLocal = new TraitDAO();
						traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
						String traitFromLocal="";
						try {
							/*traitFromLocal = traitDAOLocal.getByTraitId(iTraitId);
							if (null != traitFromLocal){
								strTrait = traitFromLocal.getAbbreviation();
							} else {
								TraitDAO traitDAOCentral = new TraitDAO();
								traitDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
								Trait traitFromCentral = traitDAOCentral.getByTraitId(iTraitId);
								strTrait = traitFromCentral.getAbbreviation();
							}*/
							
							traitFromLocal=om.getStandardVariable(iTraitId).getName();
							
						} catch (MiddlewareQueryException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					
					//List<QtlDetailElement> retrieveTrait = retrieveQTL.retrieveTrait(retrieveTraitNameStartWith.getTrait());
					List<QtlDetailElement> retrieveTrait = retrieveQTL.retrieveTrait(strTrait);
					if(null != retrieveTrait) {
						listOfQTLDetailsByQTLIDs.addAll(retrieveTrait);
					}
				}
			}

			private void getAllTraitDetails() throws MiddlewareQueryException {
				RetrieveQTL retrieveQTL = new RetrieveQTL();
				List<String> retrieveTraitNames = new ArrayList<String>();
				List<QtlDetailElement> retrieveQTLDetails = retrieveQTL.retrieveQTLDetails();
				for (QtlDetailElement qtlDetailElement : retrieveQTLDetails) {
					//String strTrait = qtlDetailElement.getTrait();
					String strTrait = "";
					Integer iTraitId = qtlDetailElement.getTraitId();
					if (null != iTraitId){
						/*TraitDAO traitDAOLocal = new TraitDAO();
						traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
						String traitFromLocal="";
						try {	
							
							traitFromLocal=om.getStandardVariable(iTraitId).getName();
							
						} catch (MiddlewareQueryException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(null != strTrait && false == retrieveTraitNames.contains(strTrait)) {
						retrieveTraitNames.add(strTrait);
					}
				}
				
				for (String string : retrieveTraitNames) {
					List<QtlDetailElement> retrieveTrait = retrieveQTL.retrieveTrait(string);
					if(null != retrieveTrait) {
						listOfQTLDetailsByQTLIDs.addAll(retrieveTrait);
					}
				}
			}
		});

		//searchTraitsLayout.addComponent(gridLayout);
		searchTraitsLayout.addComponent(horizontalLayout);
		
		searchTraitsLayout.addComponent(layoutForButton);
		searchTraitsLayout.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);

		return searchTraitsLayout;
	}


	private Component buildTraitResultsComponent() {
		VerticalLayout verticalLayout = new VerticalLayout();
		
		VerticalLayout resultsLayout = new VerticalLayout();
		verticalLayout.setCaption("Results");
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true, true, true, true);
		resultsLayout.setMargin(true);
		resultsLayout.setWidth("700px");
		
		int iNumOfTraitssFound = 0;
		int iNumOfMTAsFound=0;
		
		if (null != finalListOfMappingData){
		
			iNumOfMTAsFound=finalListOfMappingData.size();
			if (0 != iNumOfMTAsFound){
				Label lblMAPsFound = new Label(finalListOfMappingData.size() + " MTA(s) Found");
				lblMAPsFound.setStyleName(Reindeer.LABEL_H2);
				resultsLayout.addComponent(lblMAPsFound);
				resultsLayout.setComponentAlignment(lblMAPsFound, Alignment.TOP_CENTER);			
				
				Table tableForMTAResults = buildMTAsTable();
				tableForMTAResults.setWidth("100%");
				tableForMTAResults.setPageLength(10);
				tableForMTAResults.setSelectable(true);
				tableForMTAResults.setColumnCollapsingAllowed(true);
				tableForMTAResults.setColumnReorderingAllowed(true);
				tableForMTAResults.setStyleName("strong");
				resultsLayout.addComponent(tableForMTAResults);
				resultsLayout.setComponentAlignment(tableForMTAResults, Alignment.MIDDLE_CENTER);
				
				
				verticalLayout.addComponent(resultsLayout);
			}
		}
		
		
		if (null != listOfQTLDetailsByQTLIDs){
			iNumOfTraitssFound = listOfQTLDetailsByQTLIDs.size();
		}

		Label lblMAPsFound = new Label(iNumOfTraitssFound + " QTL's Found");
		lblMAPsFound.setStyleName(Reindeer.LABEL_H2);
		resultsLayout.addComponent(lblMAPsFound);
		resultsLayout.setComponentAlignment(lblMAPsFound, Alignment.TOP_CENTER);

		if (0 != iNumOfTraitssFound){
			Table tableForMAPResults = buildmapTable();
			tableForMAPResults.setWidth("100%");
			tableForMAPResults.setPageLength(10);
			tableForMAPResults.setSelectable(true);
			tableForMAPResults.setColumnCollapsingAllowed(true);
			tableForMAPResults.setColumnReorderingAllowed(true);
			tableForMAPResults.setStyleName("strong");
			resultsLayout.addComponent(tableForMAPResults);
			resultsLayout.setComponentAlignment(tableForMAPResults, Alignment.MIDDLE_CENTER);
		}

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
				if (null != listOfQTLDetailsByQTLIDs){

					for (int i = 0; i < listOfQTLDetailsByQTLIDs.size(); i++){

						QtlDetailElement qtlDetailElement = listOfQTLDetailsByQTLIDs.get(i);

						final String strQTLName = qtlDetailElement.getQtlName();
						String strMapName = qtlDetailElement.getMapName();
						final String strChromosome = qtlDetailElement.getChromosome();
						final Float fMinPosition = qtlDetailElement.getMinPosition();
						final Float fMaxPosition = qtlDetailElement.getMaxPosition();
						
						//String strTrait = qtlDetailElement.getTrait();
						
						//System.out.println("^|^................"+qtlDetailElement.getTraitId());
						
						String strTrait = "";
						Integer iTraitId = qtlDetailElement.getTraitId();
						if (null != iTraitId){
							/*TraitDAO traitDAOLocal = new TraitDAO();
							traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
							String traitFromLocal="";
							try {
								
								traitFromLocal=om.getStandardVariable(iTraitId).getName();
							} catch (MiddlewareQueryException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						String strExperiment = qtlDetailElement.getExperiment();
						String strLeftFlankingMarker = qtlDetailElement.getLeftFlankingMarker();
						String strRightFlankingMarker = qtlDetailElement.getRightFlankingMarker();
						//Integer iEffect = qtlDetailElement.getEffect();
						Float iEffect = qtlDetailElement.getEffect();
						Float fScoreValue = qtlDetailElement.getScoreValue();
						Float fRSquare = qtlDetailElement.getrSquare();
						String strInteractions = qtlDetailElement.getInteractions();
						String strTRName = qtlDetailElement.gettRName();
						String strOntology = qtlDetailElement.getOntology();
						
						
						String seAdditive=qtlDetailElement.getSeAdditive();
						String lVParent=qtlDetailElement.getLvParent();
						String lVAllele=qtlDetailElement.getLvAllele();
						String hVParent=qtlDetailElement.getHvParent();
						String hVAllele=qtlDetailElement.getHvAllele();

						String strCropOntologyLink = "http://www.cropontology.org/terms/" + strOntology + "/"+strTRName;
						Link linkCropOntologySite = new Link(strTrait, new ExternalResource(strCropOntologyLink));
						linkCropOntologySite.setTargetName("_blank");
						
						
						String strCMapLink = "http://cmap.icrisat.ac.in/cgi-bin/cmap_public/" + "feature_search?features=" +
								            strQTLName + "&order_by=&data_source=CMAP_PUBLIC";
						Link linkCMap = new Link("CMap", new ExternalResource(strCMapLink));
						linkCMap.setTargetName("_blank");

						
						listOfData.add(new String[] {strQTLName, strMapName, strChromosome, String.valueOf(fMinPosition), String.valueOf(fMaxPosition),
								strTRName, strExperiment, strLeftFlankingMarker, strRightFlankingMarker, String.valueOf(iEffect),seAdditive,hVParent, hVAllele, lVParent, lVAllele,String.valueOf(fScoreValue), String.valueOf(fRSquare), 
								strInteractions, strCMapLink});
					}
					
					
					
					
					String[] strArrayOfColNames = {"QTl-NAME", "MAP-NAME", "CHROMOSOME", "MIN-POSITION", "MAX-POSITION",
							"TRAIT", "EXPERIMENT", "LM", "RM", "EFFECT", "SE ADDITIVE", "HIGH VALUE PARENT","HIGH VALUE ALLELE","LOW VALUE PARENT","LOW VALUE ALLELE",
							"SCORE-VALUE", "R-SQUARE", "INTERACTIONS", "VISUALIZE"};
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

		

		if (0 == iNumOfTraitssFound){
			//pdfButton.setEnabled(false);
			excelButton.setEnabled(false);
			//printButton.setEnabled(false);
		}
		resultsLayout.addComponent(layoutForExportTypes);
		resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);

		verticalLayout.addComponent(resultsLayout);
		return verticalLayout;
	}

	private Table buildMTAsTable(){
		_mtaTable = new Table();
		
		_mtaTable.setPageLength(10);
		_mtaTable.setSelectable(true);
		_mtaTable.setColumnCollapsingAllowed(true);
		_mtaTable.setColumnReorderingAllowed(true);

		
		String[] strArrayOfColNames = {"TRAIT", "MARKER", "MAP-NAME", "CHROMOSOME", "POSITION", "HIGH VALUE ALLELE", "EXPERIMENT", "EFFECT", "SCORE-VALUE", "R-SQUARE"};

		for (int i = 0; i < strArrayOfColNames.length; i++){
			_mtaTable.addContainerProperty(strArrayOfColNames[i], String.class, null);
			
		}
		for (int i = 0; i < finalListOfMappingData.size(); i++){

			Mta mtaDetailElement = finalListOfMappingData.get(i);

			final String markerName = hmOfMarkerIDAndNames.get(mtaDetailElement.getMarkerId());
			String strMapName = hmOfMapIDAndNames.get(mtaDetailElement.getMapId());
			//final String strChromosome = mtaDetailElement.getLinkageGroup();
			final String strChromosome = mtaDetailElement.getChromosome();
			String strTrait=hmOfTraitIDAndTraitNames.get(mtaDetailElement.gettId());
			final Float fPosition = mtaDetailElement.getPosition();
			/*String hVAllele=mtaDetailElement.getHvAllele();
			
			String strExperiment = mtaDetailElement.getExperiment();
			
			Integer iEffect = mtaDetailElement.getEffect();*/
			String hVAllele="";
			
			String strExperiment = "";
			
			Integer iEffect = 0;
			Float fScoreValue = mtaDetailElement.getScoreValue();
			Float fRSquare = mtaDetailElement.getrSquare();
			
			
			
			
			_mtaTable.addItem(new Object[] {strTrait, markerName, strMapName, strChromosome, fPosition, hVAllele,
					 strExperiment, iEffect, fScoreValue, fRSquare}, new Integer(i));
		}
		return _mtaTable;
	}
	
	
	
	private Table buildmapTable() {
		_qtlTable = new Table();
		//_qtlTable.setStyleName("markertable");
		_qtlTable.setPageLength(10);
		_qtlTable.setSelectable(true);
		_qtlTable.setColumnCollapsingAllowed(true);
		_qtlTable.setColumnReorderingAllowed(true);

		/*String[] strArrayOfColNames = {"QTl-NAME", "MAP-NAME", "CHROMOSOME", "MIN-POSITION", "MAX-POSITION",
				"TRAIT", "EXPERIMENT", "LM", "RM", "EFFECT", "SCORE-VALUE",
				"R-SQUARE", "INTERACTIONS", "TR-NAME", "ONTOLOGY", "VISUALIZE"};*/

		String[] strArrayOfColNames = {"QTl-NAME", "MAP-NAME", "CHROMOSOME", "MIN-POSITION", "MAX-POSITION",
				"TRAIT", "EXPERIMENT", "LM", "RM", "EFFECT", "SE ADDITIVE", "HIGH VALUE PARENT","HIGH VALUE ALLELE","LOW VALUE PARENT","LOW VALUE ALLELE",
				"SCORE-VALUE", "R-SQUARE", "INTERACTIONS", "VISUALIZE"};

		for (int i = 0; i < strArrayOfColNames.length; i++){
			if (0 == i || 2 == i){
				_qtlTable.addContainerProperty(strArrayOfColNames[i], Button.class, null);
			} else if (5 == i || 18 == i){
				_qtlTable.addContainerProperty(strArrayOfColNames[i], Link.class, null);
			} else {
				_qtlTable.addContainerProperty(strArrayOfColNames[i], String.class, null);
			}
			//_qtlTable.setColumnWidth(strArrayOfColNames[i], 30);
		}

		
		if (null != listOfQTLDetailsByQTLIDs){

			for (int i = 0; i < listOfQTLDetailsByQTLIDs.size(); i++){

				QtlDetailElement qtlDetailElement = listOfQTLDetailsByQTLIDs.get(i);

				final String strQTLName = qtlDetailElement.getQtlName();
				final String strMapName = qtlDetailElement.getMapName();
				final String strChromosome = qtlDetailElement.getChromosome();
				final Float fMinPosition = qtlDetailElement.getMinPosition();
				final Float fMaxPosition = qtlDetailElement.getMaxPosition();
				
				//String strTrait = qtlDetailElement.getTrait();
				String strTrait = "";
				Integer iTraitId = qtlDetailElement.getTraitId();
				if (null != iTraitId){
					/*TraitDAO traitDAOLocal = new TraitDAO();
					traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
					String traitFromLocal="";
					try {
						/*traitFromLocal = traitDAOLocal.getByTraitId(iTraitId);
						if (null != traitFromLocal){
							strTrait = traitFromLocal.getAbbreviation();
						} else {
							TraitDAO traitDAOCentral = new TraitDAO();
							traitDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
							Trait traitFromCentral = traitDAOCentral.getByTraitId(iTraitId);
							strTrait = traitFromCentral.getAbbreviation();
						}*/
						traitFromLocal=om.getStandardVariable(iTraitId).getName();
					} catch (MiddlewareQueryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				String strExperiment = qtlDetailElement.getExperiment();
				String strLeftFlankingMarker = qtlDetailElement.getLeftFlankingMarker();
				String strRightFlankingMarker = qtlDetailElement.getRightFlankingMarker();
				//Integer iEffect = qtlDetailElement.getEffect();
				Float iEffect = qtlDetailElement.getEffect();
				Float fScoreValue = qtlDetailElement.getScoreValue();
				Float fRSquare = qtlDetailElement.getrSquare();
				String strInteractions = qtlDetailElement.getInteractions();
				String strTRName = qtlDetailElement.gettRName();
				String strOntology = qtlDetailElement.getOntology();
				String seAdditive=qtlDetailElement.getSeAdditive();
				String lVParent=qtlDetailElement.getLvParent();
				String lVAllele=qtlDetailElement.getLvAllele();
				String hVParent=qtlDetailElement.getHvParent();
				String hVAllele=qtlDetailElement.getHvAllele();
				mapName=qtlDetailElement.getMapName();
				//qtlDetailElement.getMapName();
				//System.out.println("...........:"+strTRName);
				String ontTrName="";
				try{
					Set<StandardVariable> standardVariables = om.findStandardVariablesByNameOrSynonym(strTRName);
					
					for (StandardVariable stdVar : standardVariables) {
						//System.out.println(stdVar);
						//System.out.println("....................~~~~~~~~~~~~~~~~~~~~  :"+stdVar.getProperty().getName()+"   ontology ID=:"+stdVar.getCropOntologyId());
						strOntology=stdVar.getCropOntologyId();
						ontTrName=stdVar.getProperty().getName();
					}
				}catch (MiddlewareQueryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Button qtlNameLink = new Button();
				qtlNameLink.setCaption(strQTLName);
				qtlNameLink.setStyleName(Reindeer.BUTTON_LINK);
				qtlNameLink.setDescription(strQTLName);
				qtlNameLink.addListener(new Button.ClickListener() {

					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						QtlDetailsDAO qtlDetailsDAO = new QtlDetailsDAO();
						
						String qtlName = strQTLName;
						String chromosome = strChromosome;
						int intMinValue = fMinPosition.intValue();
						int intMaxValue = fMaxPosition.intValue();

						try {							
							List<QtlDetailElement> results = genoManager.getQtlByName(qtlName, 0, (int)genoManager.countQtlByName(qtlName));
							for (QtlDetailElement res : results) {
								
								mapId=genoManager.getMapIdByName(res.getMapName());
								
								chromosome=res.getChromosome();
								intMinValue=res.getMinPosition().intValue();
								intMaxValue=res.getMaxPosition().intValue();
								
							}
							List<Integer> markerIdsByQtl = new ArrayList();
							
							qtlDetailsDAO.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession());
							marker_IDs="";
							Set<Integer> markerIDs = genoManager.getMarkerIDsByMapIDAndLinkageBetweenStartPosition(mapId, chromosome, intMinValue, intMaxValue, 0, (int)genoManager.countMarkerIDsByMapIDAndLinkageBetweenStartPosition(mapId, chromosome, intMinValue, intMaxValue));

							for (Integer markerID : markerIDs) {
								if(!markerIdsByQtl.contains(markerID)){
									markerIdsByQtl.add(markerID);
									marker_IDs=marker_IDs+markerID+",";
								}
							}
							marker_IDs=marker_IDs.substring(0, marker_IDs.length()-1);
							System.out.println("markerIdsByQtl:"+markerIdsByQtl);
							qtlDetailsDAO.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession());
							
							//List<Integer> markerIdsByQtl = qtlDetailsDAO.getMarkerIdsByQtl(qtlName, chromosome, intMinValue, intMaxValue, 0, (int) genoManager.countMarkerIdsByQtl(qtlName, chromosome, intMinValue, intMaxValue));
							
							MarkerDAO markerDAO = new MarkerDAO();
							markerDAO.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession());
							long lCntMarkersByIds = markerDAO.countMarkersByIds(markerIdsByQtl);
							//List<Marker> listOfMarkersByIds = markerDAO.getMarkersByIds(markerIdsByQtl, 0, (int)lCntMarkersByIds);
							/*System.out.println("%%%%%%%%%%%%%%%%%   :"+genoManager.getMarkersByIds(markerIdsByQtl, 0, (int)genoManager.countMarkersByMarkerIds(markerIdsByQtl)));
							List<Marker> listOfMarkersByIds =genoManager.getMarkersByIds(markerIdsByQtl, 0, (int)genoManager.countMarkersByMarkerIds(markerIdsByQtl));
							String strMarkers = "";

							if (null == listOfMarkersByIds){
								_mainHomePage.getMainWindow().getWindow().showNotification("Markers could not be obtained for the selected QTL", Notification.TYPE_ERROR_MESSAGE);
								return;
							} else if (0 == listOfMarkersByIds.size()){
								_mainHomePage.getMainWindow().getWindow().showNotification("There are no Markers the selected QTL", Notification.TYPE_ERROR_MESSAGE);
								return;
							} 

							for (Marker resM:listOfMarkersByIds){
								//Marker marker = listOfMarkersByIds.get(i);
								strMarkers = resM.getMarkerName();
							}
							System.out.println("strMarkers:"+strMarkers);*/
							// List<MarkerInfo> markerInfo = genoManager.getMarkerInfoByMarkerName(strMarkers, 0, (int)genoManager.countMarkerInfoByMarkerName(strMarkers));
							
							// List<MarkerInfo> markerInfo = genoManager.getMarkerInfoByMarkerIds(markerIdsByQtl);
							
							try{
								localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
								centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
							}catch (Exception e){
								e.printStackTrace();
							}
							String strQuerryLG="select * from gdms_marker_retrieval_info where marker_id in("+marker_IDs+")";	
							
							System.out.println(strQuerryLG);			
							List markerFromLocal=new ArrayList();		
							ArrayList markersDetailsList=new ArrayList();		
							List markerFromCentral=new ArrayList();
						
							Object obj=null;
							Object objL=null;
							Iterator itListC=null;
							Iterator itListL=null;		
							
							ArrayList markerIdsByQtlList=new ArrayList();
								
								
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
								if(! markerIdsByQtlList.contains(Integer.parseInt(strMareO[0].toString()))){
									markerIdsByQtlList.add(Integer.parseInt(strMareO[0].toString()));	
									markersDetailsList.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11]);
								}
							}
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
								if(! markerIdsByQtlList.contains(Integer.parseInt(strMareO[0].toString()))){
									markerIdsByQtlList.add(Integer.parseInt(strMareO[0].toString()));
									markersDetailsList.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11]);
								}
							}	
							Window messageWindow = new Window("Marker Names");
							GDMSInfoDialog gdmsMessageWindow = new GDMSInfoDialog(_mainHomePage,markersDetailsList,strMsg);
							messageWindow.addComponent(gdmsMessageWindow);
							messageWindow.setWidth("400px");
							messageWindow.setBorder(Window.BORDER_NONE);
							messageWindow.setClosable(true);
							messageWindow.center();

							if (!_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
								_mainHomePage.getMainWindow().addWindow(messageWindow);
							} 

						} catch (MiddlewareQueryException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
							return;
						}catch(GDMSException ge){
							ge.printStackTrace();
						}
					}
				});
				

				Button linkageGroupLink = new Button();
				linkageGroupLink.setCaption(strChromosome);
				linkageGroupLink.setStyleName(Reindeer.BUTTON_LINK);
				linkageGroupLink.setDescription(strChromosome);
				linkageGroupLink.addListener(new Button.ClickListener() {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						QtlDetailsDAO qtlDetailsDAO = new QtlDetailsDAO();
						
						
						String chromosome = strChromosome;
						
						try {
							mapId=genoManager.getMapIdByName(strMapName);
							
							List<Integer> markerIdsByQtl = new ArrayList();
							
							try{
								localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
								centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
							}catch (Exception e){
								e.printStackTrace();
							}
							/*List<MapInfo> resMap=genoManager.getMapInfoByMapAndChromosome(Database.CENTRAL, mapId, chromosome);
							for(MapInfo mInfo:resMap){
								System.out.println(mInfo);
								markerIdsByQtl.add(mInfo.getMarkerId());
							}*/
							String strQuerry="select * from gdms_markers_onmap where map_id="+mapId+" and linkage_group ='"+chromosome+"' order by start_position ";	
							
							//System.out.println(strQuerry);			
							List snpsFromLocal=new ArrayList();		
							List markersList=new ArrayList();		
							List snpsFromCentral=new ArrayList();
						
							Object obj=null;
							Object objL=null;
							Iterator itListC=null;
							Iterator itListL=null;		
								
							SQLQuery queryL=localSession.createSQLQuery(strQuerry);		
							queryL.addScalar("marker_id",Hibernate.INTEGER);
							queryL.addScalar("start_position",Hibernate.DOUBLE);
							
							snpsFromLocal=queryL.list();
							for(int w=0;w<snpsFromLocal.size();w++){
								Object[] strMareO= (Object[])snpsFromLocal.get(w);
								if(! markerIdsByQtl.contains(Integer.parseInt(strMareO[0].toString()))){
									markerIdsByQtl.add(Integer.parseInt(strMareO[0].toString()));
									marker_IDs=marker_IDs+strMareO[0]+",";
								}
								
							}	
								
								
							SQLQuery queryC=centralSession.createSQLQuery(strQuerry);		
							queryC.addScalar("marker_id",Hibernate.INTEGER);
							queryC.addScalar("start_position",Hibernate.DOUBLE);
							snpsFromCentral=queryC.list();			
							for(int w=0;w<snpsFromCentral.size();w++){
								Object[] strMareO= (Object[])snpsFromCentral.get(w);
								if(! markerIdsByQtl.contains(Integer.parseInt(strMareO[0].toString()))){
									markerIdsByQtl.add(Integer.parseInt(strMareO[0].toString()));	
									marker_IDs=marker_IDs+strMareO[0]+",";
								}
								
							}
							
							marker_IDs=marker_IDs.substring(0, marker_IDs.length()-1);
							
							
							//Set<Integer> markerIDs = genoManager.getMarkerIDsByMapIDAndLinkageBetweenStartPosition(mapId, chromosome, intMinValue, intMaxValue, 0, (int)genoManager.countMarkerIDsByMapIDAndLinkageBetweenStartPosition(mapId, chromosome, intMinValue, intMaxValue));

							//for (Integer markerID : markerIDs) {								
								//markerIdsByQtl.add(markerID);
							//}
							
							System.out.println("markerIdsByQtl=:"+markerIdsByQtl);							
							
							if (null == markerIdsByQtl){
							 	_mainHomePage.getMainWindow().getWindow().showNotification("Markers could not be obtained for the selected Chromosome", Notification.TYPE_ERROR_MESSAGE);
								return;
							} else if (0 == markerIdsByQtl.size()){
								_mainHomePage.getMainWindow().getWindow().showNotification("There are no Markers the selected Chromosome", Notification.TYPE_ERROR_MESSAGE);
								return;
							} 
							
							//List<MarkerInfo> markerInfo = genoManager.getMarkerInfoByMarkerIds(markerIdsByQtl);
							try{
								localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
								centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
							}catch (Exception e){
								e.printStackTrace();
							}
							String strQuerryLG="select * from gdms_marker_retrieval_info where marker_id in("+marker_IDs+")";	
							
							System.out.println(strQuerryLG);			
							List markerFromLocal=new ArrayList();		
							ArrayList markersDetailsList=new ArrayList();		
							List markerFromCentral=new ArrayList();
						
							obj=null;
							objL=null;
							itListC=null;
							itListL=null;		
							/*markerName, markerType, species, markerId, ploidy, motif, forwardPrimer;
							reversePrimer, annealingTemp, principalInvestigator;
							contact, institute;*/
							
							ArrayList markerIdsByQtlLGList= new ArrayList();
							
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
								if(! markerIdsByQtlLGList.contains(Integer.parseInt(strMareO[0].toString()))){
									markerIdsByQtlLGList.add(Integer.parseInt(strMareO[0].toString()));	
									markersDetailsList.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11]);
								}
							}
							
							
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
								if(! markerIdsByQtlLGList.contains(Integer.parseInt(strMareO[0].toString()))){
									markerIdsByQtlLGList.add(Integer.parseInt(strMareO[0].toString()));
									markersDetailsList.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11]);
								}
							}	
								
								
							
							System.out.println("markersDetailsList:"+markersDetailsList);
							
							
							
							
							// List<MarkerInfo> markerInfo = genoManager.getMarkerInfoByMarkerName(strMarkers, 0, (int)genoManager.countMarkerInfoByMarkerName(strMarkers));
							Window messageWindow = new Window("Markers on Chromosome : '"+chromosome+"'");
							GDMSInfoDialog gdmsMessageWindow = new GDMSInfoDialog(_mainHomePage,markersDetailsList,strMsg);
							messageWindow.addComponent(gdmsMessageWindow);
							messageWindow.setWidth("400px");
							messageWindow.setBorder(Window.BORDER_NONE);
							messageWindow.setClosable(true);
							messageWindow.center();

							if (!_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
								_mainHomePage.getMainWindow().addWindow(messageWindow);
							} 

						} catch (MiddlewareQueryException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
							return;
						} /*catch (GDMSException e){
							e.printStackTrace();
						}*/
					}
				});
				
				
				
				
				
				
				String strCropOntologyLink = "http://www.cropontology.org/terms/" + strOntology + "/"+ontTrName;
				//String strCropOntologyLink = "http://www.cropontology.org/terms/" + strOntology + "/Harvest";
				Link linkCropOntologySite = new Link(strTRName, new ExternalResource(strCropOntologyLink));
				linkCropOntologySite.setTargetName("_blank");
				

				String strCMapLink = "http://cmap.icrisat.ac.in/cgi-bin/cmap_public/" + "feature_search?features=" +
						            strQTLName + "&order_by=&data_source=CMAP_PUBLIC";
				Link linkCMap = new Link("CMap", new ExternalResource(strCMapLink));
				linkCMap.setTargetName("_blank");

				
				_qtlTable.addItem(new Object[] {qtlNameLink, strMapName, linkageGroupLink, fMinPosition, fMaxPosition,
						linkCropOntologySite, strExperiment, strLeftFlankingMarker, strRightFlankingMarker, iEffect,seAdditive,hVParent, hVAllele, lVParent, lVAllele, fScoreValue, fRSquare, 
						strInteractions, linkCMap}, new Integer(i));
			}
		}

		return _qtlTable;
	}

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}
	
	/*private void buildOnLoad(final GridLayout gridLayout, String theSearchString) {
		gridLayout.removeAllComponents();
		gridLayout.setSpacing(true);
		RetrieveQTL retrieveQTL = new RetrieveQTL();
		List<String> retrieveTraitNames = new ArrayList<String>();
		try {
			List<QtlDetailElement> retrieveQTLDetails = retrieveQTL.retrieveQTLDetails();
			for (QtlDetailElement qtlDetailElement : retrieveQTLDetails) {
				//String strTrait = qtlDetailElement.getTrait();
				String strTrait = "";
				Integer iTraitId = qtlDetailElement.getTraitId();
				if (null != iTraitId){
					TraitDAO traitDAOLocal = new TraitDAO();
					traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
					Trait traitFromLocal;
					try {
						traitFromLocal = traitDAOLocal.getByTraitId(iTraitId);
						if (null != traitFromLocal){
							strTrait = traitFromLocal.getAbbreviation();
						} else {
							TraitDAO traitDAOCentral = new TraitDAO();
							traitDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
							Trait traitFromCentral = traitDAOCentral.getByTraitId(iTraitId);
							strTrait = traitFromCentral.getAbbreviation();
						}
					} catch (MiddlewareQueryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(null != strTrait && false == retrieveTraitNames.contains(strTrait)) {
					retrieveTraitNames.add(strTrait);
				}
			}
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			return;
		}
		

		
		List<String> retrieveQTLNamesFinal = new ArrayList<String>();
		
		if(null != theSearchString && false == theSearchString.equals("*")) {
			//theSearchString = theSearchString.substring(0, theSearchString.length() - 1);
			for (int i = 0; i < retrieveTraitNames.size(); i++) {
				String string = retrieveTraitNames.get(i);
				if(true == string.startsWith(theSearchString)) {
					retrieveQTLNamesFinal.add(string);
				}
			}
		} else {
			for (int i = 0; i < retrieveTraitNames.size(); i++) {
				retrieveQTLNamesFinal.add(retrieveTraitNames.get(i));
			}
		}

		gridLayout.setColumns(1);
		int rowCount = retrieveQTLNamesFinal.size();
		if(rowCount == 0) {
			rowCount = 1;
		}
		gridLayout.setRows(rowCount);
		int iCounter = 0;
		arrayOfCheckBoxes = new CheckBox[retrieveQTLNamesFinal.size()];
		int i = 0;
		for (String string : retrieveQTLNamesFinal) {
			arrayOfCheckBoxes[iCounter] = new CheckBox(string);
			arrayOfCheckBoxes[iCounter].setImmediate(true);
			gridLayout.addComponent(arrayOfCheckBoxes[i], 0, i);
			iCounter++;
			i++;
		}
	}*/

	private void buildOnLoad(final HorizontalLayout horizontalLayout, String theSearchString) {
		horizontalLayout.removeAllComponents();
		horizontalLayout.setSpacing(true);
		RetrieveQTL retrieveQTL = new RetrieveQTL();
		List<String> retrieveTraitNames = new ArrayList<String>();
		try {
			
			List<Integer> retrieveQTLDetails = retrieveQTL.retrieveAllTraits();
			for(int q=0;q<retrieveQTLDetails.size();q++){
				String trait=om.getStandardVariable(retrieveQTLDetails.get(q)).getName();
				if(!retrieveTraitNames.contains(trait))
					retrieveTraitNames.add(trait);
			}
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			return;
		}
				
		List<String> retrieveQTLNamesFinal = new ArrayList<String>();
		
		if(null != theSearchString && false == theSearchString.equals("*")) {
			//theSearchString = theSearchString.substring(0, theSearchString.length() - 1);
			for (int i = 0; i < retrieveTraitNames.size(); i++) {
				String string = retrieveTraitNames.get(i);
				if(true == string.startsWith(theSearchString)) {
					retrieveQTLNamesFinal.add(string);
				}
			}
		} else {
			for (int i = 0; i < retrieveTraitNames.size(); i++) {
				retrieveQTLNamesFinal.add(retrieveTraitNames.get(i));
			}
		}

		
		//System.out.println("..........   retrieveQTLNamesFinal.size()=:"+retrieveQTLNamesFinal.size()+"    :   "+retrieveQTLNamesFinal);
		if (0 < retrieveQTLNamesFinal.size()) {
			_tableWithAllTraits = new Table();
			_tableWithAllTraits.setSizeFull();
			_tableWithAllTraits.setPageLength(5);
			_tableWithAllTraits.setSelectable(false);
			_tableWithAllTraits.setColumnCollapsingAllowed(false);
			_tableWithAllTraits.setColumnReorderingAllowed(false);
			_tableWithAllTraits.setEditable(false);
			_tableWithAllTraits.setStyleName("strong");
			horizontalLayout.addComponent(_tableWithAllTraits);
			
			_tableWithAllTraits.addContainerProperty("Select", CheckBox.class, null);
			_tableWithAllTraits.addContainerProperty("Trait Name", String.class, null);
			_tableWithAllTraits.setColumnWidth("Select", 40);
			_tableWithAllTraits.setColumnWidth("Trait Name", 500);
			
			int i = 0;
			for (String strQTLName : retrieveQTLNamesFinal) {
				_tableWithAllTraits.addItem(new Object[]{new CheckBox(), strQTLName}, new Integer(i));
				i++;
			}
		}
		btnNext.setEnabled(true);
	}
}
