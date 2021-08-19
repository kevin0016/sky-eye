package com.itkevin.common.filter;

import com.itkevin.common.util.MDCUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Slf4j
@Activate(
        group = {CommonConstants.PROVIDER_SIDE},
        order = -1
)
public class LogApacheMDCClearDubboFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            return invoker.invoke(invocation);
        } finally {
            MDCUtils.clear();
        }
    }
}
