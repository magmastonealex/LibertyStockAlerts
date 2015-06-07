package net.magmastone.liberysharealerts;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class settingsArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private final String[] valuesSub;
    public settingsArrayAdapter(Context context, String[] valuesMain,String[] valuesSecond) {
        super(context, R.layout.settingslayout, valuesMain);
        this.context = context;
        this.values = valuesMain;
        this.valuesSub = valuesSecond;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.settingslayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.setValue);
        TextView valView = (TextView) rowView.findViewById(R.id.setName);
        //ImageView imageView = (ImageView) rowView.findViewById(R.id.personImg);

        textView.setText(values[position]);
        valView.setText(valuesSub[position]);
        if(position==1 ||position==4){
            valView.setTextColor(context.getResources().getColor(R.color.green));
        }
        // change the icon for Windows and iPhone
        //String s = values[position];
        //if (s.startsWith("iPhone")) {
        //    imageView.setImageResource(R.drawable.no);
        //} else {
        //    imageView.setImageResource(R.drawable.ok);
        //}

        return rowView;
    }
}