package com.itkevin.common.util;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

public class CommonConverter {
    private static MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
    private static MapperFacade mapperFacade = null;

    static {
        CommonConverter.mapperFacade = CommonConverter.mapperFactory.getMapperFacade();
    }

    private CommonConverter() {

    }

    public static MapperFacade getConverter() {
        return CommonConverter.mapperFacade;
    }
}
