package com.archiservice.code.tagmeta.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.archiservice.code.tagmeta.domain.TagMeta;

public interface TagMetaService {
    List<String> extractTagsFromCode(Long tagCode);
    Long calculateTagCodeFromDescriptions(List<String> tagDescriptions);
    public TagMeta findTagMetaByDescription(String description);
}
