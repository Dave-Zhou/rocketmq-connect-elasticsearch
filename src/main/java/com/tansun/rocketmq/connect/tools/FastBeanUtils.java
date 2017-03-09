/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.tansun.rocketmq.connect.tools;

import com.google.common.base.Preconditions;
import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.core.Converter;
import net.sf.cglib.core.ReflectUtils;

/**
 * High performance JavaBean attribute copy tool
 *
 * @author von gosling 2011-12-24 3:58:02
 */
public abstract class FastBeanUtils {
    public static Object copyProperties(Object source, Class<?> target) {
        Preconditions.checkNotNull(source, "Source must not be null!");
        Preconditions.checkNotNull(target, "Target must not be null!");

        Object targetObject = ReflectUtils.newInstance(target);
        BeanCopier beanCopier = BeanCopier.create(source.getClass(), target, false);
        beanCopier.copy(source, targetObject, null);

        return targetObject;
    }

    public static Object copyProperties(Object source, Class<?> target, Converter converter) {
        Preconditions.checkNotNull(source, "Source must not be null");
        Preconditions.checkNotNull(target, "Target must not be null");

        Object targetObject = ReflectUtils.newInstance(target);
        BeanCopier beanCopier = BeanCopier.create(source.getClass(), target, true);
        beanCopier.copy(source, targetObject, converter);

        return targetObject;
    }

    private FastBeanUtils() {
    }
}
