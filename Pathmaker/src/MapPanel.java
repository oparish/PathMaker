import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;

import javax.swing.*;

@SuppressWarnings("serial")
class MapPanel extends JPanel implements Scrollable, ActionListener
{
	private static final int REDFLAGVALUE=2;
	private static final int BLUEFLAGVALUE=3;
	private static final int GREENFLAGVALUE=4;
	
	private int mapWidth;
	private int mapHeight;
	
	private MapButton[][] buttons;
	
	private int[][][][] controller;
	private int[][] mapValues;

  public MapPanel(int width, int height, int[][] data)
  {
	mapWidth = width;
	mapHeight = height;
	  
	setLayout(new GridBagLayout());    
	setupSizing();
    setupTiles(data);
  }
  
  private void setupTiles(int[][] data)
  {
	   setupController();
	   buttons = new MapButton[mapWidth][mapHeight];
	   mapValues = data;
	   
	   loadTiles(mapValues);
	   finishTiles();
  }
  
  private void setupSizing()
  {
	    Dimension size = new Dimension(mapWidth * 40, mapHeight * 40);
	    setPreferredSize(size);
	    setMinimumSize(size);
	    setMaximumSize(size);
	    setSize(size);
  }
  
  private void setupController()
  {
	    controller = new int[2][2][2][2];
	    controller[0][0][0][0] = 0;
	    controller[0][0][0][1] = 1;
	    controller[0][0][1][0] = 2;
	    controller[0][0][1][1] = 5;
	    controller[0][1][0][0] = 3;
	    controller[0][1][0][1] = 6;
	    controller[0][1][1][0] = 8;
	    controller[0][1][1][1] = 14;
	    controller[1][0][0][0] = 4;
	    controller[1][0][0][1] = 7;
	    controller[1][0][1][0] = 9;
	    controller[1][0][1][1] = 13;
	    controller[1][1][0][0] = 10;
	    controller[1][1][0][1] = 12;
	    controller[1][1][1][0] = 11;
	    controller[1][1][1][1] = 15;
  }
	
	private void addButton(int x, int y)
	{
		add(buttons[x][y], setupButtonConstraints(x,y));
	}
	
	private GridBagConstraints setupButtonConstraints(int x, int y)
	{
	    GridBagConstraints buttonConstraints = new GridBagConstraints();
	    buttonConstraints.anchor=GridBagConstraints.FIRST_LINE_START;
	    buttonConstraints.weightx=0;
	    buttonConstraints.weighty=0;
	    buttonConstraints.gridx = x;
	    buttonConstraints.gridy = y;
	    return buttonConstraints;
	}
	
	private void placeVillage(int x, int y, int flagType)
	{
		int fenceCounter = 0;
		
		for (int j=-3;j<4;j++)
		{
			for (int i=-3;i<4;i++)
			{
				if ((i==-3 || i==3 || j==-3 || j==3))
					adjustTilesAdjacentToVillage(x+i,y+j);
				else if ((i==-2 || i==2 || j==-2 || j==2))
				{
					placeFence(x+i,y+j,fenceCounter);
					fenceCounter++;
				}
				else if (i==0 && j==0)
					placeFlag(x, y, flagType);
				else
					placeOpenArea(x+i,y+j);
			}
		}
	}
	
	private void adjustTilesAdjacentToVillage(int a, int b)
	{
		if (a!=-1 && b!=-1 && a!=mapWidth && b!=mapHeight)
			changeTile(a,b,canvasCheck(a,b));
	}
	
	private void placeFence(int a, int b, int fenceCounter)
	{
		mapValues[a][b] = 7;
		changeTile(a,b,MainScreen.getFences(fenceCounter));
	}
	
	private void placeFlag(int x, int y, int flagType)
	{
		mapValues[x][y] = flagType;
		if (flagType==REDFLAGVALUE)
			changeTile(x,y,MainScreen.getRedFlag());
		else if (flagType==GREENFLAGVALUE)
			changeTile(x,y,MainScreen.getGreenFlag());
		else if (flagType==BLUEFLAGVALUE)
			changeTile(x,y,MainScreen.getBlueFlag());
	}
	
	private void placeOpenArea(int a, int b)
	{
		mapValues[a][b] = 7;
		changeTile(a,b,MainScreen.getBaseTile());
	}
	
	private BufferedImage canvasCheck(int x,int y)
	{
		if (mapValues[x][y]==7)
			return null;
		
		int a = 0;
		int b = 0;
		int c = 0;
		int d = 0;
		BufferedImage transferTile = MainScreen.getBaseTile();
		
		if (x!=0)
		{
			if (mapValues[x-1][y]==mapValues[x][y])
				a = 1;
		}
		if (y!=0)
		{
			if (mapValues[x][y-1]==mapValues[x][y])
				d = 1;
		}
		if (x!=(mapWidth-1))
		{
			if (mapValues[x+1][y]==mapValues[x][y])
				c = 1;
		}
		if (y!=(mapHeight-1))
		{
			if (mapValues[x][y+1]==mapValues[x][y])
				b = 1;
		}
		
		switch (mapValues[x][y])
		{
		case 1:
			transferTile = MainScreen.getBaseTile();
			break;
		case 2:
			transferTile = MainScreen.getRedFlag();
			break;
		case 3:
			transferTile = MainScreen.getBlueFlag();
			break;
		case 4:
			transferTile = MainScreen.getGreenFlag();
			break;
		case 5:
			transferTile = MainScreen.getTerrain1(controller[a][b][c][d]);
			break;
		case 6:
			transferTile = MainScreen.getTerrain2(controller[a][b][c][d]);
			break;
		case 8:
			transferTile = MainScreen.getRoads(controller[a][b][c][d]);
			break;
		case 9:
			transferTile = MainScreen.getLargeTerrain(1);
			break;
		case 10:
			transferTile = MainScreen.getLargeTerrain(0);
			break;
		case 11:
			transferTile = MainScreen.getLargeTerrain(2);
			break;
		}
		return transferTile;
	}
	
	private void changeTile(int x, int y, BufferedImage image)
	{
		((MapButton)buttons[x][y]).changeImage(image);
	}
	
	private void loadTiles(int[][] mapData)
	{
		for (int i = 0; i<mapWidth; i++)
		{
			for (int j = 0; j<mapHeight; j++)
			{
				loadButton(i,j,mapData[i][j]);
			}
		}
	}

	private void loadButton(int i, int j, int type)
	{
		mapValues[i][j] = type;
		buttons[i][j] = new MapButton(null, i, j);
		buttons[i][j].addActionListener(this);
	}
	
	private void finishTiles()
	{
		for (int i = 0; i<mapWidth; i++)
		{
			for (int j = 0; j<mapHeight; j++)
			{
				if (mapValues[i][j]==REDFLAGVALUE ||
					mapValues[i][j]==BLUEFLAGVALUE ||
					mapValues[i][j]==GREENFLAGVALUE)
					placeVillage(i,j, mapValues[i][j]);
				else if (mapValues[i][j]!=7)
					changeTile(i,j,canvasCheck(i,j));
				addButton(i,j);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		MapButton eventButton = (MapButton) e.getSource();
		buttonPress(eventButton.getXPos(), eventButton.getYPos());	
	}
	
	public void placeOverlay(int x, int y, Image overlay)
	{
		buttons[x][y].defineOverlayImage(overlay);
	}
	
	public void removeOverlay(int x, int y)
	{
		buttons[x][y].removeOverlay();
	}
	
	public void buttonPress(int x, int y){};
	
	public int MapWidth()
	{
		return mapWidth;
	}

	public int MapHeight()
	{
		return mapHeight;
	}
	
	public int[][] MapValues()
	{
		return mapValues;
	}
	
@Override
public Dimension getPreferredScrollableViewportSize() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public boolean getScrollableTracksViewportHeight() {
	// TODO Auto-generated method stub
	return false;
}

@Override
public boolean getScrollableTracksViewportWidth() {
	// TODO Auto-generated method stub
	return false;
}

@Override
public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
	// TODO Auto-generated method stub
	return 0;
}
}
