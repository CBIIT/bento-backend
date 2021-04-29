package gov.nih.nci.bento.model;

import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.Document;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ListType;
import graphql.language.NullValue;
import graphql.language.ObjectValue;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.language.Value;
import graphql.language.VariableDefinition;
import graphql.language.VariableReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    private String queryName;
    private String query = "";
    private Map<String, Object> variables;

    public Request(Field field, Document document, Map<String, Object> variablesMap){
        //Parse info from document
        OperationDefinition operationDefinition = (OperationDefinition) document.getDefinitions().get(0);
        List<VariableDefinition> variableDefinitions = operationDefinition.getVariableDefinitions();
        queryName = field.getName();
        List<Argument> arguments = field.getArguments();
        variables = new HashMap<>();

        //Generate Query Variables
        List<String> variablesReferencesInQuery = new ArrayList<>();
        List<String> queryVariables = new ArrayList<>();
        for(Argument argument : arguments){
            Value value = argument.getValue();
            String stringValue = parseValueAsString(value);
            queryVariables.add(argument.getName()+": "+stringValue);

            if (value instanceof VariableReference){
                VariableReference variableReference = (VariableReference) value;
                variablesReferencesInQuery.add(variableReference.getName());
                variables.put(variableReference.getName(), variablesMap.get(variableReference.getName()));
            }
        }
        //Generate Request Variables
        List<String> requestVariables = new ArrayList<>();
        for(VariableDefinition variableDefinition : variableDefinitions){
            String name = variableDefinition.getName();
            String definition = "";
            if (variablesReferencesInQuery.contains(name)){
                Type type = variableDefinition.getType();
                if (type instanceof ListType){
                    ListType listType = (ListType) type;
                    type = listType.getType();
                    TypeName typeName = (TypeName) type;
                    definition = "[" + typeName.getName() + "]";
                }
                else{
                    TypeName typeName = (TypeName) type;
                    definition = typeName.getName();
                }
                requestVariables.add("$"+name+": "+definition);
            }
        }

        //Get return fields
        List<String> returnFields = parseReturnFields(field);
        String requestName = operationDefinition.getName();
        if (requestName == null){
            requestName = "defaultName";
        }

        query += operationDefinition.getOperation().toString().toLowerCase() + " " + requestName;
        if (!requestVariables.isEmpty()){
            query += "(";
            query += String.join(",", requestVariables);
            query += ")";
        }
        query += "{ "+queryName;
        if (!queryVariables.isEmpty()){
            query += "(";
            query += String.join(",", queryVariables);
            query += ")";
        }
        query += String.join(" ", returnFields);
        query += "}";
    }

    public String getQueryName(){
        return queryName;
    }

    public String getQueryKey(){
        return query+"::"+variables.toString();
    }

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    private ArrayList<String> parseReturnFields(Field field){
        return parseReturnFields(field, new ArrayList<String>());
    }

    private ArrayList<String> parseReturnFields(Field field, ArrayList<String> returnFields){
        SelectionSet selectionSet = field.getSelectionSet();
        if (selectionSet != null){
            List<Selection> selections = selectionSet.getSelections();
            if (!selections.isEmpty()){
                returnFields.add("{");
                for (Selection selection : selections){
                    Field subField = (Field) selection;
                    returnFields.add(subField.getName());
                    returnFields = parseReturnFields(subField, returnFields);
                }
                returnFields.add("}");
            }
        }
        return returnFields;
    }

    private String parseValueAsString(Value value){
        if (value instanceof VariableReference){
            VariableReference variableReference = (VariableReference) value;
            return "$"+variableReference.getName();
        }
        else if(value instanceof IntValue){
            IntValue intValue = (IntValue) value;
            return intValue.getValue().toString();
        }
        else if(value instanceof ArrayValue){
            ArrayValue arrayValue = (ArrayValue) value;
            List<Value> values = arrayValue.getValues();
            ArrayList<String> valueStrings = new ArrayList<>();
            for (Value element : values){
                valueStrings.add(parseValueAsString(element));
            }
            return "["+String.join(", ", valueStrings.toArray(new String[0]))+"]";
        }
        else if(value instanceof BooleanValue){
            BooleanValue booleanValue = (BooleanValue) value;
            return booleanValue.toString();
        }
        else if(value instanceof EnumValue){
            EnumValue enumValue = (EnumValue) value;
            return enumValue.getName();
        }
        else if(value instanceof FloatValue){
            FloatValue floatValue = (FloatValue) value;
            return floatValue.getValue().toString();
        }
        else if(value instanceof NullValue){
            return null;
        }
        else if(value instanceof ObjectValue){
            ObjectValue objectValue = (ObjectValue) value;
            return objectValue.toString();
        }
        else if(value instanceof StringValue){
            StringValue stringValue = (StringValue) value;
            return "\""+stringValue.getValue()+"\"";
        }
        else{
            return value.toString();
        }
    }
}
