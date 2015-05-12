package org.icrisat.gdms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.maporqtl.HaplotypeUpload;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

public class UploadHaplotypesComponent implements Component.Listener {
	
	
	private static final long serialVersionUID = -8029538406915003384L;
	private GDMSModel _gdmsModel;
	private GDMSMain _mainHomePage;
	private String _strMarkerType;
	private TabSheet _tabsheet;
	
	String hapValue="";
	
	ManagerFactory factory =null;
	
	GenotypicDataManager genoManager;
	private Session localSession;
	private Session centralSession;
	private ArrayList<String> listOfSNPsSelected = new ArrayList<String>();
	private List emptyList=new ArrayList();
	
	public UploadHaplotypesComponent(GDMSMain theMainHomePage, String theMarkerType){
		_gdmsModel = GDMSModel.getGDMSModel();
		_mainHomePage = theMainHomePage;
		_strMarkerType = theMarkerType;
		
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();		
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
			
			genoManager=factory.getGenotypicDataManager();
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public HorizontalLayout buildTabbedComponentForHaplotypes() {

		HorizontalLayout horizontalLayout = new HorizontalLayout();

		_tabsheet = new TabSheet();
		_tabsheet.setWidth("700px");
		
		Component buildHapSearchComponent = buildComponentForHaplotypes();
		buildHapSearchComponent.setSizeFull();


		_tabsheet.addComponent(buildHapSearchComponent);
		
		_tabsheet.getTab(0).setEnabled(true);
		
		horizontalLayout.addComponent(_tabsheet);
		
		return horizontalLayout;
		
	}	
	
	
	public VerticalLayout buildComponentForHaplotypes(){

		VerticalLayout layoutForHapsTab = new VerticalLayout();
		layoutForHapsTab.setCaption("Haplotypes");
		layoutForHapsTab.setSpacing(true);
		layoutForHapsTab.setSizeFull();
		layoutForHapsTab.setMargin(true, true, true, true);
		
		VerticalLayout verticalLayoutForHap = new VerticalLayout();
		verticalLayoutForHap.setSpacing(true);
		verticalLayoutForHap.setMargin(true, true, true, false);
		
		
		
		HorizontalLayout horizontalLayoutForHap = new HorizontalLayout();
		horizontalLayoutForHap.setSpacing(true);
		horizontalLayoutForHap.setMargin(false, true, true, false);
		
		
		String strIntroPara1 = "<B>Name the Haplotype :   ";	
		Label lblPara = new Label(strIntroPara1 , Label.CONTENT_XHTML);
		horizontalLayoutForHap.addComponent(lblPara);
		
		final TextField txtFieldHapName = new TextField();
		txtFieldHapName.setWidth("150px");
		txtFieldHapName.setImmediate(true);
		horizontalLayoutForHap.addComponent(txtFieldHapName);
		//horizontalLayoutForHap.setComponentAlignment(childComponent, alignment)
		verticalLayoutForHap.addComponent(horizontalLayoutForHap);
		verticalLayoutForHap.setComponentAlignment(horizontalLayoutForHap, Alignment.TOP_CENTER);
		
		/*HorizontalLayout horizontalLayoutForHapValue = new HorizontalLayout();
		horizontalLayoutForHapValue.setSpacing(true);
		horizontalLayoutForHapValue.setMargin(false, true, true, false);
		
		
		String strHapValue = "<B>Haplotype :   ";	
		Label lblParaValue = new Label(strHapValue , Label.CONTENT_XHTML);
		horizontalLayoutForHapValue.addComponent(lblParaValue);
		
		final TextField txtFieldHaplotype = new TextField();
		txtFieldHaplotype.setWidth("450px");
		txtFieldHaplotype.setImmediate(true);
		horizontalLayoutForHapValue.addComponent(txtFieldHaplotype);
		verticalLayoutForHap.addComponent(horizontalLayoutForHapValue);*/
		
		
		final ArrayList<String> listOfSNPs = getListOfMarkersforHaplotypesCreation();

		Label lblTitle = new Label("Select SNPs from the list");
		if (null != listOfSNPs){
			if (0 != listOfSNPs.size()){
				lblTitle = new Label("Select from the list of " + listOfSNPs.size() + " SNPs");
			}
		}

		final TwinColSelect selectForSNPs = new TwinColSelect();
		selectForSNPs.setLeftColumnCaption("All SNPs");
		selectForSNPs.setRightColumnCaption("Selected SNPs");
		
		//selectForSNPs.setNullSelectionAllowed(false);
		//selectForSNPs.setInvalidAllowed(false);
		selectForSNPs.setWidth("450px");
		for (String strMarker : listOfSNPs) {
			selectForSNPs.addItem(strMarker);
		}
		selectForSNPs.setRows(20);
		/*selectForSNPs.setRows(20);
		selectForSNPs.setColumns(20);*/
		
		selectForSNPs.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			public void valueChange(ValueChangeEvent event) {
                if (event.getProperty().getValue() != null) {
                	TwinColSelect colSelect = (TwinColSelect)event.getProperty();
                	Object value = colSelect.getValue();
                	Set<String> hashSet = (Set<String>) value;
                	listOfSNPsSelected = new ArrayList<String>();
                	for (String string : hashSet) {
                		listOfSNPsSelected.add(string);
					}                	
                }
            }
        });
		selectForSNPs.setImmediate(true);
		
		
		HorizontalLayout horizLytForSelectComponent = new HorizontalLayout();
		//horizLytForSelectComponent.setSizeFull();
		horizLytForSelectComponent.setWidth("650px");
		horizLytForSelectComponent.setSpacing(true);
		horizLytForSelectComponent.setMargin(false);
		horizLytForSelectComponent.addComponent(selectForSNPs);
		horizLytForSelectComponent.setComponentAlignment(selectForSNPs, Alignment.MIDDLE_CENTER);
		
		final TextField txtFieldSearch = new TextField();
		txtFieldSearch.setWidth("300px");
		txtFieldSearch.setImmediate(true);
		txtFieldSearch.addListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void textChange(TextChangeEvent event) {
				ArrayList<String> arrayListOfMarkerNamesFromTextField = new ArrayList<String>();
				String strMarkerNames = txtFieldSearch.getValue().toString();
				if (strMarkerNames.endsWith("*")){
					int indexOf = strMarkerNames.indexOf('*');
					String substring = strMarkerNames.substring(0, indexOf);
					
					for (String strSNP : listOfSNPs){
						if (strSNP.toUpperCase().startsWith(substring) ||
						    strSNP.toLowerCase().startsWith(substring)) {
							arrayListOfMarkerNamesFromTextField.add(strSNP);
						}
					}
				} else if (strMarkerNames.trim().equals("*")) {
					arrayListOfMarkerNamesFromTextField.addAll(listOfSNPs);
				} 
				selectForSNPs.setValue(arrayListOfMarkerNamesFromTextField);
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
					
					for (String strSNP : listOfSNPs){
						if (strSNP.toUpperCase().startsWith(substring) ||
							 strSNP.toLowerCase().startsWith(substring)) {
							arrayListOfMarkerNamesFromTextField.add(strSNP);
						}
					}
				} else if (strMarkerNames.trim().equals("*")) {
					arrayListOfMarkerNamesFromTextField.addAll(listOfSNPs);
				} 
				selectForSNPs.setValue(arrayListOfMarkerNamesFromTextField);
			}
		});

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(false, true, true, false);
		
		horizontalLayout.addComponent(txtFieldSearch);
		horizontalLayout.addComponent(searchButton);

		HorizontalLayout horizontalLayoutForButton = new HorizontalLayout();
		Button btnSubmit = new Button("Submit");
		horizontalLayoutForButton.addComponent(btnSubmit);
		//horizontalLayoutForButton.setComponentAlignment(btnSubmit, Alignment.MIDDLE_CENTER);
		horizontalLayoutForButton.setMargin(true, false, true, true);
		btnSubmit.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				
				listOfSNPsSelected = new ArrayList<String>();
				hapValue=txtFieldHapName.getValue().toString();
				Object value2 = selectForSNPs.getValue();
				Set<String> hashSet = (Set<String>) value2;
				for (String string : hashSet) {
					listOfSNPsSelected.add(string);
				}
				//System.out.println("%%%%%%%%%%%  :"+listOfSNPsSelected+"     "+hapValue);
				if (0 == listOfSNPsSelected.size()){
					_mainHomePage.getMainWindow().getWindow().showNotification("Please select SNPs", Notification.TYPE_ERROR_MESSAGE);
					return;
				}else{
					try{
						HaplotypeUpload hapUpload=new HaplotypeUpload();
						String op=hapUpload.createObjectsToBeSavedToDB(hapValue, listOfSNPsSelected);
					
						//String strDataUploaded = uploadMarker.getDataUploaded();
						if(op.equalsIgnoreCase("successfull")){
							Window messageWindow = new Window("Upload Message");
							GDMSInfoDialog gdmsMessageWindow = new GDMSInfoDialog(_mainHomePage,emptyList, "Data uploaded successfully \r\n\n" + op);
							messageWindow.addComponent(gdmsMessageWindow);
							messageWindow.setWidth("500px");
							messageWindow.setBorder(Window.BORDER_NONE);
							messageWindow.setClosable(true);
							messageWindow.center();
		
							if (!_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
								_mainHomePage.getMainWindow().addWindow(messageWindow);
							} 
						}
					}catch(GDMSException e){
						e.printStackTrace();
						Window messageWindow = new Window("Haplotype(s) Upload Error Message");
						
						GDMSInfoDialog gdmsMessageWindow = new GDMSInfoDialog(_mainHomePage, emptyList, "Error uploading SNPGenotype \r\n\n" + 
						                                                      e.getMessage());
						messageWindow.addComponent(gdmsMessageWindow);
						messageWindow.setWidth("500px");
						messageWindow.setBorder(Window.BORDER_NONE);
						messageWindow.setClosable(true);
						messageWindow.center();
						
						if (!_mainHomePage.getMainWindow().getChildWindows().contains(messageWindow)) {
							_mainHomePage.getMainWindow().addWindow(messageWindow);
						}
					}
				}
			}
		});		
		
		
		layoutForHapsTab.addComponent(verticalLayoutForHap);
		layoutForHapsTab.setComponentAlignment(verticalLayoutForHap, Alignment.TOP_CENTER);
		

		//layoutForHapsTab.addComponent(lblTitle);
		//layoutForHapsTab.setComponentAlignment(lblTitle, Alignment.TOP_CENTER);
		//layoutForHapsTab.addComponent(horizontalLayout);
		//layoutForHapsTab.addComponent(gridLayout);
		layoutForHapsTab.addComponent(horizLytForSelectComponent);
		layoutForHapsTab.setComponentAlignment(horizLytForSelectComponent, Alignment.MIDDLE_CENTER);
		
		
		layoutForHapsTab.addComponent(horizontalLayoutForButton);
		layoutForHapsTab.setComponentAlignment(horizontalLayoutForButton, Alignment.BOTTOM_CENTER);
		
		return layoutForHapsTab;
	
	}
	
	

	@Override
	public void componentEvent(Event event) {
		// TODO Auto-generated method stub
		
	}
	
	
	public ArrayList<String> getListOfMarkersforHaplotypesCreation() {
		ArrayList<String> listOfSNPs = new ArrayList<String>();
		try{
			
			String strQuerry="select marker_name from gdms_marker where UPPER(marker_type)='SNP'";			
			//System.out.println(strQuerry);			
			List snpsFromLocal=new ArrayList();		
			List markersList=new ArrayList();		
			List snpsFromCentral=new ArrayList();
			
			Object obj=null;
			Object objL=null;
			Iterator itListC=null;
			Iterator itListL=null;		
					
			SQLQuery queryL=localSession.createSQLQuery(strQuerry);		
			queryL.addScalar("marker_name",Hibernate.STRING);	  
			
			snpsFromLocal=queryL.list();
			itListL=snpsFromLocal.iterator();			
			while(itListL.hasNext()){
				objL=itListL.next();
				if(objL!=null){
					if(! listOfSNPs.contains(objL.toString()))
						listOfSNPs.add(objL.toString());	
				}
			}
				
				
				
			SQLQuery queryC=centralSession.createSQLQuery(strQuerry);		
			queryC.addScalar("marker_name",Hibernate.STRING);	
			snpsFromCentral=queryC.list();			
			itListC=snpsFromCentral.iterator();			
			while(itListC.hasNext()){
				obj=itListC.next();
				if(obj!=null){		
					if(! listOfSNPs.contains(obj.toString()))
						listOfSNPs.add(obj.toString());
				}
			}
					
			
			//System.out.println("%%%%%%%%%%%%%%%%%%%%%%% markersList=:"+listOfSNPs);
			
		} catch (Exception e) {
			/*_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Germplasm Names from the database", Notification.TYPE_ERROR_MESSAGE);
			return null;*/
			e.printStackTrace();
		}
		
		
		return listOfSNPs;
	}
	
	
	
}
