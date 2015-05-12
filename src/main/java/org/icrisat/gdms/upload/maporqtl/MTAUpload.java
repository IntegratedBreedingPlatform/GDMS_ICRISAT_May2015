package org.icrisat.gdms.upload.maporqtl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.manager.GdmsTable;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.GenotypicDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.gdms.DatasetElement;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.icrisat.gdms.common.GDMSException;
import org.icrisat.gdms.ui.FieldProperties;
import org.icrisat.gdms.ui.GDMSMain;
import org.icrisat.gdms.ui.common.GDMSModel;
import org.icrisat.gdms.upload.UploadMarker;
import org.icrisat.gdms.upload.genotyping.DatasetBean;
import org.icrisat.gdms.upload.genotyping.GenotypeUsersBean;
import org.icrisat.gdms.upload.genotyping.MTABean;
import org.icrisat.gdms.upload.genotyping.MTAmetadataBean;
import org.icrisat.gdms.upload.marker.UploadField;

public class MTAUpload implements UploadMarker {

	private String strFileLocation;
	private Workbook workbook;
	private String[] strSheetNames;
	private ArrayList<HashMap<String, String>> listOfDataInSourceSheet;
	private ArrayList<HashMap<String, String>> listOfDataInDataSheet;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromSourceTable;
	private ArrayList<HashMap<String, String>> listOfDataRowsFromDataTable;
	private ArrayList<HashMap<String, String>> listOfGIDRowsFromGIDTableForDArT;
	private GDMSMain _mainHomePage;
	
	
	/*private DatasetUsers datasetUser;
	private Dataset dataset;
	private Mta mta;*/
	
	private GenotypeUsersBean datasetUser;
	private DatasetBean dataset;
	private MTABean mta;
	private MTAmetadataBean mtaMetadata;
	private MTABean[] arrayOfMTAs;
	
	private Transaction tx;
	
	private Session localSession;
	private Session centralSession;

	ArrayList traitsList=new ArrayList();
	
	ArrayList markersList=new ArrayList();
	ArrayList<Integer> listOfMarkerIds = new ArrayList<Integer>();
	
	HashMap<String, Integer> hmOfSelectedMNamesandMID = new HashMap<String, Integer>();
	
	
	String traits="";
	
	ManagerFactory factory;
    GenotypicDataManager genoManager;
    OntologyDataManager om;
    ArrayList traitsComList=new ArrayList();
    SortedMap mapTraits = new TreeMap();
    
    Integer mapId =0;
    
    String alertT="no";
    int size=0;
    String notExisTraits="";
    
    //Integer strEffect =0;
    Float strEffect;
    
    String strErrorMsg="no";
    
	@Override
	public void readExcelFile() throws GDMSException {
		try {
			workbook = Workbook.getWorkbook(new File(strFileLocation));
			strSheetNames = workbook.getSheetNames();
		} catch (BiffException e) {
			throw new GDMSException("Error Reading MTA Upload Sheet - " + e.getMessage());
		} catch (IOException e) {
			throw new GDMSException("Error Reading MTA Upload Sheet - " + e.getMessage());
		}
	}

	@Override
	public String validateDataInExcelSheet() throws GDMSException {

		if (false == strSheetNames[0].equalsIgnoreCase("mta_source")){
			throw new GDMSException("MTA_Source Sheet Name Not Found");
		}

		if (false == strSheetNames[1].equalsIgnoreCase("mta_data")){
			throw new GDMSException("MTA_Data Sheet Name Not Found");
		}


		//check the template fields
		for(int i = 0; i < strSheetNames.length; i++){

			String strSName = strSheetNames[i].toString();

			if(strSName.equalsIgnoreCase("MTA_Source")){

				Sheet mtaSourceSheet = workbook.getSheet(strSName);
				//String strArrayOfReqColNames[] = {"Institute", "Principle investigator", "Dataset Name", "Dataset description", "Genus", "Species", "Remark"};
				String strArrayOfReqColNames[] = {"Institute", "Principle investigator", "Dataset Name", "Dataset Description", "Genus", "Species", "Method", "Score", "Project", "Population", "Population size", "Position units", "Remark"};

				for(int j = 0; j < strArrayOfReqColNames.length; j++){
					String strColNameFromTemplate = (String)mtaSourceSheet.getCell(0, j).getContents().trim();
					if(!strArrayOfReqColNames[j].toLowerCase().contains(strColNameFromTemplate.toLowerCase())){
						throw new GDMSException("Column " + strArrayOfReqColNames[j].toLowerCase() + " not found");
					}
					if(strColNameFromTemplate == null || strColNameFromTemplate == ""){
						throw new GDMSException("Delete empty column " + strColNameFromTemplate);
					}
				}	

				//After checking for the required columns, have  to verify if the values have been provided for
				//all the required columns.
				//That is value at cell positon(1, n) should not be null or empty

				//Checking for value at Row#:0 Institute
				String strInstitue = mtaSourceSheet.getCell(1, 0).getContents().trim().toString();
				if (null == strInstitue){
					throw new GDMSException("Please provide the value for Institute at position (1, 0) in MTA_Source sheet of the template.");
				} else if (strInstitue.equals("")){
					throw new GDMSException("Please provide the value for Institute at position (1, 0) in MTA_Source sheet of the template.");
				}

				//Checking for value at Row#:2 Dataset Name
				String strDatasetName = mtaSourceSheet.getCell(1, 2).getContents().trim().toString();
				if (null == strDatasetName){
					throw new GDMSException("Please provide the value for Dataset Name at position (1, 2) in MTA_Source sheet of the template.");
				} else if (strDatasetName.equals("")){
					throw new GDMSException("Please provide the value for Dataset Name at position (1, 2) in MTA_Source sheet of the template.");
				}

				//Checking for value at Row#:3 Dataset Description
				String strDatasetDescription = mtaSourceSheet.getCell(1, 3).getContents().trim().toString();
				if (null == strDatasetDescription){
					throw new GDMSException("Please provide the value for Dataset Description at position (1, 3) in MTA_Source sheet of the template.");
				} else if (strDatasetDescription.equals("")){
					throw new GDMSException("Please provide the value for Dataset Description at position (1, 3) in MTA_Source sheet of the template.");
				}

				//Checking for value at Row#:4 Genus
				String strGenus = mtaSourceSheet.getCell(1, 4).getContents().trim().toString();
				if (null == strGenus){
					throw new GDMSException("Please provide the value for Genus at position (1, 4) in MTA_Source sheet of the template.");
				} else if (strGenus.equals("")){
					throw new GDMSException("Please provide the value for Genus at position (1, 4) in MTA_Source sheet of the template.");
				}
				//Checking for value at Row#:4 Genus
				String strSpecies = mtaSourceSheet.getCell(1, 5).getContents().trim().toString();
				if (null == strSpecies){
					throw new GDMSException("Please provide the value for Species at position (1, 5) in MTA_Source sheet of the template.");
				} else if (strSpecies.equals("")){
					throw new GDMSException("Please provide the value for Species at position (1, 5) in MTA_Source sheet of the template.");
				}
				
				//Checking for value at Row#:4 Genus
				String strMethod = mtaSourceSheet.getCell(1, 6).getContents().trim().toString();
				if (null == strMethod){
					throw new GDMSException("Please provide the value for Method at position (1, 6) in MTA_Source sheet of the template.");
				} else if (strMethod.equals("")){
					throw new GDMSException("Please provide the value for Method at position (1, 6) in MTA_Source sheet of the template.");
				}
				
				//Checking for value at Row#:4 Genus
				String strScore = mtaSourceSheet.getCell(1, 7).getContents().trim().toString();
				if (null == strScore){
					throw new GDMSException("Please provide the value for Score at position (1, 7) in MTA_Source sheet of the template.");
				} else if (strScore.equals("")){
					throw new GDMSException("Please provide the value for Score at position (1, 7) in MTA_Source sheet of the template.");
				}
				
				//Checking for value at Row#:4 Genus
				String strProject = mtaSourceSheet.getCell(1, 8).getContents().trim().toString();
				if (null == strProject){
					throw new GDMSException("Please provide the value for Project at position (1, 8) in MTA_Source sheet of the template.");
				} else if (strProject.equals("")){
					throw new GDMSException("Please provide the value for Project at position (1, 8) in MTA_Source sheet of the template.");
				}
				

			}

			//SSR_DataList fields validation
			if(strSName.equalsIgnoreCase("MTA_Data")){

				Sheet sheetMTAData = workbook.getSheet(strSName);
				
				String strArrayOfRequiredColumnNames[] = {"Marker", "Gene", "Chromosome", "Position", "Map-Name", "Trait", "Effect", "Score (e.g.,LOD (or) -log10 (p))", "R2", "Allele A", "Allele B", "Allele A Phenotype", "Allele B Phenotype", "Freq Allele A", "Freq Allele B", "P value-Uncorrected", "Pvalue -corrected", "Correction method", "Trait average for allele A", "Trait average for allele B", "Dominance", "Evidence", "Reference", "Notes"};

				//String strArrayOfRequiredColumnNames[] = {"Marker", "Chromosome", "Map-Name", "Position", "Trait", "Effect", "High value allele", "Experiment", "Score (e.g.,LOD (or) -log10 (p))", "R2"};

				for(int j = 0; j < strArrayOfRequiredColumnNames.length; j++){
					String strColNamesFromDataSheet = (String)sheetMTAData.getCell(j, 0).getContents().trim();
					//System.out.println("strArrayOfRequiredColumnNames[j].toLowerCase()=:"+strArrayOfRequiredColumnNames[j].toLowerCase()+"    strColNamesFromDataSheet :"+strColNamesFromDataSheet.toLowerCase()+"    "+strArrayOfRequiredColumnNames[j].toLowerCase().contains(strColNamesFromDataSheet.toLowerCase()));
					if(!strArrayOfRequiredColumnNames[j].toLowerCase().contains(strColNamesFromDataSheet.toLowerCase())){
						throw new GDMSException("column " + strColNamesFromDataSheet + " not found.");
					}
					if(strColNamesFromDataSheet == null || strColNamesFromDataSheet == ""){
						throw new GDMSException("Delete column " + strColNamesFromDataSheet);
					}
				}


				int iNumOfRows = sheetMTAData.getRows();

				for (int r = 1; r < iNumOfRows; r++){

					//0 --- Marker					
					String strMarker = sheetMTAData.getCell(0, r).getContents().trim().toString();
					if (strMarker.equals("")){
						String strErrMsg = "Please provide value in Marker column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					/*String strName = sheetMTAData.getCell(0, r).getContents().trim().toString();
					if (strName.equals("")){
						String strErrMsg = "Please provide value in Project column at row:" + r;
						throw new GDMSException(strErrMsg);
					}
*/

					//1 --- Gene	
					String strGene = sheetMTAData.getCell(1, r).getContents().trim().toString();
					if (strGene.equals("")){
						String strErrMsg = "Please provide value in Gene column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					//2 --- Chromosome	
					String strChromosome = sheetMTAData.getCell(2, r).getContents().trim().toString();
					if (strChromosome.equals("")){
						String strErrMsg = "Please provide value in Chromosome column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					
					//3 --- Position	
					String strPosition = sheetMTAData.getCell(3, r).getContents().trim().toString();
					if (strPosition.equals("")){
						String strErrMsg = "Please provide value in Allele A Phenotype column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					//4 --- MapName	
					String strMapName = sheetMTAData.getCell(4, r).getContents().trim().toString();
					if (strMapName.equals("")){
						String strErrMsg = "Please provide value in Allele B Phenotype column at row:" + r;
						throw new GDMSException(strErrMsg);
					}
					
					
					//5 --- Trait	
					String strTrait = sheetMTAData.getCell(5, r).getContents().trim().toString();
					if(!traitsList.contains(strTrait)){
						traitsList.add(strTrait);
						traits=traits+"'"+strTrait+"',";
					}
					if (strTrait.equals("")){
						String strErrMsg = "Please provide value in Trait column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					//6 -- Effect
					String strEffect = sheetMTAData.getCell(6, r).getContents().trim().toString();
					if (strEffect.equals("")){
						String strErrMsg = "Please provide value in Allele A column at row:" + r;
						throw new GDMSException(strErrMsg);
					}
					//9 --- AlleleA	
					String strAlleleA = sheetMTAData.getCell(9, r).getContents().trim().toString();
					if (strAlleleA.equals("")){
						String strErrMsg = "Please provide value in Allele A column at row:" + r;
						throw new GDMSException(strErrMsg);
					}
					//10 --- AlleleB	
					String strAlleleB = sheetMTAData.getCell(10, r).getContents().trim().toString();
					if (strAlleleB.equals("")){
						String strErrMsg = "Please provide value in Allele B column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					/*//7 --- Experiment	
					String strAlleleAPhenotype = sheetMTAData.getCell(11, r).getContents().trim().toString();
					if (strAlleleAPhenotype.equals("")){
						String strErrMsg = "Please provide value in Allele A Phenotype column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					//8 --- Score Value	
					String strAlleleBPhenotype = sheetMTAData.getCell(12, r).getContents().trim().toString();
					if (strAlleleBPhenotype.equals("")){
						String strErrMsg = "Please provide value in Allele B Phenotype column at row:" + r;
						throw new GDMSException(strErrMsg);
					}*/

/*
					//9 --- R2	
					String strEvidence = sheetMTAData.getCell(23, r).getContents().trim().toString();
					if (strEvidence.equals("")){
						String strErrMsg = "Please provide value in Evidence column at row:" + r;
						throw new GDMSException(strErrMsg);
					}

					String strReference = sheetMTAData.getCell(24, r).getContents().trim().toString();
					if (strReference.equals("")){
						String strErrMsg = "Please provide value in Reference column at row:" + r;
						throw new GDMSException(strErrMsg);
					}*/
					
				}
			}
		}
		return strErrorMsg;
	}

	@Override
public void createObjectsToBeDisplayedOnGUI(GDMSMain theMainHomePage) throws GDMSException {
		
		_mainHomePage = theMainHomePage;

		Sheet sourceSheet = workbook.getSheet(0);
		listOfDataInSourceSheet = new ArrayList<HashMap<String,String>>();

		HashMap<String, String> hmOfDataInSourceSheet = new HashMap<String, String>();

		String strInstitute = sourceSheet.getCell(1, 0).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Institute.toString(), strInstitute);

		String strPrincipalInvestigator = sourceSheet.getCell(1, 1).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PrincipleInvestigator.toString(), strPrincipalInvestigator);

		String strDatasetName = sourceSheet.getCell(1, 2).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.DatasetName.toString(), strDatasetName);

		String strDatasetDescription = sourceSheet.getCell(1, 3).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.DatasetDescription.toString(), strDatasetDescription);

		String strGenus = sourceSheet.getCell(1, 4).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Genus.toString(), strGenus);

		String strSpecies = sourceSheet.getCell(1, 5).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Species.toString(), strSpecies);

		String strMethod = sourceSheet.getCell(1, 6).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Method.toString(), strMethod);

		String strScore = sourceSheet.getCell(1, 7).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Score.toString(), strScore);
		
		String strProject = sourceSheet.getCell(1, 8).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Project.toString(), strProject);

		String strPopulation = sourceSheet.getCell(1, 9).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Population.toString(), strPopulation);
		
		String strPopSize = sourceSheet.getCell(1, 10).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PopulationSize.toString(), strPopSize);

		String strPositionUnits = sourceSheet.getCell(1, 11).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.PositionUnits.toString(), strPositionUnits);		
		
		String strRemark = sourceSheet.getCell(1, 12).getContents().toString();
		hmOfDataInSourceSheet.put(UploadField.Remark.toString(), strRemark);

		listOfDataInSourceSheet.add(hmOfDataInSourceSheet);

		
		Sheet dataSheet = workbook.getSheet(1);
		int iNumOfRowsInDataSheet = dataSheet.getRows();
		listOfDataInDataSheet = new ArrayList<HashMap<String,String>>();

		for (int rIndex = 1; rIndex < iNumOfRowsInDataSheet; rIndex++){

			HashMap<String, String> hmOfDataInDataSheet = new HashMap<String, String>();

			String strMarker = dataSheet.getCell(0, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Marker.toString(), strMarker);		
			
			String strGene = dataSheet.getCell(1, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Gene.toString(), strGene);

			String strChr = dataSheet.getCell(2, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Chromosome.toString(), strChr);			

			String strPosition = dataSheet.getCell(3, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Position.toString(), strPosition);
			
			String strMapName = dataSheet.getCell(4, rIndex).getContents().toString();			
			hmOfDataInDataSheet.put(UploadField.MapName.toString(), strMapName);
			
			String strTrait = dataSheet.getCell(5, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.TraitID.toString(), strTrait);

			String strEffect = dataSheet.getCell(6, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Effect.toString(), strEffect);			
			
			String strScoreVal = dataSheet.getCell(7, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.ScoreValue.toString(), strScoreVal);
			
			String strR2 = dataSheet.getCell(8, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.R2.toString(), strR2);

			String strAlleleA = dataSheet.getCell(9, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Allele_A.toString(), strAlleleA);
			
			String strAlleleB = dataSheet.getCell(10, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Allele_B.toString(), strAlleleB);
			
			String strAlleleAPhenotype = dataSheet.getCell(11, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Allele_A_Phenotype.toString(), strAlleleAPhenotype);
			
			String strAlleleBPhenotype = dataSheet.getCell(12, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Allele_B_Phenotype.toString(), strAlleleBPhenotype);
			
			String strFreqAlleleA = dataSheet.getCell(13, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Freq_Allele_A.toString(), strFreqAlleleA);
			
			String strFreqAlleleB = dataSheet.getCell(14, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Freq_Allele_B.toString(), strFreqAlleleB);			
			
			String strPValueUnCorrected = dataSheet.getCell(15, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Pvalue_Uncorrected.toString(), strPValueUnCorrected);
			
			String strPValueCorrected = dataSheet.getCell(16, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Pvalue_corrected.toString(), strPValueCorrected);			
			
			String strCorrectionMethod = dataSheet.getCell(17, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.CorrectionMethod.toString(), strCorrectionMethod);
			
			
			String strTrAvgAlleleA = dataSheet.getCell(18, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.TraitAverageForAlleleA.toString(), strTrAvgAlleleA);
			
			String strTrAvgAlleleB = dataSheet.getCell(19, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.TraitAverageForAlleleB.toString(), strTrAvgAlleleB);			
			
			
			String strDominance = dataSheet.getCell(20, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Dominance.toString(), strDominance);			
			
			String strEvidence = dataSheet.getCell(21, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Evidence.toString(), strEvidence);
			
			String strReference = dataSheet.getCell(22, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Reference.toString(), strReference);
			
			String strNotes = dataSheet.getCell(23, rIndex).getContents().toString();
			hmOfDataInDataSheet.put(UploadField.Notes.toString(), strNotes);	
			

			listOfDataInDataSheet.add(hmOfDataInDataSheet);
		}
	}


	@Override
	public void setFileLocation(String theAbsolutePath) {
		strFileLocation = theAbsolutePath;
	}

	@Override
	public void setDataToBeUploded(
			ArrayList<HashMap<String, String>> theListOfSourceDataRows,
			ArrayList<HashMap<String, String>> listOfDataRows,
			ArrayList<HashMap<String, String>> listOfGIDRows) {
		listOfDataRowsFromSourceTable = theListOfSourceDataRows;
		listOfDataRowsFromDataTable = listOfDataRows;
		listOfGIDRowsFromGIDTableForDArT = listOfGIDRows;
	}

	@Override
	public void upload() throws GDMSException {
		//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$");
		validateData();
		createObjectsToBeSavedToDB();
	}

	@Override
	public void validateData() throws GDMSException {
		mapTraits = new TreeMap();
        List retTraits = new ArrayList();
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			//genoManager=factory.getGenotypicDataManager();
			om=factory.getNewOntologyDataManager();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		//System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		String strArrayOfReqColNames[] = {UploadField.Institute.toString(), UploadField.DatasetName.toString(), UploadField.DatasetDescription.toString(), 
				UploadField.Genus.toString()};

		HashMap<String, String> hashMapOfFieldsAndValuesFromSource = listOfDataRowsFromSourceTable.get(0);
		for(int j = 0; j < strArrayOfReqColNames.length; j++){
			String strReqCol = strArrayOfReqColNames[j];
			if(false == hashMapOfFieldsAndValuesFromSource.containsKey(strReqCol)){
				throw new GDMSException("Column " + strArrayOfReqColNames[j].toLowerCase() + " not found in MTA_Source sheet.");
			} else {
				String strReqColValue = hashMapOfFieldsAndValuesFromSource.get(strReqCol);
				if (null == strReqColValue){
					throw new GDMSException("Please provide the value for " +  strReqCol  + " in MTA_Source sheet of the template.");
				} else if (strReqColValue.equals("")){
					throw new GDMSException("Please provide the value for " +  strReqCol  + " in MTA_Source sheet of the template.");
				}
			}
		}
		
		String strArrayOfRequiredColumnNames[] = {UploadField.Marker.toString(), UploadField.Chromosome.toString(), UploadField.MapName.toString(), 
				UploadField.Position.toString(), UploadField.TraitID.toString(),  UploadField.Effect.toString(),
				UploadField.HighValueAllele.toString(), UploadField.Experiment.toString(), UploadField.ScoreValue.toString(), UploadField.R2.toString()};

		for(int j = 0; j < listOfDataRowsFromDataTable.size(); j++){
			String strReqColInDataSheet = strArrayOfRequiredColumnNames[j];
			HashMap<String, String> hashMapOfDataRow = listOfDataRowsFromDataTable.get(j);
			if(false == hashMapOfDataRow.containsKey(strReqColInDataSheet)){
				throw new GDMSException("Column " + strArrayOfReqColNames[j] + " not found in QTL_Data sheet.");
			} else {
				String strReqColValue = hashMapOfDataRow.get(strReqColInDataSheet);
				if (null == strReqColValue){
					throw new GDMSException("Please provide the value for " +  strReqColInDataSheet  + " in MTA_Data sheet of the template.");
				} else if (strReqColValue.equals("")){
					throw new GDMSException("Please provide the value for " +  strReqColInDataSheet  + " in MTA_Data sheet of the template.");
				}
			}			
			
		}
		
		try{
        	//System.out.println(" ......................    traitList=:"+traitsList);
	        for(int t=0;t<traitsList.size();t++){	        
	        	Set<StandardVariable> standardVariables = om.findStandardVariablesByNameOrSynonym(traitsList.get(t).toString());
				for (StandardVariable stdVar : standardVariables) {
					traitsComList.add(stdVar.getId());
					retTraits.add(stdVar.getName());
					mapTraits.put(stdVar.getName(), stdVar.getId());
					
				}
	        }
		} catch (MiddlewareQueryException e) {
			//throw new GDMSException(e.getMessage());
			e.printStackTrace();
		}
		if(mapTraits.size()==0){
         	alertT="yes";
	        size=0;
	     }
         if(mapTraits.size()>0){
         	for(int t=0;t<traitsList.size();t++){	        	   
         		 if(!retTraits.contains(traitsList.get(t).toString())){
        		   alertT="yes";
        		   size=mapTraits.size();
        		   notExisTraits=notExisTraits+traitsList.get(t).toString()+", ";
		        }
         	}
         }
         
         if(alertT.equalsIgnoreCase("yes")){
            if(size==0){
            	String strErrMsg = "The Trait(s) provided do not exist in the database. \n Please upload the relevant information";
				throw new GDMSException(strErrMsg);
     	   	}else{
     	   		String strErrMsg = "The following Trait(s) provided do not exist in the database. \n Please upload the relevant information. \n"+notExisTraits.substring(0,notExisTraits.length()-1);
     	   		throw new GDMSException(strErrMsg);
     	   	}
           
         }    
	}

	@Override
	public void createObjectsToBeSavedToDB() throws GDMSException {
		
		Integer iDatasetId = null; //Will be set/overridden by the function
		long lastId =0;
		String strDatasetType = "MTA";
		Date uploadTemplateDate = new Date(System.currentTimeMillis()); 
		String strDataType = "int"; 
		//String strMissingData = null;
		
		try{
			factory=GDMSModel.getGDMSModel().getManagerFactory();
			genoManager=factory.getGenotypicDataManager();
			om=factory.getNewOntologyDataManager();
			localSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession();
			//centralSession = GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession();
		}catch (Exception e){
			e.printStackTrace();
		}
		tx=localSession.beginTransaction();
		int iNumOfQTLDataRowsFromDataTable = listOfDataRowsFromDataTable.size();
		
		HashMap<String, String> hashMapOfSourceDataFields = listOfDataRowsFromSourceTable.get(0);
		int strIntPopSize=0;
		//Assigning the User Id value based on the Principle investigator's value in the MTA_Source sheet
		Integer iUserId = 0;
		String strPrincipleInvestigator = hashMapOfSourceDataFields.get(UploadField.PrincipleInvestigator.toString());
		String strDatasetName = hashMapOfSourceDataFields.get(UploadField.DatasetName.toString());
		String strDatasetDesc = hashMapOfSourceDataFields.get(UploadField.DatasetDescription.toString());
		String strGenus = hashMapOfSourceDataFields.get(UploadField.Genus.toString());
		String strSpecies = hashMapOfSourceDataFields.get(UploadField.Species.toString()); 
		String strRemarks = hashMapOfSourceDataFields.get(UploadField.Remark.toString());
		String strInstitute = hashMapOfSourceDataFields.get(UploadField.Institute.toString());
		String strMethod=hashMapOfSourceDataFields.get(UploadField.Method.toString());
		String strScore=hashMapOfSourceDataFields.get(UploadField.Score.toString());
		String strProject=hashMapOfSourceDataFields.get(UploadField.Project.toString());
		String strEmail = null;
		String strPurposeOfStudy = null;
		
		String strPopulation=hashMapOfSourceDataFields.get(UploadField.Population.toString());
		String strPopSize=hashMapOfSourceDataFields.get(UploadField.PopulationSize.toString());
		if(!strPopSize.equals(""))
			strIntPopSize=Integer.parseInt(strPopSize);
		String strPopUnits=hashMapOfSourceDataFields.get(UploadField.PositionUnits.toString());
		
		
		try {
			
			iUserId=GDMSModel.getGDMSModel().getWorkbenchDataManager().getWorkbenchRuntimeData().getUserId();
			
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		
		try{
			lastId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_DATASET);
		}catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		int intDatasetId=(int)lastId;
		
		iDatasetId=intDatasetId-1;
		
		try{
			List<DatasetElement> results =genoManager.getDatasetDetailsByDatasetName(strDatasetName, Database.CENTRAL);
			if(results.isEmpty()){			
				results =genoManager.getDatasetDetailsByDatasetName(strDatasetName, Database.LOCAL);
				if(results.size()>0)
					throw new GDMSException("Dataset Name already exists.");
			}else 
				throw new GDMSException("Dataset Name already exists.");
		
		} catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		if(strDatasetName.length()>30){
			//ErrMsg = "Dataset Name value exceeds max char size.";
			throw new GDMSException("Dataset Name value exceeds max char size.");
		}
		
		Integer iMTAId = null;
		String mon="";
		Calendar cal = new GregorianCalendar();
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		if(month>=10) 
			mon=String.valueOf(month+1);
		else 
			mon="0"+(month+1);
		  
		 String curDate=year+"-"+mon+"-"+day;
		//genoManager
		arrayOfMTAs = new MTABean[iNumOfQTLDataRowsFromDataTable];
		int iUploadedQTLCtr = 0;
		Integer iMapId = 1;
		for (int i = 0; i < iNumOfQTLDataRowsFromDataTable; i++){			
			HashMap<String, String> hashMapOfDataRowFromDataTable = listOfDataRowsFromDataTable.get(i);			
			if((!markersList.contains(hashMapOfDataRowFromDataTable.get(UploadField.Marker.toString())))&&(hashMapOfDataRowFromDataTable.get(UploadField.Marker.toString())!=null)){
				markersList.add(hashMapOfDataRowFromDataTable.get(UploadField.Marker.toString()));				
			}
			
			
			listOfMarkerIds = new ArrayList<Integer>();
			try {
				List<Marker> listOfMarkerIdsFromLocal =genoManager.getMarkersByMarkerNames(markersList, 0, markersList.size(), Database.LOCAL);
				
				List<Marker> listOfMarkerIDsFromCentral = genoManager.getMarkersByMarkerNames(markersList, 0, markersList.size(), Database.CENTRAL);
				if(listOfMarkerIDsFromCentral!=null){
					for (Marker iMarkerID : listOfMarkerIDsFromCentral){
						if (false == listOfMarkerIds.contains(iMarkerID)){
							listOfMarkerIds.add(iMarkerID.getMarkerId());
							hmOfSelectedMNamesandMID.put(iMarkerID.getMarkerName(), iMarkerID.getMarkerId());
						}
					}
				}
				if(listOfMarkerIdsFromLocal!=null){
					for (Marker iMarkerID : listOfMarkerIdsFromLocal){
						if (false == listOfMarkerIds.contains(iMarkerID)){
							listOfMarkerIds.add(iMarkerID.getMarkerId());
							hmOfSelectedMNamesandMID.put(iMarkerID.getMarkerName(), iMarkerID.getMarkerId());
						}
					}
				}
				/*List<Integer> listOfMarkerIdsFromLocal =genoManager.getMarkerIdsByMarkerNames(markersList, 0, markersList.size(), Database.LOCAL);
				
				List<Integer> listOfMarkerIDsFromCentral = genoManager.getMarkerIdsByMarkerNames(markersList, 0, markersList.size(), Database.CENTRAL);
				if(listOfMarkerIDsFromCentral!=null){
					for (Integer iMarkerID : listOfMarkerIDsFromCentral){
						if (false == listOfMarkerIds.contains(listOfMarkerIDsFromCentral.get(iMarkerID))){
							listOfMarkerIds.add(listOfMarkerIDsFromCentral.get(iMarkerID));
							//hmOfSelectedMNamesandMID.put(iMarkerID.getMarkerName(), iMarkerID.getMarkerId());
						}
					}
				}
				if(listOfMarkerIdsFromLocal!=null){
					for (Integer iMarkerID : listOfMarkerIdsFromLocal){
						if (false == listOfMarkerIds.contains(listOfMarkerIDsFromCentral.get(iMarkerID))){
							listOfMarkerIds.add(listOfMarkerIDsFromCentral.get(iMarkerID));
							//hmOfSelectedMNamesandMID.put(iMarkerID.getMarkerName(), iMarkerID.getMarkerId());
						}
					}
				}*/
				
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Marker-IDs for the Markers provided", Notification.TYPE_ERROR_MESSAGE);
				//return;
				throw new GDMSException("Error Retrieving Marker-IDs for the Markers provided");
			}	
			
			/*try{
			
				MarkerDAO markerDAOForLocal = new MarkerDAO();
				markerDAOForLocal.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForLocal().getSession());
				MarkerDAO markerDAOForCentral = new MarkerDAO();
				markerDAOForCentral.setSession(GDMSModel.getGDMSModel().getManagerFactory().getSessionProviderForCentral().getSession());
		
				long countAllLocal = markerDAOForLocal.countAll();
				List<Integer> listOfMIDsByMNamesLocal = markerDAOForLocal.getIdsByNames(markersList, 0, (int)countAllLocal);
		
				long countAllCentral = markerDAOForCentral.countAll();
				List<Integer> listOfMIDsByMNamesCentral = markerDAOForCentral.getIdsByNames(markersList, 0, (int)countAllCentral);
				
					
				hmOfSelectedMNamesandMID = new HashMap<String, Integer>();
				long countMarkersByIds = markerDAOForLocal.countMarkersByIds(listOfMarkerIds);
				List<Marker> listOfMarkersByIdsLocal = markerDAOForLocal.getMarkersByIds(listOfMarkerIds, 0, (int)countMarkersByIds);
				long countMarkersByIds2 = markerDAOForCentral.countMarkersByIds(listOfMarkerIds);
				List<Marker> listOfMarkersByCentral = markerDAOForCentral.getMarkersByIds(listOfMarkerIds, 0, (int)countMarkersByIds2);
		
				if (null != listOfMarkersByIdsLocal){
					for (Marker marker : listOfMarkersByIdsLocal){
						Integer markerId = marker.getMarkerId();
						String markerName = marker.getMarkerName();
						if (false == hmOfSelectedMNamesandMID.containsKey(markerName)){
							hmOfSelectedMNamesandMID.put(markerName, markerId);
						}
					}
				}
				if (null != listOfMarkersByCentral){
					for (Marker marker : listOfMarkersByCentral){
						Integer markerId = marker.getMarkerId();
						String markerName = marker.getMarkerName();
						if (false == hmOfSelectedMNamesandMID.containsKey(markerName)){
							hmOfSelectedMNamesandMID.put(markerName, markerId);
						}
					}
				}
			} catch (MiddlewareQueryException e) {
				//_mainHomePage.getMainWindow().getWindow().showNotification("Error Retrieving Marker-IDs for the Markers provided", Notification.TYPE_ERROR_MESSAGE);
				//return;
				throw new GDMSException("Error Retrieving Marker-IDs for the Markers provided");
			}*/	
		}
		
		int intMaxVal=0;
	/*	Object obj=null;
		Iterator itList=null;
		List listValues=null;
		Query query=localSession.createSQLQuery("select min(mta_id) from gdms_mta");
		
		listValues=query.list();
		itList=listValues.iterator();
					
		while(itList.hasNext()){
			obj=itList.next();
			if(obj!=null)
				intMaxVal=Integer.parseInt(obj.toString());
		}*/
		long lastIdMPId=0;
		try{
			lastIdMPId = genoManager.getLastId(Database.LOCAL, GdmsTable.GDMS_MTA);
		
		}catch (MiddlewareQueryException e) {
			throw new GDMSException(e.getMessage());
		}
		/*int maxCHid=(int)lastIdMPId-1;
		iACId=maxCHid;*/
		
		iMTAId=(int)lastIdMPId-1;
		
		
		
		dataset = new DatasetBean();
		dataset.setDataset_id(iDatasetId);
		dataset.setDataset_name(strDatasetName);
		dataset.setDataset_desc(strDatasetDesc);
		dataset.setDataset_type(strDatasetType);
		dataset.setGenus(strGenus);
		dataset.setSpecies(strSpecies);
		dataset.setUpload_template_date(curDate);
		dataset.setRemarks(strRemarks);
		dataset.setDatatype(strDataType);
		//dataset.setMissing_data(strMissingData);
		dataset.setMethod(strMethod);
		dataset.setScore(strScore);
		dataset.setInstitute(strInstitute);
		dataset.setPrincipal_investigator(strPrincipleInvestigator);
		dataset.setEmail(strEmail);
		dataset.setPurpose_of_study(strPurposeOfStudy);
		localSession.save(dataset);
		
		//System.out.println(iDatasetId+","+ iUserId);
		//datasetUser = new DatasetUsers(iDatasetId, iUserId);
		
		datasetUser = new GenotypeUsersBean();
		datasetUser.setDataset_id(iDatasetId);
		datasetUser.setUser_id(iUserId);
		localSession.save(datasetUser);
		
		
		mtaMetadata=new MTAmetadataBean();
		//mtaMetadata.setMtaId(iMTAId);
		mtaMetadata.setDatasetID(iDatasetId);
		mtaMetadata.setProject(strProject);
		mtaMetadata.setPopulation(strPopulation);
		mtaMetadata.setPopulationSize(strIntPopSize);
		mtaMetadata.setPopulationUnits(strPopUnits);
		localSession.save(mtaMetadata);
		
		
		
		for (int i = 0; i < iNumOfQTLDataRowsFromDataTable; i++){
			
			HashMap<String, String> hashMapOfDataRowFromDataTable = listOfDataRowsFromDataTable.get(i);			
			//First checking for the Map Name provided exists in the Database (gdms_map table) or not
			String strMapNameFromTemplate = hashMapOfDataRowFromDataTable.get(UploadField.MapName.toString());
		
			boolean bMapExists = false;
			//System.out.println("Marker=:"+hashMapOfDataRowFromDataTable.get(UploadField.Marker.toString()));
			
			String strMarker=hashMapOfDataRowFromDataTable.get(UploadField.Marker.toString());
			try {
				mapId = genoManager.getMapIdByName(strMapNameFromTemplate);
			} catch (MiddlewareQueryException e) {
				throw new GDMSException(e.getMessage());
			}
				//System.out.println("map id=:"+mapId);
			if(mapId!=0){
				iMapId =mapId;
				bMapExists = true;
				//break;
			}
			
			if (false == bMapExists){
				String strErrMsg = "Map does not exists.\nPlease Upload the corresponding Map";
				throw new GDMSException(strErrMsg);
			}
			
			 //Will be set/overridden by the function, testSetQTL() of TestGenotypicDataManagerImpl class
			
			String strGene = hashMapOfDataRowFromDataTable.get(UploadField.Gene.toString());
			String strLinkageGroup = hashMapOfDataRowFromDataTable.get(UploadField.Chromosome.toString());
			
			String strPosition = hashMapOfDataRowFromDataTable.get(UploadField.Position.toString());
			Float fPosition = new Float(0.0);
			if (null != strPosition){
				fPosition = Float.parseFloat(strPosition);
			}
			
			
			
			String strTrait = hashMapOfDataRowFromDataTable.get(UploadField.TraitID.toString());
			Integer traitId = Integer.parseInt(mapTraits.get(strTrait).toString());
			
			if(hashMapOfDataRowFromDataTable.get(UploadField.Effect.toString())!=null)
				//strEffect = Integer.parseInt(hashMapOfDataRowFromDataTable.get(UploadField.Effect.toString()));
				strEffect = Float.parseFloat(hashMapOfDataRowFromDataTable.get(UploadField.Effect.toString()));
			else
				strEffect=0f;
						
			String strLOD = hashMapOfDataRowFromDataTable.get(UploadField.ScoreValue.toString());
			Float fScoreValue = 0f;
			if (false == strLOD.equals("")){
				fScoreValue = Float.parseFloat(strLOD);
			}	
			
			String strR2 = hashMapOfDataRowFromDataTable.get(UploadField.R2.toString());
			Float fRSquare = 0f;
			if (false == strR2.equals("")){
				fRSquare = Float.parseFloat(strR2);
			}
			String strAlleleA = hashMapOfDataRowFromDataTable.get(UploadField.Allele_A.toString());			
			String strAlleleB = hashMapOfDataRowFromDataTable.get(UploadField.Allele_B.toString());
			String strAlleleAPhenotype = hashMapOfDataRowFromDataTable.get(UploadField.Allele_A_Phenotype.toString());
			String strAlleleBPhenotype = hashMapOfDataRowFromDataTable.get(UploadField.Allele_B_Phenotype.toString());
			String strFreqAlleleA = hashMapOfDataRowFromDataTable.get(UploadField.Freq_Allele_A.toString());	
			Float fFreqAlleleA = 0f;
			if (false == strFreqAlleleA.equals("")){
				fFreqAlleleA = Float.parseFloat(strFreqAlleleA);
			}
			String strFreqAlleleB = hashMapOfDataRowFromDataTable.get(UploadField.Freq_Allele_B.toString());
			Float fFreqAlleleB = 0f;
			if (false == strFreqAlleleB.equals("")){
				fFreqAlleleB = Float.parseFloat(strFreqAlleleB);
			}
			String strPValueUnCorrected = hashMapOfDataRowFromDataTable.get(UploadField.Pvalue_Uncorrected.toString());
			Float fPValueUnCorrected = 0f;
			if (false == strPValueUnCorrected.equals("")){
				fPValueUnCorrected = Float.parseFloat(strPValueUnCorrected);
			}
			String strPValueCorrected = hashMapOfDataRowFromDataTable.get(UploadField.Pvalue_corrected.toString());
			Float fPValueCorrected = 0f;
			if (false == strPValueCorrected.equals("")){
				fPValueCorrected = Float.parseFloat(strPValueCorrected);
			}
			
			String strCorrectionMethod = hashMapOfDataRowFromDataTable.get(UploadField.CorrectionMethod.toString());			
			String strTraitAvgAlleleA = hashMapOfDataRowFromDataTable.get(UploadField.TraitAverageForAlleleA.toString());
			Float fTraitAvgAlleleA = 0f;
			if (false == strTraitAvgAlleleA.equals("")){
				fTraitAvgAlleleA = Float.parseFloat(strTraitAvgAlleleA);
			}
			
			String strTraitAvgAlleleB = hashMapOfDataRowFromDataTable.get(UploadField.TraitAverageForAlleleB.toString());
			Float fTraitAvgAlleleB = 0f;
			if (false == strTraitAvgAlleleB.equals("")){
				fTraitAvgAlleleB = Float.parseFloat(strTraitAvgAlleleB);
			}
			String strDominance = hashMapOfDataRowFromDataTable.get(UploadField.Dominance.toString());
			String strEvidence =hashMapOfDataRowFromDataTable.get(UploadField.Evidence.toString());
			String strReference = hashMapOfDataRowFromDataTable.get(UploadField.Reference.toString());
			String strNotes =hashMapOfDataRowFromDataTable.get(UploadField.Notes.toString());
			
			
			mta=new MTABean();
			mta.setMta_id(iMTAId);
			mta.setMarker_id(hmOfSelectedMNamesandMID.get(strMarker));
			mta.setDataset_id(iDatasetId);
			mta.setMap_id(iMapId);
			mta.setChromosome(strLinkageGroup);
			mta.setPosition(fPosition);
			mta.setTid(traitId);
			mta.setEffect(strEffect);			
			mta.setScoreValue(fScoreValue);
			mta.setrSquare(fRSquare);
			mta.setGene(strGene);
			mta.setAlleleA(strAlleleA);
			mta.setAlleleB(strAlleleB);
			mta.setAlleleAPhenotype(strAlleleAPhenotype);
			mta.setAlleleBPhenotype(strAlleleBPhenotype);
			mta.setFreqAlleleA(fFreqAlleleA);
			mta.setFreqAlleleB(fFreqAlleleB);
			mta.setpValueUnCorrected(fPValueUnCorrected);
			mta.setpValueCorrected(fPValueCorrected);
			mta.setCorrectionMethod(strCorrectionMethod);
			mta.setAlleleATraitAvg(fTraitAvgAlleleA);
			mta.setAlleleBTraitAvg(fTraitAvgAlleleB);
			mta.setDominance(strDominance);
			mta.setEvidence(strEvidence);
			mta.setReference(strReference);
			mta.setNotes(strNotes);
			localSession.save(mta);
			
			
			
			iMTAId--;
			if (i % 1 == 0){
				localSession.flush();
				localSession.clear();
			}
			//saveMTA();
			
			
			arrayOfMTAs[iUploadedQTLCtr++] = mta;
			
		}

		
		
		tx.commit();
	}
	
	/*protected void saveMTA() throws GDMSException {
		try{
			genoManager.addMTA(dataset, mta, datasetUser);
		}catch (MiddlewareQueryException e) {
			throw new GDMSException("Error uploading Map");
		}  catch (Throwable th){
			throw new GDMSException("Error uploading QTL", th);
		}
	}*/

	@Override
	public void setListOfColumns(ArrayList<FieldProperties> theListOfColumnsInTheTable) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDataUploaded() {
		String strDataUploaded = "";
		if (null != arrayOfMTAs && arrayOfMTAs.length > 0){
			String strUploadInfo = "";

			/*for (int i = 0; i < arrayOfQTLs.length; i++){
				Integer iMTAId = arrayOfQTLs[i].getQtlId();
				String strQTLName = arrayOfQTLs[i].getQtlName();
				String strQTL = "QTL: " + iMTAId + ": " + strQTLName;
				strUploadInfo += strQTL + "\n";
			}*/

			strDataUploaded = "Uploaded MTA(s): \n";
		}	
		return strDataUploaded;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromSourceSheet() {
		return listOfDataInSourceSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromDataSheet() {
		return listOfDataInDataSheet;
	}

	@Override
	public ArrayList<HashMap<String, String>> getDataFromAdditionalGIDsSheet() {
		// TODO Auto-generated method stub
		return null;
	}

}

