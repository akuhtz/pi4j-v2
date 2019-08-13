package com.pi4j.library.pigpio.test;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: PIGPIO Library
 * FILENAME      :  HardwareHarnessTest.java
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

import com.fazecast.jSerialComm.SerialPort;
import com.pi4j.library.pigpio.PiGpio;
import com.pi4j.library.pigpio.PiGpioState;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.Scanner;

import static com.fazecast.jSerialComm.SerialPort.FLOW_CONTROL_DISABLED;
import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_SEMI_BLOCKING;


@TestMethodOrder(OrderAnnotation.class)
public class TestDigitalOutputs {

    @Before
    public void beforeTest() {
    }

    @After
    public void afterTest() {
    }

    @Test
    @Timeout(5) // seconds
    @Order(1)
    public void testGpioDigitalOutputHigh() throws IOException, InterruptedException {

        PiGpio pig = PiGpio.newSocketInstance("rpi3bp");

        // get port instance
        SerialPort port = SerialPort.getCommPort("tty.usbmodem142301");

        // configure port
        port.setBaudRate(115200);
        port.setNumDataBits(8);
        port.setNumStopBits(1);
        port.setParity(0);
        port.setFlowControl(FLOW_CONTROL_DISABLED);

        // configure read timeout
        port.setComPortTimeouts(TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        // open serial port
        port.openPort();

        // test serial port was opened
        // if the port is not open, then fail the test
        if(!port.isOpen()) {
            Assert.fail("Serial port to test harness failed.");
            return;
        }

        // send the "info" command to the test harness
        port.getOutputStream().write("pin 2 input\r\n".getBytes());
        port.getOutputStream().flush();

        pig.gpioWrite(2, PiGpioState.HIGH);

        // send the "info" command to the test harness
        port.getOutputStream().write("pin 2\r\n".getBytes());
        port.getOutputStream().flush();

        Scanner in = new Scanner(port.getInputStream());
        while(in.hasNextLine()){
            var received  = in.nextLine();
            System.out.println(received);

            JSONObject payload = new JSONObject(received);
            if(payload.has("id") && payload.has("pin") &&
               payload.has("mode") && payload.has("value")){

               if(payload.getString("id").equalsIgnoreCase("get") &&
                  payload.getInt("pin") == 2  &&
                  payload.getString("mode").equalsIgnoreCase("input") ) {
                   int value = payload.getInt("value");
                   Assert.assertEquals("PIN VALUE MISMATCH", PiGpioState.HIGH.value(), value);
                   port.closePort();
                   return;
               }
            }
        }

        // test failed
        Assert.fail("No 'info' response received from test harness.");

        // close port when done
        port.closePort();
    }


    @Test
    @Timeout(5) // seconds
    @Order(2)
    public void testGpioDigitalOutputLow() throws IOException, InterruptedException {

        PiGpio pig = PiGpio.newSocketInstance("rpi3bp");

        // get port instance
        SerialPort port = SerialPort.getCommPort("tty.usbmodem142301");

        // configure port
        port.setBaudRate(115200);
        port.setNumDataBits(8);
        port.setNumStopBits(1);
        port.setParity(0);
        port.setFlowControl(FLOW_CONTROL_DISABLED);

        // configure read timeout
        port.setComPortTimeouts(TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        // open serial port
        port.openPort();

        // test serial port was opened
        // if the port is not open, then fail the test
        if(!port.isOpen()) {
            Assert.fail("Serial port to test harness failed.");
            return;
        }

        // send the "info" command to the test harness
        port.getOutputStream().write("pin 2 input\r\n".getBytes());
        port.getOutputStream().flush();

        pig.gpioWrite(2, PiGpioState.LOW);

        // send the "info" command to the test harness
        port.getOutputStream().write("pin 2\r\n".getBytes());
        port.getOutputStream().flush();

        Scanner in = new Scanner(port.getInputStream());
        while(in.hasNextLine()){
            var received  = in.nextLine();
            System.out.println(received);

            JSONObject payload = new JSONObject(received);
            if(payload.has("id") && payload.has("pin") &&
                    payload.has("mode") && payload.has("value")){

                if(payload.getString("id").equalsIgnoreCase("get") &&
                        payload.getInt("pin") == 2  &&
                        payload.getString("mode").equalsIgnoreCase("input") ) {
                    int value = payload.getInt("value");
                    Assert.assertEquals("PIN VALUE MISMATCH", PiGpioState.LOW.value(), value);
                    port.closePort();
                    return;
                }
            }
        }

        // test failed
        Assert.fail("No 'info' response received from test harness.");

        // close port when done
        port.closePort();
    }

}
