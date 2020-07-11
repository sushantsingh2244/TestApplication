package com.sushi.testproject.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DataModel {
    @SerializedName("GlobalModel")
    @Expose
    private GlobalModel global;
    @SerializedName("CountryModel")
    @Expose
    private List<CountryModel> countries = null;
    @SerializedName("Date")
    @Expose
    private String date;

    public GlobalModel getGlobal() {
        return global;
    }

    public void setGlobal(GlobalModel global) {
        this.global = global;
    }

    public List<CountryModel> getCountries() {
        return countries;
    }

    public void setCountries(List<CountryModel> countries) {
        this.countries = countries;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
