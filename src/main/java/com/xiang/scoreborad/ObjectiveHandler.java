package com.xiang.scoreborad;

/**
 * 计分项处理器接口 (用于更新分数)
 */
public interface ObjectiveHandler {
    /**
     * 更新计分项的分数
     *
     * @param objective   被更新的计分项
     * @param cycle       当前的周期
     */
    void onObjectiveUpdate(BetterObjective objective, int cycle);

    /**
     * 获取最大周期
     * @return 周期长度 最大周期
     */
    int getMaxCycle();
}
