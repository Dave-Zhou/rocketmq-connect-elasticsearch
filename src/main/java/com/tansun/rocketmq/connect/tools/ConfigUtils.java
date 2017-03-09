/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.tansun.rocketmq.connect.tools;


import com.tansun.rocketmq.connect.config.RocketMQConfig;
import org.apache.commons.lang3.BooleanUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Utilities for Elasticsearch connector read from rocketmq.
 */
public abstract class ConfigUtils {

    public static final String ES_HOST = "es.host";

    public static final String INDEX_PREFIX = "es.index.prefix";

    public static final String INDEX_SUFFIX = "es.index.suffix";

    public static final String ES_CLUSTER_NAME = "es.cluster.name";

    public static final String CONFIG_NAMESRV_ADDR = "rocketmq.elasticsearch.namesrv.addr";
    /**
     * elasticsearch configuration key used to determine the rocketmq topic to read from
     * ( {@code "rocketmq.spout.topic"}).
     */
    public static final String CONFIG_TOPIC = "rocketmq.elasticsearch.topic";
    /**
     * Default rocketmq topic to read from ({@code "rocketmq_elasticsearch_topic"}).
     */
    public static final String CONFIG_DEFAULT_TOPIC = "rocketmq_elasticsearch_topic";
    /**
     * elasticsearch configuration key used to determine the rocketmq consumer group (
     * {@code "rocketmq.spout.consumer.group"}).
     */
    public static final String CONFIG_CONSUMER_GROUP = "rocketmq.elasticsearch.consumer.group";
    /**
     * Default rocketmq consumer group id (
     * {@code "rocketmq_elasticsearch_consumer_group"}).
     */
    public static final String CONFIG_DEFAULT_CONSUMER_GROUP = "rocketmq_elasticsearch_consumer_group";
    /**
     * elasticsearch configuration key used to determine the rocketmq topic tag(
     * {@code "rocketmq.elasticsearch.topic.tag"}).
     */
    public static final String CONFIG_TOPIC_TAG = "rocketmq.elasticsearch.topic.tag";

    public static final String CONFIG_ROCKETMQ = "rocketmq.config";

    public static final String CONFIG_PREFETCH_SIZE = "rocketmq.prefetch.size";

    /**
     * Reads configuration from a classpath resource stream obtained from the
     * current thread's class loader through
     * {@link ClassLoader#getSystemResourceAsStream(String)}.
     *
     * @param resource The resource to be read.
     * @return A {@link Properties} object read from the specified
     * resource.
     * @throws IllegalArgumentException When the configuration file could not be
     *                                  found or another I/O error occurs.
     */
    public static Properties getResource(final String resource) {
        InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resource);
        if (input == null) {
            throw new IllegalArgumentException("configuration file '" + resource
                    + "' not found on classpath");
        }

        final Properties config = new Properties();
        try {
            config.load(input);
        } catch (final IOException e) {
            throw new IllegalArgumentException("reading configuration from '" + resource
                    + "' failed", e);
        }
        return config;
    }

    public static Map<Object,Object> init(String configFile) {
        Map<Object,Object> config = new HashMap<>();
        //1.
        Properties prop = ConfigUtils.getResource(configFile);
        for (Entry<Object, Object> entry : prop.entrySet()) {
            config.put((String) entry.getKey(), entry.getValue());
        }
        //2.
        String namesrvAddr = (String) config.get(ConfigUtils.CONFIG_NAMESRV_ADDR);
        String topic = (String) config.get(ConfigUtils.CONFIG_TOPIC);
        String consumerGroup = (String) config.get(ConfigUtils.CONFIG_CONSUMER_GROUP);
        String topicTag = (String) config.get(ConfigUtils.CONFIG_TOPIC_TAG);
        //Integer pullBatchSize = (Integer) config.get(ConfigUtils.CONFIG_PREFETCH_SIZE);
        Integer pullBatchSize = Integer.parseInt((String) config.get(ConfigUtils.CONFIG_PREFETCH_SIZE));

        RocketMQConfig mqConfig = new RocketMQConfig(namesrvAddr, consumerGroup, topic, topicTag);

        if (pullBatchSize != null && pullBatchSize > 0) {
            mqConfig.setPullBatchSize(pullBatchSize);
        }

        boolean ordered = BooleanUtils.toBooleanDefaultIfNull(
                Boolean.valueOf((String) (config.get("rocketmq.elasticsearch.ordered"))), false);
        mqConfig.setOrdered(ordered);

        config.put(CONFIG_ROCKETMQ, mqConfig);

        return config;
    }

    private ConfigUtils() {
    }
}
