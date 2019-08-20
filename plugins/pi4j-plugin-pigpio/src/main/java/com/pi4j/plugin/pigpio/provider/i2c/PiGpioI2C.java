package com.pi4j.plugin.pigpio.provider.i2c;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: PLUGIN   :: PIGPIO I/O Providers
 * FILENAME      :  PiGpioI2C.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2019 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.io.exception.IOReadException;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CBase;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import com.pi4j.library.pigpio.PiGpio;
import com.pi4j.library.pigpio.PiGpioMode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class PiGpioI2C extends I2CBase implements I2C {

    protected final PiGpio piGpio;
    protected final int handle;

    public PiGpioI2C(PiGpio piGpio, I2CProvider provider, I2CConfig config) throws IOException {
        super(provider, config);

        // set local reference instance
        this.piGpio = piGpio;

        // set pin ALT0 modes for I2C BUS<1> or BUS<2> usage on RPI3B
        switch(config.bus()) {
            case 0: {
                piGpio.gpioSetMode(0, PiGpioMode.ALT0);
                piGpio.gpioSetMode(1, PiGpioMode.ALT0);
                break;
            }
            case 1: {
                piGpio.gpioSetMode(2, PiGpioMode.ALT0);
                piGpio.gpioSetMode(3, PiGpioMode.ALT0);
            }
        }

        // create I2C instance of PIGPIO I2C
        this.handle = piGpio.i2cOpen(config.bus(), config.device());

        // set open state flag
        this.isOpen = true;
    }

    @Override
    public I2C initialize(Context context) throws InitializeException {
        super.initialize(context);
        return this;
    }

    @Override
    public void close() throws IOException {
        piGpio.i2cClose(this.handle);
        super.close();
    }

    // -------------------------------------------------------------------
    // RAW DEVICE WRITE FUNCTIONS
    // -------------------------------------------------------------------

    @Override
    public void write(byte b) throws IOException {
        piGpio.i2cWriteByte(this.handle, b);;
    }

    @Override
    public int write(ByteBuffer buffer, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, buffer.capacity());
        piGpio.i2cWriteDevice(this.handle, buffer, offset, length);
        return length;
    }

    // -------------------------------------------------------------------
    // RAW DEVICE READ FUNCTIONS
    // -------------------------------------------------------------------

    @Override
    public int read() throws IOException{
        return piGpio.i2cReadByte(this.handle);
    }

    @Override
    public int read(ByteBuffer buffer, int offset, int length) throws IOException{
        Objects.checkFromIndexSize(offset, length, buffer.capacity());
        byte[] data = piGpio.i2cReadDevice(this.handle, length);
        if(data.length > 0)
            buffer.put(data, offset, length);
        return length;
    }

    // -------------------------------------------------------------------
    // DEVICE REGISTER WRITE FUNCTIONS
    // -------------------------------------------------------------------

    @Override
    public void writeRegister(int register, byte b) throws IOException {
        piGpio.i2cWriteByteData(this.handle, register, b);
    }

    @Override
    public int writeRegister(int register, ByteBuffer buffer, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, buffer.capacity());
        piGpio.i2cWriteI2CBlockData(this.handle, register, buffer, offset, length);
        return length;
    }

    // -------------------------------------------------------------------
    // DEVICE REGISTER READ FUNCTIONS
    // -------------------------------------------------------------------

    @Override
    public int readRegister(int register) throws IOException {
        return piGpio.i2cReadByteData(this.handle, register);
    }

    @Override
    public int readRegister(int register, ByteBuffer buffer, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, buffer.capacity());
        byte data[] = piGpio.i2cReadI2CBlockData(this.handle, register, length);
        if(data.length > 0)
            buffer.put(data, offset, length);
        return length;
    }

    @Override
    public int writeReadRegisterWord(int register, int word) throws IOException, IOReadException {
        return piGpio.i2cProcessCall(this.handle, register, word);
    }
}