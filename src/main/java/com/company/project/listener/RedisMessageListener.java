package com.company.project.listener;

import com.company.project.core.IGlobalCache;
import com.company.project.websocket.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class RedisMessageListener {

    @Autowired
    private IGlobalCache globalCache;

    @Autowired
    private PushService pushService;

    private final String BTN_EVENT_CHANNEL = "btn.event.channel";
    private final String ALARM_EVENT_CHANNEL = "alarm.event.channel";

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter,MessageListenerAdapter listenerAlarmAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(BTN_EVENT_CHANNEL));
        container.addMessageListener(listenerAlarmAdapter, new PatternTopic(ALARM_EVENT_CHANNEL));
        return container;
    }

    /**
     * 绑定消息监听者和接收监听的方法,必须要注入这个监听器，不然会报错
     */
    @Bean
    public MessageListenerAdapter listenerAlarmAdapter(){
        return new MessageListenerAdapter(new AlarmReceiver(),"receiveMessage");
    }

    class AlarmReceiver {
        public void receiveMessage(String message) {
            try {
                message = message.replace("\"", "");
                String[] msgs = message.split(":");
                System.out.println("message=" + message);
                globalCache.set(msgs[0],msgs[1]);
                // 通知前端告警信息变化
                pushService.pushMsgToAll(ALARM_EVENT_CHANNEL + ":" +msgs[1]);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(){
        return new MessageListenerAdapter(new Receiver(),"receiveMessage");
    }

    class Receiver {
        public void receiveMessage(String message) {
            System.out.format("Received <%s>%n",message);
        }
    }

}
