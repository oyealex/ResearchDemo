package com.oyealex.server.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author oye
 * @since 2020-05-16 20:29:59
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Msg {

    private String msgType;
    private String data;
    private LocalDateTime timestamp;

}
