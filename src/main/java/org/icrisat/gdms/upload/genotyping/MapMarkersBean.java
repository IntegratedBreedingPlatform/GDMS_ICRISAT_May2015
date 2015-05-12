package org.icrisat.gdms.upload.genotyping;

public class MapMarkersBean {
	
	private int markeronmap_id;
	private int marker_id;
	private int map_id;
	private String linkage_group;
	private double start_position;
	private double end_position;
	/*private float start_position;
	private float end_position;*/
	
	
	//private String map_unit;
	
	
	public int getMarkerId() {
		return marker_id;
	}
	public int getMarkeronmap_id() {
		return markeronmap_id;
	}
	public void setMarkeronmap_id(int markeronmap_id) {
		this.markeronmap_id = markeronmap_id;
	}
	public void setMarkerId(int marker_id) {
		this.marker_id = marker_id;
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
	public double getStart_position() {
		return start_position;
	}
	public void setStart_position(double start_position) {
		this.start_position = start_position;
	}
	public double getEnd_position() {
		return end_position;
	}
	public void setEnd_position(double end_position) {
		this.end_position = end_position;
	}
		
	
	/*public String getMap_unit() {
		return map_unit;
	}
	public void setMap_unit(String map_unit) {
		this.map_unit = map_unit;
	}*/
	public int getMarker_id() {
		return marker_id;
	}
	public void setMarker_id(int marker_id) {
		this.marker_id = marker_id;
	}
	/*public float getStart_position() {
		return start_position;
	}
	public void setStart_position(float start_position) {
		this.start_position = start_position;
	}
	public float getEnd_position() {
		return end_position;
	}
	public void setEnd_position(float end_position) {
		this.end_position = end_position;
	}*/
		

}
