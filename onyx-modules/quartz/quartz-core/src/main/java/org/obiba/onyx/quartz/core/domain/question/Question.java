package org.obiba.onyx.quartz.core.domain.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.core.domain.condition.Condition;

public abstract class Question implements Serializable {

	private QuestionnairePage questionnairePage;

	private String name;

	private String number;

	private String label;

	private String instructions;

	private String caption;

	private String help;

	private String image;

	private Integer displayInParentOrder;

	private Boolean mandatory;

	private Boolean multiple;

	private CodeAnswerLayout codeAnswerLayout;

	private List<QuestionCodeAnswer> questionCodeAnswers;
	
	private OpenAnswerDefinition openAnswerDefinition;
	
	private Condition condition;

	public QuestionnairePage getQuestionnairePage() {
		return questionnairePage;
	}

	public void setQuestionnairePage(QuestionnairePage questionnairePage) {
		this.questionnairePage = questionnairePage;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public Integer getDisplayInParentOrder() {
		return displayInParentOrder;
	}

	public void setDisplayInParentOrder(Integer displayInParentOrder) {
		this.displayInParentOrder = displayInParentOrder;
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

	public CodeAnswerLayout getCodeAnswerLayout() {
		return codeAnswerLayout;
	}

	public void setCodeAnswerLayout(CodeAnswerLayout codeAnswerLayout) {
		this.codeAnswerLayout = codeAnswerLayout;
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
			questionCodeAnswer.setQuestion(this);
		}
	}

	public OpenAnswerDefinition getOpenAnswerDefinition() {
		return openAnswerDefinition;
	}

	public void setOpenAnswerDefinition(OpenAnswerDefinition openAnswerDefinition) {
		this.openAnswerDefinition = openAnswerDefinition;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}
	
}
