package org.icrisat.gdms.upload.genotyping;

public class MappingPopCharValuesBean {
	/*private int mp_id;
	private int dataset_id;*/
	private int gid;
	private long marker_id;
	private String map_char_value;
	
	private int markerSampleId;
	private int accSampleID;
	
	/*public int getMp_id() {
		return mp_id;
	}
	public void setMp_id(int mp_id) {
		this.mp_id = mp_id;
	}
	public int getDataset_id() {
		return dataset_id;
	}
	public void setDataset_id(int dataset_id) {
		this.dataset_id = dataset_id;
	}*/
	private MapCharArrayCompositeKey mapComKey;
	
	
	public MapCharArrayCompositeKey getMapComKey() {
		return mapComKey;
	}
	public void setMapComKey(MapCharArrayCompositeKey mapComKey) {
		this.mapComKey = mapComKey;
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
	/*public String getChar_value() {
		return char_value;
	}
	public void setChar_value(String char_value) {
		this.char_value = char_value;
	}*/
	public String getMap_char_value() {
		return map_char_value;
	}
	public void setMap_char_value(String map_char_value) {
		this.map_char_value = map_char_value;
	}
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
	
	
	
	

}
