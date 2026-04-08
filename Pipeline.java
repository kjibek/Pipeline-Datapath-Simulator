import java.util.*;
import java.io.*;

public class Pipeline {
	
	static String[] Main_mem = new String[1024]; // 1k memory
	static String[] Regs = new String[32]; // 32 register
	static int[] instr; // instruction cache
	static int PC; // global variable pc counter
	
	// pipeline register read and write classes. all reads initialized w/ 0
	static class IFID_R {
	    int instruction = 0;
	    int PC = 0;
	}

	static class IFID_W {
	    int instruction;
	    int PC;
	}

	static class IDEX_R {
	    String rsVal = "0";
	    String rtVal = "0";
	    int imm = 0;

	    int rs = 0;
	    int rt = 0;
	    int destReg = 0;

	    int ALUOp = 0;
	    int ALUSrc = 0;
	    int MemRead = 0;
	    int MemWrite = 0;
	    int RegWrite = 0;
	    int MemToReg = 0;
	}

	static class IDEX_W {
	    String rsVal, rtVal;
	    int rs, rt, imm;
	    int destReg, ALUOp, ALUSrc, MemRead, MemWrite, RegWrite, MemToReg;
	}

	static class EXMEM_R {
	    String ALURes = "0";
	    String WriteData = "0";
	    
	    int destReg = 0;
	    int MemRead = 0;
	    int MemWrite = 0;
	    int RegWrite = 0;
	    int MemToReg = 0;
	}

	static class EXMEM_W {
	    String ALURes, WriteData;
	    int destReg, MemRead, MemWrite, RegWrite, MemToReg; 
	}

	static class MEMWB_R {
	    String ALURes = "0";
	    String MemData = "0";
	    
	    int destReg = 0;
	    int RegWrite = 0;
	    int MemToReg = 0;
	}

	static class MEMWB_W {
	    String ALURes, MemData;
	    int destReg, RegWrite, MemToReg;
	}
	
	// create instance of each pipeline register
	static IFID_R IFIDRead  = new IFID_R();
    static IFID_W IFIDWrite  = new IFID_W();
    static IDEX_R  IDEXRead  = new IDEX_R();
    static IDEX_W IDEXWrite  = new IDEX_W();
    static EXMEM_R EXMEMRead = new EXMEM_R();
    static EXMEM_W EXMEMWrite = new EXMEM_W();
    static MEMWB_R MEMWBRead = new MEMWB_R();
    static MEMWB_W MEMWBWrite = new MEMWB_W();
	
	// constructor method
	public Pipeline(ArrayList<Integer> instructions) {		
		// fill reg and main mem arrays w/ hex values. fill instr. array from file input
		Regs[0] = "0";
		for (int i = 1; i < Regs.length; i++) {
			Regs[i] = Integer.toHexString(0x100 + i);
		}
		
		for (int i = 0; i < Main_mem.length; i++) {
			Main_mem[i] = Integer.toHexString(i % 256);
		}
		
		instr = new int[instructions.size()];
		for (int i = 0; i < instr.length; i++) {
			instr[i] = instructions.get(i);
		}
        
		// print initial reg and main mem arrays
		System.out.println("Registers: " + Arrays.toString(Regs));
        System.out.println("Main Memory: " + Arrays.toString(Main_mem));
        
        // pipeline cycle
		for (int i = 0; i < instr.length; i++) {
			IFIDWrite  = new IFID_W();
		    IDEXWrite  = new IDEX_W();
		    EXMEMWrite = new EXMEM_W();
		    MEMWBWrite = new MEMWB_W();
			System.out.println("Cycle: " + i);
			IF_stage();
			ID_stage();
			EX_stage();
			MEM_stage();
			WB_stage();
			print_out_everything();
			copy_write_to_read();
			System.out.println();
		}
		
        System.out.println("Main Memory: " + Arrays.toString(Main_mem)); // print final main mem array
	}
	
	
	public static void IF_stage () {
		if (PC < instr.length) { // if there are still instr
			IFIDWrite.instruction = instr[PC]; // update instr
			IFIDWrite.PC = PC; // update PC
			PC ++;
		}
		
		else { // if no more instr left, all IFID class types = 0
			IFIDWrite.instruction = 0; 
			IFIDWrite.PC = 0;
		}
	}
	
	public static void ID_stage() {
		if (IFIDRead.instruction == 0) { // if nop instr, all IDEX class types = 0
		    IDEXWrite.rsVal = "0";
		    IDEXWrite.rtVal = "0";
		    IDEXWrite.imm = 0;
		    IDEXWrite.rs = 0;
		    IDEXWrite.rt = 0;
		    IDEXWrite.destReg = 0;
		    IDEXWrite.ALUOp = 0;
		    IDEXWrite.ALUSrc = 0;
		    IDEXWrite.MemRead = 0;
		    IDEXWrite.MemWrite = 0;
		    IDEXWrite.RegWrite = 0;
		    IDEXWrite.MemToReg = 0;
		    return;
		}
		
		else {
			int op = (IFIDRead.instruction >>> 26) & 0x3F; // obtain op
			int rs, rt, rd, funct, immediate;
			
			// R type format
			if (op == 0) {
				// obtain rs, rt, rd, funct
				rs = (IFIDRead.instruction >>> 21) & 0x1F;
				rt = (IFIDRead.instruction >>> 16) & 0x1F; 
				rd = (IFIDRead.instruction >>> 11) & 0x1F; 
				funct = IFIDRead.instruction & 0x3F;
				
				// fill all IDEX class types using rs, rt, rd, funct
				IDEXWrite.rsVal = Regs[rs];
				IDEXWrite.rtVal = Regs[rt];
				IDEXWrite.rs = rs;
				IDEXWrite.rt = rt;
				IDEXWrite.destReg = rd;
				IDEXWrite.ALUSrc = 0; // 0 = rt, 1 = imm
				IDEXWrite.RegWrite = 1;
				if (funct == 32) {
					IDEXWrite.ALUOp = 1; // ALUOp 1 = add, 2 = sub, 3 = lb, 4 = sb
				}
				
				else if (funct == 34) {
					IDEXWrite.ALUOp = 2; // ALUOp 1 = add, 2 = sub, 3 = lb, 4 = sb
				}
				
				// fill w/ 0 bc. R type
				IDEXWrite.imm = 0;
				IDEXWrite.MemRead = 0;
				IDEXWrite.MemWrite = 0;
				IDEXWrite.MemToReg = 0;
			}
			
			// I type format
			else {
				// obtain rs, rt, imm
				rs = (IFIDRead.instruction >>> 21) & 0x1F;
				rt = (IFIDRead.instruction >>> 16) & 0x1F;
				immediate = IFIDRead.instruction & 0xFFFF;
				if ((immediate & 0x8000) != 0) immediate = immediate - 0x10000; // sign extending 16 bit imm
				
				// fill all IDEX class types using rs, rt, imm
				IDEXWrite.rsVal = Regs[rs];
				IDEXWrite.rtVal = Regs[rt];
				IDEXWrite.imm = immediate;
				IDEXWrite.rs = rs;
				IDEXWrite.rt = rt;
				IDEXWrite.ALUSrc = 1; // 0 = rt, 1 = imm
				if (op == 32) { // lb
					IDEXWrite.ALUOp = 3; // ALUOp 1 = add, 2 = sub, 3 = lb, 4 = sb
					IDEXWrite.destReg = rt;
					IDEXWrite.MemRead = 1;
					IDEXWrite.MemWrite = 0;
					IDEXWrite.RegWrite = 1;
					IDEXWrite.MemToReg = 1;
				}
				
				else if (op == 40) { // sb
					IDEXWrite.ALUOp = 4; // ALUOp 1 = add, 2 = sub, 3 = lb, 4 = sb
					IDEXWrite.destReg = 0;
					IDEXWrite.MemRead = 0;
					IDEXWrite.MemWrite = 1;
					IDEXWrite.RegWrite = 0;
					IDEXWrite.MemToReg = 0;
				}
			}
		}
	}
	
	public static void EX_stage() {
		// if nop all class types EXMEM = 0
		if (IDEXRead.ALUOp == 0 && IDEXRead.RegWrite == 0 && IDEXRead.MemRead == 0 && IDEXRead.MemWrite == 0) {
			EXMEMWrite.ALURes = "0";
		    EXMEMWrite.WriteData = "0";
		    EXMEMWrite.destReg = 0;
		    EXMEMWrite.MemRead = 0;
		    EXMEMWrite.MemWrite = 0;
		    EXMEMWrite.RegWrite = 0;
		    EXMEMWrite.MemToReg = 0;
		    return;
		    }
		
		// update EXMEM class types = respective class types from IDEX
		EXMEMWrite.destReg = IDEXRead.destReg;
		EXMEMWrite.MemRead = IDEXRead.MemRead;
		EXMEMWrite.MemWrite = IDEXRead.MemWrite;
		EXMEMWrite.RegWrite = IDEXRead.RegWrite;
		EXMEMWrite.MemToReg = IDEXRead.MemToReg;
		
		if (IDEXRead.ALUOp == 1) { // ALUOp 1 = add, 2 = sub, 3 = lb, 4 = sb
			EXMEMWrite.ALURes = Integer.toHexString(Integer.parseInt(IDEXRead.rsVal, 16) + Integer.parseInt(IDEXRead.rtVal, 16));
			EXMEMWrite.WriteData = "0";
		}
		
		else if (IDEXRead.ALUOp == 2) { // ALUOp 1 = add, 2 = sub, 3 = lb, 4 = sb
			EXMEMWrite.ALURes = Integer.toHexString(Integer.parseInt(IDEXRead.rsVal, 16) - Integer.parseInt(IDEXRead.rtVal, 16));
			EXMEMWrite.WriteData = "0";
		}
		
		else if (IDEXRead.ALUOp == 3) { // ALUOp 1 = add, 2 = sub, 3 = lb, 4 = sb
			EXMEMWrite.ALURes = Integer.toHexString(Integer.parseInt(IDEXRead.rsVal, 16) + IDEXRead.imm);
			EXMEMWrite.WriteData = "0";
		}
		
		else if (IDEXRead.ALUOp == 4 ) { // ALUOp 1 = add, 2 = sub, 3 = lb, 4 = sb
			EXMEMWrite.ALURes = Integer.toHexString(Integer.parseInt(IDEXRead.rsVal, 16) + IDEXRead.imm);
			EXMEMWrite.WriteData = IDEXRead.rtVal;
		}
	}
	
	public static void MEM_stage() {
		// if nop, all MEMWB class types = 0
		if (EXMEMRead.RegWrite == 0 && EXMEMRead.MemRead == 0 && EXMEMRead.MemWrite == 0) {
			MEMWBWrite.ALURes = "0";
		    MEMWBWrite.MemData = "0";
		    MEMWBWrite.destReg = 0;
		    MEMWBWrite.RegWrite = 0;
		    MEMWBWrite.MemToReg = 0;
			return;
			}
		
		if (EXMEMRead.MemRead == 1) {
			MEMWBWrite.MemData = Main_mem[Integer.parseInt(EXMEMRead.ALURes, 16)];
		}
		
		else if (EXMEMRead.MemWrite == 1) {
			Main_mem[Integer.parseInt(EXMEMRead.ALURes, 16)] = EXMEMRead.WriteData;
			MEMWBWrite.MemData = "0";
		}
		
		else {
		    MEMWBWrite.MemData = "0";
		}
		
		// update MEMWB class types w/ respective class types EXMEM
		MEMWBWrite.ALURes = EXMEMRead.ALURes;
		MEMWBWrite.destReg = EXMEMRead.destReg;
		MEMWBWrite.RegWrite = EXMEMRead.RegWrite;
		MEMWBWrite.MemToReg = EXMEMRead.MemToReg;
	}
	
	public static void WB_stage() {
		if (MEMWBRead.RegWrite == 1) {
			if (MEMWBRead.MemToReg == 0) {
				Regs[MEMWBRead.destReg] = MEMWBRead.ALURes;
			}
			
			else if (MEMWBRead.MemToReg == 1){
				Regs[MEMWBRead.destReg] = MEMWBRead.MemData;
			}
		}
	}
	
	public static void print_out_everything() {
	    System.out.println(
	        "Registers: " + Arrays.toString(Regs)

	        + "\nIFID READ: "
	        + "inst = " + String.format("%08X", IFIDRead.instruction)
	        + " PC = " + IFIDRead.PC

	        + "\n\nIFID WRITE: "
	        + "inst = " + String.format("%08X", IFIDWrite.instruction)
	        + " PC = " + IFIDWrite.PC

	        + "\n\nIDEX READ: "
	        + "rsVal = " + IDEXRead.rsVal
	        + " rtVal = " + IDEXRead.rtVal
	        + " imm = " + IDEXRead.imm
	        + " rs = " + IDEXRead.rs
	        + " rt = " + IDEXRead.rt
	        + " destReg = " + IDEXRead.destReg
	        + " ALUOp = " + IDEXRead.ALUOp
	        + " ALUSrc = " + IDEXRead.ALUSrc
	        + " MemRead = " + IDEXRead.MemRead
	        + " MemWrite = " + IDEXRead.MemWrite
	        + " RegWrite = " + IDEXRead.RegWrite
	        + " MemToReg = " + IDEXRead.MemToReg

	        + "\n\nIDEX WRITE: "
	        + "rsVal = " + IDEXWrite.rsVal
	        + " rtVal = " + IDEXWrite.rtVal
	        + " imm = " + IDEXWrite.imm
	        + " rs = " + IDEXWrite.rs
	        + " rt = " + IDEXWrite.rt
	        + " destReg = " + IDEXWrite.destReg
	        + " ALUOp = " + IDEXWrite.ALUOp
	        + " ALUSrc = " + IDEXWrite.ALUSrc
	        + " MemRead = " + IDEXWrite.MemRead
	        + " MemWrite = " + IDEXWrite.MemWrite
	        + " RegWrite = " + IDEXWrite.RegWrite
	        + " MemToReg = " + IDEXWrite.MemToReg

	        + "\n\nEXMEM READ: "
	        + "ALURes = " + EXMEMRead.ALURes
	        + " WriteData = " + EXMEMRead.WriteData
	        + " destReg = " + EXMEMRead.destReg
	        + " MemRead = " + EXMEMRead.MemRead
	        + " MemWrite = " + EXMEMRead.MemWrite
	        + " RegWrite = " + EXMEMRead.RegWrite
	        + " MemToReg = " + EXMEMRead.MemToReg

	        + "\n\nEXMEM WRITE: "
	        + "ALURes = " + EXMEMWrite.ALURes
	        + " WriteData = " + EXMEMWrite.WriteData
	        + " destReg = " + EXMEMWrite.destReg
	        + " MemRead = " + EXMEMWrite.MemRead
	        + " MemWrite = " + EXMEMWrite.MemWrite
	        + " RegWrite = " + EXMEMWrite.RegWrite
	        + " MemToReg = " + EXMEMWrite.MemToReg

	        + "\n\nMEMWB READ: "
	        + "ALURes = " + MEMWBRead.ALURes
	        + " MemData = " + MEMWBRead.MemData
	        + " destReg = " + MEMWBRead.destReg
	        + " RegWrite = " + MEMWBRead.RegWrite
	        + " MemToReg = " + MEMWBRead.MemToReg

	        + "\n\nMEMWB WRITE: "
	        + "ALURes = " + MEMWBWrite.ALURes
	        + " MemData = " + MEMWBWrite.MemData
	        + " destReg = " + MEMWBWrite.destReg
	        + " RegWrite = " + MEMWBWrite.RegWrite
	        + " MemToReg = " + MEMWBWrite.MemToReg
	    );
	}

	
	public static void copy_write_to_read() {
		// IFID
	    IFIDRead.instruction = IFIDWrite.instruction;
	    IFIDRead.PC          = IFIDWrite.PC;

	    // IDEX
	    IDEXRead.rsVal      = IDEXWrite.rsVal;
	    IDEXRead.rtVal      = IDEXWrite.rtVal;
	    IDEXRead.imm        = IDEXWrite.imm;

	    IDEXRead.rs         = IDEXWrite.rs;
	    IDEXRead.rt         = IDEXWrite.rt;
	    IDEXRead.destReg    = IDEXWrite.destReg;

	    IDEXRead.ALUOp      = IDEXWrite.ALUOp;
	    IDEXRead.ALUSrc     = IDEXWrite.ALUSrc;
	    IDEXRead.MemRead    = IDEXWrite.MemRead;
	    IDEXRead.MemWrite   = IDEXWrite.MemWrite;
	    IDEXRead.RegWrite   = IDEXWrite.RegWrite;
	    IDEXRead.MemToReg   = IDEXWrite.MemToReg;

	    // EXMEM
	    EXMEMRead.ALURes    = EXMEMWrite.ALURes;
	    EXMEMRead.WriteData = EXMEMWrite.WriteData;
	    EXMEMRead.destReg   = EXMEMWrite.destReg;

	    EXMEMRead.MemRead   = EXMEMWrite.MemRead;
	    EXMEMRead.MemWrite  = EXMEMWrite.MemWrite;
	    EXMEMRead.RegWrite  = EXMEMWrite.RegWrite;
	    EXMEMRead.MemToReg  = EXMEMWrite.MemToReg;

	    // MEMWB
	    MEMWBRead.ALURes    = MEMWBWrite.ALURes;
	    MEMWBRead.MemData   = MEMWBWrite.MemData;
	    MEMWBRead.destReg   = MEMWBWrite.destReg;

	    MEMWBRead.RegWrite  = MEMWBWrite.RegWrite;
	    MEMWBRead.MemToReg  = MEMWBWrite.MemToReg;
        }

	
	public static void main(String[] args) {
		ArrayList<Integer> instructions = new ArrayList<>();
		try {
			File file = new File("input.txt");
			Scanner scnr = new Scanner(file);
			
			while (scnr.hasNextLine()) { // copy input file to arraylist
				String stringHex = scnr.nextLine().trim().substring(2);
				instructions.add(Integer.parseUnsignedInt(stringHex, 16));
			}
			
			scnr.close();
		} 
		
		catch (IOException e) {
			System.out.println("IO Error!");
		}
		
		Pipeline datapath = new Pipeline(instructions); // pass arraylist to constructor method
	}

}
