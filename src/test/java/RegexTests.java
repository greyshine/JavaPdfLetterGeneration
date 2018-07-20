import org.junit.Assert;
import org.junit.Test;

import de.greyshine.utils.Utils;

public class RegexTests {
	
	static String ptn_name = "\\w+([\\-\\s]\\w+)*";
	static String pattern = "\\s*"+ ptn_name +"\\s*,\\s*"+ ptn_name +"\\s*";
	
	@Test
	public void testFirstLastName() {
		
		System.out.println( pattern.replaceAll( "\\\\" , "\\\\\\\\") );
		
		String name = "  von Meiser-Eberhardt ,   Hans-Fred ";
		Assert.assertTrue( Utils.isMatch( name , pattern) );
		
		name = "Eckhardt, Hubert";
		Assert.assertTrue("fail: "+name, Utils.isMatch( name , pattern) );
	}

	@Test
	public void testFirstLastName2() {
		
		String name = "Wurst, Hans";
		String thePattern = "\\s*\\w+([\\-\\s]\\w+)*\\s*,\\s*\\w+([\\-\\s]\\w+)*\\s*";
		
		System.out.println( pattern.equals( thePattern ) );
		
		Assert.assertTrue( Utils.isMatch( name , thePattern ) );
	}

}
