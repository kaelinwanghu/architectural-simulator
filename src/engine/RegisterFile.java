package engine;

import java.util.ArrayList;

import engine.types.Register;

public class RegisterFile {

	private int pc;
	private ArrayList<Register> registers;
	
	public RegisterFile(int instructionsStartAddress) {
		registers = new ArrayList<Register>(8);
		for (int i = 0; i < 8; i++)
			registers.add(new Register(i, i != 0));
		pc = instructionsStartAddress;
	}
	
	public Register getRegister(String registerName) {
		for (Register register : registers)
			if (register.getName().equalsIgnoreCase(registerName)) 
				return register;
		
		return null;
	}
	
	public Object[] displayRegisters(boolean hex) {
		String[] headers = {"Register", "Word"}; 
		String[][] data = new String[registers.size()][2];
		for (int i = 0; i < data.length; i++) {
			Register r = registers.get(i);
			data[i][0] = r.getName();
			data[i][1] = String.format((hex)? "0x%04X" : "%d", r.getValue());
		}
		return new Object[]{data, headers, String.format("\nPC : " + ((hex)? "0x%04X" : "%d"), pc)};
	}
	
	public int getPc() {
		return pc;
	}

	public void setPc(int pc) {
		this.pc = pc;
	}
	
	public void incrementPc(int value) {
		pc += value;
	}
	
	public void clear(int instructionsStartAddress) {
		for (Register register : registers)
			register.clear();
		pc = instructionsStartAddress;
	}
	
}
