package org.icrisat.gdms.upload.genotyping;

public class QTLDetailsBean {
	private int map_id;
	private int qtl_id;
	private String linkage_group;
	private float position;
	private float min_position;
	private float max_position;
	private int tid;
	private String experiment;
	private String left_flanking_marker;
	private String right_flanking_marker;
	private float effect;
	//private float lod;
	private float score_value;
	private float clen;
	private float r_square;
	private String interactions;
	
	private String se_additive;
	private String hv_parent;
	
	
	private String hv_allele;
	private String lv_parent;
	private String lv_allele;
	
	public int getQtl_id() {
		return qtl_id;
	}
	public void setQtl_id(int qtl_id) {
		this.qtl_id = qtl_id;
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
	public float getMin_position() {
		return min_position;
	}
	public void setMin_position(float min_position) {
		this.min_position = min_position;
	}
	public float getMax_position() {
		return max_position;
	}
	public void setMax_position(float max_position) {
		this.max_position = max_position;
	}
	/*public String getTrait() {
		return trait;
	}
	public void setTrait(String trait) {
		this.trait = trait;
	}*/
	public String getExperiment() {
		return experiment;
	}
	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}
	public String getLeft_flanking_marker() {
		return left_flanking_marker;
	}
	public void setLeft_flanking_marker(String left_flanking_marker) {
		this.left_flanking_marker = left_flanking_marker;
	}
	public String getRight_flanking_marker() {
		return right_flanking_marker;
	}
	public void setRight_flanking_marker(String right_flanking_marker) {
		this.right_flanking_marker = right_flanking_marker;
	}
	
	public float getEffect() {
		return effect;
	}
	public void setEffect(float effect) {
		this.effect = effect;
	}
	/*public float getLod() {
		return lod;
	}
	public void setLod(float lod) {
		this.lod = lod;
	}*/
	
	public float getR_square() {
		return r_square;
	}
	public void setR_square(float r_square) {
		this.r_square = r_square;
	}
	public String getInteractions() {
		return interactions;
	}
	public void setInteractions(String interactions) {
		this.interactions = interactions;
	}
	public float getPosition() {
		return position;
	}
	public void setPosition(float position) {
		this.position = position;
	}
	public float getScore_value() {
		return score_value;
	}
	public void setScore_value(float score_value) {
		this.score_value = score_value;
	}
	public float getClen() {
		return clen;
	}
	public void setClen(float clen) {
		this.clen = clen;
	}
	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}
	public String getSe_additive() {
		return se_additive;
	}
	public void setSe_additive(String se_additive) {
		this.se_additive = se_additive;
	}
	public String getHv_parent() {
		return hv_parent;
	}
	public void setHv_parent(String hv_parent) {
		this.hv_parent = hv_parent;
	}
	public String getHv_allele() {
		return hv_allele;
	}
	public void setHv_allele(String hv_allele) {
		this.hv_allele = hv_allele;
	}
	public String getLv_parent() {
		return lv_parent;
	}
	public void setLv_parent(String lv_parent) {
		this.lv_parent = lv_parent;
	}
	public String getLv_allele() {
		return lv_allele;
	}
	public void setLv_allele(String lv_allele) {
		this.lv_allele = lv_allele;
	}
	
	
}
