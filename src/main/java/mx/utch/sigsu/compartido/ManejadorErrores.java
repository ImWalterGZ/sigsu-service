package mx.utch.sigsu.compartido;

import mx.utch.sigsu.compartido.dto.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ManejadorErrores {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> negocio(BusinessException error) {
        return ResponseEntity.status(error.respuesta().status()).body(error.respuesta());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> validar(MethodArgumentNotValidException error) {
        return ResponseEntity.unprocessableEntity()
                .body(
                        new ErrorResponseDto(
                                422,
                                "datos inválidos",
                                null,
                                error.getBindingResult().getFieldErrors().stream()
                                        .map(
                                                c ->
                                                        new ErrorCampo(
                                                                c.getField(),
                                                                c.getDefaultMessage()))
                                        .toList()));
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponseDto> formato(Exception error) {
        return negocio(new BusinessException(422, "formato de petición inválido"));
    }

    @ExceptionHandler({
        ObjectOptimisticLockingFailureException.class,
        DataIntegrityViolationException.class
    })
    public ResponseEntity<ErrorResponseDto> conflicto(Exception error) {
        return negocio(new BusinessException(409, "conflicto de versión o restricción de datos"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> tamano(Exception error) {
        return negocio(new BusinessException(413, "el archivo supera 5 MB"));
    }
}
