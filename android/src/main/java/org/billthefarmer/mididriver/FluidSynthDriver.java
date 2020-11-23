package org.billthefarmer.mididriver;
import android.util.Log;

public class FluidSynthDriver extends DriverBase
{
    static
    {
        Log.i("FluidSynthDriver","loading native-lib");
        System.loadLibrary("native-lib"); //fluidSynth

    }

    public FluidSynthDriver()
    {
    }

    public void start()
    {
        Log.i("FluidSynthDriver","start() invoked");
        init();
    }

    public void stop()
    {
        Log.i("FluidSynthDriver","stop() invoked");
    }

    public native boolean init();

    public native boolean write(byte a[]);
    public native boolean setSF2(String path);

    public native void setDefaultStreamValues(int defaultSampleRate, int defaultFramesPerBurst);

}