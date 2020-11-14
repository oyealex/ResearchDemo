package com.oyealex.server.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author oye
 * @since 2020-05-17 09:01:53
 */
@Slf4j
public class MsgTest {

    public static void main(String[] args) {
        MsgManager.getInstance().registerHandlers(
                new NewSubTaskMsgHandler(),
                new SubTaskEventMsgHandler());

        String newMsg = MsgManager.getInstance().toJson(new Msg("NEW_SUB_TASK",
                MsgManager.getInstance().toJson(new SubTask(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                        "/path/to/src", "/path/to/dest")), null));
        String eventMsg = MsgManager.getInstance().toJson(new Msg("SUB_TASK_EVENT",
                MsgManager.getInstance().toJson(new SubTaskEvent(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                        "complete")), LocalDateTime.now()));
        log.info("{}", newMsg);
        log.info("{}", eventMsg);

        MsgManager.getInstance().handleMsg(newMsg);
        MsgManager.getInstance().handleMsg(eventMsg);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class SubTask {
        private String id;
        private String baseTaskId;
        private String srcPath;
        private String destPath;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class SubTaskEvent {
        private String id;
        private String baseTaskId;
        private String event;
    }

    @Slf4j
    private static class NewSubTaskMsgHandler extends AbstractMsgHandler<SubTask> {

        protected NewSubTaskMsgHandler() {
            super("NEW_SUB_TASK", SubTask.class);
        }

        @Override
        protected void handleData(SubTask data, LocalDateTime timestamp) {
            log.info("handle sub task: {}, {}", data, timestamp);
        }
    }

    @Slf4j
    private static class SubTaskEventMsgHandler extends AbstractMsgHandler<SubTaskEvent> {

        protected SubTaskEventMsgHandler() {
            super("SUB_TASK_EVENT", SubTaskEvent.class);
        }

        @Override
        protected void handleData(SubTaskEvent data, LocalDateTime timestamp) {
            log.info("handle sub task event: {}, {}", data, timestamp);
        }
    }


}
