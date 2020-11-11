package net.reichholf.repola.views.models;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import net.reichholf.repola.AppInfo;
import net.reichholf.repola.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ApplicationViewModel extends AndroidViewModel implements Utils.ApplicationListReadyHandler {
	private static MutableLiveData<List<AppInfo>> sApplications = null;
	private AsyncTask mTask;

	private class AsyncGetApps extends AsyncTask<Void, Void, Void> {
		Utils.ApplicationListReadyHandler mReadyHandler;
		Context mContext;
		ArrayList<AppInfo> mApplications;

		public AsyncGetApps(Context context, Utils.ApplicationListReadyHandler readyHandler) {
			mReadyHandler = readyHandler;
			mContext = context;
			mApplications = new ArrayList<>();
			mTask = null;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			mApplications.clear();
			PackageManager packageManager = mContext.getPackageManager();

			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			List<ResolveInfo> intentActivities = packageManager.queryIntentActivities(mainIntent, 0);
			for (ResolveInfo resolveInfo : intentActivities) {
				mApplications.add(new AppInfo(packageManager, resolveInfo));
			}

			Collections.sort(mApplications, (lhs, rhs) -> lhs.getName().compareToIgnoreCase(rhs.getName()));
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			mReadyHandler.onApplicationListReady(mApplications);
		}
	}

	public ApplicationViewModel(@NonNull Application application) {
		super(application);
	}

	public LiveData<List<AppInfo>> getApplications() {
		if (sApplications == null) {
			sApplications = new MutableLiveData<>();
			loadApplications();
		}
		return sApplications;
	}

	private void loadApplications() {
		if (mTask != null) {
			mTask.cancel(true);
			mTask = null;
		}
		mTask = new AsyncGetApps(getApplication(), this).execute();
	}

	@Override
	public void onApplicationListReady(ArrayList<AppInfo> apps) {
		sApplications.setValue(apps);
	}
}
