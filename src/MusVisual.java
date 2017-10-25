import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.*;
import processing.core.*;

@SuppressWarnings("serial")
public class MusVisual extends PApplet{

	static Minim minim;
	static AudioPlayer player;
	static FFT fftLin;
	static BeatDetect beat;
	static BeatListener bl;


	static int timer;
	static int ballTime= 4000;
	static int particleTime = 9000;
	static int particleFTime = 14000;
	static float smallMouseX;
	static float smallMouseY;

	static float waveColor;
	static float colorH;
	static float sat1;
	static float sat2;
	static float sat3;
	static float bright1;
	static float bright2;
	static float bright3;
	Ball ball1;
	Ball ball2;
	Ball ball3;
	Ball ball4;

	int numDots = 1500;
	float radius = (float) 1.2;
	float frictionScalar = (float) -.01;
	float deltaF = (float) -.0005;
	ArrayList<ColorDot> cdList;
	
	NavierStokesSolver fluidSolver;
	ArrayList<SimpleColorDot> scdList;
	double visc = .000001;
	double diff = .02;
	double limitVelocity = 100;
	int oldMouseX = 1, oldMouseY = 1;
	float scale = 100;

	public void setup()
	{
		//////////////////INITIALIZE WINDOW////////////////////////////////////
		this.size(displayWidth, displayHeight,P3D);
		smallMouseX = width/2;
		smallMouseY = height/2;
		colorMode(HSB, 255);

		//////////////////INITIALIZE AUDIO PLAYER//////////////////////////////
		minim = new Minim(this);
		//get file path for "starter" song
		char[] pathAr = (System.getProperty("user.dir")+"").toCharArray();
		for(char c : pathAr){
			if(c == '\\'){
				c = '/';
			}
		}
		String curDir= new String(pathAr);
		player = minim.loadFile(curDir + "/MATLAB/C2CDownTheRoad.mp3"); 

		/////////////////INITIALIZE FFT OBJECT/////////////////////////////////
		fftLin = new FFT( player.bufferSize(), player.sampleRate() );
		fftLin.linAverages( 30 );
		System.out.println("Spectrum shows 0 - "+ (int)191*fftLin.getBandWidth() +" Hz");
		System.out.println("Midpoint: " + (int) 191/2* fftLin.getBandWidth() + " Hz");

		/////////////////INITIALIZE BEAT DETECTOR//////////////////////////////
		beat = new BeatDetect(player.bufferSize(), player.sampleRate());
		beat.setSensitivity(10);
		bl = new BeatListener(beat, player);

		/////////////////INITIALIZE PARTICLES///////////////////////////////////
		cdList = new ArrayList<ColorDot>();

		/////////////////INITIALIZE FLUID AND PARTICLES////////////////////////
		scdList = new ArrayList<SimpleColorDot>();

		camera(width/2,(float)(height/2.3),(float)((height/2.0)/tan((float) (PI*30.0/180.0))+18),  width/2,height/2,0,  0,1,0);
	}

	
	//Music player
	public static void playSong(String path){
		if(player.isPlaying()){
			player.pause();
		}
		player = minim.loadFile(path);
		//FFT calculator
		fftLin = new FFT( player.bufferSize(), player.sampleRate() );
		fftLin.linAverages( 30 );
		//Beat detector
		beat = new BeatDetect(player.bufferSize(), player.sampleRate());
		beat.setSensitivity(330);
		bl = new BeatListener(beat, player);

		player.play();
	}

	public static void stopSong(){
		if(player.isPlaying()){
			player.pause();
		}
	}

	public static void skip10s(){
		if(player.isPlaying()){
			player.skip(10000);
		}
	}

	public void mousePressed(){
		smallMouseX = mouseX;
		smallMouseY = mouseY;
	}

	
	
	public void draw()
	{
		background(0);
		timer++;

		//////////////////////////////BALL MODE/////////////////////////////////
		if(timer < ballTime){
			if(timer==1){
				frameRate(50);
				ball1 = new Ball((float)800, (float)500, (float)-2, (float)-2, (float)70, (float).53, this);
				ball2 = new Ball((float)500, (float)500, (float)-4, (float)-2, (float)60, (float).5 ,this);
				ball3 = new Ball((float)600, (float)600, (float)6, (float)-2, (float)50, (float).4,  this);
				ball4 = new Ball((float)700, (float)700, (float)7, (float)-2, (float)40, (float).44, this);
				smallMouseX = width/2;
				smallMouseY = height/2;
			}
			if(mousePressed == true && mouseButton == RIGHT){
				timer = ballTime - 1;
				mousePressed = false;
			}
			camera((smallMouseX-width/2)*(float).2+width/2,smallMouseY*(float)2-500,(float)((height/2.0)/tan((float) (PI*30.0/180.0))+18),  width/2,height/2,0,  0,1,0);

			//SET UP COLORS
			fftLin.forward( player.mix );
			//set up fill color for fft bar
			colorH=(float) (colorH+.05); 
			if(colorH>225){
				colorH=0;
			}
			if(beat.isKick()){
				sat1 = 255;
				bright1 = 255;
			}else{
				sat1 = constrain((int) (sat1*.9),0,255);
				bright1 = constrain((int) (bright1*.9),70,255);
			}
			if(beat.isSnare()){
				sat2 = 225;
				bright2 = 255;
			}else{
				sat2 = constrain((int) (sat2*.9),0,255);
				bright2 = constrain((int) (bright2*.9),70,255);
			}
			if(beat.isHat()){
				sat3 = 225;
				bright3 = 255;
			}else{
				sat3 = constrain((int) (sat3*.9),0,255);
				bright3 = constrain((int) (bright3*.9),70,255);
			}
			
			// WAVEFORMS
			// the values returned by left.get() and right.get() will be between -1 and 1,
			// so we need to scale them up to see the waveform
			// note that if the file is MONO, left.get() and right.get() will return the same value
			stroke(colorH,sat2,bright2);
			for(int i = 0; i < player.bufferSize() - 1; i++)
			{
				float x1 = map( i, 0, player.bufferSize(), -20, width+20 );
				float x2 = map( i+1, 0, player.bufferSize(), -20, width+20 );
				if(player.mix.get(i) > .7){
					waveColor = 0;
				}
				line( x1, 50 + player.mix.get(i)*50, x2, 50 + player.mix.get(i+1)*50 );
			}
			
			//FFT SPECTRUM
			// FFT has a depth of 5 and is set back 10
			int wid = 10;
			fill(colorH, sat1, bright1 );
			stroke(255);
			translate(0,height,-15);
			//draw fft
			float curY;
			for(int i = 0; i < 191; i++) //Only draw first ___ of spec
			{
				curY = fftLin.getBand(i);
				translate(0,-curY*2,0);
				box(wid, -curY*4, -8);
				translate(wid,curY*2,0);
			}
			translate(-width,-height,-8);

			//BALLS
			//check x coordinate of ball to send ball the y coord of fft at that balls x
			//ball 1 gets kick beat detect
			//ball 2 gets snare beat detect
			//ball 3 gets high hat beat detect
			directionalLight(colorH, sat1, bright1, 0, -5, -2);
			directionalLight(colorH, sat2, bright2, 0, 5, -2);
			ball1.drawBall(colorH, sat1, bright1, 4*(int)fftLin.getBand((int) (ball1.x*191/width)));
			ball2.drawBall(colorH, sat2, bright2, 4*(int)fftLin.getBand((int) (ball2.x*191/width)));
			ball3.drawBall(colorH, sat3, bright3, 4*(int)fftLin.getBand((int) (ball3.x*191/width)));
			ball4.drawBall(colorH, sat3, bright3, 4*(int)fftLin.getBand((int) (ball4.x*191/width)));
		}

		////////////////////////PARTICLE MODE//////////////////////////////////
		else if(timer >= ballTime && timer<particleTime){
			if(timer == ballTime){
				frameRate(30);
				camera(width/2,height/2,(float)((height/2.0)/tan((float) (PI*30.0/180.0))+18),  width/2,height/2,0,  0,1,0);
				cdList.clear();
			}
			if(mousePressed == true && mouseButton == RIGHT){
				timer = particleTime - 1;
				mousePressed = false;
			}
			//write text
			//write text to screen
			fill(255);
			textSize(20);
			text("Warm Colors: Low Frequencies", 0, height-20, 0);
			text("Cold Colors: High Frequencies", width - 300, height-20, 0);
			//change friction
			frictionScalar += deltaF;
			if (frictionScalar > -.1) {
				deltaF *= -1;
			}
			if (frictionScalar < -.8) {
				deltaF *= -1;
			}

			fftLin.forward( player.mix );
			int curNumParticles;
			ColorDot curDot;
			float angle;
			float phase = (float)(timer - ballTime)/(float)(particleTime)*8*PI;
			for(float i =0; i<130; i++){
				curNumParticles = (int) (log(log(fftLin.getBand((int)i)))*1.5);
				if(i>95) curNumParticles = curNumParticles*2;
				angle = i/130*2*PI;

				for(int j = 0; j<curNumParticles; j++){
					curDot = new ColorDot(width/2, height/2, radius, i/191, this);
					cdList.add(curDot);
					curDot.applyForce( new PVector(cos(angle + phase)*60, 60*sin(angle + phase)) );
					curDot.applyForce(new PVector(random(-1,1), random(-1,1)));
				}
			}


			for (int i = 0; i < cdList.size(); i++) {
				ColorDot cd = cdList.get(i);
				cd.run();
				PVector friction = PVector.mult(cd.vel, frictionScalar);
				cd.applyForce(friction);


				if (cd.checkDead() == true) {
					cdList.remove(i);
				}

			}

		}
		
		//////////////////////PARTICLE FLUID MODE//////////////////////////////
		else if(timer >= particleTime && timer<particleFTime){
			if(timer == particleTime){
				frameRate(30);
				smallMouseX = width/2;
				smallMouseY = height/2;
				camera(width/2,height/2,(float)((height/2.0)/tan((float) (PI*30.0/180.0))+18),  width/2,height/2,0,  0,1,0);
			    fluidSolver = new NavierStokesSolver();
			    scdList.clear();
			    colorH=0;
			}
			if(mousePressed == true && mouseButton == RIGHT){
				timer = particleFTime - 1;
				mousePressed = false;
			}
			//SET UP COLORS
			fftLin.forward( player.mix );
			//set up fill color for fft bar
			colorH=(float) (colorH+.05); 
			if(colorH>225){
				colorH=0;
			}
			if(beat.isKick()){
				sat1 = 255;
				bright1 = 255;
			}else{
				sat1 = constrain((int) (sat1*.9),0,255);
				bright1 = constrain((int) (bright1*.9),70,255);
			}
			if(beat.isHat()){
				sat3 = 225;
				bright3 = 255;
			}else{
				sat3 = constrain((int) (sat3*.9),0,255);
				bright3 = constrain((int) (bright3*.9),70,255);
			}
			//Draw corner elipses
			fill(255,0,bright1);
			ellipse(0,0,800,800);
			fill(colorH,0,bright3);
			ellipse(displayWidth,displayHeight,800,800);
		    
			
			//Apply forces to fluid
		    int n = NavierStokesSolver.N;
			int curBand;
		    float cos;
		    float sin;
		    float dotColor;
			for(int i = 0; i < 95; i+=2) //Only draw first half of spec
			{
				cos = cos((float) ((i/95.) *Math.PI/2.));
				sin = sin((float) ((i/95.) *Math.PI/2.));
				//curBand = (int) log(fftLin.getBand(i)) ;
				curBand = (int) log(fftLin.getBand(i));
				if(curBand<0) curBand = 0;
				if(curBand>2) curBand = 2;

				fluidSolver.applyForce((int)(10*cos), (int)(20*sin), curBand*cos*20, curBand*sin*37.5);
				dotColor = map(i, 0, 95, 0, (float) .5);
				scdList.add( new SimpleColorDot(400*cos, 400*sin, radius, dotColor, this));

			}
			for(int i = 97; i < 191; i+=2) //Only draw second half of spec
			{
				cos = cos((float) ((i/95.)*Math.PI/2.));
				sin = -sin((float) ((i/95.)*Math.PI/2.));
				//curBand = (int) abs(log(fftLin.getBand(i)));
				curBand = (int) log(fftLin.getBand(i));
				if(curBand<0) curBand = 0;
				if(curBand>2) curBand = 2;

				fluidSolver.applyForce((int)(n+10*cos), (int)(n+20*sin), curBand*cos*125, curBand*sin*50);
				dotColor = map(i, 97, 191, (float).5, (float) .9);
				scdList.add( new SimpleColorDot(width+400*cos, height+400*sin, radius, dotColor, this));
			}
			
			
			double dt = 1 / frameRate;
		    fluidSolver.tick(dt, visc, diff);
			
			//apply forces to dots
			int cellX;
			int cellY;
		    SimpleColorDot curDot;
			for (int i = 0; i < scdList.size(); i++) {
				curDot = scdList.get(i);
			    cellX = floor(curDot.pos.x / (width/n));
			    cellY = floor(curDot.pos.y / (height/n));
			    cellY = min(cellY, n-1);
	            float dx = (float) fluidSolver.getDx(cellX, cellY);
	            float dy = (float) fluidSolver.getDy(cellX, cellY);
				curDot.applyForce(new PVector(dx*10, dy*10));
				curDot.run();
				
				if (curDot.checkDead() == true) {
					scdList.remove(i);
				}

			}
			//write text to screen
			fill(255);
			textSize(30);
			text("0-4000 Hz", 0, 20, 0);
			text("4000-8000 Hz", width - 220, height);

//			stroke(color(216));
///		    paintGrid();	 
//		    float scale = 100;
//		    stroke(color(255));
//		    paintMotionVector(scale);
			
			
		}
		

		else if (timer > particleFTime){timer = 0;}
	}
	

	
	private void paintGrid() {
	    int n = NavierStokesSolver.N;
	    float cellHeight = height / n;
	    float cellWidth = width / n;
	    for (int i = 1; i < n; i++) {
	        line(0, cellHeight * i, width, cellHeight * i);
	        line(cellWidth * i, 0, cellWidth * i, height);
	    }
	}

	private void paintMotionVector(float scale) {
	    int n = NavierStokesSolver.N;
	    float cellHeight = height / n;
	    float cellWidth = width / n;
	    for (int i = 0; i < n; i++) {
	        for (int j = 0; j < n; j++) {
	            float dx = (float) fluidSolver.getDx(i, j);
	            float dy = (float) fluidSolver.getDy(i, j);
	 
	            float x = cellWidth / 2 + cellWidth * i;
	            float y = cellHeight / 2 + cellHeight * j;
	            dx *= scale;
	            dy *= scale;
	 
	            line(x, y, x + dx, y + dy);
	        }
	    }
	}
}
