import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.swing.JOptionPane;
import matlabcontrol.*;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
import net.sf.javaml.core.*;
import net.sf.javaml.filter.normalize.NormalizeMidrange;

public class MIR {

	/*
	 * Song files for data aquisition reside in the MATLAB folder.
	 * Song files for music Visualizer reside in 'bin' folder.
	 * Song text files reside in home folder.
	 */
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException
	{
		String sep = System.getProperty("file.separator");
		
		MatlabProxy proxy = null;
		try{
			//Create a proxy, which we will use to control MATLAB
			MatlabProxyFactory factory = new MatlabProxyFactory();
			proxy = factory.getProxy();
			//change current folder
			proxy.eval("cd "+ System.getProperty("user.dir")+sep+"MATLAB");
		}
		catch(MatlabConnectionException e){
			JOptionPane.showMessageDialog(null,"MATLAB NOT FOUND. New songs cannot be added.","Warning",JOptionPane.WARNING_MESSAGE);
		}
		catch(MatlabInvocationException f){
			JOptionPane.showMessageDialog(null,"MATLAB cannot find its folder. New songs cannot be added.","Warning",JOptionPane.WARNING_MESSAGE);
		}
		catch(Exception g){
			JOptionPane.showMessageDialog(null,"MATLAB error. New songs cannot be added.","Warning",JOptionPane.WARNING_MESSAGE);
		}

		//get list of songs from FileIO
		ArrayList<ArrayList<String>> songInfos = null;
		try{
			songInfos = Utility.inputSongs("songInfo.txt");
		}catch (Exception e){
            JOptionPane.showMessageDialog(null,"Cannot find file named songInfo.txt\n"
					+ "It contains a list of songs residing in MATLAB folder."
                    ,"Fatal Error",JOptionPane.WARNING_MESSAGE);
            return;
		}

		//all them songs
		ArrayList<Song> origSongs = new ArrayList<Song>();

		///////////MAIN LOOP for DATA AQUISITION////////////////////////////////////////

		for(int i = 0; i < songInfos.get(0).size(); i++){


			Song s = Utility.getStoredInfo(songInfos.get(0).get(i),
					songInfos.get(1).get(i),
					songInfos.get(2).get(i)); //Try to find song file

			if(s == null){ //if there is no file found, gather data from matlab



				Song mySong = new Song(songInfos.get(0).get(i), 
						songInfos.get(1).get(i),
						songInfos.get(2).get(i));

				//read mp3 file in matlab to array
				proxy.eval("songArray = mp3read('"+ mySong.fileName +"');");

				//set beats per minute
				Object[] bpmArray = proxy.returningEval("tempo(songArray(:,1),44100);" , 1);
				double bpm = ((double[]) bpmArray[0])[0];
				mySong.setBPM(bpm);

				//find .3 differential threshold
				Object[] tArray = proxy.returningEval("DiffThresh(songArray,.3);", 1);
				double tThresh = ((double[]) tArray[0])[0];
				mySong.settThresh(tThresh);

				//do Short Time Fourier Transform stuff
				Object[] anss = proxy.returningEval("STFTs(songArray, .3, .05," +"'"+System.getProperty("user.dir")+"\\storedData\\STFT"+ mySong.title +"')", 9);
				double noiseAmtLo = ((double[]) anss[0])[0];
				double noiseAmtHi = ((double[]) anss[1])[0];
				double midAnalLow = ((double[]) anss[2])[0];
				double midAnalMid = ((double[]) anss[3])[0];
				double midAnalHi = ((double[]) anss[4])[0];
				double maxFreq = ((double[]) anss[5])[0];
				double centroidFDiff = ((double[]) anss[6])[0];
				double centroidADiff = ((double[]) anss[7])[0];
				double spectralPeakRatio = ((double[]) anss[8])[0];

				mySong.setNoiseAmt(noiseAmtLo, noiseAmtHi);
				mySong.setMidAnal(midAnalLow, midAnalMid, midAnalHi);
				mySong.setMaxF(maxFreq);
				mySong.setCentroidTrack(centroidFDiff, centroidADiff);
				mySong.setSPR(spectralPeakRatio);

				//store song data for future
				try {
					Utility.storeSongInfo(mySong);
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					System.out.println("Could not store song file");
				} 

				origSongs.add(mySong);

			}

			else{ //song file data has been found, add it to the list
				origSongs.add(s);
				s.print();
			}
		}
        //Disconnect the proxy from MATLAB
        if(proxy != null){
            proxy.exit();
            proxy.disconnect();
        }

		//////////RANK SONGS///////////////////////////////////////////////////////////

		//Method 1:
		//Put songs into three buckets and Sort with method in Song class
		//Send GUI array list of sorted songs. GUI picks two surrounding songs in list
		/*
		ArrayList<Song> softSongs = new ArrayList<Song>();
		ArrayList<Song> stdSongs= new ArrayList<Song>();
		ArrayList<Song> hardSongs = new ArrayList<Song>();

		for(Song s: songs){
			if((s.tThresh < 20  && s.maxFreq < 275) || s.centroidAFDiff > 20){//Soft Songs
				s.calculateFRatingL();
				softSongs.add(s);
			}else if((s.tThresh > 20 && s.tThresh < 400) || s.centroidFDiff<0){//Standrd Songs
				s.calculateFRatingM();
				stdSongs.add(s);
			}else{////////////////////////////////////////////////////Hard Songs
				s.calculateFRatingH();
				hardSongs.add(s);
			}
		}
		ArrayList<Song> origSongs = new ArrayList<Song>(songs);
		Utility.sortALByFRL(softSongs);
		Utility.sortALByFRM(stdSongs);
		Utility.sortALByFRH(hardSongs);
		songs.clear();
		songs.addAll(softSongs);
		songs.addAll(stdSongs);
		songs.addAll(hardSongs);
		 */

		//Method 2:
		//Use K-NearestNeighbors
		Instance songInst;
		Dataset songData = new DefaultDataset();
		for(Song s: origSongs){
			songInst = s.getDataInstance();
			songData.add(songInst);
		}

		//Method 3:
		//Use K-means
		////send song's features to a large dataset to do clustering
		/*
		Instance songInst;
		Dataset songData = new DefaultDataset();
		for(Song s: origSongs){
			double[] songValues = new double[]{s.bpm, s.tThresh, s.noiseAmtLo, 
					s.noiseAmtHi, s.midAnalLow, s.midAnalMid, s.midAnalHi,s.maxFreq,
					s.centroidFDiff, s.centroidADiff, s.centroidAFDiff};
			songInst = new DenseInstance(songValues);
			songData.add(songInst);
		}
		Clusterer kmeans = new KMeans(20, 200);
		Dataset[] clusters = kmeans.cluster(songData);
		// Create a measure for the cluster quality
		ClusterEvaluation sse= new SumOfSquaredErrors();
		// Measure the quality of the clustering
		double score=sse.score(clusters);
		System.out.println("Score of clustering: " + score);
		////retrieve original song object from each cluster dataset
		double songToFindBPM;
		ArrayList<Song> sortedSongs = new ArrayList<Song>();
		Song foundSong;
		for(Dataset c: clusters){
			for(Instance i: c){
				songToFindBPM = i.value(0);//get bpm
				foundSong = findSongwBPM(songToFindBPM, origSongs);
				sortedSongs.add(foundSong);
				System.out.println(foundSong.title);
			}
			System.out.println();
		}
		 */


		//////////////////PRINT SONG CONTINUUM//////////Method 1 only//////////
		/*
		System.out.println("Song Continuum:");
		for(Song s: sortedSongs){
			System.out.println(s.title);
		}
		System.out.println();
		 */

		////////////////PRINT DATA FOR EXCEL///////////////////////////////////
		System.out.println("Songs:");
		for(Song s: origSongs){
			System.out.println(s.title);
		}
		System.out.println();

		System.out.println("tThresh: ");
		for(Song s: origSongs){
			System.out.println(s.tThresh);
		}
		System.out.println();

		System.out.println("Low Noise: ");
		for(Song s: origSongs){
			System.out.println(s.noiseAmtLo);
		}
		System.out.println();

		System.out.println("High Noise: ");
		for(Song s: origSongs){
			System.out.println(s.noiseAmtHi);
		}
		System.out.println();

		System.out.println("Low Freqs: ");
		for(Song s: origSongs){
			System.out.println(s.midAnalLow);
		}
		System.out.println();

		System.out.println("Mid Freqs: ");
		for(Song s: origSongs){
			System.out.println(s.midAnalMid);
		}
		System.out.println();

		System.out.println("High Freqs: ");
		for(Song s: origSongs){
			System.out.println(s.midAnalHi);
		}
		System.out.println();

		System.out.println("Max Freqs: ");
		for(Song s: origSongs){
			System.out.println(s.maxFreq);
		}
		System.out.println();

		System.out.println("Centroid Amp Diffs: ");
		for(Song s: origSongs){
			System.out.println(s.centroidADiff);
		}
		System.out.println();

		System.out.println("Centroid Freq Diffs: ");
		for(Song s: origSongs){
			System.out.println(s.centroidFDiff);
		}
		System.out.println();

		System.out.println("Low over High Compression Ratio: ");
		for(Song s: origSongs){
			System.out.println(s.lowOverHigh);
		}
		System.out.println();

		System.out.println("Low over Mid Compression Ratio: ");
		for(Song s: origSongs){
			System.out.println(s.lowOverMid);
		}
		System.out.println();

		System.out.println("High over Low Compression Ratio: ");
		for(Song s: origSongs){
			System.out.println(s.highOverMid);
		}
		System.out.println();

		System.out.println("Spectral Peak Periodicity Ratio: ");
		for(Song s: origSongs){
			System.out.println(s.spectralPeakRatio);
		}
		System.out.println();

		new Gui(origSongs, songData);
	}
}