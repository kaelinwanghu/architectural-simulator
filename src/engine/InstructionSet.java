package engine;
import java.lang.reflect.Method;

import engine.types.FunctionType;
import engine.types.Register;


public class InstructionSet {
	
	private Processor processor;
	
	public InstructionSet(Processor processor) {
		this.processor = processor;
	}
	
	public Object[] add(Register r1, Register r2, Register r3) {
		r1.setValue((short)(r2.getValue() + r3.getValue()));
		return new Object[]{FunctionType.ADD, r1.getNumber(), -1};
	}
	
	public Object[] addi(Register r1, Register r2, int immediate) {
		r1.setValue((short)(r2.getValue() + immediate));
		return new Object[]{FunctionType.ADD, r1.getNumber(), -1};
	}
	
	public Object[] sub(Register r1, Register r2, Register r3) {
		r1.setValue((short)(r2.getValue() - r3.getValue()));
		return new Object[]{FunctionType.ADD, r1.getNumber(), -1};
	}
	
	public Object[] subi(Register r1, Register r2, int immediate) {
		r1.setValue((short)(r2.getValue() - immediate));
		return new Object[]{FunctionType.ADD, r1.getNumber(), -1};
	}
	
	
	public Object[] and(Register r1, Register r2, Register r3) {
		r1.setValue((short)(r2.getValue() & r3.getValue()));
		return new Object[]{FunctionType.ALU, r1.getNumber(), -1};
	}
	
	public Object[] andi(Register r1, Register r2, int immediate) {
		r1.setValue((short)(r2.getValue() & immediate));
		return new Object[]{FunctionType.ALU, r1.getNumber(), -1};
	}
	
	public Object[] or(Register r1, Register r2, Register r3) {
		r1.setValue((short)(r2.getValue() | r3.getValue()));
		return new Object[]{FunctionType.ALU, r1.getNumber(), -1};
	}
	
	public Object[] ori(Register r1, Register r2, int immediate) {
		r1.setValue((short)(r2.getValue() | immediate));
		return new Object[]{FunctionType.ALU, r1.getNumber(), -1};
	}
	
	public Object[] nand(Register r1, Register r2, Register r3) {
		r1.setValue((short)(~(r2.getValue() & r3.getValue())));
		return new Object[]{FunctionType.ALU, r1.getNumber(), -1};
	}
	
	public Object[] nor(Register r1, Register r2, Register r3) {
		r1.setValue((short)(~(r2.getValue() | r3.getValue())));
		return new Object[]{FunctionType.ALU, r1.getNumber(), -1};
	}
	
	
	public Object[] mul(Register r1, Register r2, Register r3) {
		r1.setValue((short)(r2.getValue() * r3.getValue()));
		return new Object[]{FunctionType.MULTIPLY, r1.getNumber(), -1};
	}
	
	public Object[] muli(Register r1, Register r2, int immediate) {
		r1.setValue((short)(r2.getValue() * immediate));
		return new Object[]{FunctionType.MULTIPLY, r1.getNumber(), -1};
	}
	
	public Object[] div(Register r1, Register r2, Register r3) {
		if (r3.getValue() == 0)
			throw new IllegalArgumentException("Can not divide by zero");
		
		r1.setValue((short)(r2.getValue() / r3.getValue()));
		return new Object[]{FunctionType.DIVIDE, r1.getNumber(), -1};
	}
	
	public Object[] divi(Register r1, Register r2, int immediate) {
		if (immediate == 0)
			throw new IllegalArgumentException("Can not divide by zero");
		
		r1.setValue((short)(r2.getValue() / immediate));
		return new Object[]{FunctionType.DIVIDE, r1.getNumber(), -1};
	}
	
	public Object[] mod(Register r1, Register r2, Register r3) {
		if (r3.getValue() == 0)
			throw new IllegalArgumentException(r2.getValue() + " % 0 is undefined");
		
		r1.setValue((short)(r2.getValue() % r3.getValue()));
		return new Object[]{FunctionType.DIVIDE, r1.getNumber(), -1};
	}
	
	public Object[] modi(Register r1, Register r2, int immediate) {
		if (immediate == 0)
			throw new IllegalArgumentException(r2.getValue() + " % 0 is undefined");
		
		r1.setValue((short)(r2.getValue() % immediate));
		return new Object[]{FunctionType.DIVIDE, r1.getNumber(), -1};
	}
		
	
	public Object[] lw(Register r1, Register r2, int immediate) {
		int effectiveAddress = r2.getValue() + immediate;
		
		if (!processor.getMemory().isWordAddress(effectiveAddress))
			throw new IllegalArgumentException("Invalid word address (" + effectiveAddress + ")");
		
		int time1 = processor.getDataAccessTime();
		r1.setValue(Helpers.toWord(processor.getDataCache(0).getData(effectiveAddress, 2)));
		int time2 = processor.getDataAccessTime();
		return new Object[]{FunctionType.LOAD, r1.getNumber(), effectiveAddress, time2 - time1};
	}
	
	public Object[] sw(Register r1, Register r2, int immediate) {
		int effectiveAddress = r2.getValue() + immediate;
		
		if (!processor.getMemory().isWordAddress(effectiveAddress))
			throw new IllegalArgumentException("Invalid word address (" + effectiveAddress + ")");
		
		int time1 = processor.getDataAccessTime();
		processor.getDataCache(0).setData(effectiveAddress, Helpers.toBytes(r1.getValue()));
		int time2 = processor.getDataAccessTime();
		return new Object[]{FunctionType.STORE, -1, effectiveAddress, time2 - time1};
	}
	
	
	public Object[] beq(Register r1, Register r2, int immediate) {
		if (r1.getValue() == r2.getValue())
			processor.getRegisterFile().incrementPc(immediate);
		
		return new Object[]{FunctionType.BRANCH, -1, processor.getRegisterFile().getPc()};
	}
	
	public Object[] bne(Register r1, Register r2, int immediate) {
		if (r1.getValue() != r2.getValue())
			processor.getRegisterFile().incrementPc(immediate);
		
		return new Object[]{FunctionType.BRANCH, -1, processor.getRegisterFile().getPc()};
	}
	
	public Object[] bgt(Register r1, Register r2, int immediate) {
		if (r1.getValue() > r2.getValue())
			processor.getRegisterFile().incrementPc(immediate);
		
		return new Object[]{FunctionType.BRANCH, -1, processor.getRegisterFile().getPc()};
	}
	
	public Object[] blt(Register r1, Register r2, int immediate) {
		if (r1.getValue() < r2.getValue())
			processor.getRegisterFile().incrementPc(immediate);
		
		return new Object[]{FunctionType.BRANCH, -1, processor.getRegisterFile().getPc()};
	}

	public Object[] bge(Register r1, Register r2, int immediate) {
		if (r1.getValue() >= r2.getValue())
			processor.getRegisterFile().incrementPc(immediate);
		
		return new Object[]{FunctionType.BRANCH, -1, processor.getRegisterFile().getPc()};
	}
	
	public Object[] ble(Register r1, Register r2, int immediate) {
		if (r1.getValue() <= r2.getValue())
			processor.getRegisterFile().incrementPc(immediate);
		
		return new Object[]{FunctionType.BRANCH, -1, processor.getRegisterFile().getPc()};
	}
	
	
	public Object[] jmp(Register r, int immediate) {
		processor.getRegisterFile().incrementPc(r.getValue() + immediate);
		return new Object[]{FunctionType.JUMP, -1, processor.getRegisterFile().getPc()};
	}
	
	public Object[] ret(Register r) {
		processor.getRegisterFile().setPc(r.getValue());
		return new Object[]{FunctionType.JUMP, -1, (int)r.getValue()};
	}
	
	public Object[] jalr(Register r1, Register r2) {
		r1.setValue((short) (processor.getRegisterFile().getPc()));
		processor.getRegisterFile().setPc(r2.getValue());
		return new Object[]{FunctionType.JUMP_AND_LINK, r1.getNumber(), (int)r2.getValue()};
	}
	
	
	public static Method getMethod(String operation) {
		Method[] methods = InstructionSet.class.getDeclaredMethods();
		for (Method method : methods) {
			if (method.getName().equals(operation))
				return method;
		}
		return null;
	}
	
}
