/**
 * 
 */
package org.icrisat.gdms.upload;

public class DatasetBean {
	
	private int dataset_id;
	private String dataset_name;
	private String dataset_desc;
	private String dataset_type;
	private String upload_template_date;
	
	private String genus;
	private String species;
	private String datatype;
	private String remarks;
	private String missing_data;
	private String method;
	private String score;
	
	private String institute;
	private String principal_investigator;
	private String email;
	private String purpose_of_study; 
	
	public int getDataset_id() {
		return dataset_id;
	}
	public void setDataset_id(int dataset_id) {
		this.dataset_id = dataset_id;
	}
	public String getDataset_desc() {
		return dataset_desc;
	}
	public void setDataset_desc(String dataset_desc) {
		this.dataset_desc = dataset_desc;
	}
	public String getDataset_type() {
		return dataset_type;
	}
	public void setDataset_type(String dataset_type) {
		this.dataset_type = dataset_type;
	}
	/*public String getTemplate_date() {
		return template_date;
	}
	public void setTemplate_date(String template_date) {
		this.template_date = template_date;
	}*/
	
	
	
	public String getGenus() {
		return genus;
	}
	public String getDataset_name() {
		return dataset_name;
	}
	public void setDataset_name(String dataset_name) {
		this.dataset_name = dataset_name;
	}
	public String getUpload_template_date() {
		return upload_template_date;
	}
	public void setUpload_template_date(String upload_template_date) {
		this.upload_template_date = upload_template_date;
	}
	public String getMissing_data() {
		return missing_data;
	}
	public void setMissing_data(String missing_data) {
		this.missing_data = missing_data;
	}
	
	public void setGenus(String genus) {
		this.genus = genus;
	}
	public String getSpecies() {
		return species;
	}
	public void setSpecies(String species) {
		this.species = species;
	}
	public String getDatatype() {
		return datatype;
	}
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getInstitute() {
		return institute;
	}
	public void setInstitute(String institute) {
		this.institute = institute;
	}
	public String getPrincipal_investigator() {
		return principal_investigator;
	}
	public void setPrincipal_investigator(String principal_investigator) {
		this.principal_investigator = principal_investigator;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPurpose_of_study() {
		return purpose_of_study;
	}
	public void setPurpose_of_study(String purpose_of_study) {
		this.purpose_of_study = purpose_of_study;
	}
	
	
	
	

}
