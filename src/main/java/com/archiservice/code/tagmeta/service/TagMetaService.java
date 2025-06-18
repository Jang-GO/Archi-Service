package com.archiservice.code.tagmeta.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TagMetaService {
    List<String> extractTagsFromCode(Long tagCode);
    Long calculateTagCodeFromDescriptions(List<String> tagDescriptions);
}
