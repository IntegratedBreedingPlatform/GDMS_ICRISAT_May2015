package org.icrisat.gdms.upload.genotyping;

import java.io.Serializable;

public class CharArrayCompositeKey implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	private int dataset_id;	
	//private int dataorder_index;
	private int ac_id;
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
	public int getAc_id() {
		return ac_id;
	}
	public void setAc_id(int ac_id) {
		this.ac_id = ac_id;
	}
	
	
}
