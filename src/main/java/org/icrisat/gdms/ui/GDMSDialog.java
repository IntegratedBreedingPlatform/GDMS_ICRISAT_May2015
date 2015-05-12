package org.icrisat.gdms.ui;

import java.util.List;

import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.gdms.MarkerInfo;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class GDMSDialog extends CustomComponent{
	private static final long serialVersionUID = 1L;
	private GDMSMain _mainHomePage;
	private List<Name> _strMessage;
	private String finalMsg="";
	private Button _okButton;
	private Table _tableWithMarkersOfQTL;
	private TextArea textArea;
	public GDMSDialog(GDMSMain theMainHomePage, List theMessage, String msg) {
		_mainHomePage = theMainHomePage;
		_strMessage = theMessage;
		finalMsg=msg;
		System.out.println("%$%$%$%$%$%%%%%%%%%%%%%   :"+_strMessage.size());
		setCompositionRoot(buildMessageWindow());
	}


	public VerticalLayout buildMessageWindow() {
		String germplasmName ="";
		Integer GID=0;
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setCaption("Upload Message");
		String[] strArrayOfColNames = {"GID", "GERMPLASM-NAME"};
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
			for (Name strMarkers : _strMessage) {
				GID=strMarkers.getGermplasmId();
				germplasmName=strMarkers.getNval();
				
				
				
				_tableWithMarkersOfQTL.addItem(new Object[]{GID, germplasmName}, new Integer(i));
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
