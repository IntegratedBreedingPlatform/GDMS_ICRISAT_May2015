package org.icrisat.gdms.ui;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.dao.gdms.AccMetadataSetDAO;
import org.generationcp.middleware.dao.gdms.AlleleValuesDAO;
import org.generationcp.middleware.dao.gdms.CharValuesDAO;
import org.generationcp.middleware.dao.gdms.MapDAO;
import org.generationcp.middleware.dao.gdms.MappingDataDAO;
import org.generationcp.middleware.dao.gdms.MappingPopDAO;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.dao.gdms.QtlDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AccMetadataSet;
//import org.generationcp.middleware.pojos.gdms.AccMetadataSetPK;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.GermplasmMarkerElement;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.MapDetailElement;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.generationcp.middleware.pojos.gdms.MappingValueElement;
import org.generationcp.middleware.pojos.gdms.Marker;
//import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.gdms.MarkerMetadataSet;
import org.generationcp.middleware.pojos.gdms.Qtl;
import org.generationcp.middleware.pojos.gdms.QtlDetails;
//import org.generationcp.middleware.util.Debug;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.retrieve.RetrieveQTL;
import org.icrisat.gdms.ui.common.FileDownloadResource;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.ui.common.OptionWindowForFlapjackMap;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
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
import com.vaadin.ui.Link;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;
//import org.generationcp.middleware.dao.TraitDAO;
//import org.generationcp.middleware.pojos.Trait;


public class RetrieveMarkersComponent  implements Component.Listener {

	private static final long serialVersionUID = 1L;

	private GDMSMain _mainHomePage;
	private TabSheet _tabsheetForMarkers;
	private Component buildMarkerResultComponent;
	private Component buildMarkerGermplasmComponent;
	private Component buildFormatComponent;
	private FileUploadComponent uploadComponent;
	private TextArea textArea;
	private CheckBox chbMatrix;
	private CheckBox chbFlapjack;
	private final String MATRIX_FORMAT = "Genotyping X Marker Matrix"; 
	private final String FLAPJACK_FORMAT = "Flapjack";
	private ArrayList<String> listGermplasmsEnteredInTheSearchField;
	private Object strSelectedFormat;
	private ArrayList<String> listOfGermplasmsByMarkers;
	protected ArrayList<String> listOfMarkersProvided;
	protected ArrayList<String> listGermplasmsSelected;
	private Session localSession;
	private Session centralSession;
	private ArrayList<Integer> listOfGIDs;
	private MarkerDAO markerDAOLocal;
	private MarkerDAO markerDAOCentral;
	private ArrayList<Integer> listOfMarkerIds;
	private AlleleValuesDAO alleleValuesDAOLocal;
	private AlleleValuesDAO alleleValuesDAOCentral;
	private HashMap<String, Integer> hmOfGNamesAndGids;
	private ArrayList<Integer> listOfGIDsSelected;
	private ArrayList<GermplasmMarkerElement> listOfGermplasmMarkerElements;
	private ArrayList<AllelicValueElement> listOfAllelicValueElements;
	private ArrayList<MappingValueElement> listOfMappingValueElements;
	private ArrayList<String> listOfMarkerNameElement;
	private File matrixFile;
	private HashMap<Object, String> hmOfGIDsAndGermplamsSelected;
	ArrayList<Integer> listOfNameIDs = new ArrayList<Integer>();
	protected Integer iSelectedMapId;
	HashMap<Object, HashMap<String, Object>> mapEx = new HashMap<Object, HashMap<String,Object>>();	
	HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
	HashMap marker = new HashMap();
	/*HashMap<Integer, HashMap<String, Object>> mapEx = new HashMap<Integer, HashMap<String,Object>>();	
	HashMap<String,Object> markerAlleles= new HashMap<String,Object>();*/
	
	HashMap<String , Integer> hmMarkerNamesIDs;
	
	
	//HashMap marker = new HashMap();
	ArrayList datasetIDs= new ArrayList();
	
	String strMap = "";
	String realPath="";
	
	public File flapjackDat;
	public File flapjackMap;
 	public File flapjackTxt;
	
	
	public String folderPath;
	public String strQTLExists="";
	
	protected File generatedTextFile;
	protected File generatedMapFile;
	protected File generatedDatFile;
	
	private String _strSeletedFlapjackType = null;
	private List<File> generateFlagjackFiles;
	protected String strSelectedMap;
	protected String strSelectedColumn;
	ManagerFactory factory=null;
	OntologyDataManager om;
	GermplasmDataManager manager;
	GenotypicDataManager genoManager;
	private Button markersSampleFile;
	private HashMap<Integer, String> hmOfSelectedMIDandMNames;
	
	SQLQuery queryL;
	SQLQuery queryC;
	String strQuerry="";
	ArrayList glist = new ArrayList();
	private HashMap<Object, String> hmOfGIdAndNval;
	
	private TreeMap<Object, String> sortedMapOfGIDAndGName;
	
	HashMap<Object, String> hmOfGIDs;
	public RetrieveMarkersComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		try{
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			factory = GDMSModel.getGDMSModel().getManagerFactory();
			om=factory.getOntologyDataManager();
			manager = factory.getGermplasmDataManager();
			genoManager=factory.getGenotypicDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	public HorizontalLayout buildTabbedComponentForMarkers() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();

		_tabsheetForMarkers = new TabSheet();
		//_tabsheetForMarkers.setSizeFull();
		_tabsheetForMarkers.setWidth("700px");
		
		Component buildGIDComponent = buildMarkersComponent();

		buildMarkerGermplasmComponent = buildMarkerGermplasmComponent();

		buildFormatComponent = buildFormatComponent();

		buildMarkerResultComponent = buildResultComponent();

		//buildGIDComponent.setSizeFull();
		//buildMarkerGermplasmComponent.setSizeFull();
		//buildFormatComponent.setSizeFull();
		//buildMarkerResultComponent.setSizeFull();
		
		_tabsheetForMarkers.addComponent(buildGIDComponent);
		_tabsheetForMarkers.addComponent(buildMarkerGermplasmComponent);
		_tabsheetForMarkers.addComponent(buildFormatComponent);
		_tabsheetForMarkers.addComponent(buildMarkerResultComponent);
		
		_tabsheetForMarkers.getTab(1).setEnabled(false);
		_tabsheetForMarkers.getTab(2).setEnabled(false);
		_tabsheetForMarkers.getTab(3).setEnabled(false);

		horizontalLayout.addComponent(_tabsheetForMarkers);

		return horizontalLayout;
	}


	private Component buildMarkersComponent() {
		VerticalLayout layoutForGIDTab = new VerticalLayout();
		layoutForGIDTab.setCaption("Markers");
		layoutForGIDTab.setSpacing(true);
		layoutForGIDTab.setSizeFull();
		layoutForGIDTab.setMargin(true, true, true, true);

		VerticalLayout verticalLayoutOne = new VerticalLayout();
		Label label1 = new Label("Upload text file with desired Markers");
		label1.setStyleName(Reindeer.LABEL_H2);

		label1.setStyleName(Reindeer.LABEL_H2);
		markersSampleFile = new Button("Sample File");
		markersSampleFile.setImmediate(true);
		markersSampleFile.setStyleName(Reindeer.BUTTON_LINK);
		markersSampleFile.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
                
				WebApplicationContext ctx = (WebApplicationContext) _mainHomePage.getContext();
                String strTemplateFolderPath = ctx.getHttpSession().getServletContext().getRealPath("\\VAADIN\\themes\\gdmstheme\\Templates");
                //System.out.println("Folder-Path: " + strTemplateFolderPath);
                
				//String strMarkerType = _strMarkerType.replace(" ", "");
				String strFileName = "Markers.txt";
				
				File strFileLoc = new File(strTemplateFolderPath + "\\" + strFileName);
				FileResource fileResource = new FileResource(strFileLoc, _mainHomePage);
				//_mainHomePage.getMainWindow().getWindow().open(fileResource, "_blank", true);
				
				if (strFileName.endsWith(".txt")) {
					_mainHomePage.getMainWindow().getWindow().open(fileResource, "GIDs", true);
				} 				
			}
			
		});
		
		
		uploadComponent = new FileUploadComponent(_mainHomePage, "Marker Name");
		uploadComponent.setWidth("90%");
		//uploadComponent.init("basic");
		//20131205: Tulasi --- Modified the code to display the Markers read from the text file in the TextArea and then proceed to the next tab upon clicking Next
		uploadComponent.registerListener(new FileUploadListener() {
			@Override
			public void updateLocation(String absolutePath) {
				List<String> listOfMarkesFromTextFile = uploadComponent.getListOfMarkers();
				if (null != listOfMarkesFromTextFile) {
					String strListOfMarkersFromTextFile = "";
					for (String strMarker : listOfMarkesFromTextFile) {
						strListOfMarkersFromTextFile += String.valueOf(strMarker) + ",";
					}
					if (0 != strListOfMarkersFromTextFile.trim().length()) {
						strListOfMarkersFromTextFile = strListOfMarkersFromTextFile.substring(0, strListOfMarkersFromTextFile.length()-1);
						textArea.setValue(strListOfMarkersFromTextFile);
					}
				}
			}
		});
		//20131205: Tulasi --- Modified the code to display the GIDs read from the text file in the TextArea and then proceed to the next tab upon clicking Next
		
		verticalLayoutOne.addComponent(label1);
		verticalLayoutOne.addComponent(uploadComponent);
		
		verticalLayoutOne.addComponent(markersSampleFile);
		verticalLayoutOne.setComponentAlignment(markersSampleFile, Alignment.BOTTOM_CENTER);
		
		VerticalLayout verticalLayoutTwo = new VerticalLayout();
		verticalLayoutTwo.setSpacing(true);
		Label label2 = new Label("Enter Markers separated by commas");
		label2.setStyleName(Reindeer.LABEL_H2);

		textArea = new TextArea();
		//textArea.setWidth("200px");
		textArea.setWidth("90%");
		layoutForGIDTab.addComponent(textArea);

		Button btnNext = new Button("Next");
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {

				if (false == textArea.getValue().toString().equals("")){
					listOfMarkersProvided = obtainListOfMarkersInTextArea();
				} else {
					listOfMarkersProvided = uploadComponent.getListOfMarkers();
				}
				System.out.println("listOfMarkersProvided..............:"+listOfMarkersProvided);
				if (null == listOfMarkersProvided || 0 == listOfMarkersProvided.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please provide the list of Markers.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				boolean bObtainedDataRequiredForNextTab = false;
				try {
					retrieveTheGermplasmNames();
					
					if (null == listOfGermplasmsByMarkers || 0 == listOfGermplasmsByMarkers.size()) {
						bObtainedDataRequiredForNextTab = false;
						_mainHomePage.getMainWindow().getWindow().showNotification("Genotyping data for the provided marker(s) doesnot exist.", Notification.TYPE_ERROR_MESSAGE);
						textArea.requestRepaint();
						//textArea = new TextArea();
						return;
					}
					
					
					bObtainedDataRequiredForNextTab = true;
				} catch (GDMSException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Genotyping data for the provided marker(s) doesnot exist", Notification.TYPE_ERROR_MESSAGE);
					textArea.requestRepaint();
					return;
				}
				
				if (bObtainedDataRequiredForNextTab){
					VerticalLayout newMarkerGermplasmComponent = (VerticalLayout) buildMarkerGermplasmComponent();
					_tabsheetForMarkers.replaceComponent(buildMarkerGermplasmComponent, newMarkerGermplasmComponent);
					buildMarkerGermplasmComponent = newMarkerGermplasmComponent;
					buildMarkerGermplasmComponent.requestRepaint();
					_tabsheetForMarkers.getTab(1).setEnabled(true);
					_tabsheetForMarkers.setSelectedTab(buildMarkerGermplasmComponent);
					_tabsheetForMarkers.requestRepaint();
				} else {
					_mainHomePage.getMainWindow().getWindow().showNotification("Genotyping data for the provided marker(s) doesnot exist", Notification.TYPE_ERROR_MESSAGE);
					textArea.requestRepaint();
					return;
				}
					
				}
		});

		verticalLayoutTwo.addComponent(label2);
		verticalLayoutTwo.addComponent(textArea);
		verticalLayoutTwo.addComponent(btnNext);

		layoutForGIDTab.addComponent(verticalLayoutOne);
		layoutForGIDTab.addComponent(verticalLayoutTwo);

		return layoutForGIDTab;
	}


	protected void retrieveTheGermplasmNames() throws GDMSException {
		
		listOfGIDs = new ArrayList<Integer>();
		
		//exportFileFormats.Matrix(listOfGIDs, listOfMarkerElementsFromDB, listOfMarkersProvided, listOfAllelicValues, listGermplasmsSelected, listOfMappingValues);

		/*markerDAOLocal = new MarkerDAO();
		markerDAOLocal.setSession(localSession);
		markerDAOCentral = new MarkerDAO();
		markerDAOCentral.setSession(centralSession);*/
		listOfMarkerIds = new ArrayList<Integer>();
		//System.err.println("listOfMarkersProvided:"+listOfMarkersProvided);
		hmMarkerNamesIDs=new HashMap<String, Integer>();
		try {
			//System.out.println(listOfMarkersProvided);
			//genoManager.getMarker
			List<Marker> listOfMarkerIdsFromLocal =genoManager.getMarkersByMarkerNames(listOfMarkersProvided, 0, listOfMarkersProvided.size(), Database.LOCAL);
			
			List<Marker> listOfMarkerIDsFromCentral = genoManager.getMarkersByMarkerNames(listOfMarkersProvided, 0, listOfMarkersProvided.size(), Database.CENTRAL);
			//System.out.println("listOfMarkerIDsFromCentral:"+listOfMarkerIDsFromCentral);
			if(! listOfMarkerIDsFromCentral.isEmpty()){
				for (Marker iMarkerIDC : listOfMarkerIDsFromCentral){
					//if (false == listOfMarkerIds.contains(iMarkerIDC.getMarkerId())){
					if (! listOfMarkerIds.contains(iMarkerIDC.getMarkerId())){
						listOfMarkerIds.add(iMarkerIDC.getMarkerId());
						hmMarkerNamesIDs.put(iMarkerIDC.getMarkerName(), iMarkerIDC.getMarkerId());
					}
				}
			}
			//System.out.println("listOfMarkerIdsFromLocal=:"+listOfMarkerIdsFromLocal);
			if(! listOfMarkerIdsFromLocal.isEmpty()){
				for (Marker iMarkerID : listOfMarkerIdsFromLocal){
					if (false == listOfMarkerIds.contains(iMarkerID.getMarkerId())){
						listOfMarkerIds.add(iMarkerID.getMarkerId());
						hmMarkerNamesIDs.put(iMarkerID.getMarkerName(), iMarkerID.getMarkerId());
					}
				}
			}
			System.out.println("hmMarkerNamesIDs:"+hmMarkerNamesIDs);
			System.out.println("listOfMarkerIds:"+listOfMarkerIds);
			/*List<Integer> listOfMarkerIdsFromLocal =genoManager.getMarkerIdsByMarkerNames(listOfMarkersProvided, 0, listOfMarkersProvided.size(), Database.LOCAL);			
			List<Integer> listOfMarkerIDsFromCentral = genoManager.getMarkerIdsByMarkerNames(listOfMarkersProvided, 0, listOfMarkersProvided.size(), Database.CENTRAL);
			for (Integer iMarkerID : listOfMarkerIDsFromCentral){
				if (false == listOfMarkerIds.contains(iMarkerID)){
					listOfMarkerIds.add(iMarkerID);
				}
			}
			for (Integer iMarkerID : listOfMarkerIdsFromLocal){
				if (false == listOfMarkerIds.contains(iMarkerID)){
					listOfMarkerIds.add(iMarkerID);
				}
			}*/
			
		} catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Marker-IDs for the Markers provided", Notification.TYPE_ERROR_MESSAGE);
			//return;
			throw new GDMSException("Error Retrieving Marker-IDs for the Markers provided");
		}
	
		
		alleleValuesDAOLocal = new AlleleValuesDAO();
		alleleValuesDAOLocal.setSession(localSession);
		alleleValuesDAOCentral = new AlleleValuesDAO();
		alleleValuesDAOCentral.setSession(centralSession);
		datasetIDs= new ArrayList();
		
		try{
			/*for (int i = 0; i < listOfMarkerIds.size(); i++){
				Integer iMarkerID = listOfMarkerIds.get(i);
				List<MarkerMetadataSet> result = genoManager.getAllFromMarkerMetadatasetByMarker(iMarkerID);
				if (result != null) {
		            for (MarkerMetadataSet elem : result) {
		               // Debug.println(4, elem.toString());
		            	datasetIDs.add(elem.getDatasetId());
		            }
		        }
			}*/
			List<MarkerMetadataSet> result = genoManager.getAllFromMarkerMetadatasetByMarkers(listOfMarkerIds);
			if (result != null) {
	            for (MarkerMetadataSet elem : result) {	               
	            	datasetIDs.add(elem.getDatasetId());
	            }
	        }
		} catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving GIDs for the Markers provided", Notification.TYPE_ERROR_MESSAGE);
			//return;
			throw new GDMSException("Error Retrieving GIDs for the Markers provided");
		}
		
		if (0 == datasetIDs.size()){
			//_mainHomePage.getMainWindow().getWindow().showNotification("No Genotyping data for the provided marker(s)", Notification.TYPE_ERROR_MESSAGE);
			//return;
			throw new GDMSException("No Genotyping data for the provided marker(s)");
		}
		try{
			listOfNameIDs = new ArrayList<Integer>();
			//List<Integer> nids = genoManager.getNidsFromAccMetadatasetByDatasetIds(datasetIDs, 0, 20000);
			List<AccMetadataSet> resAcc=genoManager.getAccMetadatasetsByDatasetIds(datasetIDs, 0, (int)genoManager.countAccMetadatasetByDatasetIds(datasetIDs));
			for(AccMetadataSet resAccMetadataset: resAcc){
					
				listOfNameIDs.add(resAccMetadataset.getNameId());
			}
			
			/*List<Integer> nids = genoManager.getNidsFromAccMetadatasetByDatasetIds(datasetIDs, 0, 20000);
			for(int n=0; n<nids.size(); n++){
				listOfNameIDs.add(Integer.parseInt(nids.get(n).toString()));
			}*/
			//System.out.println("listOfNameIDs:"+listOfNameIDs);
		} catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving GIDs for the Markers provided", Notification.TYPE_ERROR_MESSAGE);
			//return;
			
			throw new GDMSException("Error Retrieving GIDs for the Markers provided");
		}
		
		Name names = null;
		
		try {
			//System.out.println(genoManager.getNamesByNameIds(listOfNameIDs));
			listOfGermplasmsByMarkers = new ArrayList<String>();
			hmOfGNamesAndGids = new HashMap<String, Integer>();
			List<Name> nameResults=genoManager.getNamesByNameIds(listOfNameIDs);
			for(Name res:nameResults){
				listOfGermplasmsByMarkers.add(res.getNval());
				hmOfGNamesAndGids.put(res.getNval(), res.getGermplasmId());
			}
			/*for(int n=0;n<listOfNameIDs.size();n++){
				//System.out.println(genoManager.getNamesByNameIds(listOfNameIDs));
				names=manager.getGermplasmNameByID(Integer.parseInt(listOfNameIDs.get(n).toString()));
				System.out.println("names:"+names);
				String nval = names.getNval();
				Integer germplasmId = names.getGermplasmId();
				if (false == listOfGermplasmsByMarkers.contains(nval)){
					listOfAllGermplasmNamesForGIDsProvided.add(nval);
					hmOfGIDAndGName.put(germplasmId, nval);
					
					listOfGermplasmsByMarkers.add(nval);
					hmOfGNamesAndGids.put(nval, germplasmId);
				}				
			}	*/		
			
		} catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Names for the GIDs", Notification.TYPE_ERROR_MESSAGE);
			//return;
			throw new GDMSException("Error Retrieving Names for the GIDs");
		}	
	
	}


	protected ArrayList<String> obtainListOfMarkersInTextArea() {
		ArrayList<String> listOfMarkers = new ArrayList<String>();
		String strTextWithMarkers = textArea.getValue().toString();
		String[] arrayOfMarkers = strTextWithMarkers.split(",");
		for (int i = 0; i < arrayOfMarkers.length; i++){
			String strMarker = arrayOfMarkers[i].trim();
			listOfMarkers.add(strMarker);
		}
		return listOfMarkers;
	}


	private Component buildMarkerGermplasmComponent() {
		VerticalLayout layoutForGIDMarkerTab = new VerticalLayout();
		layoutForGIDMarkerTab.setCaption("Germplasms");
		layoutForGIDMarkerTab.setSpacing(true);
		layoutForGIDMarkerTab.setSizeFull();
		layoutForGIDMarkerTab.setMargin(true, true, true, true);

		Label lblTitle = new Label("Select the Germplasms from the list");
		if (null != listOfGermplasmsByMarkers){
			if (0 < listOfGermplasmsByMarkers.size()){
				lblTitle = new Label("Select the Germplasms from the list of " + listOfGermplasmsByMarkers.size());
			}
		}
		lblTitle.setStyleName(Reindeer.LABEL_H2);


		int iNumOfMarkers = 0;
		if (null != listOfGermplasmsByMarkers && 0 < listOfGermplasmsByMarkers.size()){
			iNumOfMarkers = listOfGermplasmsByMarkers.size();
		}
		
		final TwinColSelect selectForGermplasms = new TwinColSelect();
		selectForGermplasms.setLeftColumnCaption("All Germplasms");
		selectForGermplasms.setRightColumnCaption("Selected Germplasms");
		//selectForGermplasms.setNullSelectionAllowed(false);
		//selectForGermplasms.setInvalidAllowed(false);
		//select.setWidth("500px");
		if (null != listOfGermplasmsByMarkers && 0 < listOfGermplasmsByMarkers.size()){
			for (String strGermplasm : listOfGermplasmsByMarkers) {
				selectForGermplasms.addItem(strGermplasm);
			}
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
                	listGermplasmsSelected = new ArrayList<String>();
                	for (String string : hashSet) {
                		listGermplasmsSelected.add(string);
					}
                }
            }
        });
		selectForGermplasms.setImmediate(true);
		
		
		HorizontalLayout horizLytForSelectComponent = new HorizontalLayout();
		horizLytForSelectComponent.setSizeFull();
		horizLytForSelectComponent.setSpacing(true);
		horizLytForSelectComponent.setMargin(true);
		horizLytForSelectComponent.addComponent(selectForGermplasms);


		final CheckBox chbSelectAll = new CheckBox("Select All");
		chbSelectAll.setImmediate(true);
		chbSelectAll.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				selectForGermplasms.setValue(listOfGermplasmsByMarkers);
			}
		});


		final TextField txtFieldSearch = new TextField();
		txtFieldSearch.setWidth("300px");
		txtFieldSearch.addListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;

			public void textChange(TextChangeEvent event) {
				if (null == listGermplasmsEnteredInTheSearchField) {
					listGermplasmsEnteredInTheSearchField = new ArrayList<String>();
				}
				
				if (null != txtFieldSearch.getValue()){
					String strGermplasmFromTextField = txtFieldSearch.getValue().toString();
					if (strGermplasmFromTextField.endsWith("*")){
						int indexOf = strGermplasmFromTextField.indexOf('*');
						String substring = strGermplasmFromTextField.substring(0, indexOf);
						
						for (String strGName : listOfGermplasmsByMarkers){
							//20131205: Tulasi --- modified the condition to check for the initial string for both upper and lower case alphabets
							if (strGName.toUpperCase().startsWith(substring) ||
								strGName.toLowerCase().startsWith(substring)) {
								listGermplasmsEnteredInTheSearchField.add(strGName);
							}
						}
					} else if (strGermplasmFromTextField.trim().equals("*")) {
						listGermplasmsEnteredInTheSearchField.addAll(listOfGermplasmsByMarkers);
					}
					selectForGermplasms.setValue(listGermplasmsEnteredInTheSearchField);
				}
			}
			
		});
		

		ThemeResource themeResource = new ThemeResource("images/find-icon.png");
		Button searchButton = new Button();
		searchButton.setIcon(themeResource);
		searchButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {

				listGermplasmsEnteredInTheSearchField = new ArrayList<String>();
				
				if (null != txtFieldSearch.getValue()){
					String strGermplasmFromTextField = txtFieldSearch.getValue().toString();
					if (strGermplasmFromTextField.endsWith("*")){
						int indexOf = strGermplasmFromTextField.indexOf('*');
						String substring = strGermplasmFromTextField.substring(0, indexOf);
						
						for (String strGName : listOfGermplasmsByMarkers){
							//20131205: Tulasi --- modified the condition to check for the initial string for both upper and lower case alphabets
							if (strGName.toLowerCase().startsWith(substring) ||
								strGName.toUpperCase().startsWith(substring)) {
								listGermplasmsEnteredInTheSearchField.add(strGName);
							}
						}
					} else if (strGermplasmFromTextField.trim().equals("*")) {
						listGermplasmsEnteredInTheSearchField.addAll(listOfGermplasmsByMarkers);
					}else{
						listGermplasmsEnteredInTheSearchField.add(strGermplasmFromTextField);
					}
					selectForGermplasms.setValue(listGermplasmsEnteredInTheSearchField);
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

				listGermplasmsSelected = new ArrayList<String>();
				Object value2 = selectForGermplasms.getValue();
            	Set<String> hashSet = (Set<String>) value2;
            	for (String string : hashSet) {
            		listGermplasmsSelected.add(string);
				}
		
				//retrieveDataForSelectedGermplasms();
				/*if (null == listGermplasmsSelected){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Germplasms to be exported and click Next.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}*/
            	
            	if (0 == listGermplasmsSelected.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Germplasms to be exported and click Next.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (0 != listGermplasmsSelected.size()) {
					Component newFormatComponent = buildFormatComponent();
					_tabsheetForMarkers.replaceComponent(buildFormatComponent, newFormatComponent);
					_tabsheetForMarkers.requestRepaint();
					buildFormatComponent = newFormatComponent;
					_tabsheetForMarkers.getTab(2).setEnabled(true);
					_tabsheetForMarkers.setSelectedTab(buildFormatComponent);
				} else {
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Germplasms to be exported and click Next.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
			}
		});
		
		if (0 == iNumOfMarkers){
			chbSelectAll.setEnabled(false);
			txtFieldSearch.setEnabled(false);
			searchButton.setEnabled(false);
			btnNext.setEnabled(false);
			selectForGermplasms.setEnabled(false);
		} else {
			chbSelectAll.setEnabled(true);
			txtFieldSearch.setEnabled(true);
			searchButton.setEnabled(true);
			btnNext.setEnabled(true);
			selectForGermplasms.setEnabled(true);
		}

		layoutForGIDMarkerTab.addComponent(lblTitle);
		layoutForGIDMarkerTab.setComponentAlignment(lblTitle, Alignment.TOP_CENTER);
		layoutForGIDMarkerTab.addComponent(horizontalLayout);
		layoutForGIDMarkerTab.addComponent(horizLytForSelectComponent);
		layoutForGIDMarkerTab.addComponent(horizontalLayoutForButton);
		layoutForGIDMarkerTab.setComponentAlignment(horizontalLayoutForButton, Alignment.MIDDLE_CENTER);

		return layoutForGIDMarkerTab;
	}


	protected void retrieveDataForSelectedGermplasms() throws GDMSException {
		
		//exportFileFormats.Matrix(listOfGIDs, listOfMarkerNameElement, listOfMarkersProvided, listOfAllelicValueElements, listOfGermplasmMarkerElements, listOfMappingValueElements);
		
	    listOfGIDsSelected = new ArrayList<Integer>();
		hmOfGIDsAndGermplamsSelected = new HashMap<Object, String>();
		for (String strGermplasmSelected : listGermplasmsSelected){
			Integer iGID = hmOfGNamesAndGids.get(strGermplasmSelected);
			listOfGIDsSelected.add(iGID);
			hmOfGIDsAndGermplamsSelected.put(iGID, strGermplasmSelected);
		}
		System.out.println("listOfGIDsSelected:"+listOfGIDsSelected);
		String strGids="";
		for(int g=0;g<listOfGIDsSelected.size();g++){
			strGids=strGids+listOfGIDsSelected.get(g)+",";
		}
		strGids=strGids.substring(0, strGids.length()-1);
		String strMids="";
		
		for(int m=0;m<listOfMarkerIds.size();m++){
			strMids=strMids+listOfMarkerIds.get(m)+",";
		}
		strMids=strMids.substring(0, strMids.length()-1);
		//Retrieving list of MarkerNameElements
		listOfMarkerNameElement = new ArrayList<String>();
		/*try {
			
			
			List<MarkerIdMarkerNameElement> markerNames=genoManager.getMarkerNamesByMarkerIds(listOfMarkerIds);
			for (MarkerIdMarkerNameElement markerNameElement : markerNames){
				if (false == listOfMarkerNameElement.contains(markerNameElement.getMarkerName())){
					listOfMarkerNameElement.add(markerNameElement.getMarkerName());
				}
			}
			
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException("Error retrieving list of MarkerNameElements required for Matrix format.");
		}*/
		
		glist = new ArrayList();
		ArrayList midslist = new ArrayList();
		String data="";
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
		System.out.println(strQuerry);
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
		
		System.out.println("markerAlleles:"+markerAlleles);
		System.out.println("glist:"+glist);
		
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
		System.out.println("mapEx:"+mapEx);
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
		 System.out.println("...........................SRIKALYANI");
		String strQuerry="select distinct gid,nid, acc_sample_id from gdms_acc_metadataset where gid in ("+ strGids +") order by gid, nid,acc_sample_id asc";	
		System.out.println("strQuerry:"+strQuerry);
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
		hmOfGIDsAndGermplamsSelected = new HashMap<Object, String>();
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
			
			hmOfGIDsAndGermplamsSelected.put(gidSampleid, gname);
			hmOfGIDs.put(gidSampleid, gids);
			
		}
		System.out.println("hashMapOfGIDsAndGNamesSelected:"+hmOfGIDsAndGermplamsSelected);
	
		
		/*try{
			
			List<AllelicValueElement> allelicValues =genoManager.getAllelicValuesByGidsAndMarkerNames(listOfGIDsSelected, listOfMarkerNameElement);
			
			//System.out.println(" allelicValues =:"+allelicValues);		
			marker = new HashMap();
			if (null != allelicValues){
				for (AllelicValueElement allelicValueElement : allelicValues){
					if(!(midslist.contains(allelicValueElement.getMarkerName())))
						midslist.add(allelicValueElement.getMarkerName());
					
					data=data+allelicValueElement.getGid()+"~!~"+allelicValueElement.getData()+"~!~"+allelicValueElement.getMarkerName()+"!~!";
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
			
			
			
		} catch (MiddlewareQueryException e) {
			String strErrMsg = "Error retrieving list of AllelicValueElement for the selected GIDs required for Matrix format.";
			throw new GDMSException(strErrMsg);
		}*/		
		
	}


	private Component buildFormatComponent() {
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
		optionGroupForColumn.setImmediate(true);
		optionGroupForColumn.addListener(new Property.ValueChangeListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
                _strSeletedFlapjackType  = String.valueOf(event.getProperty().getValue());
			}
		});
		HorizontalLayout horizLayoutForColumns = new HorizontalLayout();
		horizLayoutForColumns.setSpacing(true);
		horizLayoutForColumns.addComponent(lblColumn);
		horizLayoutForColumns.addComponent(optionGroupForColumn);
		
		final ComboBox selectMap = new ComboBox();
		selectMap.setWidth("200px");
		Object itemId = selectMap.addItem();
		selectMap.setItemCaption(itemId, "Select Map");
		selectMap.setValue(itemId);
		selectMap.setNullSelectionAllowed(false);
		selectMap.setImmediate(true);
		selectMap.setEnabled(false);
		optionGroupForColumn.setEnabled(false);
		
		final ArrayList<String> arrayListOfMapNames = new ArrayList<String>();
		HashMap<String, Integer> hmOfMapNameAndID = new HashMap<String, Integer>();

		if (null != listGermplasmsSelected && 0 != listGermplasmsSelected.size()){
			MapDAO mapDAOLocal = new MapDAO();
			mapDAOLocal.setSession(localSession);
			MapDAO mapDAOCentral = new MapDAO();
			mapDAOCentral.setSession(centralSession);			
			try {

				System.out.println("listOfMarkerIds:"+listOfMarkerIds);
				
				List<MapDetailElement> details = genoManager.getMapAndMarkerCountByMarkers(listOfMarkerIds);
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
		        System.out.println("arrayListOfMapNames:"+arrayListOfMapNames);
				if (null != arrayListOfMapNames){
					for (int i = 0; i < arrayListOfMapNames.size(); i++){
						//Map map = listOfAllMaps.get(i);
						selectMap.addItem(arrayListOfMapNames.get(i));
					}
				}

			} catch (MiddlewareQueryException e) {
				_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Maps",  Notification.TYPE_ERROR_MESSAGE);
				return null;
			}
		}

		/*for (String strMapName : arrayListOfMapNames){
			selectMap.addItem(strMapName);
		}*/

		/**
		 * 
		 * Building the left side components and layout
		 * 
		 */
		VerticalLayout layoutForGenotypingMatrixFormat = new VerticalLayout();
		layoutForGenotypingMatrixFormat.setSpacing(true);
		layoutForGenotypingMatrixFormat.setMargin(true, true, true, true);

		chbMatrix = new CheckBox();
		chbMatrix.setCaption(MATRIX_FORMAT);
		chbMatrix.setHeight("25px");
		chbMatrix.setImmediate(true);
		layoutForGenotypingMatrixFormat.addComponent(chbMatrix);
		chbMatrix.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {

				if (true == (Boolean) chbMatrix.getValue()){
					chbFlapjack.setValue(false);
					chbMatrix.setValue(true);

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
		layoutForGenotypingMatrixFormat.addComponent(cssLayoutMatrix);


		/**
		 * 
		 * Building the right side components and layout
		 * 
		 */
		chbFlapjack = new CheckBox();
		chbFlapjack.setCaption(FLAPJACK_FORMAT);
		chbFlapjack.setHeight("25px");
		chbFlapjack.setImmediate(true);
		chbFlapjack.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				if (true == (Boolean) chbFlapjack.getValue()){
					chbMatrix.setValue(false);
					chbFlapjack.setValue(true);
					optionGroupForColumn.setEnabled(true);
					selectMap.setEnabled(true);
				} else {
					chbFlapjack.setValue(false);
					optionGroupForColumn.setEnabled(false);
					selectMap.setEnabled(false);
				}
			}
		});


		HorizontalLayout topHorizLayoutForFlapjack = new HorizontalLayout();
		topHorizLayoutForFlapjack.setSizeFull();
		topHorizLayoutForFlapjack.setSpacing(true);
		topHorizLayoutForFlapjack.addComponent(chbFlapjack);
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
			boolean dataToBeExportedBuiltSuccessfully = false;
			
			public void buttonClick(ClickEvent event) {

				if (null == listOfMarkersProvided || 0 == listOfMarkersProvided.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Markers to be displayed in the required format.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (null == listGermplasmsSelected || 0 == listGermplasmsSelected.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Germplasms to be displayed in the required format.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (chbMatrix.getValue().toString().equals("true")){
					strSelectedFormat = "Matrix";		
						
						try {
							retrieveDataForSelectedGermplasms();
							dataToBeExportedBuiltSuccessfully = true;
						} catch (GDMSException e1) {
							_mainHomePage.getMainWindow().getWindow().showNotification(e1.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
							return;
						}
						
						if (dataToBeExportedBuiltSuccessfully) {
							//System.out.println("Format Selected: " + strSelectedFormat);
							ExportFileFormats exportFileFormats = new ExportFileFormats();
							System.out.println(">>>>>>>>>>>>>>>glist:"+glist);
							try{
								matrixFile = exportFileFormats.Matrix(_mainHomePage, glist, listOfMarkersProvided, hmOfGIDsAndGermplamsSelected, mapEx, hmMarkerNamesIDs,hmOfGIDs);
							} catch (GDMSException e) {
								_mainHomePage.getMainWindow().getWindow().showNotification("Error generating the Matrix File", Notification.TYPE_ERROR_MESSAGE);
								return;
							}
							//matrixFile = exportFileFormats.MatrixDataSNP(_mainHomePage, listOfGIDsSelected, listOfMarkersProvided, listOfGermplasmMarkerElements, mapEx, hmOfGIDsAndGermplamsSelected);
							//System.out.println("Received the generated Matrix file.");
						}
						
					
					
				} else if (chbFlapjack.getValue().toString().equals("true")){
					strSelectedFormat = "Flapjack";
					//System.out.println("Format Selected: " + strSelectedFormat);
					
					Object mapValue = selectMap.getValue();
					if (mapValue instanceof Integer){
						Integer itemId = (Integer)mapValue;
						if (itemId.equals(1)){
							strSelectedMap = "";
						} 
					} else {
						String strMapSelected = mapValue.toString();
						if (null != arrayListOfMapNames){
							if (arrayListOfMapNames.contains(strMapSelected)){
								strSelectedMap = strMapSelected;
							}
						}	
					}
					
					Object value = optionGroupForColumn.getValue();
					if (null != value){
						strSelectedColumn = value.toString();
					} else {
						strSelectedColumn = "";
					}
					//System.out.println("Selected Map: " + strSelectedMap + " --- " + "Selected Column: " + strSelectedColumn);
					
					
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
										dataToBeExportedBuiltSuccessfully = false;
										_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required column GID or Germplasm for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
										return;
									}
									
									try {
										generateFlagjackFiles = generateFlagjackFiles(strSelectedMap, _strSeletedFlapjackType);
									} catch (GDMSException e) {
										_mainHomePage.getMainWindow().getWindow().showNotification(e.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
										return;
									}
									if (null != generateFlagjackFiles){
										dataToBeExportedBuiltSuccessfully = true;
									}
									
									if (dataToBeExportedBuiltSuccessfully){
										Component newResultComponent = buildResultComponent();
										_tabsheetForMarkers.replaceComponent(buildMarkerResultComponent, newResultComponent);
										buildMarkerResultComponent.requestRepaint();
										buildMarkerResultComponent = newResultComponent;
										_tabsheetForMarkers.getTab(3).setEnabled(true);
										_tabsheetForMarkers.setSelectedTab(buildMarkerResultComponent);
									}
									
								}
							}
							
						});
						dataToBeExportedBuiltSuccessfully = false;
						/*_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required map for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;*/
					} else if (strSelectedColumn.equals("")){
						dataToBeExportedBuiltSuccessfully = false;
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required column GID or Germplasm for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;
					} else {
						if (false == bGenerateFlapjack) {
							bGenerateFlapjack = true;
						}
					}
					if (bGenerateFlapjack) {
						try {
							generateFlagjackFiles = generateFlagjackFiles(strSelectedMap, _strSeletedFlapjackType);
						} catch (GDMSException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getExceptionMessage(),  Notification.TYPE_ERROR_MESSAGE);
							return;
						}
						if (null != generateFlagjackFiles){
							dataToBeExportedBuiltSuccessfully = true;
						}
					}
				}

				if (null == strSelectedFormat || strSelectedFormat.equals("")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required format.",  Notification.TYPE_ERROR_MESSAGE);
					return;
				}  

				if (dataToBeExportedBuiltSuccessfully){
					Component newResultComponent = buildResultComponent();
					_tabsheetForMarkers.replaceComponent(buildMarkerResultComponent, newResultComponent);
					buildMarkerResultComponent.requestRepaint();
					buildMarkerResultComponent = newResultComponent;
					_tabsheetForMarkers.getTab(3).setEnabled(true);
					_tabsheetForMarkers.setSelectedTab(buildMarkerResultComponent);
				}
			}

		});

		HorizontalLayout horizontalLayoutForTwoFormats = new HorizontalLayout();
		horizontalLayoutForTwoFormats.setSpacing(true);
		horizontalLayoutForTwoFormats.setSizeFull();
		horizontalLayoutForTwoFormats.addComponent(layoutForGenotypingMatrixFormat);
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
		optiongroup.addItem(chbMatrix);
		optiongroup.addItem(chbFlapjack);
		optiongroup.setMultiSelect(false);

		
		if (null != listGermplasmsSelected && 0 != listGermplasmsSelected.size()){
			btnNext.setEnabled(true);
			chbMatrix.setEnabled(true);
			chbFlapjack.setEnabled(true);
			optiongroup.setEnabled(true);
		} else {
			btnNext.setEnabled(false);
			chbMatrix.setEnabled(false);
			chbFlapjack.setEnabled(false);
			optiongroup.setEnabled(false);
		}
		
		
		return completeFormatLayout;
	}

	private List<File> generateFlagjackFiles(String theMap, String theSeletedFlapjackType2) throws GDMSException {
		generateFlagjackFiles = null;
		if(null == listGermplasmsSelected) {
			return null;
		}
		
		boolean bGIDs = false;
		boolean bGerm = false;
		if(null != theSeletedFlapjackType2) {
			if(theSeletedFlapjackType2.startsWith("GID")) {
				bGIDs = true;
			} else {
				bGerm = true;
			}
		}
		//strSelectedMap = strSelectedMap.substring(0,strSelectedMap.lastIndexOf("("));
		if(! theMap.isEmpty())
			strMap = theMap.substring(0,theMap.lastIndexOf("("));
		//List<String> listOfSelectedItems = new ArrayList<String>();
		List<Integer> listOfGIDs = new ArrayList<Integer>();
		for (int i = 0; i < listGermplasmsSelected.size(); i++) {
			String strMarkerName = listGermplasmsSelected.get(i);
			Integer integerGID = hmOfGNamesAndGids.get(strMarkerName);
			listOfGIDs.add(integerGID);
		}
		System.out.println("listOfGIDs:"+listOfGIDs);
		System.out.println("listOfMarkersProvided:"+listOfMarkersProvided);
		String strGIDS="";
		String strMidS="";
		for(int g=0;g<listOfGIDs.size();g++){
			strGIDS=strGIDS+listOfGIDs.get(g)+",";
		}		
		strGIDS=strGIDS.substring(0, strGIDS.length()-1);
		
		listOfMarkerIds = new ArrayList<Integer>();
		ArrayList listOfGNames = new ArrayList<String>();
		ArrayList gDupNameList=new ArrayList<Integer>();
		//listGermplasmsSelected
		try {
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			List<Marker> listCentral=genoManager.getMarkersByMarkerNames(listOfMarkersProvided, 0, listOfMarkersProvided.size(), Database.CENTRAL);
			for(Marker resC:listCentral){
				listOfMarkerIds.add(resC.getMarkerId());
			}
			List<Marker> listLocal=genoManager.getMarkersByMarkerNames(listOfMarkersProvided, 0, listOfMarkersProvided.size(), Database.LOCAL);
			for(Marker resL:listLocal){
				listOfMarkerIds.add(resL.getMarkerId());
			}
			
			for(int m=0;m<listOfMarkerIds.size();m++){
				strMidS=strMidS+listOfMarkerIds.get(m)+",";
			}		
			strMidS=strMidS.substring(0, strMidS.length()-1);
			
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
			strQuerry="select distinct gid,marker_id, char_value,acc_sample_id,marker_sample_id from gdms_char_values where gid in("+strGIDS+") and marker_id in ("+strMidS+") ORDER BY gid, marker_id,acc_sample_id asc";	
			System.out.println(strQuerry);
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
				System.out.println(strMareO);
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
			System.out.println("markerAlleles:"+markerAlleles);
			System.out.println("glist:"+glist);
			
			strQuerry="select distinct gid,marker_id, allele_bin_value,acc_sample_id,marker_sample_id from gdms_allele_values where gid in("+strGIDS+") and marker_id in ("+strMidS+") ORDER BY gid, marker_id,acc_sample_id asc";	
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
			
			strQuerry="select distinct gid,marker_id, map_char_value,acc_sample_id,marker_sample_id from gdms_mapping_pop_values where gid in("+strGIDS+") and marker_id in ("+strMidS+") ORDER BY gid, marker_id,acc_sample_id asc";	
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
			HashMap<Object, Integer> hmGidSampleIdNid=new HashMap<Object, Integer>();
			HashMap<Integer, String> hmNidGermplasmName=new HashMap<Integer, String>();
			try{
				localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
				centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			}catch (Exception e){
				e.printStackTrace();
			}
			List listOfNIDs = new ArrayList<Integer>();
			allelesFromLocal=new ArrayList();	
			allelesFromCentral=new ArrayList();	
			parentsData=new ArrayList();		
			allelesList=new ArrayList();
			//List gidsList=new ArrayList();
			 objAL=null;
			objAC=null;
			 itListAC=null;
			 itListAL=null;	
			 gDupNameList=new ArrayList<Integer>();
			strQuerry="select distinct gid,nid, acc_sample_id from gdms_acc_metadataset where gid in ("+ strGIDS +") order by gid, nid,acc_sample_id asc";	
			System.out.println("strQuerry:"+strQuerry);
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
				System.out.println("listGermplasmsSelected:"+listGermplasmsSelected);		
			
			
			int rep=1;
			Name names1 = null;
			String germplasmName="";
			Object gid="";
			String gids="";
			//ArrayList<String> gNameList=new ArrayList<String>();
			for(int n=0;n<listOfNIDs.size();n++){			
				names1=genoManager.getNameByNameId(Integer.parseInt(listOfNIDs.get(n).toString()));
				if(names1 != null)
				hmNidGermplasmName.put(names1.getNid(), names1.getNval());				
			}
			
			hmOfGIDsAndGermplamsSelected = new HashMap<Object, String>();
			hmOfGIDs = new HashMap<Object, String>();
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
			
				hmOfGIDsAndGermplamsSelected.put(gidSampleid, gname);
				hmOfGIDs.put(gidSampleid, gids);
			}
			System.out.println("hmOfGIDs:"+hmOfGIDs);
			
			System.out.println("hmOfGIDsAndGermplamsSelected:"+hmOfGIDsAndGermplamsSelected);
			
			/*
			List<Integer> listOfNewGids = new ArrayList<Integer>();
			List<String> listOfNVals = new ArrayList<String>();
			List<String> list = new ArrayList<String>();
			Hashtable<String, Integer> gListExp1 = new Hashtable<String, Integer>();
			List<Integer> gListExp = new ArrayList<Integer>();
			SortedMap<Integer, String> mapN = new TreeMap<Integer, String>();
			List<Integer> nidfromAccMetadataset = getNidfromAccMetadataset(listOfGIDs);
			//GET_ALLELE_COUNT_BY_GID
			long iCountOfAlleleCountByGID = getAlleleCountByGID(listOfGIDs);
			//GET_CHAR_COUNT_BY_GID
			long lGetCharCountByGID = getCharCountByGID(listOfGIDs);
			//GET_MAPPING_COUNT_BY_GID
			long lGetMappingCountByGID = getMappingCountByGID(listOfGIDs);
			List<AllelicValueElement> finalMappingValues = new ArrayList<AllelicValueElement>();
			List<AllelicValueElement> finalMappingValues2 = new ArrayList<AllelicValueElement>();
			
			List<AllelicValueElement> alleleValuesByMarkerNameElement = new ArrayList<AllelicValueElement>();
			if(0 < iCountOfAlleleCountByGID) {
				alleleValuesByMarkerNameElement = getMarkerData(listOfGIDs, listOfMarkersProvided);
				finalMappingValues.addAll(alleleValuesByMarkerNameElement);
			}
			
			List<AllelicValueElement> charValuesByMarkerNameElement = new ArrayList<AllelicValueElement>();
			if(0 < lGetCharCountByGID) {
				charValuesByMarkerNameElement = getMarkerData(listOfGIDs, listOfMarkersProvided);
				finalMappingValues.addAll(charValuesByMarkerNameElement); 
			}
			
			List<AllelicValueElement> mappingValuesByMarkerNameElement = new ArrayList<AllelicValueElement>();
			if(0 < lGetMappingCountByGID) {
				mappingValuesByMarkerNameElement = getMarkerData(listOfGIDs, listOfMarkersProvided);
				finalMappingValues.addAll(mappingValuesByMarkerNameElement);
				
				List<Integer> idsByNames = getMarkerIds();
				
				List<MappingValueElement> mappingPopValuesByGidsAndMarkerIds = getMappingPopValuesByGidsAndMarkerIds(listOfGIDs, idsByNames);
				
				System.out.println("mappingPopValuesByGidsAndMarkerIds:"+mappingPopValuesByGidsAndMarkerIds);
				
				boolean isExisting = false;
				
				Integer parentAGid = null; 
				Integer parentBGid = null;
				String strMappingType = "";
				String strMarkerType = "";
				List<Integer> listOfParents = new ArrayList<Integer>();
				for (MappingValueElement mappingValueElement : mappingPopValuesByGidsAndMarkerIds) {
					if(false == isExisting) {
						for (Integer integer : listOfGIDs) {
							if(mappingValueElement.getParentAGid().equals(integer) && mappingValueElement.getParentBGid().equals(integer)) {
								isExisting = true;
								parentAGid = mappingValueElement.getParentAGid();
								parentBGid = mappingValueElement.getParentBGid();
								break;
							}
						}
					}
					if(false == listOfParents.contains(mappingValueElement.getParentAGid())) {
						listOfParents.add(mappingValueElement.getParentAGid());
					}
					if(false == listOfParents.contains(mappingValueElement.getParentBGid())) {
						listOfParents.add(mappingValueElement.getParentBGid());
					}
					strMappingType = mappingValueElement.getMappingType();
					strMarkerType = mappingValueElement.getMarkerType();
				}
				
				if(isExisting) {
					if(true == (null != parentAGid && null != parentBGid)) {
						if(false == listOfGIDs.contains(parentAGid)) {
							listOfGIDs.add(parentAGid);
						}
						if(false == listOfGIDs.contains(parentBGid)) {
							listOfGIDs.add(parentBGid);
						}
					}
					
					List<Integer> nameIdsByGermplasmIds = getAccMetaDataSet(listOfParents);
					
						List<Name> namesByNameIds = getName(nameIdsByGermplasmIds);
						for (Name name : namesByNameIds) {
							
							if(false == listOfGIDs.contains(name.getGermplasmId())) {
								listOfGIDs.add(name.getGermplasmId());
							}
						}
						
				}
				
				List<AllelicValueElement> markerData = new ArrayList<AllelicValueElement>();
				System.out.println("strMappingType:"+strMappingType);
				System.out.println("strMarkerType=:"+strMarkerType);
				if(strMappingType.equalsIgnoreCase("allelic")) {
					if(strMarkerType.equalsIgnoreCase("snp")){
						
						markerData = getMarkerData(listOfParents, listOfMarkersProvided);
						
					}else if((strMarkerType.equalsIgnoreCase("ssr"))||(strMarkerType.equalsIgnoreCase("DArT"))){
						
						if(0 < iCountOfAlleleCountByGID) {
							markerData = getMarkerData(listOfParents, listOfMarkersProvided);
						}
					}
					for (AllelicValueElement allelicValueElement : markerData) {
						finalMappingValues2.add(allelicValueElement);
					}
					
				}
				
				List<Name> listOfNames = getNames(nidfromAccMetadataset);
				for (Name name : listOfNames) {
					Integer germplasmId = name.getGermplasmId();
					if(false == listOfNewGids.contains(germplasmId)) {
						listOfNewGids.add(germplasmId);
					}
					
					if(bGerm ) {
						if(false == listOfNVals.contains(name.getNval())) {
							listOfNVals.add(name.getNval());
						}
						gListExp1.put(name.getNval(), name.getGermplasmId());
					} else {
						if(!(gListExp.contains(name.getGermplasmId())))
							gListExp.add(name.getGermplasmId());
					}
					mapN.put(name.getGermplasmId(), name.getNval());	
				}
				
				//System.out.println();
			}
			
			for (AllelicValueElement allelicValueElementallelic : finalMappingValues2) {
				for (AllelicValueElement allelicValueElementOld : finalMappingValues) {
					if(allelicValueElementallelic.getGid().equals(allelicValueElementOld.getGid())
							&& allelicValueElementallelic.getMarkerName().equals(allelicValueElementOld.getMarkerName())) {
						finalMappingValues.remove(allelicValueElementOld);
						finalMappingValues.add(allelicValueElementallelic);
					}
				}
			}

			
			for (AllelicValueElement allelicValueElement : finalMappingValues) {
				if(listOfGIDs.contains(allelicValueElement.getGid())) {
					if (bGerm) {
						list.add(mapN.get(allelicValueElement.getGid())+","+allelicValueElement.getMarkerName()+","+allelicValueElement.getData());	
						
					} else {
						//list.add(allelicValueElement.getGid()+","+allelicValueElement.getMarkerName()+","+allelicValueElement.getData());
					}
				}
			}
			*/
			
			if(! strSelectedMap.isEmpty())
				strSelectedMap = strSelectedMap.substring(0,strSelectedMap.lastIndexOf("("));
			
			try{
				//String mapName  = strMapSelected.substring(0,strMapSelected.lastIndexOf("("));
				iSelectedMapId=genoManager.getMapIdByName(strSelectedMap);
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
				_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Map Id for the selected Map", Notification.TYPE_ERROR_MESSAGE);							
			}
			
			List<Qtl> qtlDetails = null;
			String mapData = "";
			Integer iMapId = 0;
			List<String> markersInMap = new ArrayList<String>();
			List<MappingData> mappingData = getMappingData(strMap);
			System.out.println("mappingData:"+mappingData);
			for (MappingData mapInfo : mappingData) {
				iMapId = mapInfo.getMapId();
				mapData=mapData+mapInfo.getMarkerName()+"!~!"+mapInfo.getLinkageGroup()+"!~!"+mapInfo.getStartPosition()+"~~!!~~";
				if(!markersInMap.contains(mapInfo.getMarkerName()))
					markersInMap.add(mapInfo.getMarkerName());
			}
			
			
			for (String mapInfo : listOfMarkersProvided) {
				if(false == markersInMap.contains(mapInfo)) {
					mapData=mapData+mapInfo+"!~!"+"unmapped"+"!~!"+"0"+"~~!!~~";
				}
			}
			System.out.println("mapData:"+mapData);
			//get QTl ids list details using list of gids
			ArrayList<Integer> listOfMapIds = new ArrayList();
			if(!strMap.isEmpty()){
			int mapId=genoManager.getMapIdByName(strMap);
			listOfMapIds.add(mapId);
			List<QtlDetails> qtlIdsListByListOfMapIds=null ;
			for(int m=0;m<listOfMapIds.size();m++){
				qtlIdsListByListOfMapIds = genoManager.getQtlDetailsByMapId(listOfMapIds.get(m));
				//List<QtlDetails> qtlIdsListByListOfMapIds = getQTLIdsListByListOfMapIds(listOfMapIds);
				boolean bQTLPresent = false;
				if(null != qtlIdsListByListOfMapIds && 0 != qtlIdsListByListOfMapIds.size()) {
					bQTLPresent = true;
				}
			}
			qtlDetails = getQTLDAO(qtlIdsListByListOfMapIds);
			}
			//List<File> createDatFile = createDatFile(listOfMarkersProvided, listOfGIDs, bGIDs, qtlIdsListByListOfMapIds, qtlDetails, mapData, finalMappingValues, list, bQTLPresent);
			
			MarkerDAO markerDAOForLocal = new MarkerDAO();
			markerDAOForLocal.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession());
			MarkerDAO markerDAOForCentral = new MarkerDAO();
			markerDAOForCentral.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession());

			long countAllLocal = markerDAOForLocal.countAll();
			List<Integer> listOfMIDsByMNamesLocal = markerDAOForLocal.getIdsByNames(listOfMarkersProvided, 0, (int)countAllLocal);

			long countAllCentral = markerDAOForCentral.countAll();
			List<Integer> listOfMIDsByMNamesCentral = markerDAOForCentral.getIdsByNames(listOfMarkersProvided, 0, (int)countAllCentral);
			
			hmOfSelectedMIDandMNames = new HashMap<Integer, String>();
			long countMarkersByIds = markerDAOForLocal.countMarkersByIds(listOfMarkerIds);
			List<Marker> listOfMarkersByIdsLocal = markerDAOForLocal.getMarkersByIds(listOfMarkerIds, 0, (int)countMarkersByIds);
			long countMarkersByIds2 = markerDAOForCentral.countMarkersByIds(listOfMarkerIds);
			List<Marker> listOfMarkersByCentral = markerDAOForCentral.getMarkersByIds(listOfMarkerIds, 0, (int)countMarkersByIds2);

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

			
			RetrieveDataForFlapjack retrieveDataForFlapjack = new RetrieveDataForFlapjack(_mainHomePage);
			retrieveDataForFlapjack.setGenotypingType("Markers");
			retrieveDataForFlapjack.setListOfGermplasmsProvided(listOfGermplasmsByMarkers);
			retrieveDataForFlapjack.setListOfMarkersSelected(listOfMarkersProvided);
			retrieveDataForFlapjack.setListOfGIDsSelected((ArrayList<Integer>) listOfGIDs);
			retrieveDataForFlapjack.setListOfMIDsSelected(listOfMarkerIds);
			retrieveDataForFlapjack.setHashmapOfSelectedMIDsAndMNames(hmOfSelectedMIDandMNames);
			retrieveDataForFlapjack.setHashmapOfSelectedGIDsAndGNames(hmOfGIDsAndGermplamsSelected);
			retrieveDataForFlapjack.setMapSelected(strSelectedMap, iSelectedMapId);
			retrieveDataForFlapjack.setExportType(strSelectedColumn);
			retrieveDataForFlapjack.retrieveFlapjackData();
			
			boolean dataToBeExportedBuiltSuccessfully = retrieveDataForFlapjack.isFlapjackDataBuiltSuccessfully();

			List<File> createDatFile = new ArrayList<File>();
			if (dataToBeExportedBuiltSuccessfully){
				generatedTextFile = retrieveDataForFlapjack.getGeneratedTextFile();
				generatedMapFile = retrieveDataForFlapjack.getGeneratedMapFile();
				generatedDatFile = retrieveDataForFlapjack.getGeneratedDatFile();
				
				folderPath=retrieveDataForFlapjack.getStrBMSFilePath();
				strQTLExists=retrieveDataForFlapjack.getQTLExists();
				/*createDatFile.add(generatedTextFile);
				createDatFile.add(generatedMapFile);
				createDatFile.add(generatedDatFile);*/
				
			}
			return createDatFile;
		} catch (MiddlewareQueryException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error while generating flapjack files.",  Notification.TYPE_ERROR_MESSAGE);
			return  null;
		}
		//return new ArrayList<File>();
	}


	private List<Name> getName(List<Integer> nameIdsByGermplasmIds)
			throws MiddlewareQueryException {
		List<Name> listToReturn = new ArrayList<Name>();
		List<Name> localName = getLocalName(nameIdsByGermplasmIds);
		if(null != localName) {
			listToReturn.addAll(localName);
		}
		List<Name> centralName = getCentralName(nameIdsByGermplasmIds);
		if(null != centralName) {
			listToReturn.addAll(centralName);
		}
		return listToReturn;
	}


	private List<Name> getCentralName(List<Integer> nameIdsByGermplasmIds) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(centralSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nameIdsByGermplasmIds);
		return namesByNameIds;
	}


	private List<Name> getLocalName(List<Integer> nameIdsByGermplasmIds)
			throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		List<Name> namesByNameIds = nameDAO.getNamesByNameIds(nameIdsByGermplasmIds);
		return namesByNameIds;
	}


	private List<Integer> getAccMetaDataSet(List<Integer> listOfParents)
			throws MiddlewareQueryException {
		List<Integer> listToReturn = new ArrayList<Integer>();
		List<Integer> localAccMetaDataSet = getLocalAccMetaDataSet(listOfParents);
		if(null != localAccMetaDataSet) {
			listToReturn.addAll(localAccMetaDataSet);
		}
		List<Integer> centralAccMetaDataSet = getCentralAccMetaDataSet(listOfParents);
		if(null != centralAccMetaDataSet) {
			for (Integer integer : centralAccMetaDataSet) {
				if(false == listToReturn.contains(integer)) {
					listToReturn.add(integer);
				}
			}
		}
		return listToReturn;
	}


	private List<Integer> getLocalAccMetaDataSet(List<Integer> listOfParents)
			throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(localSession);
		List<Integer> nameIdsByGermplasmIds = accMetadataSetDAO.getNameIdsByGermplasmIds(listOfParents);
		return nameIdsByGermplasmIds;
	}
	private List<Integer> getCentralAccMetaDataSet(List<Integer> listOfParents)
			throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(centralSession);
		List<Integer> nameIdsByGermplasmIds = accMetadataSetDAO.getNameIdsByGermplasmIds(listOfParents);
		return nameIdsByGermplasmIds;
	}


	/*protected List<File> createDatFile(ArrayList<String> listOfMarkersProvided2, List<Integer> listOfGIDs2, 
			boolean isGIDSelected, List<QtlDetails> qtlIdsListByListOfMapIds, List<Qtl> qtlDetails, String mapData, List<AllelicValueElement> finalMappingValues, List<String> list, boolean bQTLPresent) {
		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		//System.out.println(absoluteFile);


		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		List<File> listOfFiles = new ArrayList<File>();
		String strAbsolutePath = fileExport.getAbsolutePath();
		//System.out.println(">>>>>" + strAbsolutePath);
		
		String strFJDatFileLink = strAbsolutePath + "\\Flapjack.dat";
		Link fjDatFileLink = new Link("View Flapjack.dat file", new ExternalResource(strFJDatFileLink));
		fjDatFileLink.setTargetName("_blank");
		File fileDatFile = new File(strFJDatFileLink);
		listOfFiles.add(fileDatFile);
		FileWriter flapjackdat;
		try {
			flapjackdat = new FileWriter(fileDatFile);
			
			BufferedWriter fjackdat = new BufferedWriter(flapjackdat);
			for (String string : listOfMarkersProvided2) {
				fjackdat.write("\t" + string);
			}
			
			if(false == isGIDSelected) {
//				fjackdat.write("\n");
				for (String strValue : list) {
					String[] strValues = strValue.split(",");
					fjackdat.write("\n" + strValues[0]);
					fjackdat.write("\t" + strValues[2]);
				}
			} else {
				for (Integer integer : listOfGIDs2) {
					fjackdat.write("\n" + integer);

					for (String string : listOfMarkersProvided2) {
						boolean bWrote = false;
						for (AllelicValueElement allelicValueElement : finalMappingValues) {
							if(false == allelicValueElement.getGid().equals(integer) && false == allelicValueElement.getMarkerName().equals(string)) {
								continue;
							}
							bWrote = true;
							String finalData = allelicValueElement.getData();
							fjackdat.write("\t"+finalData);
						}
						if(false == bWrote) {
							fjackdat.write("\t");
						}
					}
				}
			}
			fjackdat.close();
			
			//write map file.
			String strFlapjackMap = fileExport+("//")+"/Flapjack.map";
			File fileFlapjackMap = new File(strFlapjackMap);
			listOfFiles.add(fileFlapjackMap);
			FileWriter flapjackmapstream = new FileWriter(fileFlapjackMap);
			BufferedWriter fjackmap = new BufferedWriter(flapjackmapstream);
			String[] mData=mapData.split("~~!!~~");
			
			for(int m=0;m<mData.length;m++){		
				String[] strMData=mData[m].split("!~!");
				fjackmap.write(strMData[0]);
				fjackmap.write("\t");
				fjackmap.write(strMData[1]);
				fjackmap.write("\t");
				fjackmap.write(strMData[2]);
				fjackmap.write("\n");		
			}
			fjackmap.close();
			
			
			//write qtl
			String strFlapjackTxt = fileExport+("//")+"/Flapjack.txt";
			
			System.out.println("```````````````````````````````````````strFlapjackTxt:"+strFlapjackTxt);
			
			File fileFlapjackTxt = new File(strFlapjackTxt);
			listOfFiles.add(fileFlapjackTxt);
			FileWriter flapjackQTLstream = new FileWriter(fileFlapjackTxt);
			BufferedWriter fjackQTL = new BufferedWriter(flapjackQTLstream);
			if(bQTLPresent){
				//String[] qtlData=qtlData.split("~~!!~~");
				fjackQTL.write("QTL\tChromosome\tPosition\tMinimum\tMaximum\tTrait\tExperiment\tTrait Group\tLOD\tR2\tfavallele\tFlanking markers in original publication\teffect");
				fjackQTL.write("\n");
				for (QtlDetails qtlDetails2 : qtlIdsListByListOfMapIds) {
					String strQTL = "";
					for (Qtl qtl : qtlDetails) {
						if(true == qtl.getQtlId().equals(qtlDetails2.getId().getQtlId())) {
							strQTL = qtl.getQtlName();
							break;
						}
					}
					fjackQTL.write(strQTL);
					fjackQTL.write("\t");

					//Chromosome
					String strChromosome = qtlDetails2.getLinkageGroup();
					fjackQTL.write(strChromosome);
					fjackQTL.write("\t");

					//Position
					String strPosition = String.valueOf(qtlDetails2.getPosition());
					fjackQTL.write(strPosition);
					fjackQTL.write("\t");

					//Minimum
					String strMinimum = String.valueOf(qtlDetails2.getMinPosition());
					fjackQTL.write(strMinimum);
					fjackQTL.write("\t");

					//Maximum
					String strMaximum = String.valueOf(qtlDetails2.getMaxPosition());
					fjackQTL.write(strMaximum);
					fjackQTL.write("\t");

					//Trait
					//String strTrait = qtlDetails2.getTrait();
					String strTrait = "";
					Integer iTraitId = qtlDetails2.getTraitId();
					if (null != iTraitId){
						/*TraitDAO traitDAOLocal = new TraitDAO();
						traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
						/*String traitFromLocal="";
						try {
							
							traitFromLocal=om.getStandardVariable(iTraitId).getName();
						} catch (MiddlewareQueryException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Trait objects", Notification.TYPE_ERROR_MESSAGE);
							return null;
						}
					}

					fjackQTL.write(strTrait);
					fjackQTL.write("\t");

					//Experiment
					String strExperiment = qtlDetails2.getExperiment();
					fjackQTL.write(strExperiment);
					fjackQTL.write("\t");

					//Trait Group
					String strTraitGroup = qtlDetails2.getExperiment();
					fjackQTL.write(strTraitGroup);
					fjackQTL.write("\t");

					//LOD
					String strLOD = String.valueOf(qtlDetails2.getScoreValue());
					fjackQTL.write(strLOD);
					fjackQTL.write("\t");

					//R2
					String strR2 = String.valueOf(qtlDetails2.getrSquare());
					fjackQTL.write(strR2);
					fjackQTL.write("\t");

					//favallele
					String strfavallele = qtlDetails2.getExperiment();
					fjackQTL.write(strfavallele);
					fjackQTL.write("\t");

					//Flanking markers in original publication
					String strFlankingmarkersinoriginalpublication = "";
					if(qtlDetails2.getLeftFlankingMarker().equals(qtlDetails2.getRightFlankingMarker())) {
						strFlankingmarkersinoriginalpublication = qtlDetails2.getLeftFlankingMarker();
					} else {
						strFlankingmarkersinoriginalpublication = qtlDetails2.getLeftFlankingMarker() + "/" + qtlDetails2.getRightFlankingMarker();
					}
					fjackQTL.write(strFlankingmarkersinoriginalpublication);
					fjackQTL.write("\t");

					//effect
					String streffect = String.valueOf(qtlDetails2.getEffect());
					fjackQTL.write(streffect);
					fjackQTL.write("\n");

				}
				fjackQTL.close();
			}
			
		} catch (IOException e) {
			return null;
		}
		
		return listOfFiles;
	}*/

	private List<MappingData> getMappingData(String strMap) throws MiddlewareQueryException {
		List<MappingData> listToReturn = new ArrayList<MappingData>();
		List<MappingData> localMappingData = getLocalMappingData(strMap);
		if(null != localMappingData) {
			listToReturn.addAll(localMappingData);
		}
		List<MappingData> centralMappingData = getCentralMappingData(strMap);
		if(null != centralMappingData) {
			listToReturn.addAll(centralMappingData);
		}
		return listToReturn;
	}


	private List<MappingData> getCentralMappingData(String strMap) throws MiddlewareQueryException {
		//return getMapInfoByMapName(strMap, centralSession);
		List<MappingData> listToReturn = new ArrayList<MappingData>();
		MappingDataDAO mappingDataDAO = new MappingDataDAO();
		mappingDataDAO.setSession(centralSession);
		List<MappingData> list = mappingDataDAO.getAll();
		for (MappingData mappingData : list) {
			if(mappingData.getMapName().equals(strMap)) {
				listToReturn.add(mappingData);
			}
		}
		return listToReturn;
	}


	private List<MappingData> getLocalMappingData(String strMap) throws MiddlewareQueryException {
		//return getMapInfoByMapName(strMap, localSession);
		List<MappingData> listToReturn = new ArrayList<MappingData>();
		MappingDataDAO mappingDataDAO = new MappingDataDAO();
		mappingDataDAO.setSession(localSession);
		List<MappingData> list = mappingDataDAO.getAll();
		for (MappingData mappingData : list) {
			if(mappingData.getMapName().equals(strMap)) {
				listToReturn.add(mappingData);
			}
		}
		return listToReturn;
	}

	private List<Name> getNames(List<Integer> nidfromAccMetadataset) throws MiddlewareQueryException {
		List<Name> listToReturn = new ArrayList<Name>();
		List<Name> localNames = getLocalNames(nidfromAccMetadataset);
		if(null != localNames) {
			listToReturn.addAll(localNames);
		}
		List<Name> centralNames = getCentralNames(nidfromAccMetadataset);
		if(null != centralNames) {
			listToReturn.addAll(centralNames);
		}
		return listToReturn;
	}


	private List<Name> getLocalNames(List<Integer> nidfromAccMetadataset) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(localSession);
		return nameDAO.getNamesByNameIds(nidfromAccMetadataset);
	}


	private List<Name> getCentralNames(List<Integer> nidfromAccMetadataset) throws MiddlewareQueryException {
		NameDAO nameDAO = new NameDAO();
		nameDAO.setSession(centralSession);
		return nameDAO.getNamesByNameIds(nidfromAccMetadataset);
	}


	private List<MappingValueElement> getMappingPopValuesByGidsAndMarkerIds(List<Integer> listOfGids,
			List<Integer> idsByNames) throws MiddlewareQueryException {
		
		List<MappingValueElement> listToReturn = new ArrayList<MappingValueElement>();
		
		List<MappingValueElement> localMappingPopValuesByGidsAndMarkerIds = getLocalMappingPopValuesByGidsAndMarkerIds(listOfGids, idsByNames);
		if(null != localMappingPopValuesByGidsAndMarkerIds) {
			listToReturn.addAll(localMappingPopValuesByGidsAndMarkerIds);
		}
		List<MappingValueElement> centralMappingPopValuesByGidsAndMarkerIds = getCentralMappingPopValuesByGidsAndMarkerIds(listOfGids, idsByNames);
		if(null != centralMappingPopValuesByGidsAndMarkerIds) {
			listToReturn.addAll(centralMappingPopValuesByGidsAndMarkerIds);
		}
		return listToReturn;
	}


	private List<MappingValueElement> getCentralMappingPopValuesByGidsAndMarkerIds(
			List<Integer> listOfGids2, List<Integer> idsByNames) throws MiddlewareQueryException {
		MappingPopDAO mappingPopDAO = new MappingPopDAO();
		mappingPopDAO.setSession(localSession);
		return mappingPopDAO.getMappingValuesByGidAndMarkerIds(listOfGids2, idsByNames);
	}


	private List<MappingValueElement> getLocalMappingPopValuesByGidsAndMarkerIds(
			List<Integer> listOfGids, List<Integer> idsByNames)
			throws MiddlewareQueryException {
		MappingPopDAO mappingPopDAO = new MappingPopDAO();
		mappingPopDAO.setSession(localSession);
		return mappingPopDAO.getMappingValuesByGidAndMarkerIds(listOfGids, idsByNames);
	}


	private List<Integer> getMarkerIds() throws MiddlewareQueryException {
		List<Integer> listToReturn = new ArrayList<Integer>();
		List<Integer> localMarkerNames = getLocalMarkerIds();
		if(null != localMarkerNames) {
			listToReturn.addAll(localMarkerNames);
		}
		List<Integer> centralMarkerName = getCentralIdsName();
		if(null != centralMarkerName) {
			listToReturn.addAll(centralMarkerName);
		}
		return listToReturn;
	}
	


	private List<Integer> getCentralIdsName() throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(centralSession);
		List<Integer> idsByNames = markerDAO.getIdsByNames(listOfMarkersProvided, 0, (int)markerDAO.countAll());
		return idsByNames;
	}


	private List<Integer> getLocalMarkerIds() throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(localSession);
		List<Integer> idsByNames = markerDAO.getIdsByNames(listOfMarkersProvided, 0, (int)markerDAO.countAll());
		return idsByNames;
	}
	

	private List<AllelicValueElement> getMarkerData(List<Integer> listOfGIDs2,
			List<String> listOfMarkerName) throws MiddlewareQueryException {
		List<AllelicValueElement> listToReturn = new ArrayList<AllelicValueElement>();
		
		List<AllelicValueElement> localMarkerData = getLocalMarkerData(listOfGIDs2, listOfMarkerName);
		if(null != localMarkerData) {
			listToReturn.addAll(localMarkerData);
		}
		List<AllelicValueElement> centralMarkerData = getCentralMarkerData(listOfGIDs2, listOfMarkerName);
		if(null != centralMarkerData) {
			listToReturn.addAll(centralMarkerData);
		}
		return listToReturn;
	}


	private List<AllelicValueElement> getCentralMarkerData(List<Integer> listOfGIDs2,
			List<String> listOfMarkerName) throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(centralSession);
		List<AllelicValueElement> allelicValuesByGidsAndMarkerNames = markerDAO.getAllelicValuesByGidsAndMarkerNames(listOfGIDs2, listOfMarkerName);
		return allelicValuesByGidsAndMarkerNames;
	}


	private List<AllelicValueElement> getLocalMarkerData(
			List<Integer> listOfGIDs2, List<String> listOfMarkerName)
			throws MiddlewareQueryException {
		MarkerDAO markerDAO = new MarkerDAO();
		markerDAO.setSession(localSession);
		List<AllelicValueElement> allelicValuesByGidsAndMarkerNames = markerDAO.getAllelicValuesByGidsAndMarkerNames(listOfGIDs2, listOfMarkerName);
		return allelicValuesByGidsAndMarkerNames;
	}


	private long getMappingCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		/*long localMappingCountByGID = getLocalMappingCountByGID(listOfGIDs2);
		long centralMappingCountByGID = getCentralMappingCountByGID(listOfGIDs2);
		return localMappingCountByGID + centralMappingCountByGID;
		*/
		int mappingCount = (int)genoManager.countMappingPopValuesByGids(listOfGIDs2);
		return mappingCount;
		
	}


	private long getLocalMappingCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		try {
		/* SQLQuery query = localSession.createSQLQuery(MappingPopValues.GET_MAPPING_COUNT_BY_GID);
         query.setParameterList("gIdList", listOfGIDs2);
         BigInteger mappingCount = (BigInteger) query.uniqueResult();
         return mappingCount.intValue();*/
			int mappingCount = (int)genoManager.countMappingPopValuesByGids(listOfGIDs2);
			 return mappingCount;
		} catch (HibernateException e) {
            throw new MiddlewareQueryException("Error with getLocalMappingCountByGID(gids=" + listOfGIDs2 + ") query from MappingPopValues: " + e.getMessage(), e);
        }
	}


	private long getCentralMappingCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		try {
			// SQLQuery query = centralSession.createSQLQuery(MappingPopValues.GET_MAPPING_COUNT_BY_GID);
			 
			int mappingCount = (int)genoManager.countMappingPopValuesByGids(listOfGIDs2);
			 
	        // query.setParameterList("gIdList", listOfGIDs2);
	         //BigInteger mappingCount = (BigInteger) query.uniqueResult();
	         return mappingCount;
			} catch (HibernateException e) {
	            throw new MiddlewareQueryException("Error with getLocalMappingCountByGID(gids=" + listOfGIDs2 + ") query from MappingPopValues: " + e.getMessage(), e);
	        }
		}


	private long getCharCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		long localCharCountByGID = getLocalCharCountByGID(listOfGIDs2);
		long centralCharCountByGID = getCentralCharCountByGID(listOfGIDs2);
		return localCharCountByGID + centralCharCountByGID;
	}


	private long getCentralCharCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		CharValuesDAO charValuesDAO = new CharValuesDAO();
		charValuesDAO.setSession(centralSession);
		return charValuesDAO.countCharValuesByGids(listOfGIDs2);
	}


	private long getLocalCharCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		CharValuesDAO charValuesDAO = new CharValuesDAO();
		charValuesDAO.setSession(localSession);
		return charValuesDAO.countCharValuesByGids(listOfGIDs2);
	}


	private long getAlleleCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		long localAlleleCountByGID = getLocalAlleleCountByGID(listOfGIDs2);
		long centralAlleleCountByGID = getCentralAlleleCountByGID(listOfGIDs2);
		return localAlleleCountByGID + centralAlleleCountByGID;
	}


	private long getLocalAlleleCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		AlleleValuesDAO alleleValuesDAO = new AlleleValuesDAO();
		alleleValuesDAO.setSession(localSession);
		return alleleValuesDAO.countAlleleValuesByGids(listOfGIDs2);
	}


	private long getCentralAlleleCountByGID(List<Integer> listOfGIDs2) throws MiddlewareQueryException {
		AlleleValuesDAO alleleValuesDAO = new AlleleValuesDAO();
		alleleValuesDAO.setSession(centralSession);
		return alleleValuesDAO.countAlleleValuesByGids(listOfGIDs2);
	}


	private List<Qtl> getQTLDAO(List<QtlDetails> qtlIdsListByListOfMapIds) throws MiddlewareQueryException {
		List<Integer> listOfQLTIds = new ArrayList<Integer>();
		for (QtlDetails qtlDetails : qtlIdsListByListOfMapIds) {
			if(false == listOfQLTIds.contains(qtlDetails.getQtlId())) {
				listOfQLTIds.add(qtlDetails.getQtlId());
			}
		}
		return getQTLDAOByQTLIds(listOfQLTIds);
	}


	private List<Qtl> getQTLDAOByQTLIds(List<Integer> listOfQLTIds) throws MiddlewareQueryException {
		List<Qtl> listOfQTLDetailElement = new ArrayList<Qtl>();
		List<Qtl> localQTLDAO = getLocalQTLDAO(listOfQLTIds);
		if(null != localQTLDAO) {
			listOfQTLDetailElement.addAll(localQTLDAO);
		}
		List<Qtl> centralQTLDAO = getCentralQTLDAO(listOfQLTIds);
		if(null != centralQTLDAO) {
			listOfQTLDetailElement.addAll(centralQTLDAO);
		}
		return listOfQTLDetailElement;
	}


	private List<Qtl> getLocalQTLDAO(List<Integer> listOfQLTIds) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(localSession);
		List<Qtl> listToReturn = new ArrayList<Qtl>();
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			for (Integer integer : listOfQLTIds) {
				if(qtl.getDatasetId().equals(integer)) {
					listToReturn.add(qtl);
				}
			}
		}
		
		return listToReturn;
	}


	private List<Qtl> getCentralQTLDAO(List<Integer> listOfQLTIds) throws MiddlewareQueryException {
		QtlDAO qtlDAO = new QtlDAO();
		qtlDAO.setSession(centralSession);
		List<Qtl> listToReturn = new ArrayList<Qtl>();
		List<Qtl> all = qtlDAO.getAll();
		for (Qtl qtl : all) {
			for (Integer integer : listOfQLTIds) {
				if(qtl.getDatasetId().equals(integer)) {
					listToReturn.add(qtl);
				}
			}
		}
		
		return listToReturn;
	}


	private List<QtlDetails> getQTLIdsListByListOfMapIds(List<Integer> listOfMapIds) throws MiddlewareQueryException {
		List<QtlDetails> listOfQTLIds = new ArrayList<QtlDetails>();
		RetrieveQTL retrieveQTL = new RetrieveQTL();
		List<QtlDetails> retrieveQTLDetailsWithQTLDetailsPK = retrieveQTL.retrieveQTLDetailsWithQTLDetailsPK();
		for (QtlDetails qtlDetails : retrieveQTLDetailsWithQTLDetailsPK) {
			for (Integer integer : listOfMapIds) {
				if(qtlDetails.getMapId().equals(integer)) {
					boolean bFound = false;
					for (QtlDetails qtlDetails2 : listOfQTLIds) {
						if(qtlDetails2.getQtlId().equals(qtlDetails.getQtlId())) {
							bFound = true;
						}
					}
					if(false == bFound) {
						listOfQTLIds.add(qtlDetails);
					}
				}
			}
		}
		return listOfQTLIds;
	}


	/*private List<Integer> getListOfMapIds(String strMap) throws MiddlewareQueryException {
		
		genoManager.getMapIdByName(arg0)
		List<Integer> listOfMapIds = new ArrayList<Integer>();
		List<Map> mapDAO = getMapDAO();
		for (Map map : mapDAO) {
			if(map.getMapName().equals(strMap)) {
				if(false == listOfMapIds.contains(map.getMapId())) {
					listOfMapIds.add(map.getMapId());
				}
			}
		}
		return listOfMapIds;
	}*/


	/*private List<Map> getMapDAO() throws MiddlewareQueryException {
		List<Map> listToReturn = new ArrayList<Map>();
		List<Map> localMapDAO = getLocalMapDAO();
		if(null != localMapDAO) {
			listToReturn.addAll(localMapDAO);
		}
		List<Map> centralMapDAO = getCentralMapDAO();
		if(null != centralMapDAO) {
			listToReturn.addAll(centralMapDAO);
		}
		return listToReturn;
	}*/


	/*private List<Map> getCentralMapDAO() throws MiddlewareQueryException {
		MapDAO mapDAO = new MapDAO();
		mapDAO.setSession(centralSession);
		List<Map> all = mapDAO.getAll();
		return all;
	}


	private List<Map> getLocalMapDAO() throws MiddlewareQueryException {
		MapDAO mapDAO = new MapDAO();
		mapDAO.setSession(localSession);
		List<Map> all = mapDAO.getAll();
		return all;
	}
*/
	
	private List<Integer> getNidfromAccMetadataset(List<Integer> integerGID) throws MiddlewareQueryException {
		List<Integer> listOfNids = new ArrayList<Integer>();
		
		Name names = null;
		/*List<AccMetadataSetPK> accMetadataSets = genoManager.getGdmsAccMetadatasetByGid(integerGID, 0, (int) genoManager.countGdmsAccMetadatasetByGid(integerGID));
		for (AccMetadataSetPK accMetadataSet : accMetadataSets) {*/
		List<AccMetadataSet> accMetadataSets = genoManager.getGdmsAccMetadatasetByGid(integerGID, 0, (int) genoManager.countGdmsAccMetadatasetByGid(integerGID));
		for (AccMetadataSet accMetadataSet : accMetadataSets) {
			listOfNids.add(accMetadataSet.getNameId());
        	datasetIDs.add(accMetadataSet.getDatasetId());
        }
		
		/*List<Integer> localMetadataSetDAO = getLocalMetadataSetDAO(integerGID);
		if(null != localMetadataSetDAO) {
			for (Integer integer : localMetadataSetDAO) {
				if(false == listOfNids.contains(integer)) {
					listOfNids.add(integer);
				}
			}
		}
		List<Integer> centralMetadataSetDAO = getCentralMetadataSetDAO(integerGID);
		if(null != centralMetadataSetDAO) {
			for (Integer integer : centralMetadataSetDAO) {
				if(false == listOfNids.contains(integer)) {
					listOfNids.add(integer);
				}
			}
		}*/
		return listOfNids;
	}


	/*private List<Integer> getCentralMetadataSetDAO(List<Integer> integerGID)
			throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(centralSession);
		return accMetadataSetDAO.getNameIdsByGermplasmIds(integerGID);
	}*/

/*
	private List<Integer> getLocalMetadataSetDAO(List<Integer> integerGID) throws MiddlewareQueryException {
		AccMetadataSetDAO accMetadataSetDAO = new AccMetadataSetDAO();
		accMetadataSetDAO.setSession(localSession);
		return accMetadataSetDAO.getNameIdsByGermplasmIds(integerGID);
	}*/


	private Component buildResultComponent() {
		VerticalLayout resultsLayout = new VerticalLayout();
		resultsLayout.setCaption("Results");
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true, true, true, true);

		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		//System.out.println(absoluteFile);


		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("WEB-INF")) {
				fileExport = file;
				break;
			}
		}

		final String strAbsolutePath = fileExport.getAbsolutePath();
		

		
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
					File directory = new File(folderPath);
					File[] fList = directory.listFiles();
					for (File file : fList){
						if (file.isFile()){
							System.out.println(file.getName());
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
		
		
		final String strFJVisualizeLink = strAbsolutePath + "\\" + "flapjackrun.bat";
		HorizontalLayout horLayoutForExportTypes = new HorizontalLayout();
		horLayoutForExportTypes.setSpacing(true);
		
		realPath=_mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory().toString();
			
		
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
		
		/*String strGenotypicMatrixFile = _gdmsModel.getGenotypicMatrixFileName();
		String strGTPExcelFileLink = strAbsolutePath + strGenotypicMatrixFile;
		Link gtpExcelFileLink = new Link("Download Genotypic Matrix file", new ExternalResource(strGTPExcelFileLink));
		gtpExcelFileLink.setTargetName("_blank");*/
		
		Button btnDownloadMatrixFile = new Button("Download Matrix File");
		btnDownloadMatrixFile.setStyleName(Reindeer.BUTTON_LINK);
		btnDownloadMatrixFile.addListener(new Button.ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
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
		if (null == strSelectedFormat) {
			layoutForExportTypes.addComponent(excelButton);
		}

		

			//20131216: Added link to download Similarity Matrix File
		final String strSMVisualizeLink = strAbsolutePath + "\\" + "flapjackMatrix.bat";
		Button similarityMatrixButton = new Button("Similarity Matrix File");
		//similarityMatrixButton.setStyleName(Reindeer.BUTTON_LINK);
		similarityMatrixButton.setDescription("Similarity Matrix File");
		similarityMatrixButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				
				File similarityMatrixFile = new File("");
				String[] cmd = {"cmd.exe", "/c", "start", "\""+"flapjack"+"\"", strSMVisualizeLink};
				Runtime rtSM = Runtime.getRuntime();
				try {
					rtSM.exec(cmd);
				
				} catch (IOException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error occurred while trying to create Similarity Matrix.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
			}
		});
		horLayoutForExportTypes.addComponent(similarityMatrixButton);
		//20131216: Added link to download Similarity Matrix File
				
		if (null != strSelectedFormat){
			if (strSelectedFormat.equals("Flapjack")){
				if(null != generateFlagjackFiles) {
					
					/*resultsLayout.addComponent(btnVisualizeFJ);
					resultsLayout.addComponent(similarityMatrixButton);*/
					resultsLayout.addComponent(horLayoutForExportTypes);
					
					resultsLayout.addComponent(flapjackFilesLayout);
				}
			} else if (strSelectedFormat.equals("Matrix")) {
				resultsLayout.addComponent(btnDownloadMatrixFile);
			}
		} else {
			excelButton.setEnabled(false);
			
		}
		if(false == (null != strSelectedFormat && strSelectedFormat.equals("Flapjack"))) {
			resultsLayout.addComponent(layoutForExportTypes);
			resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
		}
		
		return resultsLayout;
	}


	protected File createFlapjackTextFile() {
		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		//System.out.println(absoluteFile);

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		String strAbsolutePath = fileExport.getAbsolutePath();
		//System.out.println(">>>>>" + strAbsolutePath);

		String strFJTextFileLink = strAbsolutePath + "/Flapjack.txt";
		Link fjTextFileLink = new Link("View Flapjack.txt file", new ExternalResource(strFJTextFileLink));
		fjTextFileLink.setTargetName("_blank");

		return null;
	}


	protected File createFlapjackMapFile() {
		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		//System.out.println(absoluteFile);

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}

		String strAbsolutePath = fileExport.getAbsolutePath();
		//System.out.println(">>>>>" + strAbsolutePath);

		String strFJMapFileLink = strAbsolutePath + "/Flapjack.map";
		Link fjMapFileLink = new Link("View Flapjack.map file", new ExternalResource(strFJMapFileLink));
		fjMapFileLink.setTargetName("_blank");

		return null;
	}

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}
}
