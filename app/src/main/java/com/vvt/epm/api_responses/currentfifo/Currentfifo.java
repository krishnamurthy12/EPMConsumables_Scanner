package com.vvt.epm.api_responses.currentfifo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Currentfifo {

    @SerializedName("materialId")
    @Expose
    private Integer materialId;
    @SerializedName("presentfifonumber")
    @Expose
    private String presentfifonumber;

    public Integer getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Integer materialId) {
        this.materialId = materialId;
    }

    public String getPresentfifonumber() {
        return presentfifonumber;
    }

    public void setPresentfifonumber(String presentfifonumber) {
        this.presentfifonumber = presentfifonumber;
    }
}
