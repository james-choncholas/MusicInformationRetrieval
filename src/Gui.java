
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.javaml.core.*;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.distance.ManhattanDistance;
import processing.core.PApplet;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class Gui {

	public CustomPlayer plyr1;
	public CustomPlayer plyr2;

	public Gui(final ArrayList<Song> songs, final Dataset ordered)
	{
		//Start Visualizer
		PApplet.main(new String[] { "--present", "MusVisual" });

		JFrame guiFrame = new JFrame();

		//make sure the program exits when the frame closes
		guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		guiFrame.setTitle("MIRACLE");
		guiFrame.setSize(1500,1090);

		//This will center the JFrame in the middle of the screen
		guiFrame.setLocationRelativeTo(null);


		//Options for the JList1 Initial Song Selection
		final String[] allSongs = new String[songs.size()];
		for(int j =0; j < songs.size(); j++){
			allSongs[j] = songs.get(j).title;
		}

		//////////////////////////ALL SONGS PANEL//////////////////////////////
		//All Songs List
		JPanel panel1A = new JPanel(new BorderLayout());		
		final JList allSongL = new JList(allSongs);
		JScrollPane scrollPane = new JScrollPane(allSongL);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		panel1A.add(scrollPane, BorderLayout.CENTER);

		//buttons
		final JPanel panel1B = new JPanel(new BorderLayout());
		JButton play1 = new JButton("Play");
		JButton pause1 = new JButton("Stop");
		JButton skip1 = new JButton("FF -- >");
		panel1B.add(play1, BorderLayout.WEST);
		panel1B.add(pause1, BorderLayout.EAST);
		panel1B.add(skip1, BorderLayout.SOUTH);

		//assemble
		final JPanel panel1 = new JPanel(new BorderLayout());
		JLabel Lbl1 = new JLabel("All Songs:");
		Lbl1.setFont(new Font("Arial", Font.PLAIN, 18));
		panel1.add(Lbl1, BorderLayout.NORTH);
		panel1.add(scrollPane, BorderLayout.CENTER);
		panel1.add(panel1B, BorderLayout.SOUTH);


		///////////////////////SUGGESTED SONG PANEL/////////////////////////////
		final DefaultListModel<String> model = new DefaultListModel<String>();

		//Suggested Songs List
		final JPanel panel2A = new JPanel(new BorderLayout());
		JLabel Lbl2 = new JLabel("Suggested Songs:");
		Lbl2.setFont(new Font("Arial", Font.PLAIN, 18));
		final JList chosenSongsL = new JList(model);
		panel2A.add(Lbl2, BorderLayout.NORTH);
		panel2A.add(chosenSongsL, BorderLayout.CENTER);

		//buttons
		final JPanel panel2B = new JPanel(new BorderLayout());
		JButton play2 = new JButton("Play");
		JButton pause2 = new JButton("Stop");
		JButton skip2 = new JButton("FF -- >");
		panel2B.add(play2, BorderLayout.WEST);
		panel2B.add(pause2, BorderLayout.EAST);
		panel2B.add(skip2, BorderLayout.SOUTH);

		final JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(panel2A, BorderLayout.NORTH);
		panel2.add(panel2B, BorderLayout.SOUTH);

		////////////////////THE SUGGEST and INFO PANEL//////////////////////////
		final JTextArea songInfo = new JTextArea();
		songInfo.setName("Song Info");
		songInfo.setEditable(false);
		songInfo.setFont(new Font("Arial", Font.PLAIN, 24));

		final JPanel panel3B = new JPanel(new BorderLayout());
		JButton suggest = new JButton( "Suggest New Songs");
		panel3B.add(suggest);
		suggest.setFont(new Font("Arial", Font.PLAIN, 26));

		BufferedImage stftInit = new BufferedImage(1200,900,BufferedImage.TYPE_3BYTE_BGR);
		final JLabel picLabel = new JLabel(new ImageIcon(stftInit));

		final JPanel panel3 = new JPanel(new BorderLayout());
		panel3.add(songInfo, BorderLayout.NORTH);
		panel3.add(picLabel, BorderLayout.CENTER);
		panel3.add(panel3B, BorderLayout.SOUTH);

		/////////////////////////LISTENERS/////////////////////////////////////
		//Action Listener for suggest button
		suggest.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				if(allSongL.getSelectedValue() == null){
					//do nothing
				}else{
					model.clear();
					int i = allSongL.getSelectedIndex();
					Song s = songs.get(i);
					Instance sI = s.getDataInstance();
					Set<Instance>  nearest = ordered.kNearest(4, sI, new EuclideanDistance());
					for(Instance neighbor: nearest){
						double songToFindtThresh = neighbor.value(0);//get tThresh
						Song foundSong = findSongwtThresh(songToFindtThresh, songs);
						model.addElement(foundSong.title);
					}

					/* MANUAL SONG SELECTION MODE. ordered is arraylist of songs
					i = ordered.indexOf(s);
					if(i == 0){//first song in list
						model.addElement(ordered.get(i+1).title);
						model.addElement(ordered.get(i+2).title);

					} else if(i == ordered.size()-1){//Last song in list
						model.addElement(ordered.get(i-1).title);
						model.addElement(ordered.get(i-2).title);
					} else{
						model.addElement(ordered.get(i-1).title);
						model.addElement(ordered.get(i+1).title);
					}
					*/
				}
			}
		});

		//Action Listener for List of songs
		allSongL.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent arg0) {
				//Set info box
				int i = allSongL.getSelectedIndex();
				Song s = songs.get(i);
				String t =  "Title: " + s.title + "\n" +
						"Artist: " + s.artist +"\n"; /*+
						"Beats per Minute: " + s.bpm + "\n"+
						"DiffThresh (.3): " + s.tThresh + "\n" +
						"Noise at 11.1 Hz: " + s.noiseAmtLo + "\n" +
						"Noise at 22055 Hz: " + s.noiseAmtHi + "\n" +
						"Quantity of Frequency Content (0-100Hz): " + s.midAnalLow + "\n" +
						"Quantity of Frequency Content (110-500Hz): " + s.midAnalMid + "\n" +
						"Quantity of Frequency Content (500-900Hz): " + s.midAnalHi + "\n" +
						"Maximum Quantity of Frequencies: " +s.maxFreq + "\n";*/
				songInfo.setText(t);
				//Change Picture
				BufferedImage stft = null;
				try {
					stft = ImageIO.read(new File(System.getProperty("user.dir")+"\\storedData\\STFT" + s.title +".jpg"));
					picLabel.setIcon(new ImageIcon(stft));
				} catch (IOException e) {
					System.out.println(System.getProperty("user.dir")+"\\storedData\\STFT" + s.title +".jpg");
					System.out.println("image could not be read");
					System.out.println(stft == null);
				}
			}
		});

		//Add Action Listener for play button 1
		play1.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				if(allSongL.getSelectedValue() == null){
					//do nothing, nothing is selected
				}else{
					//find song
					int i = allSongL.getSelectedIndex();
					Song s = songs.get(i);
					//play song
					MusVisual.playSong(s.path);
				}
			}
		});

		//Add Action Listener for pause button 1
		pause1.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				MusVisual.stopSong();
			}
		});

		//Add Action Listener for skip button 1
		skip1.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				MusVisual.skip10s();
			}
		});

		//Add Action Listener for play button 2
		play2.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				if(chosenSongsL.getSelectedValue() == null){
					//do nothing, nothing is selected
				}else{
					//find song
					Song s = null;
					int i = chosenSongsL.getSelectedIndex();
					String title = model.get(i);
					for(Song searchedSong: songs){
						if(title.equals(searchedSong.title)){
							s = searchedSong;
							break;
						}
					}
					//play song
					MusVisual.playSong(s.path);

				}
			}
		});

		//Add Action Listener for pause button 2
		pause2.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				MusVisual.stopSong();
			}
		});

		//Add Action Listener for skip button 2
		skip2.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				MusVisual.skip10s();
			}
		});

		///////////////////////////LAYOUT//////////////////////////////////////
		//The JFrame uses the BorderLayout layout manager. So make new frame
		//Put the two JPanels and JButton in different areas.
		guiFrame.add(panel1, BorderLayout.WEST);
		guiFrame.add(panel2, BorderLayout.EAST);
		guiFrame.add(panel3);


		//make sure the JFrame is visible
		guiFrame.setVisible(true);


	}
	
	public static Song findSongwtThresh(double songToFindtThresh, ArrayList<Song> songs) {
		for(Song s: songs){
			if (s.tThresh == songToFindtThresh){
				return s;
			}
		}
		return null;
	}

}

