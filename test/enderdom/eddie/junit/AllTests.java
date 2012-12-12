package enderdom.eddie.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import enderdom.eddie.junit.bio.sequence.SequencesTest;
import enderdom.eddie.ui.BasicPropertyLoader;

@RunWith(Suite.class)
@SuiteClasses({SequencesTest.class})
public class AllTests {
	AllTests(){
		BasicPropertyLoader.configureProps("test/log4j.properties", "test/log4j.properties");
	}
}
