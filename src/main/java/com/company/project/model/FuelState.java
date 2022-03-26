package com.company.project.model;

public enum FuelState {

    // #################### 按钮操作 start ##############
    /// 手动加油启动按钮
    FUEL_STATE_11(11,"manual.start.btn"),
    /// 手动加油停止按钮
    FUEL_STATE_12(12,"manual.stop.btn"),
    /// 自动加油停止按钮
    FUEL_STATE_13(13,"auto.start.btn"),
    /// 扫码加油停止按钮
    FUEL_STATE_14(14,"scan.start.btn"),
    /// 报警复位按钮
    FUEL_STATE_15(15,"alert.resettart.btn"),
    /// 循环OR注油开关
    FUEL_STATE_16(16,"loop.switch"),
    /// 消音按钮
    FUEL_STATE_17(17,"mute.btn"),
    // #################### 按钮操作 end ##############

    // #################### 可设置的值 start ################
    /// 吸油压力设定值
    FUEL_STATE_51(51,"expected.oil.pressure"),
    /// 注油流量偏差值
    FUEL_STATE_52(52,"oil.flow.deviation"),
    /// 自动注油量设定值
    FUEL_STATE_53(53,"expected.oil.volume"),
    // #################### 可设置的值 end ################


    // #################### 只读状态值 start ################
    /// 吸油压力当前值
    FUEL_STATE_61(61,"actual.oil.pressure"),
    /// 实时注油量
    FUEL_STATE_62(62,"actual.oil.volume"),
    /// 剩余注油量
    FUEL_STATE_63(63,"remaining.oil.volume");
    // #################### 只读状态值 end ####################

    private int state;
    private String switchKey;

    private FuelState(int state, String switchKey) {
        this.state = state;
        this.switchKey = switchKey;
    }

    // 普通方法
    public static String getSwitchKey(int state) {
        for (FuelState c : FuelState.values()) {
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
