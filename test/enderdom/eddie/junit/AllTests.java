package enderdom.eddie.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import enderdom.eddie.junit.bio.sequence.SequencesTest;

@RunWith(Suite.class)
@SuiteClasses({SequencesTest.class})
public class AllTests {
	
}
