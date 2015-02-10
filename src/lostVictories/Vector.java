package lostVictories;

public class Vector {
	
	float x;
	float y;
	float z;
	
	public Vector(double x, double y, double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}

	
	 @Override
	public String toString() {
		return x+","+y+","+z;
	}
}
