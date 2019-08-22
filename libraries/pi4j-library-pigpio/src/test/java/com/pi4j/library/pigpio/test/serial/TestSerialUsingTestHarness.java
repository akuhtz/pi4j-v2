package com.pi4j.library.pigpio.test.serial;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: PIGPIO Library
 * FILENAME      :  TestSerialUsingTestHarness.java
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

import com.pi4j.library.pigpio.PiGpio;
import com.pi4j.library.pigpio.PiGpioMode;
import com.pi4j.library.pigpio.util.StringUtil;
import com.pi4j.test.harness.ArduinoTestHarness;
import com.pi4j.test.harness.TestHarnessInfo;
import com.pi4j.test.harness.TestHarnessPins;
import org.junit.Assert;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Random;
import java.util.UUID;

@DisplayName("PIGPIO Library :: Test Serial Communication")
public class TestSerialUsingTestHarness {

    private static String SERIAL_DEVICE = "/dev/ttyS0";
    private static int BAUD_RATE = 9600;
    private static int TEST_HARNESS_UART = 3;

    private PiGpio pigpio;
    private int handle;

    @BeforeAll
    public static void initialize() {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");

        System.out.println();
        System.out.println("************************************************************************");
        System.out.println("INITIALIZE TEST (" + TestSerialUsingTestHarness.class.getName() + ")");
        System.out.println("************************************************************************");
        System.out.println();

        try {
            // create test harness and PIGPIO instances
            ArduinoTestHarness harness = new ArduinoTestHarness(System.getProperty("pi4j.test.harness.port", "tty.usbmodem142301"));

            // initialize test harness and PIGPIO instances
            harness.initialize();

            // get test harness info
            TestHarnessInfo info = harness.getInfo();
            System.out.println("... we are connected to test harness:");
            System.out.println("----------------------------------------");
            System.out.println("NAME       : " + info.name);
            System.out.println("VERSION    : " + info.version);
            System.out.println("DATE       : " + info.date);
            System.out.println("COPYRIGHT  : " + info.copyright);
            System.out.println("----------------------------------------");

//            // reset all pins on test harness before proceeding with this test
            TestHarnessPins reset = harness.reset();
            System.out.println();
            System.out.println("RESET ALL PINS ON TEST HARNESS; (" + reset.total + " pin reset)");

            // enable the Serial Echo (Loopback) function on the test harness for these tests
            harness.enableSerialEcho(TEST_HARNESS_UART,  BAUD_RATE);

            // terminate connection to test harness
            harness.terminate();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void terminate() throws IOException {
        System.out.println();
        System.out.println("************************************************************************");
        System.out.println("TERMINATE TEST (" + TestSerialUsingTestHarness.class.getName() + ") ");
        System.out.println("************************************************************************");
        System.out.println();
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        // create test harness and PIGPIO instances
        pigpio = PiGpio.newSocketInstance(System.getProperty("pi4j.pigpio.host", "rpi3bp.savage.lan"),
                System.getProperty("pi4j.pigpio.port", "8888"));

        // initialize test harness and PIGPIO instances
        pigpio.initialize();

        // set pin ALT5 modes for SERIAL RX & TX PINS on RPI3B
        pigpio.gpioSetMode(14, PiGpioMode.ALT5);
        pigpio.gpioSetMode(15, PiGpioMode.ALT5);

        // OPEN SERIAL PORT
        handle = pigpio.serOpen(SERIAL_DEVICE, BAUD_RATE);
    }

    @AfterEach
    public void afterEach() throws IOException {

        // CLOSE SERIAL PORT
        pigpio.serClose(handle);

        // terminate test harness and PIGPIO instances
        pigpio.terminate();
    }

    @Test
    @DisplayName("SERIAL :: Test SINGLE-BYTE (W/R)")
    public void testSerialSingleByteTxRx() throws IOException, InterruptedException {
        System.out.println();
        System.out.println("--------------------------------------------");
        System.out.println("TEST SERIAL PORT SINGLE BYTE RAW READ/WRITE");
        System.out.println("--------------------------------------------");

        // drain any pending bytes in buffer
        pigpio.serDrain(handle);

        // iterate over BYTE range of values, WRITE the byte then immediately
        // READ back the byte value and compare to make sure they are the same values.
        for(int b = 0; b < 256; b++) {
            System.out.println("[TEST WRITE/READ SINGLE BYTE]");
            // WRITE :: SINGLE RAW BYTE
            System.out.println(" (WRITE) >> VALUE = 0x" + Integer.toHexString(b));
            pigpio.serWriteByte(handle, (byte)b);

            // READ :: NUMBER OF BYTES AVAILABLE TO READ
            int available = pigpio.serDataAvailable(handle);
            System.out.println(" (READ)  << AVAIL = " + available);
            Assert.assertEquals("SERIAL BYTE VALUE MISMATCH",  1, available);

             // READ :: SINGLE RAW BYTE
            int rx = pigpio.serReadByte(handle);
            System.out.println(" (READ)  << VALUE = 0x" + Integer.toHexString(rx));
            Assert.assertEquals("SERIAL BYTE VALUE MISMATCH",  b, rx);
            System.out.println();
        }
    }

    @Test
    @DisplayName("SERIAL :: Test MULTI-BYTE (W/R)")
    public void testSerialMultiByteTxRx() throws IOException, InterruptedException {
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("TEST SERIAL MULTI-BYTE READ/WRITE");
        System.out.println("----------------------------------------");

        // iterate over series of test values, WRITE the byte then immediately
        // READ back the byte value and compare to make sure they are the same values.
        for(int x = 0; x < 50; x++) {
            System.out.println("[TEST WRITE/READ MULTI-BYTE]");

            // drain any pending bytes in buffer
            pigpio.serDrain(handle);

            // Arduino max serial buffer length is 32
            // create a random series of bytes up to 32 bytes long
            Random r = new Random();
            int len = r.nextInt((32 - 4) + 1) + 4;
            byte[] testData = new byte[len];
            r.nextBytes(testData);

            // WRITE :: MULTI-BYTE
            System.out.println(" (WRITE) >> VALUE = " + byteToHex(testData));
            pigpio.serWrite(handle, testData);

            // take a breath while buffer catches up
            Thread.sleep(100);

            // READ :: NUMBER OF BYTES AVAILABLE TO READ
            int available = pigpio.serDataAvailable(handle);
            System.out.println(" (READ)  << AVAIL = " + available);
            Assert.assertEquals("SERIAL READ AVAIL MISMATCH",  testData.length, available);

            // READ :: MULTI-BYTE
            byte[] readBuffer = new byte[available];
            int bytesRead = pigpio.serRead(handle, readBuffer, available);
            System.out.println(" (READ)  << BYTES READ = " + bytesRead);
            System.out.println(" (READ)  << VALUE = " + byteToHex(readBuffer));
            //Thread.sleep(50);

            Assert.assertArrayEquals("SERIAL MULTI-BYTE VALUE MISMATCH",  testData, readBuffer);
        }
    }

    private String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x ", b);
        }
        String result = "[0x" + formatter.toString().trim() + "]";
        formatter.close();
        return result;
    }
}