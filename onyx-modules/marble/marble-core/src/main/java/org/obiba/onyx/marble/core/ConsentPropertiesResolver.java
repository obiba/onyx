package org.obiba.onyx.marble.core;

import java.util.List;
import java.util.Properties;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsentPropertiesResolver {
    private static final Logger log = LoggerFactory.getLogger(ConsentPropertiesResolver.class);

    private static final String PREFIX = "org.obiba.onyx.marble.consent";

    private static final String BASENAME = "basename";

    private static final String VARIABLE_TO_FIELD_MAP = "variableToField";

    private Properties marbleConfigProperties;

    public void setMarbleConfigProperties(Properties marbleConfigProperties) {
        this.marbleConfigProperties = marbleConfigProperties;
    }

    public String getBasename(String stageName) {
        return safeGetStageProperty(stageName, BASENAME);
    }

    public String getVariableToFieldMap(String stageName) {
        return safeGetStageProperty(stageName, VARIABLE_TO_FIELD_MAP);
    }

    private String safeGetStageProperty(String stageName, String suffix) {
        String variableToField = marbleConfigProperties.getProperty(getPropertyId(stageName, suffix));

        if(variableToField == null) variableToField = marbleConfigProperties.getProperty(getPropertyId(null, suffix));

        return variableToField;
    }

    private String getPropertyId(String stageName, String suffix) {
        List<String> parts = Lists.newArrayList(PREFIX);

        if(!Strings.isNullOrEmpty(stageName)) {
            parts.add(stageName);
        }

        parts.add(suffix);

        return Joiner.on(".").join(parts);
    }
}
