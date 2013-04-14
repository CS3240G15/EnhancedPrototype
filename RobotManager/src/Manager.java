import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.SensorPortListener;
import lejos.nxt.SoundSensor;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.USB;
import lejos.robotics.Touch;
import lejos.robotics.navigation.DifferentialPilot;

public class Manager extends Object{
	
	private static boolean USBtest = false;
	
	// Initialize all sensors here
	private static Touch sTouch = new TouchSensor(SensorPort.S1);
	private static SoundSensor sSound = new SoundSensor(SensorPort.S2);
	private static LightSensor sLight = new LightSensor(SensorPort.S3);
	private static UltrasonicSensor sUltra = new UltrasonicSensor(SensorPort.S4);
	
	// DifferentialPilot is our motion controller, we will exclusively use the object to navigate the robot around
	// the first two arguments are wheel diameters and track width (in cm) respectively
	// last two arguments are left and right motors respectively
	protected static DifferentialPilot pilot = new DifferentialPilot(2.125f, 6.875f, Motor.A, Motor.C);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Waiting...");
		
		// Establish the connection here, for testing purpose, we will use USB connection
		NXTConnection connection = null;
		if (USBtest){
			connection = USB.waitForConnection();
		} else {
			connection = Bluetooth.waitForConnection();
		}
		// An additional check before opening streams
		if (connection==null){
			System.out.println("Failed");
		} else {
			System.out.println("Connected");
		}
		
		// Open two data input and output streams for read and write respectively
	    final DataOutputStream oHandle = connection.openDataOutputStream();
	    final DataInputStream iHandle = connection.openDataInputStream();
	    String input = "",output = "";
	    
	    /*// Register a listener to port S1 which is the Touch sensor at the back
	    SensorPort.S1.addSensorPortListener(new SensorPortListener() { // Listener's style

			@Override
			public void stateChanged(SensorPort arg0, int arg1,
					int arg2) {
				try {
					if (sTouch.isPressed()){
					String str = "ALERT: bump into something, stopping all actions";
					oHandle.write(str.getBytes());
					oHandle.flush();
					
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pilot.stop();
			}
			
		});*/
	    
//	    SensorPort.S4.addSensorPortListener(new SensorPortListener() { // sensor 4 somehow doesnt get this event
//
//			@Override
//			public void stateChanged(SensorPort arg0, int arg1, int arg2) {
//				if (sUltra.getDistance() < 30){
//					pilot.stop();
//					String str = "ALERT: obstacle ahead, stopping all actions";
//					try {
//						oHandle.write(str.getBytes());
//						oHandle.flush();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					pilot.stop();
//				}
//				
//			}
//	    	
//	    });

	    //setup threading for listener (Threading style)
//	    (new Thread() {
//	    	public void run(){
//	    		while (true){
//	    			if (sTouch.isPressed()){
//	    				try {
//							oHandle.writeUTF("ALERT: bump into something, stopping all actions");
//							pilot.stop();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//	    			}
//	    			try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//	    		}
//	    	}
//	    }).start();
	    
	    boolean canMove = true;
	    do {
	    	try {
	    		byte[] buffer = new byte[256]; // allocate a buffer of max size 256 bytes
	    		int count = iHandle.read(buffer); // pass the buffer to the input handle to read
	    		if (count>0){ // check if number of bytes read is more than zero
	    		input = (new String(buffer)).trim(); // convert back to string and trim down the blank space
	    		//output=performAction1(input); // perform arbitrary actions
	    		System.out.println("From PC: " + input);
	    		//Thread.sleep(5000);
	    		//message is in format [seqnum] [checksum] [opcode] [parameters...]
	    		//check message integrity
	    		String seqnum = input.substring(0,3);
	    		int checksum = Integer.parseInt(input.substring(4,7));
	    		String opcode = input.substring(8,12);
	    		/*String parameters*/
	    		System.out.println(seqnum);
	    		System.out.println(checksum);
	    		System.out.println(opcode);
	    		if (input.length() != checksum){
	    			//Send back NACK
	    			output = seqnum + " 0";
	    		}
	    		if (opcode.equals("0011") && canMove){
	    			//TakeReading
	    			System.out.println("Taking reading");
	    			System.out.println(seqnum);
	    			output = seqnum + " 1 " + Sensor.TakeReading(input.substring(13,14));
	    			//ACK back 000 
	    		}
	    		/*else if (message[2].equals("0110") && canMove) {
	    			//Abort
	    			System.out.println("Ending session...");
	    			//Exit gracefully
	    		}*/
	    		else if (opcode.equals("0101")) {
	    			//EmergencyStop
	    			canMove = !canMove;
	    			System.out.print(canMove);
	    			output = seqnum + " 1";
	    		}
	    		else if (canMove) {
	    			System.out.println("About to perform action.");
	    			output = seqnum + " 1";
	    			System.out.println(input);
	    			//System.out.println(input.length());
	    			//String direction = input.substring(13,15);
	    			//String speed = input.substring(16);
	    			//System.out.println(direction);
	    			//System.out.println(speed);
	    			performAction(input);
	    		}
	    		
	    		String str = output; //Where the ACK gets formatted
	    		oHandle.write(str.getBytes()); // ACK
	    		oHandle.flush(); // flush the output bytes 
	    		}
	    		Thread.sleep(10);
	    		
	  	    } catch (Exception e ) {
	  	      System.out.println(" write error "+e); 
	  	      System.exit(1);
	  	    }
	    } while (!input.substring(8,12).equals("0110")); // Abort
	    
	    System.out.println("Ending session...");
	    try {
			oHandle.close();
			iHandle.close();
		    connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	/*
	 * Perform different actions based on the command
	 */
	/*private static String performAction1(String cmd) {
		System.out.println("PC: "+cmd);
		String output=cmd;
		if (cmd.equalsIgnoreCase("forward")){
			pilot.forward(); 
			output = "Traveling at: "+pilot.getTravelSpeed();
		} else if (cmd.equalsIgnoreCase("stop")){
			pilot.stop();
			output = "Distance traveled: "+pilot.getMovement().getDistanceTraveled();
		} else if (cmd.equalsIgnoreCase("status")){ // String.format does not work 
			output = "\nTouch sensor: "+ ((sTouch.isPressed())?"Pressed":"Not Pressed");
			output += "\nSound sensor: "+ sSound.readValue();
			output += "\nLight sensor: "+ sLight.getLightValue();
			output += "\nUltra sensor: "+ sUltra.getDistance();
			output += "\n";
		} else if (cmd.equalsIgnoreCase("turnRight")){
			pilot.rotate(90);
		} else if (cmd.equalsIgnoreCase("turnLeft")){
			pilot.rotate(-90);
		}
		return output;
		
	}*/
	
	public static void performAction(String message) {
		System.out.println(message.substring(8,12));
		if (message.substring(8,12).equals("0000")) {
			//Move
			String direction = message.substring(13,15);
			System.out.println("About to move");
			System.out.println(direction);
			int speed = Integer.parseInt(message.substring(16));
			System.out.println(speed);
			Rotor.Move(direction, speed);
		}
		else if (message.substring(8,12).equals("0001")) {
			//MoveMotor
			String motorPort = message.substring(13, 14);
			System.out.println(motorPort);
			int speed = Integer.parseInt(message.substring(16));
			System.out.println("About to move");
			System.out.println(speed);
			Rotor.MoveMotor(motorPort, speed);
		}
		else if (message.substring(8,12).equals("0010")) {
			//StopMovement
			Rotor.Stop();
			/*System.out.println("Stopping");
			if (pilot.isMoving()) {
				pilot.stop();
			}
			else{
				Motor.A.stop();
				Motor.B.stop();
				Motor.C.stop();
			}*/
			
		}
		
	}
	/*public static void MoveMotor(String motorPort, int speed) {
		if (motorPort.equals("A")) { 
			//Motor.A.get
			Motor.A.setSpeed(speed);
			Motor.A.forward();
		}
		else if (motorPort.equals("B")) { 
			Motor.B.setSpeed(speed);
			Motor.B.forward();
		}
		else if (motorPort.equals("C")) { 
			Motor.C.setSpeed(speed);
			Motor.C.forward();
		}
		
	}*/
	/*public static void Move(String direction, int speed) {
		pilot.setTravelSpeed(speed);
		System.out.println(speed);
		if (direction.equals("00")) {
			//Forward
			pilot.forward();
			System.out.println("forward");
		}
		if (direction.equals("01")) {
			//Backward
			pilot.backward();
			System.out.println("backward");
		}
		if (direction.equals("10")) {
			//Left
			pilot.rotate(-90);
			pilot.forward();
			System.out.println("left");
		}
		if (direction.equals("11")) {
			//Right
			pilot.rotate(90);
			pilot.forward();
			System.out.println("right");
		}
	}*/
	/*private static String[] split(String s, String S) {
		  ArrayList<String> list = new ArrayList<String>();
		  int start = 0;
		  while (start < S.length()) {
			 int end = S.indexOf(s, start);
			 if (end < 0)
				break;
			 
			 list.add(S.substring(start, end));
			 start = end + s.length();
		  }
		  if (start < S.length())
			 list.add(S.substring(start));
		  return list.toArray(new String[list.size()]);
	}*/
	
	/*public static String TakeReading(String portName){
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
	}*/
	
}