import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import java.awt.image.*;

import javax.swing.*;

@SuppressWarnings("serial")
public class MapButton extends JButton
{
	public BufferedImage baseTile;
	private Image placedTile;
	private int xpos;
	private int ypos;
	
	MapButton(BufferedImage img, int x, int y)
	{
		Dimension size = new Dimension(40, 40);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		
		xpos = x;
		ypos = y;
		
		baseTile = img;
		placedTile = null;
	}
	
	public void paintComponent(Graphics g)
	{
		g.drawImage(baseTile, 0, 0, null);
		if (placedTile != null)
		{
			Graphics2D g2d = (Graphics2D) g;
			AlphaComposite ac = 
			      AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1); 
			  g2d.setComposite(ac); 
			g2d.drawImage(placedTile, 0, 0, 40, 40, null);
		}
	}
	
	public void changeImage(BufferedImage newTile)
	{
		baseTile=newTile;
		repaint();
	}
	
	public void defineOverlayImage(Image newTile)
	{
		placedTile = newTile;
		repaint();
	}
	
	public void removeOverlay()
	{
		placedTile = null;
		repaint();
	}
	
	public int getXPos()
	{
		return xpos;
	}
	
	public int getYPos()
	{
		return ypos;
	}
	
}
