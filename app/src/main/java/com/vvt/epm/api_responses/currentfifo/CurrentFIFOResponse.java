package com.vvt.epm.api_responses.currentfifo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CurrentFIFOResponse {

    @SerializedName("nextfifo")
    @Expose
    private List<Currentfifo> currentfifo = null;

    public List<Currentfifo> getCurrentfifo() {
        return currentfifo;
    }

    public void setCurrentfifo(List<Currentfifo> currentfifo) {
        this.currentfifo = currentfifo;
    }
}
