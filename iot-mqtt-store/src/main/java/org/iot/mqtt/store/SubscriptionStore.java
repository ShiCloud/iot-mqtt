package org.iot.mqtt.store;

import java.util.Collection;

import org.iot.mqtt.common.bean.Subscription;

public interface SubscriptionStore {

    boolean storeSubscription(String clientId, Subscription subscription);

    Collection<Subscription> getSubscriptions(String clientId);

    boolean clearSubscription(String clientId);

    boolean removeSubscription(String clientId,String topic);

}
