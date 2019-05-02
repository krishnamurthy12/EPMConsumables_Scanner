package com.vvt.epm.api_requests;

public class PrintConfirmation {
    String refrigerator,
            racknumber;
    int materialId;
    String fifonumber;

    public PrintConfirmation(String refrigerator, String racknumber, int materialId, String fifonumber) {
        this.refrigerator = refrigerator;
        this.racknumber = racknumber;
        this.materialId = materialId;
        this.fifonumber = fifonumber;
    }
}
