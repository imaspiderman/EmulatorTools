package emuTools;

public class MMU {

	public static byte[] memoryAreas;
	private static short _short;
	private static int _int;
	private static long _long;
	
	/****
	 * Since java does not have unsigned types each function will use the next
	 * largest size type to handle its value. So short for byte, int for short etc..
	 * Nothing fancy here just one bing byte area for a memory area.
	 * All functions assume Little Endian format
	 */
	public MMU(){		
	}
	/****
	 * Create all memory areas needed for emulator
	 * @param numAreas Number of different areas to create
	 * @param byteSize Byte size of areas
	 */
	public static void createMemoryArea(int byteSize){
		memoryAreas = new byte[byteSize];
	}
	
	/****
	 * Read in a byte of data
	 * @param address
	 * @return
	 */
	public static short read8(int address, boolean signed){
		_short = (signed)?((short)memoryAreas[address]):((short)(memoryAreas[address] & 0xFF));
		return _short;
	}
	
	/****
	 * Read in 2 bytes of data
	 * @param address
	 * @return
	 */
	public static int read16(int address, boolean signed){
		_int = (signed)?((((memoryAreas[address+1] & 0xFF) << 8)|(memoryAreas[address] & 0xFF)) << 16 >> 16):(((memoryAreas[address+1] & 0xFF) << 8)|(memoryAreas[address] & 0xFF));
		return _int;
	}
	
	/****
	 * Read in 4 bytes of data
	 * @param address
	 * @return
	 */
	public static long read32(int address, boolean signed){
		if(!signed){
			_long = (memoryAreas[address+3] << 24) & 0xFFFFFFFFl;
			_long |= (memoryAreas[address+2] << 16) & 0xFFFFFFFFl;
			_long |= (memoryAreas[address+1] << 8) & 0xFFFFFFFFl;
			_long |= memoryAreas[address] & 0xFFFFFFFFl;
		}else{
			_long = (memoryAreas[address+3] << 24) & 0xFFFFFFFFl;
			_long |= (memoryAreas[address+2] << 16) & 0xFFFFFFFFl;
			_long |= (memoryAreas[address+1] << 8) & 0xFFFFFFFFl;
			_long |= (memoryAreas[address] & 0xFFFFFFFFl) << 32 >> 32;
		}
		return _long;
	}
	
	/****
	 * Write a byte of data
	 * @param address
	 * @param value
	 */
	public static void write8(int address, short value){
		memoryAreas[address] = 0;//Clear byte
		
		memoryAreas[address] |= (value & 0x00FF);//Write lower byte
	}
	
	/****
	 * Write 2 bytes of data
	 * @param address
	 * @param value
	 */
	public static void write16(int address, int value){
		memoryAreas[address] = 0;
		memoryAreas[address+1] = 0;
		
		memoryAreas[address] |= (value & 0x00FF);
		memoryAreas[address+1] |= ((value>>8) & 0x00FF);
	}
	
	/****
	 * Write 4 bytes of data
	 * @param address
	 * @param value
	 */
	public static void write32(int address, long value){
		memoryAreas[address] = 0;
		memoryAreas[address+1] = 0;
		memoryAreas[address+2] = 0;
		memoryAreas[address+3] = 0;
		
		memoryAreas[address] |= (value & 0x00FF);
		memoryAreas[address+1] |= ((value>>8) & 0x00FF);
		memoryAreas[address+2] |= ((value>>16) & 0x00FF);
		memoryAreas[address+3] |= ((value>>24) & 0x00FF);
	}
	
	/****
	 * Used for quickly displaying memory values
	 * @param begin start address
	 * @param end end address
	 */
	public static void displayMemory(int begin, int end){
		int t = 0;
		for(int i=begin; i<end; i++){
			System.out.print(String.format("%02x ", memoryAreas[i]));
			t++;
			if(t==16){
				System.out.println();
				t=0;
			}
		}
	}
}
