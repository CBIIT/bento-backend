package gov.nih.nci.bento.error;

import java.util.List;

public class BentoGraphQLException extends Exception{
    BentoGraphqlError bentoGraphqlError;

    public BentoGraphQLException(BentoGraphqlError bentoGraphqlError){
        this.bentoGraphqlError = bentoGraphqlError;
    }

    public BentoGraphQLException(List<String> errors){
        this.bentoGraphqlError = new BentoGraphqlError(errors);
    }

    public BentoGraphqlError getBentoGraphqlError(){
        return bentoGraphqlError;
    }
}
