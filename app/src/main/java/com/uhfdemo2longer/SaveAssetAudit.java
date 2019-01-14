package com.uhfdemo2longer;

import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaveAssetAudit extends AsyncTask<Void, Void, Void> {

    Connection conn = null;
    List<EpcDataModel> listEpc = null;
    Date date = new Date();
    Location currentLocationSpinner;

    public SaveAssetAudit(Connection conn, List<EpcDataModel> listEpc, Location currentLocationSpinner) {
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
                final String sqlAsset = "INSERT INTO bt_asset_audit_report (date, rfid_tag, asset_name, previous_location, current_location, status)\n" +
                        "VALUES (CURRENT_DATE,'"+epcDataModel.getepc()+"','"+epcDataModel.getAssetName()+"','"+epcDataModel.getPreviousLocation()+"','"+epcDataModel.getCurrentLocation()+"','"+epcDataModel.getStatus()+"')";

                try {
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(sqlAsset);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if(epcDataModel.getPreviousLocation() != epcDataModel.getCurrentLocation()){
                    final String sqlAssetUpdate = "UPDATE bt_asset SET current_loc_id = '"+ currentLocationSpinner.getLocationID() +"' WHERE id = "+epcDataModel.getBtAssetId();

                    try {
                        Statement stmtUpdate = conn.createStatement();
                        stmtUpdate.executeUpdate(sqlAssetUpdate);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            /*final String sqlAssetAudit = "select * from bt_asset_audit_report";

            try  {
                Statement stmt1 = conn.createStatement();
                ResultSet rs = stmt1.executeQuery(sqlAssetAudit);

                for (EpcDataModel epcDataModel:listEpc) {
                    Boolean checkEpc = false;
                    while (rs.next()) {
                        if(rs.getString("rfid_tag").equalsIgnoreCase(epcDataModel.getepc()) && rs.getString("date").equalsIgnoreCase(new SimpleDateFormat("yyyy-MM-dd").format(date))){
                            System.out.println("----------------------------------------Match---------------------------");
                            checkEpc = true;
                            break;
                        }else{
                            checkEpc = false;
                        }
                    }
                    if(checkEpc == false){
                        final String sqlAsset = "INSERT INTO bt_asset_audit_report (date, rfid_tag, asset_name, location, status)\n" +
                                "VALUES (CURRENT_DATE,'"+epcDataModel.getepc()+"','"+epcDataModel.getAssetName()+"','"+epcDataModel.getLocation()+"','"+epcDataModel.getStatus()+"')";

                        Statement stmt = conn.createStatement();
                        stmt.executeUpdate(sqlAsset);
                        System.out.println("----------------------------------------NOT Match---------------------------");
                    }
                }
                conn.close();
            } catch (final SQLException e) {
                System.out.println(e.getMessage());
            }*/
        }
        return null;
    }
}
