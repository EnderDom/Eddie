package enderdom.eddie.graphics.bio;

import java.awt.image.BufferedImage;

public interface BioImg_Object {

	public BufferedImage getBufferedImage();

	public void parseLine(String line, int lineno) throws BioImg_Exception;
	

}
