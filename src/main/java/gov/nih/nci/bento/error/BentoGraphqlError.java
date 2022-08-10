package gov.nih.nci.bento.error;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BentoGraphqlError {
    @SerializedName("errors")
    List<Message> errors;
    @SerializedName("data")
    Object data;
    @SerializedName("extensions")
    Object extensions;

    public BentoGraphqlError(List<String> errors){
        this.errors = new ArrayList<>();
        for(String error: errors){
            this.errors.add(new Message(error));
        }
    }

    public List<String> getErrors(){
        ArrayList<String> errorStrings = new ArrayList<>();
        for(Message m: errors){
            errorStrings.add(m.getMessage());
        }
        return errorStrings;
    }

    private class Message{
        @SerializedName("message")
        private String message;

        Message(String message){
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
