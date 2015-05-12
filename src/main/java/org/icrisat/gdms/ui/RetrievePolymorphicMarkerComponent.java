package org.icrisat.gdms.ui;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import jxl.write.WriteException;

import org.generationcp.middleware.dao.gdms.MapDAO;
import org.generationcp.middleware.dao.gdms.QtlDetailsDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GetGermplasmByNameModes;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmNameDetails;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AccMetadataSet;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.MapDetailElement;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.gdms.QtlDetails;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.common.ExportFileFormats;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.retrieve.marker.RetrievePolymorphicMarker;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;


public class RetrievePolymorphicMarkerComponent implements Component.Listener {

	private static final long serialVersionUID = 1L;
	private TabSheet _tabsheetForPolymorphicMarkers;
	private Component buildPolymorphicMapComponent;
	private GDMSMain _mainHomePage;
	protected ArrayList<String> listofMarkers;
	
	protected List<String> markersList;
	
	private Component polymorphicResultComponent;
	private HashMap<String, Integer> hmOfMap = new HashMap<String, Integer>();
	private String strSelectedMapName;
	private Integer iSelectedMapId;
	private ArrayList<MappingData> finalListOfMappingData;
	private HashMap<Integer, String> hmOfMarkerIDAndQtlTrait;
	private ArrayList<String> listofTraits;
	private CheckBox[] arrayOfCheckBoxes;
	private Table _tableForMarkerResults;
	private List<Name> listOfNamesForLine1;
	private List<Name> listOfNamesForLine2;
	
	//private CheckBox checkBox;
	private CheckBox checkBox;
	private ListSelect selectTrait;
	private TextField txtBinSize;
	
	protected ArrayList<String> listOfMarkersSelected;
	private OptionGroup optiongroup;
	protected String strSelectedPolymorphicType;
	private ArrayList missingList;
	ManagerFactory factory=null;
	OntologyDataManager om;
	GenotypicDataManager genoManager;
	
	private ArrayList<Integer> markerIDs;
	
	private Session centralSession;
	private Session localSession;
	
	private Session sessionL;
	private Session sessionC;
	
	
	ArrayList<String> listOfMarkers=new ArrayList<String>();
	
	String strLine1 ="";
	String strLine2 ="";
	String marker_ids="";
	int mapID=0;
	
	String gidsList1="";
	
	ArrayList<String> listOfMapData = new ArrayList<String>();
	protected File orderFormForPlymorphicMarkers;
	
	ArrayList selMarkerList =new ArrayList();
	
	
	static HashMap<Integer, ArrayList<String>> hashMap = new HashMap<Integer,  ArrayList<String>>();
	ArrayList<Integer> listOfMarkerIDs = new ArrayList<Integer>();
	
	
	static HashMap<String, ArrayList<Integer>> hashMap1 = new HashMap<String,  ArrayList<Integer>>(); 
	
	private HashMap<Integer, String> hmOfSelectedMIDandMNames;
	
	private double dLatestPositionChecked;
	private int iNextChromosomeIndex;
	//final ListSelect selectTrait = new ListSelect();
	
	List markersOnMapList;
	
	public RetrievePolymorphicMarkerComponent(GDMSMain theMainHomePage){
		_mainHomePage = theMainHomePage;
		//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
		try{
			//factory = new ManagerFactory(GDMSModel.getGDMSModel().getLocalParams(), GDMSModel.getGDMSModel().getCentralParams());
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			om=factory.getOntologyDataManager();
			genoManager=factory.getGenotypicDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Building the entire Tabbed Component required for Polymorphic Marker
	 * 
	 */
	
	
	public HorizontalLayout buildTabbedComponentForPolymorphicMarker() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();

		_tabsheetForPolymorphicMarkers = new TabSheet();
		//_tabsheetForPolymorphicMarkers.setSizeFull();
		_tabsheetForPolymorphicMarkers.setWidth("700px");

		Component polymorphicSelectLinesComponent = buildPolymorphicSelectLinesComponent();

		polymorphicResultComponent = buildPolymorphicResultComponent();

		buildPolymorphicMapComponent = buildPolymorphicMapComponent();
		
		polymorphicSelectLinesComponent.setSizeFull();
		polymorphicResultComponent.setSizeFull();
		buildPolymorphicMapComponent.setSizeFull();
		
		_tabsheetForPolymorphicMarkers.addComponent(polymorphicSelectLinesComponent);
		_tabsheetForPolymorphicMarkers.addComponent(polymorphicResultComponent);
		_tabsheetForPolymorphicMarkers.addComponent(buildPolymorphicMapComponent);
		
		
		_tabsheetForPolymorphicMarkers.getTab(1).setEnabled(false);
		_tabsheetForPolymorphicMarkers.getTab(2).setEnabled(false);

		horizontalLayout.addComponent(_tabsheetForPolymorphicMarkers);

		return horizontalLayout;
	}


	private Component buildPolymorphicSelectLinesComponent() {
		VerticalLayout selectLinesLayout = new VerticalLayout();
		selectLinesLayout.setCaption("Select Lines");
		selectLinesLayout.setMargin(true, true, true, true);
		
		final ArrayList<String> germplasmList1=new ArrayList<String>();
		
		final ComboBox selectMarker1 = new ComboBox();
		Object itemId1 = selectMarker1.addItem();
	
		selectMarker1.setItemCaption(itemId1, "Select Line-1");
		selectMarker1.setValue(itemId1);
		selectMarker1.setNullSelectionAllowed(false);
		selectMarker1.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
		selectMarker1.setImmediate(true);
		
		
		final RetrievePolymorphicMarker retrievePolymorphicMarker = new RetrievePolymorphicMarker();
		optiongroup = new OptionGroup();
		optiongroup.setMultiSelect(false);
		optiongroup.addStyleName("horizontal");
		optiongroup.addItem("Finger Printing");
		optiongroup.addItem("Mapping");
		optiongroup.setImmediate(true);
		optiongroup.addListener(new Component.Listener() {
			private static final long serialVersionUID = 1L;
			public void componentEvent(Event event) {
				Object value = optiongroup.getValue();
				strSelectedPolymorphicType = value.toString();
				retrievePolymorphicMarker.setPolymorphicType(strSelectedPolymorphicType);
				List gNamesDup=new ArrayList<String>();
				List gNames=new ArrayList<String>();
				try {
					listOfNamesForLine1 = retrievePolymorphicMarker.getNamesForRetrievePolymorphic();
					//System.out.println("......................  :"+listOfNamesForLine1);
					
					if (null != selectMarker1 && 0 != selectMarker1.getItemIds().size()) {
						selectMarker1.removeAllItems();
						Object itemId1 = selectMarker1.addItem();
						selectMarker1.setItemCaption(itemId1, "Select Line-1");
						selectMarker1.setValue(itemId1);
					}
					//System.out.println("^^^^^^^^^^^^^^  :"+listOfNamesForLine1);
					if (null != listOfNamesForLine1){
						for (Name name : listOfNamesForLine1) {
							if(gNames.contains(name.getNval())){
								gNamesDup.add(name.getNval().toString());
							}
							gNames.add(name.getNval());
						}
						for (Name name : listOfNamesForLine1) {
							
							if(gNamesDup.contains(name.getNval())){
								germplasmList1.add(name.getNval().toString()+"("+name.getGermplasmId()+")");
							}else{
								germplasmList1.add(name.getNval().toString());
							}
						}
					}else{
						String strErrorMessage = "No Mapping datasets available";
						_mainHomePage.getMainWindow().getWindow().showNotification(strErrorMessage, Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					//germplasmList
					//System.out.println(germplasmList1.size()+"germplasmList1:"+germplasmList1);
					Collections.sort(germplasmList1);
					//System.out.println("germplasmList1.size():"+germplasmList1.size());
					for(int g=0;g<germplasmList1.size();g++){
						//System.out.println("....:"+germplasmList1.get(g));						
						selectMarker1.addItem(germplasmList1.get(g));
					}
					
				} catch (GDMSException e1) {
					String strErrorMessage = "Error retrieving data for Line-1.";
					if (null != e1.getExceptionMessage()){
						strErrorMessage = e1.getExceptionMessage();
					}
					_mainHomePage.getMainWindow().getWindow().showNotification(strErrorMessage, Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});
		optiongroup.select(0);
		
		
		Label lblPolymorphicType = new Label("Polymorphic Type:");
		lblPolymorphicType.setStyleName(Reindeer.LABEL_H2);

		HorizontalLayout horizLytForOptionGroup = new HorizontalLayout();
		horizLytForOptionGroup.addComponent(lblPolymorphicType);
		horizLytForOptionGroup.addComponent(optiongroup);
		horizLytForOptionGroup.setWidth("500px");
		horizLytForOptionGroup.setMargin(true, false, true, true);
		
		Label lblSearchConditions = new Label("Search Conditions");
		lblSearchConditions.setStyleName(Reindeer.LABEL_H2);
		selectLinesLayout.addComponent(lblSearchConditions);
		selectLinesLayout.setComponentAlignment(lblSearchConditions, Alignment.TOP_CENTER);
		selectLinesLayout.addComponent(horizLytForOptionGroup);
		
		
		Label lblAnd = new Label("and");
		lblAnd.setStyleName(Reindeer.LABEL_H2);

		
		if (null != listOfNamesForLine1){
			for (Name name : listOfNamesForLine1) {
				selectMarker1.addItem(name.getNval());
			}
		}
		
	
		final ComboBox selectMarker2 = new ComboBox();
		Object itemId2 = selectMarker2.addItem();
		selectMarker2.setItemCaption(itemId2, "Select Line-2");
		selectMarker2.setValue(itemId2);
		selectMarker2.setNullSelectionAllowed(false);
		selectMarker2.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
		
		selectMarker1.addListener(new Property.ValueChangeListener() {
			final ArrayList<String> germplasmList2=new ArrayList<String>();
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
				String strSelectedNameValue = "";
				//System.out.println("%%%%%%%%%%%%%%  :"+selectMarker1.getValue());
				if (null != selectMarker1.getValue()) {
					strSelectedNameValue = selectMarker1.getValue().toString();
				}else{
					/*_mainHomePage.getMainWindow().getWindow().showNotification("Please select Line-1.", Notification.TYPE_ERROR_MESSAGE);
					return;*/
				}
				if(strSelectedNameValue.contains("(")){
					strSelectedNameValue=strSelectedNameValue.substring(0, strSelectedNameValue.lastIndexOf("("));
				}
				
				Integer iGermplasmId = 0;

				if (null != listOfNamesForLine2) {
					listOfNamesForLine2.clear();
				}
				if (null != listOfNamesForLine1){
				for (Name name : listOfNamesForLine1) {
				
					if (name.getNval().equals(strSelectedNameValue)){
						iGermplasmId = name.getGermplasmId();
						System.out.println("   ,,,,,,,,,,,,,   line 1 selected:"+strSelectedNameValue);
						////System.out.println("%%%%%%%%%%%%%%%%%%%%%% option seleted:"+strSelectedPolymorphicType);
						try {
							//listOfNamesForLine2 = retrievePolymorphicMarker.getNames(iGermplasmId);
							listOfNamesForLine2 = retrievePolymorphicMarker.getNames(strSelectedNameValue, strSelectedPolymorphicType);
							break;
						} catch (GDMSException e) {
							String strErrorMessage = "Error retrieving data for Line-2.";
							if (null != e.getExceptionMessage()){
								strErrorMessage = e.getExceptionMessage();
							}
							_mainHomePage.getMainWindow().getWindow().showNotification(strErrorMessage, Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}
				}
				}
				if (null != selectMarker2 && 0 != selectMarker2.getItemIds().size()) {
					selectMarker2.removeAllItems();
					Object itemId2 = selectMarker2.addItem();
					selectMarker2.setItemCaption(itemId2, "Select Line-2");
					selectMarker2.setValue(itemId2);
				}
				List gNamesDup=new ArrayList<String>();
				List gNames=new ArrayList<String>();
				if (null != listOfNamesForLine2){
					
					for (Name name2 : listOfNamesForLine1) {
						if(gNames.contains(name2.getNval())){
							gNamesDup.add(name2.getNval().toString());
						}
						gNames.add(name2.getNval());
					}
					for (Name name2 : listOfNamesForLine1) {
						String nval = name2.getNval();
						if (false == nval.equals(strSelectedNameValue)) {
							if(gNamesDup.contains(name2.getNval())){
								germplasmList2.add(name2.getNval().toString()+"("+name2.getGermplasmId()+")");
							}else{
								germplasmList2.add(name2.getNval().toString());
							}
						}
					}
					
					Collections.sort(germplasmList2);
					
					for(int g=0;g<germplasmList2.size();g++){
						selectMarker2.addItem(germplasmList2.get(g));
					}
				}
			}
		});

		
		HorizontalLayout horizontalLayoutForSelectComponents = new HorizontalLayout();
		horizontalLayoutForSelectComponents.addComponent(selectMarker1);
		horizontalLayoutForSelectComponents.addComponent(lblAnd);
		horizontalLayoutForSelectComponents.addComponent(selectMarker2);
		horizontalLayoutForSelectComponents.setWidth("500px");
		horizontalLayoutForSelectComponents.setMargin(true, false, true, true);
		selectLinesLayout.addComponent(horizontalLayoutForSelectComponents);
		
		
		VerticalLayout layoutForButton = new VerticalLayout();
		Button btnNext = new Button("Next");
		layoutForButton.addComponent(btnNext);
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		selectLinesLayout.addComponent(layoutForButton);
		selectLinesLayout.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				/*String strLine1="";
				String strLine2="";*/
				if (null == optiongroup.getValue()) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select a valid polymorphic type.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				//selectMarker1.
				
				Object lineOneValue = selectMarker1.getValue();
			     if (lineOneValue instanceof Integer){
			      Integer itemId = (Integer)lineOneValue;			      
			      if (itemId.equals(2)){			    	
			    	  _mainHomePage.getMainWindow().getWindow().showNotification("Please select Line-1.", Notification.TYPE_ERROR_MESSAGE);
						return;
			      } 
			     }
			     Object lineTwoValue = selectMarker2.getValue();
			     if (lineTwoValue instanceof Integer){
			      Integer itemId = (Integer)lineTwoValue;			     
			      if (itemId.equals(4)){			    	
			    	  _mainHomePage.getMainWindow().getWindow().showNotification("Please select Line-2.", Notification.TYPE_ERROR_MESSAGE);
						return;
			      } 
			     }
				
				strSelectedPolymorphicType = optiongroup.getValue().toString();
				//System.out.println("polymorphic type="+strSelectedPolymorphicType);
				ArrayList<String> listOfGermplasmNames = new ArrayList<String>();
				
				ArrayList<String> listOfLine1 = new ArrayList<String>();
				ArrayList<String> listOfLine2 = new ArrayList<String>();
				List<Integer> listofGIDs1=new ArrayList<Integer>();
				List<Integer> listofGIDs2=new ArrayList<Integer>();
				
				boolean bValidLine1 = false;				
				strLine1 = selectMarker1.getValue().toString();
					listOfGermplasmNames.add(strLine1);
					if(strLine1.contains("(")){
						listOfLine1.add(strLine1.substring(0, strLine1.lastIndexOf("(")));
						String gid=strLine1.substring(strLine1.lastIndexOf("(")+1);
						listofGIDs1.add(Integer.parseInt(gid.substring(0, gid.length()-1 )));
					}else{
						listOfLine1.add(strLine1);
					}
				
				
				strLine2 = selectMarker2.getValue().toString();
				boolean bValidLine2 = false;
				
					listOfGermplasmNames.add(strLine2);
					if(strLine2.contains("(")){
						listOfLine2.add(strLine2.substring(0, strLine2.lastIndexOf("(")));
						String gid=strLine2.substring(strLine2.lastIndexOf("(")+1);
						listofGIDs2.add(Integer.parseInt(gid.substring(0, gid.length()-1 )));
					}else{
						listOfLine2.add(strLine2);
					}
				
				//System.out.println("listOfLine1:"+listOfLine1+"    listOfLine2:"+listOfLine2);
				if (0 == listOfGermplasmNames.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select valid lines to be viewed on Map and click Next", Notification.TYPE_ERROR_MESSAGE);
					return;
				} else if (2 != listOfGermplasmNames.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select valid lines to be viewed on Map and click Next", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				/*GermplasmDataManagerImpl germplasmDataManagerImpl = new GermplasmDataManagerImpl();
				germplasmDataManagerImpl.setSessionProviderForLocal(GDMSModel.getGDMSModel().getHibernateSessionProviderForLocal());
				germplasmDataManagerImpl.setSessionProviderForCentral(GDMSModel.getGDMSModel().getHibernateSessionProviderForCentral());*/
				GermplasmDataManager manager = factory.getGermplasmDataManager();
				GenotypicDataManager genoManager=factory.getGenotypicDataManager();
				
				/*ArrayList<Integer> listOfNids = new ArrayList<Integer>();
				String gidsList1="";
				String gidsList2="";
				String gidsList="";*/
				boolean bDataRetrievedForNextTab = false;
				try {
					ArrayList<Integer> listofGIDs = new ArrayList<Integer>();			
					if(listofGIDs1.isEmpty()){
						List<GermplasmNameDetails> gidAndNidByGermplasmNamesLine1=manager.getGermplasmNameDetailsByGermplasmNames(listOfLine1,GetGermplasmByNameModes.NORMAL);
						//System.out.println("%%%%%%%%%%%%%%%%%%%   :"+gidAndNidByGermplasmNamesLine1);
						if (null != gidAndNidByGermplasmNamesLine1){
							for(GermplasmNameDetails germNames1:gidAndNidByGermplasmNamesLine1){
								if (false == listofGIDs1.contains(germNames1.getGermplasmId())){
									listofGIDs1.add(germNames1.getGermplasmId());
									//gidsList1=gidsList1+germNames1.getGermplasmId()+",";
								}
							}					
						}					
					}
					if(listofGIDs2.isEmpty()){
						List<GermplasmNameDetails> gidAndNidByGermplasmNamesLine2=manager.getGermplasmNameDetailsByGermplasmNames(listOfLine2,GetGermplasmByNameModes.NORMAL);
						//System.out.println("%%%%%%%%%%%,,,,,,,,,,,,,,,,,%%%%%%%%   :"+gidAndNidByGermplasmNamesLine2);
						if (null != gidAndNidByGermplasmNamesLine2){
							for(GermplasmNameDetails germNames2:gidAndNidByGermplasmNamesLine2){
								if (false == listofGIDs2.contains(germNames2.getGermplasmId())){
									listofGIDs2.add(germNames2.getGermplasmId());
									//gidsList2=gidsList2+germNames2.getGermplasmId()+",";
								}
							}					
						}
					}
					/*System.out.println("listofGIDs1:"+listofGIDs1);
					System.out.println("listofGIDs2:"+listofGIDs2);*/
					Integer strGid1=0;
					List<Integer> listGid1=new ArrayList<Integer>();
					List<Integer> listGid2=new ArrayList<Integer>();					
					
					List <AccMetadataSet> res1=genoManager.getGdmsAccMetadatasetByGid(listofGIDs1, 0, (int)genoManager.countGdmsAccMetadatasetByGid(listofGIDs1));
					System.out.println("res1:"+res1);
					for(AccMetadataSet resLine1:res1){
						strGid1=resLine1.getGermplasmId();
						if(!listGid1.contains(resLine1.getGermplasmId()))
							listGid1.add(resLine1.getGermplasmId());
					}

					Integer strGid2=0;
					List <AccMetadataSet> res2=genoManager.getGdmsAccMetadatasetByGid(listofGIDs2, 0, (int)genoManager.countGdmsAccMetadatasetByGid(listofGIDs2));
					System.out.println("res2:"+res2);
					for(AccMetadataSet resLine2:res2){
						strGid2=resLine2.getGermplasmId();
						if(!listGid2.contains(resLine2.getGermplasmId()))
							listGid2.add(resLine2.getGermplasmId());
					}
					for(int l=0;l<listGid1.size();l++){
						listofGIDs.add(listGid1.get(l));
					}
					for(int l=0;l<listGid2.size();l++){
						listofGIDs.add(listGid2.get(l));
					}
					
					
					/*listofGIDs.add(strGid1);
					listofGIDs.add(strGid2);*/
					
					HashMap<String, String> hmapAlleleValues1=new HashMap<String, String>();
					HashMap<String, String> hmapAlleleValues2=new HashMap<String, String>();

					List<String> listCommonMarkers=new ArrayList<String>();
					
					List<String> listCommonMarkers1=new ArrayList<String>();
					List<String> listCommonMarkers2=new ArrayList<String>();
					listofMarkers=new ArrayList<String>();
					missingList=new ArrayList<String>();
					if (strSelectedPolymorphicType.equalsIgnoreCase("Mapping")) {
						List<String> listMark1=new ArrayList<String>();
						List<String> markerList1=new ArrayList<String>();
						List<String> listMark2=new ArrayList<String>();
						List<String> markerList2=new ArrayList<String>();
						List<AllelicValueElement> listMappingPopValuesLine1=genoManager.getMappingAlleleValuesForPolymorphicMarkersRetrieval(listGid1, 0, (int)genoManager.countMappingAlleleValuesForPolymorphicMarkersRetrieval(listGid1));
						//System.out.println("listAlleleValuesLine1:"+listMappingPopValuesLine1);
						for (AllelicValueElement mappingPopValueElement1 : listMappingPopValuesLine1){								
							listMark1.add(mappingPopValueElement1.getGid()+"!~!"+mappingPopValueElement1.getAccSampleId()+"!~!"+mappingPopValueElement1.getDatasetId()+"!~!"+mappingPopValueElement1.getMarkerName()+"!~!"+mappingPopValueElement1.getData());
							if(!markerList1.contains(mappingPopValueElement1.getMarkerName()))
								markerList1.add(mappingPopValueElement1.getMarkerName());
						}
						/*System.out.println("hmapAlleleValues1:"+hmapAlleleValues1);
						System.out.println("hmapAlleleValues2:"+hmapAlleleValues2);*/
						List<AllelicValueElement> listMappingPopValuesLine2=genoManager.getMappingAlleleValuesForPolymorphicMarkersRetrieval(listGid2, 0, (int)genoManager.countMappingAlleleValuesForPolymorphicMarkersRetrieval(listGid2));
						//System.out.println("listAlleleValuesLine2:"+listMappingPopValuesLine2);
						for (AllelicValueElement mappingPopValueElement2 : listMappingPopValuesLine2){
							listMark2.add(mappingPopValueElement2.getGid()+"!~!"+mappingPopValueElement2.getAccSampleId()+"!~!"+mappingPopValueElement2.getDatasetId()+"!~!"+mappingPopValueElement2.getMarkerName()+"!~!"+mappingPopValueElement2.getData());
							if(!markerList2.contains(mappingPopValueElement2.getMarkerName()))
								markerList2.add(mappingPopValueElement2.getMarkerName());
						}
						hmapAlleleValues1=new HashMap<String, String>();
						hmapAlleleValues2=new HashMap<String, String>();
						listCommonMarkers1=new ArrayList<String>();
						listCommonMarkers2=new ArrayList<String>();
						int listSize1=listMark1.size();
						int size1=listSize1-1;
						for(int d=0;d<size1;d++){
							String[] strDataLine1=listMark1.get(d).toString().split("!~!");
							String data=strDataLine1[4];
							String marker=strDataLine1[3];
							
							if(size1>markerList1.size()){
								if(d<size1){
									String[] strDataLine=listMark1.get(d+1).toString().split("!~!");
									if(marker.equalsIgnoreCase(strDataLine[3])){
										if(strDataLine[4].equalsIgnoreCase(data)){
											listCommonMarkers1.add(strDataLine1[3]);
											hmapAlleleValues1.put(strDataLine1[3], strDataLine1[4]);
										}									
									}
								}
							}else{
								listCommonMarkers1.add(marker);
								hmapAlleleValues1.put(marker, data);
							}
							
						}
						/*System.out.println("listCommonMarkers:"+listCommonMarkers1);
						System.out.println("hmapAlleleValues1:"+hmapAlleleValues1);
						*/
						int listSize2=listMark2.size();
						int size2=listSize2-1;
						for(int d=0;d<size2;d++){
							String[] strDataLine1=listMark2.get(d).toString().split("!~!");
							String data=strDataLine1[4];
							String marker=strDataLine1[3];
							//System.out.println("d:"+d);
							
							if(size2>markerList2.size()){
								if(d<size2){
									String[] strDataLine=listMark2.get(d+1).toString().split("!~!");
									//System.out.println(strDataLine[3]+"   "+marker);
									if(marker.equalsIgnoreCase(strDataLine[3])){
										//System.out.println("IF$$$$$$$$$$$$$$$$$$<<<<<<<<<<<<<<<<<");
										if(strDataLine[4].equalsIgnoreCase(data)){
											listCommonMarkers2.add(strDataLine1[3]);
											hmapAlleleValues2.put(strDataLine1[3], strDataLine1[4]);
										}									
									}
								}
							}else{
								listCommonMarkers2.add(marker);
								hmapAlleleValues2.put(marker, data);
							}
							//System.out.println(d);
						}
						if(listCommonMarkers2.size()>listCommonMarkers1.size()){
							for(int l1=0;l1<listCommonMarkers2.size();l1++){
								if(listCommonMarkers1.contains(listCommonMarkers2.get(l1)))
									listCommonMarkers.add(listCommonMarkers2.get(l1));
							}
							
						}else if(listCommonMarkers1.size()>listCommonMarkers2.size()){
							for(int l2=0;l2<listCommonMarkers1.size();l2++){
								if(listCommonMarkers2.contains(listCommonMarkers1.get(l2)))
									listCommonMarkers.add(listCommonMarkers1.get(l2));
							}
						}else if(listCommonMarkers1.size()==listCommonMarkers2.size()){
							for(int l1=0;l1<listCommonMarkers1.size();l1++){
								listCommonMarkers.add(listCommonMarkers1.get(l1));
							}
						}
						System.out.println("listCommonMarkers2:"+listCommonMarkers2);
						System.out.println("hmapAlleleValues2:"+hmapAlleleValues2);
//						System.out.println();
						listofMarkers=new ArrayList<String>();
						for(int m=0;m<listCommonMarkers.size();m++){
							
							if((!(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("N")||hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("?")||hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("-")))&&(!(hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("N")||hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("?")||hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("-")))&&(! hmapAlleleValues1.get(listCommonMarkers.get(m)).equals(hmapAlleleValues2.get(listCommonMarkers.get(m)))))
								listofMarkers.add(listCommonMarkers.get(m).toString());
							
							if((hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("?"))||(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("?"))||(hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("N"))||(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("N"))||(hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("-"))||(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("-"))){
								if(!missingList.contains(listCommonMarkers.get(m).toString())){
									missingList.add(listCommonMarkers.get(m).toString());
								}
							}
							/*if(! hmapAlleleValues1.get(listCommonMarkers.get(m)).equals(hmapAlleleValues2.get(listCommonMarkers.get(m))))
								listofMarkers.add(listCommonMarkers.get(m).toString());*/
						}
						System.out.println("Polymorphic markers:"+listofMarkers);
						
					}else{
						int charCount=(int)genoManager.countCharAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs);
						int alleleCount=(int)genoManager.countIntAlleleValuesForPolymorphicMarkersRetrieval(listofGIDs);
						System.out.println("listofGIDs:"+listofGIDs);
						if(charCount>0){							
							List<String> listMark1=new ArrayList<String>();
							List<String> markerList1=new ArrayList<String>();
							List<String> listMark2=new ArrayList<String>();
							List<String> markerList2=new ArrayList<String>();
							
							List<AllelicValueElement> listCharValuesLine1=genoManager.getCharAlleleValuesForPolymorphicMarkersRetrieval(listGid1, 0, (int)genoManager.countCharAlleleValuesForPolymorphicMarkersRetrieval(listGid1));
							System.out.println("listAlleleValuesLine1:"+listCharValuesLine1);
							for (AllelicValueElement charValueElement1 : listCharValuesLine1){
								if(charValueElement1.getAccSampleId()!=null)
									listMark1.add(charValueElement1.getGid()+"!~!"+charValueElement1.getAccSampleId()+"!~!"+charValueElement1.getDatasetId()+"!~!"+charValueElement1.getMarkerName()+"!~!"+charValueElement1.getData());
								else
									listMark1.add(charValueElement1.getGid()+"!~!"+"1"+"!~!"+charValueElement1.getDatasetId()+"!~!"+charValueElement1.getMarkerName()+"!~!"+charValueElement1.getData());
								if(!markerList1.contains(charValueElement1.getMarkerName()))
									markerList1.add(charValueElement1.getMarkerName());
							}
							/*System.out.println("hmapAlleleValues1:"+hmapAlleleValues1);
							System.out.println("hmapAlleleValues2:"+hmapAlleleValues2);*/
							List<AllelicValueElement> listCharValuesLine2=genoManager.getCharAlleleValuesForPolymorphicMarkersRetrieval(listGid2, 0, (int)genoManager.countCharAlleleValuesForPolymorphicMarkersRetrieval(listGid2));
							System.out.println("listAlleleValuesLine2:"+listCharValuesLine2);
							for (AllelicValueElement charValueElement2 : listCharValuesLine2){
								if(charValueElement2.getAccSampleId()!=null)
									listMark2.add(charValueElement2.getGid()+"!~!"+charValueElement2.getAccSampleId()+"!~!"+charValueElement2.getDatasetId()+"!~!"+charValueElement2.getMarkerName()+"!~!"+charValueElement2.getData());
								else
									listMark2.add(charValueElement2.getGid()+"!~!"+"1"+"!~!"+charValueElement2.getDatasetId()+"!~!"+charValueElement2.getMarkerName()+"!~!"+charValueElement2.getData());
								if(!markerList2.contains(charValueElement2.getMarkerName()))
									markerList2.add(charValueElement2.getMarkerName());
							}
							hmapAlleleValues1=new HashMap<String, String>();
							hmapAlleleValues2=new HashMap<String, String>();
							listCommonMarkers1=new ArrayList<String>();
							listCommonMarkers2=new ArrayList<String>();
							
							System.out.println("............... :"+listMark1.size()+" listMark1:"+listMark1);
							System.out.println(listMark2.size()+" listMark2:"+listMark2);
							int listSize1=listMark1.size();
							//int size1=listSize1-1;
							for(int d=0;d<listSize1;d++){
								String[] strDataLine1=listMark1.get(d).toString().split("!~!");
								//System.out.println(d+":"+listMark1.get(d));
								String data=strDataLine1[4];
								String marker=strDataLine1[3];
								//System.out.println(size1+">"+markerList1.size()+"d:"+d+"  "+data+"   "+marker);
								
								if(listSize1>markerList1.size()){
									if(d<listSize1){
										String[] strDataLine=listMark1.get(d+1).toString().split("!~!");
										//System.out.println(d+1+":"+listMark1.get(d+1));
										//System.out.println(strDataLine[3]+"   "+marker);
										if(marker.equalsIgnoreCase(strDataLine[3])){
											//System.out.println("IF$$$$$$$$$$$$$$$$$$<<<<<<<<<<<<<<<<<");
											if(strDataLine[4].equalsIgnoreCase(data)){
												listCommonMarkers1.add(strDataLine1[3]);
												hmapAlleleValues1.put(strDataLine1[3], strDataLine1[4]);
											}									
										}
									}
								}else{
									listCommonMarkers1.add(marker);
									hmapAlleleValues1.put(marker, data);
								}
								//System.out.println(d);
							}
							//System.out.println("listCommonMarkers:"+listCommonMarkers1);
							System.out.println("hmapAlleleValues1:"+hmapAlleleValues1);
							
							int listSize2=listMark2.size();
							//int size2=listSize2-1;
							//System.out.println("size2:"+size2);
							for(int d=0;d<listSize2;d++){
								String[] strDataLine1=listMark2.get(d).toString().split("!~!");
								String data=strDataLine1[4];
								String marker=strDataLine1[3];
								//System.out.println("d:"+d);
								
								if(listSize2>markerList2.size()){
									if(d<listSize2){
										String[] strDataLine=listMark2.get(d+1).toString().split("!~!");
										//System.out.println(strDataLine[3]+"   "+marker);
										if(marker.equalsIgnoreCase(strDataLine[3])){
											//System.out.println("IF$$$$$$$$$$$$$$$$$$<<<<<<<<<<<<<<<<<");
											if(strDataLine[4].equalsIgnoreCase(data)){
												listCommonMarkers2.add(strDataLine1[3]);
												hmapAlleleValues2.put(strDataLine1[3], strDataLine1[4]);
											}									
										}
									}
								}else{
									//System.out.println("..................");
									listCommonMarkers2.add(marker);
									hmapAlleleValues2.put(marker, data);
								}
								//System.out.println(d);
							}
							listCommonMarkers=new ArrayList<String>();
							if(listCommonMarkers2.size()>listCommonMarkers1.size()){
								for(int l1=0;l1<listCommonMarkers2.size();l1++){
									if(listCommonMarkers1.contains(listCommonMarkers2.get(l1)))
										listCommonMarkers.add(listCommonMarkers2.get(l1));
								}
								
							}else if(listCommonMarkers1.size()>listCommonMarkers2.size()){
								for(int l2=0;l2<listCommonMarkers1.size();l2++){
									if(listCommonMarkers2.contains(listCommonMarkers1.get(l2)))
										listCommonMarkers.add(listCommonMarkers1.get(l2));
								}
							}else if(listCommonMarkers1.size()==listCommonMarkers2.size()){
								for(int l1=0;l1<listCommonMarkers1.size();l1++){
									listCommonMarkers.add(listCommonMarkers1.get(l1));
								}
							}
						
							//System.out.println("hmapAlleleValues1:"+hmapAlleleValues1);
							System.out.println("hmapAlleleValues2:"+hmapAlleleValues2);
							System.out.println("listCommonMarkers:"+listCommonMarkers);
//							System.out.println();
							listofMarkers=new ArrayList<String>();
							for(int m=0;m<listCommonMarkers.size();m++){
								if((!(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("N")||hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("?")||hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("-")))&&(!(hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("N")||hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("?")||hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("-")))&&(! hmapAlleleValues1.get(listCommonMarkers.get(m)).equals(hmapAlleleValues2.get(listCommonMarkers.get(m)))))
									listofMarkers.add(listCommonMarkers.get(m).toString());
								
								if((hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("?"))||(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("?"))||(hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("N"))||(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("N"))||(hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("-"))||(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("-"))){
									if(!missingList.contains(listCommonMarkers.get(m).toString())){
										missingList.add(listCommonMarkers.get(m).toString());
									}
								}
								
							}
							System.out.println("Polymorphic markers:"+listofMarkers);
							
						}
						/*System.out.println("!!!!!!!!!!!!!!!!!!!!!!  :"+listGid1);
						System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@   :"+listGid2);*/
						if(alleleCount>0){	
							List<String> listMark1=new ArrayList<String>();
							List<String> markerList1=new ArrayList<String>();
							List<String> listMark2=new ArrayList<String>();
							List<String> markerList2=new ArrayList<String>();
							
							List<AllelicValueElement> listAlleleValuesLine1=genoManager.getIntAlleleValuesForPolymorphicMarkersRetrieval(listGid1, 0, (int)genoManager.countIntAlleleValuesForPolymorphicMarkersRetrieval(listGid1));
							System.out.println("listAlleleValuesLine1:"+listAlleleValuesLine1);
							for (AllelicValueElement allelicValueElement1 : listAlleleValuesLine1){								
								listMark1.add(allelicValueElement1.getGid()+"!~!"+allelicValueElement1.getAccSampleId()+"!~!"+allelicValueElement1.getDatasetId()+"!~!"+allelicValueElement1.getMarkerName()+"!~!"+allelicValueElement1.getData());
								if(!markerList1.contains(allelicValueElement1.getMarkerName()))
									markerList1.add(allelicValueElement1.getMarkerName());
							}
							/*System.out.println("hmapAlleleValues1:"+hmapAlleleValues1);
							System.out.println("hmapAlleleValues2:"+hmapAlleleValues2);*/
							List<AllelicValueElement> listAlleleValuesLine2=genoManager.getIntAlleleValuesForPolymorphicMarkersRetrieval(listGid2, 0, (int)genoManager.countIntAlleleValuesForPolymorphicMarkersRetrieval(listGid2));
							System.out.println("listAlleleValuesLine2:"+listAlleleValuesLine2);
							for (AllelicValueElement allelicValueElement2 : listAlleleValuesLine2){
								listMark2.add(allelicValueElement2.getGid()+"!~!"+allelicValueElement2.getAccSampleId()+"!~!"+allelicValueElement2.getDatasetId()+"!~!"+allelicValueElement2.getMarkerName()+"!~!"+allelicValueElement2.getData());
								if(!markerList2.contains(allelicValueElement2.getMarkerName()))
									markerList2.add(allelicValueElement2.getMarkerName());
							}
							
							hmapAlleleValues1=new HashMap<String, String>();
							hmapAlleleValues2=new HashMap<String, String>();
							listCommonMarkers1=new ArrayList<String>();
							listCommonMarkers2=new ArrayList<String>();
							System.out.println(listMark1.size()+" listMark1:"+listMark1);
							System.out.println(listMark2.size()+" listMark2:"+listMark2);
							int listSize1=listMark1.size();
							//int size1=listSize1-1;
							for(int d=0;d<listSize1;d++){
								String[] strDataLine1=listMark1.get(d).toString().split("!~!");
								String data=strDataLine1[4];
								String marker=strDataLine1[3];
								//System.out.println("d:"+d);
								
								if(listSize1>markerList1.size()){
									if(d<listSize1){
										String[] strDataLine=listMark1.get(d+1).toString().split("!~!");
										//System.out.println(strDataLine[3]+"   "+marker+"    "+strDataLine[4]+"  "+data);
										if(marker.equalsIgnoreCase(strDataLine[3])){
											//System.out.println("IF$$$$$$$$$$$$$$$$$$<<<<<<<<<<<<<<<<<");
											if(strDataLine[4].equalsIgnoreCase(data)){
												listCommonMarkers1.add(strDataLine1[3]);
												hmapAlleleValues1.put(strDataLine1[3], strDataLine1[4]);
											}									
										}
									}
								}else{
									listCommonMarkers1.add(marker);
									hmapAlleleValues1.put(marker, data);
								}
								//System.out.println(d);
							}
							
							System.out.println("INT hmapAlleleValues1:"+hmapAlleleValues1);
							int listSize2=listMark2.size();
							//int size2=listSize2-1;
							for(int d=0;d<listSize2;d++){
								String[] strDataLine1=listMark2.get(d).toString().split("!~!");
								String data=strDataLine1[4];
								String marker=strDataLine1[3];
								//System.out.println("d:"+d);
								
								if(listSize2>markerList2.size()){
									if(d<listSize2){
										String[] strDataLine=listMark2.get(d+1).toString().split("!~!");
										//System.out.println(strDataLine[3]+"   "+marker);
										if(marker.equalsIgnoreCase(strDataLine[3])){
											//System.out.println("IF$$$$$$$$$$$$$$$$$$<<<<<<<<<<<<<<<<<");
											if(strDataLine[4].equalsIgnoreCase(data)){
												listCommonMarkers2.add(strDataLine1[3]);
												hmapAlleleValues2.put(strDataLine1[3], strDataLine1[4]);
											}									
										}
									}
								}else{
									listCommonMarkers2.add(marker);
									hmapAlleleValues2.put(marker, data);
								}
								//System.out.println(d);
							}
							listCommonMarkers=new ArrayList<String>();
							if(listCommonMarkers2.size()>listCommonMarkers1.size()){
								for(int l1=0;l1<listCommonMarkers2.size();l1++){
									if(listCommonMarkers1.contains(listCommonMarkers2.get(l1)))
										listCommonMarkers.add(listCommonMarkers2.get(l1));
								}
								
							}else if(listCommonMarkers1.size()>listCommonMarkers2.size()){
								for(int l2=0;l2<listCommonMarkers1.size();l2++){
									if(listCommonMarkers2.contains(listCommonMarkers1.get(l2)))
										listCommonMarkers.add(listCommonMarkers1.get(l2));
								}
							}else if(listCommonMarkers1.size()==listCommonMarkers2.size()){
								for(int l1=0;l1<listCommonMarkers1.size();l1++){
									listCommonMarkers.add(listCommonMarkers1.get(l1));
								}
							}
							
							System.out.println("INT hmapAlleleValues2:"+hmapAlleleValues2);
							System.out.println("listCommonMarkers:"+listCommonMarkers);
							
//							System.out.println();
							
							for(int m=0;m<listCommonMarkers.size();m++){
								if((!(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("N")||hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("?")||hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("-")))&&(!(hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("N")||hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("?")||hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("-")))&&(! hmapAlleleValues1.get(listCommonMarkers.get(m)).equals(hmapAlleleValues2.get(listCommonMarkers.get(m)))))
									listofMarkers.add(listCommonMarkers.get(m).toString());
								
								if((hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("?"))||(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("?"))||(hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("N"))||(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("N"))||(hmapAlleleValues1.get(listCommonMarkers.get(m)).equals("-"))||(hmapAlleleValues2.get(listCommonMarkers.get(m)).equals("-"))){
									if(!missingList.contains(listCommonMarkers.get(m).toString())){
										missingList.add(listCommonMarkers.get(m).toString());
									}
								}
								/*if(! hmapAlleleValues1.get(listCommonMarkers.get(m)).equals(hmapAlleleValues2.get(listCommonMarkers.get(m))))
									listofMarkers.add(listCommonMarkers.get(m).toString());*/
							}
							System.out.println("Polymorphic markers:"+listofMarkers);							
						}						
					}
					
					
					 
						////System.out.println("listofMarkers:"+listofMarkers);
					bDataRetrievedForNextTab = true;
					
				} catch (MiddlewareQueryException e) {
					bDataRetrievedForNextTab = false;
					_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving list of NIDs and GIDs using Germplasm names.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}

				if (bDataRetrievedForNextTab){
					Component newPolymorphicResultComponent = buildPolymorphicResultComponent();
					_tabsheetForPolymorphicMarkers.replaceComponent(polymorphicResultComponent, newPolymorphicResultComponent);
					polymorphicResultComponent.requestRepaint();
					polymorphicResultComponent = newPolymorphicResultComponent;
					_tabsheetForPolymorphicMarkers.getTab(1).setEnabled(true);
					_tabsheetForPolymorphicMarkers.setSelectedTab(polymorphicResultComponent);
				}
			}
		});
		
		return selectLinesLayout;
	}


	private Component buildPolymorphicResultComponent() {
		VerticalLayout resultsLayoutForPolymorphicMarkers = new VerticalLayout();
		resultsLayoutForPolymorphicMarkers.setCaption("Results");
		resultsLayoutForPolymorphicMarkers.setSpacing(true);
		resultsLayoutForPolymorphicMarkers.setMargin(true, true, true, true);

		int iNumOfMarkers = 0;
		if (null != listofMarkers){
			iNumOfMarkers = listofMarkers.size();
		}
		
		Label lblPolymorphicMarkersFound = new Label(iNumOfMarkers + "  Markers are Polymorphic between '"+strLine1+"' and '"+strLine2+"'");
		lblPolymorphicMarkersFound.setStyleName(Reindeer.LABEL_H2);
		resultsLayoutForPolymorphicMarkers.addComponent(lblPolymorphicMarkersFound);
		resultsLayoutForPolymorphicMarkers.setComponentAlignment(lblPolymorphicMarkersFound, Alignment.TOP_CENTER);
		
		
		//20131112: Tulasi --- Created the tableForMissingMarkers component to display the list of Markers with missing data
		
		int iNumOfMissingMarkers = 0;
		if (null != missingList){
			iNumOfMissingMarkers = missingList.size();
		}
		
		Label lblMissingMarkersFound = new Label(iNumOfMissingMarkers + "  markers are with missing data.");
		lblMissingMarkersFound.setStyleName(Reindeer.LABEL_H2);
		resultsLayoutForPolymorphicMarkers.addComponent(lblMissingMarkersFound);
		resultsLayoutForPolymorphicMarkers.setComponentAlignment(lblMissingMarkersFound, Alignment.TOP_CENTER);
		
		ListSelect listComponentForMissingMarkers = new ListSelect();
		
		if (null != missingList){
			
			//20131205: Tulasi --- Displaying the list of missing markers in a ListSelect component instead of a table
			Object itemId1 = listComponentForMissingMarkers.addItem();
			listComponentForMissingMarkers.setItemCaption(itemId1, "Missing Markers");
			listComponentForMissingMarkers.setValue(itemId1);
			for (Object objMissingMarker : missingList) {
				String strMissingMarker = (String) objMissingMarker;
				listComponentForMissingMarkers.addItem(strMissingMarker);
			}
			listComponentForMissingMarkers.setColumns(10);
			listComponentForMissingMarkers.setNewItemsAllowed(false);
			listComponentForMissingMarkers.setWidth("100%");
			listComponentForMissingMarkers.setNullSelectionAllowed(false);
			listComponentForMissingMarkers.setImmediate(true);
			//20131205: Tulasi --- Displaying the list of missing markers in a ListSelect component instead of a table
			
		}
		//20131112: Tulasi --- Created the tableForMissingMarkers component to display the list of Markers with missing data

		final TwinColSelect selectForMarkers = new TwinColSelect();
		selectForMarkers.setLeftColumnCaption("All Markers");
		selectForMarkers.setRightColumnCaption("Selected Markers");
		selectForMarkers.setNullSelectionAllowed(false);
		selectForMarkers.setInvalidAllowed(false);
		if (null != listofMarkers) {
			for (String strMarker : listofMarkers) {
				selectForMarkers.addItem(strMarker);
			}
		}
		selectForMarkers.setRows(20);
		selectForMarkers.setColumns(25);
		
		selectForMarkers.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                	TwinColSelect colSelect = (TwinColSelect)event.getProperty();
                	Object value = colSelect.getValue();
                	Set<String> hashSet = (Set<String>) value;
                	
                	if (null == listOfMarkersSelected){
                		listOfMarkersSelected = new ArrayList<String>();
                	}
                	
                	for (String string : hashSet) {
						listOfMarkersSelected.add(string);
					}
                	////System.out.println(hashSet);
                }
            }
        });
		selectForMarkers.setImmediate(true);
		////System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$   :"+listOfMarkersSelected);
		
		HorizontalLayout horizLytForSelectComponent = new HorizontalLayout();
		horizLytForSelectComponent.setSizeFull();
		horizLytForSelectComponent.setSpacing(true);
		horizLytForSelectComponent.setMargin(true);
		horizLytForSelectComponent.addComponent(selectForMarkers);
		
		
		final ComboBox selectMap = new ComboBox();
		Object itemId = selectMap.addItem();
		selectMap.setItemCaption(itemId, "Select Map");
		selectMap.setValue(itemId);
		selectMap.setNullSelectionAllowed(false);
		selectMap.setImmediate(true);
		
		
		
		try{
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			
			
		}catch (Exception e){
			e.printStackTrace();
		}
		MapDAO mapDAOLocal = new MapDAO();
		mapDAOLocal.setSession(localSession);
		
		MapDAO mapDAOCentral = new MapDAO();
		mapDAOCentral.setSession(centralSession);

		final ArrayList<String> listOfAllMaps = new ArrayList<String>();
		List<Integer> markerIds=new ArrayList();
		markerIDs=new ArrayList<Integer>();
		try {
			
			//listofMarkers
			if (null != listofMarkers && 0 != listofMarkers.size()){
				
				/*if(null==listOfMarkersSelected){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please move the desired markers to Selected Markers List",  Notification.TYPE_ERROR_MESSAGE);
					return null;
				}*/
				
				List<Marker> markerIDsFromCentral=genoManager.getMarkersByMarkerNames(listofMarkers, 0, listofMarkers.size(), Database.CENTRAL);
				List<Marker> markerIDsFromLocal=genoManager.getMarkersByMarkerNames(listofMarkers, 0, listofMarkers.size(), Database.LOCAL);
				//genoManager.getMapAndMarkerCountByMarkers(markerIDsFromSelectedDataset);
				for(Marker mc:markerIDsFromCentral){
					if(!markerIDs.contains(mc.getMarkerId()))
						markerIDs.add(mc.getMarkerId());
				}
				for(Marker ml:markerIDsFromLocal){
					if(!markerIDs.contains(ml.getMarkerId()))
						markerIDs.add(ml.getMarkerId());
				}
				/*List<Integer> markerIDsFromCentral=genoManager.getMarkerIdsByMarkerNames(listofMarkers, 0, listofMarkers.size(), Database.CENTRAL);
				List<Integer> markerIDsFromLocal=genoManager.getMarkerIdsByMarkerNames(listofMarkers, 0, listofMarkers.size(), Database.LOCAL);
				//genoManager.getMapAndMarkerCountByMarkers(markerIDsFromSelectedDataset);
				for(int mc=0;mc<markerIDsFromCentral.size();mc++){
					if(!markerIDs.contains(markerIDsFromCentral.get(mc)))
						markerIDs.add(markerIDsFromCentral.get(mc));
				}
				for(int ml=0;ml<markerIDsFromLocal.size();ml++){
					if(!markerIDs.contains(markerIDsFromLocal.get(ml)))
						markerIDs.add(markerIDsFromLocal.get(ml));
				}*/
				//System.out.println("markerIDs:"+markerIDs);
				
				
				List<MapDetailElement> details = genoManager.getMapAndMarkerCountByMarkers(markerIDs);
				if (details != null && details.size() > 0) {
		            //Debug.println(0, "FOUND " + details.size() + " records");
		            for (MapDetailElement detail : details) {
		                //Debug.println(0, detail.getMapName() + "-" + detail.getMarkerCount());
		                if (false == listOfAllMaps.contains(detail.getMapName())){
		                	listOfAllMaps.add(detail.getMapName() + "(" + detail.getMarkerCount()+")");
							//hmOfMapNameAndMapId.put(detail.getMapName() + "(" + detail.getMarkerCount()+")", detail.get.getMapId());
						}
		            }
		        } else {
		            //Debug.println(0, "NO RECORDS FOUND");
		        	List<Map> resMapsCentral=genoManager.getAllMaps(0, (int)genoManager.countAllMaps(Database.CENTRAL), Database.CENTRAL);
					if(!resMapsCentral.isEmpty()){
						for (Map map: resMapsCentral){
							if (false == listOfAllMaps.contains(map)){
								listOfAllMaps.add(map.getMapName()+"(0)");
								//hmOfMapNameAndMapId.put(map.getMapName(), map.getMapId());
							}
						}
					}
		        	List<Map> resMapsLocal=genoManager.getAllMaps(0, (int)genoManager.countAllMaps(Database.LOCAL), Database.LOCAL);
					if(resMapsLocal.isEmpty()){
						for (Map map: resMapsLocal){
							if (false == listOfAllMaps.contains(map)){
								listOfAllMaps.add(map.getMapName()+"(0)");
								//hmOfMapNameAndMapId.put(map.getMapName(), map.getMapId());
							}
						}
					}
		        }
			}/*else{
				_mainHomePage.getMainWindow().getWindow().showNotification("Please ",  Notification.TYPE_ERROR_MESSAGE);
				return null;
			}*/
			
		} catch (MiddlewareQueryException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Maps",  Notification.TYPE_ERROR_MESSAGE);
			return null;
		}

		if (null != listOfAllMaps){
			for (int i = 0; i < listOfAllMaps.size(); i++){
				selectMap.addItem(listOfAllMaps.get(i));				
			}
		}

		
		//VerticalLayout layoutForButton = new VerticalLayout();
		HorizontalLayout layoutForButton = new HorizontalLayout();
		Button btnViewOnMap = new Button("View On Map");
		btnViewOnMap.addListener(new Button.ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {

				String strSelectedMap = selectMap.getValue().toString();
				System.out.println("strSelectedMap:"+strSelectedMap);
				if(null==listOfMarkersSelected){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please move the desired markers to Selected Markers List",  Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				if(strSelectedMap==null || strSelectedMap=="" || strSelectedMap.equals("1")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select Map.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				strSelectedMapName = strSelectedMap.substring(0,strSelectedMap.lastIndexOf("("));
				
				////System.out.println("listOfMarkersSelected=:"+listOfMarkersSelected);
				if (null != listOfMarkersSelected){
					retrieveMappingDataBetweenLines();
					//System.out.println("@@@@@@@@@@@@@@@  finalListOfMappingData:"+finalListOfMappingData);
				}

				if (null != finalListOfMappingData){
					if (0 == finalListOfMappingData.size()){
						_mainHomePage.getMainWindow().getWindow().showNotification("Selected Map doesnot contain the markers selected.", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				}
				
				Component newPolymorphicMapComponent = buildPolymorphicMapComponent();
				_tabsheetForPolymorphicMarkers.replaceComponent(buildPolymorphicMapComponent, newPolymorphicMapComponent);
				buildPolymorphicMapComponent.requestRepaint();
				buildPolymorphicMapComponent = newPolymorphicMapComponent;
				_tabsheetForPolymorphicMarkers.getTab(2).setEnabled(true);
				_tabsheetForPolymorphicMarkers.setSelectedTab(buildPolymorphicMapComponent);
			}
		});		
		
		if (0 == selectForMarkers.size()){
			btnViewOnMap.setEnabled(false);
			selectMap.setEnabled(false);
		}

		layoutForButton.addComponent(btnViewOnMap);
		layoutForButton.setComponentAlignment(btnViewOnMap, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		
		//20131206: Tulasi --- Added a new button for KBio order form
		//Button btnViewKBioOrderForm = new Button("View KBio Order Form");
		Button btnViewKBioOrderForm = new Button("Create LGC Genomics Order Form");
		btnViewKBioOrderForm.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				// 20131512 : Kalyani added to create kbio order form
				String mType="SNP";
				ExportFileFormats exportFileFormats = new ExportFileFormats();
				ArrayList <String> markersForKBio=new ArrayList();		
				try {
					List<String> snpMarkers=genoManager.getMarkerNamesByMarkerType(mType, 0, (int)genoManager.countMarkerNamesByMarkerType(mType));
					if(!(snpMarkers.isEmpty())){
						for(int m=0; m<listOfMarkersSelected.size();m++){
							if(snpMarkers.contains(listOfMarkersSelected.get(m))){
								markersForKBio.add(listOfMarkersSelected.get(m));
							}
						}
						if(!(markersForKBio.isEmpty())){
							orderFormForPlymorphicMarkers=exportFileFormats.exportToKBio(markersForKBio, _mainHomePage);
							FileResource fileResource = new FileResource(orderFormForPlymorphicMarkers, _mainHomePage);
							//_mainHomePage.getMainWindow().getWindow().open(fileResource, "", true);
							_mainHomePage.getMainWindow().getWindow().open(fileResource, "LGC Order Form", true);
						}
						
						else{
							_mainHomePage.getMainWindow().getWindow().showNotification("Selected Marker(s) are not SNPs", Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}else{
						_mainHomePage.getMainWindow().getWindow().showNotification("No SNP Marker(s) to create LGC Order form", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					
				} catch (Exception e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error generating LGC Order Form", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});
		
		/*Link gtpExcelFileLink = new Link("Download KBio Order Form", new ExternalResource(""));
		 if (null != orderFormForPlymorphicMarkers){
			gtpExcelFileLink = new Link("Download KBio Order Form", new FileDownloadResource(
					orderFormForPlymorphicMarkers, _mainHomePage.getMainWindow().getWindow().getApplication()));
		}
		gtpExcelFileLink.setTargetName("_blank");*/
		
		layoutForButton.addComponent(btnViewKBioOrderForm);
		layoutForButton.setComponentAlignment(btnViewKBioOrderForm, Alignment.MIDDLE_CENTER);
		layoutForButton.setSpacing(true);
		//20131206: Tulasi --- Added a new button for KBio order form
		if (null != listofMarkers && listofMarkers.size()>0){
			resultsLayoutForPolymorphicMarkers.addComponent(horizLytForSelectComponent);
			resultsLayoutForPolymorphicMarkers.addComponent(selectMap);
			resultsLayoutForPolymorphicMarkers.addComponent(layoutForButton);
			resultsLayoutForPolymorphicMarkers.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);
		
			//20131112: Tulasi --- Added the tableForMissingMarkers component for the final results layout
			resultsLayoutForPolymorphicMarkers.addComponent(lblMissingMarkersFound);
			resultsLayoutForPolymorphicMarkers.setComponentAlignment(lblMissingMarkersFound, Alignment.MIDDLE_CENTER);
			resultsLayoutForPolymorphicMarkers.addComponent(listComponentForMissingMarkers);
			resultsLayoutForPolymorphicMarkers.setComponentAlignment(listComponentForMissingMarkers, Alignment.MIDDLE_CENTER);
			//20131112:  Tulasi --- Added the tableForMissingMarkers component for the final results layout
		}
		if (null == listofMarkers){
			selectMap.setEnabled(false);
			btnViewOnMap.setEnabled(false);
			selectForMarkers.setEnabled(false);
			listComponentForMissingMarkers.setEnabled(false);
		} else {
			selectMap.setEnabled(true);
			btnViewOnMap.setEnabled(true);
			selectForMarkers.setEnabled(true);
			listComponentForMissingMarkers.setEnabled(true);
		}

		return resultsLayoutForPolymorphicMarkers;
	}


	private Component buildPolymorphicMapComponent() {
		VerticalLayout resultsLayoutForPolymorphicMaps = new VerticalLayout();
		resultsLayoutForPolymorphicMaps.setCaption("Map");
		resultsLayoutForPolymorphicMaps.setSpacing(true);
		resultsLayoutForPolymorphicMaps.setMargin(true, true, true, true);

		/*if (null != listofMarkers){
			retrieveMappingDataBetweenLines();
		}*/
		/*if (null != listOfMarkersSelected){
			retrieveMappingDataBetweenLines();
		}*/

		int iTotalPolymorphicMarkers = 0;
		int iMarkersOnMap = 0;
		//System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^   ^|^       :"+finalListOfMappingData);
		
		if (null != finalListOfMappingData){
			//iTotalPolymorphicMarkers = listofMarkers.size();
			iTotalPolymorphicMarkers = listofMarkers.size();
			iMarkersOnMap = markersOnMapList.size();
		}


		Label lblMapsFound = new Label("Out of  " +  iTotalPolymorphicMarkers + "  polymorphic markers only  " + iMarkersOnMap  + "  are on Map");
		lblMapsFound.setStyleName(Reindeer.LABEL_H2);
		resultsLayoutForPolymorphicMaps.addComponent(lblMapsFound);
		resultsLayoutForPolymorphicMaps.setComponentAlignment(lblMapsFound, Alignment.TOP_CENTER);
		
		
		_tableForMarkerResults = new Table();
		_tableForMarkerResults.setWidth("100%");
		_tableForMarkerResults.setPageLength(10);
		_tableForMarkerResults.setSelectable(true);
		_tableForMarkerResults.setColumnCollapsingAllowed(false);
		_tableForMarkerResults.setColumnReorderingAllowed(false);
		_tableForMarkerResults.setStyleName("strong");


		_tableForMarkerResults.addContainerProperty("", CheckBox.class, null);
		_tableForMarkerResults.addContainerProperty("Marker", String.class, null);
		_tableForMarkerResults.addContainerProperty("Map", String.class, null);
		_tableForMarkerResults.addContainerProperty("Chromosome", String.class, null);
		_tableForMarkerResults.addContainerProperty("Position", String.class, null);
		if(hmOfMarkerIDAndQtlTrait!=null && hmOfMarkerIDAndQtlTrait.size()>0){
			_tableForMarkerResults.addContainerProperty("Trait", String.class, null);
		}
		//ArrayList<MappingData> arrayListOfSortedData = sortFinalListOfMappingData();
		
		if (null != finalListOfMappingData){
			_tableForMarkerResults.setEnabled(true);
			arrayOfCheckBoxes = new CheckBox[finalListOfMappingData.size()];
			for (int i = 0; i < listOfMapData.size(); i++){

				//MappingData mappingData = finalListOfMappingData.get(i);
				String[] strArg=listOfMapData.get(i).split("!~!");
				String strLinkageGroup = strArg[2];
				//Integer mapId = mappingData.getMapId();
				String strMapName =  strArg[1];
				//String mapUnit = mappingData.getMapUnit();
				Integer markerId =  Integer.parseInt(strArg[4].toString());
				String markerName = strArg[0].toString();
				float startPosition =Float.parseFloat(strArg[3].toString());
				String strQtlTrait = hmOfMarkerIDAndQtlTrait.get(markerId);
				arrayOfCheckBoxes[i] = new CheckBox();
				if(hmOfMarkerIDAndQtlTrait!=null && hmOfMarkerIDAndQtlTrait.size()>0){
					_tableForMarkerResults.addItem(new Object[] {arrayOfCheckBoxes[i], markerName, strMapName, strLinkageGroup, startPosition, strQtlTrait}, new Integer(i));
				}else{
					_tableForMarkerResults.addItem(new Object[] {arrayOfCheckBoxes[i], markerName, strMapName, strLinkageGroup, startPosition}, new Integer(i));
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

		final TextField txtBinSize = new TextField();
		//txtBinSize.setMaxLength(4);
		txtBinSize.setImmediate(true);
		txtBinSize.setDescription("Bin Size can be max of 4 digits");
		txtBinSize.setTextChangeEventMode(TextChangeEventMode.EAGER);
		//final Hashtable<String, String> htTraitList = new Hashtable<String, String>();
		final HashMap<Integer, String> htTraitList = new HashMap<Integer, String>();
		//String regexp = "[0-9.]{1,4}";
		String regexp = "[0-9.]*";
		final RegexpValidator regexpValidator = new RegexpValidator(regexp, "Not a number");
		regexpValidator.setErrorMessage("Must be a number of max 4 digits");
		txtBinSize.setNullRepresentation("");
		txtBinSize.addValidator(regexpValidator);
		final Property.ValueChangeListener valueChangelistener = new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
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
		            //System.out.println("clicked the TextField");
		            String strValue = txtBinSize.getValue().toString(); 
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
		topHorizontalLayout.addComponent(txtBinSize);

		Label lblCM = new Label("cM");
		lblCM.setStyleName(Reindeer.LABEL_SMALL);
		topHorizontalLayout.addComponent(lblCM);
		
		
		
			checkBox = new CheckBox();
			checkBox.setCaption("Check Trait(s)");
			checkBox.setImmediate(true);		
			selectTrait = new ListSelect();
			
			Object itemId1 = selectTrait.addItem();
			selectTrait.setItemCaption(itemId1, "Select Trait");
			selectTrait.setValue(itemId1);
			selectTrait.setNullSelectionAllowed(false);
			selectTrait.setMultiSelect(true);
			selectTrait.setRows(3);
			int iCount = 2;
			//System.out.println("listofTraits:"+listofTraits);
			if (null != listofTraits){
				for (String strTrait : listofTraits){
					//selectTrait.addItem(strTrait);
					
					selectTrait.addItem(iCount);
					selectTrait.setItemCaption(iCount, strTrait);
					htTraitList.put(iCount, strTrait);
					iCount++;
					
					
				}
			}
		//System.out.println("htTraitList:"+htTraitList);
			
			checkBox.addListener(new ValueChangeListener() {
				private static final long serialVersionUID = 1L;
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
		            		
		                        
		     	               //System.out.println("$$$$$$$$$$$$$$$$$$$>>>>>>>>>>>>>>>>>>>>>>> ............ :"+selectedTraits.get(0).toString());
		     	                if(value){
		     	                	handleChecksWithTraits(checkBox, selectedTraits, htTraitList);
		     	                	//_mainHomePage.getMainWindow().getWindow().showNotification(value+"",  Notification.TYPE_ERROR_MESSAGE);
		     	                }else{
		     	                	
		     	                	String binsize=txtBinSize.getValue().toString();
		     	                	
		     	                	
		     	                	ArrayList markers=new ArrayList();
		     	            		String selectedTrait="";
		     	            		//selMarkerList
		     	            		//System.out.println("...........selectTrait:"+selectTrait+"   "+listOfSelectedTraits);
		     	            		 for(int t=0;t<selectedTraits.size();t++){
		     	            			 ArrayList markersList=new ArrayList();
		     	            			 selectedTrait=htTraitList.get(Integer.parseInt(selectedTraits.get(t).toString()));
		     	            		      //System.out.println("!!!!!!!!!!!!!...............  :"+selectedTrait);
		     	            		      markersList=hashMap1.get(selectedTrait);
		     	            		      for(int m=0;m<markersList.size();m++){
		     	            		    	  if(!markers.contains(markersList.get(m)))
		     	            		    		  markers.add(markersList.get(m));
		     	            		      }
		     	            		 }
		     	            		 
		     	            		 //System.out.println(".......... markers=:"+markers);
		     	            		 
		     	            		 
		     	            		 //arrayOfCheckBoxesForMap= new 
		     	            		//System.out.println("... selMarkerList:"+selMarkerList);
		     	            		for (int ii = 0; ii < finalListOfMappingData.size(); ii++){
		     	            			arrayOfCheckBoxes[ii].setValue(false);
		     	            		}
		     	            		
		     	            		
		     	            		
		     	            		
		     	            		for (int ic = 0; ic < finalListOfMappingData.size(); ic++){            		
		     	            			MappingData mappingData = finalListOfMappingData.get(ic);            			
		     	            			if(selMarkerList.size()>0){            			
		     	            				if(selMarkerList.contains(mappingData.getMarkerId())){           					
		     	            					if(markers.contains(mappingData.getMarkerId())){
		     	            						if((Boolean)arrayOfCheckBoxes[ic].getValue()==false){            							
		     	            							arrayOfCheckBoxes[ic].setValue(true);
		     	            						}
		     	            					
		     	            					}        						
		     	        					}else {
		     	        						arrayOfCheckBoxes[ic].setValue(false);        					
		     	        					}      				
		     	            			}
		     	            		}
		     	            		       		
		     	                	
		     	                      txtBinSize.setValue(binsize);
		     	                      txtBinSize.requestRepaint();
		     	                      layoutForBinSize.requestRepaint();
		     	                }
		            	}
		            
				}
			});
			
			
			if(hmOfMarkerIDAndQtlTrait!=null && hmOfMarkerIDAndQtlTrait.size()>0){	
				topHorizontalLayout.addComponent(selectTrait);
				topHorizontalLayout.addComponent(checkBox);
			}
	//	}
		
		
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
				exportToExcel();
			}
		});
		
		Button btnViewKBioOrderForm = new Button("Create LGC Genomics Order Form");
		btnViewKBioOrderForm.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				// 20131512 : Kalyani added to create kbio order form
				String mType="SNP";
				ExportFileFormats exportFileFormats = new ExportFileFormats();
				ArrayList <String> markersForKBio=new ArrayList();		
				if(null == finalListOfMappingData || 0 == finalListOfMappingData.size()) {
					return;
				}
				List<String[]> listToExport = new ArrayList<String[]>();
				listOfMarkers=new ArrayList();
				for (int i = 0; i < finalListOfMappingData.size(); i++){

					MappingData mappingData = finalListOfMappingData.get(i);	
					if (false == (Boolean) arrayOfCheckBoxes[i].getValue()) {
						continue;
					}
					String markerName = mappingData.getMarkerName();					
					
					//arrayOfCheckBoxes[i] = new CheckBox();
					listToExport.add(new String[] {markerName});
				}
				//System.out.println("listToExport=:"+listToExport);
				for (int i = 0; i < listToExport.size(); i++){
					String[] strings = listToExport.get(i);
					////System.out.println("........................:"+strings);
					for (int j = 0; j < strings.length; j++) {
						if(null != strings[j]) {
							//Label lGID = new Label(j, i,  "");
							//sheet.addCell(lGID);
							////System.out.println("........................:"+strings[j]);
							listOfMarkers.add(strings[j].toString());
							continue;
						} 
					}
				}
				if(listOfMarkers.size()==0){
					_mainHomePage.getMainWindow().getWindow().showNotification("No Markers to export",  Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				try {
					List<String> snpMarkers=genoManager.getMarkerNamesByMarkerType(mType, 0, (int)genoManager.countMarkerNamesByMarkerType(mType));
					if(!(snpMarkers.isEmpty())){
						for(int m=0; m<listOfMarkers.size();m++){
							if(snpMarkers.contains(listOfMarkers.get(m))){
								markersForKBio.add(listOfMarkers.get(m).toString());
							}
						}
						if(!(markersForKBio.isEmpty())) {
							orderFormForPlymorphicMarkers=exportFileFormats.exportToKBio(markersForKBio, _mainHomePage);
							FileResource fileResource = new FileResource(orderFormForPlymorphicMarkers, _mainHomePage);
							_mainHomePage.getMainWindow().getWindow().open(fileResource, "", true);
						}
						
						else{
							_mainHomePage.getMainWindow().getWindow().showNotification("Selected Marker(s) are not SNPs", Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}else{
						_mainHomePage.getMainWindow().getWindow().showNotification("No SNP Marker(s) to create KBio Order form", Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					
				} catch (Exception e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Error generating KBioOrder Form", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
			}
		});
		layoutForExportTypes.addComponent(btnViewKBioOrderForm);
		
		
		
		if (0 == _tableForMarkerResults.size()){
			txtBinSize.setEnabled(false);
			//if(hmOfMarkerIDAndQtlTrait!=null && hmOfMarkerIDAndQtlTrait.size()>0){
				//selectTrait.setEnabled(false);
			
				//checkBox.setEnabled(false);
			//}
			excelButton.setEnabled(false);
			//pdfButton.setEnabled(false);
			//printButton.setEnabled(false);
		} else {
			txtBinSize.setEnabled(true);
			if(hmOfMarkerIDAndQtlTrait!=null && hmOfMarkerIDAndQtlTrait.size()>0){
				selectTrait.setEnabled(true);
				checkBox.setEnabled(true);
			}
			excelButton.setEnabled(true);
			//pdfButton.setEnabled(true);
			//printButton.setEnabled(true);
		}
		
		resultsLayoutForPolymorphicMaps.addComponent(layoutForExportTypes);
		resultsLayoutForPolymorphicMaps.setComponentAlignment(layoutForExportTypes, Alignment.MIDDLE_RIGHT);
		
		return resultsLayoutForPolymorphicMaps;
	}
	private ArrayList<MappingData> sortFinalListOfMappingData() {
		
		if (null != finalListOfMappingData) {
			
			Collections.sort(finalListOfMappingData, new Comparator<MappingData>() {

				@Override
				public int compare(MappingData o1, MappingData o2) {							

					//return o1.getLinkageGroup().compareTo(o2.getLinkageGroup());
					float startPosition1 = o1.getStartPosition();
					Float fStartPosition1 = Float.valueOf(startPosition1);
					
					float startPosition2 = o2.getStartPosition();
					Float fStartPosition2 = Float.valueOf(startPosition2);
					
					return fStartPosition1.compareTo(fStartPosition2);
				}
			});
		}	
		
		return null;
	}
	
	private void handleChecksWithTraits(CheckBox checkBox, ArrayList selectTrait, HashMap<Integer, String> htTraitList) {
			
		ArrayList markers=new ArrayList();
		String selectedTrait="";
		//if(listOfSelectedTraits!=null){
			//System.out.println("...........selectTrait:"+selectTrait+"   "+listOfSelectedTraits);
			 for(int t=0;t<selectTrait.size();t++){
				 ArrayList markersList=new ArrayList();
				 selectedTrait=htTraitList.get(Integer.parseInt(selectTrait.get(t).toString()));
			     // System.out.println("!!!!!!!!!!!!!...............  :"+selectedTrait);
			      markersList=hashMap1.get(selectedTrait);
			      for(int m=0;m<markersList.size();m++){
			    	  if(!markers.contains(markersList.get(m)))
			    		  markers.add(markersList.get(m));
			      }
			 }
			//System.out.println(markers.size()+".......... markers=:"+markers);
			for (int i = 0; i < finalListOfMappingData.size(); i++){
				MappingData mappingData = finalListOfMappingData.get(i);
				//System.out.println(mappingData.get);
				if(markers.contains(mappingData.getMarkerId())){
					arrayOfCheckBoxes[i].setValue(true);
				}
			}
		/*}else{
			_mainHomePage.getMainWindow().getWindow().showNotification("Please select the Trait and click CheckTrait(s).", Notification.TYPE_ERROR_MESSAGE);			
			return;
		}*/
		 
	}
	
	//selecting markers based on the binsize given
	private void handleChecks(TextField txtBinSize, CheckBox checkBox,
			ListSelect selectTrait, HashMap<Integer, String> htTraitList) {
		Object value = txtBinSize.getValue();
		if(null == value) {
			uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
			return;
		}
		String strValue = value.toString();
		if (0 == strValue.trim().length()){
			uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
			return;
		}
		double dData = 0.0;
		try {
			dData = Double.parseDouble(strValue);
		} catch(Throwable th) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a valid real value.", Notification.TYPE_ERROR_MESSAGE);
			uncheckAllCheckBox(checkBox, selectTrait, htTraitList);
			return;
		}
		if(null == finalListOfMappingData) {
			return;
		}
		
		/*ArrayList<MapInfo> arrayListOfChromosomeData = new ArrayList<MapInfo>();
		arrayListOfChromosomeData.addAll(listOfMapData);*/
		
		String strCurrentChromosome = "";
		//String strNextChromosome = "";
		//for (int iLGIndex = iNextChromosomeIndex; iLGIndex <= arrayListOfChromosomeData.size(); iLGIndex++) {
		int iLGIndex =  0;
		while (iLGIndex < listOfMapData.size()) {
			
			//MapInfo mappingData = listOfMapData.get(iLGIndex);
			String mappingData = listOfMapData.get(iLGIndex);
			//MapInfo mappingDataNext = null;
			String mappingDataNext = null;
			if((iLGIndex + 1) < listOfMapData.size()) {
				mappingDataNext = listOfMapData.get(iLGIndex+1);
			} else {
				break;
			}
			if(null == mappingDataNext) {
				continue;
			}
			
			
				//strCurrentChromosome = mappingData.getLinkageGroup();
				String[] mappingDataArray = mappingData.toString().split("!~!");
				strCurrentChromosome=mappingDataArray[2].toString();
			
			
			//System.out.println("Current Chromosome: " + strCurrentChromosome + " --- dData: " + dData);
			handleChecksWithinChromosome(strCurrentChromosome, iLGIndex, (float)dData, checkBox, selectTrait, htTraitList);
			iLGIndex = iNextChromosomeIndex;
			
		};
		
		
	}
	
private void handleChecksWithinChromosome(String theChromosome, int theChromosomesStartingIndex, float dBinSizeValue, CheckBox checkBox, ListSelect selectTrait, HashMap<Integer, String> htTraitList) {
		
		//boolean bOneCheckBoxChecked = false;
		float temp = dBinSizeValue;
		for (int i = theChromosomesStartingIndex; i < listOfMapData.size(); i++){
			
			if (null == listOfMapData.get(i)){
				iNextChromosomeIndex = i;
				return;
			}
			//System.out.println("................:"+listOfMapData.get(i));
			//MapInfo mappingData = listOfAllMapData.get(i);
			String[] mappingDataArray = listOfMapData.get(i).toString().split("!~!");
			
			if (false == theChromosome.equals(mappingDataArray[2].toString())){
				iNextChromosomeIndex = i;
				//System.out.println("iNextChromosomeIndex: " + iNextChromosomeIndex);
				return;
			} else {
				iNextChromosomeIndex = i;
			}
			
			//float startPosition = mappingData.getStartPosition();
			float startPosition = Float.parseFloat(mappingDataArray[3].toString());
			
			//System.out.print("handleChecks: --- i:" + i + " --- startPosition:" + startPosition + " --- chromosome:" + mappingDataArray[2] + " --- dData: " + dBinSizeValue);
			
			if(0 == dBinSizeValue) {
				arrayOfCheckBoxes[i].setValue(true);
				//System.out.println(" --- Selected");
				//bOneCheckBoxChecked = true;
				if(!selMarkerList.contains(mappingDataArray[4])){
					selMarkerList.add(mappingDataArray[4]);
				}
			} 
			
			if (0.0 == startPosition){
				arrayOfCheckBoxes[i].setValue(true);
				//System.out.println(" --- Selected");
				if(!selMarkerList.contains(mappingDataArray[4])){
					selMarkerList.add(mappingDataArray[4]);
				}
				//bOneCheckBoxChecked = true;
			} else if (temp <= startPosition) {
				arrayOfCheckBoxes[i].setValue(true);
				temp = dBinSizeValue + startPosition;
				//System.out.println(" --- Selected");
				if(!selMarkerList.contains(mappingDataArray[4])){
					selMarkerList.add(mappingDataArray[4]);
				}
				//bOneCheckBoxChecked = true;
			}			
		}		
	}
	
	private void uncheckAllCheckBox(CheckBox checkBox, ListSelect selectTrait, HashMap<Integer, String> htTraitList) {
		if(null == listOfMapData) {
			return;
		}
		if(0 == listOfMapData.size()) {
			return;
		}
		
		for (int i = 0; i < listOfMapData.size(); i++) {
			handleCheckBox(checkBox, selectTrait, i, htTraitList);
		}
		
	}
	private void handleCheckBox(CheckBox checkBox, ListSelect selectTrait, int i, HashMap<Integer,String> htTraitList) {
		if(checkBox.booleanValue()) {
			handlechecks(selectTrait, i, htTraitList);
		} else {
			arrayOfCheckBoxes[i].setValue(false);
		}
	}

	private void handlechecks(ListSelect selectTrait, int i, HashMap<Integer, String> htTraitList) {
		if(null == listOfMapData) {
			return;
		}
		if(0 == listOfMapData.size()) {
			return;
		}

		if(i >= listOfMapData.size()) {
			return;
		}

		/*if(null != htTraitList) {
			QtlDetails qtlDetails = htTraitList.get(markerId + markerName);
			if(null != qtlDetails) {}
		}*/
		
		
		for (String strQtlTrait : listofTraits) {
			
			if (false == checkBox.booleanValue()){
				break;
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
								arrayOfCheckBoxes[i].setValue(true);
							}
						}
					} else {
						if(strQtlTrait.equals(value.toString())) {
							arrayOfCheckBoxes[i].setValue(true);
						}
					}
				} else {
					arrayOfCheckBoxes[i].setValue(false);
				}
			} else {
				arrayOfCheckBoxes[i].setValue(false);
			}
			
		}
		
	}

	private void exportToExcel() {
		if(null == finalListOfMappingData || 0 == finalListOfMappingData.size()) {
			return;
		}
		List<String[]> listToExport = new ArrayList<String[]>();
		String strFileName = "MarkerTrait";
		for (int i = 0; i < finalListOfMappingData.size(); i++){

			MappingData mappingData = finalListOfMappingData.get(i);

			String strLinkageGroup = mappingData.getLinkageGroup();
			//Integer mapId = mappingData.getMapId();
			String strMapName = mappingData.getMapName();
			//String mapUnit = mappingData.getMapUnit();
			Integer markerId = mappingData.getMarkerId();
			String markerName = mappingData.getMarkerName();
			float startPosition = mappingData.getStartPosition();
			String strQtlTrait = hmOfMarkerIDAndQtlTrait.get(markerId);
			//arrayOfCheckBoxes[i] = new CheckBox();
			if (null != arrayOfCheckBoxes) {
				if (null != arrayOfCheckBoxes[i]){
					if (true == arrayOfCheckBoxes[i].booleanValue()) {
						listToExport.add(new String[] {markerName, strMapName, strLinkageGroup, String.valueOf(startPosition), strQtlTrait});
					}
				}
			}
		}
		
		if(0 == listToExport.size()) {
			_mainHomePage.getMainWindow().getWindow().showNotification("No Markers to export",  Notification.TYPE_ERROR_MESSAGE);
			return;
		}

		String[] strValues = new String[] {"MARKER", "MAP", "CHROMOSOME", "POSITION", "TRAIT"};
		listToExport.add(0, strValues);
		
		
		ExportFileFormats exportFileFormats = new ExportFileFormats();
		try {
			exportFileFormats.exportMap(_mainHomePage, listToExport, strFileName);
		} catch (WriteException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
		} catch (IOException e) {
			_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
		}
	}


	private void retrieveMappingDataBetweenLines() {
		try{			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();				
		}catch (Exception e){
			e.printStackTrace();
		}
		HashMap<Integer, String> hashMapOfMarkerIDsAndNames = new HashMap<Integer, String>();
		String strQueryMap="";
		listOfMarkerIDs=new ArrayList<Integer>();
		try{
			mapID=genoManager.getMapIdByName(strSelectedMapName);
			
		}catch (MiddlewareQueryException e1) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Map Id for the selected Map: '"+strSelectedMapName+"' .", Notification.TYPE_ERROR_MESSAGE);
			e1.printStackTrace();
			return;
		}
		try{
			List<Marker> markerIDsCentral=genoManager.getMarkersByMarkerNames(listOfMarkersSelected, 0, listOfMarkersSelected.size(), Database.CENTRAL);
			if(markerIDsCentral!=null){
				for(Marker mc:markerIDsCentral){
					listOfMarkerIDs.add(mc.getMarkerId());
				}
				
			}
			List<Marker> markerIDsLocal=genoManager.getMarkersByMarkerNames(listOfMarkersSelected, 0, listOfMarkersSelected.size(), Database.LOCAL);
			if(markerIDsLocal!=null){
				for(Marker ml:markerIDsLocal){
					listOfMarkerIDs.add(ml.getMarkerId());
				}
			}
			/*List<Integer> markerIDsCentral=genoManager.getMarkerIdsByMarkerNames(listOfMarkersSelected, 0, listOfMarkersSelected.size(), Database.CENTRAL);
			if(markerIDsCentral!=null){
				for(int c=0;c<markerIDsCentral.size();c++){
					listOfMarkerIDs.add(markerIDsCentral.get(c));
				}
				
			}
			List<Integer> markerIDsLocal=genoManager.getMarkerIdsByMarkerNames(listOfMarkersSelected, 0, listOfMarkersSelected.size(), Database.LOCAL);
			if(markerIDsLocal!=null){
				for(int c=0;c<markerIDsLocal.size();c++){
					listOfMarkerIDs.add(markerIDsLocal.get(c));
				}
			}*/
			
		}catch (MiddlewareQueryException e1) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Marker IDs.", Notification.TYPE_ERROR_MESSAGE);
			e1.printStackTrace();
			return;
		}
		
		for(int m=0;m<listOfMarkerIDs.size();m++){
			marker_ids=marker_ids+listOfMarkerIDs.get(m)+",";
		}
		try {
			 List<MarkerIdMarkerNameElement> markerNames =genoManager.getMarkerNamesByMarkerIds(listOfMarkerIDs);
			 for (MarkerIdMarkerNameElement e : markerNames) {		        
				 hashMapOfMarkerIDsAndNames.put(e.getMarkerId(), e.getMarkerName());
		       }
		}catch (MiddlewareQueryException e1) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Marker IDs.", Notification.TYPE_ERROR_MESSAGE);
			e1.printStackTrace();
			return;
		}
		String strQuerry="SELECT distinct gdms_markers_onmap.marker_id, gdms_map.map_name, gdms_markers_onmap.start_position, gdms_markers_onmap.linkage_group, gdms_map.map_unit FROM gdms_map join gdms_markers_onmap on gdms_map.map_id=gdms_markers_onmap.map_id where gdms_markers_onmap.marker_id in ("+marker_ids.substring(0, marker_ids.length()-1)+") and gdms_map.map_id="+mapID+" order BY gdms_map.map_name, gdms_markers_onmap.linkage_group, gdms_markers_onmap.start_position asc";
		finalListOfMappingData = new ArrayList<MappingData>();
		MappingData mappingDataPOJO;
		
		//System.out.println(strQuerry);
		listOfMapData = new ArrayList<String>();
		
		List newListL=new ArrayList();
		List newListC=new ArrayList();
		//try {	
		Object obj=null;
		Object objL=null;
		Iterator itListC=null;
		Iterator itListL=null;
		
		markersOnMapList=new ArrayList();
		
		
		//listOfMapData.clear();
		if(mapID>0){		
			sessionC=centralSession.getSessionFactory().openSession();			
			SQLQuery queryC=sessionC.createSQLQuery(strQuerry);		
			queryC.addScalar("marker_id",Hibernate.INTEGER);
			queryC.addScalar("map_name",Hibernate.STRING);	
			queryC.addScalar("start_position",Hibernate.DOUBLE);
			queryC.addScalar("linkage_group",Hibernate.STRING);
			queryC.addScalar("map_unit",Hibernate.STRING);
			newListC=queryC.list();			
			itListC=newListC.iterator();			
			while(itListC.hasNext()){
				obj=itListC.next();
				if(obj!=null){		
					Object[] strMareO= (Object[])obj;
	        	
					//listOfMapData.add(obj.toString());	
					mappingDataPOJO = new MappingData();
					mappingDataPOJO.setMapName(strSelectedMapName);
					mappingDataPOJO.setMapId(mapID);
					mappingDataPOJO.setLinkageGroup(strMareO[3].toString());
					
					mappingDataPOJO.setMarkerId(Integer.parseInt(strMareO[0].toString()));
					mappingDataPOJO.setMarkerName(hashMapOfMarkerIDsAndNames.get(Integer.parseInt(strMareO[0].toString())).toString());
					mappingDataPOJO.setStartPosition(Float.parseFloat(strMareO[2].toString()));
					mappingDataPOJO.setMapUnit(strMareO[4].toString());
					if(! markersOnMapList.contains(hashMapOfMarkerIDsAndNames.get(Integer.parseInt(strMareO[0].toString())).toString()))
						markersOnMapList.add(hashMapOfMarkerIDsAndNames.get(Integer.parseInt(strMareO[0].toString())).toString());
					
					finalListOfMappingData.add(mappingDataPOJO);
					listOfMapData.add(hashMapOfMarkerIDsAndNames.get(Integer.parseInt(strMareO[0].toString())).toString()+"!~!"+strMareO[1].toString()+"!~!"+strMareO[3].toString()+"!~!"+Float.parseFloat(strMareO[2].toString())+"!~!"+strMareO[0].toString());
				}
				//listOfAllMappingData.
				
			}
			//System.out.println("finalListOfMappingData=:"+finalListOfMappingData);
		}else{	
			sessionL=localSession.getSessionFactory().openSession();			
			SQLQuery queryL=sessionL.createSQLQuery(strQuerry);				
			queryL.addScalar("marker_id",Hibernate.INTEGER);
			queryL.addScalar("map_name",Hibernate.STRING);	
			queryL.addScalar("start_position",Hibernate.DOUBLE);
			queryL.addScalar("linkage_group",Hibernate.STRING);
			queryL.addScalar("map_unit",Hibernate.STRING);
			newListL=queryL.list();			
			itListL=newListL.iterator();			
			while(itListL.hasNext()){
				objL=itListL.next();
				if(objL!=null){		
					Object[] strMareO= (Object[])objL;	        	
					mappingDataPOJO = new MappingData();
					mappingDataPOJO.setMapName(strSelectedMapName);
					mappingDataPOJO.setMapId(mapID);
					mappingDataPOJO.setLinkageGroup(strMareO[3].toString());	
					mappingDataPOJO.setMarkerName(hashMapOfMarkerIDsAndNames.get(Integer.parseInt(strMareO[0].toString())).toString());
					mappingDataPOJO.setMarkerId(Integer.parseInt(strMareO[0].toString()));
					mappingDataPOJO.setStartPosition(Float.parseFloat(strMareO[2].toString()));
					mappingDataPOJO.setMapUnit(strMareO[4].toString());
					if(! markersOnMapList.contains(hashMapOfMarkerIDsAndNames.get(Integer.parseInt(strMareO[0].toString())).toString()))
						markersOnMapList.add(hashMapOfMarkerIDsAndNames.get(Integer.parseInt(strMareO[0].toString())).toString());
					finalListOfMappingData.add(mappingDataPOJO);
					listOfMapData.add(hashMapOfMarkerIDsAndNames.get(Integer.parseInt(strMareO[0].toString())).toString()+"!~!"+strMareO[1].toString()+"!~!"+strMareO[3].toString()+"!~!"+Float.parseFloat(strMareO[2].toString())+"!~!"+strMareO[0].toString());
				}
				//listOfAllMappingData.
			}	
			//System.out.println("finalListOfMappingData=:"+finalListOfMappingData);
		}		
		
		QtlDetailsDAO qtlDetailsDAOLocal = new QtlDetailsDAO();
		qtlDetailsDAOLocal.setSession(localSession);
		QtlDetailsDAO qtlDetailsDAOCentral = new QtlDetailsDAO();
		qtlDetailsDAOCentral.setSession(centralSession);
		ArrayList<QtlDetails> listOfAllQtlDetails = new ArrayList<QtlDetails>();
		try {
			List<QtlDetails> listOfAllQtlDetailsLocal = qtlDetailsDAOLocal.getAll();
			List<QtlDetails> listOfAllQtlDetailsCentral = qtlDetailsDAOCentral.getAll();
			if (null != listOfAllQtlDetailsLocal) {
				for (QtlDetails qtlDetails : listOfAllQtlDetailsLocal){
					if (false == listOfAllQtlDetails.contains(qtlDetails)){
						listOfAllQtlDetails.add(qtlDetails);
					}
				}
			}
			if (null != listOfAllQtlDetailsCentral) { 
				for (QtlDetails qtlDetails : listOfAllQtlDetailsCentral){
					if (false == listOfAllQtlDetails.contains(qtlDetails)){
						listOfAllQtlDetails.add(qtlDetails);
					}
				}
			}
		} catch (MiddlewareQueryException e1) {
			_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Qtl Detail objects", Notification.TYPE_ERROR_MESSAGE);
			return;
		}

		
		
		hmOfMarkerIDAndQtlTrait = new HashMap<Integer, String>();
		listofTraits = new ArrayList<String>();
		for (MappingData mappingData : finalListOfMappingData){
			String linkageGroup = mappingData.getLinkageGroup();
			float startPosition = mappingData.getStartPosition();
			Integer markerId = mappingData.getMarkerId();
			
			for (QtlDetails qtlDetails : listOfAllQtlDetails){
				String lgFromQTL = qtlDetails.getLinkageGroup();
				
				if (linkageGroup.equals(lgFromQTL)){
					Float minPosition = qtlDetails.getMinPosition();
					Float maxPosition = qtlDetails.getMaxPosition();
					
					if ((minPosition <= startPosition) && (startPosition <= maxPosition)){
						//String trait = qtlDetails.getTrait();
						String trait = "";
						Integer iTraitId = qtlDetails.getTraitId();
						if (null != iTraitId){							
							try {								
								trait=om.getStandardVariable(iTraitId).getName();	
								
							} catch (MiddlewareQueryException e) {
								_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Traits.", Notification.TYPE_ERROR_MESSAGE);
								return;
							}
						}
						hmOfMarkerIDAndQtlTrait.put(markerId, trait);
						if(!listofTraits.contains(trait))
							listofTraits.add(trait);
						addValues1(trait, markerId);
					}
				}
			}
		}
		
		//System.out.println("hmOfMarkerIDAndQtlTrait=:"+hmOfMarkerIDAndQtlTrait);
		
		
	}

	public void componentEvent(Event event) {
		event.getComponent().requestRepaint();
	}
	private static void addValues(int key, String value){
		ArrayList<String> tempList = null;
		if(hashMap.containsKey(key)){
			tempList=hashMap.get(key);
			if(tempList == null)
				tempList = new ArrayList<String>();
			tempList.add(value);
		}else{
			tempList = new ArrayList();
			tempList.add(value);
		}
		hashMap.put(key,tempList);
	}
	
	private static void addValues1(String key, Integer value){
		ArrayList<Integer> tempList = null;
		if(hashMap1.containsKey(key)){
			tempList=hashMap1.get(key);
			if(tempList == null)
				tempList = new ArrayList<Integer>();
			tempList.add(value);
		}else{
			tempList = new ArrayList();
			tempList.add(value);
		}
		hashMap1.put(key,tempList);
	}
	

}


