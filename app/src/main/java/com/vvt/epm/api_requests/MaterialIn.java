package com.vvt.epm.api_requests;

public class MaterialIn {
    private String refrigerator,racknumber,quantity;
    private int materialId;

    public MaterialIn(String refrigerator, String racknumber, String quantity, int materialId) {
        this.refrigerator = refrigerator;
        this.racknumber = racknumber;
        this.quantity = quantity;
        this.materialId = materialId;
    }
}
