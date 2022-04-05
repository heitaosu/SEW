package com.company.project.model;

/**
 * 操作页面上的告警
 */
public enum FuelAlarm {

    // #################### 报警状态 start ####################
    /// 注油电机启动错误报警
    FUEL_STATE_82(82,"oilpump.out.alarm","注油电机启动错误报警"),
    /// 吸油电机启动错误报警
    FUEL_STATE_83(83,"oilpump.in.alarm","吸油电机启动错误报警"),
    /// 吸油循环堵塞报警
    FUEL_STATE_84(84,"oil.cycle.blocked.alarm","吸油循环堵塞报警"),
    /// 吸油回油堵塞报警
    FUEL_STATE_85(85,"oil.return.blocked.alarm","吸油回油堵塞报警"),
    /// 油箱液位高报警
    FUEL_STATE_86(86,"oillevel.high.alarm","注油油箱液位高报警"),
    /// 油箱液位低报警
    FUEL_STATE_87(87,"oillevel.low.alarm","注油油箱液位低报警"),
    /// 抽油邮箱液位高报警
    FUEL_STATE_88(88,"v10_7","抽油邮箱液位高报警"),
    FUEL_STATE_89(89,"v11_0","抽油邮箱液位低报警"),
    FUEL_STATE_90(90,"v11_1","吸油压力传感器断线报警"),
    FUEL_STATE_91(91,"v11_2","吸油滤油器堵塞报警");
    // #################### 报警状态 end ###################

    private int type;
    private String switchKey;
    private String msg;

    private FuelAlarm(int type, String switchKey,String msg) {
        this.type = type;
        this.switchKey = switchKey;
        this.msg = msg;
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

    // 普通方法
    public static String getMsg(int type) {
        for (FuelAlarm c : FuelAlarm.values()) {
            if (c.getType() == type) {
                return c.msg;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
