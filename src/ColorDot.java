import processing.core.*;

public class ColorDot extends PApplet{
	
	static PApplet parent;
	PVector accel = new PVector();
	PVector vel = new PVector();
	PVector pos = new PVector();
	float mass;
	float rad;
	float h;
	float s;
	float b;
	int life = 50;
	int myColor;

	ColorDot(float x, float y, float rad, float colorH,PApplet p) {
		pos.x = x;
		pos.y = y;
		this.rad = rad;
		parent = p;
		
		mass = random(1,2);
		h = map(colorH, 0, 1, 0, 255);
		s = parent.random(20, 255);
		b = parent.random(120,255);
		myColor = parent.color(h, s, b);
	}

	public void run() {
		vel.add(accel);
		pos.add(vel);
		accel.mult(0);
		parent.fill(myColor);
		parent.ellipse(pos.x, pos.y, rad*2, rad*2);
		life --;
	}
	public void applyForce(PVector force) {
		force.div(mass);
		accel.add(force);
	}
	public boolean checkDead() {
		if (pos.x < 0 || pos.x > parent.width || pos.y < 0 || pos.y > parent.height || life < 0) {
			return true;
		}
		else {
			return false;
		}
	}
}