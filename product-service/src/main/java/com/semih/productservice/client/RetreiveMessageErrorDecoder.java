package com.semih.productservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.semih.common.dto.response.ApiError;
import com.semih.productservice.exception.NotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class RetreiveMessageErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String s, Response response) {
        ApiError message = null;
        try(InputStream body = response.body().asInputStream()){
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            message = mapper.readValue(body, ApiError.class);
        }catch(IOException e){
            return new Exception(e.getMessage());
        }
        switch(response.status()){
            case 404:
                throw new NotFoundException(message);
            default:
                return errorDecoder.decode(s,response);
        }
    }
}
