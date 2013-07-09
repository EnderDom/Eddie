package enderdom.eddie.graphics.bio;

import java.awt.image.BufferedImage;

public interface BioImg_Object {

	public BufferedImage getBufferedImage();

	public int parseLine(String line, int lineno) throws BioImg_Exception;
	

}
