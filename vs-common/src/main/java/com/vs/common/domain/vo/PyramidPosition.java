package com.vs.common.domain.vo;

import com.vs.common.domain.enums.TradeDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by erix-mac on 15/10/19.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PyramidPosition implements Cloneable, Position{
    private int index = -1;
    private TradeDirection direction = TradeDirection.NONE;
    private long positions = -1;

    @Override
    public Object clone(){
        return new PyramidPosition(index,direction,positions);
    }

    @Override
    public long getPosition() {
        return positions;
    }
}
