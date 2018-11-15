package eu.einfracentral.utils;

import eu.einfracentral.domain.Provider;
import eu.einfracentral.domain.VocabularyEntry;
import eu.einfracentral.registry.service.ProviderService;
import eu.einfracentral.registry.service.VocabularyService;
import eu.openminted.registry.core.domain.Facet;
import eu.openminted.registry.core.domain.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FacetLabelService {

    private static final Logger logger = LogManager.getLogger(FacetLabelService.class);
    private ProviderService<Provider, Authentication> providerService;
    private VocabularyService vocabularyService;

    private Map<String, String> subcategoryNames;

    @Autowired
    FacetLabelService(ProviderService<Provider, Authentication> providerService, VocabularyService vocabularyService) {
        this.providerService = providerService;
        this.vocabularyService = vocabularyService;
    }

    @PostConstruct
    void createSubcategoriesMap() {
        subcategoryNames = new HashMap<>();
        try {
            Map<String, VocabularyEntry> categories = vocabularyService.get("categories").getEntries();
            for (Map.Entry<String, VocabularyEntry> entry : categories.entrySet()) {
                entry.getValue().getChildren().forEach(sub -> subcategoryNames.put(sub.getId(), sub.getName()));
            }
        } catch (Exception e) {
            logger.error("ERROR", e);
        }
    }

    // FIXME: replace this method
    List<Facet> createFacetValueLabels(List<Facet> facets) { // TODO: core should probably return these values
        return facets.stream()
                .peek(f -> f.setValues(f.getValues()
                        .stream()
                        .peek(value -> {
                            String val = value.getValue();
                            value.setLabel(toProperCase(toProperCase(val, "-", "-"), "_", " "));
                        })
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    // FIXME: remove this as well
    String toProperCase(String str, String delimiter, String newDelimiter) {
        return String.join(newDelimiter, Arrays.stream(str.split(delimiter)).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.toList()));
    }

    String getProviderLabel(String value) {
        return providerService.get(value).getName();
    }

    String getVocabularyLabel(String type, String value) {
        return vocabularyService.get(type).getEntries().get(value).getName();
    }

    public void createLabels(List<Facet> facets) {
        for (Facet facet : facets) {
            for (Value value : facet.getValues()) {
                switch (facet.getField()) {
                    case "providers":
                        value.setLabel(getProviderLabel(value.getValue()));
                        break;
                    case "category":
                        value.setLabel(getVocabularyLabel("categories", value.getValue()));
                        break;
                    case "subcategory":
                        value.setLabel(subcategoryNames.get(value.getValue()));
                        break;
                    case "language":
                        value.setLabel(getVocabularyLabel("languages", value.getValue()));
                        break;
                    case "place":
                        value.setLabel(getVocabularyLabel("places", value.getValue()));
                        break;
                    case "trl":
                        value.setLabel(getVocabularyLabel("trl", value.getValue()));
                        break;

                    case "lifeCycleStatus":
                        value.setLabel(getVocabularyLabel("lifecyclestatus", value.getValue()));
                        break;

                    default:
                        value.setLabel(toProperCase(toProperCase(value.getValue(), "-", "-"), "_", " "));
                }
            }
        }
    }
}
