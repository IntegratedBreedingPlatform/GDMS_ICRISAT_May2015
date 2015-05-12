package org.icrisat.gdms.ui.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateUtil;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.icrisat.gdms.ui.FileUploadListener;
import org.icrisat.gdms.ui.GDMSMain;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Window.Notification;


public class GDMSFileChooser extends CustomComponent {

	private static final long serialVersionUID = 1L;
	boolean allowUpload = false;
	private TextField txtFieldLocation;
	private GDMSMain _mainHomePage;
	protected File file;
	private FileUploadListener _fileUploadListener;
	protected File dupFile;
	
	private GDMSModel _gdmsModel;		
	private static WorkbenchDataManager workbenchDataManager;
	private static HibernateUtil hibernateUtil;
	HashMap<Object, String> IBWFProjects= new HashMap<Object, String>();
		

	 String pathWB="";
	 String instDir="";
	 //int currWorkingProject=0;	 
	 String currWorkingProject="";
	    

	public GDMSFileChooser(GDMSMain theMainHomePage, boolean bHaveSubmit){

		_mainHomePage = theMainHomePage;

		GridLayout gridLayout = new GridLayout(3, 1);
		gridLayout.setSpacing(true);

		txtFieldLocation = new TextField();
		txtFieldLocation.setWidth("350px");
		txtFieldLocation.setInputPrompt("Please provide the template location.");
		gridLayout.addComponent(txtFieldLocation);
		gridLayout.setComponentAlignment(txtFieldLocation, Alignment.BOTTOM_LEFT);

		Button submitButton = new Button("Submit");
		submitButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;
			public void buttonClick(ClickEvent event) {
				allowUpload = true;
				_mainHomePage.getMainWindow().getWindow().showNotification("Marker details saved to the database.");
			}
		});

		_gdmsModel = GDMSModel.getGDMSModel();
    	try{
    		instDir=_gdmsModel.getWorkbenchDataManager().getWorkbenchSetting().getInstallationDirectory().toString();
    		Project results = _gdmsModel.getWorkbenchDataManager().getLastOpenedProject(_gdmsModel.getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId());
    		//currWorkingProject=Integer.parseInt(results.getProjectId().toString());	    		
    		currWorkingProject=results.getProjectName();
    		pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/input";
    		//System.out.println("pathWB:"+pathWB);
    	}catch (MiddlewareQueryException e) {
    		//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
    		return;
    	}
		
		Upload uploadComponent = new Upload("", new Upload.Receiver() {
			private static final long serialVersionUID = 1L;
			public OutputStream receiveUpload(String filename, String mimeType) {
				FileOutputStream fos = null; // Output stream to write to
				try {
					//System.out.println(pathWB+"filename:"+filename);
					file = new File(pathWB+"\\"+filename);
					
					FileResource fileResource = new FileResource(file, _mainHomePage.getMainWindow().getApplication());
					String absolutePath = fileResource.getSourceFile().getAbsolutePath();
					//System.out.println("absolutePath:"+pathWB);
					dupFile = new File(absolutePath);
					dupFile.createNewFile();
					
					
					
					fos = new FileOutputStream(file);
					txtFieldLocation.setValue(dupFile.getName());

					if (null != _fileUploadListener){
						//System.out.println("Dup file's location: " + dupFile.getAbsolutePath());
						//_fileUploadListener.updateLocation(dupFile.getAbsolutePath());
					}

				} catch (final java.io.FileNotFoundException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Could not open file<br/>", e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
					return null;
				} catch (IOException e) {
					_mainHomePage.getMainWindow().getWindow().showNotification("Could not open file<br/>", e.getMessage(),  Notification.TYPE_ERROR_MESSAGE);
					return null;
				}
				return fos; // Return the output stream to write to
			}
		});
		uploadComponent.setImmediate(true);
		uploadComponent.setButtonCaption("Browse");
		uploadComponent.addListener(new Upload.FinishedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void uploadFinished(FinishedEvent event) {
				
				if (null == dupFile){
					return;
				}
				
				if (null != _fileUploadListener){
					//System.out.println("Dup file's location: " + dupFile.getAbsolutePath());
					_fileUploadListener.updateLocation(dupFile.getAbsolutePath());
				}
				
			}
		});
		
		gridLayout.addComponent(uploadComponent);
		if(bHaveSubmit){
			gridLayout.addComponent(submitButton);
			gridLayout.setComponentAlignment(submitButton, Alignment.BOTTOM_RIGHT);
		}

		setCompositionRoot(gridLayout);
	}

	public void registerListener(FileUploadListener fileUploadListener) {
		_fileUploadListener = fileUploadListener;
	}

}
