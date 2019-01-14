package com.uhfdemo2longer;

import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PgConnection extends AsyncTask<Void, Void, Void> {

/*    private final String url = "jdbc:postgresql://192.168.1.180/lipu";
    private final String user = "postgres";
    private final String password = "1234";*/   /*Uzzal Vai PC*/

/*    private final String url = "jdbc:postgresql://172.16.0.15/asset-mgmt";
    private final String user = "openpg";
    private final String password = "openpgpwd";*/  /*Client PC*/

/*    private final String url = "jdbc:postgresql://192.168.1.190/lipu";
    private final String user = "postgres";
    private final String password = "1234";*/     /*Lipu`s PC*/

    private final String url = "jdbc:postgresql://182.160.124.14/asset-mgmt";
    private final String user = "openpg";
    private final String password = "openpgpwd";  /*Client PC(Real IP)*/

    Connection conn = null;

    List<Asset> assets;
    List<Asset> listAssetByLocation;
    List<Location> locations;

    public PgConnection() {
        super();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (final SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("PostgreSQL server Not Connected.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("PostgreSQL server Not Connected......................ClassNotFoundException");
        }

        if (conn==null){
            System.out.println("con not found");
        }
        else {
            final String sqlAsset = "select bt_asset.id as id, bt_asset.name as name, bt_asset.rfid_tags as rfid_tags, bt_asset_location.name as loc_name \n" +
                    "from bt_asset\n" +
                    "LEFT JOIN bt_asset_location\n" +
                    "ON bt_asset.current_loc_id = bt_asset_location.id";

            assets= new ArrayList<Asset>();

            try  {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlAsset);

                // loop through the result set
                while (rs.next()) {
                    Asset asset= new Asset();
                    asset.setAssetID(rs.getInt("id"));
                    asset.setAssetName(rs.getString("name"));
                    asset.setRfidTags(rs.getString("rfid_tags"));
                    asset.setLocation(rs.getString("loc_name"));
                    assets.add(asset);
                }
            } catch (final SQLException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("LIST SIZE--------------------------------------------------------------------------"+assets.size());

            final String sqlLocation = "select * from bt_asset_location";
            locations= new ArrayList<Location>();
            try  {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlLocation);

                // loop through the result set
                while (rs.next()) {
                    Location location = new Location();
                    location.setLocationID(rs.getString("id"));
                    location.setLocationName(rs.getString("name"));
                    locations.add(location);
                }
            } catch (final SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    /*public List<Asset> selectAll() {
        final String sql = "SELECT * FROM bt_asset";

        List<Asset> assets= new ArrayList<Asset>();

        try  {
            if (conn==null) System.out.println("con not found");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("id") + "\t" + rs.getString("name") + "\t" + rs.getString("rfid_tags") + "\t");
                Asset asset= new Asset();
                asset.setAssetName(rs.getString("name"));
                asset.setRfidTags(rs.getString("rfid_tags"));
                assets.add(asset);
            }
        } catch (final SQLException e) {
            System.out.println(e.getMessage());
        }
        return assets;
    }*/

    public List<Asset> selectAllAssets(){
        if (conn!=null){
            return assets;
        }
        else
            assets= new ArrayList<Asset>();
            return assets;
    }

    public List<Location> selectAllLocations(){
        if (conn!=null){
            return locations;
        }
        else
            locations= new ArrayList<Location>();
        return locations;
    }

    public List<Asset> selectAllAssetsByLocationId(String assetID){
        if (conn!=null){
            AllAssetsByLocationId allAssetsByLocationId = new AllAssetsByLocationId(conn, assetID);
            allAssetsByLocationId.execute();
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            listAssetByLocation = allAssetsByLocationId.selectAllAssetsByLocationId();
            return listAssetByLocation;
        }
        else
            listAssetByLocation= new ArrayList<Asset>();
        return listAssetByLocation;
    }

    public Boolean SaveAsssetAudit(List<EpcDataModel> listEpc, Location currentLocationSpinner){
        if (conn!=null){
            SaveAssetAudit saveAssetAudit = new SaveAssetAudit(conn, listEpc, currentLocationSpinner);
            saveAssetAudit.execute();
            return true;
        }
        else{
            return false;
        }
    }
}
