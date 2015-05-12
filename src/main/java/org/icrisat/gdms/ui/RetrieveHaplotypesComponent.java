package org.icrisat.gdms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.AccMetadataSet;
//import org.generationcp.middleware.pojos.gdms.AccMetadataSetPK;
import org.generationcp.middleware.pojos.gdms.AlleleValues;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.itextpdf.text.log.SysoLogger;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

public class RetrieveHaplotypesComponent implements Component.Listener {
	private static final long serialVersionUID = 1L;
	
	ManagerFactory factory=null;
	private Session localSession;
	private Session centralSession;
	GermplasmDataManager manager;
	GenotypicDataManager genoManager;
	private GDMSMain _mainHomePage;
	private TabSheet _tabsheetForHaps;
	private Component buildHapResultsComponent;
	private Table _tableWithAllHAPs;
	Button btnNext;
	
	private Table _hapsTable;
	
	String markers="";
	String strHapQuerry="";
	String trackNames="";
	
	ArrayList hapsList=new ArrayList();
	
	HashMap<String, Object> markersMap = new HashMap<String, Object>();	
	HashMap marker = new HashMap();
	HashMap<Integer, HashMap<String, Object>> mapEx = new HashMap<Integer, HashMap<String,Object>>();	
	HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
	ArrayList glist = new ArrayList();
	SQLQuery query2L;
	SQLQuery query2C;
	List<Marker> getDet;
	List<String> markersList=new ArrayList<String>();
	
	
	HashMap<Integer,String> hashMapGidsGermplasmNames= new HashMap<Integer,String>();
	
	HashMap<Integer,String> hashMapMarkerIDsMarkerNames= new HashMap<Integer,String>();
	
	HashMap<Integer,String> hashHapMarkers= new HashMap<Integer,String>();
	
	
	ArrayList<String> hapMarkersList=new ArrayList<String>();
	String op="";
	public RetrieveHaplotypesComponent(GDMSMain theMainHomePage){
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
	
	public HorizontalLayout buildTabbedComponentForMarkers() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();

		_tabsheetForHaps = new TabSheet();
		_tabsheetForHaps.setWidth("700px");
		
		Component buildHapSearchComponent = buildHapsSearchComponent();
		buildHapSearchComponent.setSizeFull();

		buildHapResultsComponent = buildHapsResultsComponent();
		buildHapResultsComponent.setSizeFull();

		_tabsheetForHaps.addComponent(buildHapSearchComponent);
		_tabsheetForHaps.addComponent(buildHapResultsComponent);

		_tabsheetForHaps.getTab(0).setEnabled(true);
		_tabsheetForHaps.getTab(1).setEnabled(false);
		horizontalLayout.addComponent(_tabsheetForHaps);
		
		return horizontalLayout;
		
	}
	
	
	private Component buildHapsSearchComponent() {
		VerticalLayout searchLayout = new VerticalLayout();
		searchLayout.setCaption("Search");
		searchLayout.setMargin(true, true, true, true);
		searchLayout.setSpacing(true);
		
		
		VerticalLayout searchHapsLayout = new VerticalLayout();
		//searchHapsLayout.setCaption("Search");
		searchHapsLayout.setMargin(true, true, true, true);
		searchHapsLayout.setSpacing(true);
		
		Label lblSearch = new Label("Search for Lines with particular haplotype");
		lblSearch.setStyleName(Reindeer.LABEL_H2);
		searchHapsLayout.addComponent(lblSearch);
		searchHapsLayout.setComponentAlignment(lblSearch, Alignment.TOP_CENTER);
		
		
		Label lblHap = new Label("Haplotype");
		lblHap.setWidth("50px");
		lblHap.setStyleName(Reindeer.LABEL_SMALL);

		final TextField txtFieldSearch = new TextField();
		txtFieldSearch.setWidth("500px");
		txtFieldSearch.setNullRepresentation("");
		
		ThemeResource themeResource = new ThemeResource("images/find-icon.png");
		Button searchButton = new Button();
		searchButton.setIcon(themeResource);
		
		VerticalLayout searchAllelesLayout = new VerticalLayout();
		//searchAllelesLayout.setCaption("Search");
		searchAllelesLayout.setMargin(true, true, true, true);
		searchAllelesLayout.setSpacing(true);
		
		
		Label lblAlleleSearch = new Label("Querying for a particular Allele");
		lblAlleleSearch.setStyleName(Reindeer.LABEL_H2);
		searchAllelesLayout.addComponent(lblAlleleSearch);
		searchAllelesLayout.setComponentAlignment(lblAlleleSearch, Alignment.TOP_CENTER);
		
		
		Label lblAllele = new Label("Markers");
		lblAllele.setWidth("50px");
		lblAllele.setStyleName(Reindeer.LABEL_SMALL);

		final TextField txtFieldAlleleSearch = new TextField();
		txtFieldAlleleSearch.setWidth("500px");
		txtFieldAlleleSearch.setNullRepresentation("");
		
		ThemeResource themeResourceA = new ThemeResource("images/find-icon.png");
		Button searchAlleleButton = new Button();
		searchAlleleButton.setIcon(themeResourceA);
		
		
		
		
		//final GridLayout gridLayout = new GridLayout();
		final HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		
		searchButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				op="haps";
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
					buildOnLoad(horizontalLayout, strSearchString, op);
					horizontalLayout.requestRepaint();
					//txtFieldSearch.setValue("");
				} else if(strSearchString.endsWith("*")) {
					strSearchString = strSearchString.substring(0, strSearchString.length() - 1);
					/*buildOnLoad(gridLayout, strSearchString);
					gridLayout.requestRepaint();*/
					buildOnLoad(horizontalLayout, strSearchString, op);
					horizontalLayout.requestRepaint();
					//txtFieldSearch.setValue("");
				} else {
					/*buildOnLoad(gridLayout, strSearchString);
					gridLayout.requestRepaint();*/
					buildOnLoad(horizontalLayout, strSearchString, op);
					horizontalLayout.requestRepaint();
					//txtFieldSearch.setValue("");
				}
				
				if (null == _tableWithAllHAPs){
					_mainHomePage.getMainWindow().getWindow().showNotification("There are no Haplotypes to be displayed.", Notification.TYPE_ERROR_MESSAGE);
					btnNext.setEnabled(false);
					return;
				}
				
				if (null != _tableWithAllHAPs && 0 == _tableWithAllHAPs.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("There are no Haplotypes to be displayed.", Notification.TYPE_ERROR_MESSAGE);
					btnNext.setEnabled(false);
					return;
				}
			}
			
		});
			
		HorizontalLayout layoutForTextSearch = new HorizontalLayout();
		layoutForTextSearch.setSpacing(true);
		layoutForTextSearch.addComponent(lblHap);
		layoutForTextSearch.addComponent(txtFieldSearch);
		layoutForTextSearch.addComponent(searchButton);	
		
		searchHapsLayout.addComponent(layoutForTextSearch);
		searchHapsLayout.setMargin(true, true, true, true);
		//searchHapsLayout.addComponent(horizontalLayout);
		
		searchLayout.addComponent(searchHapsLayout);
		searchLayout.addComponent(horizontalLayout);
		
		/*final HorizontalLayout horizontalLayoutForAlleles = new HorizontalLayout();
		horizontalLayoutForAlleles.setSpacing(true);
		horizontalLayoutForAlleles.setMargin(true);*/
		
		searchAlleleButton.addListener(new Button.ClickListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				op="allele";
				String strSearchString = txtFieldAlleleSearch.getValue().toString();
				if (strSearchString.trim().equals("")){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please enter a search string.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
				if (false == strSearchString.endsWith("*")){
					if (false == strSearchString.equals(""))
						strSearchString = strSearchString + "*";
				}
				if(strSearchString.equals("*")) {					
					buildOnLoad(horizontalLayout, strSearchString, op);
					horizontalLayout.requestRepaint();
					//txtFieldAlleleSearch.setValue("");
				} else if(strSearchString.endsWith("*")) {
					strSearchString = strSearchString.substring(0, strSearchString.length() - 1);
					
					buildOnLoad(horizontalLayout, strSearchString, op);
					horizontalLayout.requestRepaint();
					//txtFieldAlleleSearch.setValue("");
				} else {
					
					buildOnLoad(horizontalLayout, strSearchString, op);
					horizontalLayout.requestRepaint();
					//txtFieldAlleleSearch.setValue("");
				}
				
				if (null == _tableWithAllHAPs){
					_mainHomePage.getMainWindow().getWindow().showNotification("There is no genotyping data for the given marker(s).", Notification.TYPE_ERROR_MESSAGE);
					btnNext.setEnabled(false);
					return;
				}
				
				if (null != _tableWithAllHAPs && 0 == _tableWithAllHAPs.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("There is no genotyping data for the given marker(s).", Notification.TYPE_ERROR_MESSAGE);
					btnNext.setEnabled(false);
					//txtFieldAlleleSearch.setValue(newValue).setValue("");
					return;
				}
			}
			
		});
		
		
		
		
		HorizontalLayout layoutForAlleleSearch = new HorizontalLayout();
		layoutForAlleleSearch.setSpacing(true);
		layoutForAlleleSearch.addComponent(lblAllele);
		layoutForAlleleSearch.addComponent(txtFieldAlleleSearch);
		layoutForAlleleSearch.addComponent(searchAlleleButton);
		
		searchAllelesLayout.addComponent(layoutForAlleleSearch);
		
		
		
		searchLayout.addComponent(searchAllelesLayout);
		searchLayout.addComponent(horizontalLayout);
		
		
		VerticalLayout layoutForButton = new VerticalLayout();
		btnNext = new Button("Next");
		btnNext.setEnabled(false);
		layoutForButton.addComponent(btnNext);
		
		layoutForButton.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
		layoutForButton.setMargin(true, false, true, true);
		btnNext.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				//listOfHapDetailElementsByName = new ArrayList<QtlDetailElement>();
				String strHapName = txtFieldSearch.getValue().toString();
				//System.out.println("strHapName=:"+strHapName);
				List<String> listOfNames = new ArrayList<String>();
				//if(false == strHapName.equals("*") || false == strHapName.endsWith("*")) {					
					int iNumOfHaps = _tableWithAllHAPs.size();
					//System.out.println("iNumOfHaps=:"+iNumOfHaps);
					if(op=="allele"){
						//System.out.println("inside if op= allele loop");
						for (int i = 0; i < iNumOfHaps; i++) {
							Item item = _tableWithAllHAPs.getItem(new Integer(i));
							Property itemProperty = item.getItemProperty("Select");
							CheckBox checkBox = (CheckBox) itemProperty.getValue();
							if (checkBox.booleanValue() == true) {
								String strSelectedHap = item.getItemProperty("Allele").toString();
								listOfNames.add(strSelectedHap);
							}
						}
					}else{
						//System.out.println("inside else if op= allele loop");
						for (int i = 0; i < iNumOfHaps; i++) {
							Item item = _tableWithAllHAPs.getItem(new Integer(i));
							Property itemProperty = item.getItemProperty("Select");
							CheckBox checkBox = (CheckBox) itemProperty.getValue();
							if (checkBox.booleanValue() == true) {
								String strSelectedHap = item.getItemProperty("Haplotype Name").toString();
								listOfNames.add(strSelectedHap);
							}
						}
					}
					
					
				//}
				//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!   :"+listOfNames+"    "+op);
				for(int l=0;l<listOfNames.size();l++){
					trackNames=trackNames+"'"+listOfNames.get(l)+"',";
				}
				//System.out.println("#########################   :"+trackNames);
				List<String> hapsList1= new ArrayList<String>();
				if(op=="allele"){
					markersList=new ArrayList<String>();
					
					List<Integer> gidsList=new ArrayList<Integer>();
					List<Integer> nidsList=new ArrayList<Integer>();
					
					
					List<String> markerList=new ArrayList<String>();
					List<Integer> markerIds=new ArrayList<Integer>();
					String marker=txtFieldAlleleSearch.getValue().toString();
					markersList.add(marker);
					//System.out.println("entered marker=:"+marker);
					String markers="";
					String alleles="";
					//String strQuery="";
					try{
						List<Marker> markerIdsCentral=genoManager.getMarkersByMarkerNames(markersList, 0, markersList.size(), Database.CENTRAL);
						List<Marker> markerIdsLocal=genoManager.getMarkersByMarkerNames(markersList, 0, markersList.size(), Database.LOCAL);
						if(markerIdsCentral!=null){
							for(Marker mC:markerIdsCentral){
								if(!markerList.contains(mC.getMarkerName())){
									markerList.add(mC.getMarkerName());
									markerIds.add(mC.getMarkerId());
									hashMapMarkerIDsMarkerNames.put(mC.getMarkerId(), mC.getMarkerName());
								}
							}
						}
						if(markerIdsLocal!=null){
							for(Marker mL:markerIdsLocal){
								//markerIds.add(mL.getMarkerId());
								if(!markerList.contains(mL.getMarkerName())){
									markerList.add(mL.getMarkerName());
									markerIds.add(mL.getMarkerId());
									hashMapMarkerIDsMarkerNames.put(mL.getMarkerId(), mL.getMarkerName());
								}
							}
						}
						for(int m=0;m< markerIds.size();m++){
							markers=markers+markerIds.get(m)+",";
						}
						try{
							localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
							centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
						}catch (Exception e){
							e.printStackTrace();
						}
						//System.out.println(genoManager.getAllGidsByMarkersAndAlleleValues(markerIds, listOfNames));
						
						List allelesFromLocal=new ArrayList();	
						List allelesFromCentral=new ArrayList();	
						//List markersList=new ArrayList();		
						List allelesList=new ArrayList();
						//List gidsList=new ArrayList();
						Object objAL=null;
						Object objAC=null;
						Iterator itListAC=null;
						Iterator itListAL=null;	
						
						//System.out.println(genoManager.getAllelicValuesByMarkersAndAlleleValues(Database.CENTRAL, markerIds, listOfNames));
						//System.out.println(".............:"+genoManager.getAllAllelicValuesByMarkersAndAlleleValue(markerIds, listOfNames));
						
						
						String strQuerry1="select gid, marker_id, char_value from gdms_char_values where marker_id in("+markers.substring(0, markers.length()-1)+") and char_value in("+trackNames.substring(0, trackNames.length()-1)+") order by gid";
						//System.out.println("strQuerry1;"+strQuerry1);
						//System.out.println(strQuerry1);
						query2L=localSession.createSQLQuery(strQuerry1);		
						query2L.addScalar("gid",Hibernate.INTEGER);	 
						query2L.addScalar("marker_id",Hibernate.INTEGER);
						query2L.addScalar("char_value",Hibernate.STRING);				
						
						allelesFromLocal=query2L.list();
						
						
						query2C=centralSession.createSQLQuery(strQuerry1);
						query2C.addScalar("gid",Hibernate.INTEGER);	 
						query2C.addScalar("marker_id",Hibernate.INTEGER);
						query2C.addScalar("char_value",Hibernate.STRING);
						allelesFromCentral=query2C.list();
						
						//System.out.println(allelesFromCentral+"     "+allelesFromLocal);
						if(allelesFromCentral.isEmpty() && allelesFromLocal.isEmpty()){	
							//System.out.println("char val from both local and central is null");
							strQuerry1="select gid, marker_id, allele_bin_value from gdms_allele_values where marker_id in("+markers.substring(0, markers.length()-1)+") and allele_bin_value in("+trackNames.substring(0, trackNames.length()-1)+") order by gid";
							//System.out.println("strQuerry1;"+strQuerry1);
							//System.out.println(strQuerry1);
							query2L=localSession.createSQLQuery(strQuerry1);		
							query2L.addScalar("gid",Hibernate.INTEGER);	 
							query2L.addScalar("marker_id",Hibernate.INTEGER);
							query2L.addScalar("allele_bin_value",Hibernate.STRING);								
							allelesFromLocal=query2L.list();							
							
							query2C=centralSession.createSQLQuery(strQuerry1);
							query2C.addScalar("gid",Hibernate.INTEGER);	 
							query2C.addScalar("marker_id",Hibernate.INTEGER);
							query2C.addScalar("allele_bin_value",Hibernate.STRING);
							allelesFromCentral=query2C.list();
							for(int w=0;w<allelesFromCentral.size();w++){
								Object[] strMareO= (Object[])allelesFromCentral.get(w);	
								//System.out.println("$$$$$$$$$$$$$$$$$$$$  :"+strMareO[0]+"  "+strMareO[1]+"  "+strMareO[2]);
								if(! gidsList.contains(Integer.parseInt(strMareO[0].toString()))){
									gidsList.add(Integer.parseInt(strMareO[0].toString()));
									hapsList1.add(strMareO[0]+"!~!"+strMareO[2]+"!~!"+strMareO[1]);
								}									
							}
							
							for(int w=0;w<allelesFromLocal.size();w++){
								Object[] strMareO= (Object[])allelesFromLocal.get(w);
								//System.out.println("$$$$$$$$$$$$$$$$$$$$  :"+strMareO[0]+"  "+strMareO[1]+"  "+strMareO[2]);
								if(! gidsList.contains(Integer.parseInt(strMareO[0].toString()))){
									gidsList.add(Integer.parseInt(strMareO[0].toString()));
									hapsList1.add(strMareO[0]+"!~!"+strMareO[2]+"!~!"+strMareO[1]);
								}										
							}							
						}else{
							//System.out.println(".............."+allelesFromCentral);
							for(int w=0;w<allelesFromCentral.size();w++){
								Object[] strMareO= (Object[])allelesFromCentral.get(w);		
								//System.out.println("$$$$$$$$$$$$$$$$$$$$  :"+strMareO[0]+"  "+strMareO[1]+"  "+strMareO[2]);
								if(! gidsList.contains(Integer.parseInt(strMareO[0].toString()))){
									gidsList.add(Integer.parseInt(strMareO[0].toString()));
									hapsList1.add(strMareO[0]+"!~!"+strMareO[2]+"!~!"+strMareO[1]);
								}								
							}
							//System.out.println(".............."+allelesFromLocal);
							for(int w=0;w<allelesFromLocal.size();w++){
								Object[] strMareO= (Object[])allelesFromLocal.get(w);	
								//System.out.println("$$$$$$$$$$$$$$$$$$$$  :"+strMareO[0]+"  "+strMareO[1]+"  "+strMareO[2]);
								if(! gidsList.contains(Integer.parseInt(strMareO[0].toString()))){
									gidsList.add(Integer.parseInt(strMareO[0].toString()));
									hapsList1.add(strMareO[0]+"!~!"+strMareO[2]+"!~!"+strMareO[1]);
								}										
							}
						}
						//System.out.println(".......:"+hapsList1);
						
						/*List<AccMetadataSetPK> accMetadata=genoManager.getGdmsAccMetadatasetByGid(gidsList, 0, (int) genoManager.countGdmsAccMetadatasetByGid(gidsList));
						for(AccMetadataSetPK results: accMetadata){*/
						List<AccMetadataSet> accMetadata=genoManager.getGdmsAccMetadatasetByGid(gidsList, 0, (int) genoManager.countGdmsAccMetadatasetByGid(gidsList));
						for(AccMetadataSet results: accMetadata){
							if(!nidsList.contains(results.getNameId()))
								nidsList.add(results.getNameId());
						}
						List<Name> resName= genoManager.getNamesByNameIds(nidsList);
						for(Name  res:resName){
							hashMapGidsGermplasmNames.put(res.getGermplasmId(), res.getNval());
						}
						//System.out.println();
						for(int h=0;h<hapsList1.size();h++){
							String[] strArgs=hapsList1.get(h).split("!~!");
							hapsList.add(hashMapGidsGermplasmNames.get(Integer.parseInt(strArgs[0].toString()))+"!~!"+strArgs[1]+"!~!"+hashMapMarkerIDsMarkerNames.get(Integer.parseInt(strArgs[2].toString())));
						}
						
						//System.out.println("$$$$$$$$$  :"+hapsList);
						
					} catch (MiddlewareQueryException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
						return;
					}
				}else{
					
					List markersListH=new ArrayList();
					List gidsList=new ArrayList();
					List germplasmNamesList=new ArrayList();
					for(int h=0;h<listOfNames.size();h++){
						try{
							getDet=genoManager.getSNPsByHaplotype(listOfNames.get(h).toString());
							for(Marker res:getDet){
								if(!hapMarkersList.contains(res.getMarkerName())){
									hashMapMarkerIDsMarkerNames.put(res.getMarkerId(), res.getMarkerName());
									 hapMarkersList.add(res.getMarkerName());
									 markersListH.add(res.getMarkerId());
								}
							}
						} catch (MiddlewareQueryException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}
					try{
						 List<MarkerIdMarkerNameElement> markerNames = genoManager.getMarkerNamesByMarkerIds(markersListH);
						 for (MarkerIdMarkerNameElement e : markerNames) {
							if(!hapMarkersList.contains(e.getMarkerName())){
								 hashMapMarkerIDsMarkerNames.put(e.getMarkerId(), e.getMarkerName());
								 hapMarkersList.add(e.getMarkerName());
							 }
					    }
						
					} catch (MiddlewareQueryException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					
					//genoManager.getGIDsFromCharValuesByMarkerId(arg0, arg1, arg2)
					
					for(int m=0;m<markersListH.size();m++){
						try{
							
							//genoManager.getGIDsFromCharValuesByMarkerId(arg0, arg1, arg2)
							List<Integer> gids = genoManager.getGIDsFromCharValuesByMarkerId(Integer.parseInt(markersListH.get(m).toString()), 0, (int)genoManager.countGIDsFromCharValuesByMarkerId(Integer.parseInt(markersListH.get(m).toString())));
							//System.out.println(gids);
							for(int g=0;g<gids.size();g++){
								if(!gidsList.contains(Integer.parseInt(gids.get(g).toString())))
								gidsList.add(Integer.parseInt(gids.get(g).toString()));
							}
						} catch (MiddlewareQueryException e) {
							_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
							return;
						}
					}	
					//System.out.println("gids=:"+gidsList);
					
					
					glist = new ArrayList();
					try{
						
						//genoManager.getAllelicValuesByGidsAndMarkerNames(arg0, markersListH)
						List<AllelicValueElement> allelesByMarker=genoManager.getAllelicValuesByGidsAndMarkerNames(gidsList, hapMarkersList);
						//List<AllelicValueElement> allelesByMarker=genoManager.getAlleleValuesByMarkers(markersListH);
						//System.out.println("allelesByMarker:"+allelesByMarker);
						for(AllelicValueElement resAlleles:allelesByMarker){
							if(! markersList.contains(resAlleles.getMarkerId().toString())){
								markersList.add(resAlleles.getMarkerId().toString());
								//markersMap.put(strMareO[1].toString(), strMareO[0]);
							}
									
							markerAlleles.put(resAlleles.getGid().toString()+"!~!"+resAlleles.getMarkerId().toString(), resAlleles.getData().toString());
								
							if(!(glist.contains(Integer.parseInt(resAlleles.getGid().toString()))))
								glist.add(Integer.parseInt(resAlleles.getGid().toString()));		
						}
					} catch (MiddlewareQueryException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					

					/*System.out.println("...........:"+markersList);
					System.out.println("^^^^^^^^  :"+markersMap);
					System.out.println("#############  :"+markerAlleles);
					System.out.println(",,,,,,,,,,,,,,:"+glist);*/
					List markerKey = new ArrayList();
					markerKey.addAll(markerAlleles.keySet());
					String gidsList1="";
					for(int g=0; g<glist.size(); g++){
						gidsList1=gidsList1+glist.get(g)+",";
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
					//System.out.println("gidsList1:"+gidsList1);
					gidsList1=gidsList1.substring(0, gidsList1.length()-1);
					/*System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$   :"+mapEx);
					System.out.println("glist:"+glist);*/
					
					String strNidsQuerry="SELECT nid FROM gdms_acc_metadataset WHERE gid IN("+gidsList1+")";
					//System.out.println("strNidsQuerry:"+strNidsQuerry);
					List nidsFromLocal=new ArrayList();	
					List nidsFromCentral=new ArrayList();	
					//List markersList=new ArrayList();		
					List nidsList=new ArrayList();
					//List gidsList=new ArrayList();
					Object objNL=null;
					Object objNC=null;
					Iterator itListNC=null;
					Iterator itListNL=null;						
					//try{
						
						try{
							localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
							centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
						}catch (Exception e){
							e.printStackTrace();
						}
						
						SQLQuery queryNidsC=centralSession.createSQLQuery(strNidsQuerry);		
						queryNidsC.addScalar("nid",Hibernate.INTEGER);	  
						
						nidsFromCentral=queryNidsC.list();
						itListNC=nidsFromCentral.iterator();			
						while(itListNC.hasNext()){
							objNC=itListNC.next();
							if(objNC!=null){
								if(! nidsList.contains(Integer.parseInt(objNC.toString()))){
									nidsList.add(Integer.parseInt(objNC.toString()));										
								}
							}
						}
						
						SQLQuery queryNidsL=localSession.createSQLQuery(strNidsQuerry);		
						queryNidsL.addScalar("nid",Hibernate.INTEGER);	  
						
						nidsFromLocal=queryNidsL.list();
						itListNL=nidsFromLocal.iterator();			
						while(itListNL.hasNext()){
							objNL=itListNL.next();
							if(objNL!=null){
								if(! nidsList.contains(Integer.parseInt(objNL.toString()))){
									nidsList.add(Integer.parseInt(objNL.toString()));	
									//markers=markers+objL.toString()+",";
								}
							}
						}
						
						//genoManager.getN
						/*List<AccMetadataSetPK> accMetadataSets = genoManager.getGdmsAccMetadatasetByGid(glist, 0, (int) genoManager.countGdmsAccMetadatasetByGid(glist));
				       // sysprintln(0, "testGetGdmsAccMetadatasetByGid() RESULTS: ");
				        for (AccMetadataSetPK accMetadataSet : accMetadataSets) {
				            //Debug.println(0, accMetadataSet.toString());
				        	nIDs.add(accMetadataSet.getNameId());
				        }*/
						
						
					/*} catch (MiddlewareQueryException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
						return;
					}*/
					//System.out.println("~~~~~~~~~~~~~~~~  :"+nIDs);
					Name names = null;
					//manager.getGermplasmNameByID(nIDs)
					try{
						for(int n=0;n<nidsList.size();n++){
							names=manager.getGermplasmNameByID(Integer.parseInt(nidsList.get(n).toString()));
							//System.out.println("names:"+names);
							if(!germplasmNamesList.contains(names.getNval())){
								germplasmNamesList.add(names.getNval());
								hashMapGidsGermplasmNames.put(names.getGermplasmId(), names.getNval());
							}
						}
					} catch (MiddlewareQueryException e) {
						_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
						return;
					}
					
					//System.out.println("hashMapGidsGermplasmNames:"+hashMapGidsGermplasmNames);
					
					
					String strHaplotype="";
					
					String finalData="";
					//System.out.println("Germplasm names=:"+germplasmNamesList);
					HashMap<String,Object> mAlleles= new HashMap<String,Object>();
					for(int m=0;m<glist.size();m++){
						//System.out.println("^!^   "+m+":"+glist.get(m)+"   "+mapEx.get(Integer.parseInt(glist.get(m).toString())));
						for (int k=0;k<markersListH.size();k++){
							mAlleles=(HashMap)mapEx.get(Integer.parseInt(glist.get(m).toString()));
							if(mAlleles.containsKey(glist.get(m).toString()+"!~!"+markersListH.get(k).toString())){
								String alleleValue=markerAlleles.get(glist.get(m).toString()+"!~!"+markersListH.get(k).toString()).toString();
							 
								if(alleleValue.contains("/")){
									if((alleleValue.length()==3 && alleleValue.matches("-"))||(alleleValue.equals("?"))){										
										finalData="N";
									}else{
										String[] strAllele=alleleValue.split("/");
										//System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
										if(strAllele[0].equalsIgnoreCase(strAllele[1]))
											finalData=strAllele[0];
										else
											finalData=strAllele[0]+"/"+strAllele[1];
											//finalData=strAllele[0]+"/"+strAllele[1];
									}
								}else if(alleleValue.contains(":")){
									if((alleleValue.length()==3 && alleleValue.matches("-"))||(alleleValue.equals("?"))){									
										finalData="N";
									}else{
										String[] strAllele=alleleValue.split(":");
										//System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
										if(strAllele[0].equalsIgnoreCase(strAllele[1]))
											finalData=strAllele[0];
										else
											finalData=strAllele[0]+"/"+strAllele[1];
											//finalData=strAllele[0]+"/"+strAllele[1];
									}
								}else{
									if((alleleValue.length()==3 && alleleValue.matches("-"))||(alleleValue.equals("?"))){													
										finalData="N";
									}else{
										finalData=alleleValue;
									}
								}	
								
								strHaplotype=strHaplotype+finalData+",";
							}
						}
						strHaplotype=strHaplotype.substring(0, strHaplotype.length()-1);
						//System.out.println("!@!@!");
						//System.out.println("strHaplotype=:"+hashMapGidsGermplasmNames.get(Integer.parseInt(glist.get(m).toString()))+"    "+strHaplotype);
						hapsList.add(hashMapGidsGermplasmNames.get(Integer.parseInt(glist.get(m).toString()))+"!~!"+strHaplotype);
						strHaplotype="";
					}
				}
				Component newQTLResultsPanel = buildHapsResultsComponent();
				_tabsheetForHaps.replaceComponent(buildHapResultsComponent, newQTLResultsPanel);
				_tabsheetForHaps.requestRepaint();
				buildHapResultsComponent = newQTLResultsPanel;
				_tabsheetForHaps.getTab(1).setEnabled(true);
				_tabsheetForHaps.setSelectedTab(1);
			}
		});
		
		//if(_tableWithAllHAPs!= null && _tableWithAllHAPs.size()>0){
		searchLayout.addComponent(layoutForButton);
		searchLayout.setComponentAlignment(layoutForButton, Alignment.MIDDLE_CENTER);
		//}
		return searchLayout;
	}

	private Component buildHapsResultsComponent() {
		VerticalLayout searchHapsResLayout = new VerticalLayout();
		searchHapsResLayout.setCaption("Results");
		searchHapsResLayout.setMargin(true, true, true, true);
		searchHapsResLayout.setSpacing(true);
		String lbl1="";
		int iNumOfQTLsFound = 0;
		if(op=="allele"){
			if (null != hapsList){
				iNumOfQTLsFound = hapsList.size();
				
				lbl1="for "+markersList+" markers and allele "+trackNames.substring(0, trackNames.length()-1);
				
				/*Label lblSelectedHaps = new Label(lbl1);
				lblSelectedHaps.setStyleName("haplabel");
				
				searchHapsResLayout.addComponent(lblSelectedHaps);
				searchHapsResLayout.setComponentAlignment(lblSelectedHaps, Alignment.TOP_CENTER);*/
				Label lblGermplasmsFound = new Label(iNumOfQTLsFound + " Lines's Found "+lbl1);
				lblGermplasmsFound.setStyleName(Reindeer.LABEL_H2);
				searchHapsResLayout.addComponent(lblGermplasmsFound);
				searchHapsResLayout.setComponentAlignment(lblGermplasmsFound, Alignment.TOP_CENTER);
			}
		}else{
			if (null != hapsList){
				iNumOfQTLsFound = hapsList.size();
				//button.setDescription("This is the tooltip");
				//System.out.println(trackNames.substring(0, trackNames.length()));
				lbl1=trackNames+" consists of the following SNPs "+hapMarkersList;
				
				Label lblSelectedHaps = new Label(lbl1);
				lblSelectedHaps.setStyleName("haplabel");
				
				searchHapsResLayout.addComponent(lblSelectedHaps);
				searchHapsResLayout.setComponentAlignment(lblSelectedHaps, Alignment.TOP_CENTER);
				Label lblGermplasmsFound = new Label(iNumOfQTLsFound + " Lines's Found ");
				lblGermplasmsFound.setStyleName(Reindeer.LABEL_H2);
				searchHapsResLayout.addComponent(lblGermplasmsFound);
				searchHapsResLayout.setComponentAlignment(lblGermplasmsFound, Alignment.TOP_CENTER);
			}
		
		}
		
		
		
		if (0 != iNumOfQTLsFound){
			Table tableForHapsCreatedResults = buildGNamesHapsTable(searchHapsResLayout);
			tableForHapsCreatedResults.setWidth("100%");
			tableForHapsCreatedResults.setPageLength(10);
			tableForHapsCreatedResults.setSelectable(true);
			tableForHapsCreatedResults.setColumnCollapsingAllowed(false);
			tableForHapsCreatedResults.setColumnReorderingAllowed(true);
			tableForHapsCreatedResults.setStyleName("strong");
			searchHapsResLayout.addComponent(tableForHapsCreatedResults);
			searchHapsResLayout.setComponentAlignment(tableForHapsCreatedResults, Alignment.MIDDLE_CENTER);
		}	
		return searchHapsResLayout;
	}
	
	private Table buildGNamesHapsTable(final VerticalLayout resultsLayout) {
		
		_hapsTable = new Table();
		_hapsTable.setStyleName("markertable");
		_hapsTable.setPageLength(10);
		_hapsTable.setSelectable(true);
		_hapsTable.setColumnCollapsingAllowed(true);
		_hapsTable.setColumnReorderingAllowed(true);

		if(op=="allele"){
			String[] strArrayOfColNames = {"GERMPLASM-NAME", "ALLELE", "MARKER"};	
			//String[] strArrayOfColNames = {"GERMPLASM-NAME", "ALLELE"};
			for (int i = 0; i < strArrayOfColNames.length; i++){			
				_hapsTable.addContainerProperty(strArrayOfColNames[i], String.class, null);			
			}
		}else{
			String[] strArrayOfColNames = {"GERMPLASM-NAME", "HAPLOTYPE"};	
			for (int i = 0; i < strArrayOfColNames.length; i++){			
				_hapsTable.addContainerProperty(strArrayOfColNames[i], String.class, null);			
			}
		}
		
		
		
		if (null != hapsList){			
			for (int h = 0; h < hapsList.size(); h++){
				String[] strHaps= hapsList.get(h).toString().split("!~!");			
				String germplasmName=strHaps[0];				
				String haplotype = strHaps[1];		
				if(op=="allele"){
					_hapsTable.addItem(new Object[] {germplasmName, haplotype, strHaps[2]}, new Integer(h));
				}else
					_hapsTable.addItem(new Object[] {germplasmName, haplotype}, new Integer(h));
			}
		}
		
		return _hapsTable;
	}
	@Override
	public void componentEvent(Event event) {
		// TODO Auto-generated method stub
		event.getComponent().requestRepaint();
	}
	
	private void buildOnLoad(final HorizontalLayout horizontalLayout, String theSearchString, String option) {
		
		horizontalLayout.removeAllComponents();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setSizeFull();
		List<String> retrieveHapNamesFinal = new ArrayList<String>();
		ArrayList<String> listOfSNPs = new ArrayList<String>();
		try{
			
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			if(option=="haps"){
				String strQuerry="select track_name from gdms_track_data";	
				if(null != theSearchString && false == theSearchString.equals("*")) {
					strQuerry="select track_name from gdms_track_data where track_name like '"+theSearchString+"%'";	
				}else{
					strQuerry="select track_name from gdms_track_data";	
				}
				//System.out.println(strQuerry);			
				List snpsFromLocal=new ArrayList();		
				List markersList=new ArrayList();		
				List snpsFromCentral=new ArrayList();
			
				Object obj=null;
				Object objL=null;
				Iterator itListC=null;
				Iterator itListL=null;		
					
				SQLQuery queryL=localSession.createSQLQuery(strQuerry);		
				queryL.addScalar("track_name",Hibernate.STRING);	  
				
				snpsFromLocal=queryL.list();
				itListL=snpsFromLocal.iterator();			
				while(itListL.hasNext()){
					objL=itListL.next();
					if(objL!=null){
						if(! retrieveHapNamesFinal.contains(objL.toString()))
							retrieveHapNamesFinal.add(objL.toString());	
					}
				}	
					
					
				SQLQuery queryC=centralSession.createSQLQuery(strQuerry);		
				queryC.addScalar("track_name",Hibernate.STRING);	
				snpsFromCentral=queryC.list();			
				itListC=snpsFromCentral.iterator();			
				while(itListC.hasNext()){
					obj=itListC.next();
					if(obj!=null){		
						if(! retrieveHapNamesFinal.contains(obj.toString()))
							retrieveHapNamesFinal.add(obj.toString());
					}
				}	
			}else{
				List<String> markersGiven=new ArrayList<String>();
				List<Integer> markerIds=new ArrayList<Integer>();
				String marker_ids="";
				if(theSearchString.equals("*")){
					//genoManager.getnids
				}else{
					markersGiven.add(theSearchString);
					//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$  :"+markersGiven);
					List<Marker> markers=new ArrayList();
					markers=genoManager.getMarkersByMarkerNames(markersGiven, 0, markersGiven.size(), Database.CENTRAL);
					if(markers.size() == 0){				
						markers=genoManager.getMarkersByMarkerNames(markersGiven, 0, markersGiven.size(), Database.LOCAL);
					}
					
					for(Marker res : markers){
						markerIds.add(res.getMarkerId());
					}
				}
				for(int m=0;m<markerIds.size(); m++){
					marker_ids=marker_ids+markerIds.get(m)+",";
				}
				//System.out.println("^^^^^   :"+genoManager.getAlleleValuesByMarkers(markerIds));
				/*List<AlleleValues> resAlleles=genoManager.getAlleleValuesByMarkers(markerIds);
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@  :"+resAlleles);
				for (AlleleValues list:resAlleles){
					if(!retrieveHapNamesFinal.contains(list.getAlleleBinValue())){
						retrieveHapNamesFinal.add(list.getAlleleBinValue());
						System.out.println(list.getAlleleBinValue());
					}
				}*/
				
				//String strQuerry="select track_name from gdms_track_data";	
				
				
				
			try{
				List<AllelicValueElement> allelesByMarker=genoManager.getAlleleValuesByMarkers(markerIds);
				
				for(AllelicValueElement res:allelesByMarker){
					if(res.getData()!=null){
						//System.out.println((!res.getData().toString().equals("-"))+"  "+res.getData().toString());
						if(!retrieveHapNamesFinal.contains(res.getData())){
							if((!res.getData().contains("-"))||(!res.getData().equals(" "))||(!res.getData().trim().equals("N"))){
								String finalData="";
								String alleleValue=res.getData();
								if(alleleValue.contains(":")){
									String[] strAllele=alleleValue.split(":");
									////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
									if(strAllele[0].equalsIgnoreCase(strAllele[1]))
										finalData=strAllele[0];
									else
										finalData=strAllele[0]+"/"+strAllele[1];
								}else{
									finalData=alleleValue;
								}
								if(! retrieveHapNamesFinal.contains(finalData))
								retrieveHapNamesFinal.add(finalData);
							}
						}
					}else{
						if((! retrieveHapNamesFinal.contains(res.getAlleleBinValue()))&&(!res.getAlleleBinValue().equals("0/0"))){
							
							String finalData="";
							String alleleValue=res.getAlleleBinValue();
							if(alleleValue.contains(":")){
								String[] strAllele=alleleValue.split(":");
								////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
								if(strAllele[0].equalsIgnoreCase(strAllele[1]))
									finalData=strAllele[0];
								else
									finalData=strAllele[0]+"/"+strAllele[1];
							}else{
								finalData=alleleValue;
							}
							if(! retrieveHapNamesFinal.contains(finalData))
							retrieveHapNamesFinal.add(finalData);
							
							
							//retrieveHapNamesFinal.add(res.getAlleleBinValue());
						}
					}
				}
				
			} catch (MiddlewareQueryException e) {
				_mainHomePage.getMainWindow().getWindow().showNotification(e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
				return;
			}
				
				
				/*String strQuerry = "select allele_bin_value from gdms_allele_values where marker_id in("+marker_ids.substring(0, marker_ids.length()-1)+")";
				List snpsFromLocal=new ArrayList();		
				List markersList=new ArrayList();		
				List snpsFromCentral=new ArrayList();
			
				Object obj=null;
				Object objL=null;
				Iterator itListC=null;
				Iterator itListL=null;		
					
				SQLQuery queryC=centralSession.createSQLQuery(strQuerry);		
				queryC.addScalar("allele_bin_value",Hibernate.STRING);	
				snpsFromCentral=queryC.list();			
				itListC=snpsFromCentral.iterator();			
				while(itListC.hasNext()){
					obj=itListC.next();
					if(obj!=null){		
						if(! retrieveHapNamesFinal.contains(obj.toString()))
							retrieveHapNamesFinal.add(obj.toString());
					}
				}	
				SQLQuery queryL=localSession.createSQLQuery(strQuerry);		
				queryL.addScalar("allele_bin_value",Hibernate.STRING);	  
				
				snpsFromLocal=queryL.list();
				itListL=snpsFromLocal.iterator();			
				while(itListL.hasNext()){
					objL=itListL.next();
					if(objL!=null){
						if(! retrieveHapNamesFinal.contains(objL.toString()))
							retrieveHapNamesFinal.add(objL.toString());	
					}
				}	
				
				//if()
				String strQuerryC = "select char_value from gdms_char_values where marker_id in("+marker_ids.substring(0, marker_ids.length()-1)+")";
				snpsFromLocal=new ArrayList();		
				markersList=new ArrayList();		
				snpsFromCentral=new ArrayList();
			
				obj=null;
				objL=null;
				itListC=null;
				itListL=null;		
					
				SQLQuery query1C=centralSession.createSQLQuery(strQuerryC);		
				query1C.addScalar("char_value",Hibernate.STRING);	
				snpsFromCentral=query1C.list();			
				itListC=snpsFromCentral.iterator();			
				while(itListC.hasNext()){
					obj=itListC.next();
					if(obj!=null){		
						if(! retrieveHapNamesFinal.contains(obj.toString()))
							retrieveHapNamesFinal.add(obj.toString());
					}
				}	
				SQLQuery query1L=localSession.createSQLQuery(strQuerryC);		
				query1L.addScalar("char_value",Hibernate.STRING);	  
				
				snpsFromLocal=query1L.list();
				itListL=snpsFromLocal.iterator();			
				while(itListL.hasNext()){
					objL=itListL.next();
					if(objL!=null){
						if(! retrieveHapNamesFinal.contains(objL.toString()))
							retrieveHapNamesFinal.add(objL.toString());	
					}
				}*/
				
				
			}
			//System.out.println("option:"+option+"  %%%%%%%%%%%%%%%%%%%%%%% markersList=:"+retrieveHapNamesFinal);					
		
		if (0 < retrieveHapNamesFinal.size()) {
			_tableWithAllHAPs = new Table();
			_tableWithAllHAPs.setWidth("100%");
			_tableWithAllHAPs.setPageLength(5);
			_tableWithAllHAPs.setSelectable(false);
			_tableWithAllHAPs.setColumnCollapsingAllowed(false);
			_tableWithAllHAPs.setColumnReorderingAllowed(false);
			_tableWithAllHAPs.setEditable(false);
			_tableWithAllHAPs.setStyleName("strong");
			horizontalLayout.addComponent(_tableWithAllHAPs);
			if(option.equalsIgnoreCase("allele")){
				//System.out.println("if allele");
				_tableWithAllHAPs.addContainerProperty("Select", CheckBox.class, null);
				_tableWithAllHAPs.addContainerProperty("Allele", String.class, null);
				_tableWithAllHAPs.setColumnWidth("Select", 40);
				_tableWithAllHAPs.setColumnWidth("Allele", 500);
			}else{
				//System.out.println("if Haplotype");
				_tableWithAllHAPs.addContainerProperty("Select", CheckBox.class, null);
				_tableWithAllHAPs.addContainerProperty("Haplotype Name", String.class, null);
				_tableWithAllHAPs.setColumnWidth("Select", 40);
				_tableWithAllHAPs.setColumnWidth("Haplotype Name", 500);
			}
			int i = 0;
			for (String strHapName : retrieveHapNamesFinal) {
				_tableWithAllHAPs.addItem(new Object[]{new CheckBox(), strHapName}, new Integer(i));
				i++;
			}
			
		}
		btnNext.setEnabled(true);
		} catch (Exception e) {
			/*_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Germplasm Names from the database", Notification.TYPE_ERROR_MESSAGE);
			return null;*/
			e.printStackTrace();
		}
		
	}
	
	
	
	
}
