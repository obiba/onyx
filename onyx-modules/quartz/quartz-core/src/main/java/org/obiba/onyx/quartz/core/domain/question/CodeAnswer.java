package org.obiba.onyx.quartz.core.domain.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CodeAnswer implements Serializable {

	private String code;

	private String label;

	private String image;

	private List<QuestionCodeAnswer> questionCodeAnswers;
	
	private OpenAnswerDefinition openAnswerDefinition;

	public CodeAnswer() {
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public List<QuestionCodeAnswer> getQuestionCodeAnswers() {
		return questionCodeAnswers != null ? questionCodeAnswers
				: (questionCodeAnswers = new ArrayList<QuestionCodeAnswer>());
	}

	public void addQuestionCodeAnswer(QuestionCodeAnswer questionCodeAnswer) {
		if (questionCodeAnswer != null) {
			getQuestionCodeAnswers().add(questionCodeAnswer);
			questionCodeAnswer.setCodeAnswer(this);
		}
	}

	public OpenAnswerDefinition getOpenAnswerDefinition() {
		return openAnswerDefinition;
	}

	public void setOpenAnswerDefinition(OpenAnswerDefinition openAnswerDefinition) {
		this.openAnswerDefinition = openAnswerDefinition;
	}
	
}
