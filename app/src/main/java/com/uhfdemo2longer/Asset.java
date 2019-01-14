package com.uhfdemo2longer;

import java.io.Serializable;


public class Asset implements Serializable {
    private int assetID;
    private String assetName;
    private String rfidTags;
    private String assetModle;
    private String assetType;
    private String epc;
    private String location;
    private String createTime;
    private int status;

    public Asset() {
    }

    public Asset(int assetID, String assetName, String rfidTags, String assetModle, String assetType, String epc, String location, String createTime, int status) {
        this.assetID = assetID;
        this.assetName = assetName;
        this.rfidTags = rfidTags;
        this.assetModle = assetModle;
        this.assetType = assetType;
        this.epc = epc;
        this.location = location;
        this.createTime = createTime;
        this.status = status;
    }

    public int getAssetID() {
        return assetID;
    }

    public void setAssetID(int assetID) {
        this.assetID = assetID;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public void setAssetModle(String assetModle) {
        this.assetModle = assetModle;
    }

    public void setAssetType(String assetType) {
        this.assetType= assetType;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public String getAssetName() {
        return assetName;
    }

    public String getAssetModle() {
        return assetModle;
    }

    public String getAssetType() {
        return assetType;
    }

    public String getEpc() {
        return epc;
    }

    public String getLocation() {
        return location;
    }

    public String getCreateTime() {
        return createTime;
    }

    public int getStatus() {
        return status;
    }

    public String getRfidTags() {
        return rfidTags;
    }

    public void setRfidTags(String rfidTags) {
        this.rfidTags = rfidTags;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "assetID='" + assetID + '\'' +
                ", assetName='" + assetName + '\'' +
                ", rfidTags='" + rfidTags + '\'' +
                ", assetModle='" + assetModle + '\'' +
                ", assetType='" + assetType + '\'' +
                ", epc='" + epc + '\'' +
                ", location='" + location + '\'' +
                ", createTime='" + createTime + '\'' +
                ", status=" + status +
                '}';
    }
}
