package org.obiba.onyx.quartz.core.domain.answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.Entity;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.question.QuestionnairePage;
import org.obiba.onyx.quartz.core.domain.question.QuestionnaireVersion;

@Entity
public class QuestionnaireParticipant extends AbstractEntity {

	private Participant participant;

	private QuestionnaireVersion questionnaireVersion;

	private Locale locale;

	private QuestionnairePage currentQuestionnairePage;

	private QuestionnairePage lastVisitedQuestionnairePage;

	private List<ParticipantAnswer> participantAnswers;

	public Participant getParticipant() {
		return participant;
	}

	public void setParticipant(Participant participant) {
		this.participant = participant;
	}

	public QuestionnaireVersion getQuestionnaireVersion() {
		return questionnaireVersion;
	}

	public void setQuestionnaireVersion(
			QuestionnaireVersion questionnaireVersion) {
		this.questionnaireVersion = questionnaireVersion;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public QuestionnairePage getCurrentQuestionnairePage() {
		return currentQuestionnairePage;
	}

	public void setCurrentQuestionnairePage(
			QuestionnairePage currentQuestionnairePage) {
		this.currentQuestionnairePage = currentQuestionnairePage;
	}

	public QuestionnairePage getLastVisitedQuestionnairePage() {
		return lastVisitedQuestionnairePage;
	}

	public void setLastVisitedQuestionnairePage(
			QuestionnairePage lastVisitedQuestionnairePage) {
		this.lastVisitedQuestionnairePage = lastVisitedQuestionnairePage;
	}

	public List<ParticipantAnswer> getParticipantAnswers() {
		return participantAnswers != null ? participantAnswers
				: (participantAnswers = new ArrayList<ParticipantAnswer>());
	}

	public void addParticipantAnswer(ParticipantAnswer participantAnswer) {
		if (participantAnswer != null) {
			getParticipantAnswers().add(participantAnswer);
			participantAnswer.setQuestionnaireParticipant(this);
		}
	}

}
