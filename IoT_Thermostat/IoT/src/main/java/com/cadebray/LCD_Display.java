package com.cadebray;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;

import static java.lang.Thread.sleep;

public class LCD_Display{
    private final Context pi4j;
    private final DigitalOutput pinRS;
    private final DigitalOutput pinE;
    private final DigitalOutput pinD4;
    private final DigitalOutput pinD5;
    private final DigitalOutput pinD6;
    private final DigitalOutput pinD7;
    private final int columns =16;
    private final int rows = 2;

    /**
     * Default configuration for LCD.
     * Uses GPIO pins RS=17, E=27, D4=5, D5=6, D6=13, D7=26
     * @param pi4j Pi4J Context object, created via Pi4J.newAutoContext()
     */
    public LCD_Display(Context pi4j) {
        this.pi4j = pi4j;
        this.pinRS = createOutput("lcd-rs", "LCD RS", 17);
        this.pinE = createOutput("lcd-e", "LCD E", 27);
        this.pinD4 = createOutput("lcd-d4", "LCD D4", 5);
        this.pinD5 = createOutput("lcd-d5", "LCD D5", 6);
        this.pinD6 = createOutput("lcd-d6", "LCD D6", 13);
        this.pinD7 = createOutput("lcd-d7", "LCD D7", 26);

        // Initialize the LCD (4-bit mode)
        writeCommand(0x33); // Initialize
        writeCommand(0x32); // Set to 4-bit mode
        writeCommand(0x28); // 2 line, 5x8 matrix
        writeCommand(0x0C); // Display on, cursor off
        writeCommand(0x06); // Increment cursor
        clear();
    }

    /**
     * Custom pin configuration for LCD
     * @param pi4j Pi4J Context object, created via Pi4J.newAutoContext()
     * @param RS Provide GPIO pin for RS as an integer
     * @param E Provide GPIO pin for E as an integer
     * @param D4 Provide GPIO pin for D4 as an integer
     * @param D5 Provide GPIO pin for D5 as an integer
     * @param D6 Provide GPIO pin for D6 as an integer
     * @param D7 Provide GPIO pin for D7 as an integer
     */
    @SuppressWarnings("unused")
    public LCD_Display(Context pi4j, int RS, int E, int D4, int D5, int D6, int D7) {
        this.pi4j = pi4j;
        this.pinRS = createOutput("lcd-rs", "LCD RS", RS);
        this.pinE = createOutput("lcd-e", "LCD E", E);
        this.pinD4 = createOutput("lcd-d4", "LCD D4", D4);
        this.pinD5 = createOutput("lcd-d5", "LCD D5", D5);
        this.pinD6 = createOutput("lcd-d6", "LCD D6", D6);
        this.pinD7 = createOutput("lcd-d7", "LCD D7", D7);

        // Initialize the LCD (4-bit mode)
        writeCommand(0x33); // Initialize
        writeCommand(0x32); // Set to 4-bit mode
        writeCommand(0x28); // 2 line, 5x8 matrix
        writeCommand(0x0C); // Display on, cursor off
        writeCommand(0x06); // Increment cursor
        clear();
    }

    /**
     * Create a digital output pin
     * @param id This is the unique identifier for the pin
     * @param name This is the human-readable name for the pin
     * @param address This is the GPIO address for the pin
     * @return The created DigitalOutput pin
     */
    private DigitalOutput createOutput(String id, String name, int address) {
        DigitalOutputConfigBuilder cfgb = DigitalOutput.newConfigBuilder(pi4j)
                .id(id)
                .name(name)
                .address(address)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW);
        DigitalOutputConfig cfg = cfgb.build();
        return pi4j.create(cfg);
    }

    /**
     * Clear the LCD
     */
    public void clear() {
        writeCommand(0x01); // Clear display command
        try {
            sleep(2); // Wait for command to complete
        } catch (InterruptedException e) {
            // Restore interrupted state
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Write command byte to the LCD
     * @param value This is the byte value to write
     */
    private void writeCommand(int value) {
        write(value, false);
    }

    /**
     * Write data byte to the LCD
     * @param value This is the byte value to write
     */
    private void writeData(int value) {
        write(value, true);
    }

    /**
     * Write a byte to the LCD
     * @param value This is the byte value to write
     * @param rs This is true for data, false for command
     */
    private void write(int value, boolean rs) {
        if (rs) pinRS.high(); else pinRS.low();
        int high = (value >> 4) & 0x0F;
        int low  = value & 0x0F;
        writeNibble(high);
        writeNibble(low);
    }

    /**
     * Pulse the enable pin to latch data into the LCD
     */
    private void pulseEnable() {
        try {
            // Enable pulse
            pinE.high();
            // Sleep for 1 millisecond to allow the LCD to latch the data
            Thread.sleep(1);
            // Disable pulse
            pinE.low();
            // Sleep for 1 millisecond to allow the LCD to process the data
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Restore interrupted state
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Set cursor to column (0-based) and row (0-based).
     * DDRAM addresses: line0 start 0x00 (command 0x80), line1 start 0x40 (command 0xC0)
     */
    public void setCursor(int col, int row) {
        if (row < 0) row = 0;
        if (row >= rows) row = rows - 1;
        if (col < 0) col = 0;
        if (col >= columns) col = columns - 1;
        int addr = (row == 0) ? col : (0x40 + col);
        writeCommand(0x80 | addr);
    }

    /**
     * Print a message to the LCD starting at the home position (0,0).
     * @param message This is the message to print
     */
    public void print(String message) {
        int curRow = 0;
        int curCol = 0;
        setCursor(0, 0);
        for (char c : message.toCharArray()) {
            if (c == '\n') {
                curRow++;
                if (curRow >= rows) curRow = rows - 1;
                curCol = 0;
                setCursor(curCol, curRow);
            } else {
                writeData(c);
                curCol++;
                if (curCol >= columns) {
                    curCol = 0;
                    curRow++;
                    if (curRow >= rows) curRow = rows - 1;
                    setCursor(curCol, curRow);
                }
            }
        }
    }

    /**
     * Write a nibble (4 bits) to the data pins
     * @param nibble This is the 4-bit value to write
     */
    private void writeNibble(int nibble) {
        // nibble bits: bit0 -> D4, bit1 -> D5, bit2 -> D6, bit3 -> D7
        pinD4.state(((nibble & 0x01) != 0) ? DigitalState.HIGH : DigitalState.LOW);
        pinD5.state(((nibble & 0x02) != 0) ? DigitalState.HIGH : DigitalState.LOW);
        pinD6.state(((nibble & 0x04) != 0) ? DigitalState.HIGH : DigitalState.LOW);
        pinD7.state(((nibble & 0x08) != 0) ? DigitalState.HIGH : DigitalState.LOW);
        pulseEnable();
    }

    /**
     * Demo if ran directly
     */
    public static void main(String[] args) throws Exception {
        var pi4j = com.pi4j.Pi4J.newAutoContext();
        LCD_Display lcd = new LCD_Display(pi4j);

        // Write "Hello, World!" to the LCD
        String message = "Hello, World!\nTest";
        lcd.print(message);
        Thread.sleep(5000);
        lcd.clear();
    }
}
