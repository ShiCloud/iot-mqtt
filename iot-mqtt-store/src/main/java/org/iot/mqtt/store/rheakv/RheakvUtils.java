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
package org.iot.mqtt.store.rheakv;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.iot.mqtt.common.utils.SerializeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.rhea.JRaftHelper;
import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaIterator;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.client.pd.PlacementDriverClient;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;
import com.alipay.sofa.jraft.rhea.util.Maps;
import com.alipay.sofa.jraft.rhea.util.StackTraceUtil;
import com.alipay.sofa.jraft.rhea.util.concurrent.DistributedLock;
import com.alipay.sofa.jraft.util.BytesUtil;
import com.alipay.sofa.jraft.util.Endpoint;

/**
 * @author cloudshi 14768909@qq.com
 * @date 2020-07-13
 */
public class RheakvUtils {

	private static final Logger log = LoggerFactory.getLogger(RheakvUtils.class);

	public static void main(String[] args) {
		System.out.println(getFormatId(Integer.MAX_VALUE));
	}
	
	public static String MIN_START = "0000000000000000000";
	public static String MAX_END = String.valueOf(Long.MAX_VALUE);
	
	public static String getFormatId(long num) {
		String format = String.format("%019d", num);
		return format;
    }
	
	public static String getMinKey(String prefix) {
		return prefix+MIN_START;
    }
	
	public static String getMaxKey(String prefix) {
		return prefix+MAX_END;
    }
	
	public static Boolean delAll(RheaKVStore store,String keyPrefix) {
		return store.deleteRange(getMinKey(keyPrefix), getMaxKey(keyPrefix)).join();
    }
	
	public static <T> Collection<T> getByPrefix(RheaKVStore store,String keyPrefix, int nums,Class<T> clazz) {
		String minKey = getMinKey(keyPrefix);
		String maxKey = getMaxKey(keyPrefix);
		RheaIterator<KVEntry> iterator = store.iterator(minKey, maxKey, nums);
		Collection<T> list = new ArrayList<T>();
		int count = 0;
		while(count < nums && iterator.hasNext()) {
			KVEntry next = iterator.next();
			byte[] k = next.getKey();
			T obj = SerializeHelper.deserialize(next.getValue(),clazz);
			String key = BytesUtil.readUtf8(k);
			list.add(obj);
			count++;
			log.debug("RheakvUtils getByPrefix key {}",key);
		}
		return list.size()>0?list:null;	
	}
	
	public static boolean add(RheaKVStore store,String key, Object obj) {
		log.debug("RheakvUtils add key {}",key);
		return store.put(key,SerializeHelper.serialize(obj)).join();
	}
	
	public static <T> T getObject(RheaKVStore store,String key,Class<T> clazz) {
        return SerializeHelper.deserialize(get(store, key),clazz);
	}
	
	public static byte[] get(RheaKVStore store,String key) {
		CompletableFuture<byte[]> f = store.get(key);
		return f.join();
	}
	
	public static <T> T pop(RheaKVStore store,String key,Class<T> clazz) {
		T obj = getObject(store,key, clazz);
		CompletableFuture<Boolean> delete = store.delete(key);
		if(delete.join()) {
			return obj;
		}
		return null;
	}
	
	public static <T> T popOneByPrefix(RheaKVStore store,String keyPrefix,Class<T> clazz) {
		RheaIterator<KVEntry> iterator = store.iterator(getMinKey(keyPrefix), getMaxKey(keyPrefix), 1);
		while(iterator.hasNext()) {
			KVEntry next = iterator.next();
			T obj = SerializeHelper.deserialize(next.getValue(),clazz);
			store.bDelete(next.getKey());
			return obj;
		}
		return null;
	}
	
	public static <T> Collection<T> popByPrefix(RheaKVStore store,String keyPrefix,Class<T> clazz,int nums) {
		RheaIterator<KVEntry> iterator = store.iterator(getMinKey(keyPrefix), getMaxKey(keyPrefix), nums);
		Collection<T> list = new ArrayList<T>();
		int count = 0;
		while(count < nums && iterator.hasNext()) {
			KVEntry next = iterator.next();
			T obj = SerializeHelper.deserialize(next.getValue(),clazz);
			list.add(obj);
			store.bDelete(next.getKey());
			count++;
		}
		return list.size()>0?list:null;	
	}
	
	public static DistributedLock<byte[]> getLock(RheaKVStore store,long lease,String lockKey) {
		return store.getDistributedLock(lockKey, lease, TimeUnit.SECONDS,Executors.newSingleThreadScheduledExecutor());
	}
	
	public static RheaKVStore initClient(RheaKVStoreOptions rheaKVConfig) {
		RheaKVStore store = new DefaultRheaKVStore();
        if (!store.init(rheaKVConfig)) {
            log.error("Fail to init [RheaKVStore]");
            System.exit(-1);
        }

        final List<RegionRouteTableOptions> regionRouteTableOptionsList = rheaKVConfig.getPlacementDriverOptions()
            .getRegionRouteTableOptionsList();

        rebalance(store, rheaKVConfig.getInitialServerList(), regionRouteTableOptionsList);
        log.info("initClient success. ClusterName {}  ",rheaKVConfig.getClusterName());
        return store;
	}

	public static Node initServer(RheaKVStoreOptions rheaKVConfig) {
		Node node = new Node(rheaKVConfig);
        node.start();
        log.info("initServer success. ClusterName {}  ",rheaKVConfig.getClusterName());
		return node;
	}
	
    // Because we use fake PD, so we need manual rebalance
    public static void rebalance(final RheaKVStore store, final String initialServerList,
                                 final List<RegionRouteTableOptions> regionRouteTableOptionsList) {
        final PlacementDriverClient pdClient = store.getPlacementDriverClient();
        final Configuration configuration = new Configuration();
        configuration.parse(initialServerList);
        final int serverSize = configuration.size();
        final int regionSize = regionRouteTableOptionsList.size();
        final int regionSizePerServer = regionSize / serverSize;
        final Queue<Long> regions = new ArrayDeque<>();
        for (final RegionRouteTableOptions r : regionRouteTableOptionsList) {
            regions.add(r.getRegionId());
        }
        final Map<PeerId, Integer> peerMap = Maps.newHashMap();
        for (;;) {
            final Long regionId = regions.poll();
            if (regionId == null) {
                break;
            }
            PeerId peerId;
            try {
                final Endpoint endpoint = pdClient.getLeader(regionId, true, 10000);
                if (endpoint == null) {
                    continue;
                }
                peerId = new PeerId(endpoint, 0);
                log.info("Region {} leader is {}", regionId, peerId);
            } catch (final Exception e) {
                regions.add(regionId);
                continue;
            }
            final Integer size = peerMap.get(peerId);
            if (size == null) {
                peerMap.put(peerId, 1);
                continue;
            }
            if (size < regionSizePerServer) {
                peerMap.put(peerId, size + 1);
                continue;
            }
            for (final PeerId p : configuration.listPeers()) {
                final Integer pSize = peerMap.get(p);
                if (pSize != null && pSize >= regionSizePerServer) {
                    continue;
                }
                try {
                    pdClient.transferLeader(regionId, JRaftHelper.toPeer(p), true);
                    log.info("Region {} transfer leader to {}", regionId, p);
                    regions.add(regionId);
                    break;
                } catch (final Exception e) {
                    log.error("Fail to transfer leader to {}", p);
                }
            }
        }

        for (final RegionRouteTableOptions r : regionRouteTableOptionsList) {
            final Long regionId = r.getRegionId();
            try {
                final Endpoint endpoint = pdClient.getLeader(regionId, true, 10000);
                log.info("Finally, the region: {} leader is: {}", regionId, endpoint);
            } catch (final Exception e) {
                log.error("Fail to get leader: {}", StackTraceUtil.stackTrace(e));
            }
        }
    }
}
