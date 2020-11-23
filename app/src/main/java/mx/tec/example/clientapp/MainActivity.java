package mx.tec.example.clientapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Button connect, pick, send;
    private TextView messages, fileName, serverMessages;
    private EditText ipInput;
    private Socket socket;
    private DataOutputStream out;
    private BufferedInputStream bis;
    private FileInputStream fis;
    private Integer size;
    private byte [] buffer;
    private String filename;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        pick = (Button) findViewById(R.id.pickFile);
        send = (Button) findViewById(R.id.send);
        connect = (Button) findViewById(R.id.buttonConnect);
        serverMessages = (TextView) findViewById(R.id.serverMessages);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                                /* Creamos el socket con la dirección y elpuerto de comunicación
                                 *  dirección IP --> se obtiene del text input
                                 */
                                ipInput = (EditText) findViewById(R.id.ipServer);
                                String ip = ipInput.getText().toString().trim();
                                serverMessages.append("\nTrying to connect to the server...");
                                socket = new Socket();
                                socket.connect(new InetSocketAddress(ip, 9999), 10000);
                                //Desplegar confirmación al usuario
                                serverMessages.append("\nSuccesful connection!\n");

                                // Creamos el flujo de salida
                                out = new DataOutputStream(socket.getOutputStream());


                                serverMessages.append("\nServer is ready to recieve files, press send...\n");
                                out.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }).start();
            }
        });

        send.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
               myTaskSend mts = new myTaskSend();
               mts.execute();


            }
        });






        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(intent, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


    }

    class myTaskSend extends AsyncTask<Void,Void,Void> {

        protected Void doInBackground(Void... params){

            try {

                out.writeUTF(filename);
                out.writeInt(size);


                out.write(buffer,0,buffer.length);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    public void onActivityResult(int requestCode, int resultCode,Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // Perform operations on the document using its URI.
                try {
                    ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri, "r");

                    fis = new FileInputStream(pfd.getFileDescriptor());
                    serverMessages.append("File selected successfully!!\n");
                    filename = uri.getPath();
                    serverMessages.append("File's URI: " + uri.getPath() +"\n");
                    size = (int)pfd.getStatSize();
                    serverMessages.append("Size of file: " + size + "\n");
                    buffer = new byte[size];
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bis.read(buffer,0,buffer.length);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }



}

