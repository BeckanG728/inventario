package com.taller.common.error;

import com.taller.config.ErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Component
@Order(40)
public class WebErrorPolicy implements ErrorPolicy {

    @Override
    public boolean supports(Throwable error) {
        return error instanceof MethodArgumentNotValidException
                || error instanceof MethodArgumentTypeMismatchException
                || error instanceof HttpMessageNotReadableException
                || error instanceof MissingServletRequestParameterException
                || error instanceof NoResourceFoundException
                || error instanceof HttpRequestMethodNotSupportedException
                || error instanceof HttpMediaTypeNotSupportedException;
    }

    @Override
    public ErrorDefinition resolve(Throwable error) {
        if (error instanceof MethodArgumentNotValidException ex) {
            if (ex.getBindingResult() == null) {
                return ErrorDefinition.of(ErrorCode.VALIDATION_ERROR);
            }
            List<ErrorResponse.FieldDetail> details = ex.getBindingResult().getFieldErrors().stream()
                    .map(e -> new ErrorResponse.FieldDetail(e.getField(), e.getDefaultMessage()))
                    .toList();
            return new ErrorDefinition(ErrorCode.VALIDATION_ERROR.status(), ErrorCode.VALIDATION_ERROR.name(),
                    ErrorCode.VALIDATION_ERROR.mensaje(), details, new HttpHeaders());
        }
        if (error instanceof MethodArgumentTypeMismatchException ex) {
            List<ErrorResponse.FieldDetail> details = List.of(
                    new ErrorResponse.FieldDetail(ex.getName(), "Formato no válido"));
            return new ErrorDefinition(ErrorCode.VALIDATION_ERROR.status(), ErrorCode.VALIDATION_ERROR.name(),
                    "El parámetro '" + ex.getName() + "' no tiene un formato válido",
                    details, new HttpHeaders());
        }
        if (error instanceof HttpMessageNotReadableException) {
            return ErrorDefinition.of(ErrorCode.VALIDATION_ERROR,
                    "La petición contiene un cuerpo JSON no válido");
        }
        if (error instanceof MissingServletRequestParameterException ex) {
            List<ErrorResponse.FieldDetail> details = List.of(
                    new ErrorResponse.FieldDetail(ex.getParameterName(), "Parámetro obligatorio ausente"));
            return new ErrorDefinition(ErrorCode.VALIDATION_ERROR.status(), ErrorCode.VALIDATION_ERROR.name(),
                    "Falta el parámetro obligatorio '" + ex.getParameterName() + "'",
                    details, new HttpHeaders());
        }
        if (error instanceof NoResourceFoundException) {
            return ErrorDefinition.of(ErrorCode.NOT_FOUND);
        }
        if (error instanceof HttpRequestMethodNotSupportedException ex) {
            return new ErrorDefinition(ErrorCode.METHOD_NOT_ALLOWED.status(), ErrorCode.METHOD_NOT_ALLOWED.name(),
                    ErrorCode.METHOD_NOT_ALLOWED.mensaje(), List.of(), ex.getHeaders());
        }
        HttpMediaTypeNotSupportedException ex = (HttpMediaTypeNotSupportedException) error;
        return new ErrorDefinition(ErrorCode.UNSUPPORTED_MEDIA_TYPE.status(), ErrorCode.UNSUPPORTED_MEDIA_TYPE.name(),
                ErrorCode.UNSUPPORTED_MEDIA_TYPE.mensaje(), List.of(), ex.getHeaders());
    }
}
