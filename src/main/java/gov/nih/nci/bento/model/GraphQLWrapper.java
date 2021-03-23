package gov.nih.nci.bento.model;

import graphql.language.*;
import graphql.language.OperationDefinition.Operation;
import graphql.parser.Parser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraphQLWrapper {

    private static final Logger logger = LogManager.getLogger(GraphQLWrapper.class);

    private final String rawRequest;
    private final OperationDefinition request;
    private final String variables;
    private final Operation operation;

    //Query Maps
    private final HashMap<String, Field> databaseQueriesMap;
    private final HashMap<String, Field> serviceQueriesMap;

    //Variable Maps
    private final HashMap<String, String> graphQLVariablesMap = new HashMap<>();
    private final HashMap<String, String> serviceQueryVariablesMap = new HashMap<>();
    private final HashMap<String, String> databaseQueryVariablesMap = new HashMap<>();

    //Constructors
    public GraphQLWrapper(HttpEntity input){
        this(input.getBody().toString(), null);
    }

    public GraphQLWrapper(HttpEntity input, List<String> listOfServiceQueries){
        this(input.getBody().toString(), listOfServiceQueries);
    }

    public GraphQLWrapper(String input){
        this(input, null);
    }

    public GraphQLWrapper(String input, List<String> listOfServiceQueries){
        rawRequest = input;
        JSONObject inputJSON = new JSONObject(input);
        Document queryDoc = Parser.parse(inputJSON.get("query").toString());
        request = (OperationDefinition) queryDoc.getDefinitions().get(0);
        operation = request.getOperation();

        //Parse graphQL header variables
        VariableDefinition[] graphQLVariables = request.getVariableDefinitions().toArray(new VariableDefinition[0]);
        for(VariableDefinition variableDefinition: graphQLVariables){
            Type type = variableDefinition.getType();
            TypeName typeName;
            if(type instanceof ListType){
                ListType listType = (ListType) type;
                typeName = (TypeName) listType.getType();
                String typeDef = "[" + typeName.getName() + "]";
                graphQLVariablesMap.put(variableDefinition.getName(), typeDef);
            }
            else{
                typeName = (TypeName) type;
                graphQLVariablesMap.put(variableDefinition.getName(), typeName.getName());
            }
        }

        //Parse queries
        Field[] queries = request.getSelectionSet().getSelections().toArray(new Field[0]);
        databaseQueriesMap = new HashMap<>();
        serviceQueriesMap = new HashMap<>();
        if (listOfServiceQueries == null){
            listOfServiceQueries = new ArrayList<>();
        }
        for (Field query: queries){
            String name = query.getName();
            if (listOfServiceQueries.contains(name)){
                addToMaps(query, serviceQueriesMap, serviceQueryVariablesMap);
            }
            else{
                addToMaps(query, databaseQueriesMap, databaseQueryVariablesMap);
            }
        }

        //Parse request variables
        if(inputJSON.has("variables")){
            variables = inputJSON.get("variables").toString();
        }
        else{
            variables = "";
        }
    }

    public String getRawRequest() {
        return rawRequest;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getDatabaseRequest(){
        if (databaseQueriesMap.size() < 1){
            return null;
        }
        String graphQL = "";
        graphQL += operation.name().toLowerCase();
        if(databaseQueryVariablesMap.size() > 0){
            graphQL += "(";
            ArrayList<String> variables = new ArrayList<>();
            for(String key: databaseQueryVariablesMap.values()){
                String variableName = "$" + key;
                variableName += ":" + graphQLVariablesMap.get(key);
                variables.add(variableName);
            }
            graphQL += String.join(",", variables);
            graphQL += ")";
        }
        graphQL += "{";
        for(String key:databaseQueriesMap.keySet()){
            Field query = databaseQueriesMap.get(key);
            graphQL += query.getName();
            List<Argument> args = query.getArguments();
            if(args.size() > 0){
                graphQL += "(";
                ArrayList<String> variables = new ArrayList<>();
                for(Argument arg: args){
                    String name = arg.getName();
                    Value value = arg.getValue();
                    if(value instanceof VariableReference){
                        String varName = "$" + databaseQueryVariablesMap.get(name);
                        variables.add(name + ":" + varName);
                    }
                    else if(value instanceof IntValue){
                        variables.add(name+":"+((IntValue) value).getValue());
                    }
                    else if(value instanceof StringValue){
                        variables.add(name+":"+((StringValue) value).getValue());
                    }
                    else if(value instanceof BooleanValue){
                        variables.add(name+":"+((BooleanValue) value).isValue());
                    }
                    else if(value instanceof NullValue){
                        variables.add(name+":null");
                    }
                    else if(value instanceof FloatValue){
                        variables.add(name+":"+((FloatValue) value).getValue());
                    }
                    else if(value instanceof ArrayValue){
                        variables.add(name+":"+((ArrayValue) value).getValues().toString());
                        logger.warn(String.format("The type of the variable \"%s\" is an explicit array, " +
                                "this functionality has not yet been implemented", name));
                    }
                    else{
                        logger.warn(String.format("The type of the variable \"%s\" could not be determined and" +
                                " was omitted from the request", name));
                    }
                }
                graphQL += String.join(",", variables);
                graphQL += ")";
            }

            try{
                SelectionSet selectionSet = query.getSelectionSet();
                List<Selection> selectionList = selectionSet.getSelections();
                graphQL = appendSelections(graphQL, selectionList);
            }
            catch(NullPointerException e){
                graphQL += " ";
            }
        }
        graphQL += "}";

        JSONObject databaseRequest = new JSONObject();
        databaseRequest.put("query", graphQL);
        databaseRequest.put("variables", variables);
        return databaseRequest.toString();
    }

    public HashMap<String, HashMap<String, String[]>> getFilterParameters(){
        if(!hasServiceQueries()){
            return null;
        }
        HashMap<String, HashMap<String, String[]>> filterParametersMap = new HashMap<String, HashMap<String, String[]>>();
        JSONObject variablesObject = new JSONObject(variables);
        for(String queryName: serviceQueriesMap.keySet()){
            HashMap<String, String[]> filterParameters = new HashMap<>();
            Field query = serviceQueriesMap.get(queryName);
            for(Argument arg: query.getArguments()){
                Node node = arg.getValue();
                if(node instanceof VariableReference){
                    VariableReference variableReference = (VariableReference) node;
                    String variableName = variableReference.getName();
                    String value = variablesObject.get(variableName).toString();
                    value = value.replaceAll("\\[\"|\"\\]|^\\[|\\]$", "");
                    if(!value.equals("")){
                        String[] filters = value.split("\"\\s*,\\s*\"");
                        filterParameters.put(arg.getName(), filters);
                    }
                    filterParametersMap.put(queryName, filterParameters);
                }
            }
        }
        return filterParametersMap;
    }

    public boolean hasServiceQueries(){
        return serviceQueriesMap.size() > 0;
    }

    private String appendSelections(String graphQL, List<Selection> selections){
        graphQL += "{";
        for(Selection selection : selections){
            Field field = (Field) selection;
            graphQL += field.getName();
            try{
                List<Selection> selectionList = field.getSelectionSet().getSelections();
                if(!selectionList.isEmpty()){
                    graphQL = appendSelections(graphQL, selectionList);
                }
            }
            catch (NullPointerException e){
                //No action required
            }
            graphQL += " ";
        }
        return graphQL + "}";
    }

    private void addToMaps(Field query, HashMap<String, Field> queryMap, HashMap<String, String> queryVariablesMap){
        queryMap.put(query.getName(), query);
        for(Argument arg: query.getArguments()){
            Node node = arg.getValue();
            if(node instanceof VariableReference){
                VariableReference variableReference = (VariableReference) node;
                queryVariablesMap.put(arg.getName(), variableReference.getName());
            }
        }
    }




}
