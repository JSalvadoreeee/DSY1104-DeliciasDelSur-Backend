package cl.deliciasdelsur.backend.exception;

// Se lanza cuando no existe el recurso pedido (responde 404)
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}