package org.obiba.onyx.quartz.core.domain.answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Participant;

@Entity
public class QuestionnaireParticipant extends AbstractEntity {

	private static final long serialVersionUID = -4091599688342582439L;

	private Participant participant;

	private String questionnaireName;

	private String questionnaireVersion;

	private Locale locale;

	private List<QuestionAnswer> questionAnswers;

	public Participant getParticipant() {
		return participant;
	}

	public void setParticipant(Participant participant) {
		this.participant = participant;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getQuestionnaireName() {
		return questionnaireName;
	}

	public void setQuestionnaireName(String questionnaireName) {
		this.questionnaireName = questionnaireName;
	}

	public String getQuestionnaireVersion() {
		return questionnaireVersion;
	}

	public void setQuestionnaireVersion(String questionnaireVersion) {
		this.questionnaireVersion = questionnaireVersion;
	}

	public List<QuestionAnswer> getParticipantAnswers() {
		return questionAnswers != null ? questionAnswers
				: (questionAnswers = new ArrayList<QuestionAnswer>());
	}

	public void addParticipantAnswer(QuestionAnswer questionAnswers) {
		if (questionAnswers != null) {
			getParticipantAnswers().add(questionAnswers);
			questionAnswers.setQuestionnaireParticipant(this);
		}
	}

}
