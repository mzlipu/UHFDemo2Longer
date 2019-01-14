package com.uhfdemo2longer;

import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AllAssetsByLocationId extends AsyncTask<Void, Void, Void> {
    Connection conn = null;
    List<Asset> assets;
    String currentAssetID;

    public AllAssetsByLocationId(Connection connection , String assetID) {
        conn = connection;
        currentAssetID = assetID;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (conn==null){
            System.out.println("con not found");
        }
        else {
            final String sqlAsset = "select bt_asset.name as name, bt_asset.rfid_tags as rfid_tags,\n" +
                    "bt_asset_location.name as loc_name from bt_asset , bt_asset_location where\n" +
                    "bt_asset.current_loc_id = bt_asset_location.id And bt_asset_location.id = "+currentAssetID;

            assets= new ArrayList<Asset>();

            try  {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlAsset);

                // loop through the result set
                while (rs.next()) {
                    Asset asset= new Asset();
                    asset.setAssetName(rs.getString("name"));
                    asset.setRfidTags(rs.getString("rfid_tags"));
                    asset.setLocation(rs.getString("loc_name"));
                    System.out.println(asset);
                    assets.add(asset);
                }
            } catch (final SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    public List<Asset> selectAllAssetsByLocationId(){
        if (conn!=null){
            return assets;
        }
        else
            assets= new ArrayList<Asset>();
        return assets;
    }
}
