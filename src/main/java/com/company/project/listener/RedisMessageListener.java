package com.company.project.listener;

import com.company.project.core.IGlobalCache;
import com.company.project.core.ProjectConstant;
import com.company.project.model.FuelAlarm;
import com.company.project.model.FuelState;
import com.company.project.model.FuelValue;
import com.company.project.model.WarnRecord;
import com.company.project.service.WarnRecordService;
import com.company.project.util.DateUtil;
import com.company.project.websocket.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 监听redis消息队列的数据变化
 */
@Slf4j
@Component
public class RedisMessageListener {

    @Autowired
    private PushService pushService;

    @Autowired
    private WarnRecordService warnRecordService;

    @Autowired
    private IGlobalCache globalCache;

    public static NumberFormat nf = NumberFormat.getNumberInstance();

    private ExecutorService threadPool = Executors.newFixedThreadPool(10);

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
                    //log.error("AlarmReceiver message" + message);
                    if (alarm.getSwitchKey().equals(msgs[0])){
                        //if (Boolean.valueOf(msgs[1])){
                            threadPool.execute(new Runnable() {
                                public void run() {
                                    try {
                                        String dateStr = DateUtil.DateToString(new Date(),DateUtil.DateStyle.YYYY_MM_DD_HH_MM_SS) + " " + alarm.getMsg();
                                        String msg = String.format(ProjectConstant.VAL_ARRAY[0], ""+alarm.getType(),dateStr);
                                        pushService.pushMsgToAll(msg);
                                        //告警记录入库
                                        WarnRecord warnRecord = new WarnRecord();
                                        warnRecord.setWarnType(alarm.getType());
                                        warnRecord.setCreateTime(System.currentTimeMillis());
                                        warnRecord.setWarnMsg(dateStr);
                                        warnRecordService.save(warnRecord);
                                    } catch (Exception e) {
                                        log.error("threadPool.execute error");
                                    }
                                }
                            });
                       // }
                    }
                }

                for (FuelValue val : FuelValue.values()){
                    if (val.getSwitchKey().equals(msgs[0])){

                    /*Object msv =  msgs[1];
                    nf.setMaximumFractionDigits(3);


                    Object obj = globalCache.get(FuelState.PAGE_CODE_205.getSwitchKey());
                    if (obj != null){
                        Double dval = null;
                        dval = Double.valueOf(obj.toString());

                        //注油量实际值
                        dval = Double.valueOf(nf.format(dval));
                        globalCache.set(FuelState.PAGE_CODE_205.getSwitchKey(),dval);
                    }
*/

                        String msg = String.format(ProjectConstant.VAL_ARRAY[2], val.getState(),msgs[1]);
                        //log.error("msg={},State={}",msg,val.getState());
                        pushService.pushMsgToAll(msg);
                        if (FuelValue.FUEL_STATE_61.getState() != val.getState()){
                        }
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
