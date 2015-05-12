package org.icrisat.gdms.common;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableHyperlink;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionPerThreadProvider;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.hibernate.HibernateUtil;
import org.generationcp.middleware.manager.WorkbenchDataManagerImpl;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.gdms.AllelicValueElement;
import org.generationcp.middleware.pojos.gdms.AllelicValueWithMarkerIdElement;
import org.generationcp.middleware.pojos.gdms.GermplasmMarkerElement;
import org.generationcp.middleware.pojos.gdms.MarkerIdMarkerNameElement;
import org.generationcp.middleware.pojos.workbench.Project;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.common.GDMSModel;

import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Table;


public class ExportFileFormats {

	//	ArrayList<AlleleValues> listOfAlleleValues = new ArrayList<AlleleValues>();
	//	ArrayList<MarkerNameElement> listOfMarkers = new ArrayList<MarkerNameElement>();
	private GDMSModel _gdmsModel;		
	 private static WorkbenchDataManager workbenchDataManager;
	 private static HibernateUtil hibernateUtil;
	 HashMap<Object, String> IBWFProjects= new HashMap<Object, String>();
	 
	 String bPath="";
     String opPath="";
    
     ////System.out.println(",,,,,,,,,,,,,  :"+bPath.substring(0, bPath.indexOf("IBWorkflowSystem")-1));
     String pathWB="";
     
	    String dbNameL="";
	    String instDir="";
	    //int currWorkingProject=0;
	    String currWorkingProject="";
	public ExportFileFormats(){
		_gdmsModel = GDMSModel.getGDMSModel();
		
		try{
			/*hibernateUtil = new HibernateUtil(GDMSModel.getGDMSModel().getWorkbenchParams());
			HibernateSessionProvider sessionProvider = new HibernateSessionPerThreadProvider(hibernateUtil.getSessionFactory());
			workbenchDataManager = new WorkbenchDataManagerImpl(sessionProvider);*/
			instDir=_gdmsModel.getWorkbenchDataManager().getWorkbenchSetting().getInstallationDirectory().toString();
			Project results = _gdmsModel.getWorkbenchDataManager().getLastOpenedProject(_gdmsModel.getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId());
			
			//currWorkingProject=Integer.parseInt(results.getProjectId().toString());
			currWorkingProject=results.getProjectName().toString();
			
			////System.out.println("..........currWorkingProject=:"+currWorkingProject);
		}catch (MiddlewareQueryException e) {
			//_mainHomePage.getMainWindow().getWindow().showNotification("Error retrieving Dataset details.", Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		
	}
	
	public void CMTVTxt(ArrayList<String[]> sortMapListToBeDisplayed, String theFilePath, GDMSMain _mainHomePage, boolean shouldOpenByDefault) throws GDMSException {

		FileWriter fileWriter;
		File file;

		try {

			file = new File(theFilePath);
			fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write("\t" + "LINKAGE-GROUP" + "\t" +
					"MARKER-NAME" + "\t" + "COUNT" + "\t" + "DISTANCE" + "STARTING-POSITION" + "\n");

			for (int i = 0; i < sortMapListToBeDisplayed.size(); i++){


				String[] strArray = sortMapListToBeDisplayed.get(i);

				String strLG = strArray[0];
				String strMarkerName = strArray[1];
				String strCount = strArray[2];
				String strDistance = strArray[3];
				String strStartingPoint = strArray[4];


				bufferedWriter.write("\t" + strLG + "\t" + strMarkerName + "\t" + 
						strCount + "\t" + strDistance + "\t" + strStartingPoint + "\n");

			}

			bufferedWriter.close();

		} catch (IOException e1) {
			throw new GDMSException(e1.getMessage());
		}

		FileResource fileResource = new FileResource(file, _mainHomePage);
		if(shouldOpenByDefault) {
			_mainHomePage.getMainWindow().getWindow().open(fileResource, "_blank");
		}
	}

	public void CMTVTxt(ArrayList<String[]> sortMapListToBeDisplayed, String theFilePath, GDMSMain _mainHomePage) throws GDMSException {
		CMTVTxt(sortMapListToBeDisplayed, theFilePath, _mainHomePage, true);
	}

	/**
	 * 
	 * this method is used in Germplasm Retrieval and GID Retrieval
	 * 
	 * @param a --- list of AllelicValueElements
	 * @param accList --- list of GIDs
	 * @param markList --- list of Markers selected from the UI
	 * @param gMap --- Hashmap of GIDs and Germplasm Names selected
	 * @throws GDMSException 
	 */
	public File Matrix(
			GDMSMain theMainHomePage,
			ArrayList listOfGIDsSelected,
			ArrayList<String> listOfMarkersSelected,
			HashMap<Object, String> hashMapOfGIDsAndGNamesSelected,
			HashMap listAlleleValueElementsForGIDsSelected, HashMap hmMarkerNamesIDS, HashMap<Object, String> hashMapOfGIDs) throws GDMSException {
		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+("//")+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();	
		
        pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
       
        if(!new File(pathWB+"/"+folderName).exists())
	   		new File(pathWB+"/"+folderName).mkdir();
        
		File generatedFile = new File(strFilePath + "/" + strFileName + ".txt");
		int noOfAccs=listOfGIDsSelected.size();
		int noOfMarkers=listOfMarkersSelected.size();			
		
		int accIndex=1,markerIndex=1;
		int i;String chVal="";
		HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
		try {
			FileWriter datastream = new FileWriter(generatedFile);
			BufferedWriter SNPMatrix = new BufferedWriter(datastream);
			SNPMatrix.write("\t");
			for(int m1 = 0; m1< listOfMarkersSelected.size(); m1++){
				SNPMatrix.write("\t"+listOfMarkersSelected.get(m1));
			}
			
			int gid=0;
			String gname="";		
			/*System.out.println("listOfGIDsSelected:"+listOfGIDsSelected);
			System.out.println("hashMapOfGIDsAndGNamesSelected:"+hashMapOfGIDsAndGNamesSelected);
			System.out.println("hashMapOfGIDs:"+hashMapOfGIDs);*/
			
			for (int j=0;j<listOfGIDsSelected.size();j++){ 
				String strGid=listOfGIDsSelected.get(j).toString();
				String[] strgids=strGid.split("~~!!~~");
				SNPMatrix.write("\n"+hashMapOfGIDs.get(listOfGIDsSelected.get(j))+"\t"+hashMapOfGIDsAndGNamesSelected.get(listOfGIDsSelected.get(j)));	
				//System.out.println("listOfMarkersSelected:"+listOfMarkersSelected);
			    for (int k=0;k<listOfMarkersSelected.size();k++){
			    	
			    	//System.out.println("**************************  :"+dataMap.get(Integer.parseInt(accList.get(j).toString())));
			    	markerAlleles=(HashMap)listAlleleValueElementsForGIDsSelected.get(listOfGIDsSelected.get(j));
			    	//System.out.println("markerAlleles:"+markerAlleles);
			    	String mKey= listOfGIDsSelected.get(j)+"!~!"+hmMarkerNamesIDS.get(listOfMarkersSelected.get(k).toString());
			    	if(markerAlleles.containsKey(mKey)){
				    		SNPMatrix.write("\t"+markerAlleles.get(mKey));
				    		
				    	}else{
				    		SNPMatrix.write("\t");	
				    	}	
						
					//}
					
			    }		    	
			}					
			SNPMatrix.close();	
			

			
			File generatedFileWF = new File(pathWB+"/"+folderName + "/" + strFileName + ".txt");
			
			HashMap<String,Object> markerAllelesWF= new HashMap<String,Object>();
			//try {
				FileWriter datastreamWF = new FileWriter(generatedFileWF);
				BufferedWriter SNPMatrixWF = new BufferedWriter(datastreamWF);
				SNPMatrixWF.write("\t");
				for(int m1 = 0; m1< listOfMarkersSelected.size(); m1++){
					SNPMatrixWF.write("\t"+listOfMarkersSelected.get(m1));
				}
				
				for (int j=0;j<listOfGIDsSelected.size();j++){ 
					String strGid=listOfGIDsSelected.get(j).toString();
					String[] strgids=strGid.split("~~!!~~");
					SNPMatrixWF.write("\n"+hashMapOfGIDs.get(listOfGIDsSelected.get(j))+"\t"+hashMapOfGIDsAndGNamesSelected.get(listOfGIDsSelected.get(j)));		
					//System.out.println("listOfMarkersSelected:"+listOfMarkersSelected);
				    for (int k=0;k<listOfMarkersSelected.size();k++){
				    	//System.out.println("**************************  :"+dataMap.get(Integer.parseInt(accList.get(j).toString())));
				    	markerAlleles=(HashMap)listAlleleValueElementsForGIDsSelected.get(listOfGIDsSelected.get(j));
				    	//System.out.println("markerAlleles:"+markerAlleles);
				    	String mKey= listOfGIDsSelected.get(j)+"!~!"+hmMarkerNamesIDS.get(listOfMarkersSelected.get(k).toString());
				    	if(markerAlleles.containsKey(mKey)){
				    		SNPMatrixWF.write("\t"+markerAlleles.get(mKey));
					    		
					    	}else{
					    		SNPMatrixWF.write("\t");	
					    	}	
							
						//}
						
				    }		    	
				}					
				SNPMatrixWF.close();	
				

		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} 
		return generatedFile;
	}
	
	
	public List<File> MatrixTxt(
			GDMSMain theMainHomePage,
			ArrayList<Integer> listOfGIDsSelected,
			ArrayList<String> listOfMarkersSelected,
			HashMap<Integer, String> hashMapOfGIDsAndGNamesSelected,
			ArrayList<AllelicValueElement> listAlleleValueElementsForGIDsSelected) throws GDMSException {

		PdfFileBuilder pdfFileBuilder = new PdfFileBuilder();
		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+("//")+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "/" + strFileName + ".txt");

		try {
			pdfFileBuilder.initTempFile();
			pdfFileBuilder.setVisibleColumnsLength(listOfMarkersSelected.size());
			pdfFileBuilder.resetContent();
		} catch (IOException e1) {
		}

		try {
			
			FileWriter ssrDatastream = new FileWriter(generatedFile);
			BufferedWriter bw = new BufferedWriter(ssrDatastream);

			//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
			bw.write("\t");
			bw.write("\t");
			pdfFileBuilder.buildCell("");
			pdfFileBuilder.buildCell("");
			//Writing the Markers from Column-2 onwards in the first row
			for (int j = 0; j < listOfMarkersSelected.size(); j++){
				String strMarker = listOfMarkersSelected.get(j);
				bw.write("\t");
				bw.write(strMarker);
				pdfFileBuilder.buildCell(strMarker);
			}
			
			for (int i = 0; i < listOfGIDsSelected.size(); i++){
				bw.write("\n");
			Integer iGID = listOfGIDsSelected.get(i);
			bw.write(String.valueOf(iGID));

			String strGName = hashMapOfGIDsAndGNamesSelected.get(iGID);
			bw.write("\t");
			bw.write(strGName);
			pdfFileBuilder.buildCell(strGName);
			//Next writing the AlleleValues for the Markers
			for (int k = 0; k < listAlleleValueElementsForGIDsSelected.size(); k++){

				AllelicValueElement allelicValueElement = listAlleleValueElementsForGIDsSelected.get(k);

				Integer gid = allelicValueElement.getGid();
				String markerName = allelicValueElement.getMarkerName();
				String strData = allelicValueElement.getData();

				if (listOfMarkersSelected.contains(markerName)){

					if (listOfGIDsSelected.contains(gid)){
						bw.write("\t");
						bw.write(strData);
						pdfFileBuilder.buildCell(strData);
					}
				}
			}
		}


			bw.close();
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} 
		List<File> listOfFiles = new ArrayList<File>();
		listOfFiles.add(generatedFile);
		if(listOfMarkersSelected.size() < 20) {
			pdfFileBuilder.writeToFile();
			File file = pdfFileBuilder.file;
			listOfFiles.add(file);
		}
		return listOfFiles;
	}
	
	

	public File MatrixTextFileDataSSRDataset(
			GDMSMain theMainHomePage,
			//ArrayList<AllelicValueWithMarkerIdElement> listOfAllelicValue,
			//ArrayList<Integer> listOfNIDs, 
			ArrayList<MarkerIdMarkerNameElement> listOfAllMarkers,
			HashMap<Object, String> hmOfNIDAndNVal, HashMap<Integer, String> hmOfMIdAndMarkerName,  ArrayList gList, HashMap<Object, String> hmOfGIDs, HashMap<String, Object> hmOfAlleles) throws GDMSException {
		//FileBuilder pdfFileBuilder = new PdfFileBuilder();
		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();
		 String folderName="AnalysisFiles";
		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
        pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";      
        if(!new File(pathWB+"/"+folderName).exists())
	   		new File(pathWB+"/"+folderName).mkdir();
        
        HashMap<String,Object> markerAlleles= new HashMap<String,Object>();		
		String strFilePath = fileExport.getAbsolutePath()+("//")+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
				
		File generatedFile = new File(strFilePath + "/" + strFileName + ".txt");
		int iNumOfCols = listOfAllMarkers.size() + 2;
		//int iNumOfRows = listOfNIDs.size() + 1;
		try {
			FileWriter ssrDatastream = new FileWriter(generatedFile);
			BufferedWriter ssrMatrix = new BufferedWriter(ssrDatastream);
			ssrMatrix.write("\t");
			List allMarkers=new ArrayList();			
			for(int m1 = 0; m1< listOfAllMarkers.size(); m1++){
				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(m1);
				String strMarker = marker.getMarkerName();
				
				ssrMatrix.write("\t"+strMarker);
				allMarkers.add(marker.getMarkerId());
			}		
			
			String finalData="";	
			/*System.out.println("gList:"+gList);
			System.out.println("hmOfNIDAndNVal:"+hmOfNIDAndNVal);*/
			for (int j=0;j<gList.size();j++){ 
				String arrList6[]=new String[3];
				String[] gid=gList.get(j).toString().split("~~!!~~");
				//String gName=hmOfNIDAndNVal.get(Integer.parseInt(gid[0].toString())).toString();				
				String gName=hmOfNIDAndNVal.get(gList.get(j)).toString();
				ssrMatrix.write("\n"+hmOfGIDs.get(gList.get(j))+"\t"+gName);	
			    for (int k=0;k<allMarkers.size();k++){
			    	String strKey=gList.get(j)+"!~!"+allMarkers.get(k);
			    	if(hmOfAlleles.containsKey(strKey)){
						
						String alleleValue=hmOfAlleles.get(strKey).toString();
						//System.out.println(gList.get(j)+"!~!"+hmOfMIdAndMarkerName.get(Integer.parseInt(allMarkers.get(k).toString())) +"=:"+alleleValue);
						if(alleleValue.contains("/")){
							if(alleleValue.length()==3){
								if((alleleValue.matches("0/0"))||(alleleValue.equals("?"))){										
									finalData="";
								}else{
									String[] strAllele=alleleValue.split("/");
									////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
									if(strAllele[0].equalsIgnoreCase(strAllele[1]))
										finalData=strAllele[0];
									else
										finalData=strAllele[0]+"/"+strAllele[1];
								}
							}else{
								finalData=alleleValue;
							}
						}else if(alleleValue.contains(":")){
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
						
						ssrMatrix.write("\t"+finalData);
			    	
			    	}
			    	
			    	
			      }
		    	
		     }
			ssrMatrix.close();				
			
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
		
		//* writing the file to IBWS path
		
		File generatedFileIBWS = new File(pathWB+"/"+folderName + "/" + strFileName + ".txt");

		int iNumOfColsI = listOfAllMarkers.size() + 2;
		//int iNumOfRowsI = listOfNIDs.size() + 1;
		try {
			FileWriter ssrDatastream = new FileWriter(generatedFileIBWS);
			BufferedWriter ssrMatrix = new BufferedWriter(ssrDatastream);
			ssrMatrix.write("\t");
			List allMarkers=new ArrayList();			
			for(int m1 = 0; m1< listOfAllMarkers.size(); m1++){
				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(m1);
				String strMarker = marker.getMarkerName(); 
				ssrMatrix.write("\t"+strMarker);
				allMarkers.add(marker.getMarkerId());
			}		
			
			String finalData="";
			
			for (int j=0;j<gList.size();j++){ 
				String arrList6[]=new String[3];
				String[] gid=gList.get(j).toString().split("~~!!~~");
				//String gName=hmOfNIDAndNVal.get(Integer.parseInt(gid[0].toString())).toString();				
				String gName=hmOfNIDAndNVal.get(gList.get(j)).toString();
				ssrMatrix.write("\n"+hmOfGIDs.get(gList.get(j))+"\t"+gName);	
			    for (int k=0;k<allMarkers.size();k++){
			    	String strKey=gList.get(j)+"!~!"+allMarkers.get(k);
			    	if(hmOfAlleles.containsKey(strKey)){
						
						String alleleValue=hmOfAlleles.get(strKey).toString();
						//System.out.println(gList.get(j)+"!~!"+hmOfMIdAndMarkerName.get(Integer.parseInt(allMarkers.get(k).toString())) +"=:"+alleleValue);
						if(alleleValue.contains("/")){
							if(alleleValue.length()==3){
								if((alleleValue.matches("0/0"))||(alleleValue.equals("?"))){										
									finalData="";
								}else{
									String[] strAllele=alleleValue.split("/");
									////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
									if(strAllele[0].equalsIgnoreCase(strAllele[1]))
										finalData=strAllele[0];
									else
										finalData=strAllele[0]+"/"+strAllele[1];
								}
							}else{
								finalData=alleleValue;
							}
						}else if(alleleValue.contains(":")){
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
						
						ssrMatrix.write("\t"+finalData);
			    	
			    	}
			    	
			    	
			      }
		    	
		     }
						
			
			ssrMatrix.close();	
			
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		}
		
		/*List<File> listOfFiles = new ArrayList<File>();
		listOfFiles.add(generatedFile);
		if(iNumOfCols < 20) {
			pdfFileBuilder.writeToFile();
			File file = pdfFileBuilder.file;
			listOfFiles.add(file);
		}*/
		return generatedFile;
	}

	

	public File MatrixForSSRDataset(GDMSMain theMainHomePage,
			ArrayList<MarkerIdMarkerNameElement> listOfAllMarkers,
			HashMap<Object, String> hmOfNIDAndNVal,
			HashMap<Integer, String> hmOfMIdAndMarkerName, HashMap dataMap, ArrayList gList, HashMap<Object, String> hmOfGIDs) throws GDMSException {

		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+("//")+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		
		 pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
	        //System.out.println("pathWB=:"+pathWB);
	        //pathWB="C:/IBWorkflowSystem/workspace/1-TL1_Groundnut/gdms/output";
        if(!new File(pathWB+"/"+folderName).exists())
	   		new File(pathWB+"/"+folderName).mkdir();
	        
		//System.out.println("ExportFileFormats  listOfAllelicValue:"+listOfAllelicValue);
		
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "/" + strFileName + ".xls");
		HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
		try {

			WritableWorkbook workbook = Workbook.createWorkbook(generatedFile);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);

			Label l=new Label(0,0," ");
			sheet.addCell(l);
			
			List allMarkers=new ArrayList();
			//Writing the Markers from Column-2 onwards in the first row
			HashMap<String, Integer> hashMapOfMakerNamesAndColIndex = new HashMap<String, Integer>();
			for (int i = 0; i < listOfAllMarkers.size(); i++){
				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(i);
				String strMarker = marker.getMarkerName(); 
				Label lMarkerName = new Label((i+2), 0, strMarker + "");
				sheet.addCell(lMarkerName);
				hashMapOfMakerNamesAndColIndex.put(strMarker, i+2);
				allMarkers.add(marker.getMarkerId());
			}
			String finalData="";	
			//System.out.println("hmOfNIDAndNVal:"+hmOfNIDAndNVal);
			for (int j=0;j<gList.size();j++){ 
				String arrList6[]=new String[3];
				String[] gid=gList.get(j).toString().split("~~!!~~");
				//String gName=hmOfNIDAndNVal.get(Integer.parseInt(gid[0].toString())).toString();
				String gName=hmOfNIDAndNVal.get(gList.get(j)).toString();
				//System.out.println("j =:"+gName);
				//iGID=
				Label lGID = new Label(0, (j+1), hmOfGIDs.get(gList.get(j)) + "");
				sheet.addCell(lGID);
				Label GName = new Label(1, (j+1), gName + "");
				sheet.addCell(GName);
					
			    for (int k=0;k<allMarkers.size();k++){
			    	markerAlleles=(HashMap)dataMap.get(gList.get(j));
			    	//System.out.println("markerAlleles=:"+markerAlleles+"   "+gList.get(j));
			    	if(markerAlleles!=null){
			    		String mKey= gList.get(j)+"!~!"+allMarkers.get(k);
			    		//System.out.println("key:"+mKey);
				    	if(markerAlleles.containsKey(mKey)){							
							String alleleValue=markerAlleles.get(mKey).toString();
							//System.out.println(gList.get(j)+"!~!"+hmOfMIdAndMarkerName.get(Integer.parseInt(allMarkers.get(k).toString())) +"=:"+alleleValue);
							if(alleleValue.contains("/")){
								//if((alleleValue.length()==3 && alleleValue.matches("0/0"))||(alleleValue.equals("?"))){										
								if(alleleValue.length()==3){
									if(alleleValue.matches("0/0") ||(alleleValue.equals("?"))){
								
										finalData="";
									}else{
										String[] strAllele=alleleValue.split("/");
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
										if(alleleValue.matches("0:0")||(alleleValue.equals("?"))){		
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
							Label lGName = new Label((k+2), (j+1), finalData + "");
							sheet.addCell(lGName);
				    		
				    	}
			    	}
			      }
		    	
		     }
			
			
			workbook.write();			 
			workbook.close();	
			
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}
			
			//String strFilePath = fileExport.getAbsolutePath();
			File generatedFileIBWS = new File(pathWB+"/"+folderName + "/" + strFileName + ".xls");

			try {

				WritableWorkbook workbookIBWS = Workbook.createWorkbook(generatedFileIBWS);
				WritableSheet sheetIBWS = workbookIBWS.createSheet("DataSheet",0);

				//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
				Label l=new Label(0,0," ");
				sheetIBWS.addCell(l);
				
				List allMarkers=new ArrayList();
				//Writing the Markers from Column-2 onwards in the first row
				HashMap<String, Integer> hashMapOfMakerNamesAndColIndex = new HashMap<String, Integer>();
				for (int i = 0; i < listOfAllMarkers.size(); i++){
					MarkerIdMarkerNameElement marker = listOfAllMarkers.get(i);
					String strMarker = marker.getMarkerName(); 
					Label lMarkerName = new Label((i+2), 0, strMarker + "");
					sheetIBWS.addCell(lMarkerName);
					hashMapOfMakerNamesAndColIndex.put(strMarker, i+2);
					allMarkers.add(marker.getMarkerId());
				}
				String finalData="";	
				//System.out.println("hmOfNIDAndNVal:"+hmOfNIDAndNVal);
				for (int j=0;j<gList.size();j++){ 
					String arrList6[]=new String[3];
					String[] gid=gList.get(j).toString().split("~~!!~~");
					//String gName=hmOfNIDAndNVal.get(Integer.parseInt(gid[0].toString())).toString();
					String gName=hmOfNIDAndNVal.get(gList.get(j)).toString();
					//System.out.println("j =:"+gName);
					//iGID=
					Label lGID = new Label(0, (j+1), hmOfGIDs.get(gList.get(j)) + "");
					sheetIBWS.addCell(lGID);
					Label GName = new Label(1, (j+1), gName + "");
					sheetIBWS.addCell(GName);
						
				    for (int k=0;k<allMarkers.size();k++){
				    	markerAlleles=(HashMap)dataMap.get(gList.get(j));
				    	//System.out.println("markerAlleles=:"+markerAlleles+"   "+gList.get(j));
				    	if(markerAlleles!=null){
				    		String mKey= gList.get(j)+"!~!"+allMarkers.get(k);
				    		//System.out.println("key:"+mKey);
					    	if(markerAlleles.containsKey(mKey)){							
								String alleleValue=markerAlleles.get(mKey).toString();
								//System.out.println(gList.get(j)+"!~!"+hmOfMIdAndMarkerName.get(Integer.parseInt(allMarkers.get(k).toString())) +"=:"+alleleValue);
								if(alleleValue.contains("/")){
									if(alleleValue.length()==3){
										if((alleleValue.matches("0/0"))||(alleleValue.equals("?"))){		
											finalData="";
										}else{
											String[] strAllele=alleleValue.split("/");
											////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
											if(strAllele[0].equalsIgnoreCase(strAllele[1]))
												finalData=strAllele[0];
											else
												finalData=strAllele[0]+"/"+strAllele[1];
										}
									}else{
										finalData=alleleValue;
									}
								}else if(alleleValue.contains(":")){
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
								Label lGName = new Label((k+2), (j+1), finalData + "");
								sheetIBWS.addCell(lGName);
					    		
					    	}
				    	}
				      }
			    	
			     }

				workbookIBWS.write();			 
				workbookIBWS.close();	
			
			

		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}
		return generatedFile;
	}

	public File exportMap(
			GDMSMain theMainHomePage, 
			List<String[]> listToExport, String theFileName) throws WriteException, IOException {

		long time = new Date().getTime();
		
		String strFileName = theFileName + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		String folderName="MarkerTraitFiles";
		String strFilePath = fileExport.getAbsolutePath()+("//")+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "/" + strFileName + ".xls");

		try {

			WritableWorkbook workbook = Workbook.createWorkbook(generatedFile);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);

			//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
			for (int i = 0; i < listToExport.size(); i++){
				String[] strings = listToExport.get(i);
				for (int j = 0; j < strings.length; j++) {
					//System.out.println("%%%%%%%:"+strings[j]);
					if(null == strings[j]) {
						Label lGID = new Label(j, i,  "");
						sheet.addCell(lGID);
						continue;
					} 
					if(strings[j].startsWith("http://")) {
						//Formula f = new Formula(j, i, "HYPERLINK("+ strings[j] + "," + strings[j]);
						sheet.addHyperlink(new WritableHyperlink(j, i, new URL(strings[j])));
					} else {
						Label lGID = new Label(j, i,  strings[j]);
						sheet.addCell(lGID);
					}
				}

			}

			workbook.write();			 
			workbook.close();	

			FileResource fileResource = new FileResource(generatedFile, theMainHomePage);
			theMainHomePage.getMainWindow().getWindow().open(fileResource, "_self");

		} catch (IOException e) {
			throw e;
		} catch (WriteException e) {
			throw e;
		} 
		
		return generatedFile;
	}
	
	
	public File MatrixForDArtDataset(GDMSMain theMainHomePage,
			ArrayList<AllelicValueElement> listOfAllelicValueElements,
			ArrayList<Integer> listOfNIDs, ArrayList<MarkerIdMarkerNameElement> listOfAllMarkers,
			HashMap<Integer, String> hmOfNIDAndNVal) throws GDMSException {
		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+("//")+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "/" + strFileName + ".xls");

		try {

			WritableWorkbook workbook = Workbook.createWorkbook(generatedFile);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);

			//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
			HashMap<Integer, Integer> hashMapOfGIDsAndRowIndex = new HashMap<Integer, Integer>();
			for (int i = 0; i < listOfNIDs.size(); i++){

				Integer iGID = listOfNIDs.get(i);
				Label lGID = new Label(0, (i+1), iGID + "");
				sheet.addCell(lGID);
				hashMapOfGIDsAndRowIndex.put(iGID, (i+1));

				String strGName = hmOfNIDAndNVal.get(iGID);
				Label lGName = new Label(1, (i+1), strGName + "");
				sheet.addCell(lGName);

			}

			//Writing the Markers from Column-2 onwards in the first row
			HashMap<String, Integer> hashMapOfMakerNamesAndColIndex = new HashMap<String, Integer>();
			for (int i = 0; i < listOfAllMarkers.size(); i++){
				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(i);
				String strMarker = marker.getMarkerName(); 
				Label lMarkerName = new Label((i+2), 0, strMarker + "");
				sheet.addCell(lMarkerName);
				hashMapOfMakerNamesAndColIndex.put(strMarker, i+2);
			}

			//Next writing the AlleleValues for the Markers
			for (int i = 0; i < listOfAllelicValueElements.size(); i++){
				AllelicValueElement allelicValueElement = listOfAllelicValueElements.get(i);
				Integer gid = allelicValueElement.getGid();
				String strMarkerName = allelicValueElement.getMarkerName();
				String strData = allelicValueElement.getData();
				if (hashMapOfMakerNamesAndColIndex.containsKey(strMarkerName)) {

					Integer colIndex = hashMapOfMakerNamesAndColIndex.get(strMarkerName);
					int iColIndex = colIndex.intValue();

					int iGIDRowIndex = 0;
					if (listOfNIDs.contains(gid)){
						Integer integer = hashMapOfGIDsAndRowIndex.get(gid);
						iGIDRowIndex = integer.intValue();

						Label lGName = new Label((iColIndex), iGIDRowIndex, strData + "");
						sheet.addCell(lGName);
					}
				}
			}
			workbook.write();			 
			workbook.close();	
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}
		return generatedFile;
	}

	public File MatrixForSNPDataset(
			GDMSMain theMainHomePage,
			ArrayList<MarkerIdMarkerNameElement> listOfAllMarkers,
			HashMap<Object, String> hmOfNIDAndNVal,
			HashMap<Integer, String> hmOfMIdAndMarkerName, HashMap dataMap, ArrayList gList, HashMap<Object, String> hmOfGIDs) throws GDMSException {
		long time = new Date().getTime();
		
		////System.out.println("listofMarkerNamesForSNP=:"+listofMarkerNamesForSNP);
		
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+("//")+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		 pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
	        //System.out.println("pathWB=:"+pathWB);
	        //pathWB="C:/IBWorkflowSystem/workspace/1-TL1_Groundnut/gdms/output";
	        if(!new File(pathWB+"/"+folderName).exists())
		   		new File(pathWB+"/"+folderName).mkdir();
	        
		
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "/" + strFileName + ".xls");
		List allMarkers=new ArrayList();
		
		 HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
		
		try {

			WritableWorkbook workbook = Workbook.createWorkbook(generatedFile);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);

			for (int i = 0; i < listOfAllMarkers.size(); i++){
				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(i);
				String strMarker = marker.getMarkerName(); 
				Label lMarkerName = new Label((i+2), 0, strMarker + "");
				sheet.addCell(lMarkerName);				
				allMarkers.add(marker.getMarkerId());
			}
			String finalData="";	
			for (int j=0;j<gList.size();j++){ 
				String arrList6[]=new String[3];				
				String gName=hmOfNIDAndNVal.get(gList.get(j)).toString();
				
				Label lGID = new Label(0, (j+1), hmOfGIDs.get(gList.get(j)) + "");
				sheet.addCell(lGID);
				Label GName = new Label(1, (j+1), gName + "");
				sheet.addCell(GName);
					
			    for (int k=0;k<allMarkers.size();k++){
			    	markerAlleles=(HashMap)dataMap.get(gList.get(j));
			    	//System.out.println("markerAlleles=:"+markerAlleles+"   "+gList.get(j));
			    	if(markerAlleles!=null){
			    		String mKey= gList.get(j)+"!~!"+allMarkers.get(k);
			    		//System.out.println("key:"+mKey);
				    	if(markerAlleles.containsKey(mKey)){							
							String alleleValue=markerAlleles.get(mKey).toString();
							//System.out.println(gList.get(j)+"!~!"+hmOfMIdAndMarkerName.get(Integer.parseInt(allMarkers.get(k).toString())) +"=:"+alleleValue);
							if(alleleValue.contains("/")){								
								if(alleleValue.length()==3){
									if((alleleValue.matches("-"))||(alleleValue.equals("?"))){		
										finalData="";
									}else{
										String[] strAllele=alleleValue.split("/");
										////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
										if(strAllele[0].equalsIgnoreCase(strAllele[1]))
											finalData=strAllele[0];
										else
											finalData=strAllele[0]+"/"+strAllele[1];
									}
								}else{
									finalData=alleleValue;
								}
								
							}else if(alleleValue.contains(":")){
								if(alleleValue.length()==3){
									if((alleleValue.matches("-"))||(alleleValue.equals("?"))){		
										finalData="";
									}else{
										String[] strAllele=alleleValue.split(":");
										
										if(strAllele[0].equalsIgnoreCase(strAllele[1]))
											finalData=strAllele[0];
										else
											finalData=strAllele[0]+"/"+strAllele[1];
									}
								}else{
									finalData=alleleValue;
								}															
							}else{
								finalData=alleleValue;								
							}
							Label lGName = new Label((k+2), (j+1), finalData + "");
							sheet.addCell(lGName);
				    		
				    	}
			    	}
			      }		    	
		     }			
			workbook.write();			 
			workbook.close();

		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}
		
		
		File generatedFileIBWS = new File(pathWB+"/"+folderName + "/" + strFileName + ".xls");

		try {

			WritableWorkbook workbook = Workbook.createWorkbook(generatedFileIBWS);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);

			//Writing the GIDs and Germplasm Names in the Column-0 and Column-1 respectively
			for (int i = 0; i < listOfAllMarkers.size(); i++){
				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(i);
				String strMarker = marker.getMarkerName(); 
				Label lMarkerName = new Label((i+2), 0, strMarker + "");
				sheet.addCell(lMarkerName);				
				allMarkers.add(marker.getMarkerId());
			}
			String finalData="";	
			//System.out.println("hmOfNIDAndNVal:"+hmOfMIdAndMarkerName);
			for (int j=0;j<gList.size();j++){ 
				String arrList6[]=new String[3];
				String[] gid=gList.get(j).toString().split("~~!!~~");
				//String gName=hmOfNIDAndNVal.get(Integer.parseInt(gid[0].toString())).toString();
				String gName=hmOfNIDAndNVal.get(gList.get(j)).toString();				
				Label lGID = new Label(0, (j+1),  hmOfGIDs.get(gList.get(j)) + "");
				sheet.addCell(lGID);
				Label GName = new Label(1, (j+1), gName + "");
				sheet.addCell(GName);
					
			    for (int k=0;k<allMarkers.size();k++){
			    	markerAlleles=(HashMap)dataMap.get(gList.get(j));
			    	if(markerAlleles!=null){
			    		String mKey= gList.get(j)+"!~!"+allMarkers.get(k);
			    		if(markerAlleles.containsKey(mKey)){							
							String alleleValue=markerAlleles.get(mKey).toString();
							if(alleleValue.contains("/")){
								if(!(alleleValue.matches("-")||(alleleValue.equals("?")))){		
									String[] strAllele=alleleValue.split("/");
									if(strAllele[0].equalsIgnoreCase(strAllele[1]))
										finalData=strAllele[0];
									else
										finalData=strAllele[0]+"/"+strAllele[1];									
								}else{
									finalData=alleleValue;									
								}
							}else if(alleleValue.contains(":")){
								if(!(alleleValue.matches("-")||(alleleValue.equals("?")))){	
									String[] strAllele=alleleValue.split(":");
									if(strAllele[0].equalsIgnoreCase(strAllele[1]))
										finalData=strAllele[0];
									else
										finalData=strAllele[0]+"/"+strAllele[1];
								}else{
									finalData=alleleValue;	
								}								
							}else{
								finalData=alleleValue;								
							}
							Label lGName = new Label((k+2), (j+1), finalData + "");
							sheet.addCell(lGName);
				    		
				    	}
			    	}
			      }		    	
		     }	

			workbook.write();			 
			workbook.close();	

		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}		
		
		
		return generatedFile;
	}

	public File MatrixForMappingDataset(
			GDMSMain theMainHomePage,
			//ArrayList a,
			//String listOfParentGIDs,ArrayList<Integer> accList,
			ArrayList<MarkerIdMarkerNameElement> listOfAllMarkers,
			TreeMap<Object, String> hmOfNIDAndNVal,
			HashMap<Integer, String> hmOfMIdAndMarkerName, HashMap dataMap, ArrayList gList, HashMap<Object, String> hmGids) throws GDMSException {
		long time = new Date().getTime();
		String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = theMainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String folderName="AnalysisFiles";
		String strFilePath = fileExport.getAbsolutePath()+("//")+folderName;
		
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();	
		
		//String strFilePath = fileExport.getAbsolutePath();
		File generatedFile = new File(strFilePath + "/" + strFileName + ".xls");		
		
		try{
			WritableWorkbook workbook = Workbook.createWorkbook(generatedFile);
			WritableSheet sheet = workbook.createSheet("DataSheet",0);			
						
			HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
			List<Integer> allMarkers=new ArrayList<Integer>();
			//Writing the Markers from Column-2 onwards in the first row
			HashMap<String, Integer> hashMapOfMakerNamesAndColIndex = new HashMap<String, Integer>();
			for (int i = 0; i < listOfAllMarkers.size(); i++){
				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(i);
				String strMarker = marker.getMarkerName(); 
				Label lMarkerName = new Label((i+2), 0, strMarker + "");
				sheet.addCell(lMarkerName);
				hashMapOfMakerNamesAndColIndex.put(strMarker, i+2);
				allMarkers.add(marker.getMarkerId());
			}
			String finalData="";	
			/*System.out.println("hmOfNIDAndNVal:"+hmOfNIDAndNVal);
			System.out.println("gList:"+gList);
			System.out.println("hmGids:"+hmGids);*/
			for (int j=0;j<gList.size();j++){ 
				String arrList6[]=new String[3];
				//String[] gid=gList.get(j).toString().split("~~!!~~");
				//String gName=hmOfNIDAndNVal.get(Integer.parseInt(gid[0].toString())).toString();
				//System.out.println("gList.get(j):"+gList.get(j));
				String gName=hmOfNIDAndNVal.get(gList.get(j)).toString();
				//System.out.println("j =:"+gName);
				//iGID=
				Label lGID = new Label(0, (j+1), hmGids.get(gList.get(j)) + "");
				sheet.addCell(lGID);
				Label GName = new Label(1, (j+1), gName + "");
				sheet.addCell(GName);
					
			    for (int k=0;k<allMarkers.size();k++){
			    	markerAlleles=(HashMap)dataMap.get(gList.get(j));
			    	//System.out.println("markerAlleles=:"+markerAlleles+"   "+gList.get(j));
			    	if(markerAlleles!=null){
			    		String mKey= gList.get(j)+"!~!"+allMarkers.get(k);
			    		//System.out.println("key:"+mKey);
				    	if(markerAlleles.containsKey(mKey)){							
							String alleleValue=markerAlleles.get(mKey).toString();
							//System.out.println(gList.get(j)+"!~!"+hmOfMIdAndMarkerName.get(Integer.parseInt(allMarkers.get(k).toString())) +"=:"+alleleValue);
							if(alleleValue.contains("/")){
								if(alleleValue.length()==3){
									if((alleleValue.matches("0/0"))||(alleleValue.equals("?"))){										
										finalData="";
									}else{
										String[] strAllele=alleleValue.split("/");
										////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
										if(strAllele[0].equalsIgnoreCase(strAllele[1]))
											finalData=strAllele[0];
										else
											finalData=strAllele[0]+"/"+strAllele[1];
									}
								}else{
									finalData=alleleValue;
								}
							}else if(alleleValue.contains(":")){
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
							Label lGName = new Label((k+2), (j+1), finalData + "");
							sheet.addCell(lGName);
				    		
				    	}
			    	}
			      }
		    	
		     }
			
			workbook.write();			 
  			workbook.close();
  				
		} catch (IOException e) {
			throw new GDMSException(e.getMessage());
		} catch (RowsExceededException e) {
			throw new GDMSException(e.getMessage());
		} catch (WriteException e) {
			throw new GDMSException(e.getMessage());
		}
		
		 pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
	        if(!new File(pathWB+"/"+folderName).exists())
		   		new File(pathWB+"/"+folderName).mkdir();
	        
		
     
      		File generatedFileIBWS = new File(pathWB+"/"+folderName + "/" + strFileName + ".xls");
      		
      		try{
      			////System.out.println("****************  EXPORT FORMATS CLASS  *****************");					
      			WritableWorkbook workbook = Workbook.createWorkbook(generatedFileIBWS);
      			WritableSheet sheet = workbook.createSheet("DataSheet",0);			
      			
      			HashMap<String,Object> markerAlleles= new HashMap<String,Object>();
    			List<Integer> allMarkers=new ArrayList<Integer>();
    			//Writing the Markers from Column-2 onwards in the first row
    			HashMap<String, Integer> hashMapOfMakerNamesAndColIndex = new HashMap<String, Integer>();
    			for (int i = 0; i < listOfAllMarkers.size(); i++){
    				MarkerIdMarkerNameElement marker = listOfAllMarkers.get(i);
    				String strMarker = marker.getMarkerName(); 
    				Label lMarkerName = new Label((i+2), 0, strMarker + "");
    				sheet.addCell(lMarkerName);
    				hashMapOfMakerNamesAndColIndex.put(strMarker, i+2);
    				allMarkers.add(marker.getMarkerId());
    			}
    			String finalData="";	
    			//System.out.println("hmOfNIDAndNVal:"+hmOfNIDAndNVal);
    			for (int j=0;j<gList.size();j++){ 
    				String arrList6[]=new String[3];
    				String[] gid=gList.get(j).toString().split("~~!!~~");
    				//String gName=hmOfNIDAndNVal.get(Integer.parseInt(gid[0].toString())).toString();
    				String gName=hmOfNIDAndNVal.get(gList.get(j)).toString();
    				//System.out.println("j =:"+gName);
    				//iGID=
    				Label lGID = new Label(0, (j+1), hmGids.get(gList.get(j)) + "");
    				sheet.addCell(lGID);
    				Label GName = new Label(1, (j+1), gName + "");
    				sheet.addCell(GName);
    					
    			    for (int k=0;k<allMarkers.size();k++){
    			    	markerAlleles=(HashMap)dataMap.get(gList.get(j));
    			    	//System.out.println("markerAlleles=:"+markerAlleles+"   "+gList.get(j));
    			    	if(markerAlleles!=null){
    			    		String mKey= gList.get(j)+"!~!"+allMarkers.get(k);
    			    		if(markerAlleles.containsKey(mKey)){							
    							String alleleValue=markerAlleles.get(mKey).toString();
    							//System.out.println(gList.get(j)+"!~!"+hmOfMIdAndMarkerName.get(Integer.parseInt(allMarkers.get(k).toString())) +"=:"+alleleValue);
    							if(alleleValue.contains("/")){
    								if(alleleValue.length()==3){
    									if((alleleValue.matches("0/0"))||(alleleValue.equals("?"))){										
    										finalData="";
	    								}else{
	    									String[] strAllele=alleleValue.split("/");
	    									////System.out.println("strAllele[0]="+strAllele[0]+"    strAllele[1]="+strAllele[1]);
	    									if(strAllele[0].equalsIgnoreCase(strAllele[1]))
	    										finalData=strAllele[0];
	    									else
	    										finalData=strAllele[0]+"/"+strAllele[1];
	    								}
	    							}else{
										finalData=alleleValue;
									}
    							}else if(alleleValue.contains(":")){
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
    							Label lGName = new Label((k+2), (j+1), finalData + "");
    							sheet.addCell(lGName);
    				    		
    				    	}
    			    	}
    			      }
    		    	
    		     }
      			
      			workbook.write();			 
      			workbook.close();		
      			
      		
      		} catch (IOException e) {
      			throw new GDMSException(e.getMessage());
      		} catch (RowsExceededException e) {
      			throw new GDMSException(e.getMessage());
      		} catch (WriteException e) {
      			throw new GDMSException(e.getMessage());
      		}
      		
		
		
		
		return generatedFile;
	}
	/*public String MarkerNameIdList(ArrayList markList){		
		String MarkerIdNameList="";
		for(int i=0;i<markList.size();i++){
			MarkerIdNameList=MarkerIdNameList+markList.get(i)+"!&&!";
		}
		////System.out.println("MarkerIdNameList=:"+MarkerIdNameList);
		return MarkerIdNameList;
	}*/

	/*public void exportToPdf(Table table, GDMSMain theMainHomePage) {
		PdfExporter pdfExporter = new PdfExporter();
		File exportToPDF = pdfExporter.exportToPDF(table);
		FileResource fileResource = new FileResource(exportToPDF, theMainHomePage);
		theMainHomePage.getMainWindow().getWindow().open(fileResource, "_blank");
	}

	public void exportToPdf(List<String[]> theData, GDMSMain theMainHomePage) {
		PdfExporter pdfExporter = new PdfExporter();
		if(null == theData || 0 == theData.size()) {
			return;
		}
		Table table = new Table();
		
		for (int i = 0; i < theData.get(0).length; i++){
			table.addContainerProperty(theData.get(0)[i], String.class, null);
		}
		for (int i = 1; i < theData.get(0).length; i++){
			table.addItem(theData.get(i), new Integer(i - 1));
		}
		File exportToPDF = pdfExporter.exportToPDF(table);
		FileResource fileResource = new FileResource(exportToPDF, theMainHomePage);
		theMainHomePage.getMainWindow().getWindow().open(fileResource, "_blank");
	}*/
	public File exportToKBio(ArrayList markersList, GDMSMain _mainHomePage) {
		// 20131512 : Kalyani added to create kbio order form
		
		String folderName="LGC_GenomicsOrderForms";		
		WebApplicationContext ctx = (WebApplicationContext) _mainHomePage.getContext();
        String strTemplateFolderPath = ctx.getHttpSession().getServletContext().getRealPath("\\VAADIN\\themes\\gdmstheme\\Templates");
        ////System.out.println("Folder-Path: " + strTemplateFolderPath);
		
      //String strMarkerType = _strMarkerType.replace(" ", "");
		String strSrcFileName = strTemplateFolderPath+"\\snp_template.xls";
		
		
		long time = new Date().getTime();
		//String strFileName = "Matrix" + String.valueOf(time);
		File baseDirectory = _mainHomePage.getMainWindow().getApplication().getContext().getBaseDirectory();
		File absoluteFile = baseDirectory.getAbsoluteFile();

		File[] listFiles = absoluteFile.listFiles();
		File fileExport = baseDirectory;
		for (File file : listFiles) {
			if(file.getAbsolutePath().endsWith("FileExports")) {
				fileExport = file;
				break;
			}
		}
		
		String strFilePath = fileExport.getAbsolutePath()+("//")+folderName;
		/*bPath="C:\\IBWorkflowSystem\\infrastructure\\tomcat\\webapps\\GDMS";
	    opPath=bPath.substring(0, bPath.indexOf("IBWorkflowSystem")-1);
	     */
		if(!new File(strFilePath).exists())
	       	new File(strFilePath).mkdir();
		
		////System.out.println("strFilePath=:"+strFilePath);
		
		String destFileWF=strFilePath+"/LGC"+String.valueOf(time)+".xls";
		File generatedFile = new File(destFileWF);
		
		
		 pathWB=instDir+"/workspace/"+currWorkingProject+"/gdms/output";
	        //System.out.println("pathWB=:"+pathWB);
	        //pathWB="C:/IBWorkflowSystem/workspace/1-TL1_Groundnut/gdms/output";
	        if(!new File(pathWB+"/"+folderName).exists())
		   		new File(pathWB+"/"+folderName).mkdir();
	        
			
	        String strFilePathIBWS=pathWB+"/"+folderName;
	        String destFileIBWFS=strFilePathIBWS+"/LGC"+String.valueOf(time)+".xls";
			File generatedFileWF = new File(destFileIBWFS);
		
		try{
		
			File strFileLoc = new File(strSrcFileName);
			FileResource fileResource = new FileResource(strFileLoc, _mainHomePage);
			
			InputStream oInStream = new FileInputStream(strSrcFileName);
	        OutputStream oOutStream = new FileOutputStream(destFileWF);
	
	        OutputStream oOutStreamWF = new FileOutputStream(destFileIBWFS);
	        
	        // Transfer bytes from in to out
	        byte[] oBytes = new byte[1024];
	        int nLength;
	        BufferedInputStream oBuffInputStream = 
	                        new BufferedInputStream( oInStream );
	        while ((nLength = oBuffInputStream.read(oBytes)) > 0) 
	        {
	            oOutStream.write(oBytes, 0, nLength);
	        }
	        oInStream.close();
	        oOutStream.close();
	        
	        FileInputStream file = new FileInputStream(generatedFile);
	        
	        HSSFWorkbook workbook = new HSSFWorkbook(file);
	        HSSFSheet sheet = workbook.getSheetAt(1);
	        Row row = null;
	        int rowNum=2;
	        Cell cell = null;
	       
	        for(int m1=0;m1<markersList.size();m1++){
		    	 int colnum = 0;
		    	 row = sheet.getRow(rowNum);	        
		    	 if(row == null){row = sheet.createRow(rowNum);}
	             cell = row.getCell(0);
	             if (cell == null)
		    		 cell = row.createCell(0);
	             cell.setCellValue(cell.getStringCellValue()+markersList.get(m1).toString());
		
		    	 rowNum++;
		     }
	      
	        file.close();	         
	        FileOutputStream outFile =new FileOutputStream(destFileWF);
	        workbook.write(outFile);
	        outFile.close();
	             
	        
	        
	       
	        
	        FileInputStream fileIBWS = new FileInputStream(destFileWF);	        
	        HSSFWorkbook workbookIBWS = new HSSFWorkbook(fileIBWS);
	        HSSFSheet sheetIBWS = workbookIBWS.getSheetAt(1);
	        Row rowIBWS = null;
	        int rowNumIBWS=2;
	        Cell cellIBWS = null;	       
	        for(int m1=0;m1<markersList.size();m1++){
		    	 int colnum = 0;
		    	 rowIBWS = sheetIBWS.getRow(rowNumIBWS);	        
		    	 if(rowIBWS == null){rowIBWS = sheetIBWS.createRow(rowNumIBWS);}
	             cellIBWS = rowIBWS.getCell(0);
	             if (cellIBWS == null)
		    		 cellIBWS = rowIBWS.createCell(0);
	             if(cellIBWS.getStringCellValue()==null)
	            	 cellIBWS.setCellValue(cellIBWS.getStringCellValue()+markersList.get(m1).toString());
		
		    	 rowNumIBWS++;
		     }
	      
	        fileIBWS.close();
	         
	        FileOutputStream outFileIWBS =new FileOutputStream(destFileIBWFS);
	        workbookIBWS.write(outFileIWBS);
	        outFileIWBS.close();
	        
	        
	        
		}catch(Exception e){
			e.printStackTrace();
		}
             
		return generatedFile;
		
		// TODO Auto-generated method stub
	}
	

}
