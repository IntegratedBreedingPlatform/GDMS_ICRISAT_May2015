/**
 * 
 */
package org.icrisat.gdms.upload.genotyping;


public class CharArrayBean {
	
	private String char_value;
	private CharArrayCompositeKey comKey;
	private int gid;
	private long marker_id;
	//private String germplasm_name;
	
	private int markerSampleId;
	private int accSampleID;
	
	
	
	public int getMarkerSampleId() {
		return markerSampleId;
	}
	public void setMarkerSampleId(int markerSampleId) {
		this.markerSampleId = markerSampleId;
	}
	public int getAccSampleID() {
		return accSampleID;
	}
	public void setAccSampleID(int accSampleID) {
		this.accSampleID = accSampleID;
	}
	/**
	 * @return the char_value
	 */
	public String getChar_value() {
		return char_value;
	}
	/**
	 * @param char_value the char_value to set
	 */
	public void setChar_value(String char_value) {
		this.char_value = char_value;
	}
	/**
	 * @return the comKey
	 */
	public CharArrayCompositeKey getComKey() {
		return comKey;
	}
	/**
	 * @param comKey the comKey to set
	 */
	public void setComKey(CharArrayCompositeKey comKey) {
		this.comKey = comKey;
	}
	/**
	 * @return the gid
	 */
	public int getGid() {
		return gid;
	}
	/**
	 * @param gid the gid to set
	 */
	public void setGid(int gid) {
		this.gid = gid;
	}
	/**
	 * @return the marker_id
	 */
	public long getMarker_id() {
		return marker_id;
	}
	/**
	 * @param marker_id the marker_id to set
	 */
	public void setMarker_id(long marker_id) {
		this.marker_id = marker_id;
	}
	/**
	 * @return the germplasm_name
	 */
	/*public String getGermplasm_name() {
		return germplasm_name;
	}
	*//**
	 * @param germplasm_name the germplasm_name to set
	 *//*
	public void setGermplasm_name(String germplasm_name) {
		this.germplasm_name = germplasm_name;
	}*/
	

}
