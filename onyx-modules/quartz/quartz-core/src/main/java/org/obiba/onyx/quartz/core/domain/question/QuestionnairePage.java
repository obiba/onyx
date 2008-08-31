package org.obiba.onyx.quartz.core.domain.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuestionnairePage implements Serializable {

	private QuestionnaireSection questionnaireSection;

	private String label;

	private Integer displayOrder;

	private Integer displayInParentOrder;
	
	private List<Question> questions;

	public QuestionnairePage() {}

	public QuestionnaireSection getQuestionnaireSection() {
		return questionnaireSection;
	}

	public void setQuestionnaireSection(QuestionnaireSection questionnaireSection) {
		this.questionnaireSection = questionnaireSection;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public Integer getDisplayInParentOrder() {
		return displayInParentOrder;
	}

	public void setDisplayInParentOrder(Integer displayInParentOrder) {
		this.displayInParentOrder = displayInParentOrder;
	}

	public List<Question> getQuestions() {
		return questions != null ? questions : (questions = new ArrayList<Question>());
	}
	
	public void addQuestion(Question question) {
		if (question != null) {
			getQuestions().add(question);
			question.setQuestionnairePage(this);
		}
	}
	
	
	
}
