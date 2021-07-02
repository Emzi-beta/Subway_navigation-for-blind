package org.tensorflow.lite.examples.classification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class Speech {
    private STT stt;
    private TTS tts;
    private Context mContext;

    Speech(Context mConext) {
        this.mContext = mConext;
        stt = new STT(mContext);
        tts = new TTS(mContext);
    }

    public void say(String text) {
        tts.startTTS(text);
    }

    public void litsen() {
        stt.startSTT();
    }

    public void endSpeech() {
        this.stt.endSTT();
        this.tts.endTTS();
    }

    private class STT {
        final String[] SUBWAY_LINE_4_KEYWORDS = {"장전", "부산대", "구서"};
        Context mContext;
        Intent sttIntent;
        SpeechRecognizer mRecognizer;
        TypoEdit typoEdit = new TypoEdit();
        String keyword = "";
        TTS tts;

        STT(Context mContext) {
            this.mContext = mContext;
            sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            sttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.getPackageName());
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");//한국어 사용

            tts = new TTS(mContext);
        }

        void startSTT() {
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(sttIntent);
        }

        void endSTT() {
            if (mRecognizer != null) {
                mRecognizer.destroy();
                mRecognizer.cancel();
                mRecognizer = null;
            }

            tts.endTTS();
        }

        String getKeyword() {
            return keyword;
        }

        private RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                ToastMsg("음성인식 시작");
                tts.startTTS("말해");
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {
                String error;

                switch (i) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        error = "ERROR_AUDIO";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        error = "RROR_CLIENT";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        error = "ERROR_INSUFFICIENT_PERMISSIONS";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        error = "ERROR_NETWORK";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        error = "ERROR_NO_MATCH";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        error = "ERROR_RECOGNIZER_BUSY";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        error = "ERROR_SERVER";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        error = "ERROR_SPEECH_TIMEOUT";
                        break;
                    default:
                        error = "UNKOWN ERROR";
                        break;
                }

                ToastMsg("error : " + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                String resultStr = "";

                for (int i = 0; i < matches.size(); i++) {
                    resultStr += matches.get(i);
                }

                if (resultStr.length() < 1) return;
                resultStr = resultStr.replace(" ", "");

                ToastMsg("음성인식 성공");
                voiceOrderCheck(resultStr);
            }


            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        };

        //입력된 음성 메세지 확인 후 동작 처리
        private void voiceOrderCheck(String speech) {

            String subway_line_4_keyword = typoEdit.typo_edit(speech, SUBWAY_LINE_4_KEYWORDS);

            if (subway_line_4_keyword.equals("ERROR")) {
                tts.startTTS("다시 말해.");
            } else {
                keyword = subway_line_4_keyword;
                tts.startTTS(subway_line_4_keyword + "역으로 안내하겠습니다.");
                mContext.sendBroadcast(new Intent("navigation-start"));
            }
        }

        private void ToastMsg(final String msg) {
            final String message = msg;
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }

        private class TypoEdit {
            final char[] ChoSung = {0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145, 0x3146, 0x3147, 0x3148, 0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};
            // ㅏ      ㅐ      ㅑ      ㅒ      ㅓ      ㅔ      ㅕ      ㅖ      ㅗ      ㅘ      ㅙ      ㅚ      ㅛ      ㅜ      ㅝ      ㅞ      ㅟ      ㅠ      ㅡ      ㅢ      ㅣ
            final char[] JwungSung = {0x314f, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158, 0x3159, 0x315a, 0x315b, 0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162, 0x3163};
            // ㄱ      ㄲ      ㄳ      ㄴ      ㄵ      ㄶ      ㄷ      ㄹ      ㄺ      ㄻ      ㄼ      ㄽ      ㄾ      ㄿ      ㅀ      ㅁ      ㅂ      ㅄ      ㅅ      ㅆ      ㅇ      ㅈ      ㅊ      ㅋ      ㅌ      ㅍ      ㅎ
            final char[] JongSung = {0, 0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3139, 0x313a, 0x313b, 0x313c, 0x313d, 0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145, 0x3146, 0x3147, 0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};

            private double levenshtein(String s1, String s2) {
                String longer = s1, shorter = s2;

                //짧은 문자열은 s2, 긴 문자열은 s1으로 재설정
                if (s1.length() < s2.length()) {
                    longer = s2;
                    shorter = s1;
                }

                int longerLength = longer.length();

        /*
        if (longerLength == 0) return 1.0;
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
        */

                double[] costs = new double[s2.length() + 1];
                int c = 30;

                for (int i = 0; i <= s1.length(); i++) {
                    double lastValue = i;
                    for (int j = 0; j <= s2.length(); j++) {
                        if (i == 0) {
                            costs[j] = j;
                        } else {
                            if (j > 0) {
                                double newValue = costs[j - 1];

                                if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                                    newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + c;
                                }

                                costs[j - 1] = lastValue;
                                lastValue = newValue;
                            }
                        }
                    }

                    if (i > 0) costs[s2.length()] = lastValue;
                }

                //마지막 인덱스의 costs elements 반환
                return costs[s2.length()];
            }

            private double jamo_levenshtein(String s1, String s2) {
                String longer = s1, shorter = s2;

                //짧은 문자열은 s2, 긴 문자열은 s1으로 재설정
                if (s1.length() < s2.length()) {
                    longer = s2;
                    shorter = s1;
                }

                int longerLength = longer.length();

        /*
        if (longerLength == 0) return 1.0;
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
        */

                double[] costs = new double[s2.length() + 1];

                for (int i = 0; i <= s1.length(); i++) {
                    double lastValue = i;
                    for (int j = 0; j <= s2.length(); j++) {
                        if (i == 0) {
                            costs[j] = j;
                        } else {
                            if (j > 0) {
                                double newValue = costs[j - 1];

                                if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                                    newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + substitution_cost(s1.charAt(i - 1), s2.charAt(j - 1));
                                }

                                costs[j - 1] = lastValue;
                                lastValue = newValue;
                            }
                        }
                    }

                    if (i > 0) costs[s2.length()] = lastValue;
                }

                //마지막 인덱스의 costs elements 반환
                return costs[s2.length()];
            }

            private double substitution_cost(char c1, char c2) {
                if (c1 == c2) {
                    return 0;   //글자가 같으면 cost 0
                }

                return levenshtein(decompose(c1), decompose(c2)) / 3;      //다르면 자모 분리해서 levenshtein / 3 을 cost로
            }

            private String decompose(char ch) {
                int a, b, c; // 초성/중성/종성
                String result = "";

                if (ch >= 0xAC00 && ch <= 0xD7A3) { //한글에 속한 글자면 분해
                    c = ch - 0xAC00;
                    a = c / (21 * 28);
                    c = c % (21 * 28);
                    b = c / 28;
                    c = c % 28;

                    result = result + ChoSung[a] + JwungSung[b];

                    if (c != 0) result = result + JongSung[c]; // c가 0이 아니면, 즉 받침이 있으면
                    else result = result + ' ';
                } else {
                    result = result + ch;
                }

                return result;
            }

            public String typo_edit(String speech_text, String[] Dictionary) {
                String test = speech_text;

                int start_count = 0;
                int end_count = 1;

                //코스트 비교 변수들...
                double max = 0;
                double min = 999999;
                double count = 0;
                double total = 0;
                double avg = 0;

                String test_word = "";
                String edit_word = "";
                double cost = 0;

                for (int i = 0; i < Dictionary.length; i++) {
                    start_count = 0;
                    end_count = Dictionary[i].length();

                    if (end_count >= test.length() || end_count <= start_count) break;

                    for (int j = 0; j < test.length(); j++) {
                        String temp = test.substring(start_count, end_count);
                        cost = jamo_levenshtein(temp, Dictionary[i]);

                        count++;
                        total += cost;

                        if (max <= cost) {
                            max = cost;
                        }

                        if (min >= cost) {
                            test_word = temp;
                            edit_word = Dictionary[i];

                            min = cost;
                        }

                        if (end_count >= test.length()) {
                            break;
                        } else {
                            start_count++;
                            end_count++;
                        }
                    }
                }

                double m = max / 5;
                if (m > min) {
                    System.out.println(m + ", " + min + ", " + max);
                    return edit_word;
                } else {
                    System.out.println(m + ", " + min + ", " + max);
                    return "ERROR";
                }

            }
        }
    }

    private class TTS implements TextToSpeech.OnInitListener {
        Context mContext;
        TextToSpeech tts;

        TTS(Context mContext) {
            this.mContext = mContext;

            tts = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        tts.setLanguage(Locale.KOREAN);
                    }
                }
            });
        }

        @Override
        public void onInit(int i) {
            if (i == TextToSpeech.SUCCESS) {
                tts.setPitch(1.0f);//목소리 톤1.0
                tts.setSpeechRate(1.0f);//목소리 속도
            } else {
                ToastMsg("tts 초기화 실패");
            }
        }

        //음성 메세지 출력용
        void startTTS(String OutMsg) {
            if (OutMsg.length() < 1) return;

            if (!tts.isSpeaking()) {
                tts.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null);
            }
        }

        void endTTS() {
            if (tts != null) {
                tts.stop();
                tts.shutdown();
                tts = null;
            }
        }

        private void ToastMsg(final String msg) {
            final String message = msg;
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

}