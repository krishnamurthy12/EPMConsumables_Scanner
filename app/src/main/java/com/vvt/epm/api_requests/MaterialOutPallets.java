package com.vvt.epm.api_requests;

public class MaterialOutPallets {

    private String refrigerator,racknumber,quantity,fifonumber;
    private int materialId;

    public MaterialOutPallets(String refrigerator, String racknumber, String quantity, int materialId,String fifonumber) {
        this.refrigerator = refrigerator;
        this.racknumber = racknumber;
        this.quantity = quantity;
        this.materialId = materialId;
        this.fifonumber = fifonumber;

    }
}
