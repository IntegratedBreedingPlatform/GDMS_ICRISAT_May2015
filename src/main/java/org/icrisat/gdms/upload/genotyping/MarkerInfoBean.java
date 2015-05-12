/**
 * 
 */
package org.icrisat.gdms.upload.genotyping;

public class MarkerInfoBean {
	private int markerId;
	private String marker_type;
	private String marker_name;
	/*private String crop;
	private String accession_id;*/
	private String species;
	private String db_accession_id;
	private String reference;
	private String genotype;
	private String ploidy;
	private String remarks;
	private String primer_id;
	
	private String assay_type;
	private String motif;
	private String forward_primer;
	private String reverse_primer;
	private String product_size;
	private double annealing_temp;
	private String amplification;
	
	
	
	public int getMarkerId() {
		return markerId;
	}
	public void setMarkerId(int markerId) {
		this.markerId = markerId;
	}
	public String getMarker_type() {
		return marker_type;
	}
	public void setMarker_type(String marker_type) {
		this.marker_type = marker_type;
	}
	public String getMarker_name() {
		return marker_name;
	}
	public void setMarker_name(String marker_name) {
		this.marker_name = marker_name;
	}
		
	
	public String getReference() {
		return reference;
	}
	public String getSpecies() {
		return species;
	}
	public void setSpecies(String species) {
		this.species = species;
	}
	public String getDb_accession_id() {
		return db_accession_id;
	}
	public void setDb_accession_id(String db_accession_id) {
		this.db_accession_id = db_accession_id;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public String getGenotype() {
		return genotype;
	}
	public void setGenotype(String genotype) {
		this.genotype = genotype;
	}
	public String getPloidy() {
		return ploidy;
	}
	public void setPloidy(String ploidy) {
		this.ploidy = ploidy;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getPrimer_id() {
		return primer_id;
	}
	public void setPrimer_id(String primer_id) {
		this.primer_id = primer_id;
	}
	public String getAssay_type() {
		return assay_type;
	}
	public void setAssay_type(String assay_type) {
		this.assay_type = assay_type;
	}
	public String getMotif() {
		return motif;
	}
	public void setMotif(String motif) {
		this.motif = motif;
	}
	public String getForward_primer() {
		return forward_primer;
	}
	public void setForward_primer(String forward_primer) {
		this.forward_primer = forward_primer;
	}
	public String getReverse_primer() {
		return reverse_primer;
	}
	public void setReverse_primer(String reverse_primer) {
		this.reverse_primer = reverse_primer;
	}
	public String getProduct_size() {
		return product_size;
	}
	public void setProduct_size(String product_size) {
		this.product_size = product_size;
	}
	
	public double getAnnealing_temp() {
		return annealing_temp;
	}
	public void setAnnealing_temp(double annealing_temp) {
		this.annealing_temp = annealing_temp;
	}
	public String getAmplification() {
		return amplification;
	}
	public void setAmplification(String amplification) {
		this.amplification = amplification;
	}
	
	

}
