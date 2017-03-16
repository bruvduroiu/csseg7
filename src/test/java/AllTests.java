import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ParserTest.class, TotalStatsTest.class, DataFormatTest.class, FiltersTest.class })
public class AllTests {

}
