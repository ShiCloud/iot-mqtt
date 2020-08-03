/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.iot.mqtt.store;

/**
 * @author cloudshi
 *
 * @date 2020年7月13日
 */
public abstract class AbstractMqttStore {

    protected FlowMessageStore flowMessageStore;
    protected WillMessageStore willMessageStore;
    protected RetainMessageStore retainMessageStore;
    protected OfflineMessageStore offlineMessageStore;
    protected SubscriptionStore subscriptionStore;
    protected SessionStore sessionStore;

    public abstract void init() throws Exception;

    public abstract void shutdown();
    
    public FlowMessageStore getFlowMessageStore() {
        return flowMessageStore;
    }

    public OfflineMessageStore getOfflineMessageStore() {
        return offlineMessageStore;
    }

    public RetainMessageStore getRetainMessageStore() {
        return retainMessageStore;
    }

    public SessionStore getSessionStore() {
        return sessionStore;
    }

    public SubscriptionStore getSubscriptionStore() {
        return subscriptionStore;
    }

    public WillMessageStore getWillMessageStore() {
        return willMessageStore;
    }
}
