package org.icrisat.gdms.ui;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import org.generationcp.middleware.support.servlet.MiddlewareServletContextListener;

@WebListener
public class GDMSServletContextListener extends MiddlewareServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		super.contextDestroyed(event);
		
		System.out.println("MiddlewareServletContextListener : Context Destroyed");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		super.contextInitialized(arg0);
				
		System.out.println("MiddlewareServletContextListener : Context Initialized");
	}
	

}
