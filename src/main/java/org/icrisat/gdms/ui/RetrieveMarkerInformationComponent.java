package org.icrisat.gdms.ui;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.write.WriteException;

import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.dao.gdms.MarkerDetailsDAO;
import org.generationcp.middleware.dao.gdms.MarkerUserInfoDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.DartValues;
import org.generationcp.middleware.pojos.gdms.ExtendedMarkerInfo;
import org.generationcp.middleware.pojos.gdms.MarkerDetails;
import org.generationcp.middleware.pojos.gdms.MarkerInfo;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.generationcp.middleware.pojos.gdms.MarkerUserInfo;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
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

public class RetrieveMarkerInformationComponent implements Component.Listener{
	
	private static final long serialVersionUID = 1L;
	ManagerFactory factory;
	GenotypicDataManager genoManager;
	
	private GDMSMain _mainHomePage;
	protected String strMarkerType;
	private Session localSession;
	private Session centralSession;
	
	private MarkerDAO markerDAOLocal;
	private MarkerDAO markerDAOCentral;
	private MarkerUserInfoDAO markerUserInfoDAOLocal;	
	private MarkerUserInfoDAO markerUserInfoDAOCentral;
	private MarkerDetailsDAO markerDetailsDAOCentral;
	private MarkerDetailsDAO markerDetailsDAOLocal;
	
	private TabSheet _tabsheetForMarkers;
	private Component buildConditionsPanel;
	private Component buildResultsPanel;
	
	//private ArrayList<Marker> _finalListOfMarkers;
	private List<ExtendedMarkerInfo> _finalListOfMarkers;
	private ArrayList<MarkerUserInfo> _finalListOfMarkerInfo;
	private ArrayList<MarkerDetails> _finalListOfMarkerDetails;
	
	private Table[] arrayOfTables;
	
	private Table[] arrayOfTablesForExcel;
	
	private HashMap<Integer, java.math.BigInteger> hmGenotypeCount; 
	
	private ArrayList<String> finalListOfMarkerTypes;
	List intMarkers=new ArrayList();
	List intdatasetIds=new ArrayList();
	List<Name> results;
	String strMsg="";
	
	String option="";	
	private ComboBox selectMarkerType;
	String strSearchString = "";
	
	List<MarkerInfo> resMInfo;
	String clicked="no";
	
	List _markerIDsList;
	
	String strMarkerName="";
	
	private ArrayList<HashMap<String, String>> listOfDataRows=new ArrayList<HashMap<String, String>>();;
	
	public RetrieveMarkerInformationComponent(GDMSMain theMainHomePage) throws GDMSException{

		_mainHomePage = theMainHomePage;
		
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			
			genoManager=factory.getGenotypicDataManager();
			
			
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
		
		markerDAOLocal = new MarkerDAO();
		markerDAOLocal.setSession(localSession);
		markerDAOCentral = new MarkerDAO();
		markerDAOCentral.setSession(centralSession);

		markerUserInfoDAOLocal = new MarkerUserInfoDAO();
		markerUserInfoDAOLocal.setSession(localSession);
		markerUserInfoDAOCentral = new MarkerUserInfoDAO();
		markerUserInfoDAOCentral.setSession(centralSession);

		markerDetailsDAOLocal = new MarkerDetailsDAO();
		markerDetailsDAOLocal.setSession(localSession);
		markerDetailsDAOCentral = new MarkerDetailsDAO();
		markerDetailsDAOCentral.setSession(centralSession);

	}
	
	public HorizontalLayout buildTabbedComponentForMarker() throws GDMSException, MiddlewareQueryException {

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth("50%");

		_tabsheetForMarkers = new TabSheet();
		_tabsheetForMarkers.setWidth("50%");

		buildConditionsPanel = buildMarkerConditionsComponent();
		buildConditionsPanel.addListener(this);
		buildConditionsPanel.setWidth("50%");
		
		buildResultsPanel = buildMarkerResultsComponent();
		buildResultsPanel.addListener(this);
		buildResultsPanel.setWidth("50%");

		_tabsheetForMarkers.addComponent(buildConditionsPanel);
		_tabsheetForMarkers.addComponent(buildResultsPanel);
		
		_tabsheetForMarkers.getTab(1).setEnabled(false);

		horizontalLayout.addComponent(_tabsheetForMarkers);

		return horizontalLayout;
	}

	private Component buildMarkerConditionsComponent() {
		VerticalLayout finalConditionsLayout = new VerticalLayout();
		finalConditionsLayout.setCaption("Conditions");
		finalConditionsLayout.setMargin(true, true, true, true);	
		
		
		Label lblSearch = new Label("Search By Marker Name");
		lblSearch.setStyleName(Reindeer.LABEL_H2);
		finalConditionsLayout.addComponent(lblSearch);
		finalConditionsLayout.setComponentAlignment(lblSearch, Alignment.TOP_LEFT);
		
		Label lSearch = new Label("(Use '*' for wildcard search)");
		lSearch.setStyleName(Reindeer.LABEL_LIGHT);
		
		//finalConditionsLayout.addComponent(lSearch);
		//finalConditionsLayout.setComponentAlignment(lSearch, Alignment.TOP_LEFT);

		Label lblHap = new Label("Marker Name");
		lblHap.setWidth("70px");
		lblHap.setStyleName(Reindeer.LABEL_SMALL);

		final TextField txtFieldSearch = new TextField();
		txtFieldSearch.setWidth("300px");
		txtFieldSearch.setNullRepresentation("");
		
		ThemeResource themeResource = new ThemeResource("images/find-icon.png");
		Button searchButton = new Button();
		searchButton.setIcon(themeResource);	
		
		searchButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			
			public void buttonClick(ClickEvent event) {
				clicked="yes";
				strSearchString = txtFieldSearch.getValue().toString();
				strMarkerName=txtFieldSearch.getValue().toString();
				//System.out.println("22222222222222222222222:###:"+strSearchString);
				if (strSearchString.trim().equals("")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a search string.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				option="Marker-Name";				
				
				if (false == strSearchString.endsWith("*")){
					if (false == strSearchString.equals(""))
						strSearchString = strSearchString + "*";
				}
				
				try {
					Component newMarkerResultsPanel = buildMarkerResultsComponent();
					_tabsheetForMarkers.replaceComponent(buildResultsPanel, newMarkerResultsPanel);
					_tabsheetForMarkers.requestRepaint();
					buildResultsPanel = newMarkerResultsPanel;
					//20131206: Tulasi --- Set the next tab enabled to true to move to the next tab after clicking next
					_tabsheetForMarkers.getTab(1).setEnabled(true);
					_tabsheetForMarkers.setSelectedTab(1);

				} catch (GDMSException e) {
					e.printStackTrace();
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				} catch (MiddlewareQueryException e) {
					e.printStackTrace();
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}	
			
		});	
		
		
		HorizontalLayout horizontalLayoutForSelectComponents = new HorizontalLayout();
		horizontalLayoutForSelectComponents.addComponent(lblHap);
		horizontalLayoutForSelectComponents.addComponent(txtFieldSearch);
		horizontalLayoutForSelectComponents.addComponent(searchButton);
		horizontalLayoutForSelectComponents.setWidth("500px");
		horizontalLayoutForSelectComponents.setMargin(true, true, true, true);
		
		finalConditionsLayout.addComponent(horizontalLayoutForSelectComponents);
		
		Label lblor = new Label("(Or)");
		lblor.setStyleName(Reindeer.LABEL_H2);
		
		finalConditionsLayout.addComponent(lblor);
		finalConditionsLayout.setComponentAlignment(lblor, Alignment.MIDDLE_CENTER);
		
		Label lblAlleleSearch = new Label("Search By Marker Type");
		lblAlleleSearch.setStyleName(Reindeer.LABEL_H2);
		
		finalConditionsLayout.addComponent(lblAlleleSearch);
		finalConditionsLayout.setComponentAlignment(lblAlleleSearch, Alignment.TOP_LEFT);
		
		final List<String> listOfMarkerTypes = getListOfMarkerTypes();
		
		
		Label lblAllele = new Label("Marker Type");
		lblAllele.setWidth("70px");
		lblAllele.setStyleName(Reindeer.LABEL_SMALL);
		
		
		selectMarkerType = new ComboBox();
		Object itemId1 = selectMarkerType.addItem();
		selectMarkerType.setItemCaption(itemId1, "Select");
		//selectMarkerType.setWidth("300px");
		selectMarkerType.setValue(itemId1);
		selectMarkerType.setImmediate(true);
		selectMarkerType.setNullSelectionAllowed(false);
		for (int i = 0; i < listOfMarkerTypes.size(); i++){
			selectMarkerType.addItem(listOfMarkerTypes.get(i));
		}
		selectMarkerType.addItem("All");
		
		ThemeResource themeResourceA = new ThemeResource("images/find-icon.png");
		Button searchMTypeButton = new Button();
		searchMTypeButton.setIcon(themeResourceA);
		
		
		searchMTypeButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				clicked="yes";
				strSearchString = selectMarkerType.getValue().toString();
				option="Marker-Type";
				System.out.println("strSearchString=:"+strSearchString);
				
				if (strSearchString.trim().equals("1")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select Marker Type.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				try {
					Component newMarkerResultsPanel = buildMarkerResultsComponent();
					_tabsheetForMarkers.replaceComponent(buildResultsPanel, newMarkerResultsPanel);
					_tabsheetForMarkers.requestRepaint();
					buildResultsPanel = newMarkerResultsPanel;
					//20131206: Tulasi --- Set the next tab enabled to true to move to the next tab after clicking next
					_tabsheetForMarkers.getTab(1).setEnabled(true);
					_tabsheetForMarkers.setSelectedTab(1);

				} catch (GDMSException e) {
					e.printStackTrace();
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				} catch (MiddlewareQueryException e) {
					e.printStackTrace();
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}			
		});
		
		
		
		HorizontalLayout horizontalLayoutForMarkerTypes = new HorizontalLayout();		
		horizontalLayoutForMarkerTypes.addComponent(lblAllele);
		horizontalLayoutForMarkerTypes.addComponent(selectMarkerType);
		//horizontalLayoutForMarkerTypes.setComponentAlignment(selectMarkerType, Alignment.MIDDLE_LEFT);
		horizontalLayoutForMarkerTypes.addComponent(searchMTypeButton);		
		horizontalLayoutForMarkerTypes.setWidth("350px");
		horizontalLayoutForMarkerTypes.setMargin(true, true, true, true);		
		

		finalConditionsLayout.addComponent(horizontalLayoutForMarkerTypes);
		
		

		return finalConditionsLayout;
	}
	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}
	
	private List<String> getListOfMarkerTypes() {
		List<String> listOfMTypes = new ArrayList<String>();
		try{
			//System.out.println("................:"+genoManager.getAllMarkerTypes(0, 12));
			listOfMTypes=genoManager.getAllMarkerTypes(0, 12);
			
		}catch (MiddlewareQueryException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Marker Types from the database", Notification.TYPE_ERROR_MESSAGE);
			return null;
		}
		return listOfMTypes;
		
	}
	
	
	
	private Component buildMarkerResultsComponent() throws GDMSException, MiddlewareQueryException {
		
		VerticalLayout verticalLayout = new VerticalLayout();
		VerticalLayout resultsLayout = new VerticalLayout();
		verticalLayout.setCaption("Results");
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true);
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true, true, true, true);
		resultsLayout.setWidth("700px");
		resultsLayout.addStyleName(Reindeer.LAYOUT_WHITE);
		
		Table[] arrayOfTablesForMarkerResults = buildMarkerTable();
		if (null != arrayOfTablesForMarkerResults && arrayOfTablesForMarkerResults.length > 0){
			/*System.out.println("arrayOfTablesForMarkerResults  &&&&&&&&&&&&&&&&  :"+arrayOfTablesForMarkerResults);
			System.out.println("Length:"+arrayOfTablesForMarkerResults.length);*/
			
			for (int i = 0; i < arrayOfTablesForMarkerResults.length; i++) {
				
				final int j = i;
				
				arrayOfTablesForMarkerResults[i].setWidth("100%");
				arrayOfTablesForMarkerResults[i].setPageLength(10);
				arrayOfTablesForMarkerResults[i].setSelectable(true);
				arrayOfTablesForMarkerResults[i].setColumnCollapsingAllowed(true);
				arrayOfTablesForMarkerResults[i].setColumnReorderingAllowed(false);
				arrayOfTablesForMarkerResults[i].setStyleName("strong");
				
				int size = arrayOfTablesForMarkerResults[i].size();
				//System.out.println("size:"+size+"   "+arrayOfTablesForMarkerResults[j]);
				String strMarkerType = "";
				if (0 < finalListOfMarkerTypes.size()){
					//System.out.println("IF   &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
					strMarkerType = finalListOfMarkerTypes.get(i).toString();
					if (0 < size) {
						resultsLayout.addComponent(new Label("There are " + size +  " "  + strMarkerType + " markers in the below table."));
					}

				} else {
					//System.out.println("ELSE  ********************************************");
					//resultsLayout.addComponent(new Label("There are " + size + " markers in the below table."));
					//System.out.println("ELSE  ********************************************");
					if (0 < size) {
						resultsLayout.addComponent(new Label("There are " + size + " markers in the below table."));
					}
				}
				//System.out.println("%%%%%%%%%%%%%%%%%%   :"+arrayOfTablesForMarkerResults[0]);
				if (0 < size) {
					resultsLayout.addComponent(arrayOfTablesForMarkerResults[i]);
					resultsLayout.setComponentAlignment(arrayOfTablesForMarkerResults[i], Alignment.MIDDLE_CENTER);
					
					
					ThemeResource themeResource = new ThemeResource("images/excel.gif");
					Button excelButton = new Button();
					excelButton.setIcon(themeResource);
					excelButton.setStyleName(Reindeer.BUTTON_LINK);
					excelButton.setDescription(strMarkerType + "EXCEL Format");
					
					
					//System.out.println("$$$$$$$$$$$  :"+arrayOfTablesForMarkerResults[i]);
					HorizontalLayout layoutForExportTypes = new HorizontalLayout();
					layoutForExportTypes.setSpacing(true);
					
					layoutForExportTypes.addComponent(excelButton);
					if (strMarkerType.equalsIgnoreCase("snp")) {	
						ThemeResource themeResource1 = new ThemeResource("images/LGC_Genomics.gif");
						Button kbioButton = new Button();
						kbioButton.setIcon(themeResource1);
						kbioButton.setStyleName(Reindeer.BUTTON_LINK);
						kbioButton.setDescription("LGC Genomics Order form");
						layoutForExportTypes.addComponent(kbioButton);
						kbioButton.addListener(new ClickListener() {
							private static final long serialVersionUID = 1L;
							public void buttonClick(ClickEvent event) {
								ExportFileFormats exportFileFormats = new ExportFileFormats();
								String mType="SNP";
								ArrayList <String> markersForKBio=new ArrayList();	
								//System.out.println("_finalListOfMarkers.size():"+_finalListOfMarkers.size());
								if (1 <= _finalListOfMarkers.size()) {
									for (int i = 0; i < _finalListOfMarkers.size(); i++) {
										ExtendedMarkerInfo marker = _finalListOfMarkers.get(i);
										String markerName = marker.getMarkerName();
										String markerType=marker.getMarkerType();
										//System.out.println(markerName+"   "+markerType);
										if (markerType.equalsIgnoreCase("snp")){
											markersForKBio.add(markerName);
										}
									}
									
									
								} 
								//System.out.println("...........:"+_finalListOfMarkers);
								//System.out.println("markersForKBio:"+markersForKBio);
								try {
									//ArrayList<String> snpMarkers=(ArrayList<String>) genoManager.getMarkerNamesByMarkerType(mType, 0, (int)genoManager.countMarkerNamesByMarkerType(mType));
									if(!(markersForKBio.isEmpty())){
										if(!(markersForKBio.isEmpty())){
											File kbioOrderFormFile = exportFileFormats.exportToKBio(markersForKBio, _mainHomePage);
											FileResource fileResource = new FileResource(kbioOrderFormFile, _mainHomePage);
											//_mainHomePage.getMainWindow().getWindow().open(fileResource, "KBio Order Form", true);
											_mainHomePage.getMainWindow().getWindow().open(fileResource, "_self");
										}
									}else{
										_mainHomePage.getMainWindow().getWindow().showNotification("No SNP Marker(s) to create KBio Order form", Notification.TYPE_ERROR_MESSAGE);
										return;
									}
									
								} catch (Exception e) {
									_mainHomePage.getMainWindow().getWindow().showNotification("Error generating LGC Order Form", Notification.TYPE_ERROR_MESSAGE);
									return;
								}
								
								//exportFileFormats.exportToKBio(_markerTable, _mainHomePage);
							}
						});
						
						resultsLayout.addComponent(layoutForExportTypes);
						resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.BOTTOM_RIGHT);
						
					} else {
						resultsLayout.addComponent(excelButton);
						resultsLayout.setComponentAlignment(excelButton, Alignment.BOTTOM_RIGHT);
					}
					
					excelButton.addListener(new ClickListener() {
						private static final long serialVersionUID = 1L;
						public void buttonClick(ClickEvent event) {
							exportToExcel(j);
						}
					});
				}
				
			}
		}

		verticalLayout.addComponent(resultsLayout);
		return verticalLayout;
	}
	private void exportToExcel(int theTableIndex) {
		if(null == _finalListOfMarkers || 0 == _finalListOfMarkers.size()) {
			return;
		}
		//System.out.println("_finalListOfMarkers:"+_finalListOfMarkers);
		String strFileName = "Markers";

		ArrayList<String[]> listToExport = new ArrayList<String[]>();
		
		if (null != _finalListOfMarkers) {

			String strMarkerType = finalListOfMarkerTypes.get(theTableIndex);

			String[] strArrayOfColNames = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "GENOTYPE-COUNT","PRINCIPAL-INVESTIGATOR", "CONTACT",
					"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
					"ASSAY-TYPE", "MOTIF", "NO-OF-REPEATS", "MOTIF-TYPE", "SEQUENCE", "SEQUENCE-LENGTH",
					"MIN-ALLELE", "MAX-ALLELE", "SSR-NUMBER", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
					"FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
					"ELONGATION-TEMPERATURE", "FRAGMENT-SIZE-EXPECTED", "FRAGMENT-SIZE-OBSERVED", "AMPLIFICATION"};

			if (null != strMarkerType) {

				if (strMarkerType.equalsIgnoreCase("snp")) {

					String[] strSNParray = {"MARKER-ID", "MARKER-NAME", "MARKER_TYPE","GENOTYPE-COUNT", "ALIAS","PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
							"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE",  "PLOIDY", 
							"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
							"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
							"ANNEALING-TEMPERATURE",
					"POSITION-ON-REFERENCE-SEQUENCE"};

					strArrayOfColNames = strSNParray;

				} else if (strMarkerType.equalsIgnoreCase("cap")) {

					String[] strCAParray = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE","GENOTYPE-COUNT","PRIMER-ID", "ALIAS", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
							"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE",  "PLOIDY", 
							"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
							"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
							"RESTRICTED ENZYME FOR ASSAY", "ANNEALING-TEMPERATURE",
					"POSITION-ON-REFERENCE-SEQUENCE"};

					strArrayOfColNames = strCAParray;

				} else if (strMarkerType.equalsIgnoreCase("cisr")) {

					String[] strCISRarray = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "GENOTYPE-COUNT","PRIMER-ID", "PRINCIPAL-INVESTIGATOR", "CONTACT",
							"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
							"ASSAY-TYPE", "REPEAT", "NO-OF-REPEATS", "SEQUENCE", "SEQUENCE-LENGTH",
							"MIN-ALLELE", "MAX-ALLELE", "SIZE-OF-REPEAT-MOTIF", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
							"PRIMER-LENGTH", "FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
							"FRAGMENT-SIZE-EXPECTED", "AMPLIFICATION"};

					strArrayOfColNames = strCISRarray;
				} else if (strMarkerType.equalsIgnoreCase("dart")) {

					String[] strDARTarray = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "GENOTYPE-COUNT", "SPECIES", "CLONE ID", "Q VALUE", "REPRODUCIBILITY", "CALL RATE", "PIC VALUE", "DISCORDENCE"};

					strArrayOfColNames = strDARTarray;
				}

			}


			for (int i = 0; i < arrayOfTablesForExcel[theTableIndex].size(); i++) {

				Item item = arrayOfTablesForExcel[theTableIndex].getItem(i);

				String[] strArrayOfRowData = new String[strArrayOfColNames.length];

				for (int jCol = 0; jCol < strArrayOfColNames.length; jCol++) {
					
					if (null != item) {
						Property propertyFieldName = item.getItemProperty(strArrayOfColNames[jCol]);
						//System.out.println("propertyFieldName:"+propertyFieldName);
						if (null != propertyFieldName) {
							strArrayOfRowData[jCol] = propertyFieldName.toString();
						} else {
							strArrayOfRowData[jCol] = "";
						}
					}
				}

				listToExport.add(strArrayOfRowData);
			}
		}

		if(0 == listToExport.size()) {
			_mainHomePage.getMainWindow().getWindow().showNotification("No Markers to export",  Notification.TYPE_ERROR_MESSAGE);
			return;
		}


		String strMarkerType = finalListOfMarkerTypes.get(theTableIndex);

		String[] strArrayOfColNames = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "GENOTYPE-COUNT", "PRINCIPAL-INVESTIGATOR", "CONTACT",
				"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
				"ASSAY-TYPE", "MOTIF", "NO-OF-REPEATS", "MOTIF-TYPE", "SEQUENCE", "SEQUENCE-LENGTH",
				"MIN-ALLELE", "MAX-ALLELE", "SSR-NUMBER", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
				"FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
				"ELONGATION-TEMPERATURE", "FRAGMENT-SIZE-EXPECTED", "FRAGMENT-SIZE-OBSERVED", "AMPLIFICATION"};

		if (null != strMarkerType) {

			if (strMarkerType.equalsIgnoreCase("snp")) {

				String[] strSNParray = {"MARKER-ID", "MARKER-NAME","MARKER_TYPE", "GENOTYPE-COUNT", "ALIAS","PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
						"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
						"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
						"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
						"ANNEALING-TEMPERATURE",
				"POSITION-ON-REFERENCE-SEQUENCE"};

				strArrayOfColNames = strSNParray;

			} else if (strMarkerType.equalsIgnoreCase("cap")) {

				String[] strCAParray = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "GENOTYPE-COUNT","PRIMER-ID", "ALIAS", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
						"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "GENOTYPE-COUNT", "PLOIDY", 
						"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
						"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
						"RESTRICTED ENZYME FOR ASSAY", "ANNEALING-TEMPERATURE",
				"POSITION-ON-REFERENCE-SEQUENCE"};

				strArrayOfColNames = strCAParray;

			} else if (strMarkerType.equalsIgnoreCase("cisr")) {

				String[] strCISRarray = {"MARKER-ID", "MARKER-NAME","MARKER-TYPE","GENOTYPE-COUNT", "PRIMER-ID",  "PRINCIPAL-INVESTIGATOR", "CONTACT",
						"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE",  
						"ASSAY-TYPE", "REPEAT", "NO-OF-REPEATS", "SEQUENCE", "SEQUENCE-LENGTH",
						"MIN-ALLELE", "MAX-ALLELE", "SIZE-OF-REPEAT-MOTIF", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
						"PRIMER-LENGTH", "FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
						"FRAGMENT-SIZE-EXPECTED", "AMPLIFICATION"};

				strArrayOfColNames = strCISRarray;
			}else if (strMarkerType.equalsIgnoreCase("DART")) {

				String[] strDARTarray = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "GENOTYPE-COUNT", "SPECIES", "CLONE ID", "Q VALUE", "REPRODUCIBILITY", "CALL RATE", "PIC VALUE", "DISCORDENCE"};

				strArrayOfColNames = strDARTarray;
			}
		}

		listToExport.add(0, strArrayOfColNames);

		ExportFileFormats exportFileFormats = new ExportFileFormats();
		try {
			exportFileFormats.exportMap(_mainHomePage, listToExport, strFileName);
		} catch (WriteException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
		} catch (IOException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
		}

	}
	
	private Table[] buildMarkerTable() throws GDMSException, MiddlewareQueryException {		
		
		List<ExtendedMarkerInfo> tempListOfMarkers = null;
		ArrayList<MarkerUserInfo> tempListOfMarkerInfo = null;
		ArrayList<MarkerDetails> tempListOfMarkerDetails = null;

		_finalListOfMarkers = null;
		boolean bStartedFiltering = false;
		//System.out.println(listOfSelectedCriteria+" &&  size="+listOfSelectedCriteria.size());
		//if (null != listOfSelectedCriteria && listOfSelectedCriteria.size() > 0){
		if(clicked.equalsIgnoreCase("yes")){
			_finalListOfMarkers = new ArrayList<ExtendedMarkerInfo>();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			
			
			tempListOfMarkers = new ArrayList<ExtendedMarkerInfo>();
			tempListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
			tempListOfMarkerDetails = new ArrayList<MarkerDetails>();

			_finalListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
			_finalListOfMarkerDetails = new ArrayList<MarkerDetails>();

			_markerIDsList=new ArrayList();
			

		//"Marker-ID", "Marker-Name", "Marker-Type", "Accession-ID", "Genotype", "Annealing-Temperature", "Amplification"
		//"S.NO#:", "FIELD-NAME", "CONDITION", "FIELD-VALUE"
		
			if (option.toString().equals("Marker-Type")){	
				/*if(strSearchString.toString().equalsIgnoreCase("all")){					
					for (int j = 0; j < _finalListOfMarkers.size(); j++){
						//Marker marker = _finalListOfMarkers.get(j);
						ExtendedMarkerInfo marker = _finalListOfMarkers.get(j);
						Integer markerId = marker.getMarkerId();
					
						//if (marker.getMarkerType().equals(strSearchString.toString())){
							if (false == tempListOfMarkers.contains(marker)) {
								tempListOfMarkers.add(marker);
							}
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)) {
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)) {
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)){
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)){
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						//}						
					}
				}else{*/
					//genoManager.getMar
					tempListOfMarkers=genoManager.getMarkerInfoDataByMarkerType(strSearchString.toString());
					HashMap<String, String> hashMapOfDataRow = new HashMap<String, String>();	
					//genoManager.getMarkerInfoByMarkerNames(arg0)
					List MarkerIDs=new ArrayList();
					HashMap<Integer, String> hmMarkerNames=new HashMap<Integer, String>();
					HashMap<Integer, String> hmMarkerSpecies=new HashMap<Integer, String>();
					HashMap<Integer, BigInteger> hmMarkerGenotypeCounts=new HashMap<Integer, BigInteger>();
					System.out.println("****...........******:"+genoManager.getMarkerNamesByMarkerType(strSearchString.toString(), 0,  (int)genoManager.countMarkerNamesByMarkerType(strSearchString.toString())));
					 List<String> markerNames = genoManager.getMarkerNamesByMarkerType(strSearchString.toString(), 0,  (int)genoManager.countMarkerNamesByMarkerType(strSearchString.toString()));
					 //genoManager.getMarkersByMarkerNames(arg0, arg1, arg2, arg3)
					 System.out.println("**********:"+genoManager.getMarkerInfoByMarkerNames(markerNames));
					 List<ExtendedMarkerInfo> markers =genoManager.getMarkerInfoByMarkerNames(markerNames);
					 for(ExtendedMarkerInfo res:markers){
						 MarkerIDs.add(res.getMarkerId());
						 hmMarkerNames.put(res.getMarkerId(),res.getMarkerName());
						 hmMarkerGenotypeCounts.put(res.getMarkerId(), res.getGenotypesCount());
						 hmMarkerSpecies.put(res.getMarkerId(), res.getSpecies());
					 }
					System.out.println(",,,,,,,,,,,,,:"+genoManager.getDartMarkerDetails(MarkerIDs));
					List<DartValues> result =genoManager.getDartMarkerDetails(MarkerIDs);
					listOfDataRows=new ArrayList<HashMap<String, String>>();
					for(DartValues dResults: result){
						hashMapOfDataRow = new HashMap<String, String>();	
						System.out.println("...:"+dResults.getCloneId()+" : "+dResults.getqValue()+"   , :"+dResults.getReproducibility()+"   ,"+dResults.getCallRate()+"    ,"+dResults.getPicValue()+"  "+dResults.getDiscordance());
						hashMapOfDataRow.put("marker_id", dResults.getMarkerId().toString());
						hashMapOfDataRow.put("marker_name", hmMarkerNames.get(dResults.getMarkerId()));
						hashMapOfDataRow.put("species", hmMarkerSpecies.get(dResults.getMarkerId()));
						hashMapOfDataRow.put("genotypeCount", hmMarkerGenotypeCounts.get(dResults.getMarkerId()).toString());
						hashMapOfDataRow.put("CLONE ID", dResults.getCloneId().toString());
						hashMapOfDataRow.put("Q VALUE", dResults.getqValue().toString());
						hashMapOfDataRow.put("REPRODUCIBILITY", dResults.getReproducibility().toString());
						hashMapOfDataRow.put("CALL RATE", dResults.getCallRate().toString());
						hashMapOfDataRow.put("PIC VALUE", dResults.getMarkerId().toString());
						hashMapOfDataRow.put("DISCORDENCE", dResults.getDiscordance().toString());			
						
						
						listOfDataRows.add(hashMapOfDataRow);
					}
					//tempListOfMarkers
					bStartedFiltering = true;
			
					_finalListOfMarkers = tempListOfMarkers;					
					
					tempListOfMarkers = new ArrayList<ExtendedMarkerInfo>();
					
		} 
			System.out.println("_finalListOfMarkers:"+listOfDataRows);

		if (option.toString().equals("Marker-Name")){
			
			//20131206: Tulasi --- Added code to display Markers based on initial string given by the user
			String strFieldValue = strSearchString;
			String strInitialChars = "";
			
			System.out.println(strSearchString+":     :"+strMarkerName+" **********************:"+genoManager.getMarkerInfoDataLikeMarkerName(strMarkerName));
				
			
			
			if (strFieldValue.equals("*")) {
				/*if (false == tempListOfMarkers.contains(marker)){
					tempListOfMarkers.add(marker);
				}*/
				if (!bStartedFiltering){
					bStartedFiltering = true;
				}
				List<MarkerUserInfo> listOfAllMarkerUserInfoLocal = markerUserInfoDAOLocal.getAll();
				if (null != listOfAllMarkerUserInfoLocal && false == tempListOfMarkerInfo.contains(listOfAllMarkerUserInfoLocal)){
					tempListOfMarkerInfo.addAll(listOfAllMarkerUserInfoLocal);
				}
				List<MarkerUserInfo> listOfAllMarkerUserInfoCentral = markerUserInfoDAOCentral.getAll();
				if (null != listOfAllMarkerUserInfoCentral && false == tempListOfMarkerInfo.contains(listOfAllMarkerUserInfoCentral)){
					tempListOfMarkerInfo.addAll(listOfAllMarkerUserInfoCentral);
				}
				
				List<MarkerDetails> listOfAllMarkerDetailsLocal = markerDetailsDAOLocal.getAll();
				if (null != listOfAllMarkerDetailsLocal && false == tempListOfMarkerDetails.contains(listOfAllMarkerDetailsLocal)){
					tempListOfMarkerDetails.addAll(listOfAllMarkerDetailsLocal);
				}
				List<MarkerDetails> listOfAllMarkerDetailsCentral = markerDetailsDAOCentral.getAll();
				if (null != listOfAllMarkerDetailsCentral && false == tempListOfMarkerDetails.contains(listOfAllMarkerDetailsCentral)){
					tempListOfMarkerDetails.addAll(listOfAllMarkerDetailsCentral);
				}
			}else{
			
				if (strFieldValue.endsWith("*")) {
					int indexOfAstrisk = strFieldValue.indexOf("*");
					strInitialChars = strFieldValue.substring(0, indexOfAstrisk);
				}
				
				if (strFieldValue.startsWith("*")) {
					int indexOfAstrisk = strFieldValue.indexOf("*");
					strInitialChars = strFieldValue.substring(indexOfAstrisk, strFieldValue.length());
				}
				
				if (strFieldValue.endsWith("*") &&  strFieldValue.startsWith("*")) {
					int indexOfAstrisk = strFieldValue.indexOf("*");
					strInitialChars = strFieldValue.substring(indexOfAstrisk+1,strFieldValue.length()-1);
				}
				tempListOfMarkers=genoManager.getMarkerInfoDataLikeMarkerName(strMarkerName);
				bStartedFiltering=true;
			}
			//System.out.println("strInitialChars:"+strInitialChars);
			
			//if (false == strInitialChars.equals("")) {
			/*	for (int j = 0; j < _finalListOfMarkers.size(); j++){
					Marker marker = _finalListOfMarkers.get(j);
					Integer markerId = marker.getMarkerId();
					//_markerIDsList.add(markerId);
					//String 
					if (strFieldValue.endsWith("*")) {
					
						if (marker.getMarkerName().toLowerCase().startsWith(strInitialChars.toLowerCase())) {
							//System.out.println(marker.getMarkerName()+"   "+strInitialChars);
	
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
	
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							//genoManager.getMarkerInfoByMarkerName(arg0, arg1, arg2)
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)) {
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)) {
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)) {
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)) {
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						}
					}
					
					if (strFieldValue.startsWith("*")) {
						if (marker.getMarkerName().endsWith(strInitialChars)) {
	
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
	
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							//genoManager.getMarkerInfoByMarkerName(arg0, arg1, arg2)
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)) {
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)) {
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)) {
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)) {
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						}
					}
					if (strFieldValue.endsWith("*") &&  strFieldValue.startsWith("*")) {
						if (marker.getMarkerName().contains(strInitialChars)) {
	
							if (false == tempListOfMarkers.contains(marker)){
								tempListOfMarkers.add(marker);
							}
	
							if (!bStartedFiltering){
								bStartedFiltering = true;
							}
							//genoManager.getMarkerInfoByMarkerName(arg0, arg1, arg2)
							MarkerUserInfo markerInfoByIdLocal = markerUserInfoDAOLocal.getById(markerId, true);
							if (null != markerInfoByIdLocal && false == tempListOfMarkerInfo.contains(markerInfoByIdLocal)) {
								tempListOfMarkerInfo.add(markerInfoByIdLocal);
							}
							MarkerUserInfo markerInfoByIdCentral = markerUserInfoDAOCentral.getById(markerId, true);
							if (null != markerInfoByIdCentral && false == tempListOfMarkerInfo.contains(markerInfoByIdCentral)) {
								tempListOfMarkerInfo.add(markerInfoByIdCentral);
							}
							
							MarkerDetails markerDetailsIDLocal = markerDetailsDAOLocal.getById(markerId, true);
							if (null != markerDetailsIDLocal && false == tempListOfMarkerDetails.contains(markerDetailsIDLocal)) {
								tempListOfMarkerDetails.add(markerDetailsIDLocal);
							}
							MarkerDetails markerDetailsIDCentral = markerDetailsDAOCentral.getById(markerId, true);
							if (null != markerDetailsIDCentral && false == tempListOfMarkerDetails.contains(markerDetailsIDCentral)) {
								tempListOfMarkerDetails.add(markerDetailsIDCentral);
							}
						}
					}
				}*/
			//}
			
			/*System.out.println("DEatils L:"+tempListOfMarkerDetails);
			System.out.println("Info=:"+tempListOfMarkerInfo);
			*/
			
			if (bStartedFiltering){
				_finalListOfMarkers = tempListOfMarkers;
				_finalListOfMarkerInfo = tempListOfMarkerInfo;
				_finalListOfMarkerDetails = tempListOfMarkerDetails;

				tempListOfMarkers = new ArrayList<ExtendedMarkerInfo>();
				tempListOfMarkerInfo = new ArrayList<MarkerUserInfo>();
				tempListOfMarkerDetails = new ArrayList<MarkerDetails>();
			}
		}
		
		//System.out.println( _finalListOfMarkers.size()+"_finalListOfMarkers:"+_finalListOfMarkers);
		
		if (null != _finalListOfMarkers) {
			//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSFDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
			//System.out.println("hmGenotypeCount:"+hmGenotypeCount);
			if (0 == _finalListOfMarkers.size()){
				_mainHomePage.getMainWindow().getWindow().showNotification("No markers for the selected criteria.",  Notification.TYPE_HUMANIZED_MESSAGE);
				return null;
			}

			finalListOfMarkerTypes = new ArrayList<String>();
			if (1 <= _finalListOfMarkers.size()) {
				for (int i = 0; i < _finalListOfMarkers.size(); i++) {
					ExtendedMarkerInfo marker = _finalListOfMarkers.get(i);
					String markerType = marker.getMarkerType();
					if (false == finalListOfMarkerTypes.contains(markerType)){
						finalListOfMarkerTypes.add(markerType);
					}
				}
				//System.out.println("finalListOfMarkerTypes:"+finalListOfMarkerTypes);
				arrayOfTables = new Table[finalListOfMarkerTypes.size()];
				arrayOfTablesForExcel= new Table[finalListOfMarkerTypes.size()];
				
			} else {
				arrayOfTables = new Table[1];
				arrayOfTablesForExcel= new Table[1];
			}

			//System.out.println("arrayOfTables:"+arrayOfTables.length);
			
			for (int iTableCntr = 0; iTableCntr < arrayOfTables.length; iTableCntr++) {
				//System.out.println(iTableCntr+"   "+finalListOfMarkerTypes.get(iTableCntr));
				String strMarkerType = null;
				if (1 <= arrayOfTables.length){
					strMarkerType = finalListOfMarkerTypes.get(iTableCntr);
				} 
				
				arrayOfTables[iTableCntr] = new Table(); 
				arrayOfTables[iTableCntr].setStyleName("markertable");
				arrayOfTables[iTableCntr].setWidth("50%");
				arrayOfTables[iTableCntr].setPageLength(10);
				arrayOfTables[iTableCntr].setSelectable(true);
				arrayOfTables[iTableCntr].setColumnCollapsingAllowed(true);
				arrayOfTables[iTableCntr].setColumnReorderingAllowed(true);
				
				
				arrayOfTablesForExcel[iTableCntr] = new Table();
				arrayOfTablesForExcel[iTableCntr].setStyleName("markertable");
				arrayOfTablesForExcel[iTableCntr].setWidth("50%");
				arrayOfTablesForExcel[iTableCntr].setPageLength(10);
				arrayOfTablesForExcel[iTableCntr].setSelectable(true);
				arrayOfTablesForExcel[iTableCntr].setColumnCollapsingAllowed(true);
				arrayOfTablesForExcel[iTableCntr].setColumnReorderingAllowed(true);

				String[] strArrayOfColNames = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE",  "GENOTYPE-COUNT","PRINCIPAL-INVESTIGATOR", "CONTACT",
						"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
						"ASSAY-TYPE", "MOTIF", "NO-OF-REPEATS", "MOTIF-TYPE", "SEQUENCE", "SEQUENCE-LENGTH",
						"MIN-ALLELE", "MAX-ALLELE", "SSR-NUMBER", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
						"FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
						"ELONGATION-TEMPERATURE", "FRAGMENT-SIZE-EXPECTED", "FRAGMENT-SIZE-OBSERVED", "AMPLIFICATION"};
				
				if (null != strMarkerType) {
					
					if (strMarkerType.equalsIgnoreCase("snp")) {
						
						//"Marker Name","Alias (comma separated for multiple names)","Crop","Genotype","Ploidy","GID","Principal Investigator","Contact","Institute","Incharge Person","Assay Type",
						//"Forward Primer","Reverse Primer","Product Size","Expected Product Size","Position on Refrence Sequence","Motif","Annealing Temperature","Sequence","Reference"
						
						//Marker Name	Alias (comma separated for multiple names)	Crop	Genotype	Ploidy	GID	Principal Investigator	Contact	Institute	Incharge Person	Assay Type	Forward Primer	Reverse Primer	Product Size	Expected Product Size	Position on Refrence Sequence	Motif	Annealing Temperature	Sequence	Reference
						
						String[] strSNParray = {"MARKER-ID", "MARKER-NAME", "MARKER_TYPE","GENOTYPE-COUNT","ALIAS", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
								"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE",  "PLOIDY", 
								"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
								"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
								"ANNEALING-TEMPERATURE",
						"POSITION-ON-REFERENCE-SEQUENCE"};
						
						strArrayOfColNames = strSNParray;
						
					} else if (strMarkerType.equalsIgnoreCase("cap")) {
						
						//Marker Name	Primer ID	Alias (comma separated for multiple names)	Crop	Genotype	Ploidy	GID	Principal Investigator	Contact	Institute	Incharge Person	Assay Type	Forward Primer	Reverse Primer	Product Size	Expected Product Size	Restriction enzyme for assay	Position on Refrence Sequence	Motif	Annealing Temperature	Sequence	Reference	Remarks
						
						String[] strCAParray = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "GENOTYPE-COUNT", "PRIMER-ID", "ALIAS", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
								"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
								"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
								"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
								"RESTRICTED ENZYME FOR ASSAY", "ANNEALING-TEMPERATURE",
						"POSITION-ON-REFERENCE-SEQUENCE"};
						
						strArrayOfColNames = strCAParray;
						
					} else if (strMarkerType.equalsIgnoreCase("cisr")) {
						
						//Marker Name	Primer ID	Alias (comma separated for multiple names)	Crop	Genotype	Ploidy	GID	Principal Investigator	Contact	Institute	Incharge Person	Assay Type	Repeat	No of Repeats	Sequence	Sequence Length	Min Allele	Max Allele	Size of Repeat Motif	Forward Primer	Reverse Primer	Product Size	
						//Primer Length	Forward Primer Temperature	Reverse Primer 
						//Temperature	Annealing Temperature	Fragment Size Expected	Amplification	Reference	Remarks
						
						String[] strCISRarray = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "GENOTYPE-COUNT", "PRIMER-ID", "PRINCIPAL-INVESTIGATOR", "CONTACT",
								"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
								"ASSAY-TYPE", "REPEAT", "NO-OF-REPEATS", "SEQUENCE", "SEQUENCE-LENGTH",
								"MIN-ALLELE", "MAX-ALLELE", "SIZE-OF-REPEAT-MOTIF", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
								"PRIMER-LENGTH", "FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
								"FRAGMENT-SIZE-EXPECTED", "AMPLIFICATION"};
						
						strArrayOfColNames = strCISRarray;
					}else if (strMarkerType.equalsIgnoreCase("dart")) {			
						
						String[] strDARTarray = {"MARKER-ID", "MARKER-NAME", "MARKER-TYPE", "GENOTYPE-COUNT", "SPECIES", "CLONE ID", "Q VALUE", "REPRODUCIBILITY", "CALL RATE", "PIC VALUE", "DISCORDENCE"};
						
						strArrayOfColNames = strDARTarray;
					}
					
					
				}
				
				
				
				

				for (int i = 0; i < strArrayOfColNames.length; i++){
					if (1 == i){
						arrayOfTables[iTableCntr].addContainerProperty(strArrayOfColNames[i], Link.class, null);
					}else if(3 == i){
						arrayOfTables[iTableCntr].addContainerProperty(strArrayOfColNames[i], Button.class, null);
					}else{
						arrayOfTables[iTableCntr].addContainerProperty(strArrayOfColNames[i], String.class, null);
					}
					
					arrayOfTablesForExcel[iTableCntr].addContainerProperty(strArrayOfColNames[i], String.class, null);
				}
				
				
				
				if(listOfDataRows.isEmpty()){
				
				
				
						for (int i = 0; i < _finalListOfMarkers.size(); i++){
							
							ExtendedMarkerInfo marker = _finalListOfMarkers.get(i);
							
							if (null != strMarkerType) {
								if (false == strMarkerType.equals(marker.getMarkerType())){
									continue;
								}
							}
											
							
							final String markerId = String.valueOf(marker.getMarkerId());
							
							final String markerName = marker.getMarkerName();
							String markerType = marker.getMarkerType();
							String reference = marker.getReference();
							String species = marker.getSpecies();
							
							String dbAccessionId = marker.getAccessionId();
							String genotype = marker.getGenotype();
							//String assayType = marker.getAssayType(); marker.getMotifType()
							String assayType = marker.getMotifType();
							String motif = marker.getMotif();
							String forwardPrimer = marker.getForwardPrimer();
							String reversePrimer = marker.getReversePrimer();
							String productSize = marker.getProductSize();
							
							String annealingTemp = "";
							if (null != marker.getAnnealingTemp()) {
								annealingTemp = String.valueOf(marker.getAnnealingTemp());
							}
							
							String amplification = marker.getAmplification();
							String ploidy = marker.getPloidy();
							//String primerID = marker.getPrimerId();
							String primerID = marker.getMarkerId()+"";
							
							String principalInvestigator = "";
							String contact = "";
							String institute = "";
							//if (null != markerUserInfo){
								principalInvestigator = marker.getPrincipalInvestigator();
								contact = marker.getContact();
								institute = marker.getInstitute();
							//}
							
							String elongationTemp = "0.0";
							String fragmentSizeExpected = "";
							String fragmentSizeObserved = "0";
							String forwardPrimerTemp = "0.0";
							String reversePrimerTemp = "0.0";
							String noOfRepeats = "0";
							String motifType = "";
							String sequence = "";
							String sequenceLength = "0";
							String minAllele = "0";
							String maxAllele = "0";
							String ssrNr = "0";
							String positionOnReferenceSequence = "0";
							String expectedproductsize = "0";
							String restrictedenzymeforassay = "";
							//if (null != markerDetails){
								
								if (null != marker.getElongationTemp()) {
									elongationTemp = String.valueOf(marker.getElongationTemp());
								}
								
								if (null != marker.getFragmentSizeExpected()) {
									fragmentSizeExpected = String.valueOf(marker.getFragmentSizeExpected());
								}
								
								if (null != marker.getFragmentSizeObserved()) {
									fragmentSizeObserved = String.valueOf(marker.getFragmentSizeObserved());
								}
								
								if (null != marker.getForwardPrimerTemp()){
									forwardPrimerTemp = String.valueOf(marker.getForwardPrimerTemp());
								}
								
								if (null != marker.getReversePrimerTemp()) {
									reversePrimerTemp = String.valueOf(marker.getReversePrimerTemp());
								}
								
								if (null != marker.getNumberOfRepeats()) {
									noOfRepeats = String.valueOf(marker.getNumberOfRepeats());
								}
								
								motifType = marker.getMotifType();
								sequence = marker.getSequence();
								
								if (null != marker.getSequenceLength()){
									sequenceLength = String.valueOf(marker.getSequenceLength());
								}
								
								if (null != marker.getMinAllele()){
									minAllele = String.valueOf(marker.getMinAllele());
								}
								
								if (null != marker.getMaxAllele()) {
									maxAllele = String.valueOf(marker.getMaxAllele());
								}
								
								if (null != marker.getSsrNumber()) {
									ssrNr = String.valueOf(marker.getSsrNumber());
								}
								//marker.getPositionOnSequence()
								if (null != marker.getPositionOnSequence()){
									positionOnReferenceSequence = String.valueOf(marker.getPositionOnSequence());
								}
								
								if (null != marker.getExpectedProductSize()) {
									expectedproductsize = String.valueOf(marker.getExpectedProductSize());
								}
								restrictedenzymeforassay = marker.getRestrictionEnzyme();
							//}
							
							//20131216: Tulasi
							String genotypeCount="0";
							String gCount1=" ";
							String alias = "";
							String inchargeperson = "";
							String repeat = "";
							String sizeOfRepeatMotif = "";
							String primerLength = "";
							strMarkerType=markerType;
							intMarkers=new ArrayList();
							intdatasetIds=new ArrayList();
							if (null != marker.getGenotypesCount()){
							
								genotypeCount=marker.getGenotypesCount()+"";
							}else
								genotypeCount="";
							String cmapPath="http://cmap.icrisat.ac.in/cgi-bin/cmap_public/feature_search?features="+markerName.toString()+"&search_field=feature_name&order_by=&data_source=CMAP_PUBLIC&submit=Submit";
											//"http://cmap.icrisat.ac.in/cgi-bin/cmap_public/feature_search?features=" +strQTLName + "&order_by=&data_source=CMAP_PUBLIC";
							Link cmapSiteLink = new Link(markerName.toString(), new ExternalResource(cmapPath));
							cmapSiteLink.setTargetName("_blank");
							
							gCount1=genotypeCount+"";
							
							Button genotypeCountLink = new Button();
							//if(!gCount.isEmpty()){
								genotypeCountLink.setCaption(gCount1);
								genotypeCountLink.setStyleName(Reindeer.BUTTON_LINK);
								genotypeCountLink.setDescription(gCount1);
								genotypeCountLink.addListener(new Button.ClickListener() {
									
									private static final long serialVersionUID = 1L;
			
									@Override
									public void buttonClick(ClickEvent event) {
										//QtlDetailsDAO qtlDetailsDAO = new QtlDetailsDAO();
										//System.out.println("markerName:"+markerId);
										intMarkers.add(markerId);
										try{
											//System.out.println(genoManager.getAllFromMarkerMetadatasetByMarkers(intMarkers));
											List<MarkerMetadataSet> res=genoManager.getAllFromMarkerMetadatasetByMarkers(intMarkers);
											for(MarkerMetadataSet resM: res){
												intdatasetIds.add(resM.getDatasetId());
											}
											List<Integer> nIdList =genoManager.getNIdsByMarkerIdsAndDatasetIds(intdatasetIds, intMarkers, 0,  genoManager.countNIdsByMarkerIdsAndDatasetIds(intdatasetIds, intMarkers));
											//System.out.println(genoManager.getNamesByNameIds(nIdList));
											/*List<Integer> nIdList = manager.getNIdsByMarkerIdsAndDatasetIds(datasetIds, markerIds, 0, 
									                manager.countNIdsByMarkerIdsAndDatasetIds(datasetIds, markerIds));*/
											results = genoManager.getNamesByNameIds(nIdList);
											Window messageWindow = new Window("Lines ");
											GDMSDialog gdmsMessageWindow = new GDMSDialog(_mainHomePage,results,strMsg);
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
										}
									}
								});		
						
							//}
							
							if (strMarkerType.equalsIgnoreCase("ssr") || strMarkerType.equalsIgnoreCase("ua")) {
								
								Object[] strDataArray = {markerId, cmapSiteLink, markerType,genotypeCountLink, principalInvestigator,
										contact, institute, reference, species, dbAccessionId,
										genotype, assayType, motif, noOfRepeats, motifType,
										sequence, sequenceLength, minAllele, maxAllele, 
										ssrNr, forwardPrimer, reversePrimer, productSize,
										forwardPrimerTemp, reversePrimerTemp, annealingTemp,
										elongationTemp, fragmentSizeExpected, fragmentSizeObserved, amplification
								};
										
								if (null != strDataArray) {
									arrayOfTables[iTableCntr].addItem(strDataArray, new Integer(i));
									
								}
								
								Object[] strDataArrayExcel = {markerId, markerName, markerType,genotypeCount, principalInvestigator,
										contact, institute, reference, species, dbAccessionId,
										genotype, assayType, motif, noOfRepeats, motifType,
										sequence, sequenceLength, minAllele, maxAllele, 
										ssrNr, forwardPrimer, reversePrimer, productSize,
										forwardPrimerTemp, reversePrimerTemp, annealingTemp,
										elongationTemp, fragmentSizeExpected, fragmentSizeObserved, amplification
								};
										
								if (null != strDataArrayExcel) {
									arrayOfTablesForExcel[iTableCntr].addItem(strDataArrayExcel, new Integer(i));
									
								}
								
								
							} else if (strMarkerType.equalsIgnoreCase("snp")) { 
								//System.out.println(markerId+","+markerName+","+ cmapSiteLink+","+ alias+","+markerType+","+principalInvestigator+","+contact+","+ inchargeperson+","+ institute+","+ reference+","+ species+","+dbAccessionId+","+genotype+","+genotypeCount+","+ploidy+","+assayType+","+ sequence+","+motif+","+ forwardPrimer+","+reversePrimer+","+ productSize+","+ expectedproductsize+","+annealingTemp+","+positionOnReferenceSequence);
								/*String[] strSNParray = {"MARKER-ID", "MARKER-NAME", "ALIAS","MARKER_TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
										"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
										"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
										"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
										"ANNEALING-TEMPERATURE",
								"POSITION-ON-REFERENCE-SEQUENCE"};*/
								
								Object[] strDataArray = {markerId, cmapSiteLink, markerType,genotypeCountLink, alias, principalInvestigator,
									contact, inchargeperson, institute, reference, species, dbAccessionId,
									genotype, ploidy, assayType, sequence, motif, forwardPrimer,
									reversePrimer, productSize, expectedproductsize, annealingTemp, positionOnReferenceSequence
							    };
								
								if (null != strDataArray) {
									arrayOfTables[iTableCntr].addItem(strDataArray, new Integer(i));
								}
								
								Object[] strDataArrayExcel = {markerId, markerName, markerType,genotypeCount,alias, principalInvestigator,
										contact, inchargeperson, institute, reference, species, dbAccessionId,
										genotype, ploidy, assayType, sequence, motif, forwardPrimer,
										reversePrimer, productSize, expectedproductsize, annealingTemp, positionOnReferenceSequence
								};
										
								if (null != strDataArrayExcel) {
									arrayOfTablesForExcel[iTableCntr].addItem(strDataArrayExcel, new Integer(i));
									
								}
								
								
							} else if (strMarkerType.equalsIgnoreCase("cap")) { 
								
								/*String[] strCAParray = {"MARKER-ID", "MARKER-NAME", "PRIMER-ID", "ALIAS", "PRINCIPAL-INVESTIGATOR", "CONTACT", "INCHARGE-PERSON",
										"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", "PLOIDY", 
										"ASSAY-TYPE",  "SEQUENCE", "MOTIF",
										"FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",  "EXPECTED-PRODUCT-SIZE",
										"RESTRICTED ENZYME FOR ASSAY", "ANNEALING-TEMPERATURE",
								"POSITION-ON-REFERENCE-SEQUENCE"};*/
								
								Object[] strDataArray = {markerId, cmapSiteLink, markerType, genotypeCount,primerID, alias, principalInvestigator,
										contact, inchargeperson, institute, reference, species, dbAccessionId,
										genotype, ploidy, assayType, sequence, motif, forwardPrimer,
										reversePrimer, productSize, expectedproductsize, restrictedenzymeforassay,
										annealingTemp
								};
								
								if (null != strDataArray) {
									arrayOfTables[iTableCntr].addItem(strDataArray, new Integer(i));
								}
								Object[] strDataArrayExcel = {markerId, markerName, markerType,genotypeCount, primerID, alias, principalInvestigator,
										contact, inchargeperson, institute, reference, species, dbAccessionId,
										genotype, ploidy, assayType, sequence, motif, forwardPrimer,
										reversePrimer, productSize, expectedproductsize, restrictedenzymeforassay,
										annealingTemp
								};
										
								if (null != strDataArrayExcel) {
									arrayOfTablesForExcel[iTableCntr].addItem(strDataArrayExcel, new Integer(i));
									
								}
								
								
								
							} else if (strMarkerType.equalsIgnoreCase("cisr")) { 
		
								/*String[] strCISRarray = {"MARKER-ID", "MARKER-NAME", "PRIMER-ID", "MARKER-TYPE", "PRINCIPAL-INVESTIGATOR", "CONTACT",
										"INSTITUTE", "REFERENCE", "SPECIES", "ACCESSION-ID", "GENOTYPE", 
										"ASSAY-TYPE", "REPEAT", "NO-OF-REPEATS", "SEQUENCE", "SEQUENCE-LENGTH",
										"MIN-ALLELE", "MAX-ALLELE", "SIZE-OF-REPEAT-MOTIF", "FORWARD-PRIMER", "REVERSE-PRIMER", "PRODUCT-SIZE",
										"PRIMER-LENGTH", "FORWARD-PRIMER-TEMPERATURE", "REVERSE-PRIMER-TEMPERATURE", "ANNEALING-TEMPERATURE",
										"FRAGMENT-SIZE-EXPECTED", "AMPLIFICATION"};*/
								Object[] strDataArray = {markerId, cmapSiteLink,  markerType, genotypeCount, primerID,principalInvestigator,
										contact, institute, reference, species, dbAccessionId,
										genotype, assayType, repeat, noOfRepeats,
										sequence, sequenceLength, minAllele, maxAllele, 
										sizeOfRepeatMotif, forwardPrimer, reversePrimer, productSize,
										primerLength, forwardPrimerTemp, reversePrimerTemp, annealingTemp, fragmentSizeExpected, amplification
								};
								
								if (null != strDataArray) {
									arrayOfTables[iTableCntr].addItem(strDataArray, new Integer(i));
								}
								
								Object[] strDataArrayExcel = {markerId, markerName, markerType,genotypeCount,primerID,principalInvestigator,
										contact, institute, reference, species, dbAccessionId,
										genotype, assayType, repeat, noOfRepeats,
										sequence, sequenceLength, minAllele, maxAllele, 
										sizeOfRepeatMotif, forwardPrimer, reversePrimer, productSize,
										primerLength, forwardPrimerTemp, reversePrimerTemp, annealingTemp, fragmentSizeExpected, amplification
								};
										
								if (null != strDataArrayExcel) {
									arrayOfTablesForExcel[iTableCntr].addItem(strDataArrayExcel, new Integer(i));
									
								}
							}else if (strMarkerType.equalsIgnoreCase("DART")) { 
								/*System.out.println(".....................");
								hashMapOfDataRow.put("species", hmMarkerSpecies.get(dResults.getMarkerId()));
								hashMapOfDataRow.put("genotypeCount", hmMarkerGenotypeCounts.get(dResults.getMarkerId()).toString());
								hashMapOfDataRow.put("CLONE ID", dResults.getCloneId().toString());
								hashMapOfDataRow.put("Q VALUE", dResults.getqValue().toString());
								hashMapOfDataRow.put("REPRODUCIBILITY", dResults.getReproducibility().toString());
								hashMapOfDataRow.put("CALL RATE", dResults.getCallRate().toString());
								hashMapOfDataRow.put("PIC VALUE", dResults.getMarkerId().toString());
								hashMapOfDataRow.put("DISCORDENCE", dResults.getDiscordance().toString());	*/
								
								for(int m=0; m<listOfDataRows.size();m++){
									Map<String, String> hashMapOfDataRow = listOfDataRows.get(m);
									final String marker_Id=hashMapOfDataRow.get("marker_id");
									String mName=hashMapOfDataRow.get("marker_name"); 
									String marker_Type="DArT";
									String genotype_count=hashMapOfDataRow.get("genotypeCount");
									String strSpecies=hashMapOfDataRow.get("species");
									String clone_id=hashMapOfDataRow.get("CLONE ID");
									String qValue=hashMapOfDataRow.get("Q VALUE");
									String reproducibility=hashMapOfDataRow.get("REPRODUCIBILITY");
									String call_rate=hashMapOfDataRow.get("CALL RATE");
									String pic_value=hashMapOfDataRow.get("PIC VALUE");
									String discordence=hashMapOfDataRow.get("DISCORDENCE");
									String cmapPath1="http://cmap.icrisat.ac.in/cgi-bin/cmap_public/feature_search?features="+mName.toString()+"&search_field=feature_name&order_by=&data_source=CMAP_PUBLIC&submit=Submit";
									//"http://cmap.icrisat.ac.in/cgi-bin/cmap_public/feature_search?features=" +strQTLName + "&order_by=&data_source=CMAP_PUBLIC";
									Link cmapSiteLink1 = new Link(markerName.toString(), new ExternalResource(cmapPath1));
									cmapSiteLink1.setTargetName("_blank");
									
									
									
									System.out.println("..:"+mName);
									Object[] strDataArray = {marker_Id, cmapSiteLink1, marker_Type, genotypeCountLink, strSpecies, clone_id, qValue, reproducibility, call_rate, pic_value, discordence};
									
									if (null != strDataArray) {
										arrayOfTables[iTableCntr].addItem(strDataArray, new Integer(i));
									}
									
									Object[] strDataArrayExcel = {marker_Id, mName, marker_Type, genotype_count, strSpecies, clone_id, qValue, reproducibility, call_rate, pic_value, discordence};
										
									if (null != strDataArrayExcel) {
										arrayOfTablesForExcel[iTableCntr].addItem(strDataArrayExcel, new Integer(i));
										
									}
								}
							}
						}
				}else{
					if (strMarkerType.equalsIgnoreCase("DART")) { 						
						
						for(int i=0; i<listOfDataRows.size();i++){
							Map<String, String> hashMapOfDataRow = listOfDataRows.get(i);
							final String marker_Id=hashMapOfDataRow.get("marker_id");
							String mName=hashMapOfDataRow.get("marker_name"); 
							String marker_Type="DArT";
							String genotype_count=hashMapOfDataRow.get("genotypeCount");
							String strSpecies=hashMapOfDataRow.get("species");
							String clone_id=hashMapOfDataRow.get("CLONE ID");
							String qValue=hashMapOfDataRow.get("Q VALUE");
							String reproducibility=hashMapOfDataRow.get("REPRODUCIBILITY");
							String call_rate=hashMapOfDataRow.get("CALL RATE");
							String pic_value=hashMapOfDataRow.get("PIC VALUE");
							String discordence=hashMapOfDataRow.get("DISCORDENCE");
							String cmapPath1="http://cmap.icrisat.ac.in/cgi-bin/cmap_public/feature_search?features="+mName.toString()+"&search_field=feature_name&order_by=&data_source=CMAP_PUBLIC&submit=Submit";
							//"http://cmap.icrisat.ac.in/cgi-bin/cmap_public/feature_search?features=" +strQTLName + "&order_by=&data_source=CMAP_PUBLIC";
							Link cmapSiteLink1 = new Link(mName.toString(), new ExternalResource(cmapPath1));
							cmapSiteLink1.setTargetName("_blank");
							
							Button genotypeCountLinkD = new Button();
							//if(!gCount.isEmpty()){
								genotypeCountLinkD.setCaption(genotype_count);
								genotypeCountLinkD.setStyleName(Reindeer.BUTTON_LINK);
								genotypeCountLinkD.setDescription(genotype_count);
								genotypeCountLinkD.addListener(new Button.ClickListener() {
									
									private static final long serialVersionUID = 1L;
			
									@Override
									public void buttonClick(ClickEvent event) {
										//QtlDetailsDAO qtlDetailsDAO = new QtlDetailsDAO();
										//System.out.println("markerName:"+markerId);
										intMarkers.add(marker_Id);
										try{
											//System.out.println(genoManager.getAllFromMarkerMetadatasetByMarkers(intMarkers));
											List<MarkerMetadataSet> res=genoManager.getAllFromMarkerMetadatasetByMarkers(intMarkers);
											for(MarkerMetadataSet resM: res){
												intdatasetIds.add(resM.getDatasetId());
											}
											List<Integer> nIdList =genoManager.getNIdsByMarkerIdsAndDatasetIds(intdatasetIds, intMarkers, 0,  genoManager.countNIdsByMarkerIdsAndDatasetIds(intdatasetIds, intMarkers));
											//System.out.println(genoManager.getNamesByNameIds(nIdList));
											/*List<Integer> nIdList = manager.getNIdsByMarkerIdsAndDatasetIds(datasetIds, markerIds, 0, 
									                manager.countNIdsByMarkerIdsAndDatasetIds(datasetIds, markerIds));*/
											results = genoManager.getNamesByNameIds(nIdList);
											Window messageWindow = new Window("Lines ");
											GDMSDialog gdmsMessageWindow = new GDMSDialog(_mainHomePage,results,strMsg);
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
										}
									}
								});		
							
							
							
							System.out.println("..:"+mName);
							Object[] strDataArray = {marker_Id, cmapSiteLink1, marker_Type, genotypeCountLinkD, strSpecies, clone_id, qValue, reproducibility, call_rate, pic_value, discordence};
							
							if (null != strDataArray) {
								arrayOfTables[iTableCntr].addItem(strDataArray, new Integer(i));
							}
							
							Object[] strDataArrayExcel = {marker_Id, mName, marker_Type, genotype_count, strSpecies, clone_id, qValue, reproducibility, call_rate, pic_value, discordence};
								
							if (null != strDataArrayExcel) {
								arrayOfTablesForExcel[iTableCntr].addItem(strDataArrayExcel, new Integer(i));
								
							}
						}
					}
				}
			}			
		}
		}
		//System.out.println("....:"+arrayOfTables);
		return arrayOfTables;
	}

}
