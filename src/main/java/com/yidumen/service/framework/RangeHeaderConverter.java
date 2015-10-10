package com.yidumen.service.framework;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.Provider;

/**
 * Created by cdm on 2015/10/9.
 */
@Provider
public class RangeHeaderConverter implements ParamConverter<RangeHeader> {

    @Override
    public RangeHeader fromString(String value) {
        return RangeHeader.valueOf(value);
    }

    @Override
    public String toString(RangeHeader value) {
        return value.toString();
    }

}
