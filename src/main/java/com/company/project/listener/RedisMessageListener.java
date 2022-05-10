package com.company.project.listener;

import com.company.project.core.ProjectConstant;
import com.company.project.model.FuelAlarm;
import com.company.project.model.FuelState;
import com.company.project.model.FuelValue;
import com.company.project.util.DateUtil;
import com.company.project.websocket.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 监听redis消息队列的数据变化
 */
@Component
public class RedisMessageListener {

    @Autowired
    private PushService pushService;

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter,MessageListenerAdapter listenerAlarmAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(ProjectConstant.BTN_EVENT_CHANNEL));
        container.addMessageListener(listenerAlarmAdapter, new PatternTopic(ProjectConstant.ALARM_EVENT_CHANNEL));
        return container;
    }

    /**
     * 绑定消息监听者和接收监听的方法,必须要注入这个监听器，不然会报错
     */
    @Bean
    public MessageListenerAdapter listenerAlarmAdapter(){
        return new MessageListenerAdapter(new AlarmReceiver(),"receiveMessage");
    }

    /**
     * 输入值 e.g:
     *       alert.resettart.btn:True
     *       expected.oil.pressure:888
     *
     * 输出值 e.g:
     *       btn_1_1
     *       val_1_888
     */
    // alarm_1_2022-03-22 12:00:00 抽油油箱液位低报警
    class AlarmReceiver {
        public void receiveMessage(String message) {
            try {
                //去掉引号
                message = message.replace("\"", "");
                String[] msgs = message.split(":");
                //globalCache.set(msgs[0],msgs[1]);
                for (FuelAlarm alarm : FuelAlarm.values()){
                    if (alarm.getSwitchKey().equals(msgs[0])){
                        if (Boolean.valueOf(msgs[1])){
                            String dateStr = DateUtil.DateToString(new Date(),DateUtil.DateStyle.YYYY_MM_DD_HH_MM_SS) + " " + alarm.getMsg();
                            String msg = String.format(ProjectConstant.VAL_ARRAY[0], ""+alarm.getType(),dateStr);
                            pushService.pushMsgToAll(msg);
                        }
                    }
                }
                for (FuelValue val : FuelValue.values()){
                    if (val.getSwitchKey().equals(msgs[0])){
                        String msg = String.format(ProjectConstant.VAL_ARRAY[2], val.getState(),msgs[1]);
                        pushService.pushMsgToAll(msg);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Bean
    public MessageListenerAdapter listenerAdapter(){
        return new MessageListenerAdapter(new Receiver(),"receiveMessage");
    }

    /**
     * 输入值 e.g:
     *       alert.resettart.btn:True
     *       expected.oil.pressure:888
     *
     * 输出值 e.g:
     *       btn_1_1
     *       val_1_888
     */
    class Receiver {
        public void receiveMessage(String message) {
            //去掉引号
            message = message.replace("\"", "");
            String[] msgs = message.split(":");
            for (FuelState btn : FuelState.values()){
                if (btn.getSwitchKey().equals(msgs[0])){
                    String v = Boolean.valueOf(msgs[1]) ?  "1" : "0";
                    String msg = String.format(ProjectConstant.VAL_ARRAY[1], btn.getState(),v);
                    pushService.pushMsgToAll(msg);
                }
            }
        }
    }
}
