package com.vs.common.domain.enums;

/**
 * Created by erix-mac on 15/8/30.
 */
public enum SortOrder {
    ASC(1), DESC(-1);

    private int value;

    private SortOrder(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }
}
