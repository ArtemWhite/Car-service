package dealerShipOrder.domain.models.expection;

public class EntityNotFoundException extends RuntimeException
{
    public EntityNotFoundException(String message)
    {
        super(message);
    }
}
