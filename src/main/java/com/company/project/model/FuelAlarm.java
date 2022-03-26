package com.company.project.model;

public enum FuelAlarm {

    // #################### 报警状态 start ####################
    /// 急停报警
    FUEL_STATE_81(81,"abort.alarm"),
    /// 注油电机启动错误报警
    FUEL_STATE_82(82,"oilpump.out.alarm"),
    /// 吸油电机启动错误报警
    FUEL_STATE_83(83,"oilpump.in.alarm"),
    /// 吸油循环堵塞报警
    FUEL_STATE_84(84,"oil.cycle.blocked.alarm"),
    /// 吸油回油堵塞报警
    FUEL_STATE_85(85,"oil.return.blocked.alarm"),
    /// 邮箱液位高报警
    FUEL_STATE_86(86,"oillevel.high.alarm"),
    /// 邮箱液位低报警
    FUEL_STATE_87(87,"oillevel.low.alarm"),
    /// 吸油压力传感器短线
    FUEL_STATE_88(88,"pressure.sensor.alarm");
    // #################### 报警状态 end ###################


    private int type;
    private String switchKey;

    private FuelAlarm(int type, String switchKey) {
        this.type = type;
        this.switchKey = switchKey;
    }

    // 普通方法
    public static String getSwitchKey(int type) {
        for (FuelAlarm c : FuelAlarm.values()) {
            if (c.getType() == type) {
                return c.switchKey;
            }
        }
        return null;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSwitchKey() {
        return switchKey;
    }

    public void setSwitchKey(String switchKey) {
        this.switchKey = switchKey;
    }
}
