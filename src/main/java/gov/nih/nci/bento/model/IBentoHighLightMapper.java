package gov.nih.nci.bento.model;

import org.elasticsearch.common.text.Text;
import java.util.Map;

public interface IBentoHighLightMapper {
    Map<String, Object> getMap(Map<String, Object> source, Text fragment);
}