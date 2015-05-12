package org.icrisat.gdms.upload.genotyping;

public class DArTDetailsBean {
	private int dataset_id;
	private int ad_id;
	private long marker_id;
	private long clone_id;
	private float qvalue;
	
	private float reproducibility;
	private float call_rate;
	private float pic_value;
	private float discordance;
	public int getDataset_id() {
		return dataset_id;
	}
	public void setDataset_id(int dataset_id) {
		this.dataset_id = dataset_id;
	}
	public long getMarker_id() {
		return marker_id;
	}
	public void setMarker_id(long marker_id) {
		this.marker_id = marker_id;
	}
	public long getClone_id() {
		return clone_id;
	}
	public void setClone_id(long clone_id) {
		this.clone_id = clone_id;
	}
	
	public float getQvalue() {
		return qvalue;
	}
	public void setQvalue(float qvalue) {
		this.qvalue = qvalue;
	}
	public float getReproducibility() {
		return reproducibility;
	}
	public void setReproducibility(float reproducibility) {
		this.reproducibility = reproducibility;
	}
	public float getCall_rate() {
		return call_rate;
	}
	public void setCall_rate(float call_rate) {
		this.call_rate = call_rate;
	}
	
	public float getPic_value() {
		return pic_value;
	}
	public void setPic_value(float pic_value) {
		this.pic_value = pic_value;
	}
	public float getDiscordance() {
		return discordance;
	}
	public void setDiscordance(float discordance) {
		this.discordance = discordance;
	}
	public int getAd_id() {
		return ad_id;
	}
	public void setAd_id(int ad_id) {
		this.ad_id = ad_id;
	}
	

}
