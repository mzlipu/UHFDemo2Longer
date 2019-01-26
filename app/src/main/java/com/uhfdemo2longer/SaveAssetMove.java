package com.uhfdemo2longer;

import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

public class SaveAssetMove extends AsyncTask<Void, Void, Void> {

    Connection conn = null;
    List<EpcDataModel> listEpc = null;
    Date date = new Date();
    Location currentLocationSpinner;

    public SaveAssetMove(Connection conn, List<EpcDataModel> listEpc, Location currentLocationSpinner) {
        this.conn = conn;
        this.listEpc = listEpc;
        this.currentLocationSpinner = currentLocationSpinner;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (conn==null){
            System.out.println("con not found");
        }
        else {
            for (EpcDataModel epcDataModel:listEpc) {
                if (epcDataModel.getepc() == null){
                    continue;
                }
                final String sqlAssetMove = "INSERT INTO bt_asset_move (from_loc_id, asset_id, to_loc_id, state)\n" +
                        "VALUES ('"+epcDataModel.getPreviousLocationId()+"','"+epcDataModel.getBtAssetId()+"','"+currentLocationSpinner.getLocationID()+"','waiting for approve')";

                try {
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(sqlAssetMove);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
