package com.oms.lindanyoka.soccer_442;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda.nyoka on 2015-02-22.
 */
public class newsListAdapter extends BaseAdapter {
    private List<NewsItem> commentaryList;
    private LayoutInflater inflater;
    private Resources resources;
    private Context contextView;
    private Utility utility = new Utility();

    public newsListAdapter(Context context, ArrayList<NewsItem> comments){
        contextView = context;
        commentaryList = comments;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resources = contextView.getResources();
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View rowView = convertView;
        if(rowView == null){
            rowView = inflater.inflate(R.layout.news_item,null);
            RowDataViewHolder viewHolder = new RowDataViewHolder();
            viewHolder.comment = (TextView) rowView.findViewById(R.id.comment);
            rowView.setTag(viewHolder);
        }

        RowDataViewHolder holder = (RowDataViewHolder) rowView.getTag();
        NewsItem commentary = commentaryList.get(position);
        holder.comment.setText(commentary.title);


        if(position %2 == 1){
            //rowView.setBackgroundColor(Color.parseColor("#DEDEDE"));
        }else {
            //rowView.setBackgroundColor(Color.parseColor("#E8E8E8"));
        }
        //rowView.setBackgroundResource(R.drawable.panel);

        Animation animation = AnimationUtils.loadAnimation(contextView, R.anim.abc_fade_in);
        animation.setDuration(500);
        rowView.startAnimation(animation);
        animation = null;

        return rowView;
    }

    static class RowDataViewHolder {
        public TextView comment;
    }

    @Override
    public int getCount(){
        return commentaryList.size();
    }

    @Override
    public Object getItem(int position){
        return commentaryList.get(position);
    }

    @Override
    public long getItemId(int position){
        NewsItem commentary = commentaryList.get(position);
        //return commentary;
        return 1;
    }

}
