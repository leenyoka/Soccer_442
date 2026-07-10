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
            viewHolder.score = (TextView) rowView.findViewById(R.id.score);
            viewHolder.homeTeamImage = (ImageView) rowView.findViewById(R.id.homeTeamImage);
            viewHolder.date = (TextView) rowView.findViewById(R.id.date);
            viewHolder.title = (TextView) rowView.findViewById(R.id.title);
            viewHolder.homeTeamName = (TextView) rowView.findViewById(R.id.homeTeamName);
            viewHolder.awayTeamName = (TextView) rowView.findViewById(R.id.awayTeamName);
            rowView.setTag(viewHolder);
        }

        RowDataViewHolder holder = (RowDataViewHolder) rowView.getTag();

        // (Re)loaded on every bind, not just when the row view is first created - ListView
        // recycles row views, and a recycled view otherwise keeps showing whatever crest/badge
        // an earlier, unrelated row happened to load into it.
        utility.ShowTeamBadge(contextView, result.homeTeam, holder.homeTeamImage, holder.homeTeamName);

        holder.score.setText(utility.FormatScore(result.score));
        // This column is narrow (weight 0.39 of the row, 20dp padding each side) - a plain
        // "1 - 1" fits at 28sp, but a penalty-shootout suffix needs to shrink to fit at all.
        holder.score.setTextSize(result.score.penaltyHome != null ? 12 : 28);
        utility.ShowTeamBadge(contextView, result.awayTeam, holder.awayTeamImage, holder.awayTeamName);
        // _txt is a single fixed label for every row ("Results"/"Fixtures"/...) - empty in
        // My Teams mode (there's no one fixed competition to label rows with), where each
        // match's own competition is shown per-row instead, since rows can come from any of
        // several competitions merged together.
        if (_txt != null && !_txt.isEmpty()) {
            holder.title.setText(_txt);
        } else if (result.competition != null && result.competition.name != null) {
            holder.title.setText(result.competition.name);
        } else {
            holder.title.setText("");
        }
        holder.date.setText(utility.FormatMatchDate(result.utcDate));
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
