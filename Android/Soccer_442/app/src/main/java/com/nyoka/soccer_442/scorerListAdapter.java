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
    // #26: one entry per logItems row, same index - the short competition code ("PL", "CL")
    // to show when this adapter is showing My Teams' merged, cross-competition scorers.
    // Null in the normal single-competition case (existing behavior).
    private List<String> competitionCodes;
    private LayoutInflater inflater;
    private Resources resources;
    private Context contextView;

    public scorerListAdapter(Context context, List<Scorer> log){
        this(context, log, null);
    }

    public scorerListAdapter(Context context, List<Scorer> log, List<String> competitionCodes){
        contextView = context;
        logItems = log;
        this.competitionCodes = competitionCodes;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = contextView.getResources();
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.template_scorer_item, null);
            RowDataViewHolder viewHolder = new RowDataViewHolder();
            viewHolder.teamName = (TextView) rowView.findViewById(R.id.entry);
            viewHolder.competitionCode = (TextView) rowView.findViewById(R.id.competitionCode);
            viewHolder.points = (TextView) rowView.findViewById(R.id.goals);
            rowView.setTag(viewHolder);
        }

        RowDataViewHolder holder = (RowDataViewHolder) rowView.getTag();
        Scorer result = logItems.get(position);

        holder.teamName.setText(result.player.name);
        holder.points.setText(String.valueOf(result.goals));
        // (Re)set on every bind - ListView recycles rows, and a recycled row from the
        // normal (non-My-Teams) mode would otherwise keep showing a stale code.
        holder.competitionCode.setText(competitionCodes != null ? competitionCodes.get(position) : "");
        return rowView;
    }

    static class RowDataViewHolder {
        TextView teamName;
        TextView competitionCode;
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
