package subway.exception;

public class InvalidDistanceException extends RuntimeException {
    public InvalidDistanceException() {
        super("거리가 올바르지 않습니다.");
    }

}
