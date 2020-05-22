package com.oms.lindanyoka.soccer_442;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-24.
 */
public class liveListAdapter  extends BaseAdapter {
    private List<Live> liveList;
    private LayoutInflater inflater;
    private Resources resources;
    private Context contextView;
    Utility utility = new Utility();
    String _txt;

    public liveListAdapter(Context context, ArrayList<Live> liveGames, String txt) {
        contextView = context;
        this.liveList = liveGames;
        _txt = txt;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = contextView.getResources();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.template_game, parent, false);
            RowDataViewHolder viewHolder = new RowDataViewHolder();
            //viewHolder.awayTeamImage = (ImageView) rowView.findViewById(R.id.awayTeamImage);
            viewHolder.score = (TextView) rowView.findViewById(R.id.score);
            //viewHolder.homeTeamImage = (ImageView) rowView.findViewById(R.id.homeTeamImage);
            viewHolder.date = (TextView) rowView.findViewById(R.id.date);
            viewHolder.title = (TextView) rowView.findViewById(R.id.title);
            viewHolder.homeTeamName = (TextView) rowView.findViewById(R.id.homeTeamName);
            viewHolder.awayTeamName = (TextView) rowView.findViewById(R.id.awayTeamName);
            rowView.setTag(viewHolder);
        }

        RowDataViewHolder holder = (RowDataViewHolder) rowView.getTag();
        Live result = liveList.get(position);

        utility.GetMeAnImage(holder.homeTeamName, result.HomeTeamName);
        holder.score.setText(result.HomeTeamScore + " - " + result.AwayTeamScore);
        utility.GetMeAnImage(holder.awayTeamName, result.AwayTeamName);
        holder.title.setText(_txt);//result.MatchStatus);
        holder.date.setText(result.MatchStatus);//show latest time, from last comment
        return rowView;
    }



    static class RowDataViewHolder {
        public ImageView awayTeamImage;
        public TextView score;
        public ImageView homeTeamImage;
        public TextView title;
        public TextView date;
        public TextView homeTeamName;
        public TextView awayTeamName;
    }

    @Override
    public int getCount() {
        return liveList.size();
    }

    @Override
    public Object getItem(int position) {
        return liveList.get(position);
    }

    @Override
    public long getItemId(int position) {
        Live commentary = liveList.get(position);
        //return commentary;
        return 1;
    }
}
