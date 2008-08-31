package org.obiba.onyx.quartz.core.domain.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuestionnaireSection implements Serializable {

	private QuestionnaireVersion questionnaireVersion;
	
	private String label;
	
	private Integer displayInParentOrder;
	
	private List<QuestionnairePage> questionnairePages;
	
	private List<QuestionnaireSection> questionnaireSections;
	
	public QuestionnaireSection() {}

	public QuestionnaireVersion getQuestionnaireVersion() {
		return questionnaireVersion;
	}

	public void setQuestionnaireVersion(QuestionnaireVersion questionnaireVersion) {
		this.questionnaireVersion = questionnaireVersion;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getDisplayInParentOrder() {
		return displayInParentOrder;
	}

	public void setDisplayInParentOrder(Integer displayInParentOrder) {
		this.displayInParentOrder = displayInParentOrder;
	}

	public List<QuestionnairePage> getQuestionnairePages() {
		return questionnairePages != null ? questionnairePages : (questionnairePages = new ArrayList<QuestionnairePage>());
	}
	
	public void addQuestionnairePage(QuestionnairePage questionnairePage) {
		if (questionnairePage != null) {
			getQuestionnairePages().add(questionnairePage);
			questionnairePage.setQuestionnaireSection(this);
		}
	}
	
	public List<QuestionnaireSection> getQuestionnaireSections() {
		return questionnaireSections != null ? questionnaireSections : (questionnaireSections = new ArrayList<QuestionnaireSection>());
	}
	
}
