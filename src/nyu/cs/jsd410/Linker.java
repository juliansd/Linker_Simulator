package nyu.cs.jsd410;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

/**
 * This is an implementation of a very basic linker.
 * @author juliansmithdeniro
 * @version 1.0
 */

public class Linker {
	
	private ArrayList<String> input;
	private ArrayList<Module> modules;
	private HashMap<String, Integer> symbolTable;
	
	/**
	 * Generic constructor.
	 */
	public Linker() {}
	
	/**
	 * Generate the symbol table used by the linker object.
	 */
	protected void generateSymbolTable() {
		
		ArrayList<String> input = this.getInput();
		ArrayList<Module> modules = new ArrayList<Module>();
		HashMap<String, Integer> symbolTable = new HashMap<String, Integer>();
		
		HashMap<String, Integer> defs;
		HashMap<String, Integer> uses;
		
		Module m = new Module();
		m.setRelativeAddress(0);
		int currentAddress = 0;
		for (int i = 0; i < input.size(); i++) {
			
			String[] line = input.get(i).split(" ");

			if (i % 3 == 0) {
				// definitions
				m = new Module();
				m.setRelativeAddress(currentAddress);
				defs = new HashMap<String, Integer>();
				if (line.length != 1) {
					for (int j = 0; j < line.length; j+=2) {
						if (symbolTable.containsKey(line[j]))
							System.err.println("Symbol " + line[j] + " already defined. Using last definition.");
						defs.put(line[j], Integer.parseInt(line[j+1]) + currentAddress);
						symbolTable.put(line[j], Integer.parseInt(line[j+1]) + currentAddress);
					}
				}
				m.setDefs(defs);
				this.setSymbolTable(symbolTable);
			} else if (i % 3 == 1) {
				// uses
				uses = new HashMap<String, Integer>();
				if (line.length != 1) {
					for (int j = 0; j < line.length; j += 2) {
						uses.put(line[j], Integer.parseInt(line[j + 1]));
					} 
				}
				m.setUses(uses);
			} else {
				// text entries
				ArrayList<String> text = new ArrayList<String>();
				for (int j = 0; j < line.length; j+=2) {
					String address = line[j] + line[j+1];
					text.add(address);
				}
				m.setText(text);
				currentAddress += m.getText().size();
				modules.add(m);
			}
			this.setModules(modules);
		}
	}

	/**
	 * Get ArrayList which holds input from user.
	 * @return An ArrayList which holds input from user.
	 */
	public ArrayList<String> getInput() {
		return input;
	}

	/**
	 * Set ArrayList which holds input from user.
	 * @param input - An ArrayList containing strings of the user input.
	 */
	public void setInput(ArrayList<String> input) {
		this.input = input;
	}
	
	/**
	 * A class used to represent each module in the input.
	 * @author juliansmithdeniro
	 * @version 1.0
	 */
	class Module {
		
		private HashMap<String, Integer> defs;
		private HashMap<String, Integer> uses;
		private ArrayList<String> text;
		private int relativeAddress;
		
		/**
		 * Generic constructor.
		 */
		public Module() {}

		/**
		 * Get definitions in this module.
		 * @return A HashMap which holds key-value pairs for the module and its definitions.
		 */
		public HashMap<String, Integer> getDefs() {
			return defs;
		}

		/**
		 * Set definitions in this module.
		 * @param defs - A HashMap which holds key-value pairs for the module and its definitions.
		 */
		public void setDefs(HashMap<String, Integer> defs) {
			this.defs = defs;
		}

		/**
		 * Get address indices of where symbols are used in this module.
		 * @return A HashMap holding key-value pairs for the symbol uses in this module.
		 */
		public HashMap<String, Integer> getUses() {
			return uses;
		}

		/**
		 * Set address indices of where symbols are used in this module.
		 * @param uses - A HashMap holding key-value pairs for the symbol uses in this module.
		 */
		public void setUses(HashMap<String, Integer> uses) {
			this.uses = uses;
		}

		/**
		 * Get text in this module.
		 * @return An ArrayList holding addresses in this module.
		 */
		public ArrayList<String> getText() {
			return text;
		}

		/**
		 * Set text in this module.
		 * @param text - An ArrayList holding addresses in this module.
		 */
		public void setText(ArrayList<String> text) {
			this.text = text;
		}

		/**
		 * Get relative address of this module.
		 * @return An integer which represents the relative address of this module.
		 */
		public int getRelativeAddress() {
			return relativeAddress;
		}

		/**
		 * Set relative address of this module.
		 * @param relativeAddress - An integer which represents the relative address of this module.
		 */
		public void setRelativeAddress(int relativeAddress) {
			this.relativeAddress = relativeAddress;
		}
	}
	
	/**
	 * The main function which does the main processing 
	 * and calculating for the modified addresses we want to output.
	 * @param args - command line arguments.
	 */
	public static void main(String[] args) {
		Linker linker = new Linker();
		Scanner scan = new Scanner(System.in);
		ArrayList<String> inputs = new ArrayList<String>();
		
		ArrayList<Integer> output = new ArrayList<Integer>();
		
		System.out.println("Please enter input: ");
		int numOfMods = Integer.parseInt(scan.next());
		
		int[][] output2 = new int[numOfMods][];
		
		while (scan.hasNext()) {
			String input = "";
			String current = scan.next();
			
			int numOfDefs = Integer.parseInt(current);
			int count2 = 0;
			while (count2 < 2*numOfDefs) {
				input += scan.next() + " ";
				count2++;
			}
			inputs.add(input);
			if (inputs.size() == 3*numOfMods)
				break;
		}
		scan.close();
		linker.setInput(inputs);
		linker.generateSymbolTable();
		ArrayList<Module> modules = linker.getModules();
		
		// check for use of undefined symbol and for unused symbols
		int count = 0;
		for (int i = 0; i < modules.size(); i++) {
			Module currentMod = modules.get(i);
			
			Iterator<String> itr = currentMod.getUses().keySet().iterator();
			Iterator<String> itr2 = currentMod.getUses().keySet().iterator();
			while (itr.hasNext()) {
				String currentKey = itr.next();
				if (!linker.getSymbolTable().containsKey(currentKey)) {
					System.err.println(
							"ERROR: the symbol '" + currentKey +
							"' is used but not defined. It has been given the value 111.");
					linker.getSymbolTable().put(currentKey, 111);
				}
				if (!currentMod.getUses().containsKey(currentKey)) {
					count++;
					if (count == numOfMods)
						System.err.println(
								"WARNING: The symbol " + "'" + currentKey + 
								"' is defined but never used.");
				}
				while (itr2.hasNext()) {
					String otherCurrent = itr2.next();
					if (!currentKey.equals(otherCurrent) && 
							currentMod.getUses().get(currentKey) == 
							currentMod.getUses().get(otherCurrent)) {
						currentMod.getUses().remove(otherCurrent);
						itr = currentMod.getUses().keySet().iterator();
						System.err.println(
								"ERROR: Multiple symbols were attempted to be "
								+ "used at the same address. Only using '" + 
										currentKey + "'.");
					}
				}
			}
		}
		int numOfMaxAddresses = 0;
		for (int i = 0; i < modules.size(); i++) {
			Module m = modules.get(i);
			ArrayList<String> text = m.getText();
			int[] outputM = new int[text.size()];
			for (int j = 0; j < text.size(); j++) {
				int raw = Integer.parseInt(text.get(j).substring(1));
				int real;
				if (text.get(j).charAt(0) == 'R') {
					if (Integer.parseInt(text.get(j).substring(2)) >= text.size()) {
						System.err.println(
								"ERROR: Invalid addressing. Zeroing relative address " + 
										text.get(j).substring(1));
						raw = Integer.parseInt(text.get(j).substring(1, 2)) * 1000;
					}
					real = raw + m.getRelativeAddress();
					output.add(real);
					outputM[j] = real;
				} else if (text.get(j).charAt(0) == 'E') {
					if (Integer.parseInt(text.get(j).substring(2)) > 777) {
						String newRaw = "";
						int newAddr = 777 - numOfMaxAddresses;
						newRaw = text.get(j).substring(0, 2) + Integer.toString(newAddr);
						numOfMaxAddresses++;
						text.set(j, newRaw);
						System.err.println("ERROR: Invalid addressing. Using largest legal value: " + newRaw.substring(1));
					}
					if (!(m.getUses().size() == 0)) {
						HashMap<String, Integer> uses = m.getUses();
						HashMap<String, Integer> symbolTable = linker.getSymbolTable();
						Iterator<String> itr = uses.keySet().iterator();
						while (itr.hasNext()) {
							String current = itr.next();
							if (uses.get(current) == j) {
								if (text.get(j).substring(2).equals("777")) {
									real = (Integer.parseInt(text.get(j).substring(1,2)) * 1000)
											+ symbolTable.get(current);
									output.add(real);
									outputM[j] = real;
								} else {
									real = (Integer.parseInt(text.get(j).substring(1,2)) * 1000)
											+ symbolTable.get(current);
									output.add(real);
									outputM[j] = real;
									int nextAddressIndex = Integer.parseInt(text.get(j).substring(4));
									String currentAddress = text.get(nextAddressIndex).substring(2);
									do {
										real = (Integer.parseInt(text.get(nextAddressIndex).substring(1,2)) * 1000)
												+ symbolTable.get(current);
										output.add(real);
										outputM[nextAddressIndex] = real;
										nextAddressIndex = Integer.parseInt(text.get(nextAddressIndex).substring(4));
										if (nextAddressIndex < text.size() && !currentAddress.equals("777"))
											currentAddress = text.get(nextAddressIndex).substring(2);
										else {
											currentAddress = "777";
										}
									} while (!currentAddress.equals("777"));
									if (nextAddressIndex < text.size()) {
										real = (Integer.parseInt(text.get(nextAddressIndex).substring(1, 2)) * 1000)
												+ symbolTable.get(current);
										outputM[nextAddressIndex] = real;
									}
								}
							}
						}
					} else {
						outputM[j] = Integer.parseInt(text.get(j).substring(1));
					}
				} else if (text.get(j).charAt(0) == 'A') {
					if (Integer.parseInt(text.get(j).substring(2)) > ((text.size()+m.getRelativeAddress())*100)-1) {
						String newRaw = "";
						int newAddr = ((text.size()*100)-1) - numOfMaxAddresses;
						newRaw = text.get(j).substring(0, 2) + Integer.toString(newAddr);
						numOfMaxAddresses++;
						text.set(j, newRaw);
						raw = Integer.parseInt(newRaw.substring(1));
						System.err.println("ERROR: Invalid addressing. Using largest legal value: " + newRaw.substring(1));
					}
					real = raw;
					output.add(real);
					outputM[j] = real;
				} else {
					real = raw;
					output.add(real);
					outputM[j] = real;
				}
			}
			output2[i] = outputM;
		}
		
		Iterator<String> itr = linker.getSymbolTable().keySet().iterator();
		System.out.println("Symbol Table:");
		while (itr.hasNext()) {
			String current = itr.next();
			if (linker.getSymbolTable().get(current) != 111)
				System.out.println(current + "=" + linker.getSymbolTable().get(current));
		}
		System.out.println("");
		System.out.println("Memory Map:");
		int c = 0;
		for (int i = 0; i < output2.length; i++) {
			for (int j = 0; j < output2[i].length; j++) {
				System.out.println(c + ": " + output2[i][j]);
				c++;
			}
		}
		System.out.println("");
		Iterator<String> symbolItr = linker.getSymbolTable().keySet().iterator();
		while (symbolItr.hasNext()) {
			boolean notUsed = true;
			String currentSymbol = symbolItr.next();
			for (int j = 0; j < linker.getModules().size(); j++) {
				Module currentMod = linker.getModules().get(j);
				if (currentMod.getUses().containsKey(currentSymbol)) {
					notUsed = false;
					break;
				}
			}
			if (notUsed)
				System.err.println("WARNING: The symbol " + currentSymbol + " is never used.");
		}
	}

	/**
	 * Get modules in this linker.
	 * @return An ArrayList which holds this linker's modules and each of their respective information.
	 */
	public ArrayList<Module> getModules() {
		return modules;
	}

	/**
	 * Set modules in this linker.
	 * @param modules - An ArrayList which holds this linker's modules and each of their respective information.
	 */
	public void setModules(ArrayList<Module> modules) {
		this.modules = modules;
	}
	
	/**
	 * Get symbol table for this linker.
	 * @return A HashMap holding the key-value pairs for the symbol table in this linker.
	 */
	public HashMap<String, Integer> getSymbolTable() {
		return symbolTable;
	}

	/**
	 * Set symbol table for this linker.
	 * @param symbolTable - A HashMap holding the key-value pairs for the symbol table in this linker.
	 */
	public void setSymbolTable(HashMap<String, Integer> symbolTable) {
		this.symbolTable = symbolTable;
	}
}
