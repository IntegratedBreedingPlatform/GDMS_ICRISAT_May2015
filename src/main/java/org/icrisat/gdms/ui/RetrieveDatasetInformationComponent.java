package org.icrisat.gdms.ui;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.generationcp.middleware.dao.gdms.DatasetDAO;
import org.generationcp.middleware.dao.gdms.MapDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.AllelicValueWithMarkerIdElement;
import org.generationcp.middleware.pojos.gdms.CharValues;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.MapDetailElement;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.gdms.ParentElement;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.FileDownloadResource;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.ui.common.OptionWindowForFlapjackMap;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
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
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

public class RetrieveDatasetInformationComponent implements Component.Listener {
	private static final long serialVersionUID = 1L;

	private TabSheet _tabsheetForDataset;
	private Component _buildDatasetResultComponent;
	private Component _buildDatasetFormatComponent;
	private GDMSMain _mainHomePage;
	protected String strDatasetID;
	protected String strDatasetName;
	private String strSelectedMap;
	private String strSelectedColumn;
	protected String strSelectedFormat;
	HashMap<Integer, String> hashMapOfMapIDsAndNames = new HashMap<Integer, String>(); 
	private Session localSession;
	private Session centralSession;
	private ArrayList<String> listOfAllMaps;
	//private HashMap<String, Integer> hmOfMapNameAndMapId;
	protected File matrixFileForDatasetRetrieval;
	protected String strDatasetType;
	private ArrayList<Integer> listOfParentGIDs;
	protected Integer iSelectedMapId;
	public File generatedTextFile;
	public File generatedMapFile;
	public File generatedDatFile;
	
	
	List<Integer> markerIDsList;
	
	
	public File flapjackDat;
	public File flapjackMap;
 	public File flapjackTxt;
 	
 	String markerType="";
 	SQLQuery queryL;
	SQLQuery queryC;
	String strQuerry="";
	String pgids="";
	String mid="";
	
	SQLQuery queryMNL;
	SQLQuery queryMNC;
	
	public String folderPath;
	public String strQTLExists=""; 
	
	protected List<File> listOfmatrixTextFileDataSSRDataset;
	int markersCount=0;
	ManagerFactory factory=null;
	GermplasmDataManager germManager;
	GenotypicDataManager genoManager;
	List<AllelicValueWithMarkerIdElement> alleleValues;
	//ArrayList intAlleleValues=new ArrayList();
	String realPath="";
	
	private TreeMap<Object, String> sortedMapOfGIDsAndGNames;
	
	HashMap<Integer, String> markerIDsNamesMap=new HashMap<Integer, String>();
	
	
	SortedMap mapA = new TreeMap();
	SortedMap mapB = new TreeMap();
	
	int parentANid=0;
	int parentBNid=0;
	
	int parentAGid=0;
	int parentBGid=0;
	String mappingType="";
	String parentsListToWrite="";
	private HashMap<Integer, String> parentsGIDsNames;
	HashMap<Object, String> hmOfGIDs;
	HashMap<Object, String> IBWFProjects= new HashMap<Object, String>();
	 boolean selectedOP=false;
	 String bPath="";
     String opPath="";
    
     //////System.out.println(",,,,,,,,,,,,,  :"+bPath.substring(0, bPath.indexOf("IBWorkflowSystem")-1));
     String pathWB="";
     
	    String dbNameL="";
	
	
	protected String strSelectedMappingType = "";
	
	
	HashMap<Object, HashMap<String, Object>> mapEx = new HashMap<Object, HashMap<String,Object>>();	
	HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
	HashMap marker = new HashMap();
	
	ArrayList glist = new ArrayList();
	
	public RetrieveDatasetInformationComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			germManager=factory.getGermplasmDataManager();
			genoManager=factory.getGenotypicDataManager();			
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Building the entire Tabbed Component required for Dataset
	 * 
	 */
	public HorizontalLayout buildTabbedComponentForQTL() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();

		_tabsheetForDataset = new TabSheet();
		//_tabsheetForDataset.setSizeFull();
		_tabsheetForDataset.setWidth("700px");

		Component buildDatasetDataSetComponent = buildDatasetDataSetComponent();
		_buildDatasetFormatComponent = buildDatasetFormatComponent();
		_buildDatasetResultComponent = buildDatasetResultComponent();
		
		buildDatasetDataSetComponent.setSizeFull();
		_buildDatasetFormatComponent.setSizeFull();
		_buildDatasetResultComponent.setSizeFull();
		
		_tabsheetForDataset.addComponent(buildDatasetDataSetComponent);
		_tabsheetForDataset.addComponent(_buildDatasetFormatComponent);
		_tabsheetForDataset.addComponent(_buildDatasetResultComponent);

		
		_tabsheetForDataset.getTab(1).setEnabled(false);
		_tabsheetForDataset.getTab(2).setEnabled(false);
		
		horizontalLayout.addComponent(_tabsheetForDataset);

		return horizontalLayout;
	}

	private Component buildDatasetDataSetComponent() {
		
		VerticalLayout datasetLayout = new VerticalLayout();
		datasetLayout.setCaption("Dataset");
		datasetLayout.setSpacing(true);
		datasetLayout.setSizeFull();
		datasetLayout.setMargin(true, true, true, true);

		Panel panelForTable = new Panel();
		panelForTable.setWidth("680px");

		final Table tableForDatasetResults = buildDatasetDetailsTable();
		datasetLayout.addComponent(tableForDatasetResults);
		datasetLayout.setComponentAlignment(tableForDatasetResults, Alignment.MIDDLE_CENTER);

		
		VerticalLayout layoutForButton = new VerticalLayout();
		Button btnNext = new Button("Next");
		layoutForButton.addComponent(btnNext);
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				
				Object rowID = tableForDatasetResults.getValue();
				if (null != rowID){
					Container containerDataSource = tableForDatasetResults.getContainerDataSource();
					Item item = containerDataSource.getItem(rowID);
					
					if (null != item){
						Dataset dataset = listOfAllDatasets.get((Integer) rowID);
						strDatasetName = item.getItemProperty("DATASET-NAME").toString();						
						try{							
							List<DatasetElement> resultsL =genoManager.getDatasetDetailsByDatasetName(strDatasetName, Database.LOCAL);						
							if(resultsL.isEmpty()){							
								resultsL = genoManager.getDatasetDetailsByDatasetName(strDatasetName, Database.CENTRAL);
							}
							for (DatasetElement result : resultsL){					        	
					        	strDatasetID=result.getDatasetId().toString();
					        }							
						
						}catch (MiddlewareQueryException e) {
							//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
							e.printStackTrace();
							return;
						}
						
						strDatasetType = dataset.getDatasetType();
						
						VerticalLayout newFormatComponent = (VerticalLayout) buildDatasetFormatComponent();
						if (null != newFormatComponent) {
							_tabsheetForDataset.replaceComponent(_buildDatasetFormatComponent, newFormatComponent);
							_buildDatasetFormatComponent = newFormatComponent;
							_buildDatasetFormatComponent.requestRepaint();
							_tabsheetForDataset.getTab(1).setEnabled(true);
							_tabsheetForDataset.setSelectedTab(_buildDatasetFormatComponent);
							_tabsheetForDataset.requestRepaint();
						}
					}else {
						if (null == strDatasetID || null == strDatasetName || null == strDatasetType){
							_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required dataset from the Dataset tab and click next.", Notification.TYPE_ERROR_MESSAGE);
							
							return;
						}
					}
				} else {
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required dataset from the Dataset tab and click next.", Notification.TYPE_ERROR_MESSAGE);
					
					return;
				}
			}
		});

		datasetLayout.addComponent(layoutForButton);
		datasetLayout.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);

		return datasetLayout;
	}

	private Table buildDatasetDetailsTable() {
		Table tableForDatasetDetails = new Table();
		tableForDatasetDetails.setWidth("650px");
		tableForDatasetDetails.setSelectable(true);
		tableForDatasetDetails.setColumnCollapsingAllowed(true);
		tableForDatasetDetails.setColumnReorderingAllowed(false);
		tableForDatasetDetails.setStyleName("strong");

		/*String[] strArrayOfColNames = {"DATASET-ID", "DATASET-NAME", "DATASET-DESC", "DATASET-TYPE",
                "GENUS", "SPECIES", "DATATYPE"};*/
		
		/*String[] strArrayOfColNames = {"DATASET-ID", "DATASET-NAME", "DATASET-DESC", "SPECIES"};*/
		
		//20131206: Tulasi --- Modified the columns to be displayed
		String[] strArrayOfColNames = {"DATASET-NAME", "DATASET-DESC", "DATASET-TYPE", "DATASET SIZE(Genotypes x Markers)"};
		
		for (int i = 0; i < strArrayOfColNames.length; i++){
			tableForDatasetDetails.addContainerProperty(strArrayOfColNames[i], String.class, null);
			tableForDatasetDetails.setColumnWidth(strArrayOfColNames[i], 135);
		}
		
		DatasetDAO datasetDAOForLocal = new DatasetDAO();
		datasetDAOForLocal.setSession(localSession);
		DatasetDAO datasetDAOForCentral = new DatasetDAO();
		datasetDAOForCentral.setSession(centralSession);
		List<Dataset> listOfAllDatasetsFromLocalDB = null;
		List<Dataset> listOfAllDatasetsFromCentralDB = null;
		try {
			listOfAllDatasetsFromLocalDB = datasetDAOForLocal.getAll();
			listOfAllDatasetsFromCentralDB = datasetDAOForCentral.getAll();
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			
			return null;
		}
		listOfAllDatasets = new ArrayList<Dataset>();
		//genoManager.getaset>();
		if (null != listOfAllDatasetsFromLocalDB && 0 != listOfAllDatasetsFromLocalDB.size()){
			for (Dataset dataset : listOfAllDatasetsFromLocalDB){				
				if ((false == "QTL".equalsIgnoreCase(dataset.getDatasetType().toString()))&&(dataset.getDatasetType().toString().equalsIgnoreCase("MTA")==false)){
					listOfAllDatasets.add(dataset);
				}
			}
		}
		
		if (null != listOfAllDatasetsFromCentralDB && 0 != listOfAllDatasetsFromCentralDB.size()){
			for (Dataset dataset : listOfAllDatasetsFromCentralDB){
				//if (false == "QTL".equalsIgnoreCase(dataset.getDatasetType().toString())){
				if ((false == "QTL".equalsIgnoreCase(dataset.getDatasetType().toString()))&&(dataset.getDatasetType().toString().equalsIgnoreCase("MTA")==false)){
					listOfAllDatasets.add(dataset);
				}
			}
		}
		
		ArrayList datasetIdsList=new ArrayList();
		HashMap<Integer, String> datasetSize=new HashMap<Integer, String>();
		
		for (int i = 0; i < listOfAllDatasets.size(); i++){
			Dataset dataset = listOfAllDatasets.get(i);			
			datasetIdsList.add(dataset.getDatasetId());					
			try{
				int markerCount=(int)genoManager.countMarkersFromMarkerMetadatasetByDatasetIds(datasetIdsList);
				//int nidsCount=(int)genoManager.countNidsFromAccMetadatasetByDatasetIds(datasetIdsList);				
				int nidsCount=(int)genoManager.countAccMetadatasetByDatasetIds(datasetIdsList);
				String size=nidsCount+" x "+markerCount;
				datasetSize.put(Integer.parseInt(dataset.getDatasetId().toString()), size);
			} catch (MiddlewareQueryException e) {
				e.printStackTrace();
				_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
				return null;
			}
			datasetIdsList.clear();
		}
		
		for (int i = 0; i < listOfAllDatasets.size(); i++){

			Dataset dataset = listOfAllDatasets.get(i);			
			datasetIdsList.add(dataset.getDatasetId());
			String strDatasetName = dataset.getDatasetName();
			String strDatasetDesc = dataset.getDatasetDesc();
			String strDatasetType = dataset.getDatasetType();
			String strCount = "0";
			
			strCount=datasetSize.get(dataset.getDatasetId());
			
			tableForDatasetDetails.addItem(new Object[] {strDatasetName, strDatasetDesc, strDatasetType, strCount}, new Integer(i));
			
		}
		return tableForDatasetDetails;
	}

	private Component buildDatasetFormatComponent() {
		
	
				strMappingType = "";
				if (null != strDatasetType && strDatasetType.equalsIgnoreCase("Mapping")) {
					
					try {
						List<ParentElement> results =genoManager.getParentsByDatasetId(Integer.parseInt(strDatasetID));
						for (ParentElement parentElement : results){
							
							strMappingType=parentElement.getMappingType();
						}
						
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MiddlewareQueryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				final OptionGroup optionGroupForMappingType = new OptionGroup();
				optionGroupForMappingType.setMultiSelect(false);
				optionGroupForMappingType.addStyleName("horizontal");
				optionGroupForMappingType.addItem("Allelic");
				optionGroupForMappingType.addItem("ABH");
				optionGroupForMappingType.setImmediate(true);
				
				Label lblMappingType = new Label("Mapping Type: ");
				lblMappingType.setStyleName(Reindeer.LABEL_SMALL);
				
				HorizontalLayout horizLayoutForMappingType = new HorizontalLayout();
				horizLayoutForMappingType.setSpacing(true);
				horizLayoutForMappingType.addComponent(lblMappingType);
				horizLayoutForMappingType.addComponent(optionGroupForMappingType);
				//20131212: Tulasi --- Implemented code to display Allelic and ABH options, if the dataset selected on the first tab is of Mapping-Allelic type
				
		/**
		 * 
		 * Title label on the top
		 * 
		 */
		Label lblTitle = new Label("Choose Data Export Format");
		lblTitle.setStyleName(Reindeer.LABEL_H2);

		/**
		 * 
		 * Building the left side components and layout
		 * 
		 */
		VerticalLayout layoutForGenotypingMatrixFormat = new VerticalLayout();
		layoutForGenotypingMatrixFormat.setSpacing(true);
		layoutForGenotypingMatrixFormat.setMargin(true, true, true, true);
		
		Label lblColumn = new Label("Identify a Column");
		lblColumn.setStyleName(Reindeer.LABEL_SMALL);
		
		final OptionGroup optionGroupForColumn = new OptionGroup();
		optionGroupForColumn.setMultiSelect(false);
		optionGroupForColumn.addStyleName("horizontal");
		optionGroupForColumn.addItem("GIDs");
		optionGroupForColumn.addItem("Germplasm Names");		
		optionGroupForColumn.select("Germplasm Names");
		
		//optionGroupForColumn.setItemEnabled("Germplasm Names", true);
		optionGroupForColumn.setImmediate(true);
		optionGroupForColumn.setEnabled(false);
		
		HorizontalLayout horizLayoutForColumns = new HorizontalLayout();
		horizLayoutForColumns.setSpacing(true);
		horizLayoutForColumns.addComponent(lblColumn);
		horizLayoutForColumns.addComponent(optionGroupForColumn);
		
		chbMatrix = new CheckBox();
		chbMatrix.setCaption("Genotyping X Marker Matrix");
		chbMatrix.setHeight("25px");
		chbMatrix.setImmediate(true);
		layoutForGenotypingMatrixFormat.addComponent(chbMatrix);
		chbMatrix.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				if (true == (Boolean) chbMatrix.getValue()){
					chbFlapjack.setValue(false);
					optionGroupForColumn.setEnabled(false);
					selectMap.setEnabled(false);
				} else {
					chbMatrix.setValue(false);
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


		selectMap = new ComboBox();
		selectMap.setWidth("200px");
		Object itemId = selectMap.addItem();
		selectMap.setItemCaption(itemId, "Select Map");
		selectMap.setValue(itemId);
		selectMap.setNullSelectionAllowed(false);
		selectMap.setImmediate(true);
		selectMap.setEnabled(false);
		selectMap.addListener(new Property.ValueChangeListener(){
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				strSelectedMap = selectMap.getValue().toString();
				//iSelectedMapId = hmOfMapNameAndMapId.get(strSelectedMap);
			}
		});
		
		
		MapDAO mapDAOLocal = new MapDAO();
		mapDAOLocal.setSession(localSession);
		MapDAO mapDAOCentral = new MapDAO();
		mapDAOCentral.setSession(centralSession);
		listOfAllMaps = new ArrayList<String>();
		//hmOfMapNameAndMapId = new HashMap<String, Integer>();
		
		//System.out.println("strDatasetID=:"+strDatasetID);
		
		try {
			
			//List maps=new ArrayList();			
			if(strDatasetID != null){
				
				
				
				List<Integer> markerIDsFromSelectedDataset=genoManager.getMarkerIdsByDatasetId(Integer.parseInt(strDatasetID));		
				//System.out.println(".............:"+markerIDsFromSelectedDataset);
				List<MapDetailElement> details = genoManager.getMapAndMarkerCountByMarkers(markerIDsFromSelectedDataset);
		        if (details != null && details.size() > 0) {		            
		            for (MapDetailElement detail : details) {		               
		                if (false == listOfAllMaps.contains(detail.getMapName())){
							listOfAllMaps.add(detail.getMapName() + "(" + detail.getMarkerCount()+")");							
						}
		            }
		        } else {		            
		        	List<Map> resMapsCentral=genoManager.getAllMaps(0, (int)genoManager.countAllMaps(Database.CENTRAL), Database.CENTRAL);
					if(!resMapsCentral.isEmpty()){
						for (Map map: resMapsCentral){
							if (false == listOfAllMaps.contains(map.getMapName())){
								listOfAllMaps.add(map.getMapName()+"(0)");								
							}
						}
					}
		        	List<Map> resMapsLocal=genoManager.getAllMaps(0, (int)genoManager.countAllMaps(Database.LOCAL), Database.LOCAL);
					if(! resMapsLocal.isEmpty()){
						for (Map map: resMapsLocal){
							if (false == listOfAllMaps.contains(map.getMapName())){
								listOfAllMaps.add(map.getMapName()+"(0)");								
							}
						}
					}
					
		        }
			
			}
			//System.out.println("listOfAllMaps:"+listOfAllMaps);
			if (null != listOfAllMaps){
				for (int i = 0; i < listOfAllMaps.size(); i++){
					//Map map = listOfAllMaps.get(i);
					selectMap.addItem(listOfAllMaps.get(i));
				}
			}
			
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
			
			return null;
		}
		chbFlapjack = new CheckBox();
		chbFlapjack.setCaption("Flapjack");
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
		
		final OptionGroup optionGroupForFormat = new OptionGroup();
		optionGroupForFormat.setMultiSelect(false);
		optionGroupForFormat.setStyleName("horizontal");
		optionGroupForFormat.addItem(chbMatrix);
		optionGroupForFormat.addItem(chbFlapjack);
		optionGroupForFormat.setEnabled(false);
		optionGroupForFormat.setImmediate(true);
		chbFlapjack.addListener(new ClickListener(){
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				if (chbFlapjack.getValue().toString().equals("true")){
					selectMap.setEnabled(true);
					optionGroupForColumn.setEnabled(true);
				} else {
					selectMap.setEnabled(false);
					optionGroupForColumn.setEnabled(false);
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
			private boolean bGenerateFlapjack;
			private boolean dataToBeExportedBuiltSuccessfully;
			
			public void buttonClick(ClickEvent event) {


				dataToBeExportedBuiltSuccessfully = false;
				
				//20131212: Tulasi --- Implemented code to make Mapping Type as a mandatory selection
				//if the Dataset row selected on the the first tab is of Mapping-Allelic type
				if (null == strDatasetID || null == strDatasetName || null == strDatasetType){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required dataset from the Dataset tab and click Next.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (strDatasetType.equalsIgnoreCase("mapping") && strMappingType.equalsIgnoreCase("allelic")) {
					Object mappingTypeValue = optionGroupForMappingType.getValue();
					if (null != mappingTypeValue){
						strSelectedMappingType = mappingTypeValue.toString();
					} else {
						//strSelectedMappingType = "";
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required mapping type for the selected Mapping-Allelic Dataset.", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				}
				//20131212: Tulasi --- Implemented code to make Mapping Type as a mandatory selection
				//if the Dataset row selected on the the first tab is of Mapping-Allelic type
				if (chbMatrix.getValue().toString().equals("true")){
					strSelectedFormat = "Matrix";
					//////System.out.println("Format Selected: " + strSelectedFormat);

					/*if (null == strDatasetID || null == strDatasetName || null == strDatasetType){
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required dataset from the Dataset tab and click next.", Notification.TYPE_ERROR_MESSAGE);
						return;
					}*/
					
					try {
						
						retrieveDatasetDetailsForMatrixFormat();
						//System.out.println("intAlleleValues"+intAlleleValues);
						dataToBeExportedBuiltSuccessfully = true;
					} catch (GDMSException e1) {
						dataToBeExportedBuiltSuccessfully = false;
						_mainHomePage.getMainWindow().getWindow().showNotification(e1.getExceptionMessage(), Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					//System.out.println("listOfNIDs="+listOfNIDs);
					if (dataToBeExportedBuiltSuccessfully) {
						ExportFileFormats exportFileFormats = new ExportFileFormats();
						try {
							if ((strDatasetType.equalsIgnoreCase("SSR"))||(strDatasetType.equalsIgnoreCase("DArT"))){
								if (markersCount > 252){
									matrixFileForDatasetRetrieval = exportFileFormats.MatrixTextFileDataSSRDataset(_mainHomePage, listOfAllMarkers, hmOfNIDAndNVal, hmOfMIdAndMarkerName, glist,hmOfGIDs, markerAlleles);
								} else {
									matrixFileForDatasetRetrieval = exportFileFormats.MatrixForSSRDataset(_mainHomePage, listOfAllMarkers, hmOfNIDAndNVal, hmOfMIdAndMarkerName,mapEx,glist,hmOfGIDs);
								}
							}
							
							if (strDatasetType.equalsIgnoreCase("SNP")){
								if (markersCount > 252){
									//matrixFileForDatasetRetrieval = exportFileFormats.MatrixTextFileDataSSRDataset(_mainHomePage, listOfAllMarkers, hmOfNIDAndNVal, hmOfMIdAndMarkerName, mapEx,glist,hmOfGIDs, markerAlleles);
									matrixFileForDatasetRetrieval = exportFileFormats.MatrixTextFileDataSSRDataset(_mainHomePage, listOfAllMarkers, hmOfNIDAndNVal, hmOfMIdAndMarkerName, glist,hmOfGIDs, markerAlleles);
								} else {
									matrixFileForDatasetRetrieval = exportFileFormats.MatrixForSNPDataset(_mainHomePage, listOfAllMarkers, hmOfNIDAndNVal, hmOfMIdAndMarkerName, mapEx,glist,hmOfGIDs);
								}
							}
							if (strDatasetType.equalsIgnoreCase("mapping")){
								matrixFileForDatasetRetrieval = exportFileFormats.MatrixForMappingDataset(_mainHomePage, listOfAllMarkers, sortedMapOfGIDsAndGNames, hmOfMIdAndMarkerName, mapEx,glist,hmOfGIDs);
							}

						} catch (GDMSException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification("Error generating Matrix file", Notification.TYPE_ERROR_MESSAGE);
							e.printStackTrace();
							return;
						}
					}
				}  else if (chbFlapjack.getValue().toString().equals("true")){

					
					if ("true".equals(chbFlapjack.getValue().toString())){
						strSelectedFormat = "Flapjack";
					}

					Object mapValue = selectMap.getValue();
					if (mapValue instanceof Integer){
						Integer itemId = (Integer)mapValue;
						if (itemId.equals(1)){
							strSelectedMap = "";
						} 
					} else {
						String strMapSelected = mapValue.toString();
						
						//if(strMapSelected)
							strSelectedMap = strMapSelected.substring(0,strMapSelected.lastIndexOf("("));
						/*else
							strSelectedMap = strMapSelected;*/
						try{
							//String mapName  = strMapSelected.substring(0,strMapSelected.lastIndexOf("("));
							iSelectedMapId=genoManager.getMapIdByName(strSelectedMap);
						} catch (MiddlewareQueryException e) {
							e.printStackTrace();
							_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Map Id for the selected Map", Notification.TYPE_ERROR_MESSAGE);							
						}
					}

					Object value = optionGroupForColumn.getValue();
					if (null != value){
						strSelectedColumn = value.toString();
					} else {
						strSelectedColumn = "";
					}
					////System.out.println("Selected Map: " + strSelectedMap + " --- " + "Selected Column: " + strSelectedColumn);


					if (strSelectedMap.equals("")){
						/*_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required map for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;*/
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
									retrieveDataForFlapjack.setGenotypingType("Dataset");
									retrieveDataForFlapjack.setDatasetName(strDatasetName);
									retrieveDataForFlapjack.setDatasetID(strDatasetID);
									retrieveDataForFlapjack.setDatasetType(strDatasetType);
									retrieveDataForFlapjack.setMapSelected(strSelectedMap, iSelectedMapId);
									retrieveDataForFlapjack.setExportType(strSelectedColumn);
									retrieveDataForFlapjack.setMappingType(selectedOP, strSelectedMappingType);
									retrieveDataForFlapjack.retrieveFlapjackData();

									dataToBeExportedBuiltSuccessfully = retrieveDataForFlapjack.isFlapjackDataBuiltSuccessfully();
									
									if (dataToBeExportedBuiltSuccessfully){
										generatedTextFile = retrieveDataForFlapjack.getGeneratedTextFile();
										generatedMapFile = retrieveDataForFlapjack.getGeneratedMapFile();
										generatedDatFile = retrieveDataForFlapjack.getGeneratedDatFile();
										
										folderPath=retrieveDataForFlapjack.getStrBMSFilePath();
										strQTLExists=retrieveDataForFlapjack.getQTLExists();
										
										Component newDatasetResultsPanel = buildDatasetResultComponent();
										_tabsheetForDataset.replaceComponent(_buildDatasetResultComponent, newDatasetResultsPanel);
										_tabsheetForDataset.requestRepaint();
										_buildDatasetResultComponent = newDatasetResultsPanel;
										_tabsheetForDataset.getTab(2).setEnabled(true);
										_tabsheetForDataset.setSelectedTab(2);
									}
									
								}
								
							}
							
						});
					} else if (strSelectedColumn.equals("")){
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required column GID or Germplasm for the Flapjack export.", Notification.TYPE_ERROR_MESSAGE);
						return;
					} else {
						if (false == bGenerateFlapjack) {
							bGenerateFlapjack = true;
						}
					}


					if (bGenerateFlapjack) {
						RetrieveDataForFlapjack retrieveDataForFlapjack = new RetrieveDataForFlapjack(_mainHomePage);
						retrieveDataForFlapjack.setGenotypingType("Dataset");
						retrieveDataForFlapjack.setDatasetName(strDatasetName);
						retrieveDataForFlapjack.setDatasetID(strDatasetID);
						retrieveDataForFlapjack.setDatasetType(strDatasetType);
						retrieveDataForFlapjack.setMapSelected(strSelectedMap, iSelectedMapId);
						retrieveDataForFlapjack.setExportType(strSelectedColumn);
						retrieveDataForFlapjack.setMappingType(selectedOP, strSelectedMappingType);
						retrieveDataForFlapjack.retrieveFlapjackData();
						dataToBeExportedBuiltSuccessfully = retrieveDataForFlapjack.isFlapjackDataBuiltSuccessfully();
						folderPath=retrieveDataForFlapjack.getStrBMSFilePath();
						strQTLExists=retrieveDataForFlapjack.getQTLExists();
						//System.out.println(" ********************    dataToBeExportedBuiltSuccessfully=:"+dataToBeExportedBuiltSuccessfully);
						if (dataToBeExportedBuiltSuccessfully){
							generatedTextFile = retrieveDataForFlapjack.getGeneratedTextFile();
							generatedMapFile = retrieveDataForFlapjack.getGeneratedMapFile();
							generatedDatFile = retrieveDataForFlapjack.getGeneratedDatFile();
						}
					}
				}
								
				if (dataToBeExportedBuiltSuccessfully){
					Component newDatasetResultsPanel = buildDatasetResultComponent();
					_tabsheetForDataset.replaceComponent(_buildDatasetResultComponent, newDatasetResultsPanel);
					_tabsheetForDataset.requestRepaint();
					_buildDatasetResultComponent = newDatasetResultsPanel;
					_tabsheetForDataset.getTab(2).setEnabled(true);
					_tabsheetForDataset.setSelectedTab(2);
				}

				if (null == strSelectedFormat || strSelectedFormat.equals("")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required Export format type.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				//System.out.println("****folderPath********"+folderPath);
				if (null == strDatasetID || null == strDatasetName || null == strDatasetType){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select the required dataset from the Dataset tab and click next.", Notification.TYPE_ERROR_MESSAGE);
					return;
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
		

		//20131212: Tulasi --- Implemented code to make Mapping Type as a mandatory selection
		//if the Dataset row selected on the the first tab is of Mapping-Allelic type
		if (null != strDatasetType) {
			if (strDatasetType.equalsIgnoreCase("Mapping") && strMappingType.equalsIgnoreCase("allelic")) {
				completeFormatLayout.addComponent(horizLayoutForMappingType);
				completeFormatLayout.setComponentAlignment(horizLayoutForMappingType, Alignment.TOP_LEFT);
			}
		}
		//20131212: Tulasi --- Implemented code to make Mapping Type as a mandatory selection
		//if the Dataset row selected on the the first tab is of Mapping-Allelic type

		completeFormatLayout.addComponent(lblTitle);
		completeFormatLayout.setComponentAlignment(lblTitle, Alignment.TOP_CENTER);
		completeFormatLayout.addComponent(horizontalLayoutForTwoFormats);
		completeFormatLayout.addComponent(layoutForButton);
		completeFormatLayout.setComponentAlignment(layoutForButton, Alignment.BOTTOM_CENTER);
		
		if (null == strDatasetID || null == strDatasetName || null == strDatasetType){
			btnNext.setEnabled(false);
			chbMatrix.setEnabled(false);
			chbFlapjack.setEnabled(false);
			optionGroupForFormat.setEnabled(false);
		} else {
			btnNext.setEnabled(true);
			chbMatrix.setEnabled(true);
			chbFlapjack.setEnabled(true);
			optionGroupForFormat.setEnabled(true);
		}


		return completeFormatLayout;
	}

	protected void retrieveDatasetDetailsForMatrixFormat() throws GDMSException {
			
		selectedOP=false;
		ArrayList<Integer> listOfDatasetID = new ArrayList<Integer>();
		listOfDatasetID.add(Integer.parseInt(strDatasetID));
		listofMarkerNamesForSNP = new ArrayList<String>();
		
		if (strDatasetType.equalsIgnoreCase("mapping")){
			ArrayList<Integer> parentList = new ArrayList<Integer>();
			ArrayList markerNames = new ArrayList();
			HashMap<String, Integer> markerNamesIDsMap=new HashMap<String, Integer>();
			
			markerIDsNamesMap=new HashMap<Integer, String>();
			
			List<Integer> listOfAllMIDsForSelectedDatasetID = new ArrayList<Integer>();
			hmOfMIdAndMarkerName = new HashMap<Integer, String>();
			hmOfMarkerNamerAndMId = new HashMap<String, Integer>();
			try {				
				listOfAllMarkers = new ArrayList<MarkerIdMarkerNameElement>();
				listOfAllMIDsForSelectedDatasetID=genoManager.getMarkerIdsByDatasetId(Integer.parseInt(strDatasetID));
				
				//genoManager.getMarkerNamesByMarkerIds(listOfAllMIDsForSelectedDatasetID);
				List<MarkerIdMarkerNameElement> markerNamesList = genoManager.getMarkerNamesByMarkerIds(listOfAllMIDsForSelectedDatasetID);
		        //////System.out.println("testGetMarkerNamesByMarkerIds(" + listOfAllMIDsForSelectedDatasetID + ") RESULTS: ");
				markersCount=markerNamesList.size();
				markerIDsList=new ArrayList<Integer>();
				listofMarkerNamesForSNP=new ArrayList<String>();
		        for (MarkerIdMarkerNameElement e : markerNamesList) {
		            //////System.out.println(e.getMarkerId() + " : " + e.getMarkerName()+"    "+markerNames.size());
		        	markerIDsList.add(e.getMarkerId());
		            listOfAllMarkers.add(e);
		            listofMarkerNamesForSNP.add(e.getMarkerName().toString());
		            hmOfMIdAndMarkerName.put(e.getMarkerId(), e.getMarkerName().toString());
		            hmOfMarkerNamerAndMId.put(e.getMarkerName().toString(), e.getMarkerId());
		           
		        }
		        if(!strSelectedMappingType.isEmpty()){
			       List<String> marker_Type=genoManager.getMarkerTypesByMarkerIds(markerIDsList);
			       markerType=marker_Type.get(0);
		        }
		        
				
			} catch (NumberFormatException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Marker IDs for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving Marker IDs for selected Dataset";
				throw new GDMSException(strErrMessage);
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Marker IDs for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving Marker IDs for selected Dataset";
				throw new GDMSException(strErrMessage);
			}
			
			hmOfGIDs = new HashMap<Object, String>();
			//List<Integer> listOfGIDs = new ArrayList<Integer>();
			listOfNIDs = new ArrayList<Integer>();
			String mappingType="";
			hmOfNIDAndNVal = new HashMap<Object, String>();
				//if (strDatasetType.equalsIgnoreCase("mapping")){
				List<Integer> parentNIDs = new ArrayList<Integer>();
				try {
					 List<ParentElement> results = genoManager.getParentsByDatasetId(Integer.parseInt(strDatasetID));
					
					 parentNIDs.add(results.get(0).getParentANId());
					 parentNIDs.add(results.get(0).getParentBGId());
					 mappingType=results.get(0).getMappingType();
					 Name names = null;
						
						for(int n=0;n<parentNIDs.size();n++){
							
							names=germManager.getGermplasmNameByID(Integer.parseInt(parentNIDs.get(n).toString()));
							
							if(!listOfNIDs.contains(names.getGermplasmId())){
								listOfNIDs.add(names.getGermplasmId());
								hmOfNIDAndNVal.put(names.getGermplasmId()+"~~!!~~"+"1",names.getNval());
								//hmOfGIDs.put(gidSampleid, gids);
								hmOfGIDs.put(names.getGermplasmId()+"~~!!~~"+"1",names.getGermplasmId()+"");
							}
								
						}
				     //System.out.println("testGetParentsByDatasetId(" + datasetId + ") RESULTS: " + results);
				}catch (MiddlewareQueryException e) {
					//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving NIDs for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
					String strErrMessage = "Error Retrieving NIDs for selected Dataset";
					throw new GDMSException(strErrMessage);
				}
				//}
					
				//System.out.println("hmOfNIDAndNVal=:"+hmOfNIDAndNVal);
				
				
				
				HashMap<Object, Integer> hmGidSampleIdNid=new HashMap<Object, Integer>();
				HashMap<Integer, String> hmNidGermplasmName=new HashMap<Integer, String>();
				ArrayList listOfGNames = new ArrayList<String>();
				ArrayList gDupNameList=new ArrayList<Integer>();
				try{
					localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
					centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
				}catch (Exception e){
					e.printStackTrace();
				}
				//List listOfNIDs = new ArrayList<Integer>();
				List sampleFromLocal=new ArrayList();	
				List sampleFromCentral=new ArrayList();	
				List parentsData=new ArrayList();		
				List allelesList=new ArrayList();
				//List gidsList=new ArrayList();
				Object objAL=null;
				Object objAC=null;
				Iterator itListAC=null;
				Iterator itListAL=null;	
				 gDupNameList=new ArrayList<Integer>();
				 ArrayList gDupGIDsList=new ArrayList<Integer>();
				
				String strQuerry="select distinct gid,nid, acc_sample_id from gdms_acc_metadataset where dataset_id="+ strDatasetID +" order by gid, nid,acc_sample_id asc";	
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
				//Object gid=""; 
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
				
					hmOfNIDAndNVal.put(gidSampleid, gname);
					hmOfGIDs.put(gidSampleid, gids);
					
				}
				
				
			/*try {
				
				nidsList=genoManager.getNidsFromAccMetadatasetByDatasetIds(listOfDatasetID, 0, (int)(genoManager.countNidsFromAccMetadatasetByDatasetIds(listOfDatasetID)));
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving NIDs for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving NIDs for selected Dataset";
				throw new GDMSException(strErrMessage);
			}
			
			try {			
				Name names = null;
				
				for(int n=0;n<nidsList.size();n++){
					
					names=germManager.getGermplasmNameByID(Integer.parseInt(nidsList.get(n).toString()));
					//if(!germNames.contains(names.getNval()+","+names.getGermplasmId()))
					if(!listOfNIDs.contains(names.getGermplasmId())){
						listOfNIDs.add(names.getGermplasmId());
						hmOfNIDAndNVal.put(names.getGermplasmId(),names.getNval());
					}
				}
				
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Names by NIds for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving Names by NIds for selected Dataset";
				throw new GDMSException(strErrMessage);
			}*/
			//System.out.println("**********************  listOfNIDs="+hmOfNIDAndNVal);
				//System.out.println("$$$$$$$$$$$$$$$$$$$$$$   hmOfNIDAndNVal="+hmOfNIDAndNVal);
			sortedMapOfGIDsAndGNames = new TreeMap<Object, String>();
			Set<Object> gidKeySet = hmOfNIDAndNVal.keySet();
			Iterator<Object> gidIterator = gidKeySet.iterator();
			while (gidIterator.hasNext()) {
				Object gid = gidIterator.next();
				String gname = hmOfNIDAndNVal.get(gid).toString();
				//System.out.println("gid=:"+gid+"   GName="+gname);
				sortedMapOfGIDsAndGNames.put(gid, gname);
			}
			//System.out.println("$$$$$$:"+sortedMapOfGIDsAndGNames);
			listOfAllParents = new ArrayList<ParentElement>();
			try {
				List<ParentElement> results =genoManager.getParentsByDatasetId(Integer.parseInt(strDatasetID));
				for (ParentElement parentElement : results){
					parentANid=parentElement.getParentANId();
					parentBNid=parentElement.getParentBGId();
					//parentElement.getMappingType()
					mappingType=parentElement.getMappingType();
				}
				Name namesA = null;
				Name namesB = null;						
				parentsGIDsNames= new HashMap<Integer, String>();
				
				namesA=germManager.getGermplasmNameByID(parentANid);
				parentAGid=namesA.getGermplasmId();
				parentsGIDsNames.put(namesA.getGermplasmId(), namesA.getNval());
				parentsListToWrite=parentsListToWrite+parentAGid+";;"+namesA.getNval()+"!~!";
				parentList.add(parentAGid);
				
				namesB=germManager.getGermplasmNameByID(parentBNid);
				parentBGid=namesB.getGermplasmId();
				parentsGIDsNames.put(namesB.getGermplasmId(), namesB.getNval());
				parentsListToWrite=parentsListToWrite+parentBGid+";;"+namesB.getNval()+"!~!";
				parentList.add(parentBGid);
				
				if(!strSelectedMappingType.isEmpty()){
					selectedOP=true;
					mapA=new TreeMap(); 
					mapB=new TreeMap(); 
					
					for(int p=0;p<parentList.size();p++){
						pgids=pgids+parentList.get(p)+",";
					}
					//System.out.println("pgids before:"+pgids);
					pgids=pgids.substring(0, pgids.length()-1);
					//System.out.println("pgids After:"+pgids);
					for(int m=0;m<listOfAllMIDsForSelectedDatasetID.size();m++){
						mid=mid+listOfAllMIDsForSelectedDatasetID.get(m)+",";
					}						
					mid=mid.substring(0, mid.length()-1);
					
					try{
						localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
						centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
					}catch (Exception e){
						e.printStackTrace();
					}
					
					List allelesFromLocal=new ArrayList();	
					List allelesFromCentral=new ArrayList();	
					parentsData=new ArrayList();		
					allelesList=new ArrayList();
					//List gidsList=new ArrayList();
					objAL=null;
					 objAC=null;
					 itListAC=null;
					itListAL=null;	
					List<AllelicValueElement> mapParentAllelicValues = genoManager.getAllelicValuesByGidsAndMarkerNames(parentList, listofMarkerNamesForSNP);
					//System.out.println("%$%$%$%$%$%$%$  :"+mapParentAllelicValues);
					if((markerType.equalsIgnoreCase("SSR"))||(markerType.equalsIgnoreCase("DArT"))){
						strQuerry="select distinct gid,marker_id, allele_bin_value from gdms_allele_values where gid in("+pgids+") and marker_id in("+mid+") order by gid, marker_id asc";	
						queryL=localSession.createSQLQuery(strQuerry);		
						queryL.addScalar("gid",Hibernate.INTEGER);	 
						queryL.addScalar("marker_id",Hibernate.INTEGER);
						queryL.addScalar("allele_bin_value",Hibernate.STRING);						
						allelesFromLocal=queryL.list();
						
						
						queryC=centralSession.createSQLQuery(strQuerry);
						queryC.addScalar("gid",Hibernate.INTEGER);	 
						queryC.addScalar("marker_id",Hibernate.INTEGER);
						queryC.addScalar("allele_bin_value",Hibernate.STRING);
						allelesFromCentral=queryC.list();
						for(int w=0;w<allelesFromCentral.size();w++){
							Object[] strMareO= (Object[])allelesFromCentral.get(w);
							markerAlleles.put(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
							
							if(!(glist.contains(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1")))
								glist.add(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1");
							parentsData.add(Integer.parseInt(strMareO[0].toString())+","+Integer.parseInt(strMareO[1].toString())+","+strMareO[2].toString());																
						}
						
						for(int w=0;w<allelesFromLocal.size();w++){
							Object[] strMareO= (Object[])allelesFromLocal.get(w);
							markerAlleles.put(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
							
							if(!(glist.contains(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1")))
								glist.add(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1");
							parentsData.add(Integer.parseInt(strMareO[0].toString())+","+Integer.parseInt(strMareO[1].toString())+","+strMareO[2].toString());																	
						}
						
					}else if(markerType.equalsIgnoreCase("SNP")){
						strQuerry="select distinct gid,marker_id, char_value from gdms_char_values where gid in("+pgids+") and marker_id in("+mid+") order by gid, marker_id asc";
						//System.out.println(strQuerry);
						queryL=localSession.createSQLQuery(strQuerry);		
						queryL.addScalar("gid",Hibernate.INTEGER);	 
						queryL.addScalar("marker_id",Hibernate.INTEGER);
						queryL.addScalar("char_value",Hibernate.STRING);				
						
						allelesFromLocal=queryL.list();
						
						
						queryC=centralSession.createSQLQuery(strQuerry);
						queryC.addScalar("gid",Hibernate.INTEGER);	 
						queryC.addScalar("marker_id",Hibernate.INTEGER);
						queryC.addScalar("char_value",Hibernate.STRING);
						allelesFromCentral=queryC.list();
						if(! allelesFromCentral.isEmpty()){
							for(int w=0;w<allelesFromCentral.size();w++){
								Object[] strMareO= (Object[])allelesFromCentral.get(w);
								markerAlleles.put(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
								
								if(!(glist.contains(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1")))
									glist.add(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1");
								parentsData.add(Integer.parseInt(strMareO[0].toString())+","+Integer.parseInt(strMareO[1].toString())+","+strMareO[2].toString());	
								
							}
						}
						if(!allelesFromLocal.isEmpty()){
							for(int w=0;w<allelesFromLocal.size();w++){
								Object[] strMareO= (Object[])allelesFromLocal.get(w);	
								
								markerAlleles.put(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
								
								if(!(glist.contains(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1")))
									glist.add(Integer.parseInt(strMareO[0].toString())+"~~!!~~"+"1");
								
								parentsData.add(Integer.parseInt(strMareO[0].toString())+","+Integer.parseInt(strMareO[1].toString())+","+strMareO[2].toString());																	
							}
						}
					}					
					//System.out.println("parentsData=:"+parentsData);
					
					for(int c=0;c<parentsData.size();c++){
						 String arrP[]=new String[3];
						 StringTokenizer stzP = new StringTokenizer(parentsData.get(c).toString(), ",");
						 int iP=0;
						 while(stzP.hasMoreTokens()){
							 arrP[iP] = stzP.nextToken();
							 iP++;
						 }	
						// System.out.println(arrP);
						 if(Integer.parseInt(arrP[0])==parentAGid)								
							mapA.put(Integer.parseInt(arrP[1]), arrP[2]);
						 else
							mapB.put(Integer.parseInt(arrP[1]), arrP[2]);							
						 
					}
					
								
					if(strSelectedMappingType.equalsIgnoreCase("abh")){							
						for(int m=0; m<markerIDsList.size(); m++){
							//intAlleleValues.add(parentAGid+"!~!"+markerIDsList.get(m)+"!~!"+"A");
							markerAlleles.put(parentAGid+"~~!!~~"+"1"+"!~!"+markerIDsList.get(m), "A");
							
							if(!(glist.contains(parentAGid+"~~!!~~"+"1")))
								glist.add(parentAGid+"~~!!~~"+"1");
						}
						for(int m=0; m<markerIDsList.size(); m++){
							//intAlleleValues.add(parentBGid+"!~!"+markerIDsList.get(m)+"!~!"+"B");
							markerAlleles.put(parentBGid+"~~!!~~"+"1"+"!~!"+markerIDsList.get(m), "B");
							
							if(!(glist.contains(parentBGid+"~~!!~~"+"1")))
								glist.add(parentBGid+"~~!!~~"+"1");
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
						
					}else{						
						//System.out.println("glist,,,,,,,,,,,,:"+glist);
						//System.out.println("markerAlleles...........:"+markerAlleles);
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
						
					}
					//System.out.println("mapEx...:"+mapEx);
				}else{
					
					for(int m=0; m<markerIDsList.size(); m++){
						markerAlleles.put(parentAGid+"~~!!~~"+"1"+"!~!"+markerIDsList.get(m), "A");
					
						if(!(glist.contains(parentAGid+"~~!!~~"+"1")))
							glist.add(parentAGid+"~~!!~~"+"1");
					}
					
					for(int m=0; m<markerIDsList.size(); m++){
						markerAlleles.put(parentBGid+"~~!!~~"+"1"+"!~!"+markerIDsList.get(m), "B");
						
						if(!(glist.contains(parentBGid+"~~!!~~"+"1")))
							glist.add(parentBGid+"~~!!~~"+"1");
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
					
				}
				//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$  :"+intAlleleValues);
				/*System.out.println("mapA=:"+mapA);
				System.out.println("mapB=:"+mapB);
				*/
				try{
					List<ParentElement> listOfAllParents=genoManager.getParentsByDatasetId(Integer.parseInt(strDatasetID));
					
				} catch (MiddlewareQueryException e) {
					e.printStackTrace();
					//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving ParentElements for the selected Dataset", Notification.TYPE_ERROR_MESSAGE);
					String strErrMessage = "Error Retrieving ParentElements for the selected Dataset";
					throw new GDMSException(strErrMessage);
					
				}
				
			} catch (NumberFormatException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving ParentElements for the selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving ParentElements for the selected Dataset";
				throw new GDMSException(strErrMessage);
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving ParentElements for the selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving ParentElements for the selected Dataset";
				throw new GDMSException(strErrMessage);
			}
			
			listOfParentGIDs = new ArrayList<Integer>();
			//System.out.println("mapA=:"+mapA);
			//System.out.println("mapB=:"+mapB);
			for (ParentElement parentElement : listOfAllParents){
				Integer parentAGId = parentElement.getParentANId();
				Integer parentBGId = parentElement.getParentBGId();
				
				if (false == listOfParentGIDs.contains(parentAGId)){
					listOfParentGIDs.add(parentAGId);
				}
				if (false == listOfParentGIDs.contains(parentBGId)){
					listOfParentGIDs.add(parentBGId);
				}
			}
			
			
		}else{
			HashMap<Object, Integer> hmGidSampleIdNid=new HashMap<Object, Integer>();
			HashMap<Integer, String> hmNidGermplasmName=new HashMap<Integer, String>();
			ArrayList listOfGNames = new ArrayList<String>();
			ArrayList gDupNameList=new ArrayList<Integer>();
			try{
				localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
				centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			}catch (Exception e){
				e.printStackTrace();
			}
			List listOfNIDs = new ArrayList<Integer>();
			List sampleFromLocal=new ArrayList();	
			List sampleFromCentral=new ArrayList();	
			List parentsData=new ArrayList();		
			List allelesList=new ArrayList();
			//List gidsList=new ArrayList();
			Object objAL=null;
			Object objAC=null;
			Iterator itListAC=null;
			Iterator itListAL=null;	
			 gDupNameList=new ArrayList<Integer>();
			// System.out.println("...........................SRIKALYANI");
			String strQuerry="select distinct gid,nid, acc_sample_id from gdms_acc_metadataset where dataset_id="+ strDatasetID +" order by gid, nid,acc_sample_id asc";	
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
			hmOfNIDAndNVal = new HashMap<Object, String>();
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
			
				hmOfNIDAndNVal.put(gidSampleid, gname);
				hmOfGIDs.put(gidSampleid, gids);
			}
			
			
			
			/*try {
				
				nidsList=genoManager.getNidsFromAccMetadatasetByDatasetIds(listOfDatasetID, 0, (int)(genoManager.countNidsFromAccMetadatasetByDatasetIds(listOfDatasetID)));
			} catch (MiddlewareQueryException e) {				
				String strErrMessage = "Error Retrieving NIDs for selected Dataset";
				throw new GDMSException(strErrMessage);
			}
			System.out.println("nidsList:"+nidsList);
			try {			
				Name names = null;
				listOfNIDs=new ArrayList<Integer>();
				hmOfNIDAndNVal=new HashMap<Object, String>();
				for(int n=0;n<nidsList.size();n++){
					names=germManager.getGermplasmNameByID(Integer.parseInt(nidsList.get(n).toString()));					
					if(!listOfNIDs.contains(names.getGermplasmId())){
						listOfNIDs.add(names.getGermplasmId());
						hmOfNIDAndNVal.put(names.getGermplasmId(),names.getNval());
					}
				}
				//System.out.println("hmOfNIDAndNVal:"+hmOfNIDAndNVal);
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Names by NIds for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving Names by NIds for selected Dataset";
				throw new GDMSException(strErrMessage);
			}*/
			ArrayList markerNames = new ArrayList();
			HashMap<String, Integer> markerNamesIDsMap=new HashMap<String, Integer>();
			
			markerIDsNamesMap=new HashMap<Integer, String>();
			
			List<Integer> listOfAllMIDsForSelectedDatasetID = new ArrayList<Integer>();
			hmOfMIdAndMarkerName = new HashMap<Integer, String>();
			hmOfMarkerNamerAndMId = new HashMap<String, Integer>();
			
			String markerS="";
			List samplesFromLocal=new ArrayList();	
			List samplesFromCentral=new ArrayList();
			try {
				
				/*ArrayList LocalList=new ArrayList();
				ArrayList centralList=new ArrayList();*/
				listOfAllMarkers = new ArrayList<MarkerIdMarkerNameElement>();
				listOfAllMIDsForSelectedDatasetID=genoManager.getMarkerIdsByDatasetId(Integer.parseInt(strDatasetID));
				for(int mn=0; mn<listOfAllMIDsForSelectedDatasetID.size();mn++){
					markerS=markerS+listOfAllMIDsForSelectedDatasetID.get(mn)+",";
				}
				
				String strQuerryMN="select marker_id, marker_name from gdms_marker where marker_id IN ("+ markerS.substring(0,markerS.length()-1) +") order by marker_id";	
				//System.out.println("strQuerry:"+strQuerryMN);
				queryMNL=localSession.createSQLQuery(strQuerryMN);		
				queryMNL.addScalar("marker_id",Hibernate.INTEGER);	 
				queryMNL.addScalar("marker_name",Hibernate.STRING);
							
				
				samplesFromLocal=queryMNL.list();
				
				
				queryMNC=centralSession.createSQLQuery(strQuerryMN);
				queryMNC.addScalar("marker_id",Hibernate.INTEGER);	 
				queryMNC.addScalar("marker_name",Hibernate.STRING);
				
				samplesFromCentral=queryMNC.list();
				markerIDsList=new ArrayList();
				listofMarkerNamesForSNP=new ArrayList();
				//MarkerIdMarkerNameElement e=new MarkerIdMarkerNameElement(markerId, markerName);
				if(!samplesFromCentral.isEmpty()){
					for(int w=0;w<samplesFromCentral.size();w++){
						Object[] strMareO= (Object[])samplesFromCentral.get(w);
						
						markerIDsList.add(Integer.parseInt(strMareO[0].toString()));
						MarkerIdMarkerNameElement e=new MarkerIdMarkerNameElement(Integer.parseInt(strMareO[0].toString()), strMareO[1].toString());
						/*e.setMarkerId(Integer.parseInt(strMareO[0].toString()));
						e.setMarkerName(strMareO[1].toString());*/
			            listOfAllMarkers.add(e);
			            listofMarkerNamesForSNP.add(strMareO[1].toString());
			            hmOfMIdAndMarkerName.put(Integer.parseInt(strMareO[0].toString()), strMareO[1].toString());
			            hmOfMarkerNamerAndMId.put(strMareO[1].toString(), Integer.parseInt(strMareO[0].toString()));
					}
				}
				if(!samplesFromLocal.isEmpty()){
					for(int w=0;w<samplesFromLocal.size();w++){
						Object[] strMareO= (Object[])samplesFromLocal.get(w);								
						markerIDsList.add(Integer.parseInt(strMareO[0].toString()));
						MarkerIdMarkerNameElement e=new MarkerIdMarkerNameElement(Integer.parseInt(strMareO[0].toString()), strMareO[1].toString());
						/*e.setMarkerId(Integer.parseInt(strMareO[0].toString()));
						e.setMarkerName(strMareO[1].toString());*/
			            listOfAllMarkers.add(e);
			            //listOfAllMarkers.add(e);
			            listofMarkerNamesForSNP.add(strMareO[1].toString());
			            hmOfMIdAndMarkerName.put(Integer.parseInt(strMareO[0].toString()), strMareO[1].toString());
			            hmOfMarkerNamerAndMId.put(strMareO[1].toString(), Integer.parseInt(strMareO[0].toString()));
					}
				}
				//System.out.println(".......... Marker names"+hmOfMarkerNamerAndMId);		
				
				markersCount=listofMarkerNamesForSNP.size();
				
				
				
				
				//genoManager.getMarkerNamesByMarkerIds(listOfAllMIDsForSelectedDatasetID);
				/*List<MarkerIdMarkerNameElement> markerNamesList = genoManager.getMarkerNamesByMarkerIds(listOfAllMIDsForSelectedDatasetID);
		        //////System.out.println("testGetMarkerNamesByMarkerIds(" + listOfAllMIDsForSelectedDatasetID + ") RESULTS: ");
				markersCount=markerNamesList.size();
				markerIDsList=new ArrayList();
				listofMarkerNamesForSNP=new ArrayList();
		        for (MarkerIdMarkerNameElement e : markerNamesList) {
		            //////System.out.println(e.getMarkerId() + " : " + e.getMarkerName()+"    "+markerNames.size());
		        	markerIDsList.add(e.getMarkerId());
		            listOfAllMarkers.add(e);
		            listofMarkerNamesForSNP.add(e.getMarkerName().toString());
		            hmOfMIdAndMarkerName.put(e.getMarkerId(), e.getMarkerName().toString());
		            hmOfMarkerNamerAndMId.put(e.getMarkerName().toString(), e.getMarkerId());
		           
		        }*/
			
			
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Names by NIds for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving Marker Names for selected Dataset";
				throw new GDMSException(strErrMessage);
			}
			
			
		}
		
		
		
		////System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$  strDatasetType:"+strDatasetType);
		
		if ((strDatasetType.equalsIgnoreCase("SSR"))||(strDatasetType.equalsIgnoreCase("DArT"))){
			listOfAllelicValueWithMarkerIdElements = new ArrayList<AllelicValueWithMarkerIdElement>();
			
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
			try {				
				/*List<AllelicValueWithMarkerIdElement> allelicValues = genoManager.getAllelicValuesFromAlleleValuesByDatasetId(Integer.parseInt(strDatasetID), 0, (int)genoManager.countAllelicValuesFromAlleleValuesByDatasetId(Integer.parseInt(strDatasetID)));
				//////System.out.println(allelicValues.size());
				for(AllelicValueWithMarkerIdElement results : allelicValues) {
					listOfAllelicValueWithMarkerIdElements.add(results);
					
		        }	*/			
				
				strQuerry="select distinct gid,marker_id, allele_bin_value,acc_sample_id,marker_sample_id from gdms_allele_values where dataset_id="+strDatasetID+" ORDER BY gid, marker_id,acc_sample_id asc";
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
					if(strMareO[3]!=null){
						markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
							glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);
					}else{
						markerAlleles.put(strMareO[0]+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+"1")))
							glist.add(strMareO[0]+"~~!!~~"+"1");
					}
																					
				}
				
				for(int w=0;w<allelesFromLocal.size();w++){
					Object[] strMareO= (Object[])allelesFromLocal.get(w);	
					
					if(strMareO[3]!=null){
						markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
							glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);
					}else{
						markerAlleles.put(strMareO[0]+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+"1")))
							glist.add(strMareO[0]+"~~!!~~"+"1");
					}
																					
				}
				System.out.println("markerAlleles:"+markerAlleles);
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
				
				
				/*if (null != allelicValues){
					for (AllelicValueWithMarkerIdElement allelicValueElement : allelicValues){
						if(!(midslist.contains(allelicValueElement.getMarkerId())))
							midslist.add(allelicValueElement.getMarkerId());
						
						//data=data+allelicValueElement.getGid()+"~!~"+allelicValueElement.getData()+"~!~"+allelicValueElement.getMarkerName()+"!~!";
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
				}*/
				
				//System.out.println("mapEx:"+mapEx);
				
				
			} catch (Exception e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving AllelicValues for selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving AllelicValues for selected Dataset";
				throw new GDMSException(strErrMessage);
			}		
		} else if (strDatasetType.equalsIgnoreCase("SNP")){			
			listOfAllelicValueWithMarkerIdElements = new ArrayList<AllelicValueWithMarkerIdElement>();
			try {
				
				/*List<AllelicValueWithMarkerIdElement> charValues = genoManager.getAllelicValuesFromCharValuesByDatasetId(Integer.parseInt(strDatasetID), 0, (int)genoManager.countAllelicValuesFromCharValuesByDatasetId(Integer.parseInt(strDatasetID)));
				
				for(AllelicValueWithMarkerIdElement results : charValues) {
					listOfAllelicValueWithMarkerIdElements.add(results);
		        }*/
				
				
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
				
				
				long time = new Date().getTime();
				
				
				markerAlleles= new HashMap<String,Object>();
				strQuerry="select distinct gid,marker_id, char_value,acc_sample_id,marker_sample_id from gdms_char_values where dataset_id="+strDatasetID+" ORDER BY gid, marker_id,acc_sample_id asc";	
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
					
					
					if(strMareO[3]!=null){
						markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
							glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);
					}else{
						markerAlleles.put(strMareO[0]+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+"1")))
							glist.add(strMareO[0]+"~~!!~~"+"1");
					}
					
				/*	
					markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
					
					if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
						glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);*/
																					
				}
				
				for(int w=0;w<allelesFromLocal.size();w++){
					Object[] strMareO= (Object[])allelesFromLocal.get(w);	
					
					/*markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
					
					if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
						glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);*/
					if(strMareO[3]!=null){
						markerAlleles.put(strMareO[0]+"~~!!~~"+strMareO[3]+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+strMareO[3])))
							glist.add(strMareO[0]+"~~!!~~"+strMareO[3]);
					}else{
						markerAlleles.put(strMareO[0]+"~~!!~~"+"1"+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+"1")))
							glist.add(strMareO[0]+"~~!!~~"+"1");
					}
																					
				}
				/*System.out.println("markerAlleles:"+markerAlleles);
				System.out.println(",,,,,,,,glist:"+glist);
				*/
				/*List markerKey = new ArrayList();
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
				*/
				
				//System.out.println("listOfAllelicValueWithMarkerIdElements=:"+listOfAllelicValueWithMarkerIdElements);
			//} catch (MiddlewareQueryException e) {
			} catch (Exception e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving AllelicValueWithMarkerIdElements for the selected Dataset", Notification.TYPE_ERROR_MESSAGE);
				String strErrMessage = "Error Retrieving AllelicValueWithMarkerIdElements for the selected Dataset";
				throw new GDMSException(strErrMessage);
			}
		} else if (strDatasetType.equalsIgnoreCase("mapping")){
			//System.out.println("mapA:"+mapA);
			//System.out.println("mapB:"+mapB);
			listOfAllelicValueWithMarkerIdElements = new ArrayList<AllelicValueWithMarkerIdElement>();
			//System.out.println("&&&&&&&&&&&&&&&&&&&&&&&  MAPPING &&&&&&&&&&&&&&&&&&&&&&&&");
			
			if(! strSelectedMappingType.isEmpty()){
				if(strSelectedMappingType.equalsIgnoreCase("abh")){	
					try {				
						
						
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
						String sampleAccID="";
						markerAlleles= new HashMap<String,Object>();
						strQuerry="select distinct gid,marker_id, map_char_value,acc_sample_id,marker_sample_id from gdms_mapping_pop_values where dataset_id="+strDatasetID+" ORDER BY gid, marker_id,acc_sample_id asc";
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
							if(strMareO[3]==null)
								sampleAccID="1";
							else
								sampleAccID=strMareO[3].toString();
							
							String strData=strMareO[2].toString();
							if((strData.equals("-"))||(strData.equals("?"))||(strData.equals("null"))||(strData.equals("NR"))){							
								markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
							}else{
								if(mapA.get(Integer.parseInt(strMareO[1].toString())).equals(strData)){	
									//System.out.println("PopAllelevalue is matching with Parent A");
									markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()), "A");
									//intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+"A");									
								}else if(mapB.get(Integer.parseInt(strMareO[1].toString())).equals(strData)){	
									//System.out.println("PopAllelevalue is matching with Parent B");
									markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()),"B");										
								}else{	
									//System.out.println("Not matching with both the parents");
									markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()),"A/B");
								}
							}
							
							if(!(glist.contains(strMareO[0]+"~~!!~~"+sampleAccID)))
								glist.add(strMareO[0]+"~~!!~~"+sampleAccID);
																							
						}
						
						for(int w=0;w<allelesFromLocal.size();w++){
							Object[] strMareO= (Object[])allelesFromLocal.get(w);	
							if(strMareO[3]==null)
								sampleAccID="1";
							else
								sampleAccID=strMareO[3].toString();
							String strData=strMareO[2].toString();
							if((strData.equals("-"))||(strData.equals("?"))||(strData.equals("null"))||(strData.equals("NR"))){							
								markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
							}else{
								if(mapA.get(Integer.parseInt(strMareO[1].toString())).equals(strData)){	
									//System.out.println("PopAllelevalue is matching with Parent A");
									markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()), "A");
									//intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+"A");									
								}else if(mapB.get(Integer.parseInt(strMareO[1].toString())).equals(strData)){	
									//System.out.println("PopAllelevalue is matching with Parent B");
									markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()),"B");										
								}else{	
									//System.out.println("Not matching with both the parents");
									markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()),"A/B");
								}
							}
							
							if(!(glist.contains(strMareO[0]+"~~!!~~"+sampleAccID)))
								glist.add(strMareO[0]+"~~!!~~"+sampleAccID);
																							
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
						
						
						
						
						
						/*List<AllelicValueWithMarkerIdElement> mapPopAlleleValues = genoManager.getAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID), 0, (int)genoManager.countAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID)));
						//System.out.println("@#@#@#@#@#@#@#@#@#@#@#@#@#@:"+mapPopAlleleValues);
						for(AllelicValueWithMarkerIdElement results : mapPopAlleleValues) {					
							if((results.getData().equals("-"))||(results.getData().equals("?"))||(results.getData().equals("null"))||(results.getData().equals("NR"))){
								//System.out.println(" - ");
								intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+results.getData());
							}else{
								//System.out.println(results.getMarkerId()+"   "+mapA.get(results.getMarkerId())+"   "+results.getData().toString());
								if(mapA.get(results.getMarkerId()).equals(results.getData().toString())){	
									//System.out.println("PopAllelevalue is matching with Parent A");
									intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+"A");									
								}else if(mapB.get(results.getMarkerId()).equals(results.getData().toString())){	
									//System.out.println("PopAllelevalue is matching with Parent B");
									intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+"B");										
								}else{	
									//System.out.println("Not matching with both the parents");
									intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+"A/B");
								}
								
							}
							
				        }*/
					
					} catch (Exception e) {
						e.printStackTrace();
						String strErrMessage = "Error Retrieving Mapping AllelicValueElements for the selected Dataset";
						throw new GDMSException(strErrMessage);
					}
					
					
					
					
				}else{
					try {				
						/*List<AllelicValueWithMarkerIdElement> mapPopAlleleValues  = genoManager.getAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID), 0, (int)genoManager.countAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID)));
						for(AllelicValueWithMarkerIdElement results : mapPopAlleleValues) {							
							intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+results.getData());
				        }*/
					
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
						
						String sampleAccID="";
						markerAlleles= new HashMap<String,Object>();
						strQuerry="select distinct gid,marker_id, map_char_value,acc_sample_id,marker_sample_id from gdms_mapping_pop_values where dataset_id="+strDatasetID+" ORDER BY gid, marker_id,acc_sample_id asc";
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
							if(strMareO[3]==null)
								sampleAccID="1";
							else
								sampleAccID=strMareO[3].toString();
							markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
							
							if(!(glist.contains(strMareO[0]+"~~!!~~"+sampleAccID)))
								glist.add(strMareO[0]+"~~!!~~"+sampleAccID);
																							
						}
						
						for(int w=0;w<allelesFromLocal.size();w++){
							Object[] strMareO= (Object[])allelesFromLocal.get(w);	
							if(strMareO[3]==null)
								sampleAccID="1";
							else
								sampleAccID=strMareO[3].toString();
							markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
							
							if(!(glist.contains(strMareO[0]+"~~!!~~"+sampleAccID)))
								glist.add(strMareO[0]+"~~!!~~"+sampleAccID);
																							
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
					} catch (Exception e) {
						String strErrMessage = "Error Retrieving Mapping AllelicValueElements for the selected Dataset";
						throw new GDMSException(strErrMessage);
					}
				}
				
				//System.out.println("#@#@#@#@#  :"+intAlleleValues);
				
			}else{	
				
				
				
				
				try {				
					/*List<AllelicValueWithMarkerIdElement> mapPopAlleleValues = genoManager.getAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID), 0, (int)genoManager.countAllelicValuesFromMappingPopValuesByDatasetId(Integer.parseInt(strDatasetID)));
					for(AllelicValueWithMarkerIdElement results : mapPopAlleleValues) {
						intAlleleValues.add(results.getGid()+"!~!"+results.getMarkerId()+"!~!"+results.getData());
			        }	
					
					*/
					String sampleAccID="";
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
					strQuerry="select distinct gid,marker_id, map_char_value,acc_sample_id,marker_sample_id from gdms_mapping_pop_values where dataset_id="+strDatasetID+" ORDER BY gid, marker_id,acc_sample_id asc";
					//System.out.println(strQuerry);
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
						
						if(strMareO[3]==null)
							sampleAccID="1";
						else
							sampleAccID=strMareO[3].toString();						
						
						markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+sampleAccID)))
							glist.add(strMareO[0]+"~~!!~~"+sampleAccID);
																						
					}
					
					for(int w=0;w<allelesFromLocal.size();w++){
						Object[] strMareO= (Object[])allelesFromLocal.get(w);	
						if(strMareO[3]==null)
							sampleAccID="1";
						else
							sampleAccID=strMareO[3].toString();	
						markerAlleles.put(strMareO[0]+"~~!!~~"+sampleAccID+"!~!"+Integer.parseInt(strMareO[1].toString()), strMareO[2].toString());
						
						if(!(glist.contains(strMareO[0]+"~~!!~~"+sampleAccID)))
							glist.add(strMareO[0]+"~~!!~~"+sampleAccID);
																						
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
					
					
					
					
				} catch (Exception e) {
					//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Mapping AllelicValueElements for the selected Dataset", Notification.TYPE_ERROR_MESSAGE);
					String strErrMessage = "Error Retrieving Mapping AllelicValueElements for the selected Dataset";
					throw new GDMSException(strErrMessage);
				}
			}
		}
		
		//System.out.println(".....................:"+listOfAllelicValueWithMarkerIdElements);
		
		
	}

	private Component buildDatasetResultComponent() {
		VerticalLayout resultsLayout = new VerticalLayout();
		resultsLayout.setCaption("Results");
		resultsLayout.setSpacing(true);
		resultsLayout.setMargin(true, true, true, true);

		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		//////System.out.println("buildDatasetResultComponent(): " + absoluteFile);
		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("WEB-INF")) {
				fileExport = file;
				break;
			}
		}
		final String strAbsolutePath = fileExport.getAbsolutePath();
		//////System.out.println(">>>>>" + strAbsolutePath);
	
		
		final String strFJVisualizeLink = strAbsolutePath + "\\" + "flapjackrun.bat";
		//System.out.println("strFJVisualizeLink=:"+strFJVisualizeLink);
		////System.out.println(_mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory());
		realPath=_mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory().toString();
		
		HorizontalLayout horLayoutForExportTypes = new HorizontalLayout();
		horLayoutForExportTypes.setSpacing(true);
		
		Button btnVisualizeFJ = new Button("Visualize in Flapjack");
		//btnVisualizeFJ.setStyleName(Reindeer.BUTTON_LINK);
		btnVisualizeFJ.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				//////System.out.println("Trying to execute the flapjackrun.bat file.");
				File fexists=new File(realPath+"/Flapjack/Flapjack.flapjack");
				if(fexists.exists()) { fexists.delete(); 
				//////System.out.println("proj exists and deleted");
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
		
		Link gtpExcelFileLink = new Link("Download Genotypic Matrix file", new ExternalResource(""));
		if (null != listOfmatrixTextFileDataSSRDataset){
			if(null != listOfmatrixTextFileDataSSRDataset && 0 < listOfmatrixTextFileDataSSRDataset.size()) {
				if(1 <= listOfmatrixTextFileDataSSRDataset.size()) {
					gtpExcelFileLink = new Link("Download Genotypic Matrix file", new FileDownloadResource(
							listOfmatrixTextFileDataSSRDataset.get(0), _mainHomePage.getMainWindow().getWindow().getApplication()));
				}
			} 
		} else if (null != matrixFileForDatasetRetrieval){
			gtpExcelFileLink = new Link("Download Genotypic Matrix file", new FileDownloadResource(
					matrixFileForDatasetRetrieval, _mainHomePage.getMainWindow().getWindow().getApplication()));
		}
		gtpExcelFileLink.setTargetName("_blank");
		
		
		
		HorizontalLayout layoutForExportTypes = new HorizontalLayout();
		layoutForExportTypes.setSpacing(true);

		ThemeResource themeResource = new ThemeResource("images/excel.gif");
		Button excelButton = new Button();
		excelButton.setIcon(themeResource);
		excelButton.setStyleName(Reindeer.BUTTON_LINK);
		excelButton.setDescription("EXCEL Format");
		excelButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				if(null != listOfmatrixTextFileDataSSRDataset && 0 < listOfmatrixTextFileDataSSRDataset.size()) {
					if(1 <= listOfmatrixTextFileDataSSRDataset.size()) {
						FileResource fileResource = new FileResource(listOfmatrixTextFileDataSSRDataset.get(0), _mainHomePage);
						_mainHomePage.getMainWindow().getWindow().open(fileResource, "_self");
					}
				}
			}
		});
		if (null == strSelectedFormat) {
			layoutForExportTypes.addComponent(excelButton);
		}
		final String strSMVisualizeLink = strAbsolutePath + "\\" + "flapjackMatrix.bat";
		Button similarityMatrixButton = new Button("Show Similarity Matrix");
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
					e.printStackTrace();
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
		
		//if(strQTLExists.equalsIgnoreCase("yes")){
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
				
				//resultsLayout.addComponent(btnVisualizeFJ);
				resultsLayout.addComponent(horLayoutForExportTypes);
				
				resultsLayout.addComponent(flapjackFilesLayout);
				
			} else if (strSelectedFormat.equals("Matrix")) {
				//if (null != matrixFileForDatasetRetrieval && true == matrixFileForDatasetRetrieval.toString().endsWith(".xls")){
				if (null != matrixFileForDatasetRetrieval && (true == matrixFileForDatasetRetrieval.toString().endsWith(".xls")||true == matrixFileForDatasetRetrieval.toString().endsWith(".txt"))){
					resultsLayout.addComponent(gtpExcelFileLink);
				} else {
					layoutForExportTypes.addComponent(excelButton, 0);
					resultsLayout.addComponent(layoutForExportTypes);
					resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
				}
			}
		} else {
			excelButton.setEnabled(false);
			//pdfButton.setEnabled(false);
			//printButton.setEnabled(false);
			resultsLayout.addComponent(layoutForExportTypes);
			resultsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
		}
		return resultsLayout;
	}


	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}


	private ArrayList<AllelicValueWithMarkerIdElement> listOfAllelicValueWithMarkerIdElements;
	private HashMap<Object, String> hmOfNIDAndNVal;
	private ArrayList<Integer> listOfNIDs;
	List<Integer> nidsList=null;
	private ArrayList<MarkerIdMarkerNameElement> listOfAllMarkers;
	private HashMap<Integer, String> hmOfMIdAndMarkerName;
	private HashMap<String, Integer> hmOfMarkerNamerAndMId;
	private ArrayList<AllelicValueElement> listOfAllelicValueElements;
	private ArrayList<String> listofMarkerNamesForSNP;
	private ArrayList<ParentElement> listOfAllParents;

	private CheckBox chbMatrix;

	private CheckBox chbFlapjack;

	private ComboBox selectMap;
	
	private List<Dataset> listOfAllDatasets;
	
	private String strMappingType;
	
}
