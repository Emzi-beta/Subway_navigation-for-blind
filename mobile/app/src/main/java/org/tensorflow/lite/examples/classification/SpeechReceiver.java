package org.tensorflow.lite.examples.classification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SpeechReceiver extends BroadcastReceiver {
    String[] startList = {"1번출구", "2번출구", "3번출구", "4번출구"};
    String[] endList = {"1번출구", "2번출구", "3번출구", "4번출구", "노포행개찰구앨리베이터", "여자화장실", "남자화장실", "우대권매표기", "노포행개찰구", "다대포행개찰구"};

    String start = "";
    String end = "";

    int gender = 0; //0여자 1남자
    int mode = 0; //0카드 1우대권

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("navigation-start".equals(action)) {
            Intent i = new Intent(context, ClassifierActivity.class);

            for(String s : startList) {
                if(start.equals(s)) {
                    if(start.contains("출구")) {
                        start += "시작";
                    }

                    for(String e : endList) {
                        if(end.equals(e)) {
                            i.putExtra("start", start);
                            i.putExtra("end", end);
                            context.startActivity(i);
                        }
                    }
                }
            }

            Toast.makeText(context, "제대로 입력해!!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setStartEnd(String start, String end) {
        this.start = start;
        this.end = end;
    }

    public void setGender(String gender) {
        if(gender.equals("여자")) {
            this.gender = 0;
        } else {
            this.gender = 1;
        }
    }

    public void setMode(String mode) {
        if(mode.equals("카드")) {
            this.mode = 0;
        } else {
            this.mode = 1;
        }
    }
}