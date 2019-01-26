package com.uhfdemo2longer;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


/**
 * Created by lbx on 2017/3/13.
 */
public class EPCadapter extends BaseAdapter {

    private List<EpcDataModel> list ;
    private Context context ;

    public EPCadapter(Context context , List<EpcDataModel> list){
        this.context = context ;
        this.list = list ;
    }
    @Override

    public int getCount() {
        return list.size() ;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                if(context instanceof AssetAudit) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_epc_asset_audit, null);
                } else if(context instanceof AssetMove){
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_epc_asset_move, null);
                } else{
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_epc, null);
                }
                holder.tvEpc = (TextView) convertView.findViewById(R.id.textView_epc);
                holder.tvAssetName = (TextView) convertView.findViewById(R.id.textView_asset_name);
                holder.tvPreviousLocation = (TextView) convertView.findViewById(R.id.textView_previous_location);
                holder.tvCurrentLocation = (TextView) convertView.findViewById(R.id.textView_current_location);
                holder.tvId = (TextView) convertView.findViewById(R.id.textView_id);
                holder.tvStatus = (TextView) convertView.findViewById(R.id.textView_status);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (list != null && !list.isEmpty()) {
                int id = position + 1;
                holder.tvId.setText("" + id);
                holder.tvEpc.setText(list.get(position).getepc());
                holder.tvAssetName.setText(list.get(position).getAssetName());
                holder.tvPreviousLocation.setText(list.get(position).getPreviousLocation());
                holder.tvCurrentLocation.setText(list.get(position).getCurrentLocation());
                holder.tvStatus.setText(list.get(position).getStatus());
                if(list.get(position).getStatus().equalsIgnoreCase("Not Matched")){
                    holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.light_red));
                }
            }
        }catch (Exception e){

        }

        return convertView;
    }

    private class ViewHolder {
        TextView tvEpc ;
        TextView tvAssetName ;
        TextView tvPreviousLocation ;
        TextView tvCurrentLocation ;
        TextView tvId ;
        TextView tvStatus ;
    }
}
