package org.obiba.onyx.quartz.core.domain.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.core.domain.condition.Condition;

public class Question implements Serializable, ILocalizable {

	private static final long serialVersionUID = -7795909448581432466L;

	private Page page;

	private String name;

	private String number;

	private Boolean mandatory;

	private Boolean multiple;

	private List<QuestionCategory> questionCategories;

	private OpenAnswerDefinition openAnswerDefinition;

	private Condition condition;

	private Question parentQuestion;

	private List<Question> questions;

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public Boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public List<QuestionCategory> getQuestionCategories() {
		return questionCategories != null ? questionCategories
				: (questionCategories = new ArrayList<QuestionCategory>());
	}

	public void addQuestionCategories(QuestionCategory questionCategory) {
		if (questionCategory != null) {
			getQuestionCategories().add(questionCategory);
			questionCategory.setQuestion(this);
		}
	}

	public OpenAnswerDefinition getOpenAnswerDefinition() {
		return openAnswerDefinition;
	}

	public void setOpenAnswerDefinition(
			OpenAnswerDefinition openAnswerDefinition) {
		this.openAnswerDefinition = openAnswerDefinition;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public Question getParentQuestion() {
		return parentQuestion;
	}

	public void setParentQuestion(Question parentQuestion) {
		this.parentQuestion = parentQuestion;
	}

	public List<Question> getQuestions() {
		return questions != null ? questions
				: (questions = new ArrayList<Question>());
	}

	public void addQuestion(Question question) {
		if (question != null) {
			getQuestions().add(question);
		}
	}

	public String getPropertyKey(String property) {
		return getClass().getSimpleName() + "." + getName() + "." + property;
	}

}
