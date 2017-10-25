import processing.core.*;

public class Ball extends PApplet{
	public float x; 		  // x location of square
	float y;					// y location of square
	public float xspeed; 	  // speed of square
	float yspeed;
	float gravity;
	float ballSize;
	float lifetime;
	PApplet parent;

	Ball(float xinit, float yinit, float xS, float yS, float s, float gravity, PApplet p){
		this.x = xinit;
		this.y = yinit;
		this.xspeed = xS;
		this.yspeed = yS;
		this.gravity = gravity;
		this.parent = p;
		this.ballSize = s;
	}

	//need to check x coordinate before drawing ball to send y coordinate of fft spec
	public void drawBall(float colorH, float colorS, float colorB, int yAmpFFT){

		// Display the Ball
		parent.noStroke();
		parent.fill(colorH, colorS, colorB);
		parent.lightSpecular(colorH-5,50,50); //what color does this reflect when dark
		parent.emissive(0,0,0);
		parent.shininess((float) .5);
		parent.translate(x,y,0);
		parent.specular(220);
		parent.sphere(ballSize);
		parent.translate(-x, -y, 0);
		
		//Find next XY Coordinates
		// Add speed to location.
		y = y + yspeed;
		x = x + xspeed;
		// Add gravity to speed.
		yspeed = yspeed + gravity;
		if(y-ballSize < 100){ // If ball hits top
			yspeed = (float) (abs(yspeed) * 0.7);
		}
		if (y+ballSize > parent.height) { // If ball hits bottom
			yspeed = (float) (abs(yspeed) * -0.8);  
		}else if(y+ballSize > parent.height-yAmpFFT){ // If ball hits FFT
			yspeed = (float) (-abs(yspeed) - 7);
		}
		if(x+ballSize > parent.width){ // If ball hits right
			xspeed = -abs(xspeed);
		}
		if(x-ballSize < 0){ // If ball hits left
			xspeed = abs(xspeed);
		}
	}

}
