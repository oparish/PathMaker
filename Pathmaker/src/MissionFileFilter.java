import java.io.File;

import javax.swing.filechooser.FileFilter;


public class MissionFileFilter extends FileFilter {

	@Override
	public boolean accept(File arg0)
	{
		String fileName = arg0.getName();
		return (fileName.endsWith(".xml"));
	}

	@Override
	public String getDescription()
	{
		return ".xml";
	}

}
