package com.nyoka.soccer_442;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nyoka.soccer_442.football_data.Scorer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-25.
 */
public class scorerListAdapter extends BaseAdapter {
    private List<Scorer> logItems;
    private LayoutInflater inflater;
    private Resources resources;
    private Context contextView;

    public scorerListAdapter(Context context, List<Scorer> log){
        contextView = context;
        logItems = log;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = contextView.getResources();
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.template_scorer_item, null);
            RowDataViewHolder viewHolder = new RowDataViewHolder();
            viewHolder.teamName = (TextView) rowView.findViewById(R.id.entry);
            viewHolder.points = (TextView) rowView.findViewById(R.id.goals);
            rowView.setTag(viewHolder);
        }

        RowDataViewHolder holder = (RowDataViewHolder) rowView.getTag();
        Scorer result = logItems.get(position);

        holder.teamName.setText(result.player.name);
        holder.points.setText(String.valueOf(result.goals));
        return rowView;
    }

    static class RowDataViewHolder {
        TextView teamName;

        TextView points;

    }
    @Override
    public int getCount(){
        return logItems.size();
    }

    @Override
    public Object getItem(int position){
        return logItems.get(position);
    }

    @Override
    public long getItemId(int position){
        Scorer commentary = logItems.get(position);
        //return commentary;
        return 1;
    }
}
