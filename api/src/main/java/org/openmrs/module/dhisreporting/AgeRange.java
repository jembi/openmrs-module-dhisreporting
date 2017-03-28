/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.dhisreporting;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.reporting.common.Age;
import org.openmrs.module.reporting.common.Age.Unit;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.ObjectUtil;

/**
 * This class customizes {@link org.openmrs.module.reporting.common.AgeRange} to
 * use {@link DurationUnit} instead of {@link Unit}.<br/>
 * An Age Range, inclusive of end ages For example, an Age Range of minAge = 0,
 * maxAge = 14 would include anyone less than 15 years of age
 */
public class AgeRange {

	// ***********************
	// PROPERTIES
	// ***********************

	private Integer minAge;
	private DurationUnit minAgeUnit;
	private Integer maxAge;
	private DurationUnit maxAgeUnit;
	private String label;

	// ***********************
	// CONSTRUCTORS
	// ***********************

	public AgeRange() {
	}

	public AgeRange(String ageQuery, DurationUnit minAgeUnit, DurationUnit maxAgeUnit) {
		if (StringUtils.isNotBlank(ageQuery) && minAgeUnit != null && maxAgeUnit != null) {
			setMinAgeUnit(minAgeUnit);
			setMaxAgeUnit(maxAgeUnit);

			if (ageQuery.endsWith("+")) {
				ageQuery = ">=" + ageQuery.replace("+", "");
			}
			if (ageQuery.indexOf("<=") >= 0) {
				setMinAge(0);
				setMaxAge(Integer.parseInt(ageQuery.split("<=")[1]));
			} else if (ageQuery.indexOf(">=") >= 0) {
				setMinAge(Integer.parseInt(ageQuery.split(">=")[1]));
			} else if (ageQuery.indexOf("<") >= 0) {
				setMinAge(0);
				setMaxAge(Integer.parseInt(ageQuery.split("<")[1]) - 1);
			} else if (ageQuery.indexOf(">") >= 0) {
				setMinAge(Integer.parseInt(ageQuery.split(">")[1]) + 1);
			} else if (ageQuery.indexOf("-") >= 0) {
				setMinAge(Integer.parseInt(ageQuery.split("-")[0]));
				setMaxAge(Integer.parseInt(ageQuery.split("-")[1]));
			}
		}

	}

	/**
	 * Range only Constructor
	 */
	public AgeRange(Integer minAgeYears, Integer maxAgeYears) {
		this(minAgeYears, DurationUnit.YEARS, maxAgeYears, DurationUnit.YEARS, null);
	}

	/**
	 * Full Constructor
	 */
	public AgeRange(Integer minAge, DurationUnit minAgeUnit, Integer maxAge, DurationUnit maxAgeUnit, String label) {
		this.minAge = minAge;
		this.minAgeUnit = minAgeUnit;
		this.maxAge = maxAge;
		this.maxAgeUnit = maxAgeUnit;
		this.label = label;
	}

	// ***********************
	// INSTANCE METHODS
	// ***********************

	/**
	 * Returns true if an age is within the given range
	 */
	public Boolean isInRange(Age age) {
		Integer ageMonths = age.getFullMonths();
		Integer ageYears = age.getFullYears();
		if (minAge != null) {
			DurationUnit minAgeUnit = ObjectUtil.nvl(getMinAgeUnit(), DurationUnit.YEARS);
			if (minAgeUnit == DurationUnit.MONTHS) {
				if (ageMonths < minAge) {
					return false;
				}
			} else {
				if (ageYears < minAge) {
					return false;
				}
			}
		}
		if (maxAge != null) {
			DurationUnit maxAgeUnit = ObjectUtil.nvl(getMaxAgeUnit(), DurationUnit.YEARS);
			if (maxAgeUnit == DurationUnit.MONTHS) {
				if (ageMonths > maxAge) {
					return false;
				}
			} else {
				if (ageYears > maxAge) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return ObjectUtil.nvlStr(minAge, 0) + ObjectUtil.decode(maxAge, "+", "-" + maxAge);
	}

	public String toWordString() {
		return ObjectUtil
				.nvlStr(NumberToWord.convert(minAge)
						.replaceAll(" ",
								""),
						0)
				+ (maxAge != null
						? ObjectUtil.decode(WordUtils.capitalize(NumberToWord.convert(maxAge).replaceAll(" ", "")), "+",
								"To" + WordUtils.capitalize(NumberToWord.convert(maxAge).replaceAll(" ", "")))
						: "AndAbove");
	}

	// ***********************
	// PROPERTY ACCESS
	// ***********************

	/**
	 * @return the minAge
	 */
	public Integer getMinAge() {
		return minAge;
	}

	/**
	 * @param minAge
	 *            the minAge to set
	 */
	public void setMinAge(Integer minAge) {
		this.minAge = minAge;
	}

	/**
	 * @return the minAgeUnit
	 */
	public DurationUnit getMinAgeUnit() {
		return minAgeUnit;
	}

	/**
	 * @param minAgeUnit
	 *            the minAgeUnit to set
	 */
	public void setMinAgeUnit(DurationUnit minAgeUnit) {
		this.minAgeUnit = minAgeUnit;
	}

	/**
	 * @return the maxAge
	 */
	public Integer getMaxAge() {
		return maxAge;
	}

	/**
	 * @param maxAge
	 *            the maxAge to set
	 */
	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}

	/**
	 * @return the maxAgeUnit
	 */
	public DurationUnit getMaxAgeUnit() {
		return maxAgeUnit;
	}

	/**
	 * @param maxAgeUnit
	 *            the maxAgeUnit to set
	 */
	public void setMaxAgeUnit(DurationUnit maxAgeUnit) {
		this.maxAgeUnit = maxAgeUnit;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
}
