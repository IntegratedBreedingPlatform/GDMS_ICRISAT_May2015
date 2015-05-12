package org.icrisat.gdms.ui;

import java.io.IOException;
import java.util.ArrayList;
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
import org.generationcp.middleware.pojos.gdms.MapInfo;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerInfo;
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
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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
//import org.generationcp.middleware.dao.TraitDAO;
//import org.generationcp.middleware.pojos.Trait;


public class RetrieveQTLInformationComponent implements Component.Listener {
	
	private static final long serialVersionUID = 1L;
	private TabSheet _tabsheetForQTL;

	private Component buildQTLResultsComponent;
	private GDMSMain _mainHomePage;
	private List<QtlDetailElement> listOfQTLDetailElementsByName;
	//private CheckBox[] arrayOfCheckBoxes;
	private Table _qtlTable;
	private Table _tableWithAllQTLs;

	private CheckBox[] arrayOfCheckBoxes;
	private Session localSession;
	private Session centralSession;
	ManagerFactory factory=null;
	GenotypicDataManager genoManager;
	OntologyDataManager om;
	String mapName="";
	int mapId=0;
	Button btnNext;
	String strMsg="";
	
	public RetrieveQTLInformationComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			
			
			genoManager=factory.getGenotypicDataManager();
			om=factory.getOntologyDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * Building the entire Tabbed Component required for QTL data
	 * 
	 */
	public HorizontalLayout buildTabbedComponentForQTL() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();

		_tabsheetForQTL = new TabSheet();
		_tabsheetForQTL.setWidth("700px");
		
		Component buildQTLSearchComponent = buildQTLSearchComponent();
		buildQTLSearchComponent.setSizeFull();

		buildQTLResultsComponent = buildQTLResultsComponent();
		buildQTLResultsComponent.setSizeFull();

		_tabsheetForQTL.addComponent(buildQTLSearchComponent);
		_tabsheetForQTL.addComponent(buildQTLResultsComponent);

		_tabsheetForQTL.getTab(1).setEnabled(true);
		
		horizontalLayout.addComponent(_tabsheetForQTL);
		
		return horizontalLayout;
	}


	private Component buildQTLSearchComponent() {
		VerticalLayout searchQTLsLayout = new VerticalLayout();
		searchQTLsLayout.setCaption("Search");
		searchQTLsLayout.setMargin(true, true, true, true);
		searchQTLsLayout.setSpacing(true);

		Label lblSearch = new Label("Search QTLs");
		lblSearch.setStyleName(Reindeer.LABEL_H2);
		searchQTLsLayout.addComponent(lblSearch);
		searchQTLsLayout.setComponentAlignment(lblSearch, Alignment.TOP_CENTER);
		
		
		Label lblQTLNames = new Label("QTL Names");
		lblQTLNames.setStyleName(Reindeer.LABEL_SMALL);

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
					//System.out.println("if *");
					/*buildOnLoad(gridLayout, strSearchString);
					gridLayout.requestRepaint();*/
					buildOnLoad(horizontalLayout, strSearchString);
					horizontalLayout.requestRepaint();
					txtFieldSearch.setValue("");
				} else if(strSearchString.endsWith("*")) {
					//System.out.println("if ends with *");
					strSearchString = strSearchString.substring(0, strSearchString.length() - 1);
					/*buildOnLoad(gridLayout, strSearchString);
					gridLayout.requestRepaint();*/
					buildOnLoad(horizontalLayout, strSearchString);
					horizontalLayout.requestRepaint();
					txtFieldSearch.setValue("");
				} else {
					//System.out.println("If String");
					/*buildOnLoad(gridLayout, strSearchString);
					gridLayout.requestRepaint();*/
					buildOnLoad(horizontalLayout, strSearchString);
					horizontalLayout.requestRepaint();
					txtFieldSearch.setValue("");
				}
				
				if (null == _tableWithAllQTLs){
					_mainHomePage.getMainWindow().getWindow().showNotification("There are no QTLs to be displayed.", Notification.TYPE_ERROR_MESSAGE);
					btnNext.setEnabled(false);
					return;
				}
				
				if (null != _tableWithAllQTLs && 0 == _tableWithAllQTLs.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("There are no QTLs to be displayed.", Notification.TYPE_ERROR_MESSAGE);
					btnNext.setEnabled(false);
					return;
				}
			}
		});
		HorizontalLayout layoutForTextSearch = new HorizontalLayout();
		layoutForTextSearch.setSpacing(true);
		layoutForTextSearch.addComponent(lblQTLNames);
		layoutForTextSearch.addComponent(txtFieldSearch);
		layoutForTextSearch.addComponent(searchButton);
		
		searchQTLsLayout.addComponent(layoutForTextSearch);
		searchQTLsLayout.setMargin(true, true, true, true);
		searchQTLsLayout.addComponent(horizontalLayout);
		
		VerticalLayout layoutForButton = new VerticalLayout();
		btnNext = new Button("Next");
		btnNext.setEnabled(false);
		layoutForButton.addComponent(btnNext);
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				listOfQTLDetailElementsByName = new ArrayList<QtlDetailElement>();
				String strQTLName = txtFieldSearch.getValue().toString();
				List<String> listOfNames = new ArrayList<String>();
				if(false == strQTLName.equals("*") && false == strQTLName.endsWith("*")) {
					/*if(null != arrayOfCheckBoxes && 0 != arrayOfCheckBoxes.length) {
						for (int i = 0; i < arrayOfCheckBoxes.length; i++) {
							CheckBox checkBox = arrayOfCheckBoxes[i];
							if(checkBox.booleanValue()) {
								listOfNames.add(checkBox.getCaption());
							}
						}
					} 
					if(0 != strQTLName.trim().length()) {
						if(false == listOfNames.contains(strQTLName)) {
							listOfNames.add(strQTLName);
						}
					}*/
					//TODO
					
					int iNumOfQTLs = _tableWithAllQTLs.size();
					for (int i = 0; i < iNumOfQTLs; i++) {
						Item item = _tableWithAllQTLs.getItem(new Integer(i));
						Property itemProperty = item.getItemProperty("Select");
						CheckBox checkBox = (CheckBox) itemProperty.getValue();
						if (checkBox.booleanValue() == true) {
							String strSelectedQTL = item.getItemProperty("QTL Name").toString();
							listOfNames.add(strSelectedQTL);
						}
					}
				}
				
					
				if (null != strQTLName && (0 != strQTLName.trim().length())){
					
					if(strQTLName.equals("*")) {
						RetrieveQTL retrieveQTL = new RetrieveQTL();
						try {
							List<QtlDetailElement> retrieveQTL2 = retrieveQTL.retrieveQTLDetails();
							listOfQTLDetailElementsByName = retrieveQTL2;
						} catch (MiddlewareQueryException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					} else if(strQTLName.endsWith("*")) {
						RetrieveQTL retrieveQTL = new RetrieveQTL();
						try {
							strQTLName = strQTLName.substring(0, strQTLName.length()-1);
							List<QtlDetailElement> retrieveQTL2 = retrieveQTL.retrieveQTLDetailsStartsWith(strQTLName);
							listOfQTLDetailElementsByName = retrieveQTL2;
						} catch (MiddlewareQueryException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					} else {
						//System.out.println("................:"+listOfNames);
							for (String string : listOfNames) {
								if(false == string.equals("*") && false == string.endsWith("*")) {
									try {
										RetrieveQTL retrieveQTL = new RetrieveQTL();
										List<QtlDetailElement> retrieveQTLByName = retrieveQTL.retrieveQTLByName(string);
										if(null != retrieveQTLByName) {
											listOfQTLDetailElementsByName.addAll(retrieveQTLByName);
										}
									} catch (MiddlewareQueryException e) {
										_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
										return;
									}
							}
						}
					}
				} else {
					for (String string : listOfNames) {
						if(false == string.equals("*") && false == string.endsWith("*")) {
					try {
								RetrieveQTL retrieveQTL = new RetrieveQTL();
								List<QtlDetailElement> retrieveQTLByName = retrieveQTL.retrieveQTLByName(string);
								if(null != retrieveQTLByName) {
									listOfQTLDetailElementsByName.addAll(retrieveQTLByName);
								}
					} catch (MiddlewareQueryException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				}
				}
			}
			
				if (0 == listOfQTLDetailElementsByName.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("No QTL Elements to be displayed.",  Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				Component newQTLResultsPanel = buildQTLResultsComponent();
				_tabsheetForQTL.replaceComponent(buildQTLResultsComponent, newQTLResultsPanel);
				_tabsheetForQTL.requestRepaint();
				buildQTLResultsComponent = newQTLResultsPanel;
				_tabsheetForQTL.getTab(1).setEnabled(true);
				_tabsheetForQTL.setSelectedTab(1);
			}
		});
		
		
		searchQTLsLayout.addComponent(layoutForButton);
		searchQTLsLayout.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);
		
		return searchQTLsLayout;
	}


	private Component buildQTLResultsComponent() {
		VerticalLayout verticalLayout = new VerticalLayout();
		VerticalLayout resultsLayout = new VerticalLayout();
		verticalLayout.setCaption("Results");
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true, true, true, true);
		

		int iNumOfQTLsFound = 0;
		if (null != listOfQTLDetailElementsByName){
			iNumOfQTLsFound = listOfQTLDetailElementsByName.size();
		}
		
		Label lblQTLsFound = new Label(iNumOfQTLsFound + " QTL's Found");
		lblQTLsFound.setStyleName(Reindeer.LABEL_H2);
		resultsLayout.addComponent(lblQTLsFound);
		resultsLayout.setComponentAlignment(lblQTLsFound, Alignment.TOP_CENTER);
		
		if (0 != iNumOfQTLsFound){
			Table tableForQTLResults = buildQTLTable(resultsLayout);
			tableForQTLResults.setWidth("100%");
			tableForQTLResults.setPageLength(10);
			tableForQTLResults.setSelectable(true);
			tableForQTLResults.setColumnCollapsingAllowed(false);
			tableForQTLResults.setColumnReorderingAllowed(true);
			tableForQTLResults.setStyleName("strong");
		resultsLayout.addComponent(tableForQTLResults);
		resultsLayout.setComponentAlignment(tableForQTLResults, Alignment.MIDDLE_CENTER);
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
				
				if (null != listOfQTLDetailElementsByName){

					for (int i = 0; i < listOfQTLDetailElementsByName.size(); i++){
						
						QtlDetailElement qtlDetailElement = listOfQTLDetailElementsByName.get(i);
					
						final String strQTLName = qtlDetailElement.getQtlName();
						String strMapName = qtlDetailElement.getMapName();
						final String strChromosome = qtlDetailElement.getChromosome();
						final Float fMinPosition = qtlDetailElement.getMinPosition();
						final Float fMaxPosition = qtlDetailElement.getMaxPosition();

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
						String hVParent=qtlDetailElement.getHvParent();
						String hVAllele=qtlDetailElement.getHvAllele();
						String lVParent=qtlDetailElement.getLvParent();
						String lVAllele=qtlDetailElement.getLvAllele();
						
						
						
						String strTrait = "";
						Integer iTraitId = qtlDetailElement.getTraitId();
						if (null != iTraitId){							
							String traitFromLocal="";
							try {								
								traitFromLocal=om.getStandardVariable(iTraitId).getName();								
							} catch (MiddlewareQueryException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						String ontTrName="";
						try{
							Set<StandardVariable> standardVariables = om.findStandardVariablesByNameOrSynonym(strTRName);					
							for (StandardVariable stdVar : standardVariables) {						
								strOntology=stdVar.getCropOntologyId();
								ontTrName=stdVar.getProperty().getName();
							}
						}catch (MiddlewareQueryException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
						
						
						
						listOfData.add(new String[] {strQTLName, strMapName, strChromosome, String.valueOf(fMinPosition), String.valueOf(fMaxPosition),
								strTRName, strExperiment, strLeftFlankingMarker, strRightFlankingMarker, String.valueOf(iEffect), seAdditive, hVParent, hVAllele, lVParent, lVAllele, String.valueOf(fScoreValue), String.valueOf(fRSquare), 
								strInteractions});
					}
					
					
					String[] strArrayOfColNames = {"QTl-NAME", "MAP-NAME", "CHROMOSOME", "MIN-POSITION", "MAX-POSITION",
							"TRAIT", "EXPERIMENT", "LM", "RM", "EFFECT","SE ADDITIVE", "HIGH VALUE PARENT","HIGH VALUE ALLELE","LOW VALUE PARENT","LOW VALUE ALLELE",
							"SCORE-VALUE", "R-SQUARE", "INTERACTIONS"};
					
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
		
		if (0 == iNumOfQTLsFound){
			excelButton.setEnabled(false);
			/*pdfButton.setEnabled(false);
			printButton.setEnabled(false);*/
		}
		
		resultsLayout.addComponent(layoutForExportTypes);
		resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
		
		verticalLayout.addComponent(resultsLayout);
		
		return verticalLayout;
	}
	
	private Table buildQTLTable(final VerticalLayout resultsLayout) {
				
		_qtlTable = new Table();
		_qtlTable.setStyleName("markertable");
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
			if (0 == i || 2==i){
				_qtlTable.addContainerProperty(strArrayOfColNames[i], Button.class, null);
				//_qtlTable.addContainerProperty(strArrayOfColNames[i], String.class, null);
			} else if (5 == i || 18 == i){
				_qtlTable.addContainerProperty(strArrayOfColNames[i], Link.class, null);
				//_qtlTable.addContainerProperty(strArrayOfColNames[i], String.class, null);
			} else {
				_qtlTable.addContainerProperty(strArrayOfColNames[i], String.class, null);
			}
			_qtlTable.setColumnWidth(strArrayOfColNames[i], 150);
		}
		
		
		if (null != listOfQTLDetailElementsByName){
			
			for (int i = 0; i < listOfQTLDetailElementsByName.size(); i++){
				
				QtlDetailElement qtlDetailElement = listOfQTLDetailElementsByName.get(i);
			
				final String strQTLName = qtlDetailElement.getQtlName();
				final String strMapName = qtlDetailElement.getMapName();
				final String strChromosome = qtlDetailElement.getChromosome();
				final Float fMinPosition = qtlDetailElement.getMinPosition();
				final Float fMaxPosition = qtlDetailElement.getMaxPosition();
				
				//String strTrait = qtlDetailElement.getTrait();
				String strTrait = "";
				Integer iTraitId = qtlDetailElement.getTraitId();
				if (null != iTraitId){
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
				mapName=qtlDetailElement.getMapName();
				//qtlDetailElement.getMapName();
				//System.out.println("...........:"+strTRName);
				String ontTrName="";
				try{
					Set<StandardVariable> standardVariables = om.findStandardVariablesByNameOrSynonym(strTRName);					
					for (StandardVariable stdVar : standardVariables) {						
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
						double intMinValue = fMinPosition;
						double intMaxValue = fMaxPosition;
						//int mapId=genoManager.getMapIdByName(mapName);
						try {
							
							List<QtlDetailElement> results = genoManager.getQtlByName(qtlName, 0, (int)genoManager.countQtlByName(qtlName));
							for (QtlDetailElement res : results) {
								
								mapId=genoManager.getMapIdByName(res.getMapName());
								
								chromosome=res.getChromosome();
								intMinValue=Double.parseDouble(res.getMinPosition().toString());
								intMaxValue=Double.parseDouble(res.getMaxPosition().toString());
								
							}
							List<Integer> markerIdsByQtl = new ArrayList();
							String marker_IDs="";
							
							qtlDetailsDAO.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession());
							//System.out.println("select marker_name from gdms_marker where marker_id in(select marker_id from gdms_markers_onmap where map_id ="+mapId+" and linkage_group='"+chromosome+"' and start_position between "+intMinValue+" AND "+intMaxValue+")");
							
							System.out.println(" ^^^^^^^^^^^^^^^^^^^   :"+genoManager.getMarkerIdsByQtl(qtlName, chromosome, fMinPosition.intValue(), fMaxPosition.intValue(), 0, (int)genoManager.countMarkerIdsByQtl(qtlName, chromosome, fMinPosition.intValue(), fMaxPosition.intValue())));
							//getMarkerIdsByQtl
							
							
							Set<Integer> markerIDs = genoManager.getMarkerIDsByMapIDAndLinkageBetweenStartPosition(mapId, chromosome, intMinValue, intMaxValue, 0, (int)genoManager.countMarkerIDsByMapIDAndLinkageBetweenStartPosition(mapId, chromosome, intMinValue, intMaxValue));

							for (Integer markerID : markerIDs) {								
								//markerIdsByQtl.add(markerID);
								marker_IDs=marker_IDs+markerID+",";
							}
							marker_IDs=marker_IDs.substring(0, marker_IDs.length()-1);
							System.out.println("markerIdsByQtl=:"+markerIdsByQtl);							
							
							if (null == markerIDs){
								_mainHomePage.getMainWindow().getWindow().showNotification("Markers could not be obtained for the selected QTL", Notification.TYPE_ERROR_MESSAGE);
								return;
							} else if (0 == markerIDs.size()){
								_mainHomePage.getMainWindow().getWindow().showNotification("There are no Markers the selected QTL", Notification.TYPE_ERROR_MESSAGE);
								return;
							} 
							try{
								localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
								centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
							}catch (Exception e){
								e.printStackTrace();
							}
							String strQuerry="select * from gdms_marker_retrieval_info where marker_id in("+marker_IDs+")";	
							
							//System.out.println(strQuerry);			
							List snpsFromLocal=new ArrayList();		
							List markersDetailsList=new ArrayList();		
							List snpsFromCentral=new ArrayList();
						
							Object obj=null;
							Object objL=null;
							Iterator itListC=null;
							Iterator itListL=null;		
							/*markerName, markerType, species, markerId, ploidy, motif, forwardPrimer;
							reversePrimer, annealingTemp, principalInvestigator;
							contact, institute;*/
							try{
								localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
								centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
							}catch (Exception e){
								e.printStackTrace();
							}
							
							
							SQLQuery queryL=localSession.createSQLQuery(strQuerry);		
							queryL.addScalar("marker_id",Hibernate.INTEGER);
							queryL.addScalar("marker_name",Hibernate.STRING);
							queryL.addScalar("marker_type",Hibernate.STRING);
							queryL.addScalar("species",Hibernate.STRING);
							queryL.addScalar("ploidy",Hibernate.STRING);
							queryL.addScalar("motif",Hibernate.STRING);
							queryL.addScalar("forward_primer",Hibernate.STRING);
							queryL.addScalar("reverse_primer",Hibernate.STRING);
							queryL.addScalar("annealing_temp",Hibernate.FLOAT);
							queryL.addScalar("principal_investigator",Hibernate.STRING);
							queryL.addScalar("contact",Hibernate.STRING);
							queryL.addScalar("institute",Hibernate.STRING);
							
							snpsFromLocal=queryL.list();
							for(int w=0;w<snpsFromLocal.size();w++){
								Object[] strMareO= (Object[])snpsFromLocal.get(w);
								if(! markerIdsByQtl.contains(Integer.parseInt(strMareO[0].toString()))){
									markerIdsByQtl.add(Integer.parseInt(strMareO[0].toString()));
									markersDetailsList.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11]);
								}
							}	
								
								
							SQLQuery queryC=centralSession.createSQLQuery(strQuerry);		
							queryC.addScalar("marker_id",Hibernate.INTEGER);
							queryC.addScalar("marker_name",Hibernate.STRING);
							queryC.addScalar("marker_type",Hibernate.STRING);
							queryC.addScalar("species",Hibernate.STRING);
							queryC.addScalar("ploidy",Hibernate.STRING);
							queryC.addScalar("motif",Hibernate.STRING);
							queryC.addScalar("forward_primer",Hibernate.STRING);
							queryC.addScalar("reverse_primer",Hibernate.STRING);
							queryC.addScalar("annealing_temp",Hibernate.FLOAT);
							queryC.addScalar("principal_investigator",Hibernate.STRING);
							queryC.addScalar("contact",Hibernate.STRING);
							queryC.addScalar("institute",Hibernate.STRING);
							snpsFromCentral=queryC.list();			
							for(int w=0;w<snpsFromCentral.size();w++){
								Object[] strMareO= (Object[])snpsFromCentral.get(w);
								if(! markerIdsByQtl.contains(Integer.parseInt(strMareO[0].toString()))){
									markerIdsByQtl.add(Integer.parseInt(strMareO[0].toString()));	
									markersDetailsList.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11]);
								}
							}
							System.out.println("markersDetailsList:"+markersDetailsList);
							
							
							
							//List<MarkerInfo> markerInfo = genoManager.getMarkerInfoByMarkerIds(markerIdsByQtl);
							//System.out.println("markerInfo:"+markerInfo);
							// List<MarkerInfo> markerInfo = genoManager.getMarkerInfoByMarkerName(strMarkers, 0, (int)genoManager.countMarkerInfoByMarkerName(strMarkers));
							Window messageWindow = new Window("Markers in the selected QTL");
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
						} catch (GDMSException e){
							e.printStackTrace();
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
							
							System.out.println(strQuerry);			
							List snpsFromLocal=new ArrayList();		
							List markersList=new ArrayList();		
							List snpsFromCentral=new ArrayList();
							String marker_IDs="";
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
							 	_mainHomePage.getMainWindow().getWindow().showNotification("Markers could not be obtained for the selected QTL", Notification.TYPE_ERROR_MESSAGE);
								return;
							} else if (0 == markerIdsByQtl.size()){
								_mainHomePage.getMainWindow().getWindow().showNotification("There are no Markers the selected QTL", Notification.TYPE_ERROR_MESSAGE);
								return;
							} 
							
						//	List<MarkerInfo> markerInfo = genoManager.getMarkerInfoByMarkerIds(markerIdsByQtl);
							
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
							
							
							try{
								localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
								centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
							}catch (Exception e){
								e.printStackTrace();
							}
							
							ArrayList markerIdsByQtlLGList=new ArrayList();
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
							System.out.println("markerFromCentral:"+markerFromCentral);
							for(int wc=0;wc<markerFromCentral.size();wc++){
								Object[] strMareO= (Object[])markerFromCentral.get(wc);
								if(!markerIdsByQtlLGList.contains(Integer.parseInt(strMareO[0].toString()))){
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
								//if(! markerIdsByQtl.contains(Integer.parseInt(strMareO[0].toString()))){
									//markerIdsByQtl.add(Integer.parseInt(strMareO[0].toString()));
									markersDetailsList.add(strMareO[0]+"!~!"+strMareO[1]+"!~!"+strMareO[2]+"!~!"+strMareO[3]+"!~!"+strMareO[4]+"!~!"+strMareO[5]+"!~!"+strMareO[6]+"!~!"+strMareO[7]+"!~!"+strMareO[8]+"!~!"+strMareO[9]+"!~!"+strMareO[10]+"!~!"+strMareO[11]);
								//}
							}	
								
							
							System.out.println("markersDetailsList:"+markersDetailsList);
							
							
							
							// List<MarkerInfo> markerInfo = genoManager.getMarkerInfoByMarkerName(strMarkers, 0, (int)genoManager.countMarkerInfoByMarkerName(strMarkers));
							Window messageWindow = new Window("Markers in : '"+chromosome+"'");
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
				
				
				// http://www.cropontology.org/terms/CO_337:0000059/Harvest%20index
				String strCropOntologyLink = "http://www.cropontology.org/terms/" + strOntology + "/"+ontTrName;
				//Link qtlTraitCropOntologySite = new Link("HI", new ExternalResource("http://www.cropontology.org"));
				//System.out.println("Crop Ontology Link   :"+strCropOntologyLink);
				
				/*Link qtlTraitCropOntologySite = new Link(strTrait, new ExternalResource(strCropOntologyLink));
				qtlTraitCropOntologySite.setTargetName("_blank");
				*/
				
				
				
				String strCMapLink = "http://cmap.icrisat.ac.in/cgi-bin/cmap_public/" + "feature_search?features=" +
						            strQTLName + "&order_by=&data_source=CMAP_PUBLIC";
				//Link cMapSiteLink = new Link("CMap", new ExternalResource("http://cmap.icrisat.ac.in/cmap"));
				Link cMapSiteLink = new Link("CMap", new ExternalResource(strCMapLink));
				cMapSiteLink.setTargetName("_blank");
				
				Link cropOntologySiteLink = new Link(strTRName, new ExternalResource(strCropOntologyLink));
				cropOntologySiteLink.setTargetName("_blank");
				
				
				/*_qtlTable.addItem(new Object[] {strQTLName, strMapName, strChromosome, fMinPosition, fMaxPosition,
						strCropOntologyLink, strExperiment, strLeftFlankingMarker, strRightFlankingMarker, iEffect, fScoreValue, fRSquare, 
						strInteractions, strTRName, strOntology, strCMapLink}, new Integer(i));*/
				//if(strOntology!=null){
					_qtlTable.addItem(new Object[] {qtlNameLink, strMapName, linkageGroupLink, fMinPosition, fMaxPosition,
							cropOntologySiteLink, strExperiment, strLeftFlankingMarker, strRightFlankingMarker, iEffect, seAdditive, hVParent, hVAllele, lVParent, lVAllele, fScoreValue, fRSquare, 
							strInteractions, cMapSiteLink}, new Integer(i));
				/*}else{
					_qtlTable.addItem(new Object[] {qtlNameLink, strMapName, strChromosome, fMinPosition, fMaxPosition,
							strTRName, strExperiment, strLeftFlankingMarker, strRightFlankingMarker, iEffect, fScoreValue, fRSquare, 
							strInteractions, cMapSiteLink}, new Integer(i));
				}*/
				
			}
		}
		
		return _qtlTable;
	}

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}
	
	
	private void buildOnLoad(final HorizontalLayout horizontalLayout, String theSearchString) {
		horizontalLayout.removeAllComponents();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setSizeFull();
		RetrieveQTL retrieveQTL = new RetrieveQTL();
		List<String> retrieveQTLNames = new ArrayList<String>();
		try {
			retrieveQTLNames  = retrieveQTL.retrieveQTLNames();
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			return;
		}
		
		final List<String> retrieveQTLNamesFinal = new ArrayList<String>();
		
		if(null != theSearchString && false == theSearchString.equals("*")) {
			//theSearchString = theSearchString.substring(0, theSearchString.length() - 1);
			for (int i = 0; i < retrieveQTLNames.size(); i++) {
				String string = retrieveQTLNames.get(i);
				if(true == string.startsWith(theSearchString)) {
					retrieveQTLNamesFinal.add(string);
				}
			}
		} else {
			for (int i = 0; i < retrieveQTLNames.size(); i++) {
				retrieveQTLNamesFinal.add(retrieveQTLNames.get(i));
			}
		}

		
		VerticalLayout verLayout=new VerticalLayout();		
		
		HorizontalLayout hLayoutForHap = new HorizontalLayout();
		hLayoutForHap.setSpacing(false);
		//hLayoutForHap.setMargin(true, true, false, false);	
		
		HorizontalLayout horizontalLayoutForHap = new HorizontalLayout();
		horizontalLayoutForHap.setSpacing(false);
		//horizontalLayoutForHap.setMargin(false, true, true, true);
		horizontalLayoutForHap.setMargin(true);
		horizontalLayoutForHap.addStyleName("marginSelectAll");
		
		//String strIntroPara1 = "<B>Name the Haplotype :   ";	
		//Label lblPara = new Label(strIntroPara1 , Label.CONTENT_XHTML);
		final CheckBox checkBox = new CheckBox("Select All");
		checkBox.setImmediate(true);
		checkBox.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				if (true == (Boolean) checkBox.getValue()){
					for (int i=0; i<retrieveQTLNamesFinal.size(); i++) {
						arrayOfCheckBoxes[i].setValue(true);
					}
				}else{
					for (int i=0; i<retrieveQTLNamesFinal.size(); i++) {
						arrayOfCheckBoxes[i].setValue(false);
					}					
				}
			}
		});	
		
		horizontalLayoutForHap.addComponent(checkBox);
		verLayout.addComponent(horizontalLayoutForHap);
		verLayout.setComponentAlignment(horizontalLayoutForHap, Alignment.TOP_LEFT);
		//horizontalLayout.addComponent(horizontalLayoutForHap);
		//horizontalLayout.setComponentAlignment(horizontalLayoutForHap, Alignment.TOP_RIGHT);
		//CheckBox checkbox = new CheckBox("Box with a Check");
		arrayOfCheckBoxes = new CheckBox[retrieveQTLNamesFinal.size()];
		if (0 < retrieveQTLNamesFinal.size()) {
			//CheckBox checkbox = new CheckBox("Box with a Check");
			_tableWithAllQTLs = new Table();
			_tableWithAllQTLs.setWidth("100%");
			_tableWithAllQTLs.setPageLength(5);
			_tableWithAllQTLs.setSelectable(false);
			_tableWithAllQTLs.setColumnCollapsingAllowed(false);
			_tableWithAllQTLs.setColumnReorderingAllowed(false);
			_tableWithAllQTLs.setEditable(false);
			_tableWithAllQTLs.setStyleName("strong");
			hLayoutForHap.addComponent(_tableWithAllQTLs);
			hLayoutForHap.setMargin(true);
			hLayoutForHap.addStyleName("marginQTLsTable");
			
			_tableWithAllQTLs.addContainerProperty("Select", CheckBox.class, null);
			_tableWithAllQTLs.addContainerProperty("QTL Name", String.class, null);
			_tableWithAllQTLs.setColumnWidth("Select", 40);
			_tableWithAllQTLs.setColumnWidth("QTL Name", 500);
			//_tableWithAllQTLs. setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
			int i = 0;
			for (String strQTLName : retrieveQTLNamesFinal) {
				arrayOfCheckBoxes[i] = new CheckBox();
				_tableWithAllQTLs.addItem(new Object[]{arrayOfCheckBoxes[i], strQTLName}, new Integer(i));
				i++;
			}
			
			
		}
		verLayout.addComponent(hLayoutForHap);
		verLayout.setComponentAlignment(hLayoutForHap, Alignment.MIDDLE_CENTER);
		
		horizontalLayout.addComponent(verLayout);
		btnNext.setEnabled(true);
		//horizontalLayout.setComponentAlignment(hLayoutForHap, Alignment.BOTTOM_LEFT);
	}
	

}
