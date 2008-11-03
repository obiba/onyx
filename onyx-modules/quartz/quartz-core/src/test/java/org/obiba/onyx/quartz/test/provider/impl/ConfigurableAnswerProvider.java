/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.test.provider.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.test.provider.AnswerProvider;

import com.thoughtworks.xstream.XStream;

/**
 * 
 */
public class ConfigurableAnswerProvider implements AnswerProvider {

  //
  // Instance Variables
  //

  private CategoryAnswer defaultAnswer;

  private Map<String, CategoryAnswer> answers;

  private List<Group> groups;

  //
  // Constructors
  //

  public ConfigurableAnswerProvider() {
    answers = new HashMap<String, CategoryAnswer>();
    groups = new ArrayList<Group>();
  }

  //
  // AnswerProvider Methods
  //

  /**
   * Returns the answer configured for the specified question.
   * 
   * If no answer has been configured for the question, returns the answer configured for the group to which the
   * question belongs.
   * 
   * If the group does not belong to a group, returns the default answer.
   * 
   * If no default answer has been configured, returns <code>null</code>.
   * 
   * @param question question
   * @return answer answer configured for the question, otherwise the answer configured for the question's group,
   * otherwise the default answer, otherwise <code>null</code>
   */
  public CategoryAnswer getAnswer(Question question) {
    CategoryAnswer answer = answers.get(question.getName());

    if(answer == null) {
      answer = getAnswerFromGroup(question);

      if(answer == null) {
        answer = defaultAnswer;
      }
    }

    return answer;
  }

  //
  // Methods
  //

  /**
   * Sets the default answer.
   * 
   * This is returned by <code>getAnswer</code> if no answer has been configured for the specified question.
   * 
   * @param answer default answer
   */
  public void setDefaultAnswer(CategoryAnswer answer) {
    this.defaultAnswer = answer;
  }

  /**
   * Configures the provider to return the specified answer for the specified question.
   * 
   * @param question question
   * @param answer answer
   */
  public void setAnswer(Question question, CategoryAnswer answer) {
    answers.put(question.getName(), answer);
  }

  /**
   * Configures the provider to return the specified answer for the specified group of questions.
   * 
   * @param group name of group
   * @param answer answer
   */
  public void setGroupAnswer(String group, CategoryAnswer answer) {
    Group groupObj = findOrCreateGroup(group);
    groupObj.answer = answer;
  }

  /**
   * Adds a question to a group.
   * 
   * @param group name of group.
   * @param question question
   */
  public void addQuestionToGroup(String group, Question question) {
    Group groupObj = findOrCreateGroup(group);
    groupObj.questions.add(question.getName());
  }

  /**
   * If the specified question belongs to a group, returns the answer configured for the group.
   * 
   * @param question question
   * @return answer configured for the group to which the question belongs (or <code>null</code> if the question does
   * not belong to a group)
   */
  private CategoryAnswer getAnswerFromGroup(Question question) {
    CategoryAnswer answer = null;

    Group group = null;

    for(Group aGroup : groups) {
      if(aGroup.questions.contains(question.getName())) {
        group = aGroup;
        break;
      }
    }

    if(group != null) {
      answer = group.answer;
    }

    return answer;
  }

  private Group findOrCreateGroup(String name) {
    Group group = null;

    for(Group aGroup : groups) {
      if(aGroup.name.equals(name)) {
        group = aGroup;
        break;
      }
    }

    if(group == null) {
      group = new Group(name);
      groups.add(group);
    }

    return group;
  }

  public static ConfigurableAnswerProvider fromXmlFile(File file) throws IOException {
    ConfigurableAnswerProvider answerProvider = null;

    FileInputStream is = null;

    try {
      is = new FileInputStream(file);
      answerProvider = fromXml(is);
    } finally {
      if(is != null) {
        is.close();
      }
    }

    return answerProvider;
  }

  public static ConfigurableAnswerProvider fromXmlResource(String resource) {
    return fromXml(ConfigurableAnswerProvider.class.getClassLoader().getResourceAsStream(resource));
  }

  public static ConfigurableAnswerProvider fromXml(InputStream is) {
    XStream xstream = new XStream();
    xstream.alias("configurableAnswerProvider", ConfigurableAnswerProvider.class);
    xstream.alias("categoryAnswer", CategoryAnswer.class);
    xstream.alias("group", ConfigurableAnswerProvider.Group.class);

    return (ConfigurableAnswerProvider) xstream.fromXML(is);
  }

  //
  // Inner Classes
  //

  private static class Group {
    private String name;

    private Set<String> questions;

    private CategoryAnswer answer;

    public Group(String name) {
      this.name = name;
      questions = new HashSet<String>();
    }
  }
}