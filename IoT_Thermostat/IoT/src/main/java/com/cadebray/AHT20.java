package com.cadebray;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

public class AHT20 {
    private final I2C i2c;

    public AHT20() {
        Context pi4j = Pi4J.newAutoContext();
        I2CConfig config = I2C.newConfigBuilder(pi4j)
                .id("AHT20")
                .bus(1)
                .device(0x38)
                .build();

        I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
        this.i2c = i2CProvider.create(config);
    }
    
    public double[] readSensor() throws Exception {
        byte[] cmd = {(byte) 0xAC, (byte) 0x33, (byte) 0x00};
        i2c.write(cmd, 0, 3);
        
        Thread.sleep(100);
        
        final byte[] data = new byte[6];
        i2c.read(data, 0, 6);
        
        // Parse out the humidity sensor reading
        final int humid = ((data[1] & 0xFF) << 12) | ((data[2] & 0xFF) << 4) | ((data[3] & 0xF0) >> 4);
        
        // Parse out the temperature sensor reading
        final int temp = ((data[3] & 0x0F) << 16) | ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
        
        double humidity = humid * 100.0 / 1048576.0;
        double temperature_C = temp * 200.0 / 1048576.0 - 50.0;
        double temperature_F = temperature_C * 9.0 / 5.0 + 32.0;

        return new double[] {humidity, temperature_F, temperature_C};
    }

    public static void main(String[] args) throws Exception {
        AHT20 aht20 = new AHT20();

        while (true){
            var reading = aht20.readSensor();
            System.out.println("Humid: " + reading[0] + " Temp: " + reading[1]);
        }
    }
}
