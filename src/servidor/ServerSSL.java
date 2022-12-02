package servidor;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.time.*;

public class ServerSSL {

    public static void guardarLog(String entrada){
        FileWriter fichero = null;
        PrintWriter pw = null;
        try
        {
            fichero = new FileWriter("src/log.txt",true);
            pw = new PrintWriter(fichero);
            
            pw.println(LocalDateTime.now()+" --- "+entrada);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           // Nuevamente aprovechamos el finally para 
           // asegurarnos que se cierra el fichero.
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }

    }


    public static void main(String[] args) {

        int port = 1712;

        TrustManager[] trustManagers;
        KeyManager[] keyManagers;

        try {

            
    // ============ CERTIFICADOS PROPIOS ===============
            // Cargamos nuestro certificado
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("keys/server/serverKey.jks"), "123456".toCharArray());

            // Preparamos nuestra lista de certificados
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "123456".toCharArray());
            
            // Obtenemos nuestra lista de certificados
            keyManagers = kmf.getKeyManagers();

            
    // ============= CONFIANZA =====================
            // Cargamos nuestros certificados de confianza
            KeyStore trustedStore = KeyStore.getInstance("JKS");
            trustedStore.load(new FileInputStream("keys/server/serverTrustedCerts.jks"), "123456".toCharArray());

            // Preparamos nuestra lista de confianza
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustedStore);
            
            // Obtenemos nuestra lista de lugares seguros
            trustManagers = tmf.getTrustManagers();

            
    // =============== CONEXION =================
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(keyManagers, trustManagers, null);

            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            ServerSocket serverSocket = ssf.createServerSocket(port);
            
            while(true){

                System.out.println("Esperando");
                Socket conexion = serverSocket.accept();
                System.out.println("Conexión establecida");
    
                
                
                DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
                DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                
                // Enviar
                System.out.println("Enviar saludo");
                salida.writeUTF("Hola cliente");
                salida.flush();

                // Recibir
                String input = entrada.readUTF();
                System.out.println(input);

                /*
                 
                 String[] parts = input.split("\n");
                 String user = parts[0];
                 String passwd = parts[1];
                 
                 boolean datosLoginEncontrados = false;
                
                 
                 BufferedReader r = new BufferedReader(new FileReader("src/servidor/Claves.txt"));
                 String linea = r.readLine();
                 
                 while (linea != null && datosLoginEncontrados == false){
                     
                     String[] datosLogin = linea.split("--");
                     
                     if (datosLogin[0].equals(user) && datosLogin[1].equals(passwd))
                     datosLoginEncontrados = true;
                     else
                     linea = r.readLine();
                     
                    }
                    String msg = "";
                    if(datosLoginEncontrados){
                        msg = "USUARIO REGISTRADO";
                        salida.writeUTF(msg);
                        salida.flush();
                }else{
                    msg = "USUARIO O PASSWORD INCORRECTOS";
                    salida.writeUTF(msg);
                    salida.flush();
                }
                
                guardarLog(msg);
                
                if(datosLoginEncontrados){
                    
                    input = entrada.readUTF();
                    System.out.println("Enviar confirmación");
                    salida.writeUTF("Mensaje recibido");
                    salida.flush();
                }
                
                */
                conexion.close();
            }
            
            
        } catch (IOException ex) {
            System.err.println("[ERROR-SOCKET]: " + ex);
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException | KeyManagementException ex) {
            System.err.println("[ERROR]: " + ex);
        }

    }

}
