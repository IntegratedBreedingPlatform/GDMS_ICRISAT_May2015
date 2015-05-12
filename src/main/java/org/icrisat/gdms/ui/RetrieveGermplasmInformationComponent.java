package org.icrisat.gdms.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AccMetadataSet;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.MapDetailElement;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.generationcp.middleware.pojos.gdms.MarkerSampleId;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.FileDownloadResource;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.ui.common.OptionWindowForFlapjackMap;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

public class RetrieveGermplasmInformationComponent implements Component.Listener {

	private static final long serialVersionUID = 1L;
	private TabSheet _tabsheetForGermplasmNames;
	private Component _buildGermplasmMarkerComponent;
	private Component _buildGermplasmFormatComponent;
	private Component _buildGermplasmResultComponent;
	private CheckBox _chbMatrix;
	private CheckBox _chbFlapjack;
	private final String MATRIX_FORMAT = "Genotyping X Marker Matrix"; 
	private final String FLAPJACK_FORMAT = "Flapjack";
	private GDMSMain _mainHomePage;
	private ArrayList<String> listOfGermplasmNamesSelected = new ArrayList<String>();
	HashMap<String, String> hashMapOfGIDsAndNIDsFromDB = new HashMap<String, String>();
	protected ArrayList<String> listOfMarkersSelected;
	protected String strSelectedFormat; 
	private File matrixFile;
	private HashMap<String, Integer> hashMapOfAllGNamesAndGIDsFromDB;
	protected ArrayList<Integer> listOfGIDsSelected;
	private HashMap<Object, String> hashMapOfGIDsAndGNamesSelected;
	private ArrayList<AllelicValueElement> listAlleleValueElementsForGIDsSelected;
	//private ArrayList<AllelicValueElement> listOfSortedAlleleValueElements;
	protected String strSelectedMap;
	protected String strSelectedColumn;
	private HashMap<String, Integer> hmOfMapNameAndID;
	protected Integer iSelectedMapId;
	protected File generatedTextFile;
	protected File generatedMapFile;
	protected File generatedDatFile;
	private ArrayList<Integer> listOfAllMIDsSelected;
	private HashMap<Integer, String> hmOfSelectedMIDandMNames;
	protected HashMap<Object, String> hmOfSelectedGIDsAndGNames;
	private ArrayList<String> listOfMarkerNames;
	private ArrayList<Integer> markerIDs;
	
	private HashMap<String , Integer> hmOfAllMarkerNamesAndMarkerId;
	public String folderPath;
	public String strQTLExists=""; 

	public File flapjackDat;
	public File flapjackMap;
 	public File flapjackTxt;
	
	
	String realPath="";
	//HashMap<Integer, HashMap<String, Object>> mapEx = new HashMap<Integer, HashMap<String,Object>>();	
	HashMap<Object, HashMap<String, Object>> mapEx = new HashMap<Object, HashMap<String,Object>>();	
	HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
	HashMap marker = new HashMap();
	
	
	ManagerFactory factory=null;
	GermplasmDataManager manager;
	GenotypicDataManager genoManager;
	
	SQLQuery queryL;
	SQLQuery queryC;
	String strQuerry="";
	ArrayList glist = new ArrayList();
	
	private Session localSession;
	private Session centralSession;
	
	HashMap<Object, String> hmOfGIDs;
	
	public RetrieveGermplasmInformationComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		/*manager = factory.getGermplasmDataManager();
		
		genoManager=factory.getGenotypicDataManager();*/
		
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();		
			genoManager=factory.getGenotypicDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		
		
	}

	/**
	 * 
	 * Building the entire Tabbed Component required for Germplasm Names
	 * 
	 */
	public HorizontalLayout buildTabbedComponentForGermplasmNames() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();

		_tabsheetForGermplasmNames = new TabSheet();
		_tabsheetForGermplasmNames.setWidth("800px");

		Component buildGermplasmNamesComponent = buildGermplasmNamesComponent();

		_buildGermplasmMarkerComponent = buildGermplasmMarkerComponent();

		_buildGermplasmFormatComponent = buildGermplasmFormatComponent();

		_buildGermplasmResultComponent = buildGermplasmResultComponent();
		
		buildGermplasmNamesComponent.setSizeFull();
		_buildGermplasmMarkerComponent.setSizeFull();
		_buildGermplasmFormatComponent.setSizeFull();
		_buildGermplasmResultComponent.setSizeFull();

		_tabsheetForGermplasmNames.addComponent(buildGermplasmNamesComponent);
		_tabsheetForGermplasmNames.addComponent(_buildGermplasmMarkerComponent);
		_tabsheetForGermplasmNames.addComponent(_buildGermplasmFormatComponent);
		_tabsheetForGermplasmNames.addComponent(_buildGermplasmResultComponent);
		
		_tabsheetForGermplasmNames.getTab(1).setEnabled(false);
		_tabsheetForGermplasmNames.getTab(2).setEnabled(false);
		_tabsheetForGermplasmNames.getTab(3).setEnabled(false);

		horizontalLayout.addComponent(_tabsheetForGermplasmNames);

		return horizontalLayout;
	}

	private Component buildGermplasmNamesComponent() {
		VerticalLayout layoutForGermplasmNamesTab = new VerticalLayout();
		layoutForGermplasmNamesTab.setCaption("GermplasmNames");
		layoutForGermplasmNamesTab.setSpacing(true);
		layoutForGermplasmNamesTab.setSizeFull();
		layoutForGermplasmNamesTab.setMargin(true, true, true, true);


		final ArrayList<String> listOfGermplasmNames = getListOfGermplasmNames();

		Label lblTitle = new Label("Select GermplasmNames from the list");
		if (null != listOfGermplasmNames){
			if (0 != listOfGermplasmNames.size()){
				lblTitle = new Label("Select from the list of " + listOfGermplasmNames.size() + " Germplasms Names");
			}
		}

		final TwinColSelect selectForGermplasms = new TwinColSelect();
		selectForGermplasms.setLeftColumnCaption("All Germplasms");
		selectForGermplasms.setRightColumnCaption("Selected Germplasms");
		//selectForGermplasms.setNullSelectionAllowed(false);
		//selectForGermplasms.setInvalidAllowed(false);
		selectForGermplasms.setWidth("450px");
		for (String strGermplasm : listOfGermplasmNames) {
			selectForGermplasms.addItem(strGermplasm);
		}
		selectForGermplasms.setRows(20);
		selectForGermplasms.setColumns(25);
		
		selectForGermplasms.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                	TwinColSelect colSelect = (TwinColSelect)event.getProperty();
                	Object value = colSelect.getValue();
                	Set<String> hashSet = (Set<String>) value;
                	listOfGermplasmNamesSelected = new ArrayList<String>();
                	for (String string : hashSet) {
						listOfGermplasmNamesSelected.add(string);
					}
                	//////System.out.println(hashSet);
                }
            }
        });
		selectForGermplasms.setImmediate(true);
		
		
		HorizontalLayout horizLytForSelectComponent = new HorizontalLayout();
		horizLytForSelectComponent.setSizeFull();
		horizLytForSelectComponent.setSpacing(true);
		horizLytForSelectComponent.setMargin(true);
		horizLytForSelectComponent.addComponent(selectForGermplasms);
		
		final TextField txtFieldSearch = new TextField();
		txtFieldSearch.setWidth("300px");
		txtFieldSearch.setImmediate(true);
		txtFieldSearch.addListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void textChange(TextChangeEvent event) {
				ArrayList<String> arrayListOfGermplasmNamesFromTextField = new ArrayList<String>();
				String strGermplasmNames = txtFieldSearch.getValue().toString();
				if (strGermplasmNames.endsWith("*")){
					int indexOf = strGermplasmNames.indexOf('*');
					String substring = strGermplasmNames.substring(0, indexOf);
					
					for (String strGName : listOfGermplasmNames){
						//20131205: Tulasi --- modified the condition to check for the initial string for both upper and lower case alphabets
						if (strGName.toUpperCase().startsWith(substring) ||
						    strGName.toLowerCase().startsWith(substring)) {
							arrayListOfGermplasmNamesFromTextField.add(strGName);
						}
					}
				} else if (strGermplasmNames.trim().equals("*")) {
					arrayListOfGermplasmNamesFromTextField.addAll(listOfGermplasmNames);
				} 
				selectForGermplasms.setValue(arrayListOfGermplasmNamesFromTextField);
			}
			
		});
		
		
		final CheckBox chbSelectAll = new CheckBox("Select All");
		chbSelectAll.setImmediate(true);
		chbSelectAll.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				selectForGermplasms.setValue(listOfGermplasmNames);
			}
		});

		ThemeResource themeResource = new ThemeResource("images/find-icon.png");
		Button searchButton = new Button();
		searchButton.setIcon(themeResource);
		searchButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				ArrayList<String> arrayListOfGermplasmNamesFromTextField = new ArrayList<String>();
				String strGermplasmNames = txtFieldSearch.getValue().toString();
				
				//System.out.println("strGermplasmNames:"+strGermplasmNames);
				if (strGermplasmNames.endsWith("*")){
					int indexOf = strGermplasmNames.indexOf('*');
					String substring = strGermplasmNames.substring(0, indexOf);
					
					for (String strGName : listOfGermplasmNames){
						//20131205: Tulasi --- modified the condition to check for the initial string for both upper and lower case alphabets
						if (strGName.toUpperCase().startsWith(substring) ||
							 strGName.toLowerCase().startsWith(substring)) {
							arrayListOfGermplasmNamesFromTextField.add(strGName);
						}
					}
				} else if (strGermplasmNames.trim().equals("*")) {
					arrayListOfGermplasmNamesFromTextField.addAll(listOfGermplasmNames);
				}else{
					arrayListOfGermplasmNamesFromTextField.add(strGermplasmNames);
				}
				selectForGermplasms.setValue(arrayListOfGermplasmNamesFromTextField);
			}
		});

		
		Label lblSearch = new Label("(Use '*' for wildcard search)");
		lblSearch.setStyleName(Reindeer.LABEL_LIGHT);
		/*finalConditionsLayout.addComponent(lblSearch);
		finalConditionsLayout.setComponentAlignment(lblSearch, Alignment.TOP_LEFT);
*/
		
		
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(false, true, true, false);
		horizontalLayout.addComponent(chbSelectAll);
		horizontalLayout.setComponentAlignment(chbSelectAll, Alignment.TOP_LEFT);
		horizontalLayout.addComponent(txtFieldSearch);
		horizontalLayout.addComponent(searchButton);
		horizontalLayout.addComponent(lblSearch);
		horizontalLayout.setComponentAlignment(lblSearch, Alignment.TOP_LEFT);

		HorizontalLayout horizontalLayoutForButton = new HorizontalLayout();
		Button btnNext = new Button("Next");
		horizontalLayoutForButton.addComponent(btnNext);
		horizontalLayoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		horizontalLayoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				listOfGIDsSelected = new ArrayList<Integer>();
				//hmOfSelectedGIDsAndGNames = new HashMap<Object, String>();
				
				listOfGermplasmNamesSelected = new ArrayList<String>();
				
				Object value2 = selectForGermplasms.getValue();
				Set<String> hashSet = (Set<String>) value2;
				for (String string : hashSet) {
					listOfGermplasmNamesSelected.add(string);
				}
				//System.out.println("%%%%%%%%%%%  :"+listOfGermplasmNamesSelected);
				if (0 == listOfGermplasmNamesSelected.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Germplasm Names to be exported", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				for (String strGermplasmSelected : listOfGermplasmNamesSelected){
					Integer iGID = hashMapOfAllGNamesAndGIDsFromDB.get(strGermplasmSelected);
					listOfGIDsSelected.add(iGID);
					//hmOfSelectedGIDsAndGNames.put(iGID, strGermplasmSelected);
				}
				//System.out.println("listOfGermplasmNamesSelected=:"+listOfGermplasmNamesSelected);

				//germManager.getGidAndNidByGermplasmNames(arg0)
				ArrayList<String> listOfMarkerNames = getMarkersForSelectedGermplasmsSelected();
				//System.out.println("listOfMarkerNames=:"+listOfMarkerNames);
				if (null == listOfMarkerNames || 0 == listOfMarkerNames.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Markers could not be obtained for selected Germplasms.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				Component newGermplasmMarkerComponent = buildGermplasmMarkerComponent();
				_tabsheetForGermplasmNames.replaceComponent(_buildGermplasmMarkerComponent, newGermplasmMarkerComponent);
				_buildGermplasmMarkerComponent.requestRepaint();
				_buildGermplasmMarkerComponent = newGermplasmMarkerComponent;
				_tabsheetForGermplasmNames.getTab(1).setEnabled(true);
				_tabsheetForGermplasmNames.setSelectedTab(_buildGermplasmMarkerComponent);
			}
		});

		layoutForGermplasmNamesTab.addComponent(lblTitle);
		layoutForGermplasmNamesTab.setComponentAlignment(lblTitle, Alignment.TOP_CENTER);
		layoutForGermplasmNamesTab.addComponent(horizontalLayout);
		//layoutForGermplasmNamesTab.addComponent(gridLayout);
		layoutForGermplasmNamesTab.addComponent(horizLytForSelectComponent);
		layoutForGermplasmNamesTab.addComponent(horizontalLayoutForButton);
		layoutForGermplasmNamesTab.setComponentAlignment(horizontalLayoutForButton, Alignment.MIDDLE_CENTER);

		return layoutForGermplasmNamesTab;
	}


	private ArrayList<String> getListOfGermplasmNames() {

		/*NameDAO nameDAOForLocal = new NameDAO();
		nameDAOForLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());

		NameDAO nameDAOForCentral = new NameDAO();
		nameDAOForCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
*/
		ArrayList<String> listOfGNames = new ArrayList<String>();
		hashMapOfAllGNamesAndGIDsFromDB = new HashMap<String, Integer>();
		 List<Integer> ListIDsC = new ArrayList();
		 List<Integer> ListIDsL = new ArrayList();
		 List<Integer> listIDs = new ArrayList();
		 List<Integer> nids=new ArrayList<Integer>();
		try {
			 List<String> resultsC = genoManager.getDatasetNames(0, (int)genoManager.countDatasetNames(Database.CENTRAL), Database.CENTRAL);
		     for(int rC=0;rC<resultsC.size();rC++){
		    	 List<DatasetElement> resultIDsC = genoManager.getDatasetDetailsByDatasetName(resultsC.get(rC), Database.CENTRAL);
		    	 ListIDsC.add(resultIDsC.get(0).getDatasetId());
		     }
		     List<String> resultsL = genoManager.getDatasetNames(0, (int)genoManager.countDatasetNames(Database.LOCAL), Database.LOCAL);
		     for(int rL=0; rL<resultsL.size(); rL++){
		    	 List<DatasetElement> resultIDsL = genoManager.getDatasetDetailsByDatasetName(resultsL.get(rL), Database.LOCAL);
		    	 ListIDsL.add(resultIDsL.get(0).getDatasetId());
		     }
		    for(int c=0;c<ListIDsC.size();c++){
		    	if(!listIDs.contains(ListIDsC.get(c)))
		    		listIDs.add(ListIDsC.get(c));
		    }
		    
		    for(int l=0;l<ListIDsL.size();l++){
		    	if(!listIDs.contains(ListIDsL.get(l)))
		    		listIDs.add(ListIDsL.get(l));
		    }
		   //System.out.println("DatasetIds=:"+listIDs);
		    
		  //  List<Integer> nids =genoManager.getNidsFromAccMetadatasetByDatasetIds(listIDs, 0, (int)genoManager.countNidsFromAccMetadatasetByDatasetIds(listIDs));
		    try{
				List<AccMetadataSet> resAcc=genoManager.getAccMetadatasetsByDatasetIds(listIDs, 0, (int)genoManager.countAccMetadatasetByDatasetIds(listIDs));
				for(AccMetadataSet resAccMetadataset: resAcc){
						
					nids.add(resAccMetadataset.getNameId());
				}
				//System.out.println("resAcc:"+resAcc);
			} catch (MiddlewareQueryException e) {			
				_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving AccMetadataset for the selected Dataset");
				//return;			
			}
		   //List<Integer> nids =genoManager.getNidsFromAccMetadatasetByDatasetIds(ListIDsL, 0, (int)genoManager.countNidsFromAccMetadatasetByDatasetIds(ListIDsL));
		   
		   // System.out.println(".....:"+nids.size()+"   "+nids);
		    List<Name> results = genoManager.getNamesByNameIds(nids);
		    if (null != results){
		    	/*for (Name name1 : results){
					
					System.out.println("..................................?:"+name1.getNval()+"   "+name1.getGermplasmId());
		    	}*/
				for (Name name : results){
					Integer germplasmId = name.getGermplasmId();
					String gName = name.getNval();
					//System.out.println(name.getNval()+"   "+name.getGermplasmId());
					if (false == listOfGNames.contains(gName)){
						listOfGNames.add(gName);
						hashMapOfGIDsAndNIDsFromDB.put(gName, String.valueOf(germplasmId));
						hashMapOfAllGNamesAndGIDsFromDB.put(gName, germplasmId);
					}
				}
			}  		
		    //System.out.println("...:"+listOfGNames.size()+"   "+listOfGNames);
		} catch (MiddlewareQueryException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Germplasm Names from the database", Notification.TYPE_ERROR_MESSAGE);
			return null;
		}


		return listOfGNames;
	}

	private Component buildGermplasmMarkerComponent() {
		VerticalLayout layoutForGermplasmMarkerTab = new VerticalLayout();
		layoutForGermplasmMarkerTab.setCaption("Marker");
		layoutForGermplasmMarkerTab.setSpacing(true);
		layoutForGermplasmMarkerTab.setSizeFull();
		layoutForGermplasmMarkerTab.setMargin(true, true, true, true);

		int iNumOfMarkers = 0;

		listOfMarkerNames = new ArrayList<String>();

		if (null != listOfGermplasmNamesSelected){
			listOfMarkerNames = getMarkersForSelectedGermplasmsSelected();
			iNumOfMarkers = listOfMarkerNames.size();
		}

		Label lblTitle = new Label("Select Markers from the list");
		if (listOfMarkerNames.size() != 0){
			lblTitle = new Label("Select from the list of " + listOfMarkerNames.size() + " markers");
		}
		lblTitle.setStyleName(Reindeer.LABEL_H2);


		final TwinColSelect selectForMarkers = new TwinColSelect();
		selectForMarkers.setLeftColumnCaption("All Markers");
		selectForMarkers.setRightColumnCaption("Selected Markers");
		//selectForMarkers.setNullSelectionAllowed(false);
		//selectForMarkers.setInvalidAllowed(false);
		for (String strGermplasm : listOfMarkerNames) {
			selectForMarkers.addItem(strGermplasm);
		}
		selectForMarkers.setRows(20);
		selectForMarkers.setColumns(25);
		
		selectForMarkers.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                	TwinColSelect colSelect = (TwinColSelect)event.getProperty();
                	Object value2 = colSelect.getValue();
                	Set<String> hashSet = (Set<String>) value2;
                	listOfMarkersSelected = new ArrayList<String>();
                	for (String string : hashSet) {
						listOfMarkersSelected.add(string);
					}
                }
            }
        });
		selectForMarkers.setImmediate(true);
		
		
		HorizontalLayout horizLytForSelectComponent = new HorizontalLayout();
		horizLytForSelectComponent.setSizeFull();
		horizLytForSelectComponent.setSpacing(true);
		horizLytForSelectComponent.setMargin(true);
		horizLytForSelectComponent.addComponent(selectForMarkers);
		
		final CheckBox chbSelectAll = new CheckBox("Select All");
		chbSelectAll.setImmediate(true);
		chbSelectAll.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				selectForMarkers.setValue(listOfMarkerNames);
			}
		});

		final TextField txtFieldSearch = new TextField();
		txtFieldSearch.setWidth("300px");
		txtFieldSearch.addListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void textChange(TextChangeEvent event) {
				ArrayList<String> arrayListOfMarkerNamesFromTextField = new ArrayList<String>();
				String strMarkerNames = txtFieldSearch.getValue().toString();
				if (strMarkerNames.endsWith("*")){
					int indexOf = strMarkerNames.indexOf('*');
					String substring = strMarkerNames.substring(0, indexOf);
					
					for (String strGName : listOfMarkerNames){
						//20131205: Tulasi --- modified the condition to check for the initial string for both upper and lower case alphabets
						if (strGName.toLowerCase().startsWith(substring) || 
							strGName.toUpperCase().startsWith(substring)) {
							arrayListOfMarkerNamesFromTextField.add(strGName);
						}
					}
				} else if (strMarkerNames.trim().equals("*")) {
					arrayListOfMarkerNamesFromTextField.addAll(listOfMarkerNames);
				}
			}
		});
		

		ThemeResource themeResource = new ThemeResource("images/find-icon.png");
		Button searchButton = new Button();
		searchButton.setIcon(themeResource);
		searchButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				ArrayList<String> arrayListOfMarkerNamesFromTextField = new ArrayList<String>();
				String strMarkerNames = txtFieldSearch.getValue().toString();
				if (strMarkerNames.endsWith("*")){
					int indexOf = strMarkerNames.indexOf('*');
					String substring = strMarkerNames.substring(0, indexOf);
					
					for (String strGName : listOfMarkerNames){
						//20131205: Tulasi --- modified the condition to check for the initial string for both upper and lower case alphabets
						if (strGName.toLowerCase().startsWith(substring) || 
							strGName.toUpperCase().startsWith(substring)) {
							arrayListOfMarkerNamesFromTextField.add(strGName);
							selectForMarkers.setValue(arrayListOfMarkerNamesFromTextField);
						}
					}
				} else if (strMarkerNames.trim().equals("*")) {
					arrayListOfMarkerNamesFromTextField.addAll(listOfMarkerNames);
					selectForMarkers.setValue(listOfMarkerNames);
				}else{
					arrayListOfMarkerNamesFromTextField.add(strMarkerNames);
					selectForMarkers.setValue(arrayListOfMarkerNamesFromTextField);
				}
			}
		});
		Label lblSearch = new Label("(Use '*' for wildcard search)");
		lblSearch.setStyleName(Reindeer.LABEL_LIGHT);
		
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(false, true, true, false);
		horizontalLayout.addComponent(chbSelectAll);
		horizontalLayout.setComponentAlignment(chbSelectAll, Alignment.TOP_LEFT);
		horizontalLayout.addComponent(txtFieldSearch);
		horizontalLayout.addComponent(searchButton);
		horizontalLayout.addComponent(lblSearch);
		horizontalLayout.setComponentAlignment(lblSearch, Alignment.TOP_LEFT);


		HorizontalLayout horizontalLayoutForButton = new HorizontalLayout();
		Button btnNext = new Button("Next");
		horizontalLayoutForButton.addComponent(btnNext);
		horizontalLayoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		horizontalLayoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				/*if (null == listOfMarkersSelected){
					_mainHomePage.getMainWindow().showNotification("Please select the list of Markers to be exported.", Notification.TYPE_ERROR_MESSAGE);
					return;
            	}*/
				
				listOfMarkersSelected = new ArrayList<String>();
            	Object value2 = selectForMarkers.getValue();
            	Set<String> hashSet = (Set<String>) value2;
            	for (String string : hashSet) {
					listOfMarkersSelected.add(string);
				}
				
				if (0 == listOfMarkersSelected.size()){
					_mainHomePage.getMainWindow().showNotification("Please select the list of Markers to be exported.", Notification.TYPE_ERROR_MESSAGE);
					return;
				} else {
					getMarkerIDsForSelectedMarkers();

					if (null != listOfAllMIDsSelected && 0 == listOfAllMIDsSelected.size()){
						_mainHomePage.getMainWindow().showNotification("Marker-IDs could not be obtained for the selected Germplasms.", Notification.TYPE_ERROR_MESSAGE);
						return;
					}

					Component newGermplasmFormatComponent = buildGermplasmFormatComponent();
					_tabsheetForGermplasmNames.replaceComponent(_buildGermplasmFormatComponent, newGermplasmFormatComponent);
					_buildGermplasmFormatComponent.requestRepaint();
					_buildGermplasmFormatComponent = newGermplasmFormatComponent;
					_tabsheetForGermplasmNames.getTab(2).setEnabled(true);
					_tabsheetForGermplasmNames.setSelectedTab(_buildGermplasmFormatComponent);
				}

			}
		});


		if (0 == iNumOfMarkers){
			chbSelectAll.setEnabled(false);
			txtFieldSearch.setEnabled(false);
			searchButton.setEnabled(false);
			btnNext.setEnabled(false);
			selectForMarkers.setEnabled(false);
		} else {
			chbSelectAll.setEnabled(true);
			txtFieldSearch.setEnabled(true);
			searchButton.setEnabled(true);
			btnNext.setEnabled(true);
			selectForMarkers.setEnabled(true);
		}

		layoutForGermplasmMarkerTab.addComponent(lblTitle);
		layoutForGermplasmMarkerTab.setComponentAlignment(lblTitle, Alignment.TOP_CENTER);
		layoutForGermplasmMarkerTab.addComponent(horizontalLayout);
		//layoutForGermplasmMarkerTab.addComponent(gridLayout);
		layoutForGermplasmMarkerTab.addComponent(horizLytForSelectComponent);
		layoutForGermplasmMarkerTab.addComponent(horizontalLayoutForButton);
		layoutForGermplasmMarkerTab.setComponentAlignment(horizontalLayoutForButton, Alignment.MIDDLE_CENTER);

		return layoutForGermplasmMarkerTab;
	}


	protected void getMarkerIDsForSelectedMarkers() {

		
		try {
			MarkerDAO markerDAOForLocal = new MarkerDAO();
			markerDAOForLocal.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession());
			MarkerDAO markerDAOForCentral = new MarkerDAO();
			markerDAOForCentral.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession());

			long countAllLocal = markerDAOForLocal.countAll();
			List<Integer> listOfMIDsByMNamesLocal = markerDAOForLocal.getIdsByNames(listOfMarkersSelected, 0, (int)countAllLocal);

			long countAllCentral = markerDAOForCentral.countAll();
			List<Integer> listOfMIDsByMNamesCentral = markerDAOForCentral.getIdsByNames(listOfMarkersSelected, 0, (int)countAllCentral);

			listOfAllMIDsSelected = new ArrayList<Integer>();
			if (null != listOfMIDsByMNamesLocal){
				for (Integer iMID : listOfMIDsByMNamesLocal){
					if (false == listOfAllMIDsSelected.contains(iMID)){
						listOfAllMIDsSelected.add(iMID);
					}
				}
			}
			if (null != listOfMIDsByMNamesCentral){
				for (Integer iMID : listOfMIDsByMNamesCentral){
					if (false == listOfAllMIDsSelected.contains(iMID)){
						listOfAllMIDsSelected.add(iMID);
					}
				}
			}

			hmOfSelectedMIDandMNames = new HashMap<Integer, String>();
			long countMarkersByIds = markerDAOForLocal.countMarkersByIds(listOfAllMIDsSelected);
			List<Marker> listOfMarkersByIdsLocal = markerDAOForLocal.getMarkersByIds(listOfAllMIDsSelected, 0, (int)countMarkersByIds);
			long countMarkersByIds2 = markerDAOForCentral.countMarkersByIds(listOfAllMIDsSelected);
			List<Marker> listOfMarkersByCentral = markerDAOForCentral.getMarkersByIds(listOfAllMIDsSelected, 0, (int)countMarkersByIds2);

			if (null != listOfMarkersByIdsLocal){
				for (Marker marker : listOfMarkersByIdsLocal){
					Integer markerId = marker.getMarkerId();
					String markerName = marker.getMarkerName();
					if (false == hmOfSelectedMIDandMNames.containsKey(markerId)){
						hmOfSelectedMIDandMNames.put(markerId, markerName);
					}
				}
			}
			if (null != listOfMarkersByCentral){
				for (Marker marker : listOfMarkersByCentral){
					Integer markerId = marker.getMarkerId();
					String markerName = marker.getMarkerName();
					if (false == hmOfSelectedMIDandMNames.containsKey(markerId)){
						hmOfSelectedMIDandMNames.put(markerId, markerName);
					}
				}
			}

		} catch (MiddlewareQueryException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Marker-IDs for Markers selected.", Notification.TYPE_ERROR_MESSAGE);
			return;
		} catch(GDMSException e){
			e.printStackTrace();
		}

	}


	private ArrayList<String> getMarkersForSelectedGermplasmsSelected() {

		ArrayList<Integer> listOfGIDSelected = new ArrayList<Integer>();
		ArrayList<String> arrayListOfMarkerNames;

		for (int i = 0; i < listOfGermplasmNamesSelected.size(); i++){
			String strGNameSelected = listOfGermplasmNamesSelected.get(i);

			String strGID = hashMapOfGIDsAndNIDsFromDB.get(strGNameSelected);
			listOfGIDSelected.add(Integer.valueOf(strGID));
		}
		////System.out.println("listOfGIDSelected-:"+listOfGIDSelected);

		/*MarkerDAO markerDAOForLocal = new MarkerDAO();
		markerDAOForLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());

		MarkerDAO markerDAOForCentral = new MarkerDAO();
		markerDAOForCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());*/
		List<MarkerSampleId> listOfSNPMarkerIDS = new ArrayList<MarkerSampleId>();
		List<MarkerSampleId> listOfSSRMarkerIDS = new ArrayList<MarkerSampleId>();
		List<MarkerSampleId> listOfMappingMarkerIDS = new ArrayList<MarkerSampleId>();
		List listOfMarkerIDS = new ArrayList<String>();
		try {
			hmOfAllMarkerNamesAndMarkerId=new HashMap<String, Integer>();
			
			arrayListOfMarkerNames = new ArrayList<String>();
			if(listOfGIDSelected.size()>0){
				listOfSSRMarkerIDS=genoManager.getMarkerFromAlleleValuesByGids(listOfGIDSelected);
				listOfSNPMarkerIDS=genoManager.getMarkerFromCharValuesByGids(listOfGIDSelected);
				listOfMappingMarkerIDS=genoManager.getMarkerFromMappingPopByGids(listOfGIDSelected);
				//////System.out.println("^^^^^^^^^^^^^^^^^  :"+genoManager.getMarkerFromAlleleValuesByGids(listOfGIDSelected));
				for(MarkerSampleId sn:listOfSNPMarkerIDS){
					//listOfMarkerIDS.add(listOfSNPMarkerIDS.get(sn));
					listOfMarkerIDS.add(sn.getMarkerId());
				}
				
				for(MarkerSampleId ss:listOfSSRMarkerIDS){
					//listOfMarkerIDS.add(listOfSSRMarkerIDS.get(ss));
					listOfMarkerIDS.add(ss.getMarkerId());
				}
				
				for(MarkerSampleId m:listOfMappingMarkerIDS){
					//listOfMarkerIDS.add(listOfMappingMarkerIDS.get(m));
					listOfMarkerIDS.add(m.getMarkerId());
				}
				//ystem.out.println("listOfMarkerIDS:"+listOfMarkerIDS);
			//System.out.println("$$$$$$$$$$$  :"+genoManager.getMarkerNamesByMarkerIds(listOfMarkerIDS));
				List<MarkerIdMarkerNameElement> markerNames =genoManager.getMarkerNamesByMarkerIds(listOfMarkerIDS);
				
				
				for (MarkerIdMarkerNameElement e : markerNames) {
		            //Debug.println(0, e.getMarkerId() + " : " + e.getMarkerName());
		            arrayListOfMarkerNames.add(e.getMarkerName());
		            hmOfAllMarkerNamesAndMarkerId.put(e.getMarkerName(), e.getMarkerId());
		        }
			}
			////System.out.println("arrayListOfM%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%arkerNames=:"+arrayListOfMarkerNames);
			

		} catch (MiddlewareQueryException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Marker Names for Germplasms selected.", Notification.TYPE_ERROR_MESSAGE);
			return null;
		}

		return arrayListOfMarkerNames;
	}

	private Component buildGermplasmFormatComponent() {
		/**
		 * 
		 * Title label on the top
		 * 
		 */
		Label lblTitle = new Label("Choose Data Export Format");
		lblTitle.setStyleName(Reindeer.LABEL_H2);

		Label lblColumn = new Label("Identify a Column");
		lblColumn.setStyleName(Reindeer.LABEL_SMALL);

		final OptionGroup optionGroupForColumn = new OptionGroup();
		optionGroupForColumn.setMultiSelect(false);
		optionGroupForColumn.addStyleName("horizontal");
		optionGroupForColumn.addItem("GIDs");
		optionGroupForColumn.addItem("Germplasm Names");
		optionGroupForColumn.select("Germplasm Names");
		optionGroupForColumn.setEnabled(false);
		optionGroupForColumn.setImmediate(true);

		HorizontalLayout horizLayoutForColumns = new HorizontalLayout();
		horizLayoutForColumns.setSpacing(true);
		horizLayoutForColumns.addComponent(lblColumn);
		horizLayoutForColumns.addComponent(optionGroupForColumn);

		/**
		 * 
		 * Building the left side components and layout
		 * 
		 */
		VerticalLayout layoutForGermplasmFormatTab = new VerticalLayout();
		layoutForGermplasmFormatTab.setSpacing(true);
		layoutForGermplasmFormatTab.setMargin(true, true, true, true);

		_chbMatrix = new CheckBox();
		_chbMatrix.setCaption(MATRIX_FORMAT);
		_chbMatrix.setHeight("25px");
		_chbMatrix.setImmediate(true);
		layoutForGermplasmFormatTab.addComponent(_chbMatrix);
		_chbMatrix.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				if (true == (Boolean) _chbMatrix.getValue()){
					_chbFlapjack.setValue(false);
					_chbMatrix.setValue(true);

					ThemeResource themeResourceMatrix = new ThemeResource("images/Matrix.jpg");
					Embedded matrixImage = new Embedded(null, themeResourceMatrix);
					matrixImage.setWidth("240px");
					matrixImage.setHeight("180px");
				}
			}
		});


		ThemeResource themeResourceMatrix = new ThemeResource("images/Matrix.jpg");
		Embedded matrixImage = new Embedded(null, themeResourceMatrix);
		matrixImage.setWidth("240px");
		matrixImage.setHeight("180px");
		CssLayout cssLayoutMatrix = new CssLayout();
		cssLayoutMatrix.addComponent(matrixImage);
		layoutForGermplasmFormatTab.addComponent(cssLayoutMatrix);


		final ComboBox selectMap = new ComboBox();
		Object itemId1 = selectMap.addItem();
		selectMap.setItemCaption(itemId1, "Select Map");
		selectMap.setValue(itemId1);
		selectMap.setNullSelectionAllowed(false);
		selectMap.setImmediate(true);
		selectMap.setEnabled(false);
		selectMap.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				strSelectedMap = selectMap.getValue().toString();
				//iSelectedMapId = hmOfMapNameAndID.get(strSelectedMap);
			}
		});

		final ArrayList<String> arrayListOfMapNames = new ArrayList<String>();
		//hmOfMapNameAndID = new HashMap<String, Integer>();

		//System.out.println(strSelectedMap+"  listOfMarkersSelected:"+listOfMarkersSelected);
		if (null != listOfMarkersSelected && 0 != listOfMarkersSelected.size()){
			markerIDs=new ArrayList<Integer>();
			try {
				
				List<Marker> markerIDsFromCentral=genoManager.getMarkersByMarkerNames(listOfMarkersSelected, 0, listOfMarkersSelected.size(), Database.CENTRAL);
				List<Marker> markerIDsFromLocal=genoManager.getMarkersByMarkerNames(listOfMarkersSelected, 0, listOfMarkersSelected.size(), Database.LOCAL);
				//genoManager.getMapAndMarkerCountByMarkers(markerIDsFromSelectedDataset);
				for (Marker iMarkerID : markerIDsFromCentral){
				//for(int mc=0;mc<markerIDsFromCentral.size();mc++){
					if(!markerIDs.contains(iMarkerID.getMarkerId()))
						markerIDs.add(iMarkerID.getMarkerId());
				}
				for (Marker iMarker : markerIDsFromLocal){
					if(!markerIDs.contains(iMarker.getMarkerId()))
						markerIDs.add(iMarker.getMarkerId());
				}
				/*List<Integer> markerIDsFromCentral=genoManager.getMarkerIdsByMarkerNames(listOfMarkersSelected, 0, listOfMarkersSelected.size(), Database.CENTRAL);
				List<Integer> markerIDsFromLocal=genoManager.getMarkerIdsByMarkerNames(listOfMarkersSelected, 0, listOfMarkersSelected.size(), Database.LOCAL);
				//genoManager.getMapAndMarkerCountByMarkers(markerIDsFromSelectedDataset);
				for(int mc=0;mc<markerIDsFromCentral.size();mc++){
					if(!markerIDs.contains(markerIDsFromCentral.get(mc)))
						markerIDs.add(markerIDsFromCentral.get(mc));
				}
				for(int ml=0;ml<markerIDsFromLocal.size();ml++){
					if(!markerIDs.contains(markerIDsFromLocal.get(ml)))
						markerIDs.add(markerIDsFromLocal.get(ml));
				}*/
				
				List<MapDetailElement> details = genoManager.getMapAndMarkerCountByMarkers(markerIDs);
		        if (details != null && details.size() > 0) {
		            //Debug.println(0, "FOUND " + details.size() + " records");
		            for (MapDetailElement detail : details) {
		                //Debug.println(0, detail.getMapName() + "-" + detail.getMarkerCount());
		                if (false == arrayListOfMapNames.contains(detail.getMapName())){
		                	arrayListOfMapNames.add(detail.getMapName() + "(" + detail.getMarkerCount()+")");
							//hmOfMapNameAndMapId.put(detail.getMapName() + "(" + detail.getMarkerCount()+")", detail.get.getMapId());
						}
		            }
		        } else {
		            //Debug.println(0, "NO RECORDS FOUND");
		        	List<Map> resMapsCentral=genoManager.getAllMaps(0, (int)genoManager.countAllMaps(Database.CENTRAL), Database.CENTRAL);
					if(!resMapsCentral.isEmpty()){
						for (Map map: resMapsCentral){
							if (false == arrayListOfMapNames.contains(map.getMapName())){
								arrayListOfMapNames.add(map.getMapName()+"(0)");
								//hmOfMapNameAndMapId.put(map.getMapName(), map.getMapId());
							}
						}
					}
		        	List<Map> resMapsLocal=genoManager.getAllMaps(0, (int)genoManager.countAllMaps(Database.LOCAL), Database.LOCAL);
					if(resMapsLocal.isEmpty()){
						for (Map map: resMapsLocal){
							if (false == arrayListOfMapNames.contains(map.getMapName())){
								arrayListOfMapNames.add(map.getMapName()+"(0)");
								//hmOfMapNameAndMapId.put(map.getMapName(), map.getMapId());
							}
						}
					}
		        }
				
			} catch (MiddlewareQueryException e) {
				_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Maps from the database", Notification.TYPE_ERROR_MESSAGE);
				return null;
			} /*catch (GDMSException e){
				e.printStackTrace();
			}*/

		}

		for (String strMapName : arrayListOfMapNames){
			selectMap.addItem(strMapName);
		}

		/**
		 * 
		 * Building the right side components and layout
		 * 
		 */
		_chbFlapjack = new CheckBox();
		_chbFlapjack.setCaption(FLAPJACK_FORMAT);
		_chbFlapjack.setHeight("25px");
		_chbFlapjack.setImmediate(true);
		_chbFlapjack.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				if (true == (Boolean) _chbFlapjack.getValue()){
					_chbMatrix.setValue(false);
					_chbFlapjack.setValue(true);
					optionGroupForColumn.setEnabled(true);
					selectMap.setEnabled(true);
				} else {
					_chbFlapjack.setValue(false);
					optionGroupForColumn.setEnabled(false);
					selectMap.setEnabled(false);
					selectMap.removeAllItems();
				}
			}
		});

		/*_chbFlapjack.addListener(new ValueChangeListener() {
		 *//**
		 * 
		 *//*
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				if (true == (Boolean) _chbFlapjack.getValue()){
					_chbMatrix.setValue(false);
					_chbFlapjack.setValue(true);
					selectMap.setEnabled(true);
					ThemeResource themeResourceFlapjack = new ThemeResource("images/flapjack.png");
					Embedded flapjackImage = new Embedded(null, themeResourceFlapjack);
					flapjackImage.setWidth("200px");
					flapjackImage.setHeight("200px");
				}
			}
		});*/


		HorizontalLayout topHorizLayoutForFlapjack = new HorizontalLayout();
		topHorizLayoutForFlapjack.setSizeFull();
		topHorizLayoutForFlapjack.setSpacing(true);
		topHorizLayoutForFlapjack.addComponent(_chbFlapjack);
		topHorizLayoutForFlapjack.addComponent(selectMap);

		ThemeResource themeResourceFlapjack = new ThemeResource("images/flapjack.png");
		Embedded flapjackImage = new Embedded(null, themeResourceFlapjack);
		flapjackImage.setWidth("200px");
		flapjackImage.setHeight("200px");
		CssLayout cssLayoutFlapjack = new CssLayout();
		cssLayoutFlapjack.addComponent(flapjackImage);

		VerticalLayout layoutForFlapjackFormat = new VerticalLayout();
		layoutForFlapjackFormat.setSpacing(true);
		layoutForFlapjackFormat.setSizeFull();
		layoutForFlapjackFormat.setMargin(true, true, true, true);
		layoutForFlapjackFormat.addComponent(topHorizLayoutForFlapjack);
		layoutForFlapjackFormat.addComponent(horizLayoutForColumns);
		layoutForFlapjackFormat.addComponent(cssLayoutFlapjack);

		/**
		 * 
		 * Building the Next button panel at the bottom of the layout
		 * 
		 */
		VerticalLayout layoutForButton = new VerticalLayout();
		Button btnNext = new Button("Next");
		layoutForButton.addComponent(btnNext);
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			private boolean bGenerateFlapjack = false;
			private boolean dataToBeExportedBuiltSuccessfully = false;

			public void buttonClick(ClickEvent event) {

				if (null == listOfMarkersSelected || 0 == listOfMarkersSelected.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Markers to be displayed in the required format.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				if (null == listOfGIDsSelected || 0 == listOfGIDsSelected.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Germplasms to be displayed in the required format.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				if (_chbMatrix.getValue().toString().equals("true")){
					strSelectedFormat = "Matrix";
					//////System.out.println("Format Selected: " + strSelectedFormat);

					try {
						retrieveGermplasmDataForMatrixFormat();
						////System.out.println("gids selected=:"+ listOfGIDsSelected);
						////System.out.println("markers selected=:"+listOfMarkersSelected);
						dataToBeExportedBuiltSuccessfully = true;
					} catch (GDMSException e1) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e1.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
						return;
					}

					if (dataToBeExportedBuiltSuccessfully){
						ExportFileFormats exportFileFormats = new ExportFileFormats();
						try {
							matrixFile = exportFileFormats.Matrix(_mainHomePage, glist, listOfMarkersSelected, hashMapOfGIDsAndGNamesSelected, mapEx, hmOfAllMarkerNamesAndMarkerId, hmOfGIDs);
						} catch (GDMSException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification("Error generating the Matrix File", Notification.TYPE_ERROR_MESSAGE);
							return;
						}

						//////System.out.println("Received the generated Matrix file.");
					}

				} else if (_chbFlapjack.getValue().toString().equals("true")){
					try {
						retrieveGermplasmDataForMatrixFormat();
						////System.out.println("gids selected=:"+ listOfGIDsSelected);
						////System.out.println("markers selected=:"+listOfMarkersSelected);
						dataToBeExportedBuiltSuccessfully = true;
					} catch (GDMSException e1) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e1.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
						return;
					}

					//strSelectedMap = selectMap.getValue().toString();
					//strSelectedColumn = optionGroupForColumn.getValue().toString();
					Object mapValue = selectMap.getValue();
					if (mapValue instanceof Integer){
						Integer itemId = (Integer)mapValue;
						if (itemId.equals(1)){
							strSelectedMap = "";
						} 
					} else {
						if (arrayListOfMapNames.contains(mapValue.toString())){
							strSelectedMap = mapValue.toString();
						} else {
							strSelectedMap = "";
						}
					}
					if(! strSelectedMap.isEmpty())
						strSelectedMap = strSelectedMap.substring(0,strSelectedMap.lastIndexOf("("));
					
					try{
						//String mapName  = strMapSelected.substring(0,strMapSelected.lastIndexOf("("));
						iSelectedMapId=genoManager.getMapIdByName(strSelectedMap);
					} catch (MiddlewareQueryException e) {
						e.printStackTrace();
						_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Map Id for the selected Map", Notification.TYPE_ERROR_MESSAGE);							
					}
					Object value = optionGroupForColumn.getValue();
					if (null != value){
						strSelectedColumn = value.toString();
					} else {
						strSelectedColumn = "";
					}
					//////System.out.println("Selected Map: " + strSelectedMap + " --- " + "Selected Column: " + strSelectedColumn);

					if (strSelectedMap.equals("")){
						
						//20131211: Tulasi --- Implemented code to ask the user to generate Flapjack with or without Map
						bGenerateFlapjack = false;
						OptionWindowForFlapjackMap optionWindowForFlapjackMap = new OptionWindowForFlapjackMap();
						final Window messageWindow = new Window("Require Map");
						if (null != optionWindowForFlapjackMap) {
							messageWindow.setContent(optionWindowForFlapjackMap);
							messageWindow.setWidth("500px");
							messageWindow.setClosable(true);
							messageWindow.center();
							
							if (false == _mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
								_mainHomePage.getMainWindow().addWindow(messageWindow);
							}
							messageWindow.setModal(true);
							messageWindow.setVisible(true);
						}
						
						optionWindowForFlapjackMap.addMapOptionListener(new MapOptionsListener() {

							@Override
							public void isMapRequiredOption(boolean bMapRequired) {
								if (bMapRequired) {
									_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required map for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
									bGenerateFlapjack = false;
									
									if (_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
										_mainHomePage.getMainWindow().removeWindow(messageWindow);
									}
									
								} else {
									bGenerateFlapjack = true;
									
									if (_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
										_mainHomePage.getMainWindow().removeWindow(messageWindow);
									}
									
									if (strSelectedColumn.equals("")){
										_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required column GID or Germplasm for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
										return;
									} 
									
									
									
									RetrieveDataForFlapjack retrieveDataForFlapjack = new RetrieveDataForFlapjack(_mainHomePage);
									retrieveDataForFlapjack.setGenotypingType("Germplasm Names");
									retrieveDataForFlapjack.setListOfGermplasmsProvided(listOfGermplasmNamesSelected);
									retrieveDataForFlapjack.setListOfMarkersSelected(listOfMarkersSelected);
									retrieveDataForFlapjack.setListOfGIDsSelected(listOfGIDsSelected);
									retrieveDataForFlapjack.setListOfMIDsSelected(listOfAllMIDsSelected);
									retrieveDataForFlapjack.setHashmapOfSelectedMIDsAndMNames(hmOfSelectedMIDandMNames);
									retrieveDataForFlapjack.setHashmapOfSelectedGIDsAndGNames(hmOfSelectedGIDsAndGNames);
									retrieveDataForFlapjack.setMapSelected(strSelectedMap, iSelectedMapId);
									retrieveDataForFlapjack.setExportType(strSelectedColumn);
									retrieveDataForFlapjack.retrieveFlapjackData();
									dataToBeExportedBuiltSuccessfully = retrieveDataForFlapjack.isFlapjackDataBuiltSuccessfully();
									
									if (dataToBeExportedBuiltSuccessfully){
										generatedTextFile = retrieveDataForFlapjack.getGeneratedTextFile();
										generatedMapFile = retrieveDataForFlapjack.getGeneratedMapFile();
										generatedDatFile = retrieveDataForFlapjack.getGeneratedDatFile();
										
										folderPath=retrieveDataForFlapjack.getStrBMSFilePath();
										strQTLExists=retrieveDataForFlapjack.getQTLExists();
										
										Component newDatasetResultsPanel = buildGermplasmResultComponent();
										_tabsheetForGermplasmNames.replaceComponent(_buildGermplasmResultComponent, newDatasetResultsPanel);
										_tabsheetForGermplasmNames.requestRepaint();
										_buildGermplasmResultComponent = newDatasetResultsPanel;
										_tabsheetForGermplasmNames.getTab(3).setEnabled(true);
										_tabsheetForGermplasmNames.setSelectedTab(_buildGermplasmResultComponent);
									}
								}
							}
							
						});
						/*_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required map for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;*/
					} else if (strSelectedColumn.equals("")){
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required column GID or Germplasm for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;
					} else {
						if (false == bGenerateFlapjack) {
							bGenerateFlapjack = true;
						}
					}

					strSelectedFormat = "Matrix";

					if ("true".equals(_chbFlapjack.getValue().toString())){
						strSelectedFormat = "Flapjack";
					}
					////System.out.println("*************  listOfGermplasmNamesSelected:"+listOfGermplasmNamesSelected);
					////System.out.println("***********    listOfGIDsSelected=:"+listOfGIDsSelected);
					////System.out.println("***********    listOfMarkersSelected=:"+listOfMarkersSelected);
					
					
					//////System.out.println("Trying to retrieve Data required for Flapjack format.");
					if (bGenerateFlapjack) {
						RetrieveDataForFlapjack retrieveDataForFlapjack = new RetrieveDataForFlapjack(_mainHomePage);
						retrieveDataForFlapjack.setGenotypingType("Germplasm Names");
						retrieveDataForFlapjack.setListOfGermplasmsProvided(listOfGermplasmNamesSelected);
						retrieveDataForFlapjack.setListOfMarkersSelected(listOfMarkersSelected);
						retrieveDataForFlapjack.setListOfGIDsSelected(listOfGIDsSelected);
						retrieveDataForFlapjack.setListOfMIDsSelected(listOfAllMIDsSelected);
						retrieveDataForFlapjack.setHashmapOfSelectedMIDsAndMNames(hmOfSelectedMIDandMNames);
						retrieveDataForFlapjack.setHashmapOfSelectedGIDsAndGNames(hmOfSelectedGIDsAndGNames);
						retrieveDataForFlapjack.setMapSelected(strSelectedMap, iSelectedMapId);
						retrieveDataForFlapjack.setExportType(strSelectedColumn);
						retrieveDataForFlapjack.retrieveFlapjackData();
						dataToBeExportedBuiltSuccessfully = retrieveDataForFlapjack.isFlapjackDataBuiltSuccessfully();
	
						if (dataToBeExportedBuiltSuccessfully){
							generatedTextFile = retrieveDataForFlapjack.getGeneratedTextFile();
							generatedMapFile = retrieveDataForFlapjack.getGeneratedMapFile();
							generatedDatFile = retrieveDataForFlapjack.getGeneratedDatFile();
							
							folderPath=retrieveDataForFlapjack.getStrBMSFilePath();
							strQTLExists=retrieveDataForFlapjack.getQTLExists();
						}
					}
				}

				if (dataToBeExportedBuiltSuccessfully){
					Component newDatasetResultsPanel = buildGermplasmResultComponent();
					_tabsheetForGermplasmNames.replaceComponent(_buildGermplasmResultComponent, newDatasetResultsPanel);
					_tabsheetForGermplasmNames.requestRepaint();
					_buildGermplasmResultComponent = newDatasetResultsPanel;
					_tabsheetForGermplasmNames.getTab(3).setEnabled(true);
					_tabsheetForGermplasmNames.setSelectedTab(_buildGermplasmResultComponent);
				}

				if (null == strSelectedFormat || strSelectedFormat.equals("")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required format.",  Notification.TYPE_ERROR_MESSAGE);
					return;
				} 
			}
		});

		HorizontalLayout horizontalLayoutForTwoFormats = new HorizontalLayout();
		horizontalLayoutForTwoFormats.setSpacing(true);
		horizontalLayoutForTwoFormats.setSizeFull();
		horizontalLayoutForTwoFormats.addComponent(layoutForGermplasmFormatTab);
		horizontalLayoutForTwoFormats.addComponent(layoutForFlapjackFormat);

		/**
		 * 
		 * Building the final vertical layout for the Format tab
		 * 
		 */
		VerticalLayout completeFormatLayout = new VerticalLayout();
		completeFormatLayout.setCaption("Format");
		completeFormatLayout.setSpacing(true);
		completeFormatLayout.setSizeFull();
		completeFormatLayout.setMargin(true, true, true, true);

		completeFormatLayout.addComponent(lblTitle);
		completeFormatLayout.setComponentAlignment(lblTitle, Alignment.TOP_CENTER);
		completeFormatLayout.addComponent(horizontalLayoutForTwoFormats);
		completeFormatLayout.addComponent(layoutForButton);
		completeFormatLayout.setComponentAlignment(layoutForButton, Alignment.BOTTOM_CENTER);

		OptionGroup optiongroup = new OptionGroup();
		optiongroup.addItem(_chbMatrix);
		optiongroup.addItem(_chbFlapjack);
		optiongroup.setMultiSelect(false);

		if (null == listOfMarkersSelected){
			btnNext.setEnabled(false);
			_chbMatrix.setEnabled(false);
			_chbFlapjack.setEnabled(false);
			optiongroup.setEnabled(false);
		} else {
			if (0 != listOfMarkersSelected.size()){
				btnNext.setEnabled(true);
				_chbMatrix.setEnabled(true);
				_chbFlapjack.setEnabled(true);
				optiongroup.setEnabled(true);
			}
		}

		return completeFormatLayout;
	}

	protected void retrieveGermplasmDataForMatrixFormat() throws GDMSException {
		
		String strGids="";
		String strMids="";
		
		
		for(int g=0;g<listOfGIDsSelected.size();g++){
			strGids=strGids+listOfGIDsSelected.get(g)+",";
		}
		strGids=strGids.substring(0, strGids.length()-1);
		for(int m=0;m<markerIDs.size();m++){
			strMids=strMids+markerIDs.get(m)+",";
		}
		strMids=strMids.substring(0, strMids.length()-1);
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
		strQuerry="select distinct gid,marker_id, char_value,acc_sample_id,marker_sample_id from gdms_char_values where gid in("+strGids+") and marker_id in ("+strMids+") ORDER BY gid, marker_id,acc_sample_id asc";	
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
			//System.out.println(strMareO);
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
		
		strQuerry="select distinct gid,marker_id, allele_bin_value,acc_sample_id,marker_sample_id from gdms_allele_values where gid in("+strGids+") and marker_id in ("+strMids+") ORDER BY gid, marker_id,acc_sample_id asc";	
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
		
		strQuerry="select distinct gid,marker_id, map_char_value,acc_sample_id,marker_sample_id from gdms_mapping_pop_values where gid in("+strGids+") and marker_id in ("+strMids+") ORDER BY gid, marker_id,acc_sample_id asc";	
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
		//System.out.println("mapEx:"+mapEx);
		HashMap<Object, Integer> hmGidSampleIdNid=new HashMap<Object, Integer>();
		HashMap<Integer, String> hmNidGermplasmName=new HashMap<Integer, String>();
		ArrayList listOfGNames = new ArrayList<String>();
		ArrayList gDupNameList=new ArrayList<Integer>();
		/*try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}*/
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
		String strQuerry="select distinct gid,nid, acc_sample_id from gdms_acc_metadataset where gid in ("+ strGids +") order by gid, nid,acc_sample_id asc";	
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
				if(Integer.parseInt(strMareO[2].toString())==2){
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
				if(Integer.parseInt(strMareO[2].toString())==2){
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
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving list of AllelicValueElement for the selected GIDs required for Matrix format.", Notification.TYPE_ERROR_MESSAGE);
			String strErrMsg = "Error retrieving germplasm names.";
			throw new GDMSException(strErrMsg);
		}
		hashMapOfGIDsAndGNamesSelected = new HashMap<Object, String>();
		hmOfGIDs = new HashMap<Object, String>();
		//sortedMapOfGNamesAndGIDs= new TreeMap<String, Object>();
		Set<Object> gidKeySet1 = hmGidSampleIdNid.keySet();
		Iterator<Object> gidIterator1 = gidKeySet1.iterator();
		String gids="";
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
		
			hashMapOfGIDsAndGNamesSelected.put(gidSampleid, gname);
			hmOfGIDs.put(gidSampleid, gids);
			
		}
		//System.out.println("hashMapOfGIDsAndGNamesSelected:"+hashMapOfGIDsAndGNamesSelected);
	
		
		/*//HashMap of GIDs and Germplasms
		hashMapOfGIDsAndGNamesSelected = new HashMap<Object, String>();
		for (int i = 0; i < listOfGermplasmNamesSelected.size(); i++){
			String strGermplasmSelected = listOfGermplasmNamesSelected.get(i);
			Integer iGID = hashMapOfAllGNamesAndGIDsFromDB.get(strGermplasmSelected);
			hashMapOfGIDsAndGNamesSelected.put(iGID, strGermplasmSelected);
		}*/

		/*ArrayList glist = new ArrayList();
		ArrayList midslist = new ArrayList();
		String data="";
		////System.out.println("listOfMarkersSelected=:"+listOfMarkersSelected);
		////System.out.println("listOfGIDsSelected:"+listOfGIDsSelected);
		try {
			List<AllelicValueElement> allelicValues =genoManager.getAllelicValuesByGidsAndMarkerNames(listOfGIDsSelected, listOfMarkersSelected);
			
			////System.out.println(" allelicValues =:"+allelicValues);		
			marker = new HashMap();
			if (null != allelicValues){
				for (AllelicValueElement allelicValueElement : allelicValues){
					if(!(midslist.contains(allelicValueElement.getMarkerName())))
						midslist.add(allelicValueElement.getMarkerName());
					
					//data=data+allelicValueElement.getGid()+"~!~"+allelicValueElement.getData()+"~!~"+allelicValueElement.getMarkerName()+"!~!";
					markerAlleles.put(allelicValueElement.getGid()+"!~!"+allelicValueElement.getMarkerId(), allelicValueElement.getData());
					
					if(!(glist.contains(allelicValueElement.getGid())))
						glist.add(allelicValueElement.getGid());					
				}
				
				markerKey = new ArrayList();
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
			

		} catch (Exception e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving list of AllelicValueElement for the selected GIDs required for Matrix format.", Notification.TYPE_ERROR_MESSAGE);
			String strErrMsg = "Error retrieving list of AllelicValueElement for the selected GIDs required for Matrix format.";
			throw new GDMSException(strErrMsg);
		}*/

	}

	private Component buildGermplasmResultComponent() {
		VerticalLayout resultsLayout = new VerticalLayout();
		resultsLayout.setCaption("Results");
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true, true, true, true);

		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		//////System.out.println(absoluteFile);


		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("WEB-INF")) {
				fileExport = file;
				break;
			}
		}

		final String strAbsolutePath = fileExport.getAbsolutePath();
			
		
		final String strFJVisualizeLink = strAbsolutePath + "\\" + "flapjackrun.bat";
		//////System.out.println(strFJVisualizeLink);
		realPath=_mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory().toString();
		
		HorizontalLayout horLayoutForExportTypes = new HorizontalLayout();
		horLayoutForExportTypes.setSpacing(true);		
		
		Button btnVisualizeFJ = new Button("Visualize in Flapjack");
		//btnVisualizeFJ.setStyleName(Reindeer.BUTTON_LINK);
		btnVisualizeFJ.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				
				File fexists=new File(realPath+"/Flapjack/Flapjack.flapjack");
				if(fexists.exists()) { 
					fexists.delete();				
				}
				String[] cmd = {"cmd.exe", "/c", "start", "\""+"flapjack"+"\"", strFJVisualizeLink};
				Runtime rt = Runtime.getRuntime();
				try {
					rt.exec(cmd);
				} catch (IOException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error occurred while trying to create Flapjack.flapjack project.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});

		horLayoutForExportTypes.addComponent(btnVisualizeFJ);
		
		
		Button btnDownloadMatrixFile = new Button("Download Matrix File");
		btnDownloadMatrixFile.setStyleName(Reindeer.BUTTON_LINK);
		btnDownloadMatrixFile.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				_mainHomePage.getMainWindow().getWindow().open(new FileDownloadResource(
						matrixFile, _mainHomePage.getMainWindow().getWindow().getApplication()));
			}
		});


		HorizontalLayout layoutForExportTypes = new HorizontalLayout();
		layoutForExportTypes.setSpacing(true);

		ThemeResource themeResource = new ThemeResource("images/excel.gif");
		Button excelButton = new Button();
		excelButton.setIcon(themeResource);
		excelButton.setStyleName(Reindeer.BUTTON_LINK);
		excelButton.setDescription("EXCEL Format");
		excelButton.addListener(this);
		if (null == strSelectedFormat){
			layoutForExportTypes.addComponent(excelButton);
		}

		//////System.out.println("Selected Format: " + strSelectedFormat);
		//20131216: Added link to download Similarity Matrix File
		final String strSMVisualizeLink = strAbsolutePath + "\\" + "flapjackMatrix.bat";
		Button similarityMatrixButton = new Button("Show Similarity Matrix");
		//similarityMatrixButton.setStyleName(Reindeer.BUTTON_LINK);
		similarityMatrixButton.setDescription("Similarity Matrix File");
		similarityMatrixButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				/*File similarityMatrixFile = new File(""); 
				FileResource fileResource = new FileResource(similarityMatrixFile, _mainHomePage);
				_mainHomePage.getMainWindow().getWindow().open(fileResource, "Similarity Matrix File", true);*/
				File similarityMatrixFile = new File(""); 
				String[] cmd = {"cmd.exe", "/c", "start", "\""+"flapjack"+"\"", strSMVisualizeLink};
				Runtime rtSM = Runtime.getRuntime();
				try {
					rtSM.exec(cmd);
				
				} catch (IOException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error occurred while trying to create Similarity Matrix.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				/*FileResource fileResource = new FileResource(similarityMatrixFile, _mainHomePage);
				_mainHomePage.getMainWindow().getWindow().open(fileResource, "Similarity Matrix File", true);*/
			}
		});
		//20131216: Added link to download Similarity Matrix File
		horLayoutForExportTypes.addComponent(similarityMatrixButton);
		
		VerticalLayout flapjackFilesLayout = new VerticalLayout();
		//flapjackFilesLayout.setCaption("Results");
		flapjackFilesLayout.setSpacing(true);
		flapjackFilesLayout.setMargin(true, true, true, true);

		Label lblFlapjackFiles = new Label("Download Flapjack Files");
		lblFlapjackFiles.setStyleName(Reindeer.LABEL_H2);
		flapjackFilesLayout.addComponent(lblFlapjackFiles);
		flapjackFilesLayout.setComponentAlignment(lblFlapjackFiles, Alignment.TOP_CENTER);
		
		
		HorizontalLayout layoutForFlapjackfiles = new HorizontalLayout();
		layoutForFlapjackfiles.setSpacing(true);	
		
		Button btnFlapjackGenoFiles = new Button("Genotype");
		btnFlapjackGenoFiles.setStyleName(Reindeer.BUTTON_LINK);
		btnFlapjackGenoFiles.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {				
				try {
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hh:mm");
					String strDate = sdf.format(new Date());
					
					File directory = new File(folderPath);
					File[] fList = directory.listFiles();
					for (File file : fList){
						if (file.isFile()){
							//System.out.println(file.getName());
							if(file.getName().equalsIgnoreCase("FlapjackGenotype.dat"))
								flapjackDat=file;												
						}
					}
					FileResource fileResourceDat = new FileResource(flapjackDat, _mainHomePage); 
					_mainHomePage.getMainWindow().getWindow().open(fileResourceDat, "_blank", true);
					
				} catch (Exception e) {
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				} 
			}
		});
		
		Button btnFlapjackMapFiles = new Button("Map");
		btnFlapjackMapFiles.setStyleName(Reindeer.BUTTON_LINK);
		btnFlapjackMapFiles.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {			
				try {					
					File directory = new File(folderPath);
					File[] fList = directory.listFiles();
					for (File file : fList){
						if (file.isFile()){							
							if(file.getName().equalsIgnoreCase("FlapjackMap.map"))
								flapjackMap=file;											
						}
					}
					FileResource fileResourceMap = new FileResource(flapjackMap, _mainHomePage);					
					
					_mainHomePage.getMainWindow().getWindow().open(fileResourceMap, "_blank", true);
					
				} catch (Exception e) {
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});
		
		
		Button btnFlapjackQTLFiles = new Button("QTL");
		if(!strQTLExists.isEmpty()){
			if(strQTLExists.equalsIgnoreCase("yes")){
				btnFlapjackQTLFiles.setStyleName(Reindeer.BUTTON_LINK);
				btnFlapjackQTLFiles.addListener(new Button.ClickListener() {
					private static final long serialVersionUID = 1L;
					public void buttonClick(ClickEvent event) {				
						try {
							
							File directory = new File(folderPath);
							File[] fList = directory.listFiles();
							for (File file : fList){
								if (file.isFile()){							
									if(file.getName().equalsIgnoreCase("FlapjackQTL.txt"))
										flapjackTxt=file;							
								}
							}
							FileResource fileResourceQTL = new FileResource(flapjackTxt, _mainHomePage);					
							
							_mainHomePage.getMainWindow().getWindow().open(fileResourceQTL, "_blank", true);
							
						} catch (Exception e) {
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}
				});
			}
		}
		
		
		layoutForFlapjackfiles.addComponent(btnFlapjackGenoFiles);
		layoutForFlapjackfiles.addComponent(btnFlapjackMapFiles);
		if((!strQTLExists.isEmpty()) && (strQTLExists.equalsIgnoreCase("yes"))){
			layoutForFlapjackfiles.addComponent(btnFlapjackQTLFiles);
		}
		
		flapjackFilesLayout.addComponent(layoutForFlapjackfiles);
				
		if (null != strSelectedFormat){
			if (strSelectedFormat.equals("Flapjack")){				
				resultsLayout.addComponent(horLayoutForExportTypes);
				resultsLayout.addComponent(flapjackFilesLayout);				
			} else if (strSelectedFormat.equals("Matrix")) {
				resultsLayout.addComponent(btnDownloadMatrixFile);
			}
		} else {
			excelButton.setEnabled(false);
			/*pdfButton.setEnabled(false);
			printButton.setEnabled(false);*/
		}
		resultsLayout.addComponent(layoutForExportTypes);
		resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);

		return resultsLayout;
	}


	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}

}
