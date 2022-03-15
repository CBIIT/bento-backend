package gov.nih.nci.bento.model.search.result;

import org.elasticsearch.common.text.Text;
import java.util.Map;

public interface HighLightMapper {
    Map<String, Object> getMap(Map<String, Object> source, Text fragment);
}