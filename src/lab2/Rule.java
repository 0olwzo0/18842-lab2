package lab2;

public class Rule {
	public static enum Action {
		drop, duplicate, delay
	};

	private String source;
	private String dest;
	private String kind;
	private Integer seqNum;
	private Boolean duplicate;
	private Action action;

	public Rule(String source, String dest, String kind, Integer seqNum,
			Boolean duplicate, String action) {
		this.source = source;
		this.dest = dest;
		this.kind = kind;
		this.seqNum = seqNum;
		this.duplicate = duplicate;
		this.action = Action.valueOf(action);
	}

	public boolean isMatched(Message message) {
		if (this.source != null && !this.source.equals(message.getSource())) {
			return false;
		}
		if (this.dest != null && !this.dest.equals(message.getDest())) {
			return false;
		}
		if (this.kind != null && !this.kind.equals(message.getKind())) {
			return false;
		}
		if (this.seqNum != null
				&& this.seqNum.intValue() != message.getSeqNum()) {
			return false;
		}
		if (this.duplicate != null
				&& this.duplicate.booleanValue() != message.getDuplicate()) {
			return false;
		}
		return true;
	}

	public Action getAction() {
		return this.action;
	}

	@Override
	public String toString() {
		return "action\t:" + this.action + "\n" + "src\t:" + this.source + "\n"
				+ "dst\t:" + this.dest + "\n" + "kind\t:" + this.kind + "\n"
				+ "seqNum\t:" + this.seqNum + "\n" + "duplicate\t:"
				+ this.duplicate + "\n";
	}
}
