package enderdom.eddie.cli;

import java.util.ListIterator;

import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

public class LazyPosixParser extends PosixParser {
    
	@Override
    protected void processOption(String arg, @SuppressWarnings("rawtypes") ListIterator iter) throws ParseException
    {
        try {
            super.processOption(arg, iter);
        } catch (ParseException e) {
        	Logger.getRootLogger().trace("Parse Exception for arg: "+arg+" was ignored.");
        }
    }
}
