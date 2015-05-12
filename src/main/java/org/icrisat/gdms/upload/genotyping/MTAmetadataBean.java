package org.icrisat.gdms.upload.genotyping;

public class MTAmetadataBean {
	
	private int datasetID;
	private String project;
	private String population;
	private int populationSize;
	private String populationUnits;
	
	
	
	/*public int getMtaId() {
		return mtaId;
	}
	public void setMtaId(int mtaId) {
		this.mtaId = mtaId;
	}*/
	
	
	
	
	public String getProject() {
		return project;
	}
	public int getDatasetID() {
		return datasetID;
	}
	public void setDatasetID(int datasetID) {
		this.datasetID = datasetID;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public String getPopulation() {
		return population;
	}
	public void setPopulation(String population) {
		this.population = population;
	}
	public int getPopulationSize() {
		return populationSize;
	}
	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}
	public String getPopulationUnits() {
		return populationUnits;
	}
	public void setPopulationUnits(String populationUnits) {
		this.populationUnits = populationUnits;
	}
	
	
	
	

}
