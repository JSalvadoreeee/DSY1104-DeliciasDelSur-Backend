package cl.deliciasdelsur.backend.exception;

// Se lanza cuando se rompe una regla de negocio, como stock insuficiente (responde 400)
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}