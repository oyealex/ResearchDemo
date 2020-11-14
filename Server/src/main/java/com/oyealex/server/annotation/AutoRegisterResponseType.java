/*
 * Copyright (c) 2020. oyealex. All rights reserved.
 */

package com.oyealex.server.annotation;

import java.lang.annotation.*;

/**
 * TODO 2020/8/30 The Target
 *
 * @author oyealex
 * @version 1.0
 * @since 2020/8/30
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoRegisterResponseType {
}
