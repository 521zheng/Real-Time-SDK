package com.refinitiv.eta.training.common;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.nio.channels.SelectableChannel;
import java.util.Arrays;

public class TrainingModuleUtils {

    public static int getFDValueOfSelectableChannel(SelectableChannel selectableChannel) {
        try {
            return (Integer) Arrays.stream(Introspector.getBeanInfo(selectableChannel.getClass()).getPropertyDescriptors())
                    .filter(pd -> pd.getDisplayName().equals("FDVal"))
                    .map(PropertyDescriptor::getReadMethod)
                    .findFirst()
                    .orElse(null)
                    .invoke(selectableChannel);
        } catch (Exception e) {
            return -1;
        }
    }

}
