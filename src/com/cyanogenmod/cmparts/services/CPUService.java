package com.cyanogenmod.cmparts.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.io.File;

import com.cyanogenmod.cmparts.activities.CPUActivity;

public class CPUService extends Service {

	private static final String TAG = "CPUSettings";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {

		SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (mPrefs.getBoolean(CPUActivity.DIRTY_FLAG, true) == true) {
			Log.i(TAG, "Restore disabled by unclean shutdown.");
			stopSelf();
			return;
		}

		if (mPrefs.getBoolean(CPUActivity.SOB_PREF, false) == false) {
			Log.i(TAG, "Restore disabled by user preference.");
			stopSelf();
			return;
		}

		File mNoCPUd = new File("/data/.nocpu");
		File mNoCPUs = new File("/sd-ext/.nocpu");
		if (mNoCPUd.exists() || mNoCPUs.exists()) {
			Log.i(TAG, "Restore disabled by user file.");
			stopSelf();
			return;
		}

		String mGov = mPrefs.getString(CPUActivity.GOV_PREF, null);
		String mMinFreq = mPrefs.getString(CPUActivity.MIN_FREQ_PREF, null);
		String mMaxFreq = mPrefs.getString(CPUActivity.MAX_FREQ_PREF, null);
		boolean noSettings = (mGov == null) && (mMinFreq == null) && (mMaxFreq == null);

		if (noSettings) {
			Log.i(TAG, "No settings saved. Nothing to restore.");
		} else {
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putBoolean(CPUActivity.DIRTY_FLAG, true);
			editor.commit();

			List GovLst = Arrays.asList(CPUActivity.readOneLine(CPUActivity.GOVERNORS_LIST_FILE).split(" "));
			List FreqLst = Arrays.asList(CPUActivity.readOneLine(CPUActivity.FREQ_LIST_FILE).split(" "));
			if (mGov != null && GovLst.contains(mGov)) {
				CPUActivity.writeOneLine(CPUActivity.GOVERNOR, mGov);
			}
			if (mMaxFreq != null && FreqLst.contains(mMaxFreq)) {
				CPUActivity.writeOneLine(CPUActivity.FREQ_MAX_FILE, mMaxFreq);
			}
			if (mMinFreq != null && FreqLst.contains(mMinFreq)) {
				CPUActivity.writeOneLine(CPUActivity.FREQ_MIN_FILE, mMinFreq);
			}
			Log.i(TAG, "CPU Settings restored.");
			Toast.makeText(this, "CPU Settigns restored", Toast.LENGTH_LONG).show();
		}
		stopSelf();
		return;
	}
}
