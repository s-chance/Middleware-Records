package org.entropy.common.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class MultiDelayMessage<T> {
    /**
     * 消息体
     */
    private T data;
    /**
     * 记录延时时间的集合
     */
    private List<Long> delayMillis;

    public MultiDelayMessage(T data, List<Long> delayMillis) {
        this.data = data;
        this.delayMillis = delayMillis;
    }

    public static <T> MultiDelayMessage<T> of(T data, Long... delayMillis) {
        return new MultiDelayMessage<>(data, new ArrayList<>(Arrays.asList(delayMillis)));
    }


    /**
     * 获取并移除下一个延时时间
     *
     * @return 队列中的第一个延时时间
     */
    public Long removeNextDelay() {
        return delayMillis.remove(0);
    }


    /**
     * 是否还有下一个延时时间
     */
    public boolean hasNextDelay() {
        return !delayMillis.isEmpty();
    }
}
