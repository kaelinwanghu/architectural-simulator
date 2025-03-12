package engine.types;

public class Register {

	private int number;
	private short value;
	private boolean writable;
	
	public Register(int number, boolean writable) {
		this.number = number;
		this.writable = writable;
	}

	public int getNumber() {
		return number;
	}
	
	public String getName() {
		return "R" + number;
	}
	
	public short getValue() {
		return value;
	}
	
	public void setValue(short value) {
		if (number == 0) {
			return;
		}
		if (!writable) {
			throw new IllegalArgumentException("Can not write to R" + number);
		}
		this.value = value;
	}
	
	public void clear() {
		value = 0;	
	}
	
	public boolean equals(Object o) {
		if (o instanceof Register r)
			return value == r.value;
		
		return false;
	}

	public String toString() {
		return "R" + number;
	}
	
}
