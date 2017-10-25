import net.sf.javaml.core.DenseInstance;


public class Song {
	
	public String title;
	public String artist;
	public String fileName;
	public String path;
	public double bpm;
	public double tThresh;
	public double noiseAmtLo;
	public double noiseAmtHi;
	public double midAnalLow;
	public double midAnalMid;
	public double midAnalHi;
	public double maxFreq;
	public double fRatingL;
	public double fRatingM;
	public double fRatingH;
	public double centroidFDiff;
	public double centroidADiff;
	public double lowOverMid;
	public double highOverMid;
	public double lowOverHigh;
	public double spectralPeakRatio;
	
	public Song(String title, String artist, String fileName){
		this.title = title;
		this.artist = artist;
		this.fileName = fileName;
		findPath();
	}
	
	public void setBPM(double bpm){
		this.bpm = bpm;
	}
	
	public void settThresh(double tThresh){
		this.tThresh = tThresh;
	}
	
	public void setNoiseAmt(double noiseAmtLo, double noiseAmtHi){
		this.noiseAmtLo = noiseAmtLo;
		this.noiseAmtHi = noiseAmtHi;
	}
	
	public void setMidAnal(double midAnalLow, double midAnalMid, double midAnalHi){
		this.midAnalLow = midAnalLow;
		this.midAnalMid = midAnalMid;
		this.midAnalHi = midAnalHi;
		this.lowOverMid = (midAnalLow/midAnalMid) *80;
		this.highOverMid = (midAnalHi/midAnalMid) *110;
		this.lowOverHigh = (midAnalLow/midAnalHi) *150;
	}
	
	public void setMaxF(double maxF){
		this.maxFreq = maxF;
	}
	
	public void setCentroidTrack(double centroidFDiff, double centroidADiff) {
		this.centroidFDiff = centroidFDiff;
		this.centroidADiff = centroidADiff;
	}
	
	public void setSPR(double spr){
		this.spectralPeakRatio = spr;
	}
	
	public String print(){
		
		System.out.println("Title: "+title);
		System.out.println("Artist: "+artist);
//		System.out.println("FileName: "+fileName);
//		System.out.println("Beats per Minute: "+bpm);
//		System.out.println("DiffThresh(.3): "+tThresh);
//		System.out.println("Noise at 11.1 Hz: "+noiseAmtLo);
//		System.out.println("Noise at 22055 Hz: "+noiseAmtHi);
//		System.out.println("Quantity of Frequency Content (0-100Hz): "+midAnalLow);
//		System.out.println("Quantity of Frequency Content (110-500Hz): "+midAnalMid);
//		System.out.println("Quantity of Frequency Content (500-900Hz): "+midAnalHi);
//		System.out.println("LowBand/MidBand: " + lowOverMid);
//		System.out.println("HighBand/MidBand: " + highOverMid);
//		System.out.println("LowBand/HighBand: "+ lowOverHigh);
//		System.out.println("Maximum Quantity of Frequencies: "+maxFreq);
//		System.out.println("Centroid Frequency Differences (.3): " + centroidFDiff);
//		System.out.println("Centroid Amplitude Differences (.05): " + centroidADiff);
		System.out.println();
		
		//String to save to file
		String retString = title +" "+ artist +" "+ bpm +" "+ tThresh +" "+
				noiseAmtLo +" "+ noiseAmtHi + " "+ midAnalLow +" "+ midAnalMid
				+" "+ midAnalHi +" "+ maxFreq +" "+ centroidFDiff + " "+
				centroidADiff +" "+ spectralPeakRatio;
		return retString;
	}
	
	public DenseInstance getDataInstance(){
		return new DenseInstance(new double[]{tThresh, noiseAmtLo, noiseAmtHi, 
				midAnalLow, midAnalMid, midAnalHi, 
				lowOverMid, highOverMid, lowOverHigh, maxFreq,
				centroidFDiff, centroidADiff,
				spectralPeakRatio});
	}
	
	public void findPath(){
		char[] pathAr = (System.getProperty("user.dir")).toCharArray();
		for(char c : pathAr){
			if(c == '\\'){
				c = '/';
			}
		}
		String curDir = new String(pathAr);
		this.path = curDir + "/MATLAB/" + fileName;
	}

	public int compareTo(Song otherSong){
		if(otherSong.title.compareTo(this.title) > 0){
			return 1;
		}
		else if(otherSong.title.compareTo(this.title) < 0){
			return -1;
		}
		return 0;
	}
	
	
	/////////////////////////////////////////////////////////// METHOD 1 ONLY
	/*
	public void calculateFRatingL(){
		fRatingL = 100 - centroidFDiff;
	}
	
	public void calculateFRatingM(){
		//fRatingM = (midAnalMid - Math.min(midAnalHi, midAnalLow));
		fRatingM = centroidAFDiff;
	}
	
	public void calculateFRatingH(){
		fRatingH = 300 - centroidFDiff;
		//Higher Centroid Frequency Diff -> more dynamic -> more "jazzy"
	}
	*/
}