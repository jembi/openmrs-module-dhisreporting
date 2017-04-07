package org.openmrs.module.dhisreporting.mapping;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptName;
import org.openmrs.api.context.Context;

public class CodedDisaggregation {

	/**
	 * @param question
	 * @param value
	 * @return matched question's answer concept
	 */
	public static Concept matchCodedQuestionDisaggregation(Integer question, String value) {
		if (question != null && StringUtils.isNotBlank(value)) {
			Concept q = Context.getConceptService().getConcept(question);

			if (q != null && !q.getAnswers().isEmpty()) {
				return conceptAnswerExistsByName(q.getAnswers(), value);
			}
		}
		return null;
	}

	private static Concept conceptAnswerExistsByName(Collection<ConceptAnswer> answers, String answerString) {
		for (ConceptAnswer a : answers) {
			if (a != null && a.getAnswerConcept() != null && a.getAnswerConcept().getName() != null) {
				for (ConceptName n : a.getAnswerConcept().getNames()) {
					if (StringUtils.isNotBlank(n.getName()) && n.getName().equalsIgnoreCase(answerString))
						return a.getAnswerConcept();
				}
			}
		}
		return null;
	}
}
