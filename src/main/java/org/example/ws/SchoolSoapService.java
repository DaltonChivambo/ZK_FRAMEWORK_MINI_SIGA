package org.example.ws;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService
public interface SchoolSoapService {
    @WebMethod
    String listarNotasDoEstudante(int studentId);
}
