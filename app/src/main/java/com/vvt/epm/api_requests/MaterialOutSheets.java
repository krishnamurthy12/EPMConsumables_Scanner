package com.vvt.epm.api_requests;

public class MaterialOutSheets {
    private String refrigerator,racknumber,fifonumber;
    private int materialId;

    public MaterialOutSheets(String refrigerator, String racknumber, int materialId, String fifonumber) {
        this.refrigerator = refrigerator;
        this.racknumber = racknumber;
        this.materialId = materialId;
        this.fifonumber = fifonumber;

    }
}
