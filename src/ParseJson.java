
import java.io.FileReader;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import com.google.gson.Gson;
import java.io.FileWriter;
import java.util.regex.*;
import java.io.*;

public class ParseJson {
	ArrayList<Block> blocksArray;
	FileWriter output = null;
	String jsonFile;

	public ParseJson(String json) {
		jsonFile = json;
	}

	public void parse() {
		blocksArray = new ArrayList<Block>();
		Gson gson = new Gson();
		Scratch result = new Scratch();
		try {
			// serialize JSON using nested data types inside Scratch.class
			result = gson.fromJson(new FileReader(jsonFile), Scratch.class);
		} catch (FileNotFoundException e) {
			System.out.println("FILE NOT FOUND");
		}
		try {
			output = new FileWriter("result.txt");
		} catch (Exception e) {

		}
		// get the blocks for all targets
		for (int i = 0; i < result.targets.size(); i++) {
			int blockindex = 0;
			// generate global array of blocks from the blocks parsed map
			for (Map.Entry<String, Block> entry : result.targets.get(i).blocks.entrySet()) {
				Block b = entry.getValue();
				b.key = entry.getKey();
				b.index = blockindex;
				blocksArray.add(b);
				blockindex++;
			}

		}
		System.out.println("=====START=====");
		try {
			output.write("=====START===== \n");
		} catch (Exception e) {

		}
		for (Block b : blocksArray) {
			// get top level block
			if (b.topLevel && b.parent==null) {
				// start generating next blocks recursively
				generateStory(b, false);
				break;
			}

		}
		System.out.println("=====END=====");
		try {
			output.write("=====END===== \n");
		} catch (Exception e) {

		}
		try {
			output.close();
		} catch (Exception e) {

		}
	}

	public boolean shouldIndent(Block block) {
		boolean indent = false;
		String key = block.key;
		// checks if input block exists as a SUBSTACK or SUBSTACK2 of any other block
		for (Block search : blocksArray) {
			if (search.getInputs().size() > 0) {
				if ((search.getInputs().get(0).key.equals("SUBSTACK") && search.getInputs().get(0).value.equals(key))) {
					indent = true;
				}
			}

			if (search.getInputs().size() > 1) {
				if ((search.getInputs().get(1).key.equals("SUBSTACK2")
						&& search.getInputs().get(1).value.equals(key))) {
					indent = true;
				}
			}
		}
		return indent;
	}

	public String generateStory(Block current, boolean indent) {
		if (blockToString(current) != "") {
			// if the block should be indented
			if (shouldIndent(current)) {
				System.out.println("   " + blockToString(current));

				try {
					output.write("   " + blockToString(current) + "\n");
				} catch (Exception e) {
				}
			} else if (current.topLevel == false) {
				System.out.println(" " + blockToString(current));

				try {
					output.write(" " + blockToString(current) + "\n");
				} catch (Exception e) {
				}
			} else {
				System.out.println("" + blockToString(current));

				try {
					output.write("" + blockToString(current) + "\n");
				} catch (Exception e) {
				}
			}
			
		}

		Block next = null;
		// if the block has no next block then get the next block index
		if (current.next == null) {
			if (current.index < blocksArray.size() - 1)
				next = blocksArray.get(current.index + 1);
		} else {
			// else get the next block
			next = getBlock(current.next);
		}
		// base case
		if (next == null) {
			return "";
		}
		return generateStory(next, indent);

	}

	public static void main(String[] args) throws Exception {
		ParseJson PJ = new ParseJson("project.json");
		PJ.parse();

	}

	public String formatEquality(String eq) {
		String equality = "";
		switch (eq) {
		case "operator_lt":
			equality = " less than: ";
			break;
		case "operator_gt":
			equality = " greater than: ";
			break;
		case "operator_equals":
			equality = " equals: ";
			break;
		}
		return equality;
	}

	public String formatConditions(ArrayList<InputString> inputs) {
		// format all condition inputs
		String result = "";
		int index = 0;
		for (InputString in : inputs) {
			if (index != inputs.size() - 1)
				result += getBlock(in.value).getInputs().get(0).value + formatEquality(getBlock(in.value).opcode)
						+ getBlock(in.value).getInputs().get(1).value + " , ";
			else
				result += getBlock(in.value).getInputs().get(0).value + formatEquality(getBlock(in.value).opcode)
						+ getBlock(in.value).getInputs().get(1).value + "";
			index++;
		}

		return result;
	}

	public String formatNested(ArrayList<InputString> inputs) {
		// format all nested blocks
		String result = "";
		int index = 0;
		for (InputString in : inputs) {
			if (index != inputs.size() - 1)
				result += "   " + blockToString(getBlock(in.value)) + "\n";
			else
				result += "   " + blockToString(getBlock(in.value)) + "";
			index++;
		}
		return result;
	}

	public String blockToString(Block blockInput) {
		String block = "";

		switch (blockInput.opcode) {
		case "event_whenflagclicked":
			block = "When green flag is clicked:";
			break;
		case "event_whenkeypressed":
			block = "When key is pressed: " + blockInput.fields.entrySet().iterator().next().getValue().get(0);
			break;
		case "looks_thinkforsecs":
			block = "Think: " + blockInput.inputsArray.get(0).key + ": " + blockInput.inputsArray.get(0).value
					+ " for: " + blockInput.getInputs().get(1).value + " " + blockInput.getInputs().get(1).key;
			break;
		case "looks_say":
			block = "Say: " + blockInput.inputsArray.get(0).key + ": " + blockInput.inputsArray.get(0).value;
			break;
		case "looks_think":
			block = "Think: " + blockInput.inputsArray.get(0).key + ": " + blockInput.inputsArray.get(0).value;
			break;
		case "control_wait":
			block = "Wait for: " + blockInput.getInputs().get(0).key + ": " + blockInput.inputsArray.get(0).value;
			break;
		case "looks_sayforsecs":
			block = "Say: " + blockInput.inputsArray.get(0).key + ": " + blockInput.inputsArray.get(0).value + " for: "
					+ blockInput.getInputs().get(1).value + " " + blockInput.getInputs().get(1).key;
			break;
		case "control_repeat":
			ArrayList<InputString> conditions = new ArrayList<InputString>();
			for (InputString b : blockInput.getInputs()) {
				if (b.key.equals("SUBSTACK")) {
					conditions.add(b);
				}
			}
			block = "Repeat: " + blockInput.getInputs().get(1).value + " " + blockInput.getInputs().get(1).key + "\n"
					+ formatNested(conditions);
			break;
		case "motion_turnright":
			block = "Turn Right: " + blockInput.getInputs().get(0).value + " " + blockInput.getInputs().get(0).key;
			break;
		case "motion_changexby":
			block = "Change X by: " + blockInput.getInputs().get(0).value + " " + blockInput.getInputs().get(0).key;
			break;
		case "motion_changeyby":
			block = "Change Y by: " + blockInput.getInputs().get(0).value + " " + blockInput.getInputs().get(0).key;
			break;
		case "motion_gotoxy":
			if (shouldIndent(blockInput))
				block = "Go to X: " + blockInput.getInputs().get(0).value + " " + blockInput.getInputs().get(0).key
						+ "\n   Go to Y: " + blockInput.getInputs().get(1).value + " "
						+ blockInput.getInputs().get(1).key;
			else
				block = "Go to X: " + blockInput.getInputs().get(0).value + " " + blockInput.getInputs().get(0).key
						+ "\nGo to Y: " + blockInput.getInputs().get(1).value + " " + blockInput.getInputs().get(1).key;
			break;
		case "motion_turnleft":
			block = "Turn Left: " + blockInput.getInputs().get(0).value + " " + blockInput.getInputs().get(0).key;
			break;
		case "motion_setx":
			block = "Set: " + blockInput.getInputs().get(0).key + " to " + blockInput.getInputs().get(0).value;
			break;
		case "motion_sety":
			block = "Set: " + blockInput.getInputs().get(0).key + " to " + blockInput.getInputs().get(0).value;
			break;
		case "control_if":
			// get list of conditions and format them.
			conditions = new ArrayList<InputString>();
			for (InputString b : blockInput.getInputs()) {
				if (b.key.equals("CONDITION")) {
					conditions.add(b);
				}
			}
			ArrayList<InputString> thenStack = new ArrayList<InputString>();
			for (InputString b : blockInput.getInputs()) {
				if (b.key.equals("SUBSTACK")) {
					thenStack.add(b);
				}
			}
			block = "If: " + formatConditions(conditions) + "\n then: \n" + formatNested(thenStack);
			break;
		case "control_if_else":
			// if conditions
			conditions = new ArrayList<InputString>();
			for (InputString b : blockInput.getInputs()) {
				if (b.key.equals("CONDITION")) {
					conditions.add(b);
				}
			}
			// then inputs
			thenStack = new ArrayList<InputString>();
			for (InputString b : blockInput.getInputs()) {
				if (b.key.equals("SUBSTACK")) {
					thenStack.add(b);
				}
			}
			// else inputs
			ArrayList<InputString> elseStack = new ArrayList<InputString>();
			for (InputString b : blockInput.getInputs()) {
				if (b.key.equals("SUBSTACK2")) {
					elseStack.add(b);
				}
			}
			block = "If: " + formatConditions(conditions) + "\n then: \n" + formatNested(thenStack) + " \n else:\n"
					+ formatNested(elseStack);
			break;
		case "control_forever":
			block = "Forever: ";
			break;
		case "motion_movesteps":
			block = "Move: " + blockInput.getInputs().get(0).value + " " + blockInput.getInputs().get(0).key;
			break;
		case "control_wait_until":
			// get list of conditions and format them.
			conditions = new ArrayList<InputString>();
			for (InputString b : blockInput.getInputs()) {
				if (b.key.equals("CONDITION")) {
					conditions.add(b);
				}
			}
			block = "Wait Until: " + formatConditions(conditions);
			break;
		case "control_repeat_until":
			conditions = new ArrayList<InputString>();
			for (InputString b : blockInput.getInputs()) {
				if (b.key.equals("CONDITION")) {
					conditions.add(b);
				}
			}
			block = "Repeat Until: " + formatConditions(conditions);
			break;
		// fallback if not match any opcode try to stringify generically
		default:
			if (!blockInput.opcode.equals("operator_equals") && !blockInput.opcode.equals("operator_gt")
					&& !blockInput.opcode.equals("operator_lt")) {
				block = blockInput.getOpcode().replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])",
						"(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ").replace("_", "") + " ";
				String fallback = "";
				for (InputString s : blockInput.getInputs()) {
					if (!s.key.equals("SUBSTACK"))
						fallback += s.key + ": " + s.value + " ";
				}
				block += fallback;
			}
			break;

		}

		return block;
	}

	// gets a block from the global array of blocks by key.
	public Block getBlock(String key) {
		for (Block b : blocksArray) {
			if (b.key.contains(key))
				return b;
		}
		return null;
	}

	class Scratch {
		public List<Target> targets;
	}

	class Target {
		public Map<String, Block> blocks;
		public Map<String, List<String>> variables;

	}

	class FieldString {
		String key;
		String value;

		public FieldString(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return "[key=[" + key + "] value=[" + value + "]";
		}
	}

	class InputString {
		String key;
		String value;

		public InputString(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return "[key=[" + key + "] value=[" + value + "]";
		}
	}

	class Block {
		String key;
		String opcode;
		String next;
		String parent;
		Boolean shadow;
		Boolean topLevel;
		Integer x;
		Integer index;
		Integer y;
		JSONObject inputs;
		ArrayList<InputString> inputsArray;
		Map<String, List<String>> fields;

		@Override
		public String toString() {
			return "Block Data: [key=" + key + "\n opcode=" + opcode + "\n next=" + next + "\n parent=" + parent
					+ "\n shadow=" + shadow + "\n topLevel=" + topLevel + "\n x=" + x + "\n y=" + y + "\n inputs="
					+ getInputs() + "\n fields=" + fields + "]\n";
		}

		public String getOpcode() {
			return opcode;
		}

		public ArrayList<InputString> getInputs() {

			inputsArray = new ArrayList<InputString>();
			List<String> allMatches = new ArrayList<String>();

			for (String split : inputs.toJSONString().split("],")) {
				Matcher m = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1").matcher(split);
				while (m.find()) {
					allMatches.add(m.group().replace("\"", "").replace("\\", ""));
				}

			}
			for (int i = 0; i < allMatches.size(); i += 2) {
				inputsArray.add(new InputString(allMatches.get(i), allMatches.get(i + 1)));
			}

			return inputsArray;

		}

		public void setOpcode(String opcode) {
			this.opcode = opcode;
		}

		public String getNext() {
			return next;
		}

		public void setNext(String next) {
			this.next = next;
		}

		public String getParent() {
			return parent;
		}

		public void setParent(String parent) {
			this.parent = parent;
		}

		public Boolean getShadow() {
			return shadow;
		}

		public void setShadow(Boolean shadow) {
			this.shadow = shadow;
		}

		public Boolean getTopLevel() {
			return topLevel;
		}

		public void setTopLevel(Boolean topLevel) {
			this.topLevel = topLevel;
		}

		public Integer getX() {
			return x;
		}

		public void setX(Integer x) {
			this.x = x;
		}

		public Integer getY() {
			return y;
		}

		public void setY(Integer y) {
			this.y = y;
		}

	}

	class input {

	}
}