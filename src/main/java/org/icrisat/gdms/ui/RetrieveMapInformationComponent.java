package org.icrisat.gdms.ui;



import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.write.WriteException;

import org.generationcp.middleware.dao.gdms.QtlDetailsDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.gdms.MapDetailElement;
import org.generationcp.middleware.pojos.gdms.MapInfo;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.generationcp.middleware.pojos.gdms.QtlDetails;
//import org.generationcp.middleware.pojos.gdms.QtlDetailsPK;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.retrieve.RetrieveMap;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;
//import org.generationcp.middleware.dao.TraitDAO;
//import org.generationcp.middleware.pojos.Trait;

public class RetrieveMapInformationComponent  implements Component.Listener {
	
	private static final long serialVersionUID = 1L;
	private TabSheet _tabsheetForMap;
	private Component buildMapResultsComponent;
	private GDMSMain _mainHomePage;
	protected List<MappingData> listOfAllMappingData;
	
	private Session localSession;
	private Session centralSession;
	
	private ArrayList<String> traitsList;
	
	protected List<MapInfo> listOfAllMapData;
	//private CheckBox[] arrayOfCheckBoxes;
	private Component buildMapComponent;
	private Hashtable<String, QtlDetails> htQTLDetails;
	private CheckBox[] arrayOfCheckBoxesForMap;
	private Table _mapTable;
	private Table _tableForMarkerResults;
	private Table tableWithAllMaps;
	ManagerFactory factory=null;
	OntologyDataManager om;
	GenotypicDataManager genoManager;
	//StudyDataManager    manager;
	List<ArrayList<String>> mapFinalList= new ArrayList<ArrayList<String>>();
	static Map<String, ArrayList<Integer>> hashMap = new HashMap<String,  ArrayList<Integer>>(); 
	
	final Button cmtvButton = new Button();
	final Button mapButton = new Button();
	
	List<String> listOfSelectedTraits = new ArrayList<String>();
	
	ArrayList snpMarkerList =new ArrayList();
	
	ArrayList selMarkerList =new ArrayList();
	
	
	String strMapName ="";
	
	String mapName="";
	
	protected File orderFormForPlymorphicMarkers;
	int mapId=0;
	String strSelectedMap="";
	
	private double dLatestPositionChecked;
	private int iNextChromosomeIndex;
	
	private CheckBox checkBox;
	private ListSelect selectTrait;
	private TextField txtBinSize;
	
	public RetrieveMapInformationComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			om=factory.getOntologyDataManager();
			genoManager=factory.getGenotypicDataManager();
			//manager = factory.getNewStudyDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * Building the entire Tabbed Component required for MAP data
	 * 
	 */
	public HorizontalLayout buildTabbedComponentForMap() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();

		_tabsheetForMap = new TabSheet();
		_tabsheetForMap.setWidth("700px");

		Component buildMapSearchComponent = buildMapSearchComponent();

		buildMapResultsComponent = buildMapResultsComponent();

		//buildMapComponent = buildMapComponent();
		
		
		buildMapSearchComponent.setSizeFull();
		buildMapResultsComponent.setSizeFull();
		//buildMapComponent.setSizeFull();
		
		_tabsheetForMap.addComponent(buildMapSearchComponent);
		_tabsheetForMap.addComponent(buildMapResultsComponent);
		//_tabsheetForMap.addComponent(buildMapComponent);
		
		
		_tabsheetForMap.getTab(1).setEnabled(false);
		//_tabsheetForMap.getTab(2).setEnabled(false);

		horizontalLayout.addComponent(_tabsheetForMap);
		
		return horizontalLayout;
	}


	private Component buildMapSearchComponent() {
		VerticalLayout searchMAPsLayout = new VerticalLayout();
		searchMAPsLayout.setCaption("Search");
		searchMAPsLayout.setMargin(true, true, true, true);

		Label lblSearch = new Label("Search MAPs");
		lblSearch.setStyleName(Reindeer.LABEL_H2);
		searchMAPsLayout.addComponent(lblSearch);
		searchMAPsLayout.setComponentAlignment(lblSearch, Alignment.TOP_CENTER);
		
		Label lblMAPNames = new Label("Map Names");
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

			public void buttonClick(ClickEvent event) {
				String strMapName = txtFieldSearch.getValue().toString().trim();
				
				if (strMapName.equals("")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a search string.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (false == strMapName.endsWith("*")){
					if (false == strMapName.equals(""))
					strMapName = strMapName + "*";
				}
				if(strMapName.equals("*")) {
					//buildMarkerOnLoad(gridLayout);
					buildMarkerOnLoad(horizontalLayout);
					txtFieldSearch.setValue("");
				} else {
					if(strMapName.endsWith("*")) {
						//buildMarkerOnLoad(gridLayout, strMapName);
						buildMarkerOnLoad(horizontalLayout, strMapName);
						txtFieldSearch.setValue("");
//						String strSearchString = strMapName.substring(0, strMapName.length() - 1);
//						for (int i = 0; i < arrayOfCheckBoxes.length; i++) {
//							if(arrayOfCheckBoxes[i].getCaption().startsWith(strSearchString)) {
//								arrayOfCheckBoxes[i].setValue(true);
//								txtFieldSearch.setValue("");
//							} else {
//								arrayOfCheckBoxes[i].setValue(false);
//							}
//						}
					}
				}
				
				if (null == tableWithAllMaps){
					_mainHomePage.getMainWindow().getWindow().showNotification("There are no Maps to be displayed.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				
				if (null != tableWithAllMaps && 0 == tableWithAllMaps.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("There are no Maps to be displayed.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
			

		});
		
		HorizontalLayout layoutForTextSearch = new HorizontalLayout();
		layoutForTextSearch.setSpacing(true);
		layoutForTextSearch.addComponent(lblMAPNames);
		layoutForTextSearch.addComponent(txtFieldSearch);
		layoutForTextSearch.addComponent(searchButton);
		searchMAPsLayout.addComponent(layoutForTextSearch);
		searchMAPsLayout.setMargin(true, true, true, true);
		
		//searchMAPsLayout.addComponent(gridLayout);
		searchMAPsLayout.addComponent(horizontalLayout);
		
			
			HorizontalLayout layoutForExportTypes = new HorizontalLayout();
			layoutForExportTypes.setSpacing(true);
			
			//final Button mapButton = new Button();
			mapButton.setCaption("View Map");
			mapButton.setDescription("Map Format");
			mapButton.setEnabled(false);
			mapButton.addListener(new Button.ClickListener() {
				
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					
					/*Component newMapResultsPanel = buildMapComponent();
					_tabsheetForMap.replaceComponent(buildMapComponent, newMapResultsPanel);
					_tabsheetForMap.requestRepaint();
					buildMapComponent = newMapResultsPanel;
					_tabsheetForMap.getTab(2).setEnabled(true);
					_tabsheetForMap.setSelectedTab(buildMapComponent);*/
					strMapName = txtFieldSearch.getValue().toString();
					List<String> listOfNames = new ArrayList<String>();
									
					if (null == tableWithAllMaps){
						return;
					}
					int size = tableWithAllMaps.size();
					
					for (int i = 0; i < size; i++) {
						Item item = tableWithAllMaps.getItem(new Integer(i));
						Property itemProperty = item.getItemProperty("Select");
						CheckBox checkBox = (CheckBox) itemProperty.getValue();
						if (checkBox.booleanValue() == true) {
							strSelectedMap = item.getItemProperty("Map Name").toString();
							listOfNames.add(strSelectedMap);
						}
					}
					
					if (0 == listOfNames.size()){
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Maps to be displayed in the Result tab.", Notification.TYPE_HUMANIZED_MESSAGE);
						return;
					}
							
					if(null != strMapName && 0 != strMapName.trim().length() && 0 == listOfNames.size()) {
						listOfNames.add(strMapName);
						}
					
					listOfAllMapData = new ArrayList<MapInfo>();
					
					for (String string : listOfNames) {
						retrieveMapDetails(string);
					}
					if(0 == listOfAllMapData.size()) {
						return;
					}
					
					//viewMapUpdates();
					
					Component newMapResultsPanel = buildMapResultsComponent();
					_tabsheetForMap.replaceComponent(buildMapResultsComponent, newMapResultsPanel);
					_tabsheetForMap.requestRepaint();
					buildMapResultsComponent = newMapResultsPanel;
					_tabsheetForMap.getTab(1).setEnabled(true);
					_tabsheetForMap.setSelectedTab(1);
					
					/*SortingMaps sortingMaps = new SortingMaps();
					listOfAllMappingData = sortingMaps.sort(listOfAllMappingData);*/
				}

			});
			layoutForExportTypes.addComponent(mapButton);
			
			//final Button cmtvButton = new Button();
			cmtvButton.setCaption("CMTV");
			cmtvButton.setDescription("CMTV Format");
			cmtvButton.setEnabled(false);
			cmtvButton.addListener(new Button.ClickListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					strMapName = txtFieldSearch.getValue().toString();
					List<String> listOfNames = new ArrayList<String>();
									
					if (null == tableWithAllMaps){
						return;
					}
					int size = tableWithAllMaps.size();
					
					for (int i = 0; i < size; i++) {
						Item item = tableWithAllMaps.getItem(new Integer(i));
						Property itemProperty = item.getItemProperty("Select");
						CheckBox checkBox = (CheckBox) itemProperty.getValue();
						if (checkBox.booleanValue() == true) {
							strSelectedMap = item.getItemProperty("Map Name").toString();
							listOfNames.add(strSelectedMap);
						}
					}
					
					if (0 == listOfNames.size()){
						_mainHomePage.getMainWindow().getWindow().showNotification("Please select the list of Maps to be displayed in the Result tab.", Notification.TYPE_HUMANIZED_MESSAGE);
						return;
					}
							
					if(null != strMapName && 0 != strMapName.trim().length() && 0 == listOfNames.size()) {
						listOfNames.add(strMapName);
						}
					
					listOfAllMapData = new ArrayList<MapInfo>();
					
					for (String string : listOfNames) {
						retrieveMapDetails(string);
					}
					if(0 == listOfAllMapData.size()) {
						return;
					}
					if (null != listOfAllMapData){
						ExportFileFormats exportFileFormats = new ExportFileFormats();
						try {
							
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

							long time = new Date().getTime();
							strAbsolutePath = strAbsolutePath + "\\" + "CMTV" + time + ".txt";
							
							ArrayList<String[]> sortMapListToBeDisplayed = sortMappingDataListToBeDisplayed(listOfAllMapData);
							
							//System.out.println(sortMapListToBeDisplayed.size());
							
							exportFileFormats.CMTVTxt(sortMapListToBeDisplayed, strAbsolutePath, _mainHomePage);

							
						} catch (GDMSException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification("Error displaying Map Details in a text file", Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					} else {	
						_mainHomePage.getMainWindow().getWindow().showNotification("No Map Details to be diplayed in the CMTV text file", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				}
			});
			layoutForExportTypes.addComponent(cmtvButton);
			
			searchMAPsLayout.addComponent(layoutForExportTypes);
			searchMAPsLayout.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_CENTER);		
		
		return searchMAPsLayout;
	}
	
	protected void buildMarkerOnLoad(HorizontalLayout horizontalLayout) {
		buildMarkerOnLoad(horizontalLayout, "*");
	}

	protected void buildMarkerOnLoad(HorizontalLayout horizontalLayout, String theSearchString) {
		horizontalLayout.removeAllComponents();
		RetrieveMap retrieveMarker = new RetrieveMap();
		List<MapDetailElement> retrieveMaps = new ArrayList<MapDetailElement>();
		try {
			retrieveMaps = retrieveMarker.retrieveMaps();
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
			return;
		}
		
		List<MappingData> retrieveMappingData = new ArrayList<MappingData>();
		try {
			retrieveMappingData = retrieveMarker.retrieveMappingData();
		} catch (MiddlewareQueryException e) {
			e.printStackTrace();
		}
		
		final List<MapDetailElement> finalretrieveMarker2 = retrieveMaps;
		if(null != finalretrieveMarker2) {
			int iCounter = 0;
			
			
			ArrayList<String> strDataM=new ArrayList<String>();
			for (MapDetailElement mapsDetails : finalretrieveMarker2) {
				String strMapUnit = mapsDetails.getMapType();
				
				strDataM.add(mapsDetails.getMarkerCount()+"!~!"+mapsDetails.getMaxStartPosition()+"!~!"+mapsDetails.getLinkageGroup()+"!~!"+mapsDetails.getMapName()+"!~!"+strMapUnit);
			}
			if(0 == strDataM.size()) {
				return;
			}
			String[] strArr=strDataM.get(0).toString().split("!~!");
			
			String chr=strArr[3];
			int mCount=Integer.parseInt(strArr[0]);
			float distance=Float.parseFloat(strArr[1]);
			int mc=0;
			float d=0;
			mapFinalList= new ArrayList<ArrayList<String>>();
			String mType="";
			//System.out.println("strDataM.size()=:"+strDataM.size());
			for(int a=0;a<strDataM.size();a++){	
				String mapType="";
				String[] str1=strDataM.get(a).toString().split("!~!");		
				//System.out.println(" a="+a+" ,,,,markerCount="+str1[0]+"    ;startPosition="+str1[1]+"  ;LinkageGroup="+str1[2]+"  ;MapName="+str1[3]);
				if(str1[3].equals(chr)){
					mc=mc+Integer.parseInt(str1[0]);
					d=d+Float.parseFloat(str1[1]);	
					mType=str1[4];
					//System.out.println("..mc="+mc+"   d:"+d);
					if(a==(strDataM.size()-1)){
						//System.out.println("IF in IF "+mapType);
						mCount=mc;
						distance=d;
						mapType=mType;
						ArrayList<String> listOfData = new ArrayList<String>();
						listOfData.add(String.valueOf(mCount));
						listOfData.add(chr);
						listOfData.add(String.valueOf(distance));
						listOfData.add(mapType);
						mapFinalList.add(listOfData);								
					}
				}else if(!(str1[3].equals(chr))){							
					mCount=mc;
					distance=d;
					mapType=mType;
					//System.out.println("else IF in IF "+mapType+ ".... "+chr);
					//mapFinalList.add(mCount+"!~!"+chr+"!~!"+distance+"!~!"+mapType+";;");
					ArrayList<String> listOfData = new ArrayList<String>();
					listOfData.add(String.valueOf(mCount));
					listOfData.add(chr);
					listOfData.add(String.valueOf(distance));
					listOfData.add(mapType);
					mapFinalList.add(listOfData);								
					mc=0;
					d=0;
					mType="";
					chr=str1[3];
					a=a-1;
				}						
			}

			List<ArrayList<String>> tempList = new ArrayList<ArrayList<String>>();
			
			for (ArrayList<String> arrayList : mapFinalList){
				tempList.add(arrayList);
			}
				
			
			if(null != theSearchString && false == theSearchString.equals("*")) {
				//int iCount = 0;
				theSearchString = theSearchString.substring(0, theSearchString.length() - 1);
				for (ArrayList<String> arrayList : mapFinalList) {
					String strChr = arrayList.get(1);
					//20130619
					if(false == strChr.startsWith(theSearchString)) {
						//mapFinalList.remove(iCount);
						tempList.remove(arrayList);
					}
				}
				
				for (int i = 0; i < tempList.size(); i++){
					ArrayList<String> arrayList = tempList.get(i);
					String strChr = arrayList.get(1);
					if(false == strChr.startsWith(theSearchString)) {
						mapFinalList.remove(arrayList);
					}
				}
			}
			
			if (0 < mapFinalList.size()) {
				
				tableWithAllMaps = new Table();
				tableWithAllMaps.setSizeFull();
				tableWithAllMaps.setPageLength(5);
				tableWithAllMaps.setSelectable(false);
				tableWithAllMaps.setColumnCollapsingAllowed(false);
				tableWithAllMaps.setColumnReorderingAllowed(false);
				tableWithAllMaps.setEditable(false);
				tableWithAllMaps.setStyleName("strong");
				horizontalLayout.addComponent(tableWithAllMaps);
				
				tableWithAllMaps.addContainerProperty("Select", CheckBox.class, null);
				tableWithAllMaps.addContainerProperty("Map Name", String.class, null);
				tableWithAllMaps.addContainerProperty("Num Of Markers", String.class, null);
				tableWithAllMaps.addContainerProperty("Map Length", String.class, null);
				tableWithAllMaps.addContainerProperty("Map Type", String.class, null);
				
				iCounter = 0;
				int i = 0;
				for (ArrayList<String> listOfData : mapFinalList) {
					String strCount = listOfData.get(0);
					String strChr = listOfData.get(1);
					String strDistance = listOfData.get(2);
					
					//20130829: Rounding off the Distance value to one decimal point
					float parseFloat = Float.parseFloat(strDistance);
					//System.out.println("Distance: " + parseFloat);
				    double roundOff = Math.round(parseFloat*10.0)/10.0;
				    //System.out.println("Rounded Value: " + roundOff);
				    strDistance = String.valueOf(roundOff);
				    
					String strMapType = listOfData.get(3);
					CheckBox checkBox = new CheckBox();					
					
					tableWithAllMaps.addItem(new Object[] {checkBox, strChr, strCount, strDistance, strMapType}, new Integer(i));
					iCounter++;
					i++;
				}
				tableWithAllMaps.requestRepaint();
				
				if (tableWithAllMaps != null && 0 != tableWithAllMaps.size()) {
				mapButton.setEnabled(true);
				cmtvButton.setEnabled(true);
			
			}
				
				
			} else {
				_mainHomePage.getMainWindow().getWindow().showNotification("No Map/Maps exist with the given search string.", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
			
		}
	}

	private void retrieveMapDetails(String strMapName) {
		
		//System.out.println("^^^^^^^^^^^^^^^   :"+strMapName);
		if (null != strMapName && (false == strMapName.equals(""))){
			RetrieveMap retrieveMap = new RetrieveMap();
			try {
				listOfAllMapData=new ArrayList<MapInfo>();
				
				mapId=genoManager.getMapIdByName(strMapName);
				mapName=strMapName;
				
				List<MapInfo> results =genoManager.getMapInfoByMapName(strMapName);				
				 for (MapInfo mapInfo : results){
					 //System.out.println(mapInfo);
					 listOfAllMapData.add(mapInfo);
				 }
				 
				 
				// strMapName=strMapName;
				//System.out.println("listOfAllMapData:"+listOfAllMapData);
				
				
				//System.out.println("listOfAllMappingData=:"+listOfAllMappingData);
			} catch (MiddlewareQueryException e) {
				_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving MappingData list.",  Notification.TYPE_ERROR_MESSAGE);
				return;
			}
		}
	}

	

	private Component buildMapResultsComponent() {		

		VerticalLayout resultsLayoutForPolymorphicMaps = new VerticalLayout();
		resultsLayoutForPolymorphicMaps.setCaption("Results");
		resultsLayoutForPolymorphicMaps.setSpacing(true);
		resultsLayoutForPolymorphicMaps.setMargin(true, true, true, true);
		
		viewMapUpdates();
		
		_tableForMarkerResults = new Table();
		_tableForMarkerResults.setWidth("100%");
		_tableForMarkerResults.setPageLength(10);
		_tableForMarkerResults.setSelectable(true);
		_tableForMarkerResults.setColumnCollapsingAllowed(false);
		_tableForMarkerResults.setColumnReorderingAllowed(true);
		_tableForMarkerResults.setStyleName("strong");
		//_tableForMarkerResults.setStyleName(Reindeer.LAYOUT_WHITE);

		if(htQTLDetails!=null && htQTLDetails.size()>0){			
			_tableForMarkerResults.addContainerProperty("Select", CheckBox.class, null);
			_tableForMarkerResults.addContainerProperty("Marker", String.class, null);
			_tableForMarkerResults.addContainerProperty("Map", String.class, null);
			_tableForMarkerResults.addContainerProperty("Chromosome", String.class, null);
			_tableForMarkerResults.addContainerProperty("Position", String.class, null);
			_tableForMarkerResults.addContainerProperty("Trait", String.class, null);
		}else{
			_tableForMarkerResults.addContainerProperty("Select", CheckBox.class, null);
			_tableForMarkerResults.addContainerProperty("Marker", String.class, null);
			_tableForMarkerResults.addContainerProperty("Map", String.class, null);
			_tableForMarkerResults.addContainerProperty("Chromosome", String.class, null);
			_tableForMarkerResults.addContainerProperty("Position", String.class, null);
			
		}
		traitsList=new ArrayList<String>();

		if (null != listOfAllMapData){
			_tableForMarkerResults.setEnabled(true);
			arrayOfCheckBoxesForMap = new CheckBox[listOfAllMapData.size()];
			for (int i = 0; i < listOfAllMapData.size(); i++){

				MapInfo mappingData = listOfAllMapData.get(i);

				String strLinkageGroup = mappingData.getLinkageGroup();
				//Integer mapId = mappingData.getMapId();
				//String strMapName = mappingData.getMapName();
				//String mapUnit = mappingData.getMapUnit();
				Integer markerId = mappingData.getMarkerId();
				String markerName = mappingData.getMarkerName();
				float startPosition = mappingData.getStartPosition();
				
				String strQtlTrait = "";
				if(null != htQTLDetails) {
					QtlDetails qtlDetails = htQTLDetails.get(markerId + markerName);
					if(null != qtlDetails) {
						//strQtlTrait = qtlDetails.getTrait();
						Integer iTraitId = qtlDetails.getTraitId();
						if (null != iTraitId){
							/*TraitDAO traitDAOLocal = new TraitDAO();
							traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
							String traitFromLocal;
							try {
								//traitFromLocal = traitDAOLocal.getByTraitId(iTraitId);
								
								traitFromLocal=om.getStandardVariable(iTraitId).getName();
								//System.out.println(".................traitFromLocal..:"+traitFromLocal);
								strQtlTrait=traitFromLocal;
								if(!traitsList.contains(traitFromLocal))
									traitsList.add(traitFromLocal);
								addValues(traitFromLocal, markerId);
								
							} catch (MiddlewareQueryException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}
				}
				
				arrayOfCheckBoxesForMap[i] = new CheckBox();
				if(htQTLDetails!=null && htQTLDetails.size()>0){		
					_tableForMarkerResults.addItem(new Object[] {arrayOfCheckBoxesForMap[i], markerName, mapName, strLinkageGroup, startPosition, strQtlTrait}, new Integer(i));
				}else{
					_tableForMarkerResults.addItem(new Object[] {arrayOfCheckBoxesForMap[i], markerName, mapName, strLinkageGroup, startPosition}, new Integer(i));
				}
			}			
		} else {
			_tableForMarkerResults.setEnabled(false);
		}

		//Building the top panel
		HorizontalLayout topHorizontalLayout = new HorizontalLayout();
		topHorizontalLayout.setSpacing(true);

		Label lblBinSize = new Label("Bin Size");
		lblBinSize.setStyleName(Reindeer.LABEL_SMALL);
		topHorizontalLayout.addComponent(lblBinSize);

		txtBinSize = new TextField();	
		txtBinSize.setImmediate(true);
		txtBinSize.setDescription("Bin Size must be a numeric only.");
		txtBinSize.setTextChangeEventMode(TextChangeEventMode.EAGER);

		//final Hashtable<String, String> htTraitList = new Hashtable<String, String>();		
		final HashMap<Integer, String> htTraitList = new HashMap<Integer, String>();
		
		//String regexp = "[0-9.]{1,4}";
		String regexp = "[0-9.]*";
		final RegexpValidator regexpValidator = new RegexpValidator(regexp, "Not a number");
		regexpValidator.setErrorMessage("Please provide a valid numeric value.");
		txtBinSize.setNullRepresentation("");
		txtBinSize.addValidator(regexpValidator);
		final Property.ValueChangeListener valueChangelistener = new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				selMarkerList =new ArrayList();
				Object value = event.getProperty().getValue();
				//System.out.println("Value Change listener: " + value);
				String strValue = value.toString(); 
				if (0 == strValue.length()){
					uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
					return;
				}
				double dData = 0.0;
				try {
					dData = Double.parseDouble(strValue);
				} catch(Throwable th) {
					uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
					return;
				}
				handleChecks(txtBinSize, checkBox, selectTrait, htTraitList);
			}
		};
		txtBinSize.addListener(valueChangelistener);
		
		
		final VerticalLayout layoutForBinSize = new VerticalLayout();
		layoutForBinSize.addComponent(txtBinSize);
		layoutForBinSize.addListener(new LayoutClickListener() {
			private static final long serialVersionUID = 1L;
			public void layoutClick(LayoutClickEvent event) {
		        if (event.getChildComponent() == txtBinSize) {
		            String strValue = txtBinSize.getValue().toString(); 
		            //System.out.println("clicked the TextField: " + strValue);
					if (0 == strValue.length()){
						uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
						return;
					}
					double dData = 0.0;
					try {
						dData = Double.parseDouble(strValue);
					} catch(Throwable th) {
						//_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a valid real value.", Notification.TYPE_ERROR_MESSAGE);
						uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
						return;
					}
					handleChecks(txtBinSize, checkBox, selectTrait, htTraitList);
		        }
		    }
		});
		
		topHorizontalLayout.addComponent(layoutForBinSize);
		
		Label lblCM = new Label("cM");
		lblCM.setStyleName(Reindeer.LABEL_SMALL);
		topHorizontalLayout.addComponent(lblCM);

		checkBox = new CheckBox();
		checkBox.setCaption("Check Trait(s)");
		checkBox.setImmediate(true);		
		selectTrait = new ListSelect();
		
		checkBox.addListener(new Property.ValueChangeListener() {
            //private static final long serialVersionUID = -6857112166321059475L;
            //String msg="Good Morning";
            
            public void valueChange(ValueChangeEvent event) {
                boolean value = (Boolean) event.getProperty().getValue();
                
                Object value1 = selectTrait.getValue();
            	Set<Integer> hashSet1 = (Set<Integer>) value1;
            	ArrayList selectedTraits=new ArrayList(); 
            	for (Integer string : hashSet1) {
            		selectedTraits.add(string);
				}
            	
            	if(Integer.parseInt(selectedTraits.get(0).toString())==1){
            		_mainHomePage.getMainWindow().getWindow().showNotification("Please select the Trait and click CheckTrait(s).", Notification.TYPE_ERROR_MESSAGE);
            		checkBox.setValue(false);
        			return;
            	}else{
                    if(value){
	                	handleChecksWithTraits(checkBox, selectedTraits, htTraitList);
	               }else{	                	
	                	String binsize=txtBinSize.getValue().toString();	                		                	
	                	ArrayList markers=new ArrayList();
	            		String selectedTrait="";
	            		for(int t=0;t<selectedTraits.size();t++){
	            			 ArrayList markersList=new ArrayList();
	            			 selectedTrait=htTraitList.get(Integer.parseInt(selectedTraits.get(t).toString()));
	            		      //System.out.println("!!!!!!!!!!!!!...............  :"+selectedTrait);
	            		      markersList=hashMap.get(selectedTrait);
	            		      for(int m=0;m<markersList.size();m++){
	            		    	  if(!markers.contains(markersList.get(m)))
	            		    		  markers.add(markersList.get(m));
	            		      }
	            		 }
	            		 
	            		 //System.out.println(".......... markers=:"+markers);
	            		 
	            		 
	            		 //arrayOfCheckBoxesForMap= new 
	            		//System.out.println("... selMarkerList:"+selMarkerList);
	            		for (int ii = 0; ii < listOfAllMapData.size(); ii++){
	            			arrayOfCheckBoxesForMap[ii].setValue(false);
	            		}
	            		
	            		
	            		
	            		
	            		for (int ic = 0; ic < listOfAllMapData.size(); ic++){            		
	            			MapInfo mappingData = listOfAllMapData.get(ic);            			
	            			if(selMarkerList.size()>0){            			
	            				if(selMarkerList.contains(mappingData.getMarkerId())){           					
	            					if(markers.contains(mappingData.getMarkerId())){
	            						if((Boolean)arrayOfCheckBoxesForMap[ic].getValue()==false){            							
	            							arrayOfCheckBoxesForMap[ic].setValue(true);
	            						}
	            					
	            					}        						
	        					}else {
	        						arrayOfCheckBoxesForMap[ic].setValue(false);        					
	        					}      				
	            			}
	            		}
	            		       		
	                	
	                      txtBinSize.setValue(binsize);
	                      txtBinSize.requestRepaint();
	                      layoutForBinSize.requestRepaint();
	                }
                //getWindow().showNotification("Check: " + value);
            }
            }
        });
		
		
		
		//final ListSelect selectTrait = new ListSelect();
		Object itemId1 = selectTrait.addItem();
		selectTrait.setItemCaption(itemId1, "Select Trait");
		selectTrait.setValue(itemId1);
		selectTrait.setNullSelectionAllowed(true);
		selectTrait.setMultiSelect(true);
		selectTrait.setRows(3);
		int iCount = 2;
		//System.out.println("htQTLDetails=:"+htQTLDetails);
		if(null != htQTLDetails) {
			Iterator<String> iterator = htQTLDetails.keySet().iterator();
			while(iterator.hasNext()) {
				String strKey = iterator.next();
				QtlDetails qtlDetails = htQTLDetails.get(strKey);
				//String trait = qtlDetails.getTrait();
				String trait = "";
				Integer iTraitId = qtlDetails.getTraitId();
				if (null != iTraitId){
					/*TraitDAO traitDAOLocal = new TraitDAO();
					traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
					Trait traitFromLocal;*/
					String traitFromLocal="";
					try {
						trait=om.getStandardVariable(iTraitId).getName();
						
					} catch (MiddlewareQueryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				if(true == htTraitList.containsValue(trait)) {
					continue;
				}
				if(null != trait && 0 != trait.length()) {
					selectTrait.addItem(iCount);
					selectTrait.setItemCaption(iCount, trait);
					htTraitList.put(iCount, trait);
					iCount++;
					
				}
			}
		}
		if(htQTLDetails!=null && htQTLDetails.size()>0){			
			topHorizontalLayout.addComponent(selectTrait);

		}
	
		if(htQTLDetails!=null && htQTLDetails.size()>0){			
			topHorizontalLayout.addComponent(checkBox);
		}

		resultsLayoutForPolymorphicMaps.addComponent(topHorizontalLayout);
		resultsLayoutForPolymorphicMaps.setComponentAlignment(topHorizontalLayout, Alignment.TOP_CENTER);

		resultsLayoutForPolymorphicMaps.addComponent(_tableForMarkerResults);
		resultsLayoutForPolymorphicMaps.setComponentAlignment(_tableForMarkerResults, Alignment.MIDDLE_CENTER);

		HorizontalLayout layoutForExportTypes = new HorizontalLayout();		
		layoutForExportTypes.setSpacing(true);
		ThemeResource themeResource = new ThemeResource("images/excel.gif");
		Button excelButton = new Button();
		excelButton.setIcon(themeResource);
		excelButton.setStyleName(Reindeer.BUTTON_LINK);
		excelButton.setDescription("EXCEL Format");
		layoutForExportTypes.addComponent(excelButton);
		excelButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					exportToExcel();
				} catch (GDMSException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
			
		});		
		
		//20131206: Added a new button to export the data in KBio format 
		//TODO: Have to add the required icon
		
		ThemeResource themeResource1 = new ThemeResource("images/LGC_Genomics.gif");
		Button kbioButton = new Button();
		kbioButton.setIcon(themeResource1);
		kbioButton.setStyleName(Reindeer.BUTTON_LINK);
		kbioButton.setDescription("LGC Genomics Order form");
		layoutForExportTypes.addComponent(kbioButton);
		kbioButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$");
				try{
					ExportFileFormats exportFileFormats = new ExportFileFormats();
					
					String mType="SNP";
					ArrayList markerList=new ArrayList();
					
					for (int i = 0; i < listOfAllMapData.size(); i++){					

						MapInfo mappingData = listOfAllMapData.get(i);				
						
						if (false == (Boolean) arrayOfCheckBoxesForMap[i].getValue()) {
							continue;
						}				
						
						String strMarkerName = mappingData.getMarkerName();
						markerList.add(strMarkerName);
					}
					//System.out.println("markerList=:"+markerList);
					snpMarkerList=new ArrayList();
					List<String> results =genoManager.getMarkerNamesByMarkerType(mType, 0, (int)genoManager.countMarkerNamesByMarkerType(mType));
				
					for(int m=0;m<markerList.size();m++){						
						if(results.contains(markerList.get(m).toString())){
							snpMarkerList.add(markerList.get(m).toString());
						}						
					}
					
					if(snpMarkerList.size()>0){
						File kbioOrderFormFile = exportFileFormats.exportToKBio(snpMarkerList, _mainHomePage);
						FileResource fileResource = new FileResource(kbioOrderFormFile, _mainHomePage);
						_mainHomePage.getMainWindow().getWindow().open(fileResource, "_self");						
					}else{
						_mainHomePage.getMainWindow().getWindow().showNotification("No SNP Marker(s) to create KBio Order form", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				
				} catch (Exception e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error generating LGC Order Form", Notification.TYPE_ERROR_MESSAGE);
					return;
				}		
				
			}
		});
		
		
		if (0 == _tableForMarkerResults.size()){
			txtBinSize.setEnabled(false);
			selectTrait.setEnabled(false);
			checkBox.setEnabled(false);
			excelButton.setEnabled(false);
			/*pdfButton.setEnabled(false);
			printButton.setEnabled(false);*/
		} else {
			txtBinSize.setEnabled(true);
			selectTrait.setEnabled(true);
			checkBox.setEnabled(true);
			excelButton.setEnabled(true);
			/*pdfButton.setEnabled(true);
			printButton.setEnabled(true);*/
		}

		resultsLayoutForPolymorphicMaps.addComponent(layoutForExportTypes);
		resultsLayoutForPolymorphicMaps.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);

		return resultsLayoutForPolymorphicMaps;
	
		
		}
	
	
	private void exportToExcel() throws GDMSException {
		if(null == listOfAllMapData || 0 == listOfAllMapData.size()) {
			return;
		}
		List<String[]> listToExport = new ArrayList<String[]>();
		String strFileName = "tmp";
		for (int i = 0; i < listOfAllMapData.size(); i++){
			
			MapInfo mappingData = listOfAllMapData.get(i);
			
			//20131108: Tulasi --- Added the following condition to export just the selected marker
			if (false == (Boolean) arrayOfCheckBoxesForMap[i].getValue()) {
				continue;
			}
			//20131108: Tulasi --- Added the condition to export just the selected marker

			String strLinkageGroup = mappingData.getLinkageGroup();
			//Integer mapId = mappingData.getMapId();
			//String strMapName = mappingData.getMapName();
			//String mapUnit = mappingData.getMapUnit();
			Integer markerId = mappingData.getMarkerId();
			String markerName = mappingData.getMarkerName();
			float startPosition = mappingData.getStartPosition();
			String strQtlTrait = "";
			if(null != htQTLDetails) {
				QtlDetails qtlDetails = htQTLDetails.get(markerId + markerName);
				if(null != qtlDetails) {
					//strQtlTrait = qtlDetails.getTrait();
					Integer iTraitId = qtlDetails.getTraitId();
					if (null != iTraitId){
						/*TraitDAO traitDAOLocal = new TraitDAO();
						traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
						String traitFromLocal="";
						try {
							/*traitFromLocal = traitDAOLocal.getByTraitId(iTraitId);
							if (null != traitFromLocal){
								strQtlTrait = traitFromLocal.getAbbreviation();
							} else {
								TraitDAO traitDAOCentral = new TraitDAO();
								traitDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
								Trait traitFromCentral = traitDAOCentral.getByTraitId(iTraitId);
								strQtlTrait = traitFromCentral.getAbbreviation();
							}*/
							
							traitFromLocal=om.getStandardVariable(iTraitId).getName();
							
						} catch (MiddlewareQueryException e) {
							throw new GDMSException(e.getMessage());
						}
					}
					strFileName = "MarkerTrait";
				}
			}
			String[] strValues = new String[] {markerName, mapName, strLinkageGroup, String.valueOf(startPosition), strQtlTrait};
			listToExport.add(strValues);
		}
		
		if(0 == listToExport.size()) {
			_mainHomePage.getMainWindow().getWindow().showNotification("No Markers to export",  Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		String[] strValues = new String[] {"Marker", "Map", "Chromosome", "Position", "Reason"};
		listToExport.add(0, strValues);
		
		
		ExportFileFormats exportFileFormats = new ExportFileFormats();
		try {
			exportFileFormats.exportMap(_mainHomePage, listToExport, strFileName);
		} catch (WriteException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
			return;
		} catch (IOException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
			return;
		}
	}
	
	private void exportToExcel(String theBINRange) throws GDMSException {
		if(null == listOfAllMapData || 0 == listOfAllMapData.size()) {
			return;
		}
		List<String[]> listToExport = new ArrayList<String[]>();
		String strFileName = "Marker";
		for (int i = 0; i < listOfAllMapData.size(); i++){
			
			if(false == arrayOfCheckBoxesForMap[i].booleanValue()) {
				continue;
			}
			
			MapInfo mappingData = listOfAllMapData.get(i);

			String strLinkageGroup = mappingData.getLinkageGroup();
			//Integer mapId = mappingData.getMapId();
			//String strMapName = mappingData.getMapName();
			//String mapUnit = mappingData.getMapUnit();
			Integer markerId = mappingData.getMarkerId();
			String markerName = mappingData.getMarkerName();
			float startPosition = mappingData.getStartPosition();
			String strQtlTrait = "";
			if(null != htQTLDetails) {
				QtlDetails qtlDetails = htQTLDetails.get(markerId + markerName);
				if(null != qtlDetails) {
					//strQtlTrait = qtlDetails.getTrait();
					Integer iTraitId = qtlDetails.getTraitId();
					if (null != iTraitId){
						/*TraitDAO traitDAOLocal = new TraitDAO();
						traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
						String traitFromLocal="";
						try {
							traitFromLocal=om.getStandardVariable(iTraitId).getName();
						} catch (MiddlewareQueryException e) {
							throw new GDMSException(e.getMessage());
						}
					}
					strFileName = "MarkerTrait";
				}
			}
			if(null != theBINRange && 0 != theBINRange.trim().length()) {
				if(null == strQtlTrait || 0 == strQtlTrait.trim().length()) {
					strQtlTrait = theBINRange + "CM";
				} else {
					strQtlTrait = theBINRange + "CM and " + strQtlTrait;
				}
			} else {
				if(null == strQtlTrait || 0 == strQtlTrait.trim().length()) {
					strQtlTrait = "";
				}
			}
			
			String[] strValues = new String[] {markerName, mapName, strLinkageGroup, String.valueOf(startPosition), strQtlTrait};
			listToExport.add(strValues);
		}
		
		if(0 == listToExport.size()) {
			_mainHomePage.getMainWindow().getWindow().showNotification("No Maps to export",  Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		String[] strValues = new String[] {"Marker", "Map", "Chromosome", "Position", "Reason"};
		listToExport.add(0, strValues);
		
		
		ExportFileFormats exportFileFormats = new ExportFileFormats();
		try {
			exportFileFormats.exportMap(_mainHomePage, listToExport, strFileName);
		} catch (WriteException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
			return;
		} catch (IOException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
			return;
		}
	}
	
	private void handleChecks(final TextField txtBinSize, CheckBox checkBox, ListSelect selectTrait, HashMap<Integer,String> htTraitList) {
		Object value = txtBinSize.getValue();
		if(null == value) {
			uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
			return;
		}
		String strValue = value.toString();
		if (0 == strValue.trim().length()){
			uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
			//return;
		}
		double dData = 0.0;
		try {
			dData = Double.parseDouble(strValue);
		} catch(Throwable th) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a valid real value.", Notification.TYPE_ERROR_MESSAGE);
			uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
			return;
		}
		if(null == listOfAllMapData) {
			return;
		}

		ArrayList<MapInfo> arrayListOfChromosomeData = new ArrayList<MapInfo>();
		arrayListOfChromosomeData.addAll(listOfAllMapData);
		
		String strCurrentChromosome = "";
		//String strNextChromosome = "";
		//for (int iLGIndex = iNextChromosomeIndex; iLGIndex <= arrayListOfChromosomeData.size(); iLGIndex++) {
		int iLGIndex =  0;
		while (iLGIndex < arrayListOfChromosomeData.size()) {
			
			MapInfo mappingData = arrayListOfChromosomeData.get(iLGIndex);
			MapInfo mappingDataNext = null;
			if((iLGIndex + 1) < arrayListOfChromosomeData.size()) {
				mappingDataNext = arrayListOfChromosomeData.get(iLGIndex+1);
			} else {
				break;
			}
			if(null == mappingDataNext) {
				continue;
			}
			
			//if (0 == iLGIndex) {
				strCurrentChromosome = mappingData.getLinkageGroup();
				//strNextChromosome = mappingDataNext.getLinkageGroup();
			//}
			
			/*if (false == strCurrentChromosome.equals(strNextChromosome)) {
				strCurrentChromosome = strNextChromosome;
				dData = dData + mappingData.getStartPosition();
			} else {
				strNextChromosome = mappingDataNext.getLinkageGroup();
				dData = dData + fLatestPositionChecked;
			}*/
			
			//System.out.println("Current Chromosome: " + strCurrentChromosome + " --- dData: " + dData);
			handleChecksWithinChromosome(strCurrentChromosome, iLGIndex, (float)dData, checkBox, selectTrait, htTraitList);
			iLGIndex = iNextChromosomeIndex;
			
		};
		/*boolean bOneCheckBoxChecked = false;
		for (int i = 0; i < listOfAllMapData.size(); i++){
			MapInfo mappingData = listOfAllMapData.get(i);
			MapInfo mappingDataNext = null;
			if((i + 1) < listOfAllMapData.size()) {
				mappingDataNext = listOfAllMapData.get(i+1);
			} else {
				break;
			}
			if(null == mappingDataNext) {
				continue;
			}
			float startPosition = mappingData.getStartPosition();
			float startPositionNext = mappingDataNext.getStartPosition();
			if(0 == dData || dData <= (startPositionNext - startPosition)) {
				if(0 == dData) {
					arrayOfCheckBoxesForMap[i].setValue(true);
				} else {
					handleCheckBox(checkBox, selectTrait, 0, htTraitList);
				}
				arrayOfCheckBoxesForMap[i+1].setValue(true);
				bOneCheckBoxChecked = true; 
			} else if ((startPosition > dData) && false == bOneCheckBoxChecked) {
				arrayOfCheckBoxesForMap[i].setValue(true);
				bOneCheckBoxChecked = true;
				if(checkBox.booleanValue()) {
					handleCheckBox(checkBox, selectTrait, i + 1, htTraitList);
				} else {
					arrayOfCheckBoxesForMap[i+1].setValue(false);
				}
			} else {
				if(checkBox.booleanValue()) {
					handleCheckBox(checkBox, selectTrait, i + 1, htTraitList);
				} else {
					arrayOfCheckBoxesForMap[i+1].setValue(false);
				}
			}
		}
		
		//20131112: Tulasi: Added the following condition to set the first checkbox to true if its not true
		if (true != (Boolean)arrayOfCheckBoxesForMap[0].getValue()){
			arrayOfCheckBoxesForMap[0].setValue(true);
		}*/
	}
	private void handleUnChecksWithTraits(TextField txtBinSize, CheckBox checkBox, ArrayList selectTrait, HashMap<Integer, String> htTraitList) {
		ArrayList markers=new ArrayList();
		String selectedTrait="";
		//selMarkerList
		//System.out.println("...........selectTrait:"+selectTrait+"   "+listOfSelectedTraits);
		 for(int t=0;t<selectTrait.size();t++){
			 ArrayList markersList=new ArrayList();
			 selectedTrait=htTraitList.get(Integer.parseInt(selectTrait.get(t).toString()));
		      //System.out.println("!!!!!!!!!!!!!...............  :"+selectedTrait);
		      markersList=hashMap.get(selectedTrait);
		      for(int m=0;m<markersList.size();m++){
		    	  if(!markers.contains(markersList.get(m)))
		    		  markers.add(markersList.get(m));
		      }
		 }
		 
		 //System.out.println(".......... markers=:"+markers);
		 
		 
		 //arrayOfCheckBoxesForMap= new 
		//System.out.println("... selMarkerList:"+selMarkerList);
		for (int i = 0; i < listOfAllMapData.size(); i++){
			arrayOfCheckBoxesForMap[i].setValue(false);
		}
		
		
		
		
		for (int i = 0; i < listOfAllMapData.size(); i++){
			//arrayOfCheckBoxesForMap[i].setValue(false);
			MapInfo mappingData = listOfAllMapData.get(i);
			//System.out.println(mappingData.get);
			if(selMarkerList.size()>0){
				//System.out.println(".........................binsize markers size > 0");
				if(markers.contains(mappingData.getMarkerId())){
					if(selMarkerList.contains(mappingData.getMarkerId())){	
						//System.out.println("exists in selMarkersList  :"+mappingData.getMarkerName()+"   "+arrayOfCheckBoxesForMap[i].getValue()+"    "+i+"   "+mappingData.getMarkerId());
						//true == (Boolean) 
						if((Boolean)arrayOfCheckBoxesForMap[i].getValue()==false){
							//System.out.println("checking true="+i+"   "+mappingData.getMarkerId());
							arrayOfCheckBoxesForMap[i].setValue(true);
						}
						//System.out.println(mappingData.getMarkerId()+"  check "+mappingData.getMarkerName()+"   "+arrayOfCheckBoxesForMap[i].getValue()+"    "+i);
						
					}else {
						//System.out.println("doesnot exists in selMarkersList  :"+mappingData.getMarkerName()+"   "+arrayOfCheckBoxesForMap[i].getValue()+"    "+i+"   "+mappingData.getMarkerId());						
						arrayOfCheckBoxesForMap[i].setValue(false);
						//System.out.println("uncheck "+mappingData.getMarkerName()+"   "+i);
					}
				}else{
					//arrayOfCheckBoxesForMap[i].setValue(true);
					if(selMarkerList.contains(mappingData.getMarkerId())){						
						//System.out.println(" ......    exists in selMarkersList but not exists in markers  :"+mappingData.getMarkerName()+"   "+arrayOfCheckBoxesForMap[i].getValue()+"   "+i);
						if((Boolean)arrayOfCheckBoxesForMap[i].getValue()==false){
							//System.out.println(".....  checking true="+i);
							arrayOfCheckBoxesForMap[i].setValue(true);
						}
						//System.out.println(" ......    check "+mappingData.getMarkerName()+"   "+arrayOfCheckBoxesForMap[i].getValue()+"   "+i);			
					}else {
						//System.out.println(" ......    doesnot exists in selMarkersList  :"+mappingData.getMarkerName()+"   "+i);	
						arrayOfCheckBoxesForMap[i].setValue(false);
						//System.out.println(" ......    uncheck "+mappingData.getMarkerName()+"   "+i);
					}
				}
			}else{
				//System.out.println(".........................binsize markers size is 0");
				if(markers.contains(mappingData.getMarkerId())){
					arrayOfCheckBoxesForMap[i].setValue(false);
				}
			}
		}
		
		/*for (int i = 0; i < listOfAllMapData.size(); i++){			
			MapInfo mappingData = listOfAllMapData.get(i);			
			if((Boolean)arrayOfCheckBoxesForMap[i].getValue()==true){
				System.out.println("checking true="+i+"   "+mappingData.getMarkerId());
				//arrayOfCheckBoxesForMap[i].setValue(true);
			}		
		}*/
		
		
		
		
	}
	private void handleChecksWithTraits(CheckBox checkBox, ArrayList selectTrait, HashMap<Integer, String> htTraitList) {
		
		ArrayList markers=new ArrayList();
		String selectedTrait="";
		//if(listOfSelectedTraits!=null){
			//System.out.println("...........selectTrait:"+selectTrait+"   "+listOfSelectedTraits);
			 for(int t=0;t<selectTrait.size();t++){
				 ArrayList markersList=new ArrayList();
				 selectedTrait=htTraitList.get(Integer.parseInt(selectTrait.get(t).toString()));
			      System.out.println("!!!!!!!!!!!!!...............  :"+selectedTrait);
			      markersList=hashMap.get(selectedTrait);
			      for(int m=0;m<markersList.size();m++){
			    	  if(!markers.contains(markersList.get(m)))
			    		  markers.add(markersList.get(m));
			      }
			 }
			//System.out.println(markers.size()+".......... markers=:"+markers);
			for (int i = 0; i < listOfAllMapData.size(); i++){
				MapInfo mappingData = listOfAllMapData.get(i);
				//System.out.println(mappingData.get);
				if(markers.contains(mappingData.getMarkerId())){
					arrayOfCheckBoxesForMap[i].setValue(true);
				}
			}
		/*}else{
			_mainHomePage.getMainWindow().getWindow().showNotification("Please select the Trait and click CheckTrait(s).", Notification.TYPE_ERROR_MESSAGE);			
			return;
		}*/
		 
	}
	
	
	
private void handleChecksWithinChromosome(String theChromosome, int theChromosomesStartingIndex, float dBinSizeValue, CheckBox checkBox, ListSelect selectTrait, HashMap<Integer, String> htTraitList) {
		//System.out.println("selectTrait=:"+selectTrait);
		//boolean bOneCheckBoxChecked = false;
		float temp = dBinSizeValue;
		
		for (int i = theChromosomesStartingIndex; i < listOfAllMapData.size(); i++){
			
			if (null == listOfAllMapData.get(i)){
				iNextChromosomeIndex = i;
				return;
			}
			
			MapInfo mappingData = listOfAllMapData.get(i);
			
			if (false == theChromosome.equals(mappingData.getLinkageGroup())){
				iNextChromosomeIndex = i;
				//System.out.println("iNextChromosomeIndex: " + iNextChromosomeIndex);
				return;
			} else {
				iNextChromosomeIndex = i;
			}
			
			float startPosition = mappingData.getStartPosition();
			//System.out.print("handleChecks: --- i:" + i + " --- startPosition:" + startPosition + " --- chromosome:" + mappingData.getLinkageGroup() + " --- dData: " + dBinSizeValue);
			
			if(0 == dBinSizeValue) {
				arrayOfCheckBoxesForMap[i].setValue(true);
				if(!selMarkerList.contains(mappingData.getMarkerId())){
					selMarkerList.add(mappingData.getMarkerId());
				}
				//System.out.println(" --- Selected");
				//bOneCheckBoxChecked = true;
			} 
			
			if (0.0 == startPosition){
				arrayOfCheckBoxesForMap[i].setValue(true);
				if(!selMarkerList.contains(mappingData.getMarkerId())){
					selMarkerList.add(mappingData.getMarkerId());
				}
				//System.out.println(" --- Selected");
				//bOneCheckBoxChecked = true;
			} else if (temp <= startPosition) {
				//System.out.println("................Else if temp<startPosition..........");
				arrayOfCheckBoxesForMap[i].setValue(true);
				temp = dBinSizeValue + startPosition;
				//System.out.println(selMarkerList.contains(mappingData.getMarkerId()));
				if(!selMarkerList.contains(mappingData.getMarkerId())){
					//System.out.println(selMarkerList+":selMarkerList !contain "+mappingData.getMarkerId());
					selMarkerList.add(mappingData.getMarkerId());
				}
				
			}
			
		}
		//System.out.println("$$$$$$$$$$$$$$$ selMarkerList=:"+selMarkerList);
		
		
	}

	private void uncheckAllCheckBox(CheckBox checkBox, ListSelect selectTrait, HashMap<Integer,String> htTraitList) {
		if(null == listOfAllMapData) {
			return;
		}
		if(0 == listOfAllMapData.size()) {
			return;
		}
		
		if (null == arrayOfCheckBoxesForMap || 0 == arrayOfCheckBoxesForMap.length){
			return;
		}
		
		for (int i = 0; i < listOfAllMapData.size(); i++) {
			handleCheckBox(checkBox, selectTrait, i, htTraitList);
		}
		
	}

	private void handleCheckBox(CheckBox checkBox, ListSelect selectTrait, int i, HashMap<Integer,String> htTraitList) {
		if(checkBox.booleanValue()) {
			handlechecks(selectTrait, i, htTraitList);
		} else {
		   arrayOfCheckBoxesForMap[i].setValue(false);
		}
	}

	private void handlechecks(ListSelect selectTrait, int i,
			HashMap<Integer, String> htTraitList) {
		if(null == listOfAllMapData) {
			return;
		}
		if(0 == listOfAllMapData.size()) {
			return;
		}

		if(i >= listOfAllMapData.size()) {
			return;
		}

		MapInfo mappingData = listOfAllMapData.get(i);
		Integer markerId = mappingData.getMarkerId();
		String markerName = mappingData.getMarkerName();

		String strQtlTrait = "";
		if(null != htQTLDetails) {
			QtlDetails qtlDetails = htQTLDetails.get(markerId + markerName);
			if(null != qtlDetails) {
				//strQtlTrait = qtlDetails.getTrait();
				Integer iTraitId = qtlDetails.getTraitId();
				if (null != iTraitId){
					/*TraitDAO traitDAOLocal = new TraitDAO();
					traitDAOLocal.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());*/
					String traitFromLocal="";
					try {
						/*traitFromLocal = traitDAOLocal.getByTraitId(iTraitId);
						if (null != traitFromLocal){
							strQtlTrait = traitFromLocal.getAbbreviation();
						} else {
							TraitDAO traitDAOCentral = new TraitDAO();
							traitDAOCentral.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral().getSession());
							Trait traitFromCentral = traitDAOCentral.getByTraitId(iTraitId);
							strQtlTrait = traitFromCentral.getAbbreviation();
						}*/
						
						traitFromLocal=om.getStandardVariable(iTraitId).getName();
					} catch (MiddlewareQueryException e) {
						//throw new GDMSException(e.getMessage());
					}
				}
			}
		}
		if (null != strQtlTrait && 0 != strQtlTrait.length()){
			Object value = selectTrait.getValue();
			if(null != value) {
				if(value instanceof Set) {
					Set set = (Set) value;
					List<String> listOfSelectedTraits = new ArrayList<String>();
					for (Object object : set) {
						listOfSelectedTraits.add(object.toString());
					}
					for (int j = 0; j < listOfSelectedTraits.size(); j++) {
						if(null != htTraitList && htTraitList.containsKey(listOfSelectedTraits.get(j))) {
							arrayOfCheckBoxesForMap[i].setValue(true);
						}
					}
				} else {
					if(strQtlTrait.equals(value.toString())) {
						arrayOfCheckBoxesForMap[i].setValue(true);
					}
				}
			} else {
				arrayOfCheckBoxesForMap[i].setValue(false);
			}
		} else {
			arrayOfCheckBoxesForMap[i].setValue(false);
		}
	}

	
	private void viewMapUpdates() {
		try {
			if(null == listOfAllMapData) {
				return;
			}
			if(0 == listOfAllMapData.size()) {
				return;
			}
			List<QtlDetails> all2 = getQTLDetails();
			//System.out.println("all2::"+all2);
			htQTLDetails = new Hashtable<String, QtlDetails>();
			for (MapInfo mappingData : listOfAllMapData) {
				Integer strMarkerID = mappingData.getMarkerId();
				String strMarkerName = mappingData.getMarkerName();
				//int mapId =mapId;
				String linkageGroup = mappingData.getLinkageGroup();
				float startPosition = mappingData.getStartPosition();
				for (QtlDetails qtlDetails : all2) {
					/*QtlDetailsPK qtlDetailsPK = qtlDetails.getId();
					int mapId2 = qtlDetailsPK.getMapId();*/
					int mapId2 = qtlDetails.getMapId();
					//System.out.println("mapId2:"+mapId2+"    mapId="+mapId);
					String linkageGroup2 = qtlDetails.getLinkageGroup();
					Float minPosition = qtlDetails.getMinPosition();
					Float maxPosition = qtlDetails.getMaxPosition();
					if(mapId==mapId2 && linkageGroup.equals(linkageGroup2)
							&& startPosition >= minPosition && startPosition <= maxPosition) {
						htQTLDetails.put(strMarkerID + strMarkerName, qtlDetails);
					}
				}
			}
			//System.out.println("htQTLDetails=:"+htQTLDetails);
			//SELECT DISTINCT gdms_mapping_data.marker_name, gdms_mapping_data.map_name, gdms_mapping_data.start_position, gdms_mapping_data.linkage_group, 
			//gdms_qtl_details.trait FROM gdms_mapping_data, gdms_qtl_details WHERE gdms_mapping_data.map_name='ICGS 44 X ICGS 76' AND 
			//gdms_mapping_data.map_id=gdms_qtl_details.map_id AND gdms_mapping_data.linkage_group=gdms_qtl_details.linkage_group AND gdms_mapping_data.start_position 
			//BETWEEN gdms_qtl_details.min_position AND gdms_qtl_details.max_position ORDER BY map_name, linkage_group,start_position, marker_name ASC, trait
			
		} catch (MiddlewareQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<QtlDetails> getQTLDetails() throws MiddlewareQueryException {
		List<QtlDetails> listToReturn = new ArrayList<QtlDetails>();
		List<QtlDetails> localQTLDetails = getLocalQTLDetails();
		if(null != localQTLDetails) {
			listToReturn.addAll(localQTLDetails);
		}
		List<QtlDetails> centralQTLDetails = getCentralQTLDetails();
		if(null != centralQTLDetails) {
			listToReturn.addAll(centralQTLDetails);
		}
		
		//System.out.println("listToReturn....:"+listToReturn);
		
		
		return listToReturn;
	}

	private List<QtlDetails> getCentralQTLDetails() throws MiddlewareQueryException {
		QtlDetailsDAO qtlDetailsDAO = new QtlDetailsDAO();
		try{
			qtlDetailsDAO.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession());
		}catch (Exception e){
			e.printStackTrace();
		}
		List<QtlDetails> allQTLDetailsCentral = qtlDetailsDAO.getAll();
		//System.out.println("........From Central .............:"+allQTLDetailsCentral);
		return allQTLDetailsCentral;
	}

	private List<QtlDetails> getLocalQTLDetails()
			throws MiddlewareQueryException {
		QtlDetailsDAO qtlDetailsDAO = new QtlDetailsDAO();
		try{
			qtlDetailsDAO.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession());
		}catch (Exception e){
			e.printStackTrace();
		}
		List<QtlDetails> allQTLDetailsLocal = qtlDetailsDAO.getAll();
		//System.out.println("..........From Local............... :"+allQTLDetailsLocal);
		return allQTLDetailsLocal;
	}
	
	private ArrayList<String[]> sortMappingDataListToBeDisplayed(List<MapInfo> theListOfAllMappingData) {
		SortingMaps sortingMaps = new SortingMaps();
		List<MapInfo> sortedList = sortingMaps.sort(theListOfAllMappingData);
		
		DecimalFormat decfor = new DecimalFormat("#.00");
		ArrayList<String> CD=new ArrayList<String>();
		String mapUnit = "CM";//default
		for (MapInfo mappingData : sortedList) {
			mapUnit = mappingData.getMapUnit();
			if(mapUnit.equalsIgnoreCase("bp")){
				CD.add(mappingData.getLinkageGroup()+"!~!"+new BigDecimal(mappingData.getStartPosition())+"!~!"+mappingData.getMarkerName()+"!~!"+mappingData.getMapName());
			}else{
				CD.add(mappingData.getLinkageGroup()+"!~!"+decfor.format(mappingData.getStartPosition())+"!~!"+mappingData.getMarkerName()+"!~!"+mappingData.getMapName());
			}
		}
	
		ArrayList<String[]> dist=new ArrayList<String[]>();
		String[] strArr=CD.get(0).toString().split("!~!");
		double dis=Double.parseDouble(strArr[1]);
		String chr=strArr[0];
		int count=0;
		int dis1=0;
		for(int a=0;a<CD.size();a++){
			String[] str1=CD.get(a).toString().split("!~!");						
			if(str1[0].equals(chr)){							
				double distance=Double.parseDouble(str1[1])-dis;
				distance=roundThree(distance);
				if(mapUnit.equalsIgnoreCase("bp")){
					String[] strValues = new String[5];
					strValues[0] = str1[0];
					strValues[1] = str1[2];
					strValues[2] = String.valueOf(count);
					strValues[3] = String.valueOf(new BigDecimal(distance));
					strValues[4] = str1[1];
					//dist.add(str1[0]+"!~!"+str1[2]+"!~!"+count+"!~!"+new BigDecimal(distance)+"!~!"+str1[1]);
					dist.add(strValues);
				}else{
					if(distance==0.0){
						dis1=0;
						String[] strValues = new String[5];
						strValues[0] = str1[0];
						strValues[1] = str1[2];
						strValues[2] = String.valueOf(count);
						strValues[3] = String.valueOf(dis1);
						strValues[4] = str1[1];
						//dist.add(str1[0]+"!~!"+str1[2]+"!~!"+count+"!~!"+dis1+"!~!"+str1[1]);
						dist.add(strValues);
					}else{
						String[] strValues = new String[5];
						strValues[0] = str1[0];
						strValues[1] = str1[2];
						strValues[2] = String.valueOf(count);
						strValues[3] = String.valueOf(distance);
						strValues[4] = str1[1];
						//dist.add(str1[0]+"!~!"+str1[2]+"!~!"+count+"!~!"+distance+"!~!"+str1[1]);
						dist.add(strValues);
					}
				}
				count=count+1;
				dis=Double.parseDouble(str1[1]);
			}else{	
				count=0;
				dis=Double.parseDouble(str1[1]);
				chr=str1[0];
				if(mapUnit.equalsIgnoreCase("bp")){
					String[] strValues = new String[5];
					strValues[0] = str1[0];
					strValues[1] = str1[2];
					strValues[2] = String.valueOf(count);
					strValues[3] = String.valueOf(new BigDecimal(dis));
					strValues[4] = str1[1];
					//dist.add(str1[0]+"!~!"+str1[2]+"!~!"+count+"!~!"+new BigDecimal(dis)+"!~!"+str1[1]);
					dist.add(strValues);
				}else{
					if(dis==0.0){
						dis1=0;
						String[] strValues = new String[5];
						strValues[0] = str1[0];
						strValues[1] = str1[2];
						strValues[2] = String.valueOf(count);
						strValues[3] = String.valueOf(dis1);
						strValues[4] = String.valueOf(dis1);
						//dist.add(str1[0]+"!~!"+str1[2]+"!~!"+count+"!~!"+dis1+"!~!"+dis1);
						dist.add(strValues);
					}else{
						String[] strValues = new String[5];
						strValues[0] = str1[0];
						strValues[1] = str1[2];
						strValues[2] = String.valueOf(count);
						strValues[3] = String.valueOf(dis1);
						strValues[4] = str1[1];
						//dist.add(str1[0]+"!~!"+str1[2]+"!~!"+count+"!~!"+dis+"!~!"+str1[1]);
						dist.add(strValues);
					}

				}
				count=count+1;	
			}
		}
		return dist;
	}


	public double roundThree(double in){		
		return Math.round(in*1000.0)/1000.0;
	}
	
	/*private Table buildmapTable() {
		_mapTable = new Table();
		_mapTable.setStyleName("markertable");
		_mapTable.setPageLength(10);
		_mapTable.setSelectable(true);
		_mapTable.setColumnCollapsingAllowed(true);
		_mapTable.setColumnReorderingAllowed(true);
		_mapTable.setStyleName("strong");

		String[] strArrayOfColNames = {"MAP-NAME", "LINKAGE-GROUP", "MAP-UNIT", "MARKER-NAME", "MARKER-ID", "START-POSITION"};
		for (int i = 0; i < strArrayOfColNames.length; i++){
			if (0 == i){
				mapTable.addContainerProperty(strArrayOfColNames[i], Button.class, null);
			} else {
				mapTable.addContainerProperty(strArrayOfColNames[i], String.class, null);
			}
			_mapTable.addContainerProperty(strArrayOfColNames[i], String.class, null);
		}
		
		if (null != listOfAllMapData){
			
			for (int i = 0; i < listOfAllMapData.size(); i++){
				
				MapInfo mapDetailElement = listOfAllMapData.get(i);
			
				//final String strMapName = mapDetailElement.getMapName();
				final String strLinkageGroup = mapDetailElement.getLinkageGroup();
				String strMapUnit = mapDetailElement.getMapUnit();
				String strMarkerName = mapDetailElement.getMarkerName();
				Integer markerId = mapDetailElement.getMarkerId();
				final Float fStartPosition = mapDetailElement.getStartPosition();
				
				Button mapNameLink = new Button();
				mapNameLink.setCaption(strMapName);
				mapNameLink.setStyleName(Reindeer.BUTTON_LINK);
				mapNameLink.setDescription(strMapName);
				mapNameLink.addListener(new Button.ClickListener() {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {					
						
						String mapName = strMapName;
						String linkageGroup = strLinkageGroup;
						
						MarkerDAO markerDAO = new MarkerDAO();
						markerDAO.setSession(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal().getSession());
						
						Object rowID = mapTable.getValue();
						if (null != rowID){
							Container containerDataSource = mapTable.getContainerDataSource();
							Item item = containerDataSource.getItem(rowID);
							
							if (null != item){
								String strMapName = item.getItemProperty("MAP-NAME").toString();
								String strLG = item.getItemProperty("LINKAGE-GROUP").toString();
								String strStartPosition = item.getItemProperty("START-POSITION").toString();
								markerDAO.countMarkerIDsByMapIDAndLinkageBetweenStartPosition(mapID, strLG, 0, endPos);
							}
						}
					}
				});
				
				_mapTable.addItem(new Object[] {mapName, strLinkageGroup, strMapUnit, 
						strMarkerName, markerId, fStartPosition}, new Integer(i));
			}
		}
		
		return _mapTable;
	}*/

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}

	private static void addValues(String key, Integer value){
		ArrayList<Integer> tempList = null;
		if(hashMap.containsKey(key)){
			tempList=hashMap.get(key);
			if(tempList == null)
				tempList = new ArrayList<Integer>();
			tempList.add(value);
		}else{
			tempList = new ArrayList();
			tempList.add(value);
		}
		hashMap.put(key,tempList);
	}
}
