package org.openmrs.module.dhisreporting;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

public class WordToNumber {
	public static String WithSeparator(long number) {
		if (number < 0) {
			return "-" + WithSeparator(-number);
		}
		if (number / 1000L > 0) {
			return WithSeparator(number / 1000L) + "," + String.format("%1$03d", number % 1000L);
		} else {
			return String.format("%1$d", number);
		}
	}

	private static String[] before20 = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
			"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen",
			"nineteen" };
	private static String[] twentyTo100 = { "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty",
			"ninety", "hundred" };

	private static long[] values = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 30, 40,
			50, 60, 70, 80, 90, 100 };

	private static String[] getAllNumerals() {
		return (String[]) ArrayUtils.addAll(before20, twentyTo100);
	}

	private static ArrayList<String> list = new ArrayList<String>(Arrays.asList(getAllNumerals()));

	public static long parseNumerals(String text) throws Exception {
		long value = 0;
		String[] words = text.replaceAll(" and ", " ").split("\\s");

		if (words.length == 1) {
			for (String s : twentyTo100) {
				if (text.startsWith(s)) {
					words = text.split("(?<=" + s + ")");
					break;
				}
			}
		}

		for (String word : words) {
			if (!list.contains(word)) {
				throw new Exception("Unknown token : " + word);
			}

			long subval = getValueOf(word);
			if (subval == 100) {
				if (value == 0)
					value = 100;
				else
					value *= 100;
			} else
				value += subval;
		}

		return value;
	}

	private static long getValueOf(String word) {
		return values[list.indexOf(word)];
	}

	private static String[] words = { "trillion", "billion", "million", "thousand" };

	private static long[] digits = { 1000000000000L, 1000000000L, 1000000L, 1000L };

	public static Long convert(String text) throws Exception {
		text = text.toLowerCase().replaceAll("[\\-,]", " ").replaceAll(" and ", " ");
		long totalValue = 0;
		boolean processed = false;
		for (int n = 0; n < words.length; n++) {
			int index = text.indexOf(words[n]);
			if (index >= 0) {
				String text1 = text.substring(0, index).trim();
				String text2 = text.substring(index + words[n].length()).trim();

				if (text1.equals(""))
					text1 = "one";

				if (text2.equals(""))
					text2 = "zero";

				totalValue = parseNumerals(text1) * digits[n] + convert(text2);
				processed = true;
				break;
			}
		}

		if (processed)
			return totalValue;
		else
			return parseNumerals(text);
	}
}