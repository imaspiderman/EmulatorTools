package emuTools;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MMU.createMemoryArea(16);
		MMU.write8(0, (short)255);
		MMU.write16(1, 65535);
		MMU.write32(3, 4294967295l);
		
		MMU.displayMemory(0, 16);
		
		System.out.println();
		System.out.println(String.format("Read 8  signed   = %d", MMU.read8(0, true)));
		System.out.println(String.format("Read 16 signed   = %d", MMU.read16(1, true)));
		System.out.println(String.format("Read 32 signed   = %d", MMU.read32(3, true)));
		System.out.println(String.format("Read 8  unsigned = %d", MMU.read8(0, false)));
		System.out.println(String.format("Read 16 unsigned = %d", MMU.read16(1, false)));
		System.out.println(String.format("Read 32 unsigned = %d", MMU.read32(3, false)));
	}

}
