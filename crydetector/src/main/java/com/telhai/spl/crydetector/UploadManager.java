package com.telhai.spl.crydetector;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Dima Ruinskiy on 22-11-17.
 */

public class UploadManager
{
    /* upload error codes */
    static final int UP_UNKNOWN = 0;
    static final int UP_BADURL = -1;
    static final int UP_TIMEOUT = 2;

    /**
     * @param sourceFile The file to upload.
     * @return int          The server's response code.
     * @brief Uploads a file to the server. Taken from BabyService.java.
     */
    public static int uploadFile(File sourceFile, String serverUrl)
    {
        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        String sourceFileUri = sourceFile.getAbsolutePath();
        int serverResponseCode = 0;

        if(!sourceFile.isFile())
        {
            Log.e("uploadFile", "Source File does not exist :"
                    + sourceFileUri);
            return 0;
        } else
        {
            try
            {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(serverUrl);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("fileToUpload", sourceFileUri);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\";filename=\""
                        + sourceFileUri + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while(bytesRead > 0)
                {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "Uploaded " + sourceFileUri + ". HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch(MalformedURLException ex)
            {
                ex.printStackTrace();
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                serverResponseCode = UP_BADURL;
            } catch(SocketTimeoutException ex)
            {
                Log.e("Upload file to server", "timeout");
                serverResponseCode = UP_TIMEOUT;
            } catch(Exception e)
            {
                e.printStackTrace();
                Log.e("Upload file to server", "Exception : "
                        + e.getMessage(), e);
                serverResponseCode = UP_UNKNOWN;
            }
            return serverResponseCode;
        } // End else block
    }

    public static void uploadAllFiles(final Context context,
                                      String serverUrl,
                                      final File[] files,
                                      boolean deleteIfUploaded,
                                      boolean silent)
    {
        int responseCode;
        int successfully = 0;
        boolean doNext = true;
        String toastMessage = "";
        for(File file : files)
        {
            responseCode = uploadFile(file, serverUrl);
            switch(responseCode)
            {
                case HttpURLConnection.HTTP_OK:
                    ++successfully;
                    if(deleteIfUploaded)
                    {
                        file.delete();
                    }
                    break;
                case UP_BADURL:
                    toastMessage = "Bad server URL. ";
                    doNext = false;
                    break;
                case UP_TIMEOUT:
                    toastMessage = "Connection timed out. ";
                    doNext = false;
                    break;
                case UP_UNKNOWN:
                    toastMessage = "Unknown error. ";
                    doNext = false;
                    break;
            }
            if(!doNext)
            {
                break;
            }
        }
        final int totalSuccessfully = successfully;
        final String endMessage = toastMessage;
        if(!silent)
        {
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(context,
                            (endMessage + totalSuccessfully + " out of " + files.length + " files uploaded."),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
