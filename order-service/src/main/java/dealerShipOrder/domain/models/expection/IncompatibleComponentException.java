package dealerShipOrder.domain.models.expection;

public class IncompatibleComponentException extends RuntimeException
{
    public IncompatibleComponentException(String message)
    {
        super(message);
    }
}
