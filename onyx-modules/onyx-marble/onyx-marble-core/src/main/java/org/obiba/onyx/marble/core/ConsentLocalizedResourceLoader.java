package org.obiba.onyx.marble.core;

import org.obiba.onyx.core.io.support.LocalizedResourceLoader;
import org.obiba.onyx.core.service.ActiveInterviewService;

public class ConsentLocalizedResourceLoader extends LocalizedResourceLoader {

    private ActiveInterviewService activeInterviewService;

    private ConsentPropertiesResolver consentPropertiesResolver;

    public void setConsentPropertiesResolver(ConsentPropertiesResolver consentPropertiesResolver) {
        this.consentPropertiesResolver = consentPropertiesResolver;
    }

    public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
        this.activeInterviewService = activeInterviewService;
    }

    @Override
    protected String getResourceName() {
        return consentPropertiesResolver.getBasename(activeInterviewService.getInteractiveStage().getName());
    }
}
