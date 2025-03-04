package engine;

import engine.types.Register;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public final class Assembler {

	// Assembler should not be initialized, given that it is essentially a static class
	private Assembler() {
		throw new IllegalStateException("Utility class");
	}

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
			if (!cleanLine.isEmpty()) {
				hasInstruction = true;
				parseInstruction(cleanLine, processor);
			}
		}
		if (!hasInstruction) {
			throw new IllegalArgumentException("Please enter one or more instructions");
		}
		
		if (data.trim().isEmpty())
			return;
			
		lines = data.trim().split("\\n+");
		for (String line : lines) {
			String cleanLine = removeComment(line).trim();
			if (!cleanLine.isEmpty()) {
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
		if (pseudoInstructions.keySet().contains(operation)) {
			parsePseudoInstruction(operation, operands, processor);
			return;
		}
		System.out.println(operation);
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
				if (operation.equals("lui") && (immediate < 0 || immediate > 0x3ff)) {
					throw new IllegalArgumentException("Immediate must be a value between 0x000 and 0x3ff");
				}
				else if (!operation.equals("lui") && (immediate < -64 || immediate > 63)) {
					throw new IllegalArgumentException("Signed immediate must be a value between -64 and 63");
				}
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

	/**
	 * Pseudo-instruction parser that formulates the pseudo-instructions into actual instructions and then adds them to the processor
	 * @param operation the pseudo-operation to be performed
	 * @param operands the operands of the pseudo-operation
	 * @param processor the processor (necessary to call back to add i)
	 */
	public static void parsePseudoInstruction(String operation, String[] operands, Processor processor)
	{
		// Do a quick check to ensure that the operands number is valid
		if (pseudoInstructions.get(operation) != operands.length) {
			throw new IllegalArgumentException("Invalid operands number");
		}
		StringBuilder actualInstructions = new StringBuilder();
		switch (operation) {
			case "nop": {
				actualInstructions.append("add r0, r0");
				parseInstruction(actualInstructions.toString(), processor);
				break;
			}
			case "halt": {
				actualInstructions.append("jalr r0, r0");
				parseInstruction(actualInstructions.toString(), processor);
				break;
			}
			case "lli": {
				// lli really translates down to addi with a mask of only the first 6 bits of the immediate
				actualInstructions.append("addi " + operands[0] + ", " + operands[0] + ", ");
				actualInstructions.append((parseInteger(operands[1]) & 0x3f));
				parseInstruction(actualInstructions.toString(), processor);
				break;
			}
			case "movi": {
				// movi is an lui + lli pair, which necessitates 2 stringbuilder parses
				int immediate = parseInteger(operands[1]);
				// Check immediate to make sure it is within bounds
				if (immediate < 0 || immediate > 0xFFFF)
				{
					throw new IllegalArgumentException("Immediate must be a value between 0x0000 and 0xfff");
				}
				// Append lui and shift the operand back 6 so it can be correctly shifted by lui
				actualInstructions.append("lui " + operands[0] + ", ");
				actualInstructions.append(immediate >> 6);
				parseInstruction(actualInstructions.toString(), processor);

				// Reset stringbuilder
				actualInstructions.setLength(0);

				// And then append addi (lli) with the first 6 bits to be parsed
				actualInstructions.append("addi " + operands[0] + ", " + operands[0] + ", ");
				actualInstructions.append(immediate & 0x3f);
				parseInstruction(actualInstructions.toString(), processor);
				break;
			}
			// .fill and .space are not compatible with this version of the RISC, since it has a dedicated memory space
			case ".fill": {

				break;
			}
			case ".space": {
				break;
			}
			default: {
				throw new IllegalCallerException("Invalid pseudo-instruction");
			}
		}
	}

	// The pseudoInstructions to check against as well as how many operands they have (Java object instantiation sucks)
	private static final HashMap<String, Integer> pseudoInstructions = new HashMap<>(Map.of("nop", 0, "halt", 0, "lli", 2, "movi", 2, ".fill", 1, ".space", 1));
}