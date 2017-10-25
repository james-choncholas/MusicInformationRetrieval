import java.io.*;
import java.util.*;

public class Utility {

	
	/**
	 * Input file of song names structure: title artist filename
	 * <p> The song file must reside in the MATLAB folder.</p>
	 * 
	 * @param fName
	 * @return
	 */
	public static ArrayList<ArrayList<String>> inputSongs(String fName) {
		
		File f;
		Scanner s;
		try{
			f = new File(fName);
			s = new Scanner(f);
		}catch(FileNotFoundException e){
			System.out.println("File not Found");
			throw new IllegalArgumentException();
		}
		
		//set up arraylists
		ArrayList<ArrayList<String>> songs = new ArrayList<ArrayList<String>>();
		ArrayList<String> titles = new ArrayList<String>();
		ArrayList<String> artists = new ArrayList<String>();
		ArrayList<String> fileNames = new ArrayList<String>();
		songs.add(titles);
		songs.add(artists);
		songs.add(fileNames);
		
		while(s.hasNext()){
			titles.add(s.next());
			artists.add(s.next());
			fileNames.add(s.next());
		}
		
		s.close();
		return songs;
		
		
	}
	
	public static Song getStoredInfo(String title, String artist, String filename){
		//return null;
		///*
		File f;
		Scanner s = null;
		try{
			f = new File(System.getProperty("user.dir")+"\\storedData\\javaSong" + title);
			if(!f.isFile()){
				return null;
			}
			s = new Scanner(f).useLocale(Locale.US);
		}catch(FileNotFoundException e){
			return null;
		}
	
		Song strdSong = new Song(title, artist, filename);
		System.out.println("getting song: "+ strdSong.title);
		
		s.next(); s.next();
		strdSong.setBPM(s.nextDouble());
		strdSong.settThresh(s.nextDouble());
		strdSong.setNoiseAmt(s.nextDouble(), s.nextDouble());
		strdSong.setMidAnal(s.nextDouble(), s.nextDouble(), s.nextDouble());
		strdSong.setMaxF(s.nextDouble());
		strdSong.setCentroidTrack(s.nextDouble(), s.nextDouble());
		strdSong.setSPR(s.nextDouble());
		
		s.close();
		return strdSong;
		//*/
	}
	
	public static void storeSongInfo(Song s) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter w = new PrintWriter(
				new OutputStreamWriter(
						new FileOutputStream("storedData/javaSong"+s.title)));
		System.out.println("SongStored");
		w.println(s.print());
		w.close();
		return;
	}

	
	/*
	public static ArrayList<Song> sortALByFRL(ArrayList<Song> songs){
		//bubble sort (stable)
		int passes = songs.size() - 1;
		boolean swapsDone = true;
		
		for(int i = 0; i < passes && swapsDone; i++){
			swapsDone = false;
			for(int j = songs.size() - 1; j > i; j--){
				if(songs.get(j).fRatingL < songs.get(j-1).fRatingL ){
					Song temp = songs.get(j);
					Song tempPrev = songs.get(j-1);
					songs.set(j, tempPrev);
					songs.set(j-1, temp);
					swapsDone = true;
				}
			}
		}
		
		return songs;
	}
	
	public static ArrayList<Song> sortALByFRM(ArrayList<Song> songs){
		//bubble sort (stable)
		int passes = songs.size() - 1;
		boolean swapsDone = true;
		
		for(int i = 0; i < passes && swapsDone; i++){
			swapsDone = false;
			for(int j = songs.size() - 1; j > i; j--){
				if(songs.get(j).fRatingM < songs.get(j-1).fRatingM ){
					Song temp = songs.get(j);
					Song tempPrev = songs.get(j-1);
					songs.set(j, tempPrev);
					songs.set(j-1, temp);
					swapsDone = true;
				}
			}
		}
		
		return songs;
	}

	public static ArrayList<Song> sortALByFRH(ArrayList<Song> songs) {
		//bubble sort (stable)
		int passes = songs.size() - 1;
		boolean swapsDone = true;
		
		for(int i = 0; i < passes && swapsDone; i++){
			swapsDone = false;
			for(int j = songs.size() - 1; j > i; j--){
				if(songs.get(j).fRatingH < songs.get(j-1).fRatingH ){
					Song temp = songs.get(j);
					Song tempPrev = songs.get(j-1);
					songs.set(j, tempPrev);
					songs.set(j-1, temp);
					swapsDone = true;
				}
			}
		}
		
		return songs;
		
	}
	*/
}
