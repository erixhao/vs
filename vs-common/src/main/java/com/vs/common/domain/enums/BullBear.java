package com.vs.common.domain.enums;


/**
 * Created by erix-mac on 15/11/7.
 */
public enum BullBear {
    BULL("牛市"), BEAR("熊市"), VOLATILE("波动市"), BIGBULL("大牛市"), BIGBEAR("大熊市"), NA("未知");

    private String chinese;

    BullBear(String comments){
        chinese = comments;
    }

    public String getChinese(){
        return this.chinese;
    }

    public boolean isBigBull(){ return this.equals(BIGBULL); }
    public boolean isBigBear(){ return this.equals(BIGBEAR); }

    public boolean isBull(){ return this.equals(BULL) || this.equals(BIGBULL); }
    public boolean isBear(){ return this.equals(BEAR) || this.equals(BIGBEAR); }

    public boolean isVolatile(){ return this.equals(VOLATILE);
    }
}
