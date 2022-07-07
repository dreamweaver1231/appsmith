package com.appsmith.external.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document
public class Datasource extends BaseDomain {

    @Transient
    public static final String DEFAULT_NAME_PREFIX = "Untitled Datasource";

    String name;

    String pluginId;

    // name of the plugin. used to log analytics events where pluginName is a required attribute
    // It'll be null if not set
    @Transient
    String pluginName;
    
    //Organizations migrated to workspaces, kept the field as deprecated to support the old migration
    @Deprecated
    String organizationId;

    String workspaceId;

    String templateName;

    DatasourceConfiguration datasourceConfiguration;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Set<String> invalids;

    /*
     * - To return useful hints to the user.
     * - These messages are generated by the API server based on the other datasource attributes.
     */
    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Set<String> messages = new HashSet<>();

    /*
     * This field is used to determine if the Datasource has been generated by the client or auto-generated by the system.
     * We use this field because when embedded datasources are null, spring-data auditable interfaces throw exceptions
     * while trying set createdAt and updatedAt properties on the null object
     */
    @Transient
    @JsonIgnore
    Boolean isAutoGenerated = false;

    // The structure is ignored in JSON as it is not sent as part of the datasources API. We have a separate endpoint
    // to obtain the structure of the datasource. The value of this field serves as the cache.
    @JsonIgnore
    DatasourceStructure structure;


    /*
     * This field is introduced as part of git sync feature, for the git import we will need to identify the datasource's
     * which are not configured. This way user can configure those datasource, which may have been introduced as part of git import.
     */
    Boolean isConfigured;

    @Transient
    Boolean isRecentlyCreated;

    /*
     * This field is meant to indicate whether the datasource is part of a template, mock or a copy of the same.
     * The field is not used anywhere in the codebase because templates are created directly in the DB, and the field
     * serves only as a DTO property.
     */
    Boolean isTemplateOrMock;

    /**
     * This method is here so that the JSON version of this class' instances have a `isValid` field, for backwards
     * compatibility. It may be removed, when sure that no API received is relying on this field.
     *
     * @return boolean, indicating whether this datasource is valid or not.
     */
    public boolean getIsValid() {
        return CollectionUtils.isEmpty(invalids);
    }

    /**
     * Intended to function like `.equals`, but only semantically significant fields, except for the ID. Semantically
     * significant just means that if two datasource have same values for these fields, actions against them will behave
     * exactly the same.
     * @return true if equal, false otherwise.
     */
    public boolean softEquals(Datasource other) {
        if (other == null) {
            return false;
        }

        return new EqualsBuilder()
                .append(name, other.name)
                .append(pluginId, other.pluginId)
                .append(isAutoGenerated, other.isAutoGenerated)
                .append(datasourceConfiguration, other.datasourceConfiguration)
                .isEquals();
    }

    public void sanitiseToExportResource(Map<String, String> pluginMap) {
        this.setPolicies(null);
        this.setStructure(null);
        this.setUpdatedAt(null);
        this.setCreatedAt(null);
        this.setUserPermissions(null);
        this.setIsConfigured(null);
        this.setInvalids(null);
        this.setId(null);
        this.setWorkspaceId(null);
        this.setOrganizationId(null);
        this.setPluginId(pluginMap.get(this.getPluginId()));
    }

}
