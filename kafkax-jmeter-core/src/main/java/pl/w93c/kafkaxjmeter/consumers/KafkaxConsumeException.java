package pl.w93c.kafkaxjmeter.consumers;

public class KafkaxConsumeException extends RuntimeException {
    public KafkaxConsumeException(String message) {
        super(message);
    }

    public KafkaxConsumeException(Throwable cause) {
        super(cause);
    }
}
