package com.itkevin.common.notice;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.HashUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;
import com.itkevin.common.config.SysConfig;
import com.itkevin.common.constants.SysConstant;
import com.itkevin.common.enums.AlarmToolEnum;
import com.itkevin.common.enums.BusinessTypeEnum;
import com.itkevin.common.enums.LogConstantEnum;
import com.itkevin.common.enums.LogTypeEnum;
import com.itkevin.common.enums.MDCConstantEnum;
import com.itkevin.common.enums.RequestTypeEnum;
import com.itkevin.common.model.*;
import com.itkevin.common.notice.dingding.DingMarkDownMessage;
import com.itkevin.common.notice.workwx.WorkWeiXinTalkNotice;
import com.itkevin.common.util.CommonConverter;
import com.itkevin.common.util.HashedWheelUtils;
import com.itkevin.common.util.LocalCacheUtils;
import com.itkevin.common.util.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NotifyMessageTools {

    /**
     * 发送单条报警-指定线程池
     */
    private static ExecutorService executorService = new ThreadPoolExecutor(SysConstant.THREAD_NUM, SysConstant.MAX_THREAD_NUM, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1), new ThreadPoolExecutor.DiscardPolicy());

    private static NoticeInterface noticeInterface;

    private static volatile NotifyMessageTools notifyMessageTools;

    public static NotifyMessageTools getInstance() {
        try {
            if (null == notifyMessageTools) {
                synchronized (NotifyMessageTools.class) {
                    if (null == notifyMessageTools) {
                        String alarmTool = SysConfig.instance.getAlarmTool();
                        ServiceLoader<NoticeInterface> serviceLoader = ServiceLoader.load(NoticeInterface.class);
                        for (NoticeInterface noticeInterfaceLoop : serviceLoader) {
                            if (alarmTool.equals(noticeInterfaceLoop.filterFlag())) {
                                noticeInterface = noticeInterfaceLoop;
                            }
                        }
                        if (Objects.isNull(noticeInterface)) {
                            noticeInterface = WorkWeiXinTalkNotice.getInstance();
                        }
                        notifyMessageTools = new NotifyMessageTools();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notifyMessageTools;
    }

    /**
     * 发送消息
     *
     * @param logData
     */
    public void sendMessage(LogData logData) {
        Mono.fromRunnable(() -> {
            try {
                // 报警间隔、报警次数
                Integer alarmNotifyTime = SysConfig.instance.getAlarmNotifyTime();
                Integer alarmNotifyCount = SysConfig.instance.getAlarmNotifyCount();
                if (alarmNotifyTime == null || alarmNotifyCount == null || alarmNotifyTime == 0 || alarmNotifyCount == 0) {
                    sendMsgTalk(logData);
                } else {
                    // 根据requestURI+exceptionMessage聚合，如果exceptionMessage为空则根据requestURI+errorMessage聚合
                    String requestURI = StringUtils.isNotBlank(logData.getRequestURI()) ? logData.getRequestURI().replaceAll("/\\d+", "/{PathVariable}") : "default";
                    String exceptionMessage = "";
                    if (StringUtils.isNotBlank(logData.getExceptionMessage())) {
                        String exceMessage = logData.getExceptionMessage();
                        exceptionMessage = exceMessage.contains("cause") ? exceMessage.substring(0, exceMessage.indexOf("cause")) : exceMessage;
                    }
                    String errorMessage = StringUtils.isNotBlank(logData.getErrorMessage()) ? logData.getErrorMessage() : "";
                    if (StringUtils.isNotBlank(requestURI) && (StringUtils.isNotBlank(exceptionMessage) || StringUtils.isNotBlank(errorMessage)) && !isFilterAggre(logData.getErrorMessage(), logData.getExceptionMessage())) {
                        // 聚合维度key，替换调不必要的字符(因为可能会造成同一个异常信息不同的字符导致计算的哈希值不一样)
                        String key = requestURI + (StringUtils.isNotBlank(exceptionMessage) ? exceptionMessage : errorMessage);
                        key = key.replaceAll("\\$.*?\\.", "");
                        // 替换掉数字
                        key = key.replaceAll("\\d+", "");
                        // 截取500个字符
                        key = key.length() > SysConstant.ALARM_MESSAGE_STR_DEFAULT ? key.substring(0, SysConstant.ALARM_MESSAGE_STR_DEFAULT) : key;
                        String hashCode = requestURI + HashUtil.bkdrHash(key);
                        Integer count = LocalCacheUtils.incr(hashCode);
                        if (count.compareTo(alarmNotifyCount) <= 0) {
                            if (count.compareTo(1) == 0) {
                                // 第一次发消息时给时间轮上添加任务
                                BusinessData businessData = CommonConverter.getConverter().map(logData, BusinessData.class);
                                businessData.setRequestURI(requestURI);
                                businessData.setExceptionMessage(exceptionMessage);
                                businessData.setErrorMessage(errorMessage);
                                HashedWheelUtils.putWheelQueue(new HashedWheelData(alarmNotifyTime, BusinessTypeEnum.NOTIFY.name(), hashCode, JSONUtil.toJsonStr(businessData)));
                            }
                            sendMsgTalk(logData);
                        }
                    } else {
                        sendMsgTalk(logData);
                    }
                }
            } catch (Throwable e) {
                log.warn("log skyeye >>> NotifyMessageUtils.sendMessage occur exception", e);
            }
        }).subscribeOn(Schedulers.fromExecutorService(executorService, "skyeye-sendMessage-executor")).subscribe();
    }

    /**
     * 是否过滤聚合报警
     *
     * @param errorMessage
     * @param exceptionMessage
     * @return
     */
    private boolean isFilterAggre(String errorMessage, String exceptionMessage) {
        String alarmAggreWhiteList = SysConfig.instance.getAlarmAggreWhiteList();
        return LogUtils.filter(errorMessage, alarmAggreWhiteList) || LogUtils.filter(exceptionMessage, alarmAggreWhiteList);
    }

    /**
     * 发送钉钉
     *
     * @param logData
     */
    public void sendMsgTalk(LogData logData) {
        // 是否过滤日志数据
        if (logData.getFilter() != null && logData.getFilter()) {
            return;
        }
        MarkDownBaseMessage message = getMarkDownMessage(logData);
        // 发送
        noticeInterface.sendMessage(message);
    }

    private MarkDownBaseMessage getMarkDownMessage(LogData logData) {
        String requestType = logData.getRequestType();
        MarkDownBaseMessage message = new MarkDownBaseMessage();
        message.setLevel(logData.getLevel());
        message.setTitle(StringUtils.isNotBlank(logData.getErrorMessage()) ? logData.getErrorMessage() : "error");
        StringBuilder builder = new StringBuilder();
        builder.append("### **").append(MDCConstantEnum.ERROR_MESSAGE.getName()).append("：").append(logData.getErrorMessage().replaceAll(System.getProperty("line.separator"), " ").replaceAll("\n", " ")).append("**").append(System.getProperty("line.separator"));
        builder.append(" ").append(MDCConstantEnum.SERVER_NAME.getName()).append("：").append(logData.getServerName()).append(System.getProperty("line.separator"));
        if (requestType != null && requestType.toLowerCase().contains(RequestTypeEnum.HTTP.name().toLowerCase())
                || RequestTypeEnum.DUBBO.name().equalsIgnoreCase(requestType)) {
            builder.append(" ").append(MDCConstantEnum.SOURCE_IP.getName()).append("：").append(logData.getSourceIP()).append(System.getProperty("line.separator"));
        }
        builder.append(" ").append(MDCConstantEnum.SERVER_IP.getName()).append("：").append(logData.getServerIP()).append(System.getProperty("line.separator"));
        builder.append(" ").append(MDCConstantEnum.SERVER_HOSTNAME.getName()).append("：").append(logData.getServerHostname()).append(System.getProperty("line.separator"));
        builder.append(" ").append(MDCConstantEnum.OCCURRENCE_TIME.getName()).append("：").append(logData.getOccurrenceTime()).append(System.getProperty("line.separator"));
        builder.append(" ").append(MDCConstantEnum.REQUEST_TYPE.getName()).append("：").append(logData.getRequestType()).append(System.getProperty("line.separator"));
        builder.append(" ").append(MDCConstantEnum.TRACE_ID.getName()).append("：").append(logData.getTraceId()).append(System.getProperty("line.separator"));
        builder.append(" ").append(MDCConstantEnum.REQUEST_URI.getName()).append("：").append(logData.getRequestURI()).append(System.getProperty("line.separator"));
        if (requestType != null && requestType.toLowerCase().contains(RequestTypeEnum.HTTP.name().toLowerCase())
                || RequestTypeEnum.DUBBO.name().equalsIgnoreCase(requestType)
                || RequestTypeEnum.JOB.name().equalsIgnoreCase(requestType)) {
            builder.append("+ ").append(MDCConstantEnum.REQUEST_PARAM.getName()).append("：").append(logData.getRequestParam()).append(System.getProperty("line.separator"));
        }
        if (RequestTypeEnum.MQ.name().equalsIgnoreCase(requestType)) {
            builder.append("+ ").append(MDCConstantEnum.MESSAGE_TOPIC.getName()).append("：").append(logData.getMessageTopic()).append(System.getProperty("line.separator"));
            builder.append("+ ").append(MDCConstantEnum.MESSAGE_ID.getName()).append("：").append(logData.getMessageId()).append(System.getProperty("line.separator"));
            builder.append("+ ").append(MDCConstantEnum.MESSAGE_KEYS.getName()).append("：").append(logData.getMessageKeys()).append(System.getProperty("line.separator"));
        }
        if (RequestTypeEnum.EVENT.name().equalsIgnoreCase(requestType)) {
            builder.append("+ ").append(MDCConstantEnum.EVENT_NAME.getName()).append("：").append(logData.getEventName()).append(System.getProperty("line.separator"));
            builder.append("+ ").append(MDCConstantEnum.EVENT_PAYLOAD.getName()).append("：").append(logData.getEventPayload()).append(System.getProperty("line.separator"));
        }
        String exceptionMessage = StringUtils.isNotBlank(logData.getExceptionMessage()) ? logData.getExceptionMessage().replaceAll(System.getProperty("line.separator"), " ").replaceAll("\n", " ") : "";
        builder.append(" ").append(MDCConstantEnum.EXCEPTION_MESSAGE.getName()).append("：").append(exceptionMessage.length() > SysConstant.ALARM_MESSAGE_STR_DEFAULT ? exceptionMessage.substring(0, SysConstant.ALARM_MESSAGE_STR_DEFAULT) + "..." : exceptionMessage).append(System.getProperty("line.separator"));
        builder.append(" ").append(MDCConstantEnum.EXCEPTION_STACKTRACE.getName()).append("：").append(System.getProperty("line.separator")).append(System.getProperty("line.separator")).append("`").append(logData.getExceptionStackTrace().replace("###", "-###")).append("`");
        builder = SysConstant.WELCOME.equals(logData.getErrorMessage()) ? getWelcomeContent(logData) : builder;
        message.setContent(builder.toString());
        return message;
    }

    /**
     * 获取欢迎语内容格式
     *
     * @param logData
     * @return
     */
    private StringBuilder getWelcomeContent(LogData logData) {
        StringBuilder builder = new StringBuilder();
        builder.append("### **").append(logData.getErrorMessage()).append("**").append(System.getProperty("line.separator"));
        builder.append("+ ").append(MDCConstantEnum.SERVER_NAME.getName()).append("：").append(logData.getServerName()).append(System.getProperty("line.separator"));
        builder.append("+ ").append(MDCConstantEnum.SERVER_IP.getName()).append("：").append(logData.getServerIP()).append(System.getProperty("line.separator"));
        builder.append("+ ").append(MDCConstantEnum.SERVER_HOSTNAME.getName()).append("：").append(logData.getServerHostname());
        String deployTag = getDeployTag();
        if (StringUtils.isNotBlank(deployTag)) {
            builder.append(System.getProperty("line.separator"));
            builder.append("+ ").append(MDCConstantEnum.DEPLOY_TAG.getName()).append("：").append(deployTag);
        }
        return builder;
    }

    /**
     * 获取部署tag号
     *
     * @return
     */
    private String getDeployTag() {
        String deployTag = "";
        try {
            String path = System.getProperty("user.dir") + "/GIT_VERSION";
            if (FileUtil.exist(path)) {
                List<String> lines = FileUtil.readUtf8Lines(path);
                if (!CollectionUtil.isEmpty(lines)) {
                    deployTag = lines.get(0);
                }
            }
        } catch (Exception e) {
            log.warn("log skyeye >>> NotifyMessageUtils.getDeployTag occur exception", e);
        }
        return deployTag;
    }

    /**
     * 发送钉钉
     *
     * @param logCompressData
     */
    public void sendAlarmTalk(LogCompressData logCompressData) {
        DingMarkDownMessage message = new DingMarkDownMessage();
        message.setTitle(StringUtils.isNotBlank(logCompressData.getRequestURI()) ? logCompressData.getRequestURI() : "error");
        // 单独设置异常信息或error信息
        String exceptionMessage = StringUtils.isNotBlank(logCompressData.getExceptionMessage()) ? logCompressData.getExceptionMessage().replaceAll(System.getProperty("line.separator"), " ").replaceAll("\n", " ").replaceAll("\\d+", "X") : "";
        String exceptionMessageBuilder = LogConstantEnum.EXCEPTION_MESSAGE.getName() + "：" + (exceptionMessage.length() > SysConstant.ALARM_MESSAGE_STR_DEFAULT ? exceptionMessage.substring(0, SysConstant.ALARM_MESSAGE_STR_DEFAULT) + "..." : exceptionMessage);
        String errorMessage = StringUtils.isNotBlank(logCompressData.getErrorMessage()) ? logCompressData.getErrorMessage().replaceAll(System.getProperty("line.separator"), " ").replaceAll("\n", " ").replaceAll("\\d+", "X") : "";
        String errorMessageBuilder = LogConstantEnum.ERROR_MESSAGE.getName() + "：" + (errorMessage.length() > SysConstant.ALARM_MESSAGE_STR_DEFAULT ? errorMessage.substring(0, SysConstant.ALARM_MESSAGE_STR_DEFAULT) + "..." : errorMessage);
        String messageBuilder = StringUtils.isNotBlank(logCompressData.getExceptionMessage()) ? exceptionMessageBuilder : errorMessageBuilder;
        // 组装钉钉消息
        String builder = "### **" + LogConstantEnum.REQUEST_URI.getName() + "：" + logCompressData.getRequestURI() + "**" + System.getProperty("line.separator") +
                "+ " + messageBuilder + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.ALARM_TIME.getName() + "：最近" + logCompressData.getAlarmTime() + "分钟" + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.ALARM_COUNT.getName() + "：共" + logCompressData.getAlarmCount() + "次" + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.SERVER_NAME.getName() + "：" + logCompressData.getServerName() + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.SERVER_IP.getName() + "：" + logCompressData.getServerIP() + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.SERVER_HOSTNAME.getName() + "：" + logCompressData.getServerHostname();
        message.setContent(builder);
        // 发送
        noticeInterface.sendMessage(message);
    }

    /**
     * 发送钉钉
     *
     * @param logUriElapsedData
     */
    public void sendAlarmTalk(LogUriElapsedData logUriElapsedData) {
        DingMarkDownMessage message = getDingMarkDownMessage(logUriElapsedData);
        // 发送
        noticeInterface.sendMessage(message);
    }

    private DingMarkDownMessage getDingMarkDownMessage(LogUriElapsedData logUriElapsedData) {
        DingMarkDownMessage message = new DingMarkDownMessage();
        message.setTitle("超时预警");
        String builder = "### **超时预警**" + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.REQUEST_URI.getName() + "：" + logUriElapsedData.getRequestURI() + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.ALARM_TIME.getName() + "：最近" + logUriElapsedData.getAlarmTime() + "分钟" + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.ALARM_COUNT.getName() + "：共" + logUriElapsedData.getAlarmCount() + "次" + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.URI_ELAPSED_THRESHOLD.getName() + "：" + logUriElapsedData.getUriElapsedThreshold() + "ms" + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.URI_ELAPSED_TRACEID_LIST.getName() + "：" + logUriElapsedData.getTraceIdList() + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.MAX_URI_ELAPSED.getName() + "：" + logUriElapsedData.getMaxUriElapsed() + "ms" + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.MAX_URI_ELAPSED_TRACEID.getName() + "：" + logUriElapsedData.getMaxUriElapsedTraceId() + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.SERVER_NAME.getName() + "：" + logUriElapsedData.getServerName() + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.SERVER_IP.getName() + "：" + logUriElapsedData.getServerIP() + System.getProperty("line.separator") +
                "+ " + LogConstantEnum.SERVER_HOSTNAME.getName() + "：" + logUriElapsedData.getServerHostname();
        message.setContent(builder);
        return message;
    }


    public static void main(String[] args) {
        Map<String, String> map = Maps.newHashMap();
        map.put(SysConstant.ALARM_DINGTALK, "[ { \"webHook\": \"https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=b90b1e53-f75c-42dc-8636-e3d69a9c78f4\"} ]");
        map.put(SysConstant.ALARM_ENABLED, "true");
        map.put(SysConstant.ALARM_SERIOUS_DINGTALK, "[ { \"webHook\": \"https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=b90b1e53-f75c-42dc-8636-e3d69a9c78f4\"} ]");
        map.put(SysConstant.ALARM_STACKNUM, "10");
        map.put(SysConstant.ALARM_WHITE_LIST, "");
        map.put(SysConstant.ALARM_AGGRE_WHITE_LIST, "");
        map.put(SysConstant.ALARM_NOTIFY_TIME, "1");
        map.put(SysConstant.ALARM_NOTIFY_COUNT, "1");
        SysConfig.convertMap2SysConfig(map);
        String msg = "这是个错误的信息";
        String message = "这是个exception";
        String stackTrace = "这是个堆栈信息";
        // 过滤信息
        FilterMessage filterMessage = new FilterMessage();
        filterMessage.setLogType(LogTypeEnum.LOG4J.name());
        filterMessage.setMsg(msg);
        filterMessage.setMessage(message);
        filterMessage.setStackTrace(stackTrace);
        LogData logData = LogUtils.obtainLogData(LogTypeEnum.LOG4J.name(), msg, message, stackTrace);
        logData.setOccurrenceTime(DateUtil.formatDateTime(new Date()));
        logData.setFilter(LogUtils.filter(filterMessage));
        // 发送消息
        NotifyMessageTools.getInstance().sendMessage(logData);
        while (true) {

        }
    }

}
