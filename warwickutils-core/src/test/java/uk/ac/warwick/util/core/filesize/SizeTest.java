package uk.ac.warwick.util.core.filesize;

import static org.junit.Assert.*;

import org.junit.Test;

public class SizeTest {

	@Test public void workingSize() {
		Size big = Size.mibibytes(3);
		assertEquals(3072, big.getKibibytes());
		assertEquals(3145728, big.getBytes());
	
		Size small = Size.kibibytes(128);
		assertEquals(128, small.getKibibytes());
		assertEquals(0, small.getMibibytes());
	}

}
