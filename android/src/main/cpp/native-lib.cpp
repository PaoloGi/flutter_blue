#include <jni.h>
#include <string>
#include <fluidsynth.h>
#include <unistd.h>
#include <android/log.h>

#define APPNAME "recorder"

static fluid_settings_t *settings = NULL;
static fluid_synth_t * synth = NULL;
static fluid_audio_driver_t * adriver = NULL;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    __android_log_print(ANDROID_LOG_INFO, APPNAME, "JNI_OnLoad");

    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT bool JNICALL Java_org_billthefarmer_mididriver_FluidSynthDriver_init(JNIEnv* env, jobject) {
    __android_log_print(ANDROID_LOG_INFO, APPNAME, "fluid_synth init");

    // Setup synthesizer
    if( settings == NULL){
        settings = new_fluid_settings();

        int res = fluid_settings_setint(settings, "audio.period-size", 64);
        __android_log_print(ANDROID_LOG_INFO, APPNAME, "set  audio.period-size res=%d",res);

        res = fluid_settings_setint(settings, "audio.periods", 2);
        __android_log_print(ANDROID_LOG_INFO, APPNAME, "set  audio.periods res=%d",res);

        res = fluid_settings_setint(settings, "synth.polyphony", 2);
        __android_log_print(ANDROID_LOG_INFO, APPNAME, "set synth.polyphony res=%d",res);


    }
    if(synth == NULL) {
        synth = new_fluid_synth(settings);
    }
    if (adriver == NULL) {
        adriver = new_fluid_audio_driver(settings, synth);
    }
    return true;
}

extern "C" JNIEXPORT jboolean JNICALL Java_org_billthefarmer_mididriver_FluidSynthDriver_setSF2(JNIEnv* env, jobject, jstring jSoundfontPath) {
    const char* soundfontPath = env->GetStringUTFChars(jSoundfontPath, nullptr);
    env->ReleaseStringUTFChars(jSoundfontPath, soundfontPath);
    // Load sample soundfont
    int ret = fluid_synth_sfload(synth, soundfontPath, 1);
    __android_log_print(ANDROID_LOG_INFO, APPNAME, "fluid_synth_sfload path=%s synth=%x audiodriver=%x ret=%d",soundfontPath, synth,adriver,ret);
    return true;
}

extern "C" JNIEXPORT bool JNICALL Java_org_billthefarmer_mididriver_FluidSynthDriver_write(JNIEnv* env, jobject, jbyteArray array) {
    static const unsigned char MIDI_CMD_NOTE_OFF = 0x80;
    static const unsigned char MIDI_CMD_NOTE_ON = 0x90;
    static const unsigned char MIDI_CMD_NOTE_PRESSURE = 0xa0; //polyphonic key pressure
    static const unsigned char MIDI_CMD_CONTROL = 0xb0; //control change CC
    static const unsigned char MIDI_CMD_PGM_CHANGE = 0xc0;
    static const unsigned char MIDI_CMD_CHANNEL_PRESSURE = 0xd0;
    static const unsigned char MIDI_CMD_BENDER = 0xe0;

    jsize len = env->GetArrayLength(array);
    jbyte *body = env->GetByteArrayElements(array, 0);
    int cmd = body[0];
    int d1 = body[1];
    int d2 = -1;
    if(len>2)
        d2 = body[2];
    int status = cmd & 0xf0;
    int ch = cmd & 0x0f;
    __android_log_print(ANDROID_LOG_INFO, APPNAME, "FluidSynthDriver_write received %d bytes cmd=%02x (status=%02x ch=%d) d1=%d d2=%d adriver=%x synth=%x", len, cmd,status,ch,d1,d2,adriver, synth);

    switch (status){
        case MIDI_CMD_NOTE_OFF:
            //__android_log_print(ANDROID_LOG_INFO, APPNAME, "FluidSynthDriver_sending Note OFF !");
            fluid_synth_noteoff(synth, ch, d1);break;
        case MIDI_CMD_NOTE_ON:
            //__android_log_print(ANDROID_LOG_INFO, APPNAME, "FluidSynthDriver_sending Note ON !");
            fluid_synth_noteon(synth, ch, d1, d2);break;
        case MIDI_CMD_NOTE_PRESSURE:
            __android_log_print(ANDROID_LOG_INFO, APPNAME, "FluidSynthDriver_sending Note PRESSURE !");
            fluid_synth_key_pressure(synth, ch, d1, d2);break;
        case MIDI_CMD_CONTROL:
            __android_log_print(ANDROID_LOG_INFO, APPNAME, "FluidSynthDriver_sending Control Change Command!");
            fluid_synth_cc(synth, ch, d1, d2);break;
        case MIDI_CMD_PGM_CHANGE:
            __android_log_print(ANDROID_LOG_INFO, APPNAME, "FluidSynthDriver_sending Program Change Command!");
            fluid_synth_program_change(synth, ch, d1);break;
        case MIDI_CMD_CHANNEL_PRESSURE:
             __android_log_print(ANDROID_LOG_INFO, APPNAME, "FluidSynthDriver_sending Channel Pressure Command!");
           fluid_synth_channel_pressure(synth, ch, d1);break;
        case MIDI_CMD_BENDER:
             __android_log_print(ANDROID_LOG_INFO, APPNAME, "FluidSynthDriver_sending Pitch Bend Command!");
           fluid_synth_pitch_bend(synth, ch, d1);break;
    }
    return true;
}


extern "C" JNIEXPORT void JNICALL Java_org_billthefarmer_mididriver_FluidSynthDriver_shutdown(JNIEnv* env, jobject) {
    __android_log_print(ANDROID_LOG_INFO, APPNAME, "FluidSynthDriver shutdown");
    // Clean up
    delete_fluid_audio_driver(adriver);
    delete_fluid_synth(synth);
    delete_fluid_settings(settings);
}