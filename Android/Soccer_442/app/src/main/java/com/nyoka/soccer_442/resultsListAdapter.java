package com.nyoka.soccer_442;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.caverock.androidsvg.SVG;
import com.nyoka.soccer_442.football_data.FootballMatch;

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-23.
 */
public class resultsListAdapter extends BaseAdapter {
    public List<FootballMatch> Results;
    private LayoutInflater inflater;
    private Resources resources;
    private Context contextView;
    Utility utility = new Utility();
    String _txt;

    public resultsListAdapter(Context context, List<FootballMatch> comments, String txt){
        contextView = context;
        Results = comments;
        _txt = txt;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = contextView.getResources();
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        FootballMatch result = Results.get(position);
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(contextView);
            rowView = inflater.inflate(R.layout.template_game, parent, false);
            RowDataViewHolder viewHolder = new RowDataViewHolder();
            viewHolder.awayTeamImage = (ImageView) rowView.findViewById(R.id.awayTeamImage);
            if(!result.awayTeam.crest.endsWith(".svg")) {
                Glide.with(contextView).load(result.awayTeam.crest).into(viewHolder.awayTeamImage);
            }
            else {
                new SvgImageLoader().loadSvgImage(contextView, result.awayTeam.crest, viewHolder.awayTeamImage);
            }
            viewHolder.score = (TextView) rowView.findViewById(R.id.score);
            viewHolder.homeTeamImage = (ImageView) rowView.findViewById(R.id.homeTeamImage);

            if(!result.homeTeam.crest.endsWith(".svg")) {
                Glide.with(contextView).load(result.homeTeam.crest).into(viewHolder.homeTeamImage);
            }
            else {
                new SvgImageLoader().loadSvgImage(contextView, result.homeTeam.crest, viewHolder.homeTeamImage);
            }



            viewHolder.date = (TextView) rowView.findViewById(R.id.date);
            viewHolder.title = (TextView) rowView.findViewById(R.id.title);
            viewHolder.homeTeamName = (TextView) rowView.findViewById(R.id.homeTeamName);
            viewHolder.awayTeamName = (TextView) rowView.findViewById(R.id.awayTeamName);
            rowView.setTag(viewHolder);
        }

        RowDataViewHolder holder = (RowDataViewHolder) rowView.getTag();


        utility.GetMeAnImage(holder.homeTeamName, result.homeTeam.name);



        holder.score.setText(result.score.fullTime.home + " - " + result.score.fullTime.away);
        utility.GetMeAnImage(holder.awayTeamName, result.awayTeam.name);
        holder.title.setText(_txt);
        holder.date.setText(result.utcDate.toString());
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
    public int getCount(){
        return Results.size();
    }

    @Override
    public Object getItem(int position){
        return Results.get(position);
    }

    @Override
    public long getItemId(int position){
        FootballMatch commentary = Results.get(position);
        //return commentary;
        return 1;
    }
}
