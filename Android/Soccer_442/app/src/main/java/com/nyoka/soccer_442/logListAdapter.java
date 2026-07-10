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
    // #26: one entry per logItems row, same index - the short competition code ("PL", "CL")
    // to show in the extra column when this adapter is showing My Teams' merged, cross-
    // competition rows. Null in the normal single-competition case (existing behavior).
    private List<String> competitionCodes;
    private LayoutInflater inflater;
    private Resources resources;
    private Context contextView;
    Utility utility = new Utility();

    public logListAdapter(Context context, List<TableItem> log){
        this(context, log, null);
    }

    public logListAdapter(Context context, List<TableItem> log, List<String> competitionCodes){
        contextView = context;
        logItems = log;
        this.competitionCodes = competitionCodes;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = contextView.getResources();
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.template_log_item, null);
            RowDataViewHolder viewHolder = new RowDataViewHolder();
            viewHolder.position = (TextView) rowView.findViewById(R.id.position);
            viewHolder.teamName = (TextView) rowView.findViewById(R.id.entry);
            viewHolder.movement = (TextView) rowView.findViewById(R.id.movement);
            viewHolder.competitionCode = (TextView) rowView.findViewById(R.id.competitionCode);
            viewHolder.Played = (TextView) rowView.findViewById(R.id.p);
            viewHolder.won = (TextView) rowView.findViewById(R.id.w);
            viewHolder.goalDifference = (TextView) rowView.findViewById(R.id.gd);
            viewHolder.points = (TextView) rowView.findViewById(R.id.points);
            viewHolder.container = (LinearLayout) rowView.findViewById(R.id.back);
            rowView.setTag(viewHolder);
        }

        RowDataViewHolder holder = (RowDataViewHolder) rowView.getTag();
        TableItem result = logItems.get(position);

        holder.position.setText(String.valueOf(result.position));
        holder.teamName.setText(result.team.name);
        holder.Played.setText(String.valueOf(result.playedGames));
        holder.goalDifference.setText(String.valueOf(result.goalDifference));
        holder.won.setText(String.valueOf(result.won));
        holder.points.setText(String.valueOf(result.points));
        // (Re)set on every bind, not just row creation - ListView recycles rows, and a
        // recycled row from the normal (non-My-Teams) mode would otherwise keep showing a
        // stale code from whatever unrelated row last used this view.
        holder.competitionCode.setText(competitionCodes != null ? competitionCodes.get(position) : "");

        // "Top 4 / bottom 3" highlighting assumes logItems is one competition's own
        // rank-ordered table - meaningless (and actively misleading) once My Teams mode
        // merges unrelated teams from different competitions into one list, so it's skipped
        // there in favour of every row just being a plain panel.
        if (competitionCodes != null) {
            holder.container.setBackgroundResource(R.drawable.panel);
        } else if(IsInTop(result.team.name))
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
            TextView position;
            TextView teamName;
            TextView movement;
            TextView competitionCode;
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
