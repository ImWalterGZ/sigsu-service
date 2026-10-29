package mx.utch.sigsu.seguridad.service;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
public class PeticionRepetible extends HttpServletRequestWrapper {
 private final byte[] cuerpo;
 public PeticionRepetible(HttpServletRequest original) throws IOException { super(original); cuerpo=original.getInputStream().readAllBytes(); }
 public byte[] cuerpo() { return cuerpo; }
 @Override public ServletInputStream getInputStream() {
  ByteArrayInputStream entrada=new ByteArrayInputStream(cuerpo);
  return new ServletInputStream() {
   public int read() { return entrada.read(); }
   public boolean isFinished() { return entrada.available()==0; }
   public boolean isReady() { return true; }
   public void setReadListener(ReadListener escucha) { throw new UnsupportedOperationException("solo lectura síncrona"); }
  };
 }
 @Override public BufferedReader getReader() { return new BufferedReader(new InputStreamReader(getInputStream(),StandardCharsets.UTF_8)); }
}