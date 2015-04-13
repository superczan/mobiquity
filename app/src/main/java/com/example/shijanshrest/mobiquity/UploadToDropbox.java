package com.example.shijanshrest.mobiquity;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by shijan shrest on 4/11/2015.
 */
public class UploadToDropbox extends AsyncTask <Void,Void,DropboxAPI.Entry> {

    DropboxAPI mDBAPI;
    File imageFile;

    final static private String APP_KEY="dlht4ncldhy9djm";
    final static private String APP_SECRET="pj2r04644rzc01g";

    public UploadToDropbox(DropboxAPI in,File inputFile) {
        mDBAPI=in;
        imageFile=inputFile;

    }


    public DropboxAPI.Entry doInBackground(Void... params) {

        try {
                    FileInputStream fis=new FileInputStream(imageFile);

            try {
                DropboxAPI.Entry e = mDBAPI.createFolder(MainActivity.STORAGE_LOCATION);

            }
            catch (DropboxException e)
            {

            }
                    return mDBAPI.putFile(MainActivity.STORAGE_LOCATION+imageFile.getName(),fis,imageFile.length(),null,null);

            } catch (DropboxException e1) {

            e1.printStackTrace();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(DropboxAPI.Entry entry) {

        Log.i("FILE UPLOADED: ",entry.toString());



    }
}

