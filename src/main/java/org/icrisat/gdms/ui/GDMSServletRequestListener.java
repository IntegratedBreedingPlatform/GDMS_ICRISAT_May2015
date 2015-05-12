package org.icrisat.gdms.ui;

import javax.servlet.ServletRequestEvent;
import javax.servlet.annotation.WebListener;

import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.support.servlet.MiddlewareServletRequestListener;
import org.icrisat.gdms.ui.common.GDMSModel;

@WebListener
public class GDMSServletRequestListener extends MiddlewareServletRequestListener {

	@Override
	public void requestDestroyed(ServletRequestEvent event) {
		super.requestDestroyed(event);
		
		//System.out.println("MiddlewareServletRequestListener : Request Destroyed");
		
		//GDMSModel.getGDMSModel().setManagerFactory(null);
		
	}

	@Override
	public void requestInitialized(ServletRequestEvent event) {
		super.requestInitialized(event);
		
		//System.out.println("MiddlewareServletRequestListener : Request Initialized");
		
		ManagerFactory managerFactoryForRequest = getManagerFactoryForRequest(event.getServletRequest());
		
	
		//GDMSModel.getGDMSModel().setServletRequest(event);
		
		if (null != managerFactoryForRequest) {
			//System.out.println("ManagerFactory object obtained.");
			GDMSModel.getGDMSModel().setManagerFactory(managerFactoryForRequest);
			//GDMSModel.getGDMSModel().setWorkbenchDataManager(super.getWorkbenchManagerForRequest(event.getServletRequest()));
		} else {
			//System.out.println("ManagerFactory object is null.");
			
		}
		
		WorkbenchDataManager dataManager = getWorkbenchManagerForRequest(event.getServletRequest());
        if (null != dataManager) {
            GDMSModel.getGDMSModel().setWorkbenchDataManager(dataManager);
        } else {
            System.out.println("WorkbenchDataManager object is null.");

        }
	}
	
	
}
