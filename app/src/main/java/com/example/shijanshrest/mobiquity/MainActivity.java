package com.example.shijanshrest.mobiquity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends ActionBarActivity {

    final static private String APP_KEY="dlht4ncldhy9djm";
    final static private String APP_SECRET="pj2r04644rzc01g";
    private DropboxAPI<AndroidAuthSession> mDBAPI;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    private static final int MEDIA_TYPE_IMAGE = 1;
    public static final String STORAGE_LOCATION="/mobiquity/";
    private ArrayAdapter<String> listAdapter;

    Uri fileUri;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn=(Button)findViewById(R.id.launch_camera);
        btn.setOnClickListener(new CameraLauncher());
        AppKeyPair appKeys=new AppKeyPair(APP_KEY,APP_SECRET);
        AndroidAuthSession session=new AndroidAuthSession(appKeys);
        mDBAPI=new DropboxAPI<AndroidAuthSession>(session);


        SharedPreferences mem= PreferenceManager.getDefaultSharedPreferences(this);
        String accessToken=mem.getString("ACCESS_TOKEN","");

        if (accessToken.length()>0)
        {
           mDBAPI.getSession().setOAuth2AccessToken(accessToken);
        }
        else
            mDBAPI.getSession().startOAuth2Authentication(MainActivity.this);

        listAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,new ArrayList<String>());
        listAdapter.add("Take Picture to update this list view");

        ((ListView)findViewById(R.id.pictureList)).setAdapter(listAdapter);


    }

    protected void onResume()
    {
        super.onResume();

        if (mDBAPI.getSession().authenticationSuccessful())
        {
            try{
                mDBAPI.getSession().finishAuthentication();
                String accessToken=mDBAPI.getSession().getOAuth2AccessToken();
                SharedPreferences.Editor ed=PreferenceManager.getDefaultSharedPreferences(this).edit();
                ed.putString("ACCESS_TOKEN",accessToken);
                ed.commit();
                //(new UploadToDropbox(mDBAPI)).execute();


            }catch(IllegalStateException e)
            {
                Log.i("ERROR", "ERRRO");
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void updateListUI(DropboxAPI.Entry listOfFiles)
    {
        listAdapter.clear();

        for (DropboxAPI.Entry e:listOfFiles.contents)
        {
            Log.i("FILE: ", e.fileName());
            listAdapter.add(e.fileName());

        }

        listAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1)
        {
            try {

                Log.i("FILE_PATH",fileUri.getPath());
                File f=new File(fileUri.getPath());


                (new UploadToDropbox(mDBAPI,f)).execute();

                Log.d("camera_feedback", "STore complete");

                (new ListAllFiles(mDBAPI)).execute();

            }
            catch(NullPointerException e)
            {
                e.printStackTrace();
            }


        }
    }


    private static File getOutputMediaFile(int type)
    {
        File directory=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"mobiquityApp");

        if (!directory.exists())
        {
            if (!directory.mkdirs())
            {
                Log.i("file_path", "File craetion error!");
                return null;
            }
        }

        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        if (type==MEDIA_TYPE_IMAGE)
        {
            mediaFile=new File(directory.getPath()+File.separator+"IMG_"+timeStamp+".jpg");

        }
        else mediaFile=null;

        return mediaFile;
    }

    private static Uri getMediaURI(int mediaType)
    {
        return Uri.fromFile(getOutputMediaFile(mediaType));

    }


    public class CameraLauncher implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri=getMediaURI(MEDIA_TYPE_IMAGE);


            intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);

            ((Activity) v.getContext()).startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

        }



    }


    /**
     * Created by shijan shrest on 4/13/2015.
     */
    private class ListAllFiles extends AsyncTask<Void,Void,DropboxAPI.Entry> {

        DropboxAPI mAPI;


        public ListAllFiles (DropboxAPI mAPIin)
        {
            mAPI=mAPIin;


        }

        @Override
        protected DropboxAPI.Entry doInBackground(Void... params) {
            DropboxAPI.Entry listOfFiles;

            try {
                listOfFiles= mAPI.metadata(MainActivity.STORAGE_LOCATION,0,null,true,null);
                return listOfFiles;

            } catch (DropboxException e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(DropboxAPI.Entry listOfFiles)
        {
                updateListUI(listOfFiles);

        }
    }

}
