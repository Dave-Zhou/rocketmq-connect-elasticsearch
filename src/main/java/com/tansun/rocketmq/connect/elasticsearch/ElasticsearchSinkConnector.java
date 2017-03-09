package com.tansun.rocketmq.connect.elasticsearch;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.tansun.rocketmq.connect.comsumer.MessagePushConsumer;
import com.tansun.rocketmq.connect.config.RocketMQConfig;
import com.tansun.rocketmq.connect.tools.ConfigUtils;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * rocketmq comsumer数据存入es
 * <p>
 * Created by Dave on 2017/3/8.
 *
 * @version 1.0 2017/3/8
 * @autor zxd
 */
public class ElasticsearchSinkConnector implements Serializable, MessageListenerConcurrently {

    private static final long serialVersionUID = 7743521182524758725L;

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchSinkConnector.class);

    public static final String PROP_FILE_NAME = "mq.elasticsearch.default.prop";

    private String esHost;

    private String indexPrefix;

    private TransportClient client;

    private RocketMQConfig rocketMQConfig;

    private final String TYPE = "RocketMQ";

    private String indexSuffix;

    private void init() {
        try {
            Map<Object, Object> config = ConfigUtils.init(PROP_FILE_NAME);
            this.rocketMQConfig = (RocketMQConfig) config.get(ConfigUtils.CONFIG_ROCKETMQ);
            this.esHost = (String) config.get(ConfigUtils.ES_HOST);
            this.indexPrefix = (String) config.get(ConfigUtils.INDEX_PREFIX);
            this.indexSuffix = (String) config.get(ConfigUtils.INDEX_SUFFIX);
            Settings settings = Settings.builder()
                    .put("cluster.name", "elasticsearch").build();
            client = new PreBuiltTransportClient(settings);
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost), 9300));

            client
                    .admin()
                    .indices()
                    .preparePutTemplate("rocketmq_template")
                    .setTemplate(indexPrefix + "*")
                    .addMapping(TYPE, new HashMap<>())
                    .get();
            start(this.rocketMQConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        ElasticsearchSinkConnector connector = new ElasticsearchSinkConnector();
        connector.init();
    }

    public void start(RocketMQConfig config) {
        try {
            config.setInstanceName(Thread.currentThread().getId() + Thread.currentThread().getName());
            MessagePushConsumer consumer = new MessagePushConsumer(config);
            consumer.start(this);
        } catch (Exception e) {
            LOG.error("Failed to init consumer !", e);
            throw new RuntimeException(e);
        }
    }

    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                    ConsumeConcurrentlyContext context) {
        Map<String,Object> data = new HashMap<>();
        try {
            for (MessageExt msg : msgs) {
                data.put("content",new String(msg.getBody(),"utf-8"));
                client
                        .prepareIndex(indexPrefix+indexSuffix, TYPE)
                        .setSource(data)
                        .get();
            }
        } catch (Exception e) {
            LOG.error("Failed to read message {}!");
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
