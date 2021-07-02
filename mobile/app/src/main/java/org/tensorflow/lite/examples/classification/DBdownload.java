package org.tensorflow.lite.examples.classification;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/*
DB 다운로드
메뉴화면
 */

class DBdownload extends AsyncTask<String, String, String> {

    private ProgressDialog mDlg;
    private Context context;

    public DBdownload(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        mDlg = new ProgressDialog(context);
        mDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDlg.setMessage("테스트_장전역 DB 다운로드...");
        mDlg.show();

        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            Thread.sleep(100);

            //상위 폴더 없으면 만들기
            String DB_PATH = params[1];
            String DB_NAME = params[2];
            File dir = new File(DB_PATH);
            if(!dir.exists()) dir.mkdirs();

            File target = new File(dir + DB_NAME);
            target.createNewFile();


            //db 다운로드
            URL url = new URL(params[0]);
            URLConnection connection = url.openConnection();

            InputStream is = connection.getInputStream();
            FileOutputStream fos = new FileOutputStream(target);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            byte[] buf = new byte[1024];
            int count;
            while ((count = is.read(buf)) > 0) {
                bos.write(buf, 0, count);
            }

            bos.close();
            fos.close();
            is.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPostExecute(String unused) {
        mDlg.dismiss();
    }
}
