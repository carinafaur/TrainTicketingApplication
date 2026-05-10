package networking.handlers;

import domain.Train;
import dtos.TrainDTO;
import exceptions.AppException;
import networking.Request;
import networking.Response;
import service.IObserver;
import service.IService;

public class UpdateTrainHandler implements RequestHandler {
    @Override
    public Response handle(Request request, IService server, IObserver observer) {
        TrainDTO dto = (TrainDTO) request.getData();
        Train train = new Train(dto.getTrainNumber(), dto.getCapacity());
        train.setId(dto.getId());
        try {
            server.updateTrain(train);
            return Response.ok(dto);
        } catch (AppException e) {
            return Response.error(e.getMessage());
        }
    }
}
