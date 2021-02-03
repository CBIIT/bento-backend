package gov.nih.nci.bento.service;

import com.google.gson.annotations.SerializedName;
import org.neo4j.graphql.Cypher;

import java.util.Map;

/**
 * Converts a Cypher object into a serializable object used when sending a request to the Neo4j endpoint
 */
public class Neo4jRequest {

    @SerializedName("statements")
    private Statement[] statements;

    /**
     * Class constructor
     * @param cypher The cypher object used as the data source
     */
    public Neo4jRequest(Cypher cypher){
        //Extract the query and the parameters
        String query = cypher.getQuery();
        Map<String, Object> params = cypher.getParams();

        //Replace the variables in the query with their true values
        if (params != null){
            for (String key : params.keySet()) {
                query = query.replace('$' + key, "\"" + params.get(key).toString() + "\"");
            }
        }

        //Store the query in the Neo4j API required format
        this.statements = new Statement[]{new Statement(query)};
    }

    /**
     * @return The cypher query with parameters removed
     */
    public String getQuery(){
        return statements[0].query;
    }

    /**
     * Sub-class required for formatting to meet the Neo4j API expected format
     */
    private class Statement {

        @SerializedName("statement")
        private String query;

        public Statement(String query){
            this.query = query;
        }
    }
}
