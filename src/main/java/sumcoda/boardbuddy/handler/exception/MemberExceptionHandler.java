package sumcoda.boardbuddy.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sumcoda.boardbuddy.dto.common.ApiResponse;
import sumcoda.boardbuddy.exception.member.*;

import static sumcoda.boardbuddy.builder.ResponseBuilder.*;

@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e) {
        return buildFailureResponse(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NicknameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleNicknameAlreadyExistsException(NicknameAlreadyExistsException e) {
        return buildFailureResponse(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MemberSaveException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberSaveException(MemberSaveException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MemberRetrievalException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberRetrievalException(MemberRetrievalException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MemberDeletionFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberDeletionFailureException(MemberDeletionFailureException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberNotFoundException(MemberNotFoundException e) {
        return buildFailureResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MemberNotJoinedGatherArticleException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberNotJoinedGatherArticleException(MemberNotJoinedGatherArticleException e) {
        return buildFailureResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFileFormatException(InvalidFileFormatException e) {
        return buildFailureResponse(e.getMessage(),HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(ProfileImageSaveException.class)
    public ResponseEntity<ApiResponse<Void>> handleProfileImageSaveException(ProfileImageSaveException e) {
        return buildErrorResponse(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
