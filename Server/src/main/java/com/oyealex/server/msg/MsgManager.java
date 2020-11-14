package com.oyealex.server.msg;

import com.google.gson.*;
import com.oyealex.server.util.BlankUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author oye
 * @since 2020-05-16 20:31:03
 */
@Slf4j
public class MsgManager {

    @Getter
    private static final MsgManager instance = new MsgManager();

    private static final Gson gson;

    private static final DateTimeFormatter dateTimeFormatter;

    static {
        gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .disableInnerClassSerialization()
                .disableHtmlEscaping()
                .create();
        dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    private MsgManager() {
    }

    private final Map<String, AbstractMsgHandler<?>> handlerMap = new ConcurrentHashMap<>();

    public void registerHandlers(AbstractMsgHandler<?>... handlers) {
        if (handlers == null || handlers.length == 0) {
            log.error("no msg handler provide");
            return;
        }
        for (AbstractMsgHandler<?> handler : handlers) {
            if (handler != null) {
                handlerMap.put(handler.getMsgType(), handler);
            }
        }
    }

    public void handleMsg(String msgStr) {
        if (BlankUtil.isBlank(msgStr)) {
            return;
        }

        Msg msg;
        try {
            msg = gson.fromJson(msgStr, Msg.class);
        } catch (Exception ex) {
            log.error("parse msg failed, {}", msgStr, ex);
            return;
        }

        if (BlankUtil.isBlank(msg.getMsgType())) {
            return;
        }
        AbstractMsgHandler<?> handler = handlerMap.get(msg.getMsgType());
        if (handler != null) {
            handler.handle(msg);
        }
    }

    public <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    public String toJson(Object data) {
        return gson.toJson(data);
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String jsonValue;
            if (json == null || BlankUtil.isBlank(jsonValue = json.getAsString())) {
                return null;
            }
            return LocalDateTime.from(dateTimeFormatter.parse(jsonValue));
        }

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? null : new JsonPrimitive(dateTimeFormatter.format(src));
        }
    }

}
