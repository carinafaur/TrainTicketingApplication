package networking;


import java.io.Serializable;

public class Response implements Serializable {
    private final ResponseType type;
    private final Object data;
    private String errorMessage;

    public Response(ResponseType type, Object data) {
        this.type = type;
        this.data = data;
    }
    public ResponseType getType() { return type; }
    public Object getData() { return data; }
    public String getErrorMessage() { return errorMessage; }

    public static Response ok(Object data) {
        return new Response(ResponseType.OK, data);
    }

    public static Response error(String msg) {
        Response res = new Response(ResponseType.ERROR, null);
        res.errorMessage = msg;
        return res;
    }
}
