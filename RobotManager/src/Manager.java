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
	
	// DifferentialPilot is our motion controller, we will exclusively use the object to navigate the robot around
	// the first two arguments are wheel diameters and track width (in cm) respectively
	// last two arguments are left and right motors respectively
	protected static DifferentialPilot pilot = new DifferentialPilot(5.3975f, 17.4625f, Motor.A, Motor.C);
	
	/**
	 * The managing program for the robot.
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
	    
	    boolean canMove = true;
	    do {
	    	try {
	    		byte[] buffer = new byte[256]; // allocate a buffer of max size 256 bytes
	    		int count = iHandle.read(buffer); // pass the buffer to the input handle to read
	    		if (count>0){ // check if number of bytes read is more than zero
	    		input = (new String(buffer)).trim(); // convert back to string and trim down the blank space
	    		System.out.println("From PC: " + input);
	    		
	    		//message is in format [seqnum] [checksum] [opcode] [parameters...]
	    		//check message integrity
	    		String seqnum = input.substring(0,3);
	    		int checksum = Integer.parseInt(input.substring(4,7));
	    		String opcode = input.substring(8,12);
	    		
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
			e.printStackTrace();
		}
	    
	}
	
	/**
	 * Perform different actions based on the command
	 * @param	message	this is parsed to return a opcode to check against
	 */
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
		}	
	}	
}