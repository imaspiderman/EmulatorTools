package emuTools;
/*****
 * 
 * @author greg
 * Example of "function pointers" in java
 * 
public class Node {
    ...
    public void goNorth() { ... }
    public void goSouth() { ... }
    public void goEast() { ... }
    public void goWest() { ... }

    interface MoveAction {
        void move();
    }

    private MoveAction[] moveActions = new MoveAction[] {
        new MoveAction() { public void move() { goNorth(); } },
        new MoveAction() { public void move() { goSouth(); } },
        new MoveAction() { public void move() { goEast(); } },
        new MoveAction() { public void move() { goWest(); } },
    };

    public void move(int index) {
        moveActions[i].move();
    }
}
 */

public class Z80CPU {
	//8 bit Registers
	public static short registers[] = new short[]{0,0,0,0,0,0,0,0};
	//16 bit Registers
	public static int   regPC   = 0;
	public static int   regSP   = 0;
	public static int   regTemp = 0;
	//Register Codes
	public static byte  codeA = 7;
	public static byte  codeB = 0;
	public static byte  codeC = 1;
	public static byte  codeD = 2;
	public static byte  codeE = 3;
	public static byte  codeH = 4;
	public static byte  codeL = 5;
	public static byte  codeF = 6;
	//Flag value masks
	public static short ZeroFlagOnMask = 0x0080;
	public static short NegateFlagOnMask = 0x0040;
	public static short HalfCarryFlagOnMask = 0x0020;
	public static short CarryFlagOnMask = 0x0010;
	//Timers
	public static long mcycles = 0;
	//Temp Areas
	private static short _short[] = new short[8];
	
	/****
	 * Empty constructor
	 */
	public Z80CPU(){
	}
	
	/****
	 * Initialize the program counter and stack pointer
	 */
	public static void initZ80(){
		Z80CPU.regPC = 0x00000100;
		Z80CPU.regSP = 0x0000FFFE;
	}
	
	/****
	 * Get the clock cycles from the machine cycles. ~4 clock cycles per 1 machine cycle
	 * @return clock cycles
	 */
	public static long getClockCycles(){
		return Z80CPU.mcycles << 2;
	}
	
	/****
	 * Handles all LD 8bit opcodes
	 * @param opcode the byte[s] read for the opcode
	 */
	public static void LD8bit(byte[] opcode){
		Z80CPU._short[0] = (short)(opcode[0] & 0xFF);
		if((Z80CPU._short[0] & 0xC0) == 0x40){
			Z80CPU._short[1] = (short)((Z80CPU._short[0] & (0x07 << 3)) >> 3);//1st parameter
			Z80CPU._short[2] = (short)(Z80CPU._short[0] & 0x07);//2nd parameter
			if(Z80CPU._short[1] != 0x06 && Z80CPU._short[2] != 0x06){//check for register numbers
				Z80CPU.registers[Z80CPU._short[1]] = Z80CPU.registers[Z80CPU._short[2]];//Load register values
			}else{//value of 0x06 for a parameter means use the address of HL
				if(Z80CPU._short[1] == 0x06){//Write to address in HL the value from register in short[2]
					MMU.write8(((Z80CPU.registers[Z80CPU.codeH] << 8) | Z80CPU.registers[Z80CPU.codeL]), Z80CPU.registers[Z80CPU._short[2]]);
				}else{//Read value from HL into register in short[1]
					Z80CPU.registers[Z80CPU._short[1]] = MMU.read8(((Z80CPU.registers[Z80CPU.codeH] << 8) | Z80CPU.registers[Z80CPU.codeL]), false);
				}
			}
			
			Z80CPU.regPC++;//increment program counter
			Z80CPU.mcycles++;//increment machine cycles
		}
		if((Z80CPU._short[0] & 0xC0) == 0x00){
			Z80CPU._short[1] = (short)((Z80CPU._short[0] & (0x07 << 3)) >> 3);//1st parameter
			Z80CPU._short[2] = (short)(Z80CPU._short[0] & 0x07);//2nd parameter
			Z80CPU._short[3] = (short)(opcode[1] & 0xFF);//immediate 8bit value
			if(Z80CPU._short[1] == 0x06 && Z80CPU._short[2] == 0x06){//write immediate 8bit value to address HL
				MMU.write8(((Z80CPU.registers[Z80CPU.codeH] << 8) | Z80CPU.registers[Z80CPU.codeL]), Z80CPU._short[3]);
				Z80CPU.mcycles += 3;
				Z80CPU.regPC += 2;
			}else{
				if(Z80CPU._short[2]  == 0x06){//load immediate 8bit value into short[1] register
					Z80CPU.registers[Z80CPU._short[1]] = Z80CPU._short[3];
					Z80CPU.mcycles += 2;
					Z80CPU.regPC += 2;
				}
				if(Z80CPU._short[1] == 0x01 && Z80CPU._short[2] == 0x02){//load value at address in BC to register A
					Z80CPU.registers[Z80CPU.codeA] = MMU.read8(((Z80CPU.registers[Z80CPU.codeB] << 8) | Z80CPU.registers[Z80CPU.codeC]), false);
					Z80CPU.mcycles += 2;
					Z80CPU.regPC++;
				}
				if(Z80CPU._short[1] == 0x03 && Z80CPU._short[2] == 0x02){//load value at address in DE to register A
					Z80CPU.registers[Z80CPU.codeA] = MMU.read8(((Z80CPU.registers[Z80CPU.codeD] << 8) | Z80CPU.registers[Z80CPU.codeE]), false);
					Z80CPU.mcycles += 2;
					Z80CPU.regPC++;
				}
				if(Z80CPU._short[1] == 0x05 && Z80CPU._short[2] == 0x02){//load value at address from (HL) into A then increment HL
					Z80CPU.registers[Z80CPU.codeA] = MMU.read8((Z80CPU.registers[Z80CPU.codeH]<<8)|(Z80CPU.registers[Z80CPU.codeL]), false);
					Z80CPU.registers[Z80CPU.codeL] += 1;
					Z80CPU.registers[Z80CPU.codeH] += (Z80CPU.registers[Z80CPU.codeL] & 0x100) >> 8;
					Z80CPU.registers[Z80CPU.codeL] &= 0xFF;
					Z80CPU.registers[Z80CPU.codeH] &= 0xFF;
					Z80CPU.regPC++;
					Z80CPU.mcycles += 2;
				}
			}
		}
		if((Z80CPU._short[0] & 0xC0) == 0xC0){
			Z80CPU._short[1] = (short)((Z80CPU._short[0] & (0x07 << 3)) >> 3);//1st parameter
			Z80CPU._short[2] = (short)(Z80CPU._short[0] & 0x07);//2nd parameter
			Z80CPU._short[3] = (short)(opcode[1] & 0xFF);//immediate 8bit value or high 16bit value
			Z80CPU._short[4] = (short)(opcode[2] & 0xFF);//low 16bit value
			
			if(Z80CPU._short[1] == 0x06 && Z80CPU._short[2] == 0x02){//load value at address 0xFF00 + C to register A
				Z80CPU.registers[Z80CPU.codeA] = MMU.read8((Z80CPU.registers[Z80CPU.codeC] + 0xFF00), false);
				Z80CPU.mcycles += 2;
				Z80CPU.regPC++;
			}
			if(Z80CPU._short[1] == 0x04 && Z80CPU._short[2] == 0x02){//load value of register A to address of 0xFF00 + C
				MMU.write8((Z80CPU.registers[Z80CPU.codeA] + 0xFF00), Z80CPU.registers[Z80CPU.codeA]);
				Z80CPU.mcycles += 2;
				Z80CPU.regPC++;
			}
			if(Z80CPU._short[1] == 0x06 && Z80CPU._short[2] == 0x00){//load value of 0xFF00 + immediate8 into register A
				Z80CPU.registers[Z80CPU.codeA] = MMU.read8((0xFF00 + Z80CPU._short[3]), false);
				Z80CPU.mcycles += 3;
				Z80CPU.regPC++;
			}
			if(Z80CPU._short[1] == 0x04 && Z80CPU._short[2] == 0x00){//load value of register A into address 0xFF00 + immediate8
				MMU.write8((0xFF00 + Z80CPU._short[3]), Z80CPU.registers[Z80CPU.codeA]);
				Z80CPU.mcycles += 3;
				Z80CPU.regPC += 2;
			}
			if(Z80CPU._short[1] == 0x07 && Z80CPU._short[2] == 0x02){//load value at immediate16 into register A
				Z80CPU.registers[Z80CPU.codeA] = MMU.read8((Z80CPU._short[3]<<8)|(Z80CPU._short[4]), false);
				Z80CPU.mcycles += 4;
				Z80CPU.regPC += 3;
			}
			if(Z80CPU._short[1] == 0x05 && Z80CPU._short[2] == 0x02){//load value from register A into immediate16 address
				MMU.write8((Z80CPU._short[3]<<8)|(Z80CPU._short[4]), Z80CPU.registers[Z80CPU.codeA]);
				Z80CPU.mcycles += 4;
				Z80CPU.regPC += 3;
			}
		}
	}
}
