package engine;

import engine.types.Register;

public class Assembler {

	public static void assemble(String data, String program, Processor processor) {
		if (program.trim().isEmpty())
			throw new IllegalArgumentException("Please enter one or more instructions");
		
		processor.clear();
		
		String[] lines = program.toLowerCase().trim().split("\\n+");
		boolean hasInstruction = false;
		for (String line : lines)
		{
			// Remove comments if there are any for each line, and trim it
			String cleanLine = removeComment(line).trim();
			// If the line still has something, parse it and indicate that instructions exist
			if (!cleanLine.isEmpty())
			{
				hasInstruction = true;
				parseInstruction(cleanLine, processor);
			}
		}
		if (!hasInstruction)
		{
			throw new IllegalArgumentException("Please enter one or more instructions");
		}
		
		if (data.trim().isEmpty())
			return;
			
		lines = data.trim().split("\\n+");
		for (String line : lines)
		{
			String cleanLine = removeComment(line).trim();
			if (!cleanLine.isEmpty())
			{
				parseData(cleanLine, processor);
			}
		}
	}

	/**
	 * Removes the comment section of a line (anything that goes after the '#' character)
	 * @param line the line string to be parsed
	 * @return the line without the comment section
	 */
	private static String removeComment(String line) {
        int commentIndex = line.indexOf('#');
        return (commentIndex >= 0) ? line.substring(0, commentIndex) : line;
    }
	
	private static void parseInstruction(String instruction, Processor processor) {
		String operation = instruction.substring(0, instruction.indexOf(' '));
		String[] operands = instruction.substring(instruction.indexOf(' ') + 1).split(",");
		for (int i = 0; i < operands.length; i++) {
			operands[i] = operands[i].trim();
		}
		Class<?>[] types = InstructionSet.getMethod(operation).getParameterTypes();
		if (types == null)
			throw new IllegalArgumentException(operation + " is an invalid operation");
		
		if (types.length != operands.length) 
			throw new IllegalArgumentException("Invalid operands number");
		
		Object[] parameters = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			if (types[i] == Register.class) {
				Register r = processor.getRegisterFile().getRegister(operands[i]);
				if (r == null)
					throw new IllegalArgumentException(operands[i] + " is an invalid register name");
				parameters[i] = r;
			} else if (types[i] == int.class) {
				int immediate = parseInteger(operands[i]);
				if (immediate < -64 || immediate > 63)
					throw new IllegalArgumentException("Immediate must be a value between -64 and 63");
				parameters[i] = immediate;
			}
		}
		processor.getMemory().addInstruction(operation, parameters);
	}
	
	private static void parseData(String data, Processor processor) {
		String[] operands = data.split("\\s+");
		if (operands.length != 2)
			throw new IllegalArgumentException(data + " is an invalid data format");
		
		int address = parseInteger(operands[0]);
		
		if (!processor.getMemory().isWordAddress(address))
			throw new IllegalArgumentException("Invalid word address (" + address + ")");
		
		processor.getMemory().setWord(address, parseShort(operands[1]));
	}

	public static int parseInteger(String number) {
		try {
			if (number.matches("-?\\d+")) 
				return Integer.parseInt(number);
			if (number.matches("0[xX][\\da-fA-F]+"))
				return Integer.parseInt(number.substring(2), 16);
		} catch (Exception e) {
			
		}
		throw new IllegalArgumentException(number + " is an invalid integer");
	}
	
	public static short parseShort(String number) {
		try {
			if (number.matches("-?\\d+")) 
				return Short.parseShort(number);
			if (number.matches("0[xX][\\da-fA-F]+"))
				return Short.parseShort(number.substring(2), 16);
		} catch (Exception e) {
			
		}
		throw new IllegalArgumentException(number + " is an invalid short");
	}

}