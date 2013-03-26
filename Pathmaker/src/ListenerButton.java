import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

@SuppressWarnings("serial")
public class ListenerButton extends JButton implements ActionListener
{
	ListenerButton(String contentText)
	{
		setText(contentText);
		addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent event)
	{

	}
}
