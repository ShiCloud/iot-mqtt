/*
 * Copyright (c) 2012-2018 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package org.iot.mqtt.store;

import java.util.Collection;

import org.iot.mqtt.common.bean.Message;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-08-04
 */
public interface MessageQueue {
	
	public boolean offer(Message message);
	
	public Collection<Message> pop(int nums);
}
