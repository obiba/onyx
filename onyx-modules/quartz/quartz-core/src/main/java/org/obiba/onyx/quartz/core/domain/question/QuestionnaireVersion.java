package org.obiba.onyx.quartz.core.domain.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuestionnaireVersion implements Serializable {

	private Questionnaire questionnaire;

	private String version;

	private String description;

	private String labelStart;

	private String labelInterrupt;

	private String labelResume;

	private String labelEnd;

	private String labelNext;

	private String labelPrevious;

	private String labelCancel;

	private QuestionnaireLayout questionnaireLayout;

	private List<Language> languages;

	private List<QuestionnaireSection> questionnaireSections;

	public QuestionnaireVersion() {
	}

	public Questionnaire getQuestionnaire() {
		return questionnaire;
	}

	public void setQuestionnaire(Questionnaire questionnaire) {
		this.questionnaire = questionnaire;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabelStart() {
		return labelStart;
	}

	public void setLabelStart(String labelStart) {
		this.labelStart = labelStart;
	}

	public String getLabelInterrupt() {
		return labelInterrupt;
	}

	public void setLabelInterrupt(String labelInterrupt) {
		this.labelInterrupt = labelInterrupt;
	}

	public String getLabelResume() {
		return labelResume;
	}

	public void setLabelResume(String labelResume) {
		this.labelResume = labelResume;
	}

	public String getLabelEnd() {
		return labelEnd;
	}

	public void setLabelEnd(String labelEnd) {
		this.labelEnd = labelEnd;
	}

	public String getLabelNext() {
		return labelNext;
	}

	public void setLabelNext(String labelNext) {
		this.labelNext = labelNext;
	}

	public String getLabelPrevious() {
		return labelPrevious;
	}

	public void setLabelPrevious(String labelPrevious) {
		this.labelPrevious = labelPrevious;
	}

	public String getLabelCancel() {
		return labelCancel;
	}

	public void setLabelCancel(String labelCancel) {
		this.labelCancel = labelCancel;
	}

	public QuestionnaireLayout getQuestionnaireLayout() {
		return questionnaireLayout;
	}

	public void setQuestionnaireLayout(QuestionnaireLayout questionnaireLayout) {
		this.questionnaireLayout = questionnaireLayout;
	}

	public List<Language> getLanguages() {
		return languages != null ? languages
				: (languages = new ArrayList<Language>());
	}

	public List<QuestionnaireSection> getQuestionnaireSections() {
		return questionnaireSections != null ? questionnaireSections
				: (questionnaireSections = new ArrayList<QuestionnaireSection>());
	}

}
