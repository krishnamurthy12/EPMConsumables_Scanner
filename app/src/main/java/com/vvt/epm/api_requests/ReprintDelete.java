package com.vvt.epm.api_requests;

public class ReprintDelete {
    String refrigerator,racknumber,fifonumber;
    int  materialId;

    public ReprintDelete(String refrigerator, String racknumber, int materialId,String fifonumber) {
        this.refrigerator = refrigerator;
        this.racknumber = racknumber;
        this.fifonumber = fifonumber;
        this.materialId = materialId;
    }
}
