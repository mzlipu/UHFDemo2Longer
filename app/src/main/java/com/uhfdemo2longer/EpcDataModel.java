package com.uhfdemo2longer;


public class EpcDataModel {
	
	private int id;
	private String rssi;
	private String epc;
	private int count;
	private String status;
	private int btAssetId;
	private String assetName;
	private String PreviousLocationId;
	private String PreviousLocation;
	private String CurrentLocation;

	public void setepcid(int epcid) {
		this.id = epcid;
	}

	public EpcDataModel(String rssi, String epc, int count) {
		super();
		this.rssi = rssi;
		this.epc = epc;
		this.count = count;
	}

	public EpcDataModel(int id, String rssi, String epc, int count, String status, int btAssetId, String assetName, String previousLocationId, String previousLocation, String currentLocation) {
		this.id = id;
		this.rssi = rssi;
		this.epc = epc;
		this.count = count;
		this.status = status;
		this.btAssetId = btAssetId;
		this.assetName = assetName;
		PreviousLocationId = previousLocationId;
		PreviousLocation = previousLocation;
		CurrentLocation = currentLocation;
	}

	public EpcDataModel() {
		super();
	}

	public int getepcid() {
		return id;
	}


	public String getrssi() {
		return rssi;
	}


	public void setrssi(String rssi) {
		this.rssi = rssi;
	}
	
	public String getepc() {
		return epc;
	}
	public void setepc(String epc) {
		this.epc = epc;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAssetName() {
		return assetName;
	}

	public String getPreviousLocation() {
		return PreviousLocation;
	}

	public void setPreviousLocation(String previousLocation) {
		PreviousLocation = previousLocation;
	}

	public String getCurrentLocation() {
		return CurrentLocation;
	}

	public void setCurrentLocation(String currentLocation) {
		CurrentLocation = currentLocation;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public int getBtAssetId() {
		return btAssetId;
	}

	public void setBtAssetId(int btAssetId) {
		this.btAssetId = btAssetId;
	}

	public String getPreviousLocationId() {
		return PreviousLocationId;
	}

	public void setPreviousLocationId(String previousLocationId) {
		PreviousLocationId = previousLocationId;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "rssi" + this.rssi + ",epc " + this.epc;
	}

}
