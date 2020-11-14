package com.oyealex.server.msg;

import com.oyealex.server.util.BlankUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author oye
 * @since 2020-05-17 09:10:20
 */
@Slf4j
public abstract class AbstractMsgHandler<T> {

    @Getter
    private final String msgType;

    @Getter
    private final Class<T> type;

    protected AbstractMsgHandler(String msgType, Class<T> type) {
        if (BlankUtil.isBlank(msgType) || type == null) {
            throw new IllegalArgumentException("blank msg type or data type class");
        }
        this.msgType = msgType;
        this.type = type;
    }

    public void handle(Msg msg) {
        if (msg == null || BlankUtil.isBlank(msg.getData())) {
            return;
        }

        T data;
        String dataStr = msg.getData();
        try {
            data = MsgManager.getInstance().fromJson(dataStr, type);
        } catch (Exception ex) {
            log.error("parse json failed, {}", dataStr, ex);
            return;
        }

        handleData(data, msg.getTimestamp());
    }

    protected abstract void handleData(T data, LocalDateTime timestamp);

}
