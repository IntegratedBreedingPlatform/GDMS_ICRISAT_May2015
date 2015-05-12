package org.icrisat.gdms.upload.genotyping;

public class IntArrayBean {
	//private String char_value;
	private IntArrayCompositeKey comKey;
	private int gid;
	private long marker_id;
	private String allele_bin_value;
	private String allele_raw_value;
	private int accSampleID;
	private int markerSampleId;
	
	
	public IntArrayCompositeKey getComKey() {
		return comKey;
	}
	public void setComKey(IntArrayCompositeKey comKey) {
		this.comKey = comKey;
	}
	public int getGid() {
		return gid;
	}
	public void setGid(int gid) {
		this.gid = gid;
	}
	public long getMarker_id() {
		return marker_id;
	}
	public void setMarker_id(long marker_id) {
		this.marker_id = marker_id;
	}
	public String getAllele_bin_value() {
		return allele_bin_value;
	}
	public void setAllele_bin_value(String allele_bin_value) {
		this.allele_bin_value = allele_bin_value;
	}
	public String getAllele_raw_value() {
		return allele_raw_value;
	}
	public void setAllele_raw_value(String allele_raw_value) {
		this.allele_raw_value = allele_raw_value;
	}
	public int getAccSampleID() {
		return accSampleID;
	}
	public void setAccSampleID(int accSampleID) {
		this.accSampleID = accSampleID;
	}
	public int getMarkerSampleId() {
		return markerSampleId;
	}
	public void setMarkerSampleId(int markerSampleId) {
		this.markerSampleId = markerSampleId;
	}
	

}
