package org.obiba.onyx.quartz.core.engine.questionnaire.question;

/**
 * A localizable element is able to provide the localization key for each of its properties.
 * @author Yannick Marcon
 *
 */
public interface ILocalizable {
	
	/**
	 * Get the localiation key for the given property.
	 * @param property
	 * @return
	 */
	public String getPropertyKey(String property);
	
	/**
	 * Get the properties allowed for this localizable element.
	 * @return
	 */
	public String[] getProperties();
}
