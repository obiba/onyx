package org.obiba.onyx.quartz.core.domain.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Questionnaire implements Serializable {

	private String name;

	private String currentVersion;
	
	private List<QuestionnaireVersion> questionnaireVersions;

	public Questionnaire() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}

	public List<QuestionnaireVersion> getQuestionnaireVersions() {
		return questionnaireVersions != null ? questionnaireVersions : (questionnaireVersions = new ArrayList<QuestionnaireVersion>());
	}
	
	public void addQuestionnaireVersion(QuestionnaireVersion questionnaireVersion) {
		if (questionnaireVersion != null) {
			getQuestionnaireVersions().add(questionnaireVersion);
			questionnaireVersion.setQuestionnaire(this);
		}
	}
	

}
