package com.pi4j.test.provider.impl;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: UNITTEST :: Unit/Integration Tests
 * FILENAME      :  TestDigitalInputProviderImpl.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2019 Pi4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalInputProviderBase;
import com.pi4j.test.provider.TestDigitalInput;
import com.pi4j.test.provider.TestDigitalInputProvider;

public class TestDigitalInputProviderImpl extends DigitalInputProviderBase implements TestDigitalInputProvider {

    public TestDigitalInputProviderImpl(){ super(); }

    public TestDigitalInputProviderImpl(String id){
        super(id);
    }

    public TestDigitalInputProviderImpl(String id, String name){
        super(id, name);
    }

    @Override
    public DigitalInput create(DigitalInputConfig config) throws Exception {
        return new TestDigitalInput(this, config);
    }
}