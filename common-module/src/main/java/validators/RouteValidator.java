package validators;

import domain.Route;
import exceptions.ValidationException;


public class RouteValidator implements IValidator<Route> {
    @Override
    public void validate(Route route) throws ValidationException {
        StringBuilder errors = new StringBuilder();

        String routeStartName=route.getStartStation().getStationName();
        String routeEndName=route.getEndStation().getStationName();
        if(routeStartName==null || routeStartName.isEmpty() || routeEndName==null || routeEndName.isEmpty())
            errors.append("Route start and end stations are mandatory");

        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString());
        }
    }
}
