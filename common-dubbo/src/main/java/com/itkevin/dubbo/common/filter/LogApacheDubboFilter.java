package com.itkevin.dubbo.common.filter;

import com.itkevin.common.enums.MDCConstantEnum;
import com.itkevin.common.enums.RequestTypeEnum;
import com.itkevin.common.model.UriElapsedCollect;
import com.itkevin.common.util.ElapsedUtils;
import com.itkevin.common.util.MDCUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

import java.util.Arrays;

@Slf4j
@Activate(
        group = {CommonConstants.PROVIDER_SIDE},
        order = 1
)
public class LogApacheDubboFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        long start = 0;
        try {
            mdc(invoker, invocation);
            start = System.currentTimeMillis();
            return invoker.invoke(invocation);
        } finally {
            UriElapsedCollect uriElapsedCollect = ElapsedUtils.uriElapsedCollect(System.currentTimeMillis() - start);
            ElapsedUtils.uriElapsed(uriElapsedCollect);
        }
    }

    /**
     * mdc上下文
     * @param invoker
     * @param invocation
     */
    private void mdc(Invoker<?> invoker, Invocation invocation) {
        try {
            RpcContext rpcContext = RpcContext.getContext();
            MDCUtils.put(MDCConstantEnum.SERVER_NAME.getCode(), rpcContext.getUrl().getParameter("application"));
            MDCUtils.put(MDCConstantEnum.SOURCE_IP.getCode(), rpcContext.getRemoteHost());
            MDCUtils.put(MDCConstantEnum.SERVER_IP.getCode(), rpcContext.getLocalHost());
//            MDCUtils.put(MDCConstantEnum.SERVER_HOSTNAME.getCode(), IPUtils.getLocalHostName());
            MDCUtils.put(MDCConstantEnum.REQUEST_TYPE.getCode(), RequestTypeEnum.DUBBO.name().toLowerCase());
            MDCUtils.put(MDCConstantEnum.TRACE_ID.getCode(), TraceContext.traceId());
            MDCUtils.put(MDCConstantEnum.REQUEST_URI.getCode(), invoker.getInterface().getName() + "#" + invocation.getMethodName());
            MDCUtils.put(MDCConstantEnum.REQUEST_PARAM.getCode(), Arrays.asList(invocation.getArguments()).toString());
        } catch (Exception e) {
            log.warn("log skyeye >>> LogApacheDubboFilter occur exception", e);
        }
    }

}
