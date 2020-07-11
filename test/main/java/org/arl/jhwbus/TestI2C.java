import org.arl.jhwbus.*;
import java.io.IOException;

public class TestI2C
{
	public static void main(String [] args)
	{
		System.out.println("Hello World");
		try {
			I2CDevice ms5837 = I2CDevice.open("/dev/i2c-1", (byte) 0x60);
			System.out.println(ms5837);
			System.out.println(String.format("0x%04X",ms5837.readByteData((byte)0x03)));
			ms5837.close();
		}catch(IOException e){
			System.out.println(e);
		}
	}
}
