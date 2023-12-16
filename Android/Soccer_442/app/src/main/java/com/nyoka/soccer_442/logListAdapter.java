package com.nyoka.soccer_442;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nyoka.soccer_442.football_data.StandingsItem;
import com.nyoka.soccer_442.football_data.TableItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-24.
 */
public class logListAdapter  extends BaseAdapter {
    private List<TableItem> logItems;
    private LayoutInflater inflater;
    private Resources resources;
    private Context contextView;
    Utility utility = new Utility();

    public logListAdapter(Context context, List<TableItem> log){
        contextView = context;
        logItems = log;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = contextView.getResources();
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.template_log_item, null);
            RowDataViewHolder viewHolder = new RowDataViewHolder();
            viewHolder.teamName = (TextView) rowView.findViewById(R.id.entry);
            viewHolder.movement = (TextView) rowView.findViewById(R.id.movement);
            viewHolder.Played = (TextView) rowView.findViewById(R.id.p);
            viewHolder.won = (TextView) rowView.findViewById(R.id.w);
            viewHolder.goalDifference = (TextView) rowView.findViewById(R.id.gd);
            viewHolder.points = (TextView) rowView.findViewById(R.id.points);
            viewHolder.container = (LinearLayout) rowView.findViewById(R.id.back);
            rowView.setTag(viewHolder);
        }

        RowDataViewHolder holder = (RowDataViewHolder) rowView.getTag();
        TableItem result = logItems.get(position);

        holder.teamName.setText(result.team.name);
        holder.Played.setText(String.valueOf(result.playedGames));
        holder.goalDifference.setText(String.valueOf(result.goalDifference));
        holder.won.setText(String.valueOf(result.won));
        holder.points.setText(String.valueOf(result.points));

        if(IsInTop(result.team.name))
            holder.container.setBackgroundResource( R.drawable.topthree);
        else if(IsInBottomThree(result.team.name))
            holder.container.setBackgroundResource( R.drawable.bottomthree);
        else holder.container.setBackgroundResource(R.drawable.panel);
/*
        if (result.Movement == 1)
            new Utility().GetMeAnImage(holder.movement, "log_up",true);
        else if(result.Movement == -1)
            new Utility().GetMeAnImage(holder.movement, "log_down", true);
        else new Utility().GetMeAnImage(holder.movement, "invisible", true);
 */
        return rowView;
    }
    private boolean IsInTop(String name)
    {
        for(int i = 0; i < 4; i ++)
            if(logItems.get(i).team.name.equals( name))
                return true;
        return false;
    }
    private boolean IsInBottomThree(String name)
    {
        if(name.equals( logItems.get(logItems.size()-1).team.name) ||
                name.equals( logItems.get(logItems.size()-2).team.name) ||
        name.equals( logItems.get(logItems.size()-3).team.name)
                )return true;

        return false;
    }
    static class RowDataViewHolder {
            TextView teamName;
            TextView movement;
            TextView Played;
            TextView won;
        TextView goalDifference;
        TextView points;
        LinearLayout container;

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
        TableItem commentary = logItems.get(position);
        //return commentary;
        return 1;
    }
}
