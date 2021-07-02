package org.tensorflow.lite.examples.classification;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class NaviReceiver extends BroadcastReceiver {
    public Navigate navi;
    Speech speech;
    TextView directionTxt;
    String n;


    NaviReceiver (Context context, TextView directionTxt) {
        speech = new Speech(context);
        navi = new Navigate(context);
        this.directionTxt = directionTxt;
        navi.createMap();
    }

    NaviReceiver () {

        navi.createMap();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if("start".equals(action)) {

            Intent i =  ((Activity) context).getIntent(); /*데이터 수신*/

            String start = i.getExtras().getString("start");
            String end = i.getExtras().getString("end");

            navi.setRoute(start, end);

            Log.v("BORA", "경로 : " + navi.getRouteStr());
        }

        else if ("next".equals(action)) {
            n = navi.nextDirection();
            directionTxt.setText(n);





            Toast.makeText(context, n+"으로 가세요^^", Toast.LENGTH_SHORT).show();
            Log.d("Bora", n);
            speech.say(n);


            if(n.equals("도착")) {
                Toast.makeText(context, n, Toast.LENGTH_SHORT).show();
                ((Activity)context).finish();
            }
        }
    }

    public String getN() {return n;}
}
