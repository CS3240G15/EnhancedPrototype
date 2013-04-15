import lejos.nxt.SensorPort;


public class Sensor {
	
	/**
	 * Polls the sensor value in real time.
	 * @param	portName	the port to be polled (1,2,3,4)
	 * @return	value of the sensor
	 */
	public static String TakeReading(String portName){
		System.out.println(portName);
		String reading = "";
		int port;
		port = Integer.parseInt(portName);
		System.out.println(port);
		switch(port){
			case 1:			
				reading += SensorPort.S1.readValue();
				System.out.println(reading);
				break;
			case 2:
				reading += SensorPort.S2.readValue();
				System.out.println(reading);
				break;
			case 3:
				reading += SensorPort.S3.readValue();
				System.out.println(reading);
				break;
			case 4:
				reading += SensorPort.S4.readValue();
				System.out.println(reading);
				break;
			default:
				break;
		
		}
		System.out.println(reading);
		return reading;
	}
}
