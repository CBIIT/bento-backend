package gov.nih.nci.bento.error;

import com.google.gson.annotations.SerializedName;

public class ApiErrorWrapper {

    @SerializedName("apierror")
    private ApiError apiError;

    public ApiErrorWrapper(ApiError apiError){
        this.apiError = apiError;
    }
}
