package com.artinoise.recorder;

import android.content.Context;
import android.util.Log;

import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

//sherlockmidi
/*
import java.io.IOException;
import java.util.Arrays;

import cn.sherlock.com.sun.media.sound.SF2Instrument;
import cn.sherlock.com.sun.media.sound.SF2Soundbank;
import cn.sherlock.com.sun.media.sound.SoftSynthesizer;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.Receiver;
import jp.kshoji.javax.sound.midi.ShortMessage;
*/



public class MidiBridge
{
    private static String TAG = MidiBridge.class.getName().toString();

    public static int SONIVOX = 0;
    public static int KYO = 1;
    public static int FLUIDSYNTH = 2;

    private Context context;
    private WakeLock wakeLock;

    //SonyVox
    private DriverBase engine;
    //Kyo SherlockMidi
   // private SoftSynthesizer kyoSynth;

    public MidiBridge(Context context){
        this.context = context;
    }

    public void init(Object listener){


        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();

        if (getEngineIdx() == MidiBridge.SONIVOX) {
            //setSonivoxEngine((MidiDriver.OnMidiStartListener)listener);
            Log.w("MidiBridge","Sonivox engine removed.");
        } else if (getEngineIdx() == MidiBridge.KYO){
            //setKyoEngine();
            Log.w("MidiBridge","Kyo engine removed.");
        } else if (getEngineIdx() == MidiBridge.FLUIDSYNTH){
            setFluidSynthEngine();
        }
    }

    public int getEngineIdx(){
        //return 0; //sonivox
        return 2; //fluidsynth
        /*
        SharedPreferences preferences = context.getSharedPreferences(
                SettingsFragment.PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        String v = preferences.getString("select_engine","0");
        return Integer.parseInt(v);
        */

    }

    public DriverBase getEngine() {
        return engine;
    }

    /*
    public void setSonivoxEngine(MidiDriver.OnMidiStartListener midiStartListener){
        engine = new MidiDriver();
        ((MidiDriver)engine).setOnMidiStartListener(midiStartListener);

    }
    */

    public void setFluidSynthEngine() {
        String path = context.getApplicationContext().getDir("flutter", Context.MODE_PRIVATE).getPath();
        String sfPath = path + "/soundfont_GM.sf2";
        Log.i("MidiBridge","setFluidSynthEngine sfPath=" + sfPath);

        engine = new FluidSynthDriver();


        boolean hasLowLatencyFeature = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);
        boolean hasProFeature = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO);
        Log.i("MidiBridge","hasLowLatencyFeature=" + hasLowLatencyFeature + " hasProFeature=" + hasProFeature);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            AudioManager myAudioMgr = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            String sampleRateStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            int defaultSampleRate = Integer.parseInt(sampleRateStr);
            String framesPerBurstStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
            int defaultFramesPerBurst = Integer.parseInt(framesPerBurstStr);
            Log.i("MidiBridge","setting default stream values: sampleRate=" + defaultSampleRate + " framesPerBurst=" + defaultFramesPerBurst);

            ((FluidSynthDriver)engine).setDefaultStreamValues(defaultSampleRate, defaultFramesPerBurst);
        }


        engine.init();
        ((FluidSynthDriver)engine).setSF2(sfPath);
    }

    public void setKyoEngine(){
        /*
        try {
            SF2Soundbank sf = new SF2Soundbank(context.getAssets().open("GeneralUser GS v1.47.sf2")); //soundFont con soli due strumenti: Piano e WoodBlock
            SF2Instrument[] instruments = sf.getInstruments();
            String[] s_instruments = new String[instruments.length];
            for (int i=0; i<instruments.length; i++ ) {
                s_instruments[i] = instruments[i].getName();
            }
            ((SheetMusicActivity)context).setSpinnerInstrumentsArray(s_instruments);

            kyoSynth = new SoftSynthesizer();
            kyoSynth.open();
            kyoSynth.loadAllInstruments(sf);
            kyoSynth.getChannels()[0].programChange(0); //imposta Piano
            engine = kyoSynth;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
        */

    }



    public void write(byte msg[]){
        //Log.w("MidiBridge", "writing message to Synth engine... "+ CommonResources.bytesToHex(msg));
        if(engine == null){
            Log.e("MidiBridge", "write: Engine not SET :( aborting..");
            return;
        }

        engine.write(msg);

        /*
        else if( engine == kyoSynth){
            try {
                ShortMessage smsg = new ShortMessage();

                int d2 = 0;
                if (msg.length > 2){
                    d2 = msg[2];
                }
                smsg.setMessage(msg[0], msg[1], d2);

                kyoSynth.getReceiver().send(smsg,-1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
         */
    }

    public int[] config(){
        if(engine == null){
            Log.e("MidiBridge", "write: Engine not SET :( aborting..");
            return null;
        }
        return engine.config();
    }

    public void stop(){
        if(engine == null){
            Log.e("MidiBridge", "write: Engine not SET :( aborting..");
            return;
        }
        engine.stop();
        wakeLock.release();

        return;
    }

    public void start(){
        if(engine == null){
            Log.e("MidiBridge", "write: Engine not SET :( aborting..");
            return;
        }
        engine.start();
        return;
    }
}
