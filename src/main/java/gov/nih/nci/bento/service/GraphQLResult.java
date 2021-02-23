package gov.nih.nci.bento.service;

public class GraphQLResult {
    public String getQueryName() {
        return queryName;
    }

    public Object getValues() {
        return values;
    }

    private final String queryName;
    private final Object values;
    public GraphQLResult(String name, Object values) {
        this.queryName = name;
        this.values = values;
    }
}
