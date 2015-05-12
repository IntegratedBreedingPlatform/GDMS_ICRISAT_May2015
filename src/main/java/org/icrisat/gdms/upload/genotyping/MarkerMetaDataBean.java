/**
 * 
 */
package org.icrisat.gdms.upload.genotyping;


public class MarkerMetaDataBean {
	
	private int markerMetadatasetId;
	private int datasetId;
	private int markerId;
	private int markerSampleId;
	
	
	
	public int getMarkerMetadatasetId() {
		return markerMetadatasetId;
	}
	public void setMarkerMetadatasetId(int markerMetadatasetId) {
		this.markerMetadatasetId = markerMetadatasetId;
	}
	public int getMarkerSampleId() {
		return markerSampleId;
	}
	public void setMarkerSampleId(int markerSampleId) {
		this.markerSampleId = markerSampleId;
	}
	public int getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(int datasetId) {
		this.datasetId = datasetId;
	}
	public int getMarkerId() {
		return markerId;
	}
	public void setMarkerId(int markerId) {
		this.markerId = markerId;
	}
	

}
