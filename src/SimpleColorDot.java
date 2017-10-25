import processing.core.*;

public class SimpleColorDot extends PApplet{
	
	static PApplet parent;
	PVector vel = new PVector();
	PVector pos = new PVector();
	float rad;
	float friction;
	float h;
	float s;
	float b;
	int life = 72;
	int myColor;

	SimpleColorDot(float x, float y, float rad, float colorH,PApplet p) {
		pos.x = x;
		pos.y = y;
		this.rad = rad;
		friction = (float) (random(1,8)*.25 + 1);
		parent = p;
		
		h = map(colorH, 0, 1, 0, 255);
		s = parent.random(50,255);
		b = parent.random(180, 255);
		myColor = parent.color(h, s, b);
	}

	public void run() {
		pos.add(vel);
		if(life < 5) myColor = parent.color(0,0,255);
		parent.fill(myColor);
		parent.ellipse(pos.x, pos.y, rad*2, rad*2);
		life --;
	}
	public void applyForce(PVector force) {
		vel.div(friction);
		vel.add(force);

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