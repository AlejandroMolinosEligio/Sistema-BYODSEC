package cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class Client300 {

    public static List<String> listaLineas = new ArrayList<String>();

    public static void procentajeLog(){

        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;

        try {
            // Apertura del fichero y creacion de BufferedReader para poder
            // hacer una lectura comoda (disponer del metodo readLine()).
            archivo = new File ("src/log.txt");
            fr = new FileReader (archivo);
            br = new BufferedReader(fr);

            // Lectura del fichero
            String linea;
            int cont = 0;
            int contTotal = 0;
            while((linea=br.readLine())!=null){

                listaLineas.add(linea);
                if(linea.contains("REGISTRADO")){
                    cont +=1;
                }
                contTotal+=1;
            }
            FileWriter fichero = null;
            PrintWriter pw = null;
            try
            {
                fichero = new FileWriter("src/logResumen.txt");
                pw = new PrintWriter(fichero);
                Float div = (float)cont/contTotal*100;
                String linea0 = "############ "+div+"% de intentos de inicio correctos############\n";
                pw.println(linea0);
                for(int i=0;i<listaLineas.size();i++){
                    pw.println(listaLineas.get(i));
                }


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
        catch(Exception e){
            e.printStackTrace();
        }finally{
            // En el finally cerramos el fichero, para asegurarnos
            // que se cierra tanto si todo va bien como si salta 
            // una excepcion.
            try{                    
                if( null != fr ){   
                fr.close();     
                }                  
            }catch (Exception e2){ 
                e2.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {

        for (int i=0;i<300;i++){

        

        String url = "localhost";
        int port = 1712;

        TrustManager[] trustManagers;
        KeyManager[] keyManagers;

        try {

            
    // ============ CERTIFICADOS PROPIOS ===============
            // Cargamos nuestro certificado
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("keys/client/clientKey.jks"), "123456".toCharArray());

            // Preparamos nuestra lista de certificados
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "123456".toCharArray());
            
            // Obtenemos nuestra lista de certificados
            keyManagers = kmf.getKeyManagers();

            
    // ============= CONFIANZA =====================
            // Cargamos nuestros certificados de confianza
            KeyStore trustedStore = KeyStore.getInstance("JKS");
            trustedStore.load(new FileInputStream("keys/client/clientTrustedCerts.jks"), "123456".toCharArray());

            // Preparamos nuestra lista de confianza
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustedStore);
            
            // Obtenemos nuestra lista de lugares seguros
            trustManagers = tmf.getTrustManagers();
            

    // ============= CONEXION ===============
            SSLContext sc = SSLContext.getInstance("TLSV1.3");
            sc.init(keyManagers, trustManagers, null);
            
            SSLSocketFactory ssf = sc.getSocketFactory();
            SSLSocket client = (SSLSocket) ssf.createSocket(url, port);
            
            client.startHandshake();
            
            // Flujos
            DataOutputStream salida = new DataOutputStream(client.getOutputStream());
            DataInputStream entrada = new DataInputStream(client.getInputStream());
            
            // Recibir
            System.out.println("Recibir saludo");
            System.out.println(entrada.readUTF());
            
            // Enviar
            String name = "";
            String pass = "";
            Boolean condicion = Math.random()<0.2;
            if(condicion){
                name = "usuario"+i;
                pass = "pass"+i+1;
            }else{
                name = "usuario0";
                pass = "pass0";
            }
            salida.writeUTF(name+"\n"+pass);

            System.out.println("Respuesta server:");
            String llegada = entrada.readUTF();
            System.out.println(llegada);

            if(llegada.equals("USUARIO REGISTRADO")){

                salida.writeUTF("Mensaje enviado");
                System.out.println("Respuesta server:");
                System.out.println(entrada.readUTF());
    
            }

            client.close();
        } catch (IOException ex) {
            System.err.println("[ERROR-SOCKET]: " + ex);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | KeyManagementException ex) {
            System.out.println("[ERROR-SOCKET]: " + ex);
        }

    }

    procentajeLog();

}

}