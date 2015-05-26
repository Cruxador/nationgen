package nationGen.units;


import java.util.ArrayList;
import java.util.List;

import com.elmokki.Generic;

import nationGen.NationGen;
import nationGen.entities.Entity;
import nationGen.entities.Filter;
import nationGen.misc.Command;



public class ShapeShift extends Filter {
	
	
	public ShapeShift(NationGen nationGen) {
		super(nationGen);
	}

	public List<Command> commands = new ArrayList<Command>();
	//public List<Command> commands = new ArrayList<Command>();
	boolean nofeedback = false;
	boolean keepname = false;
	boolean nogcost = false;
	
	
	
	public void handleOwnCommand(String line) {
	
		

		
		List<String> args = Generic.parseArgs(line);
		if(args.get(0).equals("#keepname") && args.size() > 1)
			this.keepname = true;
		// Overrides filter implementation
		else if(args.get(0).equals("#command") && args.size() > 1) 
		{
			this.commands.add(Command.parseCommand(args.get(1)));
		}
		else
			super.handleOwnCommand(line);

	}
	

}
