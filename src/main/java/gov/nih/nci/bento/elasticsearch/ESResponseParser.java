package gov.nih.nci.bento.elasticsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESResponseParser {

    private static Gson gson = new GsonBuilder().serializeNulls().create();

    public static List<Map<String, Object>> getHits(JsonObject response) {
        JsonArray hitsArray = response.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
        ArrayList<Map<String, Object>> hitsMap = new ArrayList<>();
        hitsArray.forEach(x ->
                hitsMap.add(
                        gson.fromJson(
                                x.getAsJsonObject().get("_source").getAsJsonObject().toString(),
                                HashMap.class
                        )
                )
        );
        return hitsMap;
    }
}
