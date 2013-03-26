import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class MainScreen  extends JFrame
{
	private static final int OPEN = 0;
	private static final int SAVE = 1;
	
	private static final int STEPMODE = 0;
	private static final int WAYPOINTMODE = 1;
	private static final int DELETEMODE = 2;
	
	private static final int EMPTYJOURNEY = 0;
	private static final int STARTEDJOURNEY = 1;
	private static final int FINISHEDJOURNEY = 2;
	
	private static final String STEPWORD = "Step";
	private static final String WAYPOINTWORD = "Waypoint";
	private static final String DELETEWORD = "Delete";
	private static final String SAVEWORD = "Save";
	private static final String OPENWORD = "Open";
	private static final String SAVEAS = "Save As";
	private static final String QUIT = "Quit";
	
	private static final String NEWJOURNEY = "New Journey";
	private static final String DELETEJOURNEY = "Delete Journey";
	
	private static final String ENTERNEWJOURNEYNAME = "Enter new journey name.";
	
	private static final String DEFAULTMAPDIRECTORY = "C:\\HistoryGame\\Maps\\";
	
	private static BufferedImage baseTile;
	private static BufferedImage redFlag;
	private static BufferedImage blueFlag;
	private static BufferedImage greenFlag;
	private static BufferedImage[] terrain1;
	private static BufferedImage[] terrain2;
	private static BufferedImage[] largeTerrain;
	private static BufferedImage[] fences;
	private static BufferedImage[] roads;
	
	private static Image stepOverlay;
	private static Image waypointOverlay;
	private static Image destinationOverlay;
	
	private int width;
	private int height;
	private int[][] mapData;
	private List<Journey> journeys;
	private Journey currentJourney;
	private List<int[]> startingPoints;
	private int mode;
	
	private MapPanel mapPanel;
	private JScrollPane scrollPane;
	private JourneyBox journeyBox;
	private File mapFile;
	
	MainScreen()
	{
		setUndecorated(true);
		setLayout(new GridBagLayout());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width, screenSize.height);
		loadAllImages();
		mode = STEPMODE;
		loadMap();
		
		setupScreen(mapData);
		setVisible(true);
	}
	
	private void loadMap()
	{
		mapFile = chooseMap(OPEN);
		
		try
		{
			mapData=readMapFile(mapFile);
		}
		catch (IOException e)
		{
			System.out.println(e);
		}

		mapPanel = new MapPanel(width, height, mapData)
		{
			public void buttonPress(int x, int y)
			{
				checkStep(x, y);
			}
		};
	}
	
	private void checkStep(int x, int y)
	{
		int journeyStatus = checkJourneyStatus(currentJourney);
		int startingPoint = checkStartingPoints(x,y);
		
		if (journeyStatus == EMPTYJOURNEY)
		{
			if (startingPoint != -1)
				newDeparture(x,y, startingPoint);
		}
		else
		{
			int oldStep = checkIsOldStep(x,y);
			if (journeyStatus == STARTEDJOURNEY 
				&& oldStep==-1
				&& checkIsAdjacentStep(x,y)
				&& mode != DELETEMODE)
			{
				if (startingPoint != -1)
					newConclusion(x,y, startingPoint);
				else if (mode == STEPMODE)
					newStep(x,y);
				else
					newWaypoint(x,y);
			}
			else if ((journeyStatus != EMPTYJOURNEY) 
					&& oldStep!= -1 
					&& mode == DELETEMODE)
			{
				deleteStep(x,y, oldStep);
			}
			else if (journeyStatus != EMPTYJOURNEY 
					&& oldStep != -1 
					&& startingPoint == -1)
			{
				editStep(x,y, oldStep);
			}
		}
	}
	private int checkJourneyStatus(Journey journey)
	{
		if (journey.getSteps().size()==0)
			return EMPTYJOURNEY;
		else if (currentJourney.getB() == -1)
			return STARTEDJOURNEY;
		else
			return FINISHEDJOURNEY;
	}
	
	private int checkStartingPoints(int x, int y)
	{
		for (int i=0;i<startingPoints.size();i++)
		{
			if (startingPoints.get(i)[0] == x && startingPoints.get(i)[1] == y)
				return i;
		}
		return -1;
	}
	
	private boolean checkIsAdjacentStep(int x, int y)
	{
		int currentJourneySize = currentJourney.getSteps().size();
		Step lastStep = currentJourney.getSteps().get(currentJourneySize-1);
		
		int [][] checkArray = {{-1,0},{1,0},{0,-1},{0,1}};
		
		for (int i=0;i<4;i++)
		{
			if ((x+checkArray[i][0])==lastStep.getX() && (y+checkArray[i][1])==lastStep.getY())
				return true;
		}
		return false;
	}

	private int checkIsOldStep(int x, int y)
	{
		int i=0;
		for (Step step: currentJourney.getSteps())
		{
			if (step.getX()==x && step.getY()==y)
				return i;
			i++;
		}
		return -1;
	}
	
	private void newDeparture(int x, int y, int startingPoint)
	{
		currentJourney.addStep(new Step(x,y,StepState.DESTINATION));
		currentJourney.setA(startingPoint);
		mapPanel.placeOverlay(x,y,destinationOverlay);
	}
	
	private void newConclusion(int x, int y, int startingPoint)
	{
		currentJourney.addStep(new Step(x,y,StepState.DESTINATION));
		currentJourney.setB(startingPoint);
		mapPanel.placeOverlay(x,y,destinationOverlay);
	}
	
	private void newStep(int x, int y)
	{
		currentJourney.addStep(new Step(x,y,StepState.STEP));
		mapPanel.placeOverlay(x,y,stepOverlay);
	}
	
	private void newWaypoint(int x, int y)
	{
		currentJourney.addStep(new Step(x,y,StepState.WAYPOINT));
		mapPanel.placeOverlay(x,y,waypointOverlay);
	}
	
	private void editStep(int x, int y, int oldStep)
	{
		if (mode==STEPMODE)
		{
			currentJourney.setStep(oldStep, new Step(x,y,StepState.STEP));
			mapPanel.placeOverlay(x,y,stepOverlay);
		}
		else
		{
			currentJourney.setStep(oldStep, new Step(x,y,StepState.WAYPOINT));
			mapPanel.placeOverlay(x,y,waypointOverlay);
		}
	}
	
	private void deleteStep(int x, int y, int oldStep)
	{
		currentJourney.setB(-1);
		
		List<Step> currentSteps = currentJourney.getSteps();
		
		for (int i=oldStep;i<currentSteps.size();i++)
			mapPanel.removeOverlay(currentSteps.get(i).getX(), currentSteps.get(i).getY());
		
		if (oldStep==0)
		{
			currentJourney.setSteps(new ArrayList<Step>());
			currentJourney.setA(-1);
		}
		else
		{
			currentJourney.setSteps(currentSteps.subList(0, oldStep));
		}
		
	}
	
	private void loadAllImages()
	{
	    baseTile = loadImage("Green.PNG");
	    redFlag = loadImage("GRedFlag.PNG");
	    blueFlag = loadImage("GBlueFlag.PNG");
	    greenFlag = loadImage("GGreenFlag.PNG");
	    
	    terrain1 = arrayfill("Forest",16);
	    terrain2 = arrayfill("Swamp",16);
	    largeTerrain = arrayfill("LargeSwamp",3);
	    fences = arrayfill("Fences",16);
	    roads = arrayfill("Road",16);
	    
	    destinationOverlay = loadOverlay("DestinationOverlay.PNG");
	    stepOverlay = loadOverlay("StepOverlay.PNG");
	    waypointOverlay = loadOverlay("WaypointOverlay.PNG");
	}
	
	private BufferedImage loadImage(String name)
	{
		BufferedImage img = null;
		try
		{
			img = ImageIO.read(new File("Images/" + name));
		}
		catch(IOException e)
		{
	    	JOptionPane.showMessageDialog(new JFrame(),
	    			name + " cannot be loaded. Exception: " + e, "Error",
	    	        JOptionPane.ERROR_MESSAGE);
		}	
		return img;
	}
	
	private Image loadOverlay(String name)
	{
	    Image overlay = loadImage(name);
		return MyTransparency.makeColorTransparent(overlay, Color.WHITE);
	}
	
	private BufferedImage[] arrayfill(String name, int number)
	{
		BufferedImage[] imageArray = new BufferedImage[number];
		String fullName;
			for(int i=0;i<number;i++)
			{
				fullName = "G" + name + (i+1) + ".PNG";
				imageArray[i ]= loadImage(fullName);
			}
		
		return imageArray;
	}
	
	private File chooseMap(int chooserMode)
	{
		File initialDirectory = new File(DEFAULTMAPDIRECTORY);
		JFileChooser myFileChooser = new JFileChooser(initialDirectory);
		MapFileFilter myFileFilter = new MapFileFilter();
		myFileChooser.addChoosableFileFilter(myFileFilter);
		
		int result;
		
		if (chooserMode==SAVE)
			result = myFileChooser.showSaveDialog(this);
		else
			result = myFileChooser.showOpenDialog(this);
		
		if (result==JFileChooser.APPROVE_OPTION)
			return myFileChooser.getSelectedFile();
		else
			return null;
	}
	
	private int[][] readMapFile(File myFile) throws IOException
	{
		FileReader myFileReader = null;
		myFileReader = new FileReader(myFile);
		
		int[] parameters = loadParameters(myFileReader);
		int[][] mapData = loadMapData(myFileReader, parameters);
		journeys = loadJourneys(myFileReader);
		
		if (journeys.size()==0)
		{
			journeys.add(new Journey("New Journey"));
			currentJourney = journeys.get(0);
		}
		
		width = parameters[0];
		height = parameters[1];
		
		return mapData;
	}
	
	private int[] loadParameters(FileReader myFileReader) throws IOException
	{
		char[] limitChars = new char[2];
		myFileReader.read(limitChars,0,2);
		
		int[] parameters = new int[2];
		
		parameters[0] = (int) limitChars[0];
		parameters[1] = (int) limitChars[1];
		
		return parameters;
	}
	
	private int[][] loadMapData(FileReader myFileReader, int[] parameters) throws IOException
	{
		int charNumber = (parameters[0] * parameters[1]);
		char[] dataChars = new char[charNumber];
		myFileReader.read(dataChars,0,charNumber);
		
		int[][] mapData = new int[parameters[0]][parameters[1]];
		startingPoints = new ArrayList<int[]>();
		
		for(int i=0;i<parameters[0];i++)
		{
			for(int j=0;j<parameters[1];j++)
			{
				mapData[i][j] = (int)dataChars[(parameters[1]*i)+j];
				if (mapData[i][j]==2 || mapData[i][j]==3 || mapData[i][j]==4)
					startingPoints.add(new int[]{i,j});
			}
		}
		
		return mapData;
	}
	
	private List<Journey> loadJourneys(FileReader myFileReader) throws IOException
	{
		List<Journey> newJourneys = new ArrayList<Journey>();
		char[] dataChars = new char[3];
	
		int data = myFileReader.read(dataChars, 0, 2);
		int counter = 0;
		
		while (data != -1)
		{
			List<Step> steps = new ArrayList<Step>();
			steps.add(new Step((int)dataChars[0], (int)dataChars[1], StepState.DESTINATION));
			Journey newJourney = new Journey(String.valueOf(counter));
			newJourney.setA(checkStartingPoints((int)dataChars[0], (int)dataChars[1]));
			StepState currentStepState;
			do
			{
				myFileReader.read(dataChars, 0, 3);
				currentStepState = StepState.getStepState((int)dataChars[2]);
				steps.add((new Step((int)dataChars[0], (int)dataChars[1], currentStepState)));
			}while (currentStepState != StepState.DESTINATION);
			counter++;
			newJourney.setB(checkStartingPoints((int)dataChars[0], (int)dataChars[1]));
			newJourney.setSteps(steps);
			newJourneys.add(newJourney);
			
			data = myFileReader.read(dataChars, 0, 2);
		}
			
		return newJourneys;
	}
	
	private void save(File file) throws IOException
	{
		FileWriter fileWriter = new FileWriter(file);
		saveMap(fileWriter);
		saveJourneys(fileWriter);
		fileWriter.close();
	}
	
	private void saveAs() throws IOException
	{
		mapFile = chooseMap(SAVE);
		save(mapFile);
	}
	
	private void saveMap(FileWriter fileWriter) throws IOException
	{
		fileWriter.write((char) mapData.length);
		fileWriter.write((char) mapData[0].length);
		for (int[] dataArray : mapData)
		{
			for (int data : dataArray)
			{
				fileWriter.write((char) data);
			}
		}	
	}
	
	
	private void saveJourneys(FileWriter fileWriter ) throws IOException
	{
		for (Journey journey : journeys)
		{
			List<Step> steps = journey.getSteps();
			
			fileWriter.write((char) steps.get(0).getX());
			fileWriter.write((char) steps.get(0).getY());
			
			for (int i=1;i<steps.size();i++)
			{
				fileWriter.write((char) steps.get(i).getX());
				fileWriter.write((char) steps.get(i).getY());
				fileWriter.write((char) steps.get(i).getType().valueOf());
			}

		}
	}
	
	private void setupScreen(int[][] mapData)
	{
		setLayout(new GridBagLayout());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension sizeForScrollPane = new Dimension(screenSize.width-300, screenSize.height-60);
		
		JScrollPane scrollPane = setupScrollPane(sizeForScrollPane, mapPanel);
		JPanel buttonPane = setupButtonPane();
		JPanel radioPane = setupRadioPane();
		JPanel journeyPane = setupJourneyPane();
		
	    add(scrollPane, setupScrollPaneConstraints(0,0));
	    add(buttonPane, setupComponentConstraints(1,0));
		add(radioPane, setupComponentConstraints(1,1));
		add(journeyPane, setupComponentConstraints(1,2));
	}
	
	private JPanel setupButtonPane()
	{
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new GridBagLayout());
		buttonPane.add(createOpenButton(), createButtonConstraints(0));
		buttonPane.add(createSaveButton(), createButtonConstraints(1));
		buttonPane.add(createSaveAsButton(), createButtonConstraints(2));
		buttonPane.add(createQuitButton(), createButtonConstraints(3));
		return buttonPane;
	}
	
	private GridBagConstraints setupControlConstraints()
	{
		GridBagConstraints controlConstraints = new GridBagConstraints();
		controlConstraints.gridy = 0;
		controlConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		controlConstraints.weightx = 1;
		controlConstraints.weighty = 1;
		
		return controlConstraints;
	}
	
	private GridBagConstraints createButtonConstraints(int x)
	{
		GridBagConstraints buttonConstraints = setupControlConstraints();
		buttonConstraints.gridx = x;
		return buttonConstraints;
	}
	
	private ListenerButton createOpenButton()
	{
		ListenerButton openButton = new ListenerButton(OPENWORD)
		{	
			public void actionPerformed(ActionEvent event)
			{
				loadMap();
			}
		};
		return openButton;
	}
	
	private ListenerButton createSaveButton()
	{
		ListenerButton saveButton = new ListenerButton(SAVEWORD)
		{	
			public void actionPerformed(ActionEvent event)
			{
				if (saveCheck())
				{
					try
					{
						if (mapFile == null)
							saveAs();
						else
							save(mapFile);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		};
		return saveButton;
	}
	
	private ListenerButton createSaveAsButton()
	{
		ListenerButton saveAsButton = new ListenerButton(SAVEAS)
		{	
			public void actionPerformed(ActionEvent event)
			{
				if (saveCheck())
				{
					try
					{
						saveAs();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		};
		return saveAsButton;
	}
	
	private boolean saveCheck()
	{
		for (Journey journey : journeys)
		{
			int counter = 0;
			if (checkJourneyStatus(journey) != FINISHEDJOURNEY)
			{
				JOptionPane.showMessageDialog(this, journeys.get(counter) +" is unfinished.");
				return false;
			}
			counter++;
		}
		return true;
	}
	
	private ListenerButton createQuitButton()
	{
		ListenerButton quitButton = new ListenerButton(QUIT)
		{	
			public void actionPerformed(ActionEvent event)
			{
				quit();
			}
		};
		return quitButton;
	}
	
	private void quit()
	{
		System.exit(0);
	}
	
	private JPanel setupRadioPane()
	{
		JPanel radioPane = new JPanel();
		radioPane.setLayout(new GridBagLayout());
		ListenerRadio stepButton = new ListenerRadio(STEPWORD)
		{
			public void actionPerformed(ActionEvent event)
			{
				mode = STEPMODE;
			}
		};
		ListenerRadio waypointButton = new ListenerRadio(WAYPOINTWORD)
		{
			public void actionPerformed(ActionEvent event)
			{
				mode = WAYPOINTMODE;
			}
		};
		ListenerRadio deleteButton = new ListenerRadio(DELETEWORD)
		{
			public void actionPerformed(ActionEvent event)
			{
				mode = DELETEMODE;
			}
		};
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(stepButton);
		buttonGroup.add(waypointButton);
		buttonGroup.add(deleteButton);

		stepButton.setSelected(true);
		radioPane.add(stepButton);
		radioPane.add(waypointButton);
		radioPane.add(deleteButton);
		
		return radioPane;
	}
	
	private JPanel setupJourneyPane()
	{
		JPanel journeyPane = new JPanel();
		journeyPane.setLayout(new GridBagLayout());
		ListenerButton newJourneyButton = new ListenerButton(NEWJOURNEY)
		{
			public void actionPerformed(ActionEvent event)
			{
				addNewJourney();
			}
		};
		ListenerButton deleteJourneyButton = new ListenerButton(DELETEJOURNEY)
		{
			public void actionPerformed(ActionEvent event)
			{
				deleteJourney();
			}
		};
		
		journeyBox = new JourneyBox();
		
		for (Journey journey : journeys)
		{
			journeyBox.addItem(journey.getName());
		}
		
		journeyBox.setPreferredSize(new Dimension(100, 20));
		
		journeyPane.add(newJourneyButton, setupComponentConstraints(0,0));
		journeyPane.add(deleteJourneyButton, setupComponentConstraints(0,1));
		journeyPane.add(journeyBox, setupComponentConstraints(0,2));
		return journeyPane;
	}
	
	private class JourneyBox extends JComboBox implements ActionListener
	{
		JourneyBox()
		{
			super();
			addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			selectJourney(this.getSelectedIndex());
		}
	}
	
	private void addNewJourney()
	{
		String newJourneyName = JOptionPane.showInputDialog(ENTERNEWJOURNEYNAME);
		journeys.add(new Journey(newJourneyName));
		journeyBox.addItem(newJourneyName);
	}
	
	private void deleteJourney()
	{
		int journeyIndex = journeys.indexOf(currentJourney);
		journeyBox.removeItemAt(journeyIndex);
		journeys.remove(journeyIndex);
	}
	
	private void selectJourney(int index)
	{
		if (currentJourney != null)
			clearJourney();
		currentJourney = journeys.get(index);
		showJourney();
	}
	
	private void clearJourney()
	{
		for(Step step : currentJourney.getSteps())
		{
			mapPanel.removeOverlay(step.getX(), step.getY());
		}
	}
	
	private void showJourney()
	{
		for(Step step : currentJourney.getSteps())
		{
			switch (step.getType())
			{
				case DESTINATION:
					mapPanel.placeOverlay(step.getX(), step.getY(), destinationOverlay);
					break;
				case STEP:
					mapPanel.placeOverlay(step.getX(), step.getY(), stepOverlay);
					break;
				case WAYPOINT:
					mapPanel.placeOverlay(step.getX(), step.getY(), waypointOverlay);
					break;
				default:
			}

		}
	}
	
	private JScrollPane setupScrollPane(Dimension sizeForScrollPane, MapPanel mapPanel)
	{
		scrollPane = new JScrollPane(mapPanel);
	    scrollPane.setPreferredSize(sizeForScrollPane);
	    scrollPane.setMinimumSize(sizeForScrollPane);
	    scrollPane.setMaximumSize(sizeForScrollPane);
	    scrollPane.setSize(sizeForScrollPane);
	    scrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));
	    return scrollPane;
	}
	
	private GridBagConstraints setupScrollPaneConstraints(int x, int y)
	{
		GridBagConstraints scrollConstraints = new GridBagConstraints();
		scrollConstraints.gridx = x;
		scrollConstraints.gridy = y;
		scrollConstraints.gridheight = 3;
		scrollConstraints.insets = new Insets(5,5,5,5);
		scrollConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		scrollConstraints.weightx = 1;
		scrollConstraints.weighty = 1;
		return scrollConstraints;
	}
	
	private GridBagConstraints setupComponentConstraints(int x, int y)
	{
		GridBagConstraints componentConstraints = new GridBagConstraints();
		componentConstraints.gridx = x;
		componentConstraints.gridy = y;
		componentConstraints.insets = new Insets(10,10,10,10);
		componentConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		componentConstraints.weightx = 1;
		componentConstraints.weighty = 1;
		return componentConstraints;
	}
	
	public static BufferedImage getBaseTile()
	{
		return baseTile;
	}
	
	public static BufferedImage getRedFlag()
	{
		return redFlag;
	}
	
	public static BufferedImage getGreenFlag()
	{
		return greenFlag;
	}
	
	public static BufferedImage getBlueFlag()
	{
		return blueFlag;
	}
	
	public static BufferedImage getTerrain1(int number)
	{
		return terrain1[number];
	}
	
	public static BufferedImage getTerrain2(int number)
	{
		return terrain2[number];
	}
	
	public static BufferedImage getLargeTerrain(int number)
	{
		return largeTerrain[number];
	}
	
	public static BufferedImage getFences(int number)
	{
		return fences[number];
	}

	public static BufferedImage getRoads(int number)
	{
		return roads[number];
	}
	
	public static void main(String args[]) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
               new MainScreen();
            }
        });
	}
}
