package engine;

import engine.types.Register;
import engine.types.Instruction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public final class Assembler {

	// Assembler should not be initialized, given that it is essentially a static class
	private Assembler() {
		throw new IllegalStateException("Utility class");
	}

	public static void assemble(String data, String program, Processor processor) {
		if (program.trim().isEmpty())
			throw new IllegalArgumentException("Please enter one or more instructions");
		
		// Resetting static variables
		processor.clear();
		tags.clear();
		instructionAddress = 0;

		
		String[] lines = program.toLowerCase().trim().split("\\n+");
		List<String> instructionLines = preprocessProgram(lines);
        if (instructionLines.isEmpty()) {
            throw new IllegalArgumentException("Please enter one or more instructions");
		}
		boolean hasInstruction = false;
		for (String line : instructionLines)
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

		// changeSymbolicTags(processor);
	}

	private static List<String> preprocessProgram(String[] lines) {
	List<String> instructions = new ArrayList<>();
	for (String line : lines) {
		String cleanLine = removeComment(line).trim();
		if (cleanLine.isEmpty()) {
			continue;
		}

		// Check if a label is present.
		if (cleanLine.contains(":")) {
			String[] parts = cleanLine.split(":", 2);
			String label = parts[0].trim();
			if (label.isEmpty()) {
				throw new IllegalArgumentException("Empty label");
			}
			if (parts[1].trim().isEmpty()) {
				throw new IllegalArgumentException("Cannot have a line with just a label");
			}
			// Record the label with the current instruction address 
			tags.put(label, instructionAddress);
			cleanLine = parts[1].trim();
		}
		if (!cleanLine.isEmpty()) {
			instructions.add(cleanLine);
			instructionAddress += 2; // 2 bytes per instruction
		}
	}
	return instructions;
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
		// If the instruction is a pseudo-instruction, then parse that and return
		if (pseudoInstructions.keySet().contains(operation)) {
			parsePseudoInstruction(operation, operands, processor);
			return;
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
				Integer immediate = parseIntegerNoThrow(operands[i]); 
				if (immediate != null)
				{
					if (operation.equals("lui") && (immediate < 0 || immediate > 0x3ff)) {
						throw new IllegalArgumentException("Upper immediate must be a value between 0x000 and 0x3ff");
					}
					else if (!operation.equals("lui") && (immediate < -64 || immediate > 63)) {
						throw new IllegalArgumentException("Signed immediate must be a value between -64 and 63");
					}
					parameters[i] = immediate;
				}
				else
				{
					if (operation.equals("beq")) {
                        parameters[i] = operands[i];
					}
					else {
						throw new IllegalArgumentException("Invalid immediate operand: " + operands[i]);
					}
				}
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
			throw new IllegalCallerException("Failed to parse integer");
		}
		throw new IllegalArgumentException(number + " is an invalid integer");
	}

	public static Integer parseIntegerNoThrow(String number) {
		try {
			if (number.matches("-?\\d+")) 
				return Integer.parseInt(number);
			if (number.matches("0[xX][\\da-fA-F]+"))
				return Integer.parseInt(number.substring(2), 16);
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	
	public static short parseShort(String number) {
		try {
			if (number.matches("-?\\d+")) 
				return Short.parseShort(number);
			if (number.matches("0[xX][\\da-fA-F]+"))
				return Short.parseShort(number.substring(2), 16);
		} catch (Exception e) {
			throw new IllegalCallerException("Failed to parse short");
		}
		throw new IllegalArgumentException(number + " is an invalid short");
	}

	/**
	 * Pseudo-instruction parser that formulates the pseudo-instructions into actual instructions and then adds them to the processor
	 * @param operation the pseudo-operation to be performed
	 * @param operands the operands of the pseudo-operation
	 * @param processor the processor (necessary to call back to add i)
	 */
	private static void parsePseudoInstruction(String operation, String[] operands, Processor processor)
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
					throw new IllegalArgumentException("Word immediate must be a value between 0x0000 and 0xfff");
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

    private static void resolveSymbolicLabels(Processor processor) {
        ArrayList<Instruction> instructions = processor.getMemory().getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instr = instructions.get(i);
            Object[] operands = instr.getOperands();
            int currentAddress = i * 2;
            for (int j = 0; j < operands.length; j++) {
                if (operands[j] instanceof String label) {
                    if (!tags.containsKey(label))
                        throw new IllegalArgumentException("Undefined label: " + label);
                    int targetAddress = tags.get(label);
                    int offset = targetAddress - currentAddress - 2;
                    if (offset < -64 || offset > 63)
                        throw new IllegalArgumentException("Branch offset out of range for label: " + label);
                    operands[j] = offset;
                }
            }
        }
    }

	private static int instructionAddress = 0;
	
	private static HashMap<String, Integer> tags = new HashMap<>();
	// The pseudoInstructions to check against as well as how many operands they have (Java object instantiation sucks)
	private static final HashMap<String, Integer> pseudoInstructions = new HashMap<>(Map.of("nop", 0, "halt", 0, "lli", 2, "movi", 2, ".fill", 1, ".space", 1));
}