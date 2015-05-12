/**
 * 
 */
package org.icrisat.gdms.common;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateSessionFactory {
	 private static Log log = LogFactory.getLog(HibernateSessionFactory.class);
	    
	    private static final SessionFactory sessionFactoryC;
	   
	    
	    static {
	        try {
	            // Create the SessionFactory
	            //sessionFactoryC = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
	            sessionFactoryC = new Configuration().buildSessionFactory();
	            
	        } catch (Throwable ex) {
	        	 ex.printStackTrace();
	            // Make sure you log the exception, as it might be swallowed
	            log.error("Initial SessionFactory creation failed.", ex);
	            throw new ExceptionInInitializerError(ex);
	           
	        }
	    }
	    
	    public static final ThreadLocal session = new ThreadLocal();
	    
	    public static Session currentSession() throws HibernateException {
	        Session s = (Session) session.get();
	        // Open a new Session, if this Thread has none yet
	       // System.out.println("Crop in create session    class ="+crop);
	        if (s == null) {
	        	s = sessionFactoryC.openSession();	        	
	            session.set(s);
	        }
	        return s;
	    }
	    
	    public static void closeSession() throws HibernateException {
	        Session s = (Session) session.get();
	        session.set(null);
	        if (s != null)
	            s.close();
	    }    
}
