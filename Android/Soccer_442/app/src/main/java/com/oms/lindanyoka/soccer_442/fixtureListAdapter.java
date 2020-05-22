package com.oms.lindanyoka.soccer_442;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-23.
 */
public class fixtureListAdapter  extends BaseAdapter {
    private List<Fixture> fixtures;
    private LayoutInflater inflater;
    private Resources resources;
    private Context contextView;
    Utility utility = new Utility();
    String _txt;

    public fixtureListAdapter(Context context, ArrayList<Fixture> fixtures, String txt) {
        contextView = context;
        this.fixtures = fixtures;
        _txt = txt;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = contextView.getResources();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.template_game, parent,false);
            RowDataViewHolder viewHolder = new RowDataViewHolder();
            //viewHolder.awayTeamImage = (ImageView) rowView.findViewById(R.id.awayTeamImage);
            viewHolder.score = (TextView) rowView.findViewById(R.id.score);
            //viewHolder.homeTeamImage = (ImageView) rowView.findViewById(R.id.homeTeamImage);
            viewHolder.date = (TextView) rowView.findViewById(R.id.date);
            viewHolder.title = (TextView) rowView.findViewById(R.id.title);
            viewHolder.homeTeamName = (TextView) rowView.findViewById(R.id.homeTeamName);
            viewHolder.awayTeamName = (TextView) rowView.findViewById(R.id.awayTeamName);
            rowView.setTag(viewHolder);
            rowView.setMinimumHeight(160);
        }

        RowDataViewHolder holder = (RowDataViewHolder) rowView.getTag();
        Fixture result = fixtures.get(position);

        utility.GetMeAnImage(holder.homeTeamName, result.HomeTeamName);
        holder.score.setText("VS");
        utility.GetMeAnImage(holder.awayTeamName, result.AwayTeamName);
        holder.title.setText(_txt);
        holder.date.setText(result.FixtureDate.toString() );//+ " " + result.Time);

        //ViewGroup.LayoutParams params = rowView.getLayoutParams();
//        rowView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,175));
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
        return fixtures.size();
    }

    @Override
    public Object getItem(int position) {
        return fixtures.get(position);
    }

    @Override
    public long getItemId(int position) {
        Fixture commentary = fixtures.get(position);
        //return commentary;
        return 1;
    }
}
