package com.nyoka.soccer_442;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by linda.nyoka on 2015-03-01.
 */
public class activity_msg extends DialogFragment {
    private Context context;
    private boolean post;
    public activity_msg() {
        // Empty constructor required for DialogFragment
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.dialog_layout, container);
        Button okButton= (Button) view.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dismissTermsDialog();
            }
        });
        TextView textView = (TextView)view.findViewById(R.id.textView);
        Bundle bundle = getArguments();
        String test = bundle.getString("message");
        post = bundle.getBoolean("post");
        String heading = "Competition";
        if(bundle.getString("heading") !=null && !bundle.getString("heading").isEmpty())
        {
            heading = bundle.getString("heading");
        }
        getDialog().setTitle(heading);
        textView.setText(test);
        return view;
    }
    private void dismissTermsDialog()
    {
        this.dismiss();

    }
}
