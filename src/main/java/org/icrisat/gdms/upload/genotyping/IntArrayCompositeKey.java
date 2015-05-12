package org.icrisat.gdms.upload.genotyping;

import java.io.Serializable;

public class IntArrayCompositeKey implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	private int dataset_id;	
	//private int dataorder_index;
	private int an_id;
	public int getDataset_id() {
		return dataset_id;
	}
	public void setDataset_id(int dataset_id) {
		this.dataset_id = dataset_id;
	}
	/*public int getDataorder_index() {
		return dataorder_index;
	}
	public void setDataorder_index(int dataorder_index) {
		this.dataorder_index = dataorder_index;
	}*/
	
	
	
	
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public int getAn_id() {
		return an_id;
	}
	public void setAn_id(int an_id) {
		this.an_id = an_id;
	}
	

}
