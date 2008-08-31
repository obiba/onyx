package org.obiba.onyx.quartz.core.domain.question;

import java.io.Serializable;
import java.util.Locale;

public class Language implements Serializable {

	Locale locale;

	public Language() {
	}

	public Language(Locale locale) {
		super();
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

}
