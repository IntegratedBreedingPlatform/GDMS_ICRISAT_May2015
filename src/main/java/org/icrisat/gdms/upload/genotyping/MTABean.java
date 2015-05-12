package org.icrisat.gdms.upload.genotyping;

public class MTABean {
	
	private int mta_id;
	private int marker_id;
	private int dataset_id;
	private int map_id;
	
	private float position;
	private int tid;
	private float effect;
	private float scoreValue;
	private float rSquare;
	private String gene;
	private String chromosome;
	private String alleleA;
	private String alleleB;
	private String alleleAPhenotype;
	private String alleleBPhenotype;
	private float freqAlleleA;
	private float freqAlleleB;
	private float pValueUnCorrected;
	private float pValueCorrected;
	private String correctionMethod;
	private float alleleATraitAvg;
	private float alleleBTraitAvg;
	private String dominance;
	private String evidence;
	private String reference;
	private String notes;
	public int getMta_id() {
		return mta_id;
	}
	public void setMta_id(int mta_id) {
		this.mta_id = mta_id;
	}
	public int getMarker_id() {
		return marker_id;
	}
	public void setMarker_id(int marker_id) {
		this.marker_id = marker_id;
	}
	public int getDataset_id() {
		return dataset_id;
	}
	public void setDataset_id(int dataset_id) {
		this.dataset_id = dataset_id;
	}
	public int getMap_id() {
		return map_id;
	}
	public void setMap_id(int map_id) {
		this.map_id = map_id;
	}
	public float getPosition() {
		return position;
	}
	public void setPosition(float position) {
		this.position = position;
	}
	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}
	public float getEffect() {
		return effect;
	}
	public void setEffect(float effect) {
		this.effect = effect;
	}
	public float getScoreValue() {
		return scoreValue;
	}
	public void setScoreValue(float scoreValue) {
		this.scoreValue = scoreValue;
	}
	public float getrSquare() {
		return rSquare;
	}
	public void setrSquare(float rSquare) {
		this.rSquare = rSquare;
	}
	public String getGene() {
		return gene;
	}
	public void setGene(String gene) {
		this.gene = gene;
	}
	public String getChromosome() {
		return chromosome;
	}
	public void setChromosome(String chromosome) {
		this.chromosome = chromosome;
	}
	public String getAlleleA() {
		return alleleA;
	}
	public void setAlleleA(String alleleA) {
		this.alleleA = alleleA;
	}
	public String getAlleleB() {
		return alleleB;
	}
	public void setAlleleB(String alleleB) {
		this.alleleB = alleleB;
	}
	public String getAlleleAPhenotype() {
		return alleleAPhenotype;
	}
	public void setAlleleAPhenotype(String alleleAPhenotype) {
		this.alleleAPhenotype = alleleAPhenotype;
	}
	public String getAlleleBPhenotype() {
		return alleleBPhenotype;
	}
	public void setAlleleBPhenotype(String alleleBPhenotype) {
		this.alleleBPhenotype = alleleBPhenotype;
	}
	public float getFreqAlleleA() {
		return freqAlleleA;
	}
	public void setFreqAlleleA(float freqAlleleA) {
		this.freqAlleleA = freqAlleleA;
	}
	public float getFreqAlleleB() {
		return freqAlleleB;
	}
	public void setFreqAlleleB(float freqAlleleB) {
		this.freqAlleleB = freqAlleleB;
	}
	public float getpValueUnCorrected() {
		return pValueUnCorrected;
	}
	public void setpValueUnCorrected(float pValueUnCorrected) {
		this.pValueUnCorrected = pValueUnCorrected;
	}
	public float getpValueCorrected() {
		return pValueCorrected;
	}
	public void setpValueCorrected(float pValueCorrected) {
		this.pValueCorrected = pValueCorrected;
	}
	public String getCorrectionMethod() {
		return correctionMethod;
	}
	public void setCorrectionMethod(String correctionMethod) {
		this.correctionMethod = correctionMethod;
	}
	public float getAlleleATraitAvg() {
		return alleleATraitAvg;
	}
	public void setAlleleATraitAvg(float alleleATraitAvg) {
		this.alleleATraitAvg = alleleATraitAvg;
	}
	public float getAlleleBTraitAvg() {
		return alleleBTraitAvg;
	}
	public void setAlleleBTraitAvg(float alleleBTraitAvg) {
		this.alleleBTraitAvg = alleleBTraitAvg;
	}
	public String getDominance() {
		return dominance;
	}
	public void setDominance(String dominance) {
		this.dominance = dominance;
	}
	public String getEvidence() {
		return evidence;
	}
	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	
	
	
	
	/*private String linkage_group;
	private float position;
	private int tid;
	private int effect;
	private String hv_allele;
	private String experiment;
	private float score_value;
	private float r_square;
	public int getMta_id() {
		return mta_id;
	}
	public void setMta_id(int mta_id) {
		this.mta_id = mta_id;
	}
	public int getMarker_id() {
		return marker_id;
	}
	public void setMarker_id(int marker_id) {
		this.marker_id = marker_id;
	}
	public int getDataset_id() {
		return dataset_id;
	}
	public void setDataset_id(int dataset_id) {
		this.dataset_id = dataset_id;
	}
	public int getMap_id() {
		return map_id;
	}
	public void setMap_id(int map_id) {
		this.map_id = map_id;
	}
	public String getLinkage_group() {
		return linkage_group;
	}
	public void setLinkage_group(String linkage_group) {
		this.linkage_group = linkage_group;
	}
	public float getPosition() {
		return position;
	}
	public void setPosition(float position) {
		this.position = position;
	}
	public String getTrait() {
		return trait;
	}
	public void setTrait(String trait) {
		this.trait = trait;
	}
	public int getEffect() {
		return effect;
	}
	public void setEffect(int effect) {
		this.effect = effect;
	}
	public String getHv_allele() {
		return hv_allele;
	}
	public void setHv_allele(String hv_allele) {
		this.hv_allele = hv_allele;
	}
	public String getExperiment() {
		return experiment;
	}
	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}
	public float getScore_value() {
		return score_value;
	}
	public void setScore_value(float score_value) {
		this.score_value = score_value;
	}
	public float getR_square() {
		return r_square;
	}
	public void setR_square(float r_square) {
		this.r_square = r_square;
	}
	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}*/
	
}
