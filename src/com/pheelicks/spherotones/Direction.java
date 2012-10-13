package com.pheelicks.spherotones;
import orbotix.robot.sensor.Acceleration;

public enum Direction 
{
	LEFT (new Acceleration((short)-1, (short)0, (short)0), "left"),
	RIGHT (new Acceleration((short)1, (short)0, (short)0), "right"),
	FORWARD (new Acceleration((short)0, (short)-1, (short)0), "forward"),
	BACK (new Acceleration((short)0, (short)1, (short)0), "back"),
	UP (new Acceleration((short)0, (short)0, (short)-1), "up"),
	DOWN (new Acceleration((short)0, (short)0, (short)1), "down");
	
	public Acceleration vector;
	public String description;
	
	private Direction(Acceleration vector, String description)
	{
		this.vector = vector;
		this.description = description;
	}
}