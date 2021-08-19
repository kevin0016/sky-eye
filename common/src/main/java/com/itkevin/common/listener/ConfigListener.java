package com.itkevin.common.listener;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.itkevin.common.util.ConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class ConfigListener implements ConfigChangeListener {

    @Override
    public void onChange(ConfigChangeEvent configChangeEvent) {
        try {
            for (String key : configChangeEvent.changedKeys()) {
                ConfigChange change = configChangeEvent.getChange(key);
                PropertyChangeType changeType = change.getChangeType();
                String propertyName = change.getPropertyName();
                String oldValue = change.getOldValue();
                String newValue = change.getNewValue();
                if(StringUtils.isBlank(propertyName)){
                    continue;
                }
                log.info("log skyeye >>> ConfigListener changeType: {}, propertyName: {}, oldValue: {}, newValue: {}", changeType.name(), propertyName, oldValue, newValue);
                if(PropertyChangeType.DELETED.equals(changeType)){
                    ConfigUtils.removeProperty(propertyName);
                } else {
                    ConfigUtils.saveProperty(propertyName,newValue);
                }
            }
        } catch (Exception e) {
            log.warn("log skyeye >>> ConfigListener.onChange occur exception", e);
        }
    }
}
