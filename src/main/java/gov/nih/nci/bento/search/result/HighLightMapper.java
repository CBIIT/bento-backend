package gov.nih.nci.bento.search.result;

import org.opensearch.common.text.Text;
import java.util.Map;

public interface HighLightMapper {
    Map<String, Object> getMap(Map<String, Object> source, Text fragment);
}