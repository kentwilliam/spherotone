package com.pheelicks.spherotones;

import orbotix.robot.sensor.Acceleration;

public class VectorResolver 
{
	public static Direction[] directions2D = {Direction.LEFT, Direction.RIGHT, Direction.FORWARD, Direction.BACK};
	public static Direction[] directions3D = {Direction.LEFT, Direction.RIGHT, Direction.FORWARD, Direction.BACK, Direction.UP, Direction.DOWN};
	
	public static Direction resolve(Acceleration vector, Direction[] candidates)
	{
		// Dot product with all vectors, returning the maximum
		double max = 0;
		Direction resolvedDirection = Direction.LEFT;
		for (Direction direction : candidates) 
		{
			double dotProduct = vector.x * direction.vector.x + vector.y * direction.vector.y + vector.z * direction.vector.z;
			if(dotProduct > max)
			{
				resolvedDirection = direction;
				max = dotProduct;
			}
		}
		
		return resolvedDirection;
	}
	
	public static Direction resolve2D(Acceleration vector)
	{
		return resolve(vector, directions2D);
	}
	
	public static Direction resolve3D(Acceleration vector)
	{
		return resolve(vector, directions3D);
	}
}
