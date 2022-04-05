package com.company.project.model;

/**
 * 操作页面上的按钮
 */
public enum FuelState {

    // #################### 按钮操作 start ##############
    /// 手动加油启动按钮
    FUEL_STATE_11(11,"manual.start.btn"),
    /// 手动加油停止按钮
    FUEL_STATE_12(12,"manual.stop.btn"),
    /// 扫码加油开始按钮
    FUEL_STATE_13(13,"scan.start.btn"),
    /// 扫码加油停止按钮
    FUEL_STATE_14(14,"auto.start.btn"),
    /// 报警复位按钮
    FUEL_STATE_15(15,"alert.resettart.btn"),
    /// 循环OR注油开关
    FUEL_STATE_16(16,"loop.switch"),
    /// 消音按钮
    FUEL_STATE_17(17,"mute.btn");


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
