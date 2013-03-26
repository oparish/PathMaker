import java.io.File;

import javax.swing.filechooser.FileFilter;


public class MapFileFilter extends FileFilter {

	@Override
	public boolean accept(File arg0)
	{
		String fileName = arg0.getName();
		return (fileName.endsWith(".map"));
	}

	@Override
	public String getDescription()
	{
		return ".map";
	}

}
