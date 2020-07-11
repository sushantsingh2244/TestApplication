package com.sushi.testproject.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.app.job.JobScheduler;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sushi.testproject.MyApplication;
import com.sushi.testproject.R;
import com.sushi.testproject.adapter.DataAdapter;
import com.sushi.testproject.common.Utility;
import com.sushi.testproject.interfaces.ApiInterface;
import com.sushi.testproject.models.CountryModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final int JOB_ID = 100;
    private DataAdapter dataAdapter;
    private RecyclerView itemRecycler;
    private TextView lblTotalCase, lblRecovered, lblDeath;
    private TextView txtTotalCase, txtRecovered, txtDeath;
    private ArrayList<CountryModel> countryInfo;
    private ProgressDialog progressDoalog;
    private int TotalCase = 0;
    private int Death = 0;
    private int Recovered = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemRecycler = findViewById(R.id.itemRecycler);
        lblTotalCase = findViewById(R.id.lblTotalCase);
        lblRecovered = findViewById(R.id.lblRecovered);
        lblDeath = findViewById(R.id.lblDeath);
        txtTotalCase = findViewById(R.id.txtTotalCase);
        txtRecovered = findViewById(R.id.txtRecovered);
        txtDeath = findViewById(R.id.txtDeath);

        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/os_regular.ttf");
        Typeface typeface1 = Typeface.createFromAsset(this.getAssets(), "fonts/os_medium.ttf");
        Typeface typeface2 = Typeface.createFromAsset(this.getAssets(), "fonts/os_bold.ttf");
        lblTotalCase.setTypeface(typeface);
        lblRecovered.setTypeface(typeface);
        lblDeath.setTypeface(typeface);
        txtTotalCase.setTypeface(typeface1);
        txtRecovered.setTypeface(typeface1);
        txtDeath.setTypeface(typeface1);


        countryInfo = new ArrayList<>();
        dataAdapter = new DataAdapter(countryInfo, this);
        Utility.setRecycler(this, itemRecycler, dataAdapter);

        if (Utility.isOnline(MainActivity.this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                JobScheduler jobScheduler = (JobScheduler) MainActivity.this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                // jobScheduler.cancelAll();
                // scheduleJob(MainActivity.this, jobScheduler);
            }
        }

        progressDoalog = new ProgressDialog(MainActivity.this);
        progressDoalog.setMessage("Loading....");
        progressDoalog.show();

        /*Create handle for the RetrofitInstance interface*/
        ApiInterface service = MyApplication.getInstance().getRetrofitInstance().create(ApiInterface.class);

        Call<JsonObject> call = service.getAllData();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressDoalog.dismiss();
                final String respons = response.body().toString();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(respons);
                    JSONArray JArray = jsonObject.getJSONArray("Countries");
                    if (JArray.length() > 0) {
                        for (int i = 0; i < JArray.length(); i++) {
                            JSONObject json = JArray.getJSONObject(i);
                            CountryModel countryModel = new CountryModel();
                            countryModel.setCountry(json.getString("Country"));
                            countryModel.setCountryCode(json.getString("CountryCode"));
                            countryModel.setSlug(json.getString("Slug"));
                            countryModel.setNewConfirmed(Integer.valueOf(json.getString("NewConfirmed")));
                            countryModel.setTotalConfirmed(Integer.valueOf(json.getString("TotalConfirmed")));
                            TotalCase = TotalCase + Integer.parseInt(json.getString("TotalConfirmed"));
                            countryModel.setNewDeaths(Integer.valueOf(json.getString("NewDeaths")));
                            countryModel.setTotalDeaths(Integer.valueOf(json.getString("TotalDeaths")));
                            Death = Death + Integer.parseInt(json.getString("TotalDeaths"));
                            countryModel.setNewRecovered(Integer.valueOf(json.getString("NewRecovered")));
                            countryModel.setTotalRecovered(Integer.valueOf(json.getString("TotalRecovered")));
                            Recovered = Recovered + Integer.parseInt(json.getString("TotalRecovered"));
                            countryModel.setDate(json.getString("Date"));
                            countryInfo.add(countryModel);
                            dataAdapter.notifyDataSetChanged();
                        }
                        txtTotalCase.setText("" + TotalCase);
                        txtRecovered.setText("" + Recovered);
                        txtDeath.setText("" + Death);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressDoalog.dismiss();
                Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
