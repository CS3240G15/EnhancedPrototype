import lejos.nxt.Motor;
import lejos.robotics.navigation.DifferentialPilot;


public class Rotor {
	public static void MoveMotor(String motorPort, int speed) {
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
		
	}
	public static void Move(String direction, int speed) {
		Manager.pilot.setTravelSpeed(speed);
		System.out.println(speed);
		if (direction.equals("00")) {
			//Forward
			Manager.pilot.forward();
			System.out.println("forward");
		}
		if (direction.equals("01")) {
			//Backward
			Manager.pilot.backward();
			System.out.println("backward");
		}
		if (direction.equals("10")) {
			//Left
			Manager.pilot.rotate(-90);
			//Manager.pilot.forward();
			System.out.println("left");
		}
		if (direction.equals("11")) {
			//Right
			Manager.pilot.rotate(90);
			Manager.pilot.forward();
			System.out.println("right");
		}
	}
	public static void Stop() {
		//StopMovement
		System.out.println("Stopping");
		if (Manager.pilot.isMoving()) {
			Manager.pilot.stop();
		}
		else{
			Motor.A.stop();
			Motor.B.stop();
			Motor.C.stop();
		}
	}
}
