package BrazilCenter.UploadClient.filter;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BrazilCenter.UploadClient.Utils.Utils;


/**
 * @author MA FULI
 *
 */
public class FileNameFilter {
	
	/** check if the file name match the rules.*/
	private  boolean checkMatcher(String rule, String str) {
		Pattern pattern = Pattern.compile(rule);
		Matcher matcher = pattern.matcher(str);
		boolean result = matcher.matches();
		return result;
	}
	
	public boolean isMatched(String fileName) {

		Iterator<RuleObj> rule_iter = Utils.rulesList.iterator();
		while (rule_iter.hasNext()) {
			RuleObj ruleObj = rule_iter.next();
			if (checkMatcher(ruleObj.getRule(), fileName)) {
				return true;
			}
		}
		return false;
	}
}
