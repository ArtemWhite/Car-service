package dealerShipOrder.domain.models.expection;

public class DomainValidationException extends RuntimeException
{
    public DomainValidationException(String message)
    {
        super(message);
    }
}
