package com.sushi.testproject.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sushi.testproject.R;
import com.sushi.testproject.models.CountryModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<CountryModel> countryModels;
    private CountryModel countryModel;
    private WeakReference<Context> context;
    private FragmentManager fragmentManager;
    private SharedPreferences sharedPreferences;
    private String country;

    public DataAdapter(ArrayList<CountryModel> countryModels, Context context) {
        this.countryModels = countryModels;
        this.context = new WeakReference<>(context);
        this.fragmentManager = fragmentManager;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.data_list_item, parent, false);
        return new VHItem(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHItem) {
            countryModel = getItem(position);
            sharedPreferences = context.get().getSharedPreferences("CountryName", 0);
            country = sharedPreferences.getString("Country", "");
                String countryName= countryModel.getCountry();
                if (countryName.equalsIgnoreCase(country)) {
                    setView(holder, countryModel);
                }

            setView(holder, countryModel);
        }
    }


    private void setView(RecyclerView.ViewHolder holder, CountryModel expenseModel) {
        ((VHItem) holder).txtCountry.setText(countryModel.getCountry());
        ((VHItem) holder).txtTotalCase.setText(countryModel.getNewConfirmed() + "/" + countryModel.getTotalConfirmed());
        ((VHItem) holder).txtRecovered.setText(countryModel.getNewRecovered() + "/" + countryModel.getTotalRecovered());
        ((VHItem) holder).txtDeath.setText(countryModel.getNewDeaths() + "/" + countryModel.getTotalDeaths());
    }


    @Override
    public int getItemCount() {
        if (countryModels.size() <= 0)
            return 0;
        else return countryModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public CountryModel getItem(int position) {
        return countryModels.get(position);
    }

    private class VHItem extends RecyclerView.ViewHolder {
        private TextView txtCountry, lblTotalCase, txtTotalCase, lblDeath, txtDeath, lblRecovered, txtRecovered;
        private View itemView1;
        private LinearLayout rowLayout;

        private VHItem(View itemView) {
            super(itemView);

            txtCountry = (TextView) itemView.findViewById(R.id.txtCountry);
            lblTotalCase = (TextView) itemView.findViewById(R.id.lblTotalCase);
            txtTotalCase = (TextView) itemView.findViewById(R.id.txtTotalCase);
            lblDeath = (TextView) itemView.findViewById(R.id.lblDeath);
            txtDeath = (TextView) itemView.findViewById(R.id.txtDeath);
            lblRecovered = (TextView) itemView.findViewById(R.id.lblRecovered);
            txtRecovered = (TextView) itemView.findViewById(R.id.txtRecovered);

            Typeface typeface = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/os_regular.ttf");
            Typeface typeface1 = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/os_medium.ttf");
            Typeface typeface2 = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/os_bold.ttf");

            txtCountry.setTypeface(typeface2);
            lblTotalCase.setTypeface(typeface);
            lblRecovered.setTypeface(typeface);
            lblDeath.setTypeface(typeface);
            txtTotalCase.setTypeface(typeface1);
            txtRecovered.setTypeface(typeface1);
            txtDeath.setTypeface(typeface1);

            itemView1 = itemView;
        }
    }
}
