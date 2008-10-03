package org.obiba.onyx.quartz.core.domain.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page implements Serializable, ILocalizable {

	private static final long serialVersionUID = -7732601103831162009L;

	private Section questionnaireSection;

	private String name;
	
	private List<Question> questions;
	
	private Questionnaire questionnaire;

	public Section getQuestionnaireSection() {
		return questionnaireSection;
	}

	public void setQuestionnaireSection(Section questionnaireSection) {
		this.questionnaireSection = questionnaireSection;
	}
	
	public Questionnaire getQuestionnaire() {
		return questionnaire;
	}

	public void setQuestionnaire(Questionnaire questionnaire) {
		this.questionnaire = questionnaire;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Question> getQuestions() {
		return questions != null ? questions : (questions = new ArrayList<Question>());
	}
	
	public void addQuestion(Question question) {
		if (question != null) {
			getQuestions().add(question);
			question.setPage(this);
		}
	}
	
	public String getPropertyKey(String property) {
		return getClass().getSimpleName() + "." + getName() + "." + property;
	}
	
}
