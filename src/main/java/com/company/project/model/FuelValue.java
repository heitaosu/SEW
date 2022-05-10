package com.company.project.model;

// 操作页面上面的值
public enum FuelValue {

    // #################### 只读状态值 start ################
    /// 吸油压力当前值
    FUEL_STATE_61(61,"actual.oil.pressure"),
    /// 实时注油量
    FUEL_STATE_62(62,"actual.oil.volume"),
    /// 剩余注油量
    FUEL_STATE_63(63,"remaining.oil.volume"),
    // #################### 只读状态值 end ####################

    // #################### 可设置的值 start ################
    /// 吸油压力设定值
    FUEL_STATE_51(51,"expected.oil.pressure"),
    /// 注油流量偏差值 >> b
    FUEL_STATE_52(52,"oil.flow.deviation"),
    // 自动注油量设定值 >> 注油目标值
    FUEL_STATE_53(53,"expected.oil.volume"),
    // 注油流量系数  >> K
    FUEL_STATE_54(54,"expected.oil.coefficient");

    // #################### 可设置的值 end ################

    private int state;
    private String switchKey;

    private FuelValue(int state, String switchKey) {
        this.state = state;
        this.switchKey = switchKey;
    }

    // 普通方法
    public static String getSwitchKey(int state) {
        for (FuelValue c : FuelValue.values()) {
            if (c.getState() == state) {
                return c.switchKey;
            }
        }
        return null;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getSwitchKey() {
        return switchKey;
    }

    public void setSwitchKey(String switchKey) {
        this.switchKey = switchKey;
    }
}
