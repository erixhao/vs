package com.vs.common.domain.exception;

import lombok.Data;

/**
 * Created by erix-mac on 15/8/24.
 */
@Data
public class BusinessException extends RuntimeException {

    public BusinessException()  {}

    public BusinessException(String errorMessage){
        super(errorMessage);
    }
}
