package org.obiba.onyx.quartz.core.domain.answer;

import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;

@Entity
public class QuestionAnswer extends AbstractEntity {

	private static final long serialVersionUID = 8513736303565362142L;

	private String questionName;

	private String comment;
	
	private QuestionnaireParticipant questionnaireParticipant;

	public String getQuestionName() {
		return questionName;
	}

	public void setQuestionName(String questionName) {
		this.questionName = questionName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public QuestionnaireParticipant getQuestionnaireParticipant() {
		return questionnaireParticipant;
	}

	public void setQuestionnaireParticipant(
			QuestionnaireParticipant questionnaireParticipant) {
		this.questionnaireParticipant = questionnaireParticipant;
	}

}
