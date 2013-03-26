import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

@SuppressWarnings("serial")
public class ListenerRadio extends JRadioButton implements ActionListener
{
	ListenerRadio(String contentText)
	{
		setText(contentText);
		addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent event)
	{

	}
}
