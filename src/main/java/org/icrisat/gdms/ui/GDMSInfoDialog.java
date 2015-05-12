package org.icrisat.gdms.ui;


import java.util.List;

import org.generationcp.middleware.pojos.gdms.MarkerInfo;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class GDMSInfoDialog extends CustomComponent {

	private static final long serialVersionUID = 1L;
	private GDMSMain _mainHomePage;
	//private List<MarkerInfo> _strMessage;
	private List _strMessage;
	private String finalMsg="";
	private Button _okButton;
	private Table _tableWithMarkersOfQTL;
	private TextArea textArea;
	
	public GDMSInfoDialog(GDMSMain theMainHomePage, List theMessage, String msg) {
		_mainHomePage = theMainHomePage;
		_strMessage = theMessage;
		finalMsg=msg;
		System.out.println("%$%$%$%$%$%%%%%%%%%%%%%   :"+_strMessage.size());
		setCompositionRoot(buildMessageWindow());
	}


	public VerticalLayout buildMessageWindow() {
		String markerName, markerType, species, markerId, ploidy, motif, forwardPrimer,
		reversePrimer, annealingTemp, principalInvestigator,
		contact, institute ="";
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setCaption("Upload Message");
		String[] strArrayOfColNames = {"MARKER-NAME", "MARKER-TYPE", "SPECIES", "MARKER-ID", "MOTIF-TYPE", "MOTIF", "FORWARD-PRIMER", "REVERSE-PRIMER", "ANNEALING-TEMPERATURE",				
				"PRINCIPAL-INVESTIGATOR", "CONTACT",
				"INSTITUTE"};
		String[] strDataArray;
		if (0 < _strMessage.size()) {
			
			_tableWithMarkersOfQTL = new Table();
			_tableWithMarkersOfQTL.setWidth("100%");
			_tableWithMarkersOfQTL.setPageLength(5);
			_tableWithMarkersOfQTL.setSelectable(false);
			_tableWithMarkersOfQTL.setColumnCollapsingAllowed(false);
			_tableWithMarkersOfQTL.setColumnReorderingAllowed(false);
			_tableWithMarkersOfQTL.setEditable(false);
			_tableWithMarkersOfQTL.setStyleName("strong");
			verticalLayout.addComponent(_tableWithMarkersOfQTL);
			for (int i = 0; i < strArrayOfColNames.length; i++){
				_tableWithMarkersOfQTL.addContainerProperty(strArrayOfColNames[i], String.class, null);
			}
			int i = 0;
			//for (MarkerInfo strMarkers : _strMessage) {
			for(int m=0;m<_strMessage.size();m++){
				String[] markerDet=_strMessage.get(m).toString().split("!~!");
				/*markerName=strMarkers.getMarkerName();
				markerType=strMarkers.getMarkerType();
				species=strMarkers.getSpecies();
				markerId=strMarkers.getMarkerId().toString();
				ploidy=strMarkers.getPloidy();
				motif=strMarkers.getMotif();
				forwardPrimer=strMarkers.getForwardPrimer();
				reversePrimer=strMarkers.getReversePrimer();
				annealingTemp=strMarkers.getAnnealingTemp().toString();
				principalInvestigator=strMarkers.getPrincipalInvestigator();
				contact=strMarkers.getContact();
				institute=strMarkers.getInstitute();*/
				
				markerName=markerDet[1];
				markerType=markerDet[2];
				species=markerDet[3];
				markerId=markerDet[0];
				ploidy=markerDet[4];
				motif=markerDet[5];
				forwardPrimer=markerDet[6];
				reversePrimer=markerDet[7];
				annealingTemp=markerDet[8];
				principalInvestigator=markerDet[9];
				contact=markerDet[10];
				institute=markerDet[11];
				
				
				_tableWithMarkersOfQTL.addItem(new Object[]{markerName, markerType, species, markerId, ploidy, motif, forwardPrimer,
						reversePrimer, annealingTemp, principalInvestigator,
						contact, institute}, new Integer(i));
				i++;
			}
			
			
			
		}else{
			
			textArea = new TextArea();
			textArea.setValue(finalMsg);
			textArea.setReadOnly(true);
			textArea.setWidth("400px");
			textArea.setHeight("150px");
			
			
		}
		
		
		
		
		/*TextArea textArea = new TextArea();
		textArea.setValue(_strMessage);
		textArea.setReadOnly(true);
		textArea.setWidth("400px");
		textArea.setHeight("150px");
		*/
		
		_okButton = new Button("Ok");
		_okButton.setDescription("Ok");
		_okButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				_mainHomePage.getMainWindow().getWindow().removeWindow(getWindow());
			}
		});
		if (1 > _strMessage.size()) {
			verticalLayout.addComponent(textArea);
		}
		verticalLayout.addComponent(_okButton);
		verticalLayout.setComponentAlignment(_okButton, Alignment.BOTTOM_CENTER);
		
		return verticalLayout;
	}

	
	@Override
	public void detach() {
		super.detach();
	}
}
