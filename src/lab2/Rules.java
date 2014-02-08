package lab2;

import java.util.*;

public class Rules {
	private ArrayList<Rule> rules = new ArrayList<Rule>();

	public Rules() {}

	public Rules(Object rules) {
		String source;
		String dest;
		String kind;
		String action;
		Integer seqNum;
		Boolean duplicate;
		Object value;
		
		// Prepare ruleList object, which is read from YAML
		ArrayList<Object> ruleList = (ArrayList<Object>) rules;
		
		// Parse each rule in the ruleList object
		for (Object rule : ruleList) {
			source = null;
			dest = null;
			kind = null;
			action = null;
			seqNum = null;
			duplicate = null;
			Map<String, Object> ruleMap = (Map<String, Object>) rule;
			for (Map.Entry<String, Object> entry : ruleMap.entrySet()) {
				value = entry.getValue();
				String key = entry.getKey();
				if ("src".equals(key)) {
					source = (String) value;
				} else if ("dest".equals(key)) {
					dest = (String) value;
				} else if ("kind".equals(key)) {
					kind = (String) value;
				} else if ("seqNum".equals(key)) {
					seqNum = (Integer) value;
				} else if ("duplicate".equals(key)) {
					duplicate = (Boolean) value;
				} else if ("action".equals(key)) {
					action = (String) value;
				}
			}
			this.addRule(source, dest, kind, seqNum, duplicate, action);
		}
	}

	/**
	 * Remove all rules in the rules list.
	 */
	public void clear() {
		this.rules.clear();
	}

	/**
	 * Add one rule to the rules list.
	 * @param source
	 * @param dest
	 * @param kind
	 * @param seqNum
	 * @param duplicate
	 * @param action
	 */
	public void addRule(String source, String dest, String kind,
			Integer seqNum, Boolean duplicate, String action) {
		this.rules.add(new Rule(source, dest, kind, seqNum, duplicate, action));
	}

	/**
	 * If there would be a rule that matches this message, 
	 * this will return the action of that matched rule.
	 * @param message A message to check for a matched rule.
	 * @return Action of the matched rule.
	 */
	public Rule.Action getMatchedAction(Message message) {
		for (Rule rule : this.rules) {
			if (rule.isMatched(message)) {
				return rule.getAction();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		String toString = "[\n";
		for (Rule rule : this.rules) {
			toString += rule.toString() + "\n";
		}
		toString += "]";
		return toString;
	}
}
