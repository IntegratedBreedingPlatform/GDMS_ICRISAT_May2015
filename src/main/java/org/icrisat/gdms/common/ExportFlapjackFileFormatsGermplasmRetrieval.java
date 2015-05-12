package org.icrisat.gdms.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateUtil;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.CharValues;
import org.generationcp.middleware.pojos.gdms.MapInfo;
import org.generationcp.middleware.pojos.gdms.MappingData;
import org.generationcp.middleware.pojos.gdms.MappingPopValues;
import org.generationcp.middleware.pojos.gdms.QtlDetailElement;
import org.generationcp.middleware.pojos.workbench.Project;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.ui.Window.Notification;
//import org.generationcp.middleware.dao.TraitDAO;
//import org.generationcp.middleware.pojos.Trait;


public class ExportFlapjackFileFormatsGermplasmRetrieval {
	private GDMSModel _gdmsModel;		
	 private static WorkbenchDataManager workbenchDataManager;
	 private static HibernateUtil hibernateUtil;
	 HashMap<Object, String> IBWFProjects= new HashMap<Object, String>();
	 
	 String bPath="";
    String opPath="";
    String strBMSFilePath="";
    String QTLExists="yes";
    ////System.out.println(",,,,,,,,,,,,,  :"+bPath.substring(0, bPath.indexOf("IBWorkflowSystem")-1));
    String pathWB="";
    
	    String dbNameL="";
	    String instDir="";
	    //int currWorkingProject=0;
	    String currWorkingProject="";
	    String strFolderName="";
	    String folderName="";
	    
	    public ExportFlapjackFileFormatsGermplasmRetrieval(){
	    	_gdmsModel = GDMSModel.getGDMSModel();
	    	try{
	    		instDir=_gdmsModel.getWorkbenchDataManager().getWorkbenchSetting().getInstallationDirectory().toString();
	    		Project results = _gdmsModel.getWorkbenchDataManager().getLastOpenedProject(_gdmsModel.getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId());
	    		
	    		//currWorkingProject=Integer.parseInt(results.getProjectId().toString());
	    		currWorkingProject=results.getProjectName().toString();
	    		
	    		
	    		folderName="FlapjackFiles";
	    		long time = new Date().getTime();
	    		strFolderName = String.valueOf(time);
	    		pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
	    		
	    		strBMSFilePath=pathWB+"/"+folderName+"/"+strFolderName;
	    		////System.out.println("..........currWorkingProject=:"+currWorkingProject);
	    	}catch (MiddlewareQueryException e) {
	    		//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
	    		return;
	    	}
	    } 
	    
	private File generatedTextFile;
	private File generatedDatFile;
	private File generatedMapFile;
	ManagerFactory factory=null;
	public File getGeneratedTextFile() {
		return generatedTextFile;
	}

	public File getGeneratedDatFile() {
		return generatedDatFile;
	}

	public File getGeneratedMapFile() {
		return generatedMapFile;
	}
	public String getStrBMSFilePath(){
		return strBMSFilePath;
	}
	
	
	public String getQTLExists(){
		return QTLExists;
	}

	public void generateFlapjackDataFilesByGIDs(
			GDMSMain theMainHomePage,						
			ArrayList listOfAllMapInfo,
			ArrayList<Integer> listOfGIDsToBeExported,
			ArrayList<String> listOfMarkerNames,
			TreeMap<String, Integer> hmOfMNamesandMID, ArrayList<QtlDetailElement> listOfAllQTLDetails,
			HashMap<Integer, String> hmOfQtlPosition, HashMap<String, Integer> hmOfQtlNameId,
			HashMap<Integer, String> hmOfQtlIdandName, String strSelectedExportType, boolean bQTLExists, HashMap dataMap, ArrayList gList, HashMap<Object, String> hashMapOfGIDs) {
		
			
		  if(!new File(pathWB+"/"+folderName).exists())
		   		new File(pathWB+"/"+folderName).mkdir();
		  if(!new File(pathWB+"/"+folderName+"/"+strFolderName).exists())
			  new File(pathWB+"/"+folderName+"/"+strFolderName).mkdir();
      
		
	      if(bQTLExists){			
				try {
					writeTextFile(theMainHomePage, listOfAllQTLDetails, hmOfQtlPosition,  hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType,  bQTLExists);
				} catch (GDMSException e) {
					theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.txt file.", Notification.TYPE_ERROR_MESSAGE);
					return;
				}
	      }else{
	    	  File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
	  		//System.out.println("$$$$$$$$   :"+baseDirectory+"                      "+ baseDirectory.getAbsoluteFile());
	  		File absoluteFile = baseDirectory.getAbsoluteFile();

	  		File[] listFiles = absoluteFile.listFiles();
	  		File fileExport = baseDirectory;
	  		for (File file : listFiles) {
	  			if(file.getAbsolutePath().endsWith("Flapjack")) {
	  				fileExport = file;
	  				break;
	  			}
	  		}
	  		String strFilePath = fileExport.getAbsolutePath();
	  		//System.out.println("SRI KALYANI.................strFilePath=:"+strFilePath);
	    	  
	    	  File fexists=new File(strFilePath+"/Flapjack.txt");
				if(fexists.exists()) { fexists.delete(); 
				//System.out.println("proj exists and deleted");
				}
				QTLExists="no";
	      }
		
		try {
			writeDatFile(theMainHomePage, dataMap, listOfGIDsToBeExported,
					listOfMarkerNames, hmOfMNamesandMID, gList,hashMapOfGIDs);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.dat file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			writeMapFile(theMainHomePage, listOfAllMapInfo);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.map file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
	}

	
	
	public void generateFlapjackDataFilesByGermplasmNames(
			GDMSMain theMainHomePage,			
			ArrayList listOfAllMappingData,
			ArrayList<String> listOfGNamesToBeExported,
			ArrayList<String> listOfMarkerNames,
			TreeMap<String, Integer> hmOfMIDandMNames,
			TreeMap<Object, String> hmOfGIdsAndNval,
			ArrayList<QtlDetailElement> listOfAllQTLDetails,			
			HashMap<Integer, String> hmOfQtlPosition, 
			HashMap<String, Integer> hmOfQtlNameId,
			HashMap<Integer, String> hmOfQtlIdandName,
			String strSelectedExportType, boolean bQTLExists, HashMap dataMap, ArrayList gList) {
		

		  if(!new File(pathWB+"/"+folderName).exists())
		   		new File(pathWB+"/"+folderName).mkdir();
        if(!new File(pathWB+"/"+folderName+"/"+strFolderName).exists())
	   		new File(pathWB+"/"+folderName+"/"+strFolderName).mkdir();
        
		
		if(bQTLExists){
		
			try {
				writeTextFile(theMainHomePage, listOfAllQTLDetails, hmOfQtlPosition, hmOfQtlNameId, hmOfQtlIdandName, strSelectedExportType,  bQTLExists);
			} catch (GDMSException e) {
				theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.txt file.", Notification.TYPE_ERROR_MESSAGE);
				return;
			}
		}else{
	    	  File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
	  		//System.out.println("$$$$$$$$   :"+baseDirectory+"                      "+ baseDirectory.getAbsoluteFile());
	  		File absoluteFile = baseDirectory.getAbsoluteFile();

	  		File[] listFiles = absoluteFile.listFiles();
	  		File fileExport = baseDirectory;
	  		for (File file : listFiles) {
	  			if(file.getAbsolutePath().endsWith("Flapjack")) {
	  				fileExport = file;
	  				break;
	  			}
	  		}
	  		String strFilePath = fileExport.getAbsolutePath();
	  		  
	    	  File fexists=new File(strFilePath+"/Flapjack.txt");
				if(fexists.exists()) {
					fexists.delete(); 				
				}
				QTLExists="no";
	      }
		try {
			writeDatFileGermplasmNames(theMainHomePage, dataMap, listOfGNamesToBeExported, hmOfGIdsAndNval,
					listOfMarkerNames, hmOfMIDandMNames, gList);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.dat file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
		try {
			writeMapFile(theMainHomePage, listOfAllMappingData);
		} catch (GDMSException e) {
			theMainHomePage.getMainWindow().getWindow().showNotification("Error generating Flapjack.map file.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		//System.out.println("files gernarated ExportClass");
	}
	
	private void writeDatFileGermplasmNames(
			GDMSMain theMainHomePage,
			HashMap dataMap,
			ArrayList<String> accList,
			TreeMap<Object, String> hmOfGIdsAndNval, ArrayList<String> markList,
			TreeMap<String, Integer> hmOfMNamesandMID, ArrayList gList) throws GDMSException {
		HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
		boolean condition=false;
		int noOfAccs=accList.size();
		int noOfMarkers=markList.size();			
		
		int accIndex=1,markerIndex=1;
		int i;String chVal="";
		//long time = new Date().getTime();
		//String strFlapjackDatFile = "Flapjack" + String.valueOf(time);
		String strFlapjackDatFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		//System.out.println("$$$$$$$$   :"+baseDirectory+"                      "+ baseDirectory.getAbsoluteFile());
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		generatedDatFile = new File(strFilePath + "\\" + strFlapjackDatFile + ".dat");
		
		String finalData="";	
		
		try {
			FileWriter flapjackTextWriter = new FileWriter(generatedDatFile);
			BufferedWriter fjackdat = new BufferedWriter(flapjackTextWriter);
			//fjackdat.write("\n");
			
			for(int m = 0; m< markList.size(); m++){
				fjackdat.write("\t"+markList.get(m));
			}
			
			int al=0;
			//System.out.println("hmOfGIdsAndNval:"+hmOfGIdsAndNval);
			//System.out.println(gList);
			for (int j=0;j<gList.size();j++){ 
				String arrList6[]=new String[3];
				//System.out.println(j+" : "+gList.get(j));
				String gName=hmOfGIdsAndNval.get(gList.get(j)).toString();	
				
				fjackdat.write("\n"+gName);		
			    for (int k=0;k<markList.size();k++){
			    	markerAlleles=(HashMap)dataMap.get(gList.get(j));
			    	//System.out.println("markerAlleles=:"+markerAlleles+"   "+hmOfNvalAndGIds.get(accList.get(j).toString()).toString()+"   "+ accList.get(j).toString());
			    	if(markerAlleles!=null){
			    		if(markerAlleles.containsKey(gList.get(j).toString()+"!~!"+hmOfMNamesandMID.get(markList.get(k).toString()))){
							String alleleValue=markerAlleles.get(gList.get(j).toString()+"!~!"+hmOfMNamesandMID.get(markList.get(k).toString())).toString();
							if(alleleValue.contains("/")){
								String[] strAllele=alleleValue.split("/");
								//System.out.println("%%%%%%%:"+alleleValue.length()+"   "+gName+"  "+markList.get(k)+"  "+alleleValue+"  alleles length:"+strAllele.length);
								//if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){										
								if(alleleValue.length()==3){
									if((alleleValue.matches("0/0"))||(alleleValue.equals("?"))){
								
										finalData="";
									}else{
										//String[] strAllele=alleleValue.split("/");
										////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
										if(strAllele[0].equalsIgnoreCase(strAllele[1]))
											finalData=strAllele[0];
										else
											finalData=strAllele[0]+"/"+strAllele[1];
									}
								}else {
									finalData=alleleValue;									
								}
							}else if(alleleValue.contains(":")){
								if(alleleValue.length()==3){
									if((alleleValue.matches("0:0"))||(alleleValue.equals("?"))){									
								
										finalData="";
									}else {
										String[] strAllele=alleleValue.split(":");
										////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
										if(strAllele[0].equalsIgnoreCase(strAllele[1]))
											finalData=strAllele[0];
										else
											finalData=strAllele[0]+"/"+strAllele[1];
									}
								}else{
									finalData=alleleValue;	
								}
							}else{
								if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){													
									finalData="";
								}else{
									finalData=alleleValue;
								}
							}
							fjackdat.write("\t"+finalData);
				    		
				    	}else{
				    		fjackdat.write("\t");	
				    	}
			    	}
			      }
		    	
		     }
					
			fjackdat.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
			
	       File generatedDatFileBMS = new File(strBMSFilePath + "\\FlapjackGenotype.dat");
			
			String fData="";	
			
			try {
				FileWriter flapjackTextWriter1 = new FileWriter(generatedDatFileBMS);
				BufferedWriter fjackdatBMS = new BufferedWriter(flapjackTextWriter1);
				//fjackdat.write("\n");
				
				for(int m = 0; m< markList.size(); m++){
					fjackdatBMS.write("\t"+markList.get(m));
				}
				
				
				//System.out.println("dataMap:"+dataMap);
				for (int j=0;j<gList.size();j++){ 
					String arrList6[]=new String[3];
					String[] gid=gList.get(j).toString().split("~~!!~~");
					String gName=hmOfGIdsAndNval.get(gList.get(j)).toString();	
					
					fjackdatBMS.write("\n"+gName);		
				    for (int k=0;k<markList.size();k++){
				    	markerAlleles=(HashMap)dataMap.get(gList.get(j));
				    	if(markerAlleles!=null){
					    	if(markerAlleles.containsKey(gList.get(j)+"!~!"+hmOfMNamesandMID.get(markList.get(k).toString()))){
								String alleleValue=markerAlleles.get(gList.get(j).toString()+"!~!"+hmOfMNamesandMID.get(markList.get(k).toString())).toString();
								if(alleleValue.contains("/")){
									//if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){
									if(alleleValue.length()==3){
										if((alleleValue.matches("0/0"))||(alleleValue.equals("?"))){									
											fData="";
										}else{
											String[] strAllele=alleleValue.split("/");
											////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
											if(strAllele[0].equalsIgnoreCase(strAllele[1]))
												fData=strAllele[0];
											else
												fData=strAllele[0]+"/"+strAllele[1];
										}
									}else{
										fData=alleleValue;	
									}
								}else if(alleleValue.contains(":")){
									//if((alleleValue.length()==3 && alleleValue.matches("0:0"))||(alleleValue.equals("?"))){
									if(alleleValue.length()==3){
										if((alleleValue.matches("0:0"))||(alleleValue.equals("?"))){	
									
											fData="";
										}else{
											String[] strAllele=alleleValue.split(":");
											////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
											if(strAllele[0].equalsIgnoreCase(strAllele[1]))
												fData=strAllele[0];
											else
												fData=strAllele[0]+"/"+strAllele[1];
										}
									}else{
										fData=alleleValue;	
									}
								}else{
									if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){													
										fData="";
									}else{
										fData=alleleValue;
									}
								}
								fjackdatBMS.write("\t"+fData);
					    		
					    	}else{
					    		fjackdatBMS.write("\t");	
					    	}
				    	}
				      }
			    	
			     }
						
				fjackdatBMS.close();
		
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
	}
	private void writeDatFile(
			GDMSMain theMainHomePage,
			HashMap dataMap, 
			ArrayList<Integer> accList,
			ArrayList<String> markList,
			TreeMap<String, Integer> hmOfMNamesandMID, ArrayList gList, HashMap<Object, String> hashMapOfGIDs) throws GDMSException {
		HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
		boolean condition=false;
		//long time = new Date().getTime();
		//String strFlapjackDatFile = "Flapjack" + String.valueOf(time);
		int accIndex=1,markerIndex=1;
		int i;String chVal="";
		//long time = new Date().getTime();
		//String strFlapjackDatFile = "Flapjack" + String.valueOf(time);
		String strFlapjackDatFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePathGids = fileExport.getAbsolutePath();
		generatedDatFile = new File(strFilePathGids + "\\" + strFlapjackDatFile + ".dat");
		int al=0;
		String finalData="";	
		//System.out.println("hashMapOfGIDs:"+hashMapOfGIDs);
		try {
			FileWriter flapjackTextWriterGIDs = new FileWriter(generatedDatFile);
			BufferedWriter fjackdatGids = new BufferedWriter(flapjackTextWriterGIDs);
			
			for(int m1 = 0; m1< markList.size(); m1++){
				////System.out.println("m1=:"+m1);
				fjackdatGids.write("\t"+markList.get(m1));
			}
			/*System.out.println("hmOfMNamesandMID:"+hmOfMNamesandMID);
			System.out.println(markList);*/
			for (int j=0;j<gList.size();j++){ 				
				String arrList6[]=new String[3];
				//String[] gid=gList.get(j).toString().split("~~!!~~");							
				fjackdatGids.write("\n"+hashMapOfGIDs.get(gList.get(j)));	
				
				
			    for (int k=0;k<markList.size();k++){
			    	//System.out.println("markerAlleles:"+markerAlleles);
			    	markerAlleles=(HashMap)dataMap.get(gList.get(j).toString());
			    	if(markerAlleles.containsKey(gList.get(j)+"!~!"+hmOfMNamesandMID.get(markList.get(k).toString()))){
						String alleleValue=markerAlleles.get(gList.get(j)+"!~!"+hmOfMNamesandMID.get(markList.get(k).toString())).toString();
						if(alleleValue.contains("/")){
							//if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){
							String[] strAllele=alleleValue.split("/");
							if(alleleValue.length()==3 ){
								if((alleleValue.matches("0/0"))||(alleleValue.equals("?"))){	
							
									finalData="";
								}else{
									//String[] strAllele=alleleValue.split("/");
									////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
									if(strAllele[0].equalsIgnoreCase(strAllele[1]))
										finalData=strAllele[0];
									else
										finalData=strAllele[0]+"/"+strAllele[1];
								}
							}else {									
								finalData=alleleValue;
							}
						}else if(alleleValue.contains(":")){
							//if((alleleValue.length()==3 && alleleValue.matches("0:0"))||(alleleValue.equals("?"))){
							if(alleleValue.length()==3){
								if((alleleValue.matches("0:0"))||(alleleValue.equals("?"))){							
									finalData="";
								}else{
									String[] strAllele=alleleValue.split(":");
									////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
									if(strAllele[0].equalsIgnoreCase(strAllele[1]))
										finalData=strAllele[0];
									else
										finalData=strAllele[0]+"/"+strAllele[1];
								}
							}else {									
								finalData=alleleValue;
							}
						}else{
							if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){										
								finalData="";
							}else{
								finalData=alleleValue;
							}
						}
						fjackdatGids.write("\t"+finalData);
			    		
			    	}else{
			    		fjackdatGids.write("\t");	
			    	}	
		    	}
		    }
			   
						
			fjackdatGids.close();			
			
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
		//System.out.println("strBMSFilePath:"+strBMSFilePath);
		File generatedDatFileBMS = new File(strBMSFilePath + "\\FlapjackGenotype.dat");
		
		String fData="";	
		
		try {
			FileWriter flapjackTextWriterGIDsBMS = new FileWriter(generatedDatFileBMS);
			BufferedWriter fjackdatGidsBMS = new BufferedWriter(flapjackTextWriterGIDsBMS);
			
			for(int m1 = 0; m1< markList.size(); m1++){				
				fjackdatGidsBMS.write("\t"+markList.get(m1));
			}	
			
			for (int j=0;j<gList.size();j++){ 				
				String arrList6[]=new String[3];
				String[] gid=gList.get(j).toString().split("~~!!~~");							
				fjackdatGidsBMS.write("\n"+gid[0]);	
				
			    for (int k=0;k<markList.size();k++){
			    	markerAlleles=(HashMap)dataMap.get(gList.get(j));
			    	
			    	if(markerAlleles.containsKey(gList.get(j)+"!~!"+hmOfMNamesandMID.get(markList.get(k).toString()))){
						String alleleValue=markerAlleles.get(gList.get(j)+"!~!"+hmOfMNamesandMID.get(markList.get(k).toString())).toString();
			    	
			    		////System.out.println("k=:"+k +"   "+alleleValue);
						if(alleleValue.contains("/")){
							if(alleleValue.length()==3){
								if((alleleValue.matches("0/0"))||(alleleValue.equals("?"))){	
									fData="";
								}else{
									String[] strAllele=alleleValue.split("/");
									////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
									if(strAllele[0].equalsIgnoreCase(strAllele[1]))
										fData=strAllele[0];
									else
										fData=strAllele[0]+"/"+strAllele[1];
								}
							}else{
								fData=alleleValue;
							}
						}else if(alleleValue.contains(":")){
							if(alleleValue.length()==3){
								if((alleleValue.matches("0:0"))||(alleleValue.equals("?"))){									
									fData="";
								}else{
									String[] strAllele=alleleValue.split(":");
									////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
									if(strAllele[0].equalsIgnoreCase(strAllele[1]))
										fData=strAllele[0];
									else
										fData=strAllele[0]+"/"+strAllele[1];
								}
							}else{
								fData=alleleValue;
							}
						}else{
							if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){										
								fData="";
							}else{
								fData=alleleValue;
							}
						}
						fjackdatGidsBMS.write("\t"+fData);
			    		
			    	}else{
			    		fjackdatGidsBMS.write("\t");	
			    	}	
		    	}
		    }						
			fjackdatGidsBMS.close();
			
		}catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
	}

	private void writeTextFile(
			GDMSMain theMainHomePage,
			ArrayList<QtlDetailElement> listOfAllQTLDetails, HashMap<Integer, String> hmOfQtlPosition, HashMap<String, Integer> hmOfQtlNameId,
			HashMap<Integer, String> hmOfQtlIdandName, String strSelectedExportType, boolean bQTLExists) throws GDMSException {
		
		
		
		String strFlapjackTextFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		//System.out.println("strFilePath=:"+strFilePath);
		generatedTextFile = new File(strFilePath + "\\" + strFlapjackTextFile + ".txt");

		/**	writing tab delimited qtl file for FlapJack  
		 * 	consisting of marker chromosome & position
		 * 
		 * **/
		try {
			
			FileWriter flapjackTextWriter = new FileWriter(generatedTextFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackTextWriter);
			
			//fjackQTL.write("QTL\tChromosome\tPosition\tMinimum\tMaximum\tTrait\tExperiment\tTrait Group\tLOD\tR2\tFlanking markers in original publication");
			flapjackBufferedWriter.write("QTL\tChromosome\tPosition\tMinimum\tMaximum\tTrait\tExperiment\tTrait Group\tLOD\tR2\tFlanking markers in original publication\teffect");
			flapjackBufferedWriter.write("\n");
			for (int i = 0 ; i < listOfAllQTLDetails.size(); i++){
				//System.out.println(listOfAllQTLDetails.get(i));
				QtlDetailElement qtlDetails = listOfAllQTLDetails.get(i);
				
				/*QtlDetailsPK id = qtlDetails.getQtlName().get.getId();
				Integer qtlId = id.getQtlId();*/
				//String strQtlName = hmOfQtlIdandName.get(qtlId);
				String strQtlName =qtlDetails.getQtlName();
				int qtlId=hmOfQtlNameId.get(strQtlName);
				//qtlDetails.get
				//Float clen = qtlDetails.getClen();
				Float fEffect = qtlDetails.getEffect();
				//int fEffect = qtlDetails.getEffect();
				Float fMaxPosition = qtlDetails.getMaxPosition();
				Float fMinPosition = qtlDetails.getMinPosition();
				//Float fPosition = qtlDetails.getPosition();
				String fPosition = hmOfQtlPosition.get(qtlId);
				Float frSquare = qtlDetails.getrSquare();
				Float fScoreValue = qtlDetails.getScoreValue();
				String strExperiment = qtlDetails.getExperiment();
				//String strHvAllele = qtlDetails..getHvAllele();
				//String strHvParent = qtlDetails.getHvParent();
				//String strInteractions = qtlDetails.getInteractions();
				String strLeftFlankingMarker = qtlDetails.getLeftFlankingMarker();
				String strLinkageGroup = qtlDetails.getChromosome();
				//String strLvAllele = qtlDetails.getLvAllele();
				//String strLvParent = qtlDetails.getLvParent();
				String strRightFM = qtlDetails.getRightFlankingMarker();
				//String strSeAdditive = qtlDetails.getSeAdditive();
				
				//String strTrait = qtlDetails.getTrait();
				String strTrait = qtlDetails.gettRName();
							
				
				flapjackBufferedWriter.write(strQtlName + "\t" + strLinkageGroup + "\t" + fPosition + "\t" + fMinPosition + "\t" + fMaxPosition + "\t" +
						strTrait + "\t" + strExperiment + "\t \t" + fScoreValue + "\t" + frSquare+
	                     "\t" + strLeftFlankingMarker+"/"+strRightFM + "\t" + fEffect);
				
				flapjackBufferedWriter.write("\n");
				
			}
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
		
		File generatedTextFileBMS = new File(strBMSFilePath + "\\FlapjackQTL.txt");

		/**	writing tab delimited qtl file for FlapJack  
		 * 	consisting of marker chromosome & position
		 * 
		 * **/
		try {
			
			FileWriter flapjackTextWriterBMS = new FileWriter(generatedTextFileBMS);
			BufferedWriter flapjackBufferedWriterBMS = new BufferedWriter(flapjackTextWriterBMS);
			
			//fjackQTL.write("QTL\tChromosome\tPosition\tMinimum\tMaximum\tTrait\tExperiment\tTrait Group\tLOD\tR2\tFlanking markers in original publication");
			flapjackBufferedWriterBMS.write("QTL\tChromosome\tPosition\tMinimum\tMaximum\tTrait\tExperiment\tTrait Group\tLOD\tR2\tFlanking markers in original publication\teffect");
			flapjackBufferedWriterBMS.write("\n");
			for (int i = 0 ; i < listOfAllQTLDetails.size(); i++){
				//System.out.println(listOfAllQTLDetails.get(i));
				QtlDetailElement qtlDetails = listOfAllQTLDetails.get(i);
				
				/*QtlDetailsPK id = qtlDetails.getQtlName().get.getId();
				Integer qtlId = id.getQtlId();*/
				//String strQtlName = hmOfQtlIdandName.get(qtlId);
				String strQtlName =qtlDetails.getQtlName();
				int qtlId=hmOfQtlNameId.get(strQtlName);
				//qtlDetails.get
				//Float clen = qtlDetails.getClen();
				Float fEffect = qtlDetails.getEffect();
				//int fEffect = qtlDetails.getEffect();
				Float fMaxPosition = qtlDetails.getMaxPosition();
				Float fMinPosition = qtlDetails.getMinPosition();
				//Float fPosition = qtlDetails.getPosition();
				String fPosition = hmOfQtlPosition.get(qtlId);
				Float frSquare = qtlDetails.getrSquare();
				Float fScoreValue = qtlDetails.getScoreValue();
				String strExperiment = qtlDetails.getExperiment();
				//String strHvAllele = qtlDetails..getHvAllele();
				//String strHvParent = qtlDetails.getHvParent();
				//String strInteractions = qtlDetails.getInteractions();
				String strLeftFlankingMarker = qtlDetails.getLeftFlankingMarker();
				String strLinkageGroup = qtlDetails.getChromosome();
				//String strLvAllele = qtlDetails.getLvAllele();
				//String strLvParent = qtlDetails.getLvParent();
				String strRightFM = qtlDetails.getRightFlankingMarker();
				//String strSeAdditive = qtlDetails.getSeAdditive();
				
				//String strTrait = qtlDetails.getTrait();
				String strTrait = qtlDetails.gettRName();
							
				
				flapjackBufferedWriterBMS.write(strQtlName + "\t" + strLinkageGroup + "\t" + fPosition + "\t" + fMinPosition + "\t" + fMaxPosition + "\t" +
						strTrait + "\t" + strExperiment + "\t \t" + fScoreValue + "\t" + frSquare+
	                     "\t" + strLeftFlankingMarker+"/"+strRightFM + "\t" + fEffect);
				
				flapjackBufferedWriterBMS.write("\n");
				
			}
			flapjackBufferedWriterBMS.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
		
		
		
		
		
	}
	
	

	private void writeMapFile(
			GDMSMain theMainHomePage,
			ArrayList listOfAllMapInfo) throws GDMSException {
	
		//long time = new Date().getTime();
		//String strFlapjackMapFile = "Flapjack" + String.valueOf(time);
		String strFlapjackMapFile = "Flapjack";
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("Flapjack")) {
				fileExport = file;
				break;
			}
		}
		String strFilePath = fileExport.getAbsolutePath();
		generatedMapFile = new File(strFilePath + "\\" + strFlapjackMapFile + ".map");
		
		/**	writing tab delimited .map file for FlapJack  
		 * 	consisting of marker chromosome & position
		 * 
		 * **/
		//System.out.println("............:"+listOfAllMapInfo);
		try {
			FileWriter flapjackMapFileWriter = new FileWriter(generatedMapFile);
			BufferedWriter flapjackBufferedWriter = new BufferedWriter(flapjackMapFileWriter);
			//flapjackBufferedWriter.write("\n");
			//flapjackBufferedWriter.write("Marker-Name\tLinkage-Group\tStarting-Position\n");
			
			for (int m=0; m<listOfAllMapInfo.size();m++){
				String[] MapInfo=listOfAllMapInfo.get(m).toString().split("!~!");			
				
				flapjackBufferedWriter.write(MapInfo[0]);
				flapjackBufferedWriter.write("\t");
				flapjackBufferedWriter.write(MapInfo[1]);
				flapjackBufferedWriter.write("\t");
				flapjackBufferedWriter.write(MapInfo[2]);
				flapjackBufferedWriter.write("\n");
				
			}
			
			flapjackBufferedWriter.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
		
		File generatedMapFileBMS = new File(strBMSFilePath + "\\FlapjackMap.map");
		
		/**	writing tab delimited .map file for FlapJack  
		 * 	consisting of marker chromosome & position
		 * 
		 * **/
		//System.out.println("............:"+listOfAllMapInfo);
		try {
			FileWriter flapjackMapFileWriterBMS = new FileWriter(generatedMapFileBMS);
			BufferedWriter flapjackBufferedWriterBMS = new BufferedWriter(flapjackMapFileWriterBMS);
			//flapjackBufferedWriter.write("\n");
			//flapjackBufferedWriter.write("Marker-Name\tLinkage-Group\tStarting-Position\n");
			
			for (int m=0; m<listOfAllMapInfo.size();m++){
				String[] MapInfo=listOfAllMapInfo.get(m).toString().split("!~!");			
				
				flapjackBufferedWriterBMS.write(MapInfo[0]);
				flapjackBufferedWriterBMS.write("\t");
				flapjackBufferedWriterBMS.write(MapInfo[1]);
				flapjackBufferedWriterBMS.write("\t");
				flapjackBufferedWriterBMS.write(MapInfo[2]);
				flapjackBufferedWriterBMS.write("\n");
				
			}
			
			flapjackBufferedWriterBMS.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
		
	}
	
	

}
